#!/usr/bin/env bash

set -euo pipefail

if ! command -v actionlint >/dev/null 2>&1; then
  echo "Skipping actionlint: binary not found locally."
  echo "Install it to enforce workflow linting before commit."
  exit 0
fi

actionlint
