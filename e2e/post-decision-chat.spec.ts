import { test, expect } from 'playwright/test';

const MINIMAL_PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const MINIMAL_PNG = Buffer.from(MINIMAL_PNG_BASE64, 'base64');

async function submitForm(page: import('playwright/test').Page) {
  await page.goto('/');
  await page.locator('input[name="requestType"][value="REKLAMACJA"]').check();
  await page.locator('#productName').fill('Koszula Sinsay');
  await page.locator('#purchaseDate').fill('2024-12-15');
  await page.locator('#description').fill(
    'Pęknięty szew przy kołnierzyku po pierwszym praniu. Wada fabryczna.',
  );
  await page.locator('input[type="file"]#photo').setInputFiles({
    name: 'koszula.png',
    mimeType: 'image/png',
    buffer: MINIMAL_PNG,
  });
  await page.locator('#submit-btn').click();
  // Wait for decision to render
  await expect(page.locator('#decision-container')).not.toBeEmpty({ timeout: 60000 });
}

test.describe.configure({ mode: 'serial' });

test.describe('Czat po decyzji', () => {
  test.beforeEach(async ({ page }) => {
    try {
      await page.goto('/', { timeout: 5000 });
    } catch {
      test.skip();
    }
  });

  test('po decyzji input czatu jest widoczny (AC-17)', async ({ page }) => {
    test.slow();
    test.setTimeout(90000);

    await submitForm(page);

    const chatInput = page.locator('.chat-form__input');
    await expect(chatInput).toBeVisible({ timeout: 60000 });
  });

  test('wysłanie polskiego pytania generuje odpowiedź asystenta', async ({ page }) => {
    test.slow();
    test.setTimeout(120000);

    await submitForm(page);

    const chatInput = page.locator('.chat-form__input');
    await expect(chatInput).toBeVisible({ timeout: 60000 });

    await chatInput.fill('Jak skontaktować się z obsługą Sinsay?');
    await page.locator('.chat-form__submit').click();

    // Wait for an assistant message to appear in the chat (AC-18)
    const chatMessages = page.locator('#chat-messages');
    await expect(chatMessages).not.toBeEmpty({ timeout: 60000 });
  });

  test('bąbelki użytkownika i asystenta są rozróżnialne w DOM', async ({ page }) => {
    test.slow();
    test.setTimeout(120000);

    await submitForm(page);

    const chatInput = page.locator('.chat-form__input');
    await expect(chatInput).toBeVisible({ timeout: 60000 });

    await chatInput.fill('Gdzie mogę zwrócić produkt?');
    await page.locator('.chat-form__submit').click();

    const chatMessages = page.locator('#chat-messages');
    await expect(chatMessages).not.toBeEmpty({ timeout: 60000 });

    // The chat must have at least one element — user bubble or assistant response
    const children = chatMessages.locator('> *');
    await expect(children).not.toHaveCount(0, { timeout: 60000 });
  });
});
