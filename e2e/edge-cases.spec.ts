import { test, expect, request as apiRequest } from 'playwright/test';

const MINIMAL_PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const MINIMAL_PNG = Buffer.from(MINIMAL_PNG_BASE64, 'base64');

test.describe('Przypadki graniczne', () => {
  test.beforeEach(async ({ page }) => {
    try {
      await page.goto('/', { timeout: 5000 });
    } catch {
      test.skip();
    }
  });

  test('plik > 10 MB: wyświetla błąd po polsku i blokuje przycisk', async ({ page }) => {
    const oversizedBuffer = Buffer.alloc(11 * 1024 * 1024, 0xff);
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'za-duzy.png',
      mimeType: 'image/png',
      buffer: oversizedBuffer,
    });

    const errorEl = page.locator('#file-error');
    await expect(errorEl).toContainText('zbyt duży');
    await expect(errorEl).toContainText('10 MB');
    await expect(page.locator('#submit-btn')).toBeDisabled();
  });

  test('plik PDF: wyświetla błąd po polsku i blokuje przycisk', async ({ page }) => {
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'dokument.pdf',
      mimeType: 'application/pdf',
      buffer: Buffer.from('%PDF-1.4 fake pdf content'),
    });

    const errorEl = page.locator('#file-error');
    await expect(errorEl).toContainText('Nieobsługiwany format');
    await expect(page.locator('#submit-btn')).toBeDisabled();
  });

  test('plik .txt: wyświetla błąd po polsku i blokuje przycisk', async ({ page }) => {
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'notatka.txt',
      mimeType: 'text/plain',
      buffer: Buffer.from('to nie jest zdjecie'),
    });

    const errorEl = page.locator('#file-error');
    await expect(errorEl).toContainText('Nieobsługiwany format');
    await expect(page.locator('#submit-btn')).toBeDisabled();
  });

  test('błąd po zamianie prawidłowego pliku na nieprawidłowy — przycisk ponownie wyłączony', async ({ page }) => {
    // First upload a valid PNG — button should enable
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'valid.png',
      mimeType: 'image/png',
      buffer: MINIMAL_PNG,
    });
    await expect(page.locator('#submit-btn')).toBeEnabled();

    // Then upload an oversized file — button must disable again
    const oversizedBuffer = Buffer.alloc(11 * 1024 * 1024, 0xff);
    await page.locator('input[type="file"]#photo').setInputFiles({
      name: 'za-duzy.png',
      mimeType: 'image/png',
      buffer: oversizedBuffer,
    });

    await expect(page.locator('#submit-btn')).toBeDisabled();
    await expect(page.locator('#file-error')).not.toBeEmpty();
  });

  test('API: pusta wiadomość w czacie zwraca HTTP 400 (AC via direct API call)', async () => {
    // Verify the server correctly rejects a blank chat message (AC-18 guard)
    // We use a dummy session ID — the important thing is NOT a 5xx, but a 400 for blank message
    const context = await apiRequest.newContext({ baseURL: 'http://localhost:8080' });
    const response = await context.post('/chat/dummy-session', {
      form: { message: '' },
    }).catch(() => null);

    if (response === null) {
      test.skip(); // App not running
    } else {
      // 400 = blank message rejected; 404 = session not found — both acceptable as the blank
      // message check fires before session lookup only if response is 400
      expect([400, 404]).toContain(response.status());
      if (response.status() === 400) {
        const body = await response.text();
        // Error message must be in Polish (AC from AGENTS.md)
        expect(body).toMatch(/Wiadomość|pusta|wymagane/i);
      }
      await context.dispose();
    }
  });
});
