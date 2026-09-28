// @ts-check
const { test, expect } = require('@playwright/test');

const UI = '/fantaasta/index.html?v=20250925ui76';

async function seedLega(request) {
  const res = await request.post('/fantaasta/test/seed');
  expect(res.ok()).toBeTruthy();
}

async function getInit(request) {
  const res = await request.get('/fantaasta/init');
  expect(res.ok()).toBeTruthy();
  return res.json();
}

function buildTeamsPayload(init, overrides = {}) {
  const teams = (init.elencoAllenatori || []).map((t, idx) => ({
    id: t.id,
    ordine: t.ordine,
    nuovoNome: overrides.teamNames && overrides.teamNames[t.id] ? overrides.teamNames[t.id] : (t.nuovoNome || t.nome),
    nome: t.nome,
    pwd: t.pwd || '',
    isAdmin: overrides.adminId != null ? t.id === overrides.adminId : !!t.isAdmin,
  }));
  return teams;
}

async function saveConfig(request, init, config, teamOverrides = {}) {
  const teams = buildTeamsPayload(init, teamOverrides);
  const body = {
    durataAsta: config.durataAsta,
    budget: config.budget,
    isSingle: config.isSingle,
    isATurni: config.isATurni,
    isMantra: config.isMantra,
    maxP: config.maxP,
    minP: config.minP,
    maxD: config.maxD,
    minD: config.minD,
    maxC: config.maxC,
    minC: config.minC,
    maxA: config.maxA,
    minA: config.minA,
    numAcquisti: config.numAcquisti,
    numMinAcquisti: config.numMinAcquisti,
    elencoAllenatori: teams,
    admin: true,
    idgiocatore: teamOverrides.adminId ?? teams.find((t) => t.isAdmin)?.id ?? 0,
    tokenDispositiva: Math.floor(Math.random() * 10000) + 1,
  };
  const res = await request.post('/fantaasta/aggiornaConfigLega', { data: body });
  expect(res.ok()).toBeTruthy();
  const data = await res.json();
  expect(data.esitoDispositiva).toBe('OK');
  return data;
}

function expectConfigMatches(init, expected) {
  expect(init.budget).toBe(expected.budget);
  expect(init.durataAsta).toBe(expected.durataAsta);
  expect(init.isATurni).toBe(expected.isATurni ? 'S' : 'N');
  expect(init.isSingle).toBe(expected.isSingle ? 'S' : 'N');
  expect(init.isMantra).toBe(expected.isMantra ? 'S' : 'N');
  expect(init.maxP).toBe(expected.maxP);
  expect(init.minP).toBe(expected.minP);
  expect(init.maxD).toBe(expected.maxD);
  expect(init.minD).toBe(expected.minD);
  expect(init.maxC).toBe(expected.maxC);
  expect(init.minC).toBe(expected.minC);
  expect(init.maxA).toBe(expected.maxA);
  expect(init.minA).toBe(expected.minA);
  expect(init.numAcquisti).toBe(expected.numAcquisti);
  expect(init.numMinAcquisti).toBe(expected.numMinAcquisti);
}

