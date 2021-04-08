#
# Minify JavaScript (todo minify/compress html/css/assets)
#
FROM node:15 AS board-frontend-build

WORKDIR /usr/src/app

COPY src src
COPY package.json rollup.config.js ./

# Install rollup and roll up JavaScript
RUN npm install @rollup/plugin-node-resolve rollup-plugin-terser --save-dev
RUN npx rollup -c;
# Minifed JavaScript is now in dist/

# Remove source javascript
RUN rm -rf src/scripts
# Copy everything remaining (html, css, assets)
COPY src dist

#
# Store result in fresh layer (significantly cuts down on size) for other Docker images to use
#
FROM scratch

WORKDIR /usr/src/app

COPY --from=board-frontend-build /usr/src/app/dist dist/
# Example usage from other image
# COPY --from=board-frontend /usr/src/app/dist .

# todo add CMD/ENTRYPOINT or something to make docker stop complaining not being able to start this service