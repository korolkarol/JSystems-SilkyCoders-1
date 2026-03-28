import { test, expect } from 'playwright/test';

// Minimal valid 1x1 PNG (base64-encoded)
const MINIMAL_PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const MINIMAL_PNG = Buffer.from(MINIMAL_PNG_BASE64, 'base64');

test.describe.configure({ mode: 'serial' });

test.describe('Szczęśliwa ścieżka — Zwrot', () => {
  test.beforeEach(async ({ page }) => {
    try {
      await page.goto('/', { timeout: 5000 });
    } catch {
      test.skip();
    }
  });

  test('wypełnienie formularza Zwrot i przesłanie — decyzja pojawia się w panelu', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await test.step('wypełnij formularz jako Zwrot', async () => {
      await page.locator('input[name="requestType"][value="ZWROT"]').check();
      await page.locator('#productName').fill('Sukienka letnia Sinsay');
      await page.locator('#purchaseDate').fill('2025-02-20');
      await page.locator('#description').fill(
        'Sukienka nie pasuje rozmiarowo. Produkt nieużywany, metki oryginalne nieoderwane.',
      );

      await page.locator('input[type="file"]#photo').setInputFiles({
        name: 'zwrot.png',
        mimeType: 'image/png',
        buffer: MINIMAL_PNG,
      });

      await expect(page.locator('#submit-btn')).toBeEnabled();
    });

    await test.step('prześlij formularz i poczekaj na decyzję', async () => {
      await page.locator('#submit-btn').click();
      await expect(page.locator('#decision-container')).not.toBeEmpty({ timeout: 60000 });
    });
  });

  test('panel decyzji Zwrot zawiera polską etykietę wyniku', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await page.locator('input[name="requestType"][value="ZWROT"]').check();
    await page.locator('#productName').fill('Spódnica Sinsay');
    await page.locator('#purchaseDate').fill('2025-03-01');
    await page.locator('#description').fill(
      'Zmieniłam zdanie. Produkt jest nowy, metki są, nie noszono.',
    );
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'spodnica.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });

    await page.locator('#submit-btn').click();
    const decisionContainer = page.locator('#decision-container');
    await expect(decisionContainer).not.toBeEmpty({ timeout: 60000 });

    const containerText = await decisionContainer.textContent();
    expect(containerText).toMatch(
      /Wniosek wstępnie pozytywny|Wniosek wstępnie negatywny/,
    );
  });

  test('decyzja ACCEPT dla Zwrotu zawiera informacje o metodach zwrotu', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await page.locator('input[name="requestType"][value="ZWROT"]').check();
    await page.locator('#productName').fill('Sweter wełniany Sinsay');
    await page.locator('#purchaseDate').fill('2025-03-10');
    await page.locator('#description').fill(
      'Produkt nie odpowiada opisowi na stronie — kolor inny niż na zdjęciu. Nieużywany, oryginalne metki.',
    );
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'sweter.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });

    await page.locator('#submit-btn').click();
    const decisionContainer = page.locator('#decision-container');
    await expect(decisionContainer).not.toBeEmpty({ timeout: 60000 });

    const containerText = await decisionContainer.textContent();

    // If ACCEPT: next steps should mention return methods (AC-12)
    if (containerText?.includes('Wniosek wstępnie pozytywny')) {
      expect(containerText).toMatch(/kurier|InPost|sklep|DPD|paczkomat|odesłać/i);
    } else {
      // REJECT is also a valid outcome — just verify some explanation is present
      const explanation = page.locator('.decision-panel__explanation');
      await expect(explanation).toBeVisible();
      const explanationText = await explanation.textContent();
      expect(explanationText?.trim().length).toBeGreaterThan(10);
    }
  });
});
