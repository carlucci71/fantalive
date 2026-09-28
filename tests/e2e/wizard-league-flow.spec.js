// @ts-check
const { test, expect } = require('@playwright/test');
const path = require('path');

const XLS = '/Users/giovanniricco/Downloads/Quotazioni_6a_Serie A.xls';
const UI = '/fantaasta/index.html?v=20250925ui78';

async function waitForAngular(page) {
  await page.waitForFunction(
    () => window.angular && angular.element(document.body).scope(),
    null,
    { timeout: 30000 }
  );
}

async function rootState(page) {
  return page.evaluate(() => {
    const r = angular.element(document.body).scope().$root;
    return {
      wizardStep: r.wizardStep,
      setupInProgress: r.setupInProgress,
      setupCompletato: r.setupCompletato,
      needsWizard: r.needsWizard(),
      wizardUploadCount: r.wizardUploadCount,
      calciatori: (r.calciatori || []).length,
      config: r.config,
      showSettings: r.showSettings,
      nomegiocatore: r.nomegiocatore,
    };
  });
}

test('wizard: crea lega, importa file, home, cancella lega', async ({ page, request }) => {
  await request.post('/fantaasta/test/reset');
  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);

  await page.getByRole('button', { name: 'Entra' }).click();
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.wizardStep === 1 && r.setupInProgress;
  });

  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });

  let s = await rootState(page);
  expect(s.wizardStep).toBe(4);
  await expect(page.getByRole('button', { name: 'Indietro' })).toBeVisible();

  await page.getByRole('button', { name: 'Indietro' }).click();
  s = await rootState(page);
  expect(s.wizardStep).toBe(3);
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });

  await page.getByRole('button', { name: 'Salva squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5, null, { timeout: 20000 });

  s = await rootState(page);
  expect(s.wizardStep).toBe(5);
  await expect(page.getByRole('button', { name: 'Indietro' })).toBeVisible();

  await page.setInputFiles('#wizardQuotazioniFile', XLS);
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.wizardUploadCount > 0 || (r.calciatori && r.calciatori.length > 0);
  }, null, { timeout: 60000 });

  s = await rootState(page);
  expect(s.calciatori).toBeGreaterThan(0);

  await page.getByRole('button', { name: 'Vai in home' }).click();
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return !r.needsWizard() && r.setupCompletato && r.nomegiocatore;
  }, null, { timeout: 20000 });

  await page.getByRole('button', { name: /GIOC0/i }).click();
  await page.getByRole('button', { name: 'Opzioni' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.showSettings);

  await expect(page.getByRole('button', { name: 'Cancella lega' })).toBeVisible();
  page.once('dialog', (d) => {
    expect(d.message()).toContain('Sei sicuro');
    d.accept();
  });
  await page.getByRole('button', { name: 'Cancella lega' }).click();

  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.config && !r.nomegiocatore;
  }, null, { timeout: 20000 });

  const init = await request.get('/fantaasta/init').then((r) => r.json());
  expect(init.DA_CONFIGURARE).toBeTruthy();
});

test('wizard: reset import mantiene lega e indietro in solo-upload', async ({ page, request }) => {
  await request.post('/fantaasta/test/reset');
  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);
  await page.getByRole('button', { name: 'Entra' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });
  await page.getByRole('button', { name: 'Salva squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5, null, { timeout: 20000 });

  await page.setInputFiles('#wizardQuotazioniFile', XLS);
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.calciatori && r.calciatori.length > 0;
  }, null, { timeout: 60000 });

  await Promise.all([
    page.waitForEvent('dialog').then((d) => d.accept()),
    page.getByRole('button', { name: 'Reset import' }).click(),
  ]);
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return !r.caricamentoInCorso && r.wizardStep === 5 && !r.wizardUploadCount && (!r.calciatori || r.calciatori.length === 0);
  }, null, { timeout: 30000 });

  const init = await request.get('/fantaasta/init').then((r) => r.json());
  expect(init.DA_CONFIGURARE).toBeFalsy();
  expect((init.elencoAllenatori || []).length).toBe(8);
  expect((init.calciatori || []).length).toBe(0);

  await page.reload({ waitUntil: 'domcontentloaded' });
  await waitForAngular(page);
  await page.evaluate(async () => {
    const r = angular.element(document.body).scope().$root;
    await r.entraCome(0);
  });
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.wizardSoloUpload() && r.wizardStep === 5;
  }, null, { timeout: 20000 });
  await expect(page.getByRole('button', { name: 'Indietro' })).toBeVisible();
  await page.getByRole('button', { name: 'Indietro' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4);

  await request.post('/fantaasta/test/reset');
});

