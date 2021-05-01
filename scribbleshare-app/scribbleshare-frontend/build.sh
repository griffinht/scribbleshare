#!/bin/bash


# Clean up old build
rm -rf build

# Copy everything
cp -r ./src build

# Remove javascript, it will be added later by Rollup
rm -rf build/scripts



# Rollup (minify)
npm install @rollup/plugin-node-resolve @rollup/plugin-replace rollup-plugin-terser @rollup/plugin-typescript --save-dev

npx rollup -c;
