import cx from "classnames";
import { expect, test } from "playwright/test";

test("app loads correctly", async ({ page }) => {
  await page.goto("/");

  // Renders header
  await expect(page.getByRole("heading", { name: "RSVP" })).toBeVisible();
  await expect(
    page.getByRole("heading", { name: "Policy Verification" }),
  ).toBeVisible();
  await expect(page.getByRole("button", { name: "Verify" })).toBeEnabled();

  // Uploads drawer should be open
  await expect(page.locator(".drawer-left")).toHaveClass(
    cx("drawer", "drawer-left", "expanded"),
  );
  await expect(page.locator(".drawer-left > .drawer-container")).toBeVisible();
  await expect(page.locator(".drawer-left .drawer-tab-icon")).toHaveAttribute(
    "data-icon",
    "caret-left",
  );

  // Reports drawer should be closed
  await expect(page.locator(".drawer-right")).toHaveClass(
    cx("drawer", "drawer-right"),
  );
  await expect(
    page.locator(".drawer-right > .drawer-container"),
  ).toBeAttached();
  await expect(page.locator(".drawer-right > .drawer-container")).toBeHidden();
  await expect(page.locator(".drawer-right .drawer-tab-icon")).toHaveAttribute(
    "data-icon",
    "caret-left",
  );
});
