#!/usr/bin/env bash

# Clean up old build
rm -rf build

# Copy everything
cp -r ./src build

# Remove javascript, it will be added later by Rollup
rm -rf build/scripts



npx rollup -c;
