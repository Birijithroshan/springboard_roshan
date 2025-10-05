#!/usr/bin/env bash
set -euo pipefail

# Simple Automation Framework Runner
# Usage examples:
#   ./run-tests.sh            # all suites
#   ./run-tests.sh api        # API only
#   ./run-tests.sh ui         # UI only
# Env overrides: THREADS=5 HEADLESS=false DB_ENABLED=false ARTIFACTS_ENABLED=false

MODE="${1:-all}"
THREADS="${THREADS:-10}"
# If running UI mode and user did not explicitly export THREADS, default to 1 to avoid parallel driver setup
if [[ "$MODE" == "ui" && -z "${THREADS_OVERRIDE:-}" ]]; then
  # Users can set THREADS_OVERRIDE=1 env to signal they've chosen a value already
  THREADS=1
fi
HEADLESS="${HEADLESS:-true}"
DB_ENABLED="${DB_ENABLED:-false}"   # default false to avoid DB dependency
ARTIFACTS_ENABLED="${ARTIFACTS_ENABLED:-true}"
MAVEN_ARGS="${MAVEN_ARGS:-}"

case "$MODE" in
  all) SUITE_FILE="testng.xml" ;;
  api) SUITE_FILE="testng-api.xml" ;;
  ui)  SUITE_FILE="testng-ui.xml" ;;
  *) echo "[ERROR] Unknown mode: $MODE (expected: all|api|ui)" >&2; exit 2 ;;
esac

echo "[Runner] Mode=$MODE Suite=$SUITE_FILE Threads=$THREADS Headless=$HEADLESS DB=$DB_ENABLED Artifacts=$ARTIFACTS_ENABLED"

if [[ "$MODE" != "api" ]]; then
  if ! command -v google-chrome >/dev/null 2>&1 && ! command -v chromium-browser >/dev/null 2>&1; then
    echo "[WARN] Chrome/Chromium not detected; UI tests might fail to start browser" >&2
  fi
fi

mkdir -p artifacts/api artifacts/screenshots target/screenshots target/tmp || true

MVN_CMD=(mvn -q test \
  -Dtestng.suiteFile="${SUITE_FILE}" \
  -Dtest.threadCount="${THREADS}" \
  -Dheadless="${HEADLESS}" \
  -Ddb.enabled="${DB_ENABLED}" \
  -Dartifacts.enabled="${ARTIFACTS_ENABLED}")

if [[ -n "$MAVEN_ARGS" ]]; then
  # shellcheck disable=SC2206
  EXTRA_ARGS=( $MAVEN_ARGS )
  MVN_CMD+=( "${EXTRA_ARGS[@]}" )
fi

echo "[INFO] Executing: ${MVN_CMD[*]}"
"${MVN_CMD[@]}"
STATUS=$?

if [[ $STATUS -eq 0 ]]; then
  echo "[SUCCESS] Tests completed successfully"
else
  echo "[FAILURE] Tests finished with failures (exit $STATUS)" >&2
fi

exit $STATUS
