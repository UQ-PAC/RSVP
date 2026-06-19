import cx from "classnames";
import path from "path";
import { expect, test } from "playwright/test";
import { ServerMock } from "./ServerMock";

// TODO:
// - click file to scroll
// - click file to open tab (+tab scrolls into view)
// - click policy set to scroll

// Mock the server
test.beforeEach(async ({ page }) => {
  const server = new ServerMock();
  await page.route("/api/upload", server.upload());
  await page.route("/api/file/*", server.file());
});

test("open create policy set form", async ({ page }) => {
  await page.goto("/");

  const create = page.getByRole("button", { name: "Create policy set" });
  await expect(create).toBeEnabled();
  await create.click();

  // Create policy set button hidden when form displayed
  await expect(create).toBeHidden();

  // Text input should be focused
  const input = page.getByPlaceholder("Policy Set 1");
  await expect(input).toBeVisible();
  await expect(input).toBeFocused();

  // Create button should be enabled
  const doCreate = page.getByRole("button", { name: "Create" });
  await expect(doCreate).toBeVisible();
  await expect(doCreate).toBeEnabled();

  // Cancel button should be enabled
  const doCancel = page.getByRole("button", { name: "Cancel" });
  await expect(doCancel).toBeVisible();
  await expect(doCancel).toBeEnabled();
});

test.describe("add/remove policy set", () => {
  // Open policy set form
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await page.getByRole("button", { name: "Create policy set" }).click();
  });

  test("cancel policy set creation", async ({ page }) => {
    const doCancel = page.getByRole("button", { name: "Cancel" });

    // Cancel form
    await doCancel.click();

    await expect(page.getByPlaceholder("Policy Set 1")).toBeHidden();
    await expect(doCancel).toBeHidden();

    await expect(
      page.getByRole("button", { name: "Create policy set" }),
    ).toBeVisible();
  });

  test("create policy set", async ({ page }) => {
    await page.getByRole("button", { name: "Create" }).click();

    await expect(page.locator(".analysis-group")).toHaveCount(1);
    const analysisGroup = page.locator(".analysis-group").first();
    await expect(analysisGroup).toBeVisible();

    await expect(
      page.getByRole("heading", { name: "Policy set 1" }),
    ).toBeVisible();
    await expect(page.locator(".drop-zone > input")).toHaveCount(1);
  });

  test("create policy set with return", async ({ page }) => {
    await page.keyboard.press("Enter");

    await expect(page.locator(".analysis-group")).toHaveCount(1);

    await expect(
      page.getByRole("heading", { name: "Policy set 1" }),
    ).toBeVisible();
  });

  test("create named policy set", async ({ page }) => {
    await page.keyboard.type("My Policy Set");
    await page.getByRole("button", { name: "Create" }).click();

    await expect(page.locator(".analysis-group")).toHaveCount(1);

    await expect(
      page.getByRole("heading", { name: "My Policy Set" }),
    ).toBeVisible();
  });

  test("create named policy set with return", async ({ page }) => {
    await page.keyboard.type("Another Policy Set");
    await page.keyboard.press("Enter");

    await expect(page.locator(".analysis-group")).toHaveCount(1);

    await expect(
      page.getByRole("heading", { name: "Another Policy Set" }),
    ).toBeVisible();
  });

  test("create multiple policy sets", async ({ page }) => {
    const doCreate = page.getByRole("button", { name: "Create" });
    await doCreate.click();

    await expect(page.locator(".analysis-group")).toHaveCount(1);
    await expect(
      page.getByRole("heading", { name: "Policy set 1" }),
    ).toBeVisible();

    const openForm = page.getByRole("button", { name: "Create policy set" });
    await openForm.click();
    await doCreate.click();

    await expect(page.locator(".analysis-group")).toHaveCount(2);
    await expect(
      page.getByRole("heading", { name: "Policy set 2" }),
    ).toBeVisible();

    await openForm.click();
    await doCreate.click();

    await expect(page.locator(".analysis-group")).toHaveCount(3);
    await expect(
      page.getByRole("heading", { name: "Policy set 3" }),
    ).toBeVisible();
  });

  test("delete policy set", async ({ page }) => {
    await page.getByRole("button", { name: "Create" }).click();

    const deleteButton = page.locator(".analysis-group-delete-icon");
    expect(deleteButton).toHaveCount(1);
    expect(deleteButton.first()).toBeEnabled();

    await deleteButton.click();

    await expect(page.locator(".analysis-group")).toHaveCount(0);
    await expect(deleteButton).toHaveCount(0);
  });

  test("detect duplicate name", async ({ page }) => {
    await page.getByRole("button", { name: "Create" }).click();
    await page.getByRole("button", { name: "Create policy set" }).click();

    const input = page.getByPlaceholder("Policy Set 2");
    await expect(input).toBeVisible();

    // Enter duplicate name
    await page.keyboard.type("policy set 1");

    // Display error message & disable create button
    await expect(input).toHaveClass(
      cx("create-analysis-group-text-input", "invalid"),
    );
    await expect(page.getByText("Policy set already exists")).toBeAttached();
    await expect(page.getByRole("button", { name: "Create" })).toBeDisabled();

    await page.keyboard.press("Backspace");

    // Remove error message & enable create button
    await expect(input).toHaveClass("create-analysis-group-text-input");
    await expect(
      page.getByText("Policy set already exists"),
    ).not.toBeAttached();
    await expect(page.getByRole("button", { name: "Create" })).toBeEnabled();
  });
});

