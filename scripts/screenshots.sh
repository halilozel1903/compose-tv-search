#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running Android TV emulator.
# D-pad presses can't be timed reliably through adb on a fresh emulator, so the sample sets up
# each scene from the `scene` extra. All content comes from the sample's bundled demo catalog.
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

install_sample

fresh_launch --es scene empty
capture tv-search-empty "Recent searches"

fresh_launch --es scene typed
capture tv-search-typed "Lightning Relay"

fresh_launch --es scene result
capture tv-search-result "Light Years Apart"
