#
# Minify javascript with node and rollup
#
FROM node:15

WORKDIR /usr/src/app

COPY src src
COPY package.json rollup.config.js ./

# Install rollup and roll up javascript
RUN npm install @rollup/plugin-node-resolve rollup-plugin-terser --save-dev
RUN npx rollup -c;