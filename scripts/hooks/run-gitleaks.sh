#!/usr/bin/env bash

set -euo pipefail

if ! command -v gitleaks >/dev/null 2>&1; then
  echo "Skipping gitleaks: binary not found locally."
  echo "Install it to enforce local secret scanning before commit."
  exit 0
fi

gitleaks protect --staged --verbose
