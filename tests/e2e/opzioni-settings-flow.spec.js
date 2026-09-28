// @ts-check
const { test, expect } = require('@playwright/test');

const UI = '/fantaasta/index.html?v=20250925ui76';

async function loginAsAdmin(page, request) {
  const init = await request.get('/fantaasta/init').then((r) => r.json());
  const admin = (init.elencoAllenatori || []).find((u) => u.isAdmin);
  expect(admin).toBeTruthy();

  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await page.waitForFunction(() => window.angular && angular.element(document.body).scope());
  await page.evaluate(async (id) => {
    const r = angular.element(document.body).scope().$root;
    await r.refreshLoginElenco();
    await r.entraCome(id);
  }, admin.id);

  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.nomegiocatore && r.isAdmin && !r.needsWizard();
  }, null, { timeout: 30000 });
}

async function openOpzioni(page) {
  await page.getByRole('button', { name: /GIOC0|ADMIN/i }).click();
  await page.getByRole('button', { name: 'Opzioni' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.showSettings);
}

test.describe('Opzioni lega esistente', () => {
  test.beforeEach(async ({ request }) => {
    const seed = await request.post('/fantaasta/test/seed');
    expect(seed.ok()).toBeTruthy();
  });

  test('modifiche mostrano Salva/Annulla, dopo Salva appare Avanti e persiste', async ({ page, request }) => {
    await loginAsAdmin(page, request);
    await openOpzioni(page);

    await expect(page.getByRole('button', { name: 'Avanti' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Salva' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Vai in home' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Indietro' })).toHaveCount(0);

    await page.locator('input[ng-model="durataAstaDefault"]').fill('21');
    await expect(page.getByRole('button', { name: 'Salva' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Annulla' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Avanti' })).toHaveCount(0);

    await page.getByRole('button', { name: 'Salva' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.wizardBusy);
    await expect(page.getByRole('button', { name: 'Avanti' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Salva' })).toHaveCount(0);

    const init = await request.get('/fantaasta/init').then((r) => r.json());
    expect(init.durataAsta).toBe(21);

    await page.getByRole('button', { name: 'Vai in home' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.showSettings);

    await openOpzioni(page);
    await expect(page.locator('input[ng-model="durataAstaDefault"]')).toHaveValue('21');
    await page.getByRole('button', { name: 'Vai in home' }).click();
  });

  test('Annulla ripristina il valore e mostra Avanti', async ({ page, request }) => {
    await loginAsAdmin(page, request);
    await openOpzioni(page);

    const before = await page.locator('input[ng-model="durataAstaDefault"]').inputValue();
    await page.locator('input[ng-model="durataAstaDefault"]').fill('17');
    await page.getByRole('button', { name: 'Annulla' }).click();
    await expect(page.locator('input[ng-model="durataAstaDefault"]')).toHaveValue(before);
    await expect(page.getByRole('button', { name: 'Avanti' })).toBeVisible();
    await page.getByRole('button', { name: 'Vai in home' }).click();
  });

  test('mostra tutti gli step incluso Quotazioni con reset import', async ({ page, request }) => {
    await loginAsAdmin(page, request);
    await openOpzioni(page);

    await page.getByRole('button', { name: 'Avanti' }).click();
    await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 3);
    await page.getByRole('button', { name: 'Avanti' }).click();
    await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4);
    await page.getByRole('button', { name: 'Avanti' }).click();
    await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5);

    await expect(page.locator('#wizardQuotazioniFile')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Reset import' })).toBeVisible();
    await expect(page.getByText(/giocatori attualmente in lega/i)).toBeVisible();
    await expect(page.getByText(/Quotazioni già caricate/i)).toBeVisible();
    await expect(page.getByRole('button', { name: 'Vai in home' })).toHaveCount(1);
    await page.getByRole('button', { name: 'Vai in home' }).click();
  });
});
