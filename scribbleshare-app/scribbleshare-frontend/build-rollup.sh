#!/bin/bash

rm -rf build

# Minifies frontend code and stores output in dist/

# Copy everything to dist
cp -r ./src build
# Except for javascript, which will be added minifed
rm -rf build/scripts

# Install rollup with barebones node project and roll up JavaScript
npm install @rollup/plugin-node-resolve @rollup/plugin-replace rollup-plugin-terser --save-dev
npx rollup -c;
