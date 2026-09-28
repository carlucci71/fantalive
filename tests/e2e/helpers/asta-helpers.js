// @ts-check
const { expect } = require('@playwright/test');

async function waitForAngular(page) {
  await page.waitForFunction(
    () => window.FantaWsConnection && window.angular && angular.element(document.body).scope(),
    null,
    { timeout: 30000 }
  );
}

async function loginAsta(page, nome, id) {
  await page.goto('/fantaasta/index.html?v=20250925ui47', { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);
  await page.evaluate(async ({ id }) => {
    const root = angular.element(document.body).scope().$root;
    if (root.refreshLoginElenco) {
      await root.refreshLoginElenco();
    }
    await root.entraCome(id);
  }, { nome, id });
  await page.waitForFunction(
    (expectedNome) => {
      const root = angular.element(document.body).scope().$root;
      return window.FantaWsConnection
        && window.FantaWsConnection.isReady()
        && root.nomegiocatore === expectedNome;
    },
    nome,
    { timeout: 30000 }
  );
}

async function loginAdmin(page) {
  await page.goto('/fantaasta/admin.html', { waitUntil: 'domcontentloaded' });
  await waitForAngular(page);
  await page.waitForFunction(() => {
    const root = angular.element(document.body).scope().$root;
    return root.nomegiocatore && root.isAdmin && FantaWsConnection.isReady();
  }, null, { timeout: 30000 });
}

async function fetchInit(request, cookies) {
  const res = await request.get('/fantaasta/init', { headers: { Cookie: cookies } });
  expect(res.ok()).toBeTruthy();
  return res.json();
}

function cookiesHeader(context) {
  return context.cookies().then(cs => cs.map(c => `${c.name}=${c.value}`).join('; '));
}

async function getRootState(page) {
  return page.evaluate(() => {
    const r = angular.element(document.body).scope().$root;
    return {
      nomegiocatore: r.nomegiocatore,
      faseAsta: r.faseAsta,
      autoAllinea: r.autoAllinea,
      autoAllineaOC: r.autoAllineaOC,
      offertaVincente: r.offertaVincente ? {
        nome: r.offertaVincente.nomegiocatore,
        offerta: r.offertaVincente.offerta,
        calciatore: r.offertaVincente.giocatore?.nome || r.offertaVincente.nomeCalciatore,
      } : null,
      mapSpesoTotale: r.mapSpesoTotale,
      calciatori: (r.calciatori || []).length,
    };
  });
}

async function waitBidButtonsReady(page) {
  await page.waitForFunction(() => {
    const root = angular.element(document.body).scope().$root;
    return root.faseAsta === 'BIDDING' && root.contaTempo >= 1500;
  }, null, { timeout: 15000 });
}

async function waitForFase(page, fase, timeoutMs = 20000) {
  await page.waitForFunction(
    (expected) => angular.element(document.body).scope().$root.faseAsta === expected,
    fase,
    { timeout: timeoutMs }
  );
}

async function waitForWinner(page, nome, offerta, timeoutMs = 20000) {
  await page.waitForFunction(
    ({ nome, offerta }) => {
      const ov = angular.element(document.body).scope().$root.offertaVincente;
      return ov && ov.nomegiocatore === nome && ov.offerta === offerta;
    },
    { nome, offerta },
    { timeout: timeoutMs }
  );
}

async function startAuctionForPlayer(page, playerName) {
  await page.evaluate((name) => {
    const root = angular.element(document.body).scope().$root;
    const player = root.calciatori.find((c) => c.nome === name);
    if (!player) throw new Error('Giocatore non trovato: ' + name);
    root.selezionaCalciatore(player);
    root.inizia(root.nomegiocatore, root.idgiocatore);
  }, playerName);
  await waitForFase(page, 'BIDDING');
}

async function enableAutoBid(page, operaCome = false) {
  await page.evaluate((oc) => {
    const root = angular.element(document.body).scope().$root;
    if (oc) {
      root.sezOperaComeVisible = true;
      root.autoAllineaOC = true;
      if (root.offertaVincente) {
        root.offertaPrivOC = root.offertaVincente.offerta;
      }
    } else {
      root.autoAllinea = true;
      if (root.offertaVincente) {
        root.offertaPriv = root.offertaVincente.offerta;
      }
    }
  }, operaCome);
}

async function enableAutoBidOnPage(page, operaCome = false) {
  await enableAutoBid(page, operaCome);
  await assertAutoBidControlsVisible(page, operaCome);
}

async function assertAutoBidControlsVisible(page, operaCome = false) {
  await waitBidButtonsReady(page);
  const state = await page.evaluate((oc) => {
    const root = angular.element(document.body).scope().$root;
    const nome = oc ? root.nomegiocatoreOperaCome : root.nomegiocatore;
    const autoOn = oc ? root.autoAllineaOC : root.autoAllinea;
    const base = root.offertaVincente ? root.offertaVincente.offerta : 0;
    const priv = oc ? (root.offertaPrivOC || base) : (root.offertaPriv || base);
    const canBid = root.avviabili.indexOf(nome) >= 0
      && root.offertaVincente
      && root.offertaVincente.nomegiocatore !== nome
      && root.offertaVincente.offerta < root.getFromMapSpesoTotale('MAXRILANCIO', nome);
    const hasIncrement = root.testRilancia(nome, priv + 1);
    return { autoOn, canBid, hasIncrement, nome };
  }, operaCome);
  expect(state.autoOn).toBe(true);
  expect(state.canBid).toBe(true);
  expect(state.hasIncrement).toBe(true);
}

async function bidIncrement(page, amount, operaCome = false) {
  await waitBidButtonsReady(page);
  await page.evaluate(({ amount, operaCome }) => {
    const root = angular.element(document.body).scope().$root;
    const target = root.offertaVincente.offerta + amount;
    if (operaCome) {
      root.offertaPrivOC = root.offertaVincente.offerta;
      root.inviaOffertaLibera(root.nomegiocatoreOperaCome, String(root.idgiocatoreOperaCome), target);
    } else {
      root.offertaPriv = root.offertaVincente.offerta;
      root.inviaOffertaLibera(root.nomegiocatore, root.idgiocatore, target);
    }
  }, { amount, operaCome });
}

async function selectOperaCome(page, nome) {
  await page.evaluate((targetNome) => {
    const root = angular.element(document.body).scope().$root;
    root.sezOperaComeVisible = true;
    const al = root.elencoAllenatori.find((a) => a.nome === targetNome);
    if (!al) throw new Error('Allenatore non trovato: ' + targetNome);
    root.selezionaAllenatoreOperaCome(al);
  }, nome);
}

async function terminaAsta(page) {
  await page.evaluate(() => {
    const root = angular.element(document.body).scope().$root;
    if (root.terminaAstaPerConferma) {
      root.terminaAstaPerConferma();
    } else {
      root.sendMsg(JSON.stringify({
        operazione: 'terminaAsta',
        nomegiocatore: root.nomegiocatore,
        idgiocatore: root.idgiocatore,
      }));
    }
  });
  await waitForFase(page, 'DA_CONFERMARE');
}

async function pausaAsta(page) {
  await page.evaluate(() => {
    angular.element(document.body).scope().$root.pausaAsta();
  });
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.isAstaInPausa() && r.offertaVincente && r.offertaVincente.nomegiocatore;
  }, null, { timeout: 10000 });
}