test.describe('Persistenza configurazione lega su DB', () => {
  test.beforeEach(async ({ request }) => {
    await seedLega(request);
  });

  test('aggiornaConfigLega persiste tutti i campi numerici e flag', async ({ request }) => {
    const init0 = await getInit(request);
    const expected = {
      budget: 432,
      durataAsta: 22,
      isATurni: true,
      isSingle: true,
      isMantra: false,
      maxP: 2,
      minP: 1,
      maxD: 7,
      minD: 6,
      maxC: 7,
      minC: 6,
      maxA: 5,
      minA: 4,
      numAcquisti: 21,
      numMinAcquisti: 17,
    };

    await saveConfig(request, init0, expected);

    const init1 = await getInit(request);
    expectConfigMatches(init1, expected);
  });

  test('isMantra viene salvato e riletto da /init', async ({ request }) => {
    const init0 = await getInit(request);
    const expected = {
      budget: 500,
      durataAsta: 18,
      isATurni: false,
      isSingle: false,
      isMantra: true,
      maxP: 2,
      minP: 2,
      maxD: 0,
      minD: 0,
      maxC: 0,
      minC: 0,
      maxA: 25,
      minA: 20,
      numAcquisti: 25,
      numMinAcquisti: 22,
    };

    await saveConfig(request, init0, expected);
    const init1 = await getInit(request);
    expectConfigMatches(init1, expected);
  });

  test('nomi squadra e flag admin persistono su allenatori', async ({ request }) => {
    const init0 = await getInit(request);
    const expected = {
      budget: 500,
      durataAsta: 20,
      isATurni: false,
      isSingle: false,
      isMantra: false,
      maxP: 3,
      minP: 3,
      maxD: 8,
      minD: 8,
      maxC: 8,
      minC: 8,
      maxA: 6,
      minA: 6,
      numAcquisti: 25,
      numMinAcquisti: 25,
    };

    await saveConfig(request, init0, expected, {
      adminId: 2,
      teamNames: { 0: 'ALPHA', 1: 'BETA', 2: 'ADMINX' },
    });

    const init1 = await getInit(request);
    const byId = {};
    (init1.elencoAllenatori || []).forEach((t) => { byId[t.id] = t; });
    expect(byId[0].nome).toBe('ALPHA');
    expect(byId[1].nome).toBe('BETA');
    expect(byId[2].nome).toBe('ADMINX');
    expect(byId[2].isAdmin).toBe(true);
    expect(byId[0].isAdmin).toBe(false);
  });

  test('Opzioni UI: salva budget e composizione rosa su server', async ({ page, request }) => {
    await page.goto(UI, { waitUntil: 'domcontentloaded' });
    await page.waitForFunction(() => window.angular && angular.element(document.body).scope());

    await page.evaluate(async () => {
      const r = angular.element(document.body).scope().$root;
      await r.refreshLoginElenco();
      await r.entraCome(0);
    });

    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.nomegiocatore && r.isAdmin && !r.needsWizard();
    }, null, { timeout: 30000 });

    await page.getByRole('button', { name: /GIOC0/i }).click();
    await page.getByRole('button', { name: 'Opzioni' }).click();
    await page.waitForFunction(() => angular.element(document.body).scope().$root.showSettings);

    await page.locator('input[ng-model="budget"]').fill('377');
    await page.locator('input[ng-model="durataAstaDefault"]').fill('19');
    await page.evaluate(() => {
      const r = angular.element(document.body).scope().$root;
      r.isATurni = true;
      r.isSingle = false;
      r.isMantra = false;
      r.$applyAsync();
    });
    await page.getByRole('button', { name: 'Salva' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.wizardBusy);
    await page.getByRole('button', { name: 'Avanti' }).click();
    await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 3);

    await page.locator('input[ng-model="maxP"]').fill('2');
    await page.locator('input[ng-model="maxD"]').fill('7');
    await page.locator('input[ng-model="maxC"]').fill('6');
    await page.locator('input[ng-model="maxA"]').fill('5');
    await page.evaluate(() => angular.element(document.body).scope().$root.$applyAsync());
    await page.getByRole('button', { name: 'Salva' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.wizardBusy);
    await page.getByRole('button', { name: 'Vai in home' }).click();
    await page.waitForFunction(() => !angular.element(document.body).scope().$root.showSettings);

    const init = await getInit(request);
    expect(init.budget).toBe(377);
    expect(init.durataAsta).toBe(19);
    expect(init.isATurni).toBe('S');
    expect(init.isSingle).toBe('N');
    expect(init.maxP).toBe(2);
    expect(init.maxD).toBe(7);
    expect(init.maxC).toBe(6);
    expect(init.maxA).toBe(5);
    expect(init.numAcquisti).toBe(20);
  });
});
