#!/bin/bash

./build.sh

# Install rollup with barebones node project and roll up JavaScript
npm install @rollup/plugin-node-resolve @rollup/plugin-replace rollup-plugin-terser --save-dev
npx rollup -c;