async function resumeAsta(page) {
  await page.evaluate(() => {
    angular.element(document.body).scope().$root.resumeAsta();
  });
  await waitForFase(page, 'BIDDING');
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.offertaVincente && r.offertaVincente.nomegiocatore && r.contaTempo > 0;
  }, null, { timeout: 10000 });
}

async function annullaAsta(page) {
  page.once('dialog', (dialog) => dialog.accept());
  await page.evaluate(() => {
    angular.element(document.body).scope().$root.annulla();
  });
  await waitForFase(page, 'IDLE');
}

async function confermaAsta(page) {
  await page.evaluate(() => {
    angular.element(document.body).scope().$root.conferma();
  });
  await page.waitForFunction(() => {
    const r = angular.element(document.body).scope().$root;
    return r.faseAsta === 'IDLE' && r.mapSpesoTotale && Object.keys(r.mapSpesoTotale).length > 0;
  }, null, { timeout: 20000 });
}

async function setupAdminLega(page) {
  await loginAdmin(page);
  await page.locator('input[value="AGGIORNA"]').click();
  await page.waitForTimeout(500);
  const state = await getRootState(page);
  expect(state.calciatori).toBeGreaterThan(0);
}

module.exports = {
  waitForAngular,
  loginAsta,
  loginAdmin,
  fetchInit,
  cookiesHeader,
  getRootState,
  waitBidButtonsReady,
  waitForFase,
  waitForWinner,
  startAuctionForPlayer,
  enableAutoBid,
  enableAutoBidOnPage,
  assertAutoBidControlsVisible,
  bidIncrement,
  selectOperaCome,
  terminaAsta,
  pausaAsta,
  resumeAsta,
  annullaAsta,
  confermaAsta,
  setupAdminLega,
};