test.describe("modify policy set", () => {
  // TODO:
  // - drag-and-drop
  // - delete file versions

  test.beforeEach(async ({ page }) => {
    // Create policy set
    await page.goto("/");
    await page.getByRole("button", { name: "Create policy set" }).click();
    await page.getByRole("button", { name: "Create" }).click();

    await page
      .locator(".drop-zone > input")
      .setInputFiles([path.join(__dirname, "resources", "policy.cedar")]);
  });

  test("add files", async ({ page }) => {
    const dropZone = page.locator(".drop-zone");
    await expect(dropZone).toHaveCount(1);

    // Upload one file
    dropZone.click();
    let fileChooser = await page.waitForEvent("filechooser");
    expect(fileChooser.isMultiple()).toBeTruthy();
    await fileChooser.setFiles(
      path.join(__dirname, "resources", "schema.cedarschema"),
    );

    await expect(page.locator(".uploaded-file")).toHaveCount(2);

    // Upload two files
    dropZone.click();
    fileChooser = await page.waitForEvent("filechooser");
    await fileChooser.setFiles([
      path.join(__dirname, "resources", "entities.json"),
      path.join(__dirname, "resources", "test.invariant"),
    ]);

    await expect(page.locator(".uploaded-file")).toHaveCount(4);

    // Check icons
    const analysisGroup = page.locator(".analysis-group");
    await expect(
      analysisGroup
        .getByText("policy.cedar")
        .locator(".uploaded-filetype-icon"),
    ).toHaveAttribute("data-icon", "lock");
    await expect(
      analysisGroup
        .getByText("schema.cedarschema")
        .locator(".uploaded-filetype-icon"),
    ).toHaveAttribute("data-icon", "bars-staggered");
    await expect(
      analysisGroup
        .getByText("entities.json")
        .locator(".uploaded-filetype-icon"),
    ).toHaveAttribute("data-icon", "database");
    await expect(
      analysisGroup
        .getByText("test.invariant")
        .locator(".uploaded-filetype-icon"),
    ).toHaveAttribute("data-icon", "check-double");

    // Check only policy file has version upload icon
    await expect(
      analysisGroup.locator(".uploaded-file-version-icon"),
    ).toHaveCount(1);
    await expect(
      analysisGroup
        .locator(".uploaded-file")
        .nth(1)
        .locator(".uploaded-file-version-icon"),
    ).toHaveCount(1);

    // Check files present & expanded
    await expect(page.locator(".source-file.expanded")).toHaveCount(4);
    await expect(page.getByText("policy.cedar")).toHaveCount(2);
    await expect(page.getByText("schema.cedarschema")).toHaveCount(2);
    await expect(page.getByText("entities.json")).toHaveCount(2);
    await expect(page.getByText("test.invariant")).toHaveCount(2);
  });

  test("delete files", async ({ page }) => {
    await expect(page.locator(".uploaded-file")).toHaveCount(1);
    await expect(page.locator(".source-file")).toHaveCount(1);

    const deletefile = page.locator(".uploaded-file-delete-icon");
    await expect(deletefile).toHaveCount(1);

    await deletefile.click();

    await expect(page.locator(".uploaded-file")).toHaveCount(0);
    await expect(page.locator(".source-file")).toHaveCount(0);
  });

  test("add versions", async ({ page }) => {
    const addVersion = page.locator(".uploaded-file-version-icon");
    await expect(addVersion).toHaveCount(1);

    const fileChooserPromise = page.waitForEvent("filechooser");

    await addVersion.click();

    const fileChooser = await fileChooserPromise;
    expect(fileChooser.isMultiple()).toBeTruthy();
    await fileChooser.setFiles([
      path.join(__dirname, "resources", "policy(2).cedar"),
      path.join(__dirname, "resources", "policy(3).cedar"),
    ]);

    // Check versions added
    const files = page.locator(".uploaded-file");
    await expect(files).toHaveCount(3);

    // Check filetype icon only added to original file
    await expect(page.locator(".uploaded-filetype-icon")).toHaveCount(1);
    await expect(
      files.first().locator(".uploaded-filetype-icon"),
    ).toBeVisible();
    await expect(
      files.nth(1).locator(".uploaded-filetype-icon"),
    ).not.toBeAttached();
    await expect(
      files.last().locator(".uploaded-filetype-icon"),
    ).not.toBeAttached();

    // Check add version button only exists for original file
    await expect(addVersion).toHaveCount(1);

    // Check version indicator
    await expect(files.getByText("Version 1")).toBeVisible();
    await expect(files.getByText("Version 2")).toBeVisible();
    await expect(files.getByText("Version 3")).toBeVisible();

    // Only one source file rendered
    const source = page.locator(".source-file");
    await expect(source).toHaveCount(1);

    // Tabs rendered
    await expect(source.locator(".tab")).toHaveCount(4);
    await expect(source.getByText("Version 1")).toBeVisible();
    await expect(source.getByText("Version 2")).toBeVisible();
    await expect(source.getByText("Version 3")).toBeVisible();
    await expect(source.getByText("Compare")).toBeVisible();

    // Latest version opened
    await expect(source.locator(".tab.selected")).toHaveCount(1);
    await expect(
      source.locator(".tab.selected").getByText("Version 3"),
    ).toBeVisible();
  });

  test("detect invalid policy set", async ({ page }) => {
    // Only policy file uploaded
    const dropZone = page.locator(".drop-zone");

    await expect(dropZone).toHaveClass("drop-zone");

    // Trigger verification with incomplete set
    await page.getByRole("button", { name: "Verify" }).click();

    await expect(dropZone).toHaveClass("drop-zone error");

    await expect(
      page.getByText("At least one schema file is required"),
    ).toBeAttached();
    await expect(
      page.getByText("At least one entities file is required"),
    ).toBeAttached();
    await expect(
      page.getByText("At least one policy file is required"),
    ).not.toBeAttached();

    // Upload schema file
    await dropZone
      .locator("input")
      .setInputFiles([path.join(__dirname, "resources", "schema.cedarschema")]);

    await expect(
      page.getByText("At least one schema file is required"),
    ).not.toBeAttached();
    await expect(
      page.getByText("At least one entities file is required"),
    ).toBeAttached();
    await expect(
      page.getByText("At least one policy file is required"),
    ).not.toBeAttached();

    await expect(dropZone).toHaveClass("drop-zone error");
  });
});
