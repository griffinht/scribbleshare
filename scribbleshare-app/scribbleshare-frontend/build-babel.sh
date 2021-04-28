#!/bin/bash

rm -rf build

npx babel src/scripts --out-file build/scripts/main.js