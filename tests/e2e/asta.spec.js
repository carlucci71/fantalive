// @ts-check
const { test, expect } = require('@playwright/test');
const { loginAsta, fetchInit, cookiesHeader } = require('./helpers/asta-helpers');

test.describe('FantaAsta E2E', () => {
  test.beforeEach(async ({ request }) => {
    await request.post('/fantaasta/test/seed');
  });

  test('due browser paralleli: connessioni coerenti e pagine secondarie senza WS extra', async ({ browser }) => {
    const ctx0 = await browser.newContext();
    const ctx1 = await browser.newContext();
    const wsTab0 = await ctx0.newPage();
    const wsTab1 = await ctx1.newPage();

    await loginAsta(wsTab0, 'GIOC0', 0);
    await loginAsta(wsTab1, 'GIOC1', 1);

    const cookies0 = await cookiesHeader(ctx0);
    const cookies1 = await cookiesHeader(ctx1);
    const init0 = await fetchInit(wsTab0.request, cookies0);
    const init1 = await fetchInit(wsTab1.request, cookies1);

    expect(init0.onlineWs).toBe(true);
    expect(init1.onlineWs).toBe(true);
    expect(init0.giocatoreLoggato).toBe('GIOC0');
    expect(init1.giocatoreLoggato).toBe('GIOC1');
    expect(init0.utenti).toContain('GIOC0');
    expect(init1.utenti).toContain('GIOC1');
    expect(init0.utenti.length).toBeGreaterThanOrEqual(2);

    const libTab = await ctx0.newPage();
    await libTab.goto('/fantaasta/liberi.html', { waitUntil: 'networkidle' });
    await libTab.goto('/fantaasta/riepilogo.html', { waitUntil: 'networkidle' });
    await libTab.goto('/fantaasta/cronologiaOfferte.html', { waitUntil: 'networkidle' });
    await libTab.goto('/fantaasta/logger.html', { waitUntil: 'networkidle' });

    const initAfterSecondary = await fetchInit(wsTab0.request, cookies0);
    expect(initAfterSecondary.onlineWs).toBe(true);
    expect(initAfterSecondary.giocatoreLoggato).toBe('GIOC0');

    await ctx0.close();
    await ctx1.close();
  });

  test('due utenti: rilancio GIOC1 visibile su entrambi', async ({ browser }) => {
    const ctx0 = await browser.newContext();
    const ctx1 = await browser.newContext();
    const wsTab0 = await ctx0.newPage();
    const wsTab1 = await ctx1.newPage();
    await loginAsta(wsTab0, 'GIOC0', 0);
    await loginAsta(wsTab1, 'GIOC1', 1);

    const setupHook = async (page) => {
      await page.evaluate(() => {
        window.__astaMsgs = [];
        const root = angular.element(document.body).scope().$root;
        const orig = root.getMessaggio;
        root.getMessaggio = function (message) {
          if (message) {
            try { window.__astaMsgs.push(JSON.parse(message)); } catch (e) {}
          }
          return orig.call(root, message);
        };
      });
    };
    await setupHook(wsTab0);
    await setupHook(wsTab1);

    await wsTab0.evaluate(async () => {
      const init = await (await fetch('/fantaasta/init')).json();
      const first = init.calciatori[0];
      FantaWsConnection.send(JSON.stringify({
        operazione: 'start',
        nomegiocatore: 'GIOC0',
        idgiocatore: '0',
        nomegiocatoreOperaCome: 'GIOC0',
        idgiocatoreOperaCome: '0',
        selCalciatore: first.id + '@' + first.nome,
        selCalciatoreMacroRuolo: first.macroRuolo || first.ruolo || 'A',
      }));
    });

    await wsTab1.evaluate(async () => {
      const waitBid = async () => {
        const start = Date.now();
        while (Date.now() - start < 10000) {
          const hit = (window.__astaMsgs || []).find(m => m.offertaVincente);
          if (hit) return;
          await new Promise(r => setTimeout(r, 50));
        }
        throw new Error('no start');
      };
      await waitBid();
      FantaWsConnection.send(JSON.stringify({
        operazione: 'inviaOfferta',
        nomegiocatore: 'GIOC1',
        idgiocatore: '1',
        nomegiocatoreOperaCome: 'GIOC1',
        idgiocatoreOperaCome: '1',
        offerta: 12,
        maxRilancio: 500,
      }));
    });

    const winner = await wsTab0.evaluate(async () => {
      const start = Date.now();
      while (Date.now() - start < 12000) {
        const hit = (window.__astaMsgs || []).find(
          m => m.offertaVincente && m.offertaVincente.nomegiocatore === 'GIOC1' && m.offertaVincente.offerta === 12
        );
        if (hit) return hit.offertaVincente;
        await new Promise(r => setTimeout(r, 50));
      }
      throw new Error('no bid on GIOC0');
    });
    expect(winner.nomegiocatore).toBe('GIOC1');
    expect(winner.offerta).toBe(12);

    await ctx0.close();
    await ctx1.close();
  });

  test('asta via WS: vincitore e conferma aggiornano stato', async ({ browser }) => {
    const ctx = await browser.newContext();
    const page = await ctx.newPage();
    await loginAsta(page, 'GIOC0', 0);

    const player = await page.evaluate(async () => {
      const init = await (await fetch('/fantaasta/init')).json();
      return init.calciatori[0];
    });

    await page.evaluate((first) => {
      FantaWsConnection.send(JSON.stringify({
        operazione: 'start',
        nomegiocatore: 'GIOC0',
        idgiocatore: '0',
        nomegiocatoreOperaCome: 'GIOC0',
        idgiocatoreOperaCome: '0',
        selCalciatore: first.id + '@' + first.nome,
        selCalciatoreMacroRuolo: first.macroRuolo || first.ruolo || 'A',
      }));
    }, player);

    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.offertaVincente && r.offertaVincente.nomegiocatore === 'GIOC0';
    }, { timeout: 15000 });

    await page.evaluate(() => {
      FantaWsConnection.send(JSON.stringify({
        operazione: 'terminaAsta',
        nomegiocatore: 'GIOC0',
        idgiocatore: '0',
      }));
    });

    await page.waitForFunction(() => {
      const r = angular.element(document.body).scope().$root;
      return r.faseAsta === 'DA_CONFERMARE';
    }, { timeout: 15000 });

    await page.evaluate(() => {
      const r = angular.element(document.body).injector().get('$rootScope');
      const $resource = angular.element(document.body).injector().get('$resource');
      return $resource('./confermaAsta', {}).save({
        offerta: r.offertaVincente,
        idgiocatore: r.idgiocatore,
        tokenDispositiva: r.tokenDispositiva || '',
      }).$promise;
    });

    const cookies = await cookiesHeader(ctx);
    const initAfter = await fetchInit(page.request, cookies);
    expect(initAfter.mapSpesoTotale).toBeTruthy();
    expect(initAfter.mapSpesoTotale.GIOC0.speso).toBeGreaterThan(0);
    expect(initAfter.calciatori.length).toBe(2);

    await ctx.close();
  });
});

test.describe('FantaLive E2E', () => {
  test('connessione WS e refresh timer', async ({ page }) => {
    await page.goto('/fantalive/index.html', { waitUntil: 'networkidle' });
    const gotRefresh = await page.waitForFunction(() => {
      const root = window.angular && window.angular.element(document.body).scope();
      return root && root.$root && root.$root.timeRefresh > 0;
    }, null, { timeout: 15000 }).catch(() => null);
    expect(gotRefresh).not.toBeNull();
  });
});
