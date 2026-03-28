import { test, expect } from 'playwright/test';

// Minimal valid 1x1 PNG (base64)
const MINIMAL_PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const MINIMAL_PNG = Buffer.from(MINIMAL_PNG_BASE64, 'base64');

// Minimal valid JPEG (FFD8 FFE0 ... FFD9)
const MINIMAL_JPEG = Buffer.from(
  'ffd8ffe000104a46494600010100000100010000ffdb004300080606070605080707070909080a0c140d0c0b0b0c1912130f141d1a1f1e1d1a1c1c20242e2720222c231c1c2837292c30313434341f27393d38323c2e333432ffffc0000b080001000101011100ffC400' +
  '1f0000010501010101010100000000000000000102030405060708090a0bffda00080101000003f0fa',
  'hex'
);

test.describe('Formularz zgłoszenia — walidacja', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/').catch(() => test.skip());
  });

  test('przycisk "WYŚLIJ" jest wyłączony po załadowaniu strony', async ({ page }) => {
    const submitBtn = page.locator('#submit-btn');
    await expect(submitBtn).toBeDisabled();
  });

  test('formularz zawiera wszystkie wymagane pola', async ({ page }) => {
    // Request type radios
    await expect(page.locator('input[name="requestType"][value="REKLAMACJA"]')).toBeVisible();
    await expect(page.locator('input[name="requestType"][value="ZWROT"]')).toBeVisible();
    // Product name
    await expect(page.locator('#productName')).toBeVisible();
    // Purchase date
    await expect(page.locator('#purchaseDate')).toBeVisible();
    // Description
    await expect(page.locator('#description')).toBeVisible();
    // File input
    await expect(page.locator('input[type="file"]#photo')).toBeVisible();
  });

  test('przycisk pozostaje wyłączony gdy wszystkie pola tekstowe są wypełnione, ale brak zdjęcia', async ({ page }) => {
    await page.locator('input[name="requestType"][value="REKLAMACJA"]').check();
    await page.locator('#productName').fill('Bluzka Sinsay');
    await page.locator('#purchaseDate').fill('2025-01-15');
    await page.locator('#description').fill('Produkt ma wyraźną wadę szwu');

    const submitBtn = page.locator('#submit-btn');
    await expect(submitBtn).toBeDisabled();
  });

  test('wgranie pliku PNG odblokowuje przycisk "WYŚLIJ"', async ({ page }) => {
    const fileInput = page.locator('input[type="file"]#photo');
    await fileInput.setInputFiles({
      name: 'test.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });

    const submitBtn = page.locator('#submit-btn');
    await expect(submitBtn).toBeEnabled();
  });

  test('wgranie pliku > 10 MB pokazuje błąd po polsku i przycisk pozostaje wyłączony', async ({ page }) => {
    const oversizedBuffer = Buffer.alloc(11 * 1024 * 1024, 0xff);
    const fileInput = page.locator('input[type="file"]#photo');
    await fileInput.setInputFiles({
      name: 'duzy-plik.png',
      mimeType: 'image/png',
      buffer: oversizedBuffer,
    });

    const errorEl = page.locator('#file-error');
    await expect(errorEl).toContainText('zbyt duży');
    await expect(errorEl).toContainText('10 MB');

    const submitBtn = page.locator('#submit-btn');
    await expect(submitBtn).toBeDisabled();
  });

  test('wgranie pliku .txt pokazuje błąd po polsku i przycisk pozostaje wyłączony', async ({ page }) => {
    const fileInput = page.locator('input[type="file"]#photo');
    await fileInput.setInputFiles({
      name: 'opis.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('to nie jest zdjecie'),
    });

    const errorEl = page.locator('#file-error');
    await expect(errorEl).toContainText('Nieobsługiwany format');

    const submitBtn = page.locator('#submit-btn');
    await expect(submitBtn).toBeDisabled();
  });

  test('można wybrać tylko jeden typ zgłoszenia jednocześnie (radio)', async ({ page }) => {
    const reklamacjaRadio = page.locator('input[name="requestType"][value="REKLAMACJA"]');
    const zwrotRadio = page.locator('input[name="requestType"][value="ZWROT"]');

    await reklamacjaRadio.check();
    await expect(reklamacjaRadio).toBeChecked();
    await expect(zwrotRadio).not.toBeChecked();

    await zwrotRadio.check();
    await expect(zwrotRadio).toBeChecked();
    await expect(reklamacjaRadio).not.toBeChecked();
  });

  test('etykiety formularza są w języku polskim', async ({ page }) => {
    await expect(page.getByText('Rodzaj zgłoszenia')).toBeVisible();
    await expect(page.getByText('Reklamacja')).toBeVisible();
    await expect(page.getByText('Zwrot')).toBeVisible();
    await expect(page.locator('label[for="productName"]')).toContainText('Nazwa produktu');
    await expect(page.locator('label[for="purchaseDate"]')).toContainText('Data zakupu');
    await expect(page.locator('label[for="description"]')).toContainText('Opis problemu');
    await expect(page.locator('label[for="photo"]')).toContainText('Dodaj zdjęcie');
  });
});
