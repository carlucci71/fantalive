// @ts-check
const { test, expect } = require('@playwright/test');
const { waitForAngular, waitForFase } = require('./helpers/asta-helpers');

const UI = '/fantaasta/index.html?v=20250925ui76';
const ADMIN_UI = '/fantaasta/admin.html?v=20250925ui76';

async function findAdminId(request) {
  const init = await request.get('/fantaasta/init').then((r) => r.json());
  const admin = (init.elencoAllenatori || []).find((u) => u.isAdmin);
  expect(admin).toBeTruthy();
  return admin.id;
}

async function setDurataOnServer(request, seconds) {
  const init = await request.get('/fantaasta/init').then((r) => r.json());
  const teams = (init.elencoAllenatori || []).map((t) => ({
    id: t.id,
    ordine: t.ordine,
    nuovoNome: t.nuovoNome || t.nome,
    nome: t.nome,
    pwd: t.pwd || '',
    isAdmin: !!t.isAdmin,
  }));
  const body = {
    durataAsta: seconds,
    isSingle: init.isSingle === 'S',
    isATurni: init.isATurni === 'S',
    elencoAllenatori: teams,
    admin: true,
    idgiocatore: teams.find((t) => t.isAdmin)?.id ?? 0,
    tokenDispositiva: 4242,
    budget: init.budget,
    maxP: init.maxP, minP: init.minP,
    maxD: init.maxD, minD: init.minD,
    maxC: init.maxC, minC: init.minC,
    maxA: init.maxA, minA: init.minA,
    numAcquisti: init.numAcquisti,
    numMinAcquisti: init.numMinAcquisti,
  };
  const res = await request.post('/fantaasta/aggiornaConfigLega', { data: body });
  expect(res.ok()).toBeTruthy();
  const data = await res.json();
  expect(data.esitoDispositiva).toBe('OK');
  const after = await request.get('/fantaasta/init').then((r) => r.json());
  expect(after.durataAsta).toBe(seconds);
}

test.describe('Durata asta da impostazioni', () => {
  test.beforeEach(async ({ request }) => {
    const seed = await request.post('/fantaasta/test/seed');
    expect(seed.ok()).toBeTruthy();
    await setDurataOnServer(request, 15);
  });

  test('Opzioni index: 20s salvati e timer asta parte da 20', async ({ page, request }) => {
    const adminId = await findAdminId(request);

    await page.goto(UI, { waitUntil: 'domcontentloaded' });
    await waitForAngular(page);
    await page.evaluate(async (id) => {
      const r = angular.element(document.body).scope().$root;
      await r.refreshLoginElenco();
      await r.entraCome(id);
    }, adminId);

    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.nomegiocatore && r.isAdmin && !r.needsWizard();
    }, null, { timeout: 30000 });

    await page.getByRole('button', { name: /ADMIN|GIOC0/i }).click();
    await page.getByRole('button', { name: 'Opzioni' }).click();
    await page.waitForFunction(() => angular.element(document.body).scope().$root.showSettings);

    await page.locator('input[ng-model="durataAstaDefault"]').fill('20');
    await page.getByRole('button', { name: 'Salva' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.wizardBusy);
    await page.getByRole('button', { name: 'Vai in home' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.showSettings);

    const init = await request.get('/fantaasta/init').then((r) => r.json());
    expect(init.durataAsta).toBe(20);

    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.durataAsta === 20 && r.durataAstaDefault === 20;
    }, null, { timeout: 10000 });

    await page.evaluate(() => {
      const r = angular.element(document.body).scope().$root;
      const player = r.calciatori[0];
      r.selezionaCalciatore(player);
      r.inizia(r.nomegiocatore, r.idgiocatore);
    });
    await waitForFase(page, 'BIDDING');

    const timer = await page.evaluate(() => {
      const r = angular.element(document.body).scope().$root;
      return {
        durataAsta: r.durataAsta,
        seconds: r.auctionTimerSeconds(),
        contaTempo: r.contaTempo,
      };
    });
    expect(timer.durataAsta).toBe(20);
    expect(timer.seconds).toBe(20);
    expect(timer.contaTempo).toBeLessThan(500);
  });

  test('Opzioni: durata 21 salvata navigando tra gli step', async ({ page, request }) => {
    const adminId = await findAdminId(request);

    await page.goto(UI, { waitUntil: 'domcontentloaded' });
    await waitForAngular(page);
    await page.evaluate(async (id) => {
      const r = angular.element(document.body).scope().$root;
      await r.refreshLoginElenco();
      await r.entraCome(id);
    }, adminId);

    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.nomegiocatore && r.isAdmin && !r.needsWizard();
    }, null, { timeout: 30000 });

    await page.getByRole('button', { name: /ADMIN|GIOC0/i }).click();
    await page.getByRole('button', { name: 'Opzioni' }).click();
    await page.locator('input[ng-model="durataAstaDefault"]').fill('21');
    await page.getByRole('button', { name: 'Salva' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.wizardBusy);
    await page.getByRole('button', { name: 'Vai in home' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.showSettings);

    const init = await request.get('/fantaasta/init').then((r) => r.json());
    expect(init.durataAsta).toBe(21);

    await page.evaluate(() => {
      const r = angular.element(document.body).scope().$root;
      const player = r.calciatori[0];
      r.selezionaCalciatore(player);
      r.inizia(r.nomegiocatore, r.idgiocatore);
    });
    await waitForFase(page, 'BIDDING');

    const seconds = await page.evaluate(() => angular.element(document.body).scope().$root.auctionTimerSeconds());
    expect(seconds).toBe(21);
  });

  test('admin.html: AGGIORNA salva 20s e timer asta parte da 20', async ({ page, request }) => {
    await page.goto(ADMIN_UI, { waitUntil: 'domcontentloaded' });
    await waitForAngular(page);
    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.nomegiocatore && r.isAdmin;
    }, null, { timeout: 30000 });

    await page.locator('input[ng-model="durataAstaDefault"]').fill('20');
    await page.locator('input[value="AGGIORNA"]').click();
    await page.waitForURL(/index\.html\?v=20250925ui76/, { timeout: 20000 });

    const init = await request.get('/fantaasta/init').then((r) => r.json());
    expect(init.durataAsta).toBe(20);

    await waitForAngular(page);
    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.durataAsta === 20 && r.calciatori && r.calciatori.length > 0;
    }, null, { timeout: 15000 });

    await page.evaluate(() => {
      const r = angular.element(document.body).scope().$root;
      const player = r.calciatori[0];
      r.selezionaCalciatore(player);
      r.inizia(r.nomegiocatore, r.idgiocatore);
    });
    await waitForFase(page, 'BIDDING');

    const seconds = await page.evaluate(() => angular.element(document.body).scope().$root.auctionTimerSeconds());
    expect(seconds).toBe(20);
  });
});
