import path from "path";
import { expect, test } from "playwright/test";
import { ServerMock } from "./ServerMock";

// TODO:
//   - expand/collapse all files
//   - expand/collapse single file
//   - expand/collapse versioned file
//   - select versions
//   - compare versions
//   - scroll report panel
//   - partial line report render/highlight
//   - syntax highlighting

// Mock the server
test.beforeEach(async ({ page }) => {
  const server = new ServerMock();
  await page.route("/api/upload", server.upload());
  await page.route("/api/file/*", server.file());
  await page.route("/api/verify", server.verify());

  await page.goto("/");

  await page.getByRole("button", { name: "Create policy set" }).click();
  await page.getByRole("button", { name: "Create" }).click();

  await page
    .locator(".drop-zone > input")
    .setInputFiles([
      path.join(__dirname, "resources", "policy.cedar"),
      path.join(__dirname, "resources", "schema.cedarschema"),
      path.join(__dirname, "resources", "entities.json"),
      path.join(__dirname, "resources", "test.invariant"),
    ]);

  const fileChooserPromise = page.waitForEvent("filechooser");
  await page.locator(".uploaded-file-version-icon").click();

  const fileChooser = await fileChooserPromise;
  await fileChooser.setFiles([
    path.join(__dirname, "resources", "policy(2).cedar"),
  ]);

  await page.getByRole("button", { name: "Verify" }).click();
});

test.describe("reports", () => {
  test("renders reports", async ({ page }) => {
    const files = page.locator(".source-file.expanded");
    await expect(files).toHaveCount(4);

    const entities = page.locator(".source-file.expanded").first();

    // entities.json has 2 reports (1-line info, 5-line warning)
    await expect(
      entities.locator(".source-file-line-content.source-report"),
    ).toHaveCount(6);

    // Reports exist
    await expect(
      entities.locator(".source-file-line-content.source-report-info"),
    ).toHaveCount(1);

    await expect(
      entities.locator(".source-file-line-content.source-report-warn"),
    ).toHaveCount(5);

    // Reports not selected
    await expect(
      entities.locator(".source-file-line-content.selected-info"),
    ).toHaveCount(0);

    await expect(
      entities.locator(".source-file-line-content.selected-warn"),
    ).toHaveCount(0);

    // Reports not hovered
    await expect(
      entities.locator(".source-file-line-content.hovered-info"),
    ).toHaveCount(0);

    await expect(
      entities.locator(".source-file-line-content.hovered-warn"),
    ).toHaveCount(0);
  });

  test("highlights on hover", async ({ page }) => {
    const entities = page.locator(".source-file.expanded").first();

    // Hover over info report
    await entities
      .locator(".source-file-line-content.source-report-info")
      .hover();

    // Reports not selected
    await expect(
      entities.locator(".source-file-line-content.selected-info"),
    ).toHaveCount(0);

    await expect(
      entities.locator(".source-file-line-content.selected-warn"),
    ).toHaveCount(0);

    // Info report hovered
    await expect(
      entities.locator(".source-file-line-content.hovered-info"),
    ).toHaveCount(1);

    await expect(
      entities.locator(".source-file-line-content.hovered-warn"),
    ).toHaveCount(0);
  });

  // TODO:
  // - hovers correct source location
  // - hovers only single source location on selection

  test("highlights on selection", async ({ page }) => {
    const entities = page.locator(".source-file.expanded").first();

    // Select info report
    await entities
      .locator(".source-file-line-content.source-report-info")
      .click();

    // Info report selected but also hovered
    await expect(
      entities.locator(".source-file-line-content.selected-info"),
    ).toHaveCount(0);

    await expect(
      entities.locator(".source-file-line-content.selected-warn"),
    ).toHaveCount(0);

    // Info report hovered
    await expect(
      entities.locator(".source-file-line-content.hovered-info"),
    ).toHaveCount(1);

    await expect(
      entities.locator(".source-file-line-content.hovered-warn"),
    ).toHaveCount(0);

    // Move mouse to other report
    await entities
      .locator(".source-file-line-content.source-report-warn")
      .first()
      .hover();

    // Info report selected and not hovered
    await expect(
      entities.locator(".source-file-line-content.selected-info"),
    ).toHaveCount(1);

    await expect(
      entities.locator(".source-file-line-content.selected-warn"),
    ).toHaveCount(0);

    // Warn report hovered
    await expect(
      entities.locator(".source-file-line-content.hovered-info"),
    ).toHaveCount(0);

    await expect(
      entities.locator(".source-file-line-content.hovered-warn"),
    ).toHaveCount(5); // 5 line report
  });
});
