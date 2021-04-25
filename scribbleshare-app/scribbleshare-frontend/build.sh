#!/bin/bash
# Minifies frontend code and stores output in dist/

# Copy everything to dist
cp -r ./src dist
# Except for javascript, which will be added minifed
rm -rf dist/scripts

# Install rollup with barebones node project and roll up JavaScript
npm install @rollup/plugin-node-resolve @rollup/plugin-replace rollup-plugin-terser --save-dev
npx rollup -c;
