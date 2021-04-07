#
# Minify javascript with node and rollup
#
FROM node:15 AS board-frontend-js

WORKDIR /usr/src/app

COPY src src
COPY package.json rollup.config.js ./

# Install rollup and roll up javascript
RUN npm install @rollup/plugin-node-resolve rollup-plugin-terser --save-dev
RUN npx rollup -c;

#
# Nginx
#
FROM nginx

COPY nginx.conf /etc/nginx/nginx.conf

WORKDIR /usr/share/nginx/html

# Everything here can be served by the HTTP server (nginx)

# Copy everyhing
COPY src .
# Remove source javascript
RUN rm -rf scripts
# Add minified javascript
COPY --from=board-frontend-js /usr/src/app/dist/src/scripts scripts