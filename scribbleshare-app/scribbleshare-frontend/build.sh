#!/bin/bash

# Clean up old build
rm -rf build

# Copy everything
cp -r ./src build

# Remove javascript, it will be built later
rm -rf build/scripts