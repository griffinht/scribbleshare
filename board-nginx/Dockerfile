#
# Nginx
#
FROM nginx

COPY ../board-nginx/nginx.conf /etc/nginx/nginx.conf

WORKDIR /usr/share/nginx/html

# Everything here can be served by the HTTP server (nginx)

# Copy everyhing
COPY src .
# Remove source javascript
RUN rm -rf scripts
# Add minified javascript
COPY --from=board-frontend /usr/src/app/dist/src/scripts scripts