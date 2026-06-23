#!/usr/bin/env bash

set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "Skipping iOS verification: this hook only runs on macOS."
  exit 0
fi

if ! command -v xcodebuild >/dev/null 2>&1; then
  echo "Skipping iOS verification: xcodebuild not found."
  exit 0
fi

xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO \
  build
