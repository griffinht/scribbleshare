#!/bin/bash


# Clean up old build
rm -rf build

# Copy everything
cp -r ./src build

# Remove javascript, it will be added later by Rollup
rm -rf build/scripts



# Rollup (minify)
npm install --save-dev \
@rollup/plugin-node-resolve \
@rollup/plugin-replace \
rollup-plugin-terser \
@rollup/plugin-typescript typescript tslib

npx rollup -c;
