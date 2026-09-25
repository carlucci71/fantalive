// @ts-check
/**
 * Test browser "manuali" automatizzati — flusso completo asta tra admin e utente.
 * Richiamabili con: npm run test:flow  (o ./scripts/run-s6-tests.sh)
 */
const { test, expect } = require('@playwright/test');
const {
  loginAsta,
  loginAdmin,
  fetchInit,
  cookiesHeader,
  getRootState,
  waitForWinner,
  startAuctionForPlayer,
  enableAutoBid,
  assertAutoBidControlsVisible,
  bidIncrement,
  selectOperaCome,
  terminaAsta,
  confermaAsta,
  setupAdminLega,
} = require('./helpers/asta-helpers');

const PLAYER_1 = 'TEST_ALFA';
const PLAYER_2 = 'TEST_BETA';
const PLAYER_3 = 'TEST_GAMMA';

test.describe('FantaAsta browser flow (admin + utente)', () => {
  test.beforeEach(async ({ request }) => {
    const res = await request.post('/fantaasta/test/seed');
    expect(res.ok()).toBeTruthy();
  });

  test('setup admin: lega configurata e giocatori disponibili', async ({ page }) => {
    await setupAdminLega(page);
    const cookies = await cookiesHeader(page.context());
    const init = await fetchInit(page.request, cookies);
    expect(init.giocatoreLoggato).toBe('GIOC0');
    expect(init.onlineWs).toBe(true);
    expect(init.calciatori.length).toBe(3);
    expect(init.calciatori.map((c) => c.nome)).toContain(PLAYER_1);
  });

  test('asta 1: admin avvia, GIOC1 rilancia, admin opera-come conferma vincitore GIOC1', async ({ browser }) => {
    const ctxAdmin = await browser.newContext();
    const ctxUser = await browser.newContext();
    const admin = await ctxAdmin.newPage();
    const user = await ctxUser.newPage();

    await setupAdminLega(admin);
    await admin.goto('/fantaasta/index.html', { waitUntil: 'domcontentloaded' });
    await loginAsta(admin, 'GIOC0', 0);
    await loginAsta(user, 'GIOC1', 1);

    await startAuctionForPlayer(admin, PLAYER_1);
    await waitForWinner(admin, 'GIOC0', 1);
    await waitForWinner(user, 'GIOC0', 1);

    await enableAutoBid(user);
    await assertAutoBidControlsVisible(user);

    await selectOperaCome(admin, 'GIOC1');
    await enableAutoBid(admin, true);
    await assertAutoBidControlsVisible(admin, true);
    await bidIncrement(admin, 11, true);
    await waitForWinner(admin, 'GIOC1', 12);
    await waitForWinner(user, 'GIOC1', 12);

    await terminaAsta(admin);
    const adminConfirm = await getRootState(admin);
    const userConfirm = await getRootState(user);
    expect(adminConfirm.faseAsta).toBe('DA_CONFERMARE');
    expect(userConfirm.faseAsta).toBe('DA_CONFERMARE');
    expect(adminConfirm.offertaVincente.nome).toBe('GIOC1');
    expect(adminConfirm.offertaVincente.offerta).toBe(12);
    expect(userConfirm.offertaVincente.nome).toBe('GIOC1');

    await confermaAsta(admin);
    await admin.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.mapSpesoTotale && r.mapSpesoTotale.GIOC1;
    }, null, { timeout: 20000 });
    await user.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.mapSpesoTotale && r.mapSpesoTotale.GIOC1;
    }, null, { timeout: 20000 });

    const cookiesAdmin = await cookiesHeader(ctxAdmin);
    const initAfter = await fetchInit(admin.request, cookiesAdmin);
    expect(initAfter.calciatori.length).toBe(2);
    expect(initAfter.mapSpesoTotale.GIOC1.speso).toBe(12);

    await ctxAdmin.close();
    await ctxUser.close();
  });

  test('asta 2: admin vince, conferma coerente su entrambi i browser', async ({ browser }) => {
    const ctxAdmin = await browser.newContext();
    const ctxUser = await browser.newContext();
    const admin = await ctxAdmin.newPage();
    const user = await ctxUser.newPage();

    await loginAsta(admin, 'GIOC0', 0);
    await loginAsta(user, 'GIOC1', 1);

    await startAuctionForPlayer(admin, PLAYER_2);
    await enableAutoBid(user);
    await assertAutoBidControlsVisible(user);
    await bidIncrement(user, 9);
    await waitForWinner(admin, 'GIOC1', 10);

    await enableAutoBid(admin);
    await assertAutoBidControlsVisible(admin);
    await bidIncrement(admin, 10);
    await waitForWinner(admin, 'GIOC0', 20);
    await waitForWinner(user, 'GIOC0', 20);

    await terminaAsta(admin);
    await confermaAsta(admin);

    const adminState = await getRootState(admin);
    const userState = await getRootState(user);
    expect(adminState.faseAsta).toBe('IDLE');
    expect(userState.faseAsta).toBe('IDLE');
    expect(adminState.mapSpesoTotale.GIOC0.speso).toBe(20);

    const cookies = await cookiesHeader(ctxAdmin);
    const init = await fetchInit(admin.request, cookies);
    expect(init.calciatori.length).toBe(2);
    expect(init.calciatori.map((c) => c.nome)).toEqual(expect.arrayContaining([PLAYER_1, PLAYER_3]));

    await ctxAdmin.close();
    await ctxUser.close();
  });

  test('flusso completo: setup admin → 2 aste → stato finale coerente', async ({ browser }) => {
    const ctxAdmin = await browser.newContext();
    const ctxUser = await browser.newContext();
    const admin = await ctxAdmin.newPage();
    const user = await ctxUser.newPage();

    await setupAdminLega(admin);
    await admin.goto('/fantaasta/index.html', { waitUntil: 'domcontentloaded' });
    await loginAsta(admin, 'GIOC0', 0);
    await loginAsta(user, 'GIOC1', 1);

    // Asta 1 — admin opera come per GIOC1
    await startAuctionForPlayer(admin, PLAYER_1);
    await selectOperaCome(admin, 'GIOC1');
    await bidIncrement(admin, 11, true);
    await waitForWinner(user, 'GIOC1', 12);
    await terminaAsta(admin);
    await confermaAsta(admin);

    // Asta 2 — GIOC0 vince dopo rilancio utente
    await startAuctionForPlayer(admin, PLAYER_2);
    await bidIncrement(user, 9);
    await waitForWinner(user, 'GIOC1', 10);
    await bidIncrement(admin, 10);
    await waitForWinner(user, 'GIOC0', 20);
    await terminaAsta(admin);
    await confermaAsta(admin);

    // Asta 3 — GIOC1 vince su difensore
    await startAuctionForPlayer(admin, PLAYER_3);
    await selectOperaCome(admin, 'GIOC1');
    await bidIncrement(admin, 9, true);
    await waitForWinner(user, 'GIOC1', 10);
    await terminaAsta(admin);
    await confermaAsta(admin);

    const cookies = await cookiesHeader(ctxAdmin);
    const init = await fetchInit(admin.request, cookies);
    expect(init.calciatori.length).toBe(0);
    expect(init.mapSpesoTotale.GIOC0.speso).toBe(20);
    expect(init.mapSpesoTotale.GIOC1.speso).toBe(22);

    await ctxAdmin.close();
    await ctxUser.close();
  });
});
