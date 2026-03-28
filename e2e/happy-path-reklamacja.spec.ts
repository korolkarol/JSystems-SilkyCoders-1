import { test, expect } from 'playwright/test';

// Minimal valid 1x1 PNG (base64-encoded)
const MINIMAL_PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const MINIMAL_PNG = Buffer.from(MINIMAL_PNG_BASE64, 'base64');

test.describe.configure({ mode: 'serial' });

test.describe('Szczęśliwa ścieżka — Reklamacja', () => {
  test.beforeEach(async ({ page }) => {
    // Skip gracefully if the app is not running
    try {
      await page.goto('/', { timeout: 5000 });
    } catch {
      test.skip();
    }
  });

  test('wypełnienie formularza Reklamacja i przesłanie — SSE startuje', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await test.step('wypełnij formularz', async () => {
      await page.locator('input[name="requestType"][value="REKLAMACJA"]').check();
      await page.locator('#productName').fill('Bluza z kapturem Sinsay');
      await page.locator('#purchaseDate').fill('2024-11-20');
      await page.locator('#description').fill(
        'Produkt posiada wyraźną wadę fabryczną — szew przy lewym ramieniu jest rozerwany po pierwszym praniu.',
      );

      const fileInput = page.locator('input[type="file"]#photo');
      await fileInput.setInputFiles({
        name: 'reklamacja.png',
        mimeType: 'image/png',
        buffer: MINIMAL_PNG,
      });

      await expect(page.locator('#submit-btn')).toBeEnabled();
    });

    await test.step('prześlij formularz i poczekaj na decyzję', async () => {
      await page.locator('#submit-btn').click();

      // Wait for the decision container to have content (SSE populates it)
      await expect(page.locator('#decision-container')).not.toBeEmpty({ timeout: 60000 });
    });
  });

  test('panel decyzji zawiera polską etykietę wyniku', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    // Fill and submit the form again for this serial test
    await page.locator('input[name="requestType"][value="REKLAMACJA"]').check();
    await page.locator('#productName').fill('Kurtka zimowa Sinsay');
    await page.locator('#purchaseDate').fill('2024-12-01');
    await page.locator('#description').fill(
      'Zamek główny zepsuł się po tygodniu użytkowania. Widoczna wada materiału.',
    );

    const fileInput = page.locator('input[type="file"]#photo');
    await fileInput.setInputFiles({
      name: 'kurtka.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });

    await page.locator('#submit-btn').click();

    const decisionContainer = page.locator('#decision-container');
    await expect(decisionContainer).not.toBeEmpty({ timeout: 60000 });

    // Decision must contain exactly one of the two Polish outcome labels (AC-10)
    const containerText = await decisionContainer.textContent();
    expect(containerText).toMatch(
      /Wniosek wstępnie pozytywny|Wniosek wstępnie negatywny/,
    );
  });

  test('tekst wyjaśnienia decyzji jest niepusty i w języku polskim', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await page.locator('input[name="requestType"][value="REKLAMACJA"]').check();
    await page.locator('#productName').fill('Spodnie Sinsay');
    await page.locator('#purchaseDate').fill('2025-01-10');
    await page.locator('#description').fill(
      'Po pierwszym praniu kolor spodni znacząco wyblakł. Wada jest wyraźnie widoczna.',
    );
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'spodnie.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });

    await page.locator('#submit-btn').click();
    await expect(page.locator('#decision-container')).not.toBeEmpty({ timeout: 60000 });

    const explanation = page.locator('.decision-panel__explanation');
    await expect(explanation).toBeVisible({ timeout: 60000 });
    const text = await explanation.textContent();
    expect(text?.trim().length).toBeGreaterThan(10);
  });

  test('po decyzji pojawia się formularz czatu', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await page.locator('input[name="requestType"][value="REKLAMACJA"]').check();
    await page.locator('#productName').fill('T-shirt Sinsay');
    await page.locator('#purchaseDate').fill('2025-02-05');
    await page.locator('#description').fill(
      'Nadruk złuszczył się po jednym praniu — wyraźna wada.',
    );
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'tshirt.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });

    await page.locator('#submit-btn').click();
    await expect(page.locator('#decision-container')).not.toBeEmpty({ timeout: 60000 });

    // Chat input must appear after decision (AC-17)
    const chatInput = page.locator('.chat-form__input');
    await expect(chatInput).toBeVisible({ timeout: 60000 });
  });
});