test('wizard: step 5 senza file disabilita Vai in home', async ({ page, request }) => {
  const reset = await request.post('/fantaasta/test/reset');
  expect(reset.ok()).toBeTruthy();
  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);

  await page.getByRole('button', { name: 'Entra' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });
  await page.getByRole('button', { name: 'Salva squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5, null, { timeout: 20000 });

  const homeBtn = page.getByRole('button', { name: 'Vai in home' });
  await expect(homeBtn).toBeVisible();
  await expect(homeBtn).toBeDisabled();

  await request.post('/fantaasta/test/reset');
});

test('wizard: durata 13 persiste in Opzioni dopo creazione completa', async ({ page, request }) => {
  const reset = await request.post('/fantaasta/test/reset');
  expect(reset.ok()).toBeTruthy();
  const initReset = await request.get('/fantaasta/init').then((r) => r.json());
  expect(initReset.DA_CONFIGURARE).toBeTruthy();

  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);

  await page.getByRole('button', { name: 'Entra' }).click();
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.config && r.needsWizard() && r.wizardStep === 1;
  });
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 2);

  await page.locator('input[ng-model="durataAstaDefault"]').fill('13');
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });

  const afterCreate = await request.get('/fantaasta/init').then((r) => r.json());
  expect(afterCreate.durataAsta).toBe(13);

  await page.getByRole('button', { name: 'Salva squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5, null, { timeout: 20000 });

  const afterTeams = await request.get('/fantaasta/init').then((r) => r.json());
  expect(afterTeams.durataAsta).toBe(13);

  await page.setInputFiles('#wizardQuotazioniFile', XLS);
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.wizardUploadCount > 0 || (r.calciatori && r.calciatori.length > 0);
  }, null, { timeout: 60000 });

  await page.getByRole('button', { name: 'Vai in home' }).click();
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return !r.needsWizard() && r.setupCompletato && r.nomegiocatore;
  }, null, { timeout: 20000 });

  const initHome = await request.get('/fantaasta/init').then((r) => r.json());
  expect(initHome.durataAsta).toBe(13);

  await page.getByRole('button', { name: /GIOC0/i }).click();
  await page.getByRole('button', { name: 'Opzioni' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.showSettings);

  await expect(page.locator('input[ng-model="durataAstaDefault"]')).toHaveValue('13');
  await expect(page.getByRole('button', { name: 'Avanti' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Salva' })).toHaveCount(0);

  await request.post('/fantaasta/test/reset');
});

test('wizard: creazione incompleta → refresh torna a login e riparte da capo', async ({ page, request }) => {
  await request.post('/fantaasta/test/reset');
  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);

  await page.getByRole('button', { name: 'Entra' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });
  await page.getByRole('button', { name: 'Salva squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5, null, { timeout: 20000 });

  // Hard refresh a metà creazione (squadre ok, senza quotazioni) → login e lega azzerata
  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return !r.nomegiocatore && r.showLoginScreen() && !r.needsWizard() && r.config;
  }, null, { timeout: 20000 });

  const init = await request.get('/fantaasta/init').then((r) => r.json());
  expect(init.DA_CONFIGURARE).toBeTruthy();
  expect((init.elencoAllenatori || []).length).toBe(0);

  // Entra di nuovo: riparte il wizard da step 1
  await page.getByRole('button', { name: 'Entra' }).click();
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.nomegiocatore && r.needsWizard() && r.wizardStep === 1;
  }, null, { timeout: 20000 });

  await expect(page.getByRole('heading', { name: 'Crea la tua lega' })).toBeVisible();
  await expect(page.locator('.fa-board')).toHaveCount(0);

  await request.post('/fantaasta/test/reset');
});

test('wizard: cambio admin durante creazione passa sessione e va a upload', async ({ page, request }) => {
  await request.post('/fantaasta/test/reset');
  await page.goto(UI, { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);

  await page.getByRole('button', { name: 'Entra' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 1);
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Avanti' }).click();
  await page.getByRole('button', { name: 'Crea squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 4, null, { timeout: 20000 });

  await page.evaluate(() => {
    const r = angular.element(document.body).scope().$root;
    const target = (r.elencoAllenatori || []).find((u) => u.id === 3);
    if (!target) throw new Error('GIOC3 non trovato');
    angular.forEach(r.elencoAllenatori || [], (u) => { u.isAdmin = u.id === target.id; });
  });
  await page.getByRole('button', { name: 'Salva squadre' }).click();
  await page.waitForFunction(() => angular.element(document.body).scope().$root.wizardStep === 5, null, { timeout: 30000 });

  const state = await page.evaluate(() => {
    const r = angular.element(document.body).scope().$root;
    return {
      wizardStep: r.wizardStep,
      idgiocatore: r.idgiocatore,
      nomegiocatore: r.nomegiocatore,
      isAdmin: r.isAdmin,
      isAdminBootstrap: r.isAdminBootstrap,
    };
  });
  expect(state.wizardStep).toBe(5);
  expect(state.idgiocatore).toBe(3);
  expect(state.isAdmin).toBe(true);
  expect(state.isAdminBootstrap).toBe(false);
  await expect(page.locator('#wizardQuotazioniFile')).toBeVisible();

  await request.post('/fantaasta/test/reset');
});
