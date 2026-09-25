#!/usr/bin/env bash
# Suite completa: Java + Playwright (core + browser flow).
# Uso: ./scripts/run-s6-tests.sh
# Solo Java: SKIP_E2E=1 ./scripts/run-s6-tests.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Test automatici Java (unit + integrazione WS)"
mvn -q test -Dtest=AstaSessionRegistryTest,AstaWebSocketIntegrationTest,FantaLiveWebSocketIntegrationTest

if [[ "${SKIP_E2E:-}" == "1" ]]; then
  echo "==> SKIP_E2E=1: test browser Playwright saltati"
  exit 0
fi

if ! curl -sf "${BASE_URL:-http://localhost:8080}/fantaasta/init" >/dev/null 2>&1; then
  echo "==> Server non raggiungibile su ${BASE_URL:-http://localhost:8080}"
  echo "    Avvia il server (es. PROFILO=DEVTEMPLATE mvn spring-boot:run) oppure imposta BASE_URL"
  echo "    Per solo test Java: SKIP_E2E=1 ./scripts/run-s6-tests.sh"
  exit 1
fi

BASE_URL="${BASE_URL:-http://localhost:8080}"
echo "==> S6: seed dati test (DEVTEMPLATE: POST /fantaasta/test/seed)"
curl -sf -X POST "${BASE_URL}/fantaasta/test/seed" >/dev/null || true

echo "==> Test browser Playwright (core + browser flow)"
cd tests/e2e
if [[ ! -d node_modules ]]; then
  npm install --no-fund --no-audit
  npx playwright install chromium
fi
BASE_URL="${BASE_URL:-http://localhost:8080}" npm test

echo "==> S6 completata"
