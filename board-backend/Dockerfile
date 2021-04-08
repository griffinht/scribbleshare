#
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

# Copy HTTP stuff
# Everything here can be served by the HTTP server
WORKDIR /usr/app/html

# Copy everything
COPY src .
# Remove source javascript
RUN rm -rf scripts
# Add minified javascript
COPY --from=board-frontend /usr/src/app/dist/src/scripts scripts

# Copy actual jar
WORKDIR /usr/app

COPY --from=board-backend-jar build/libs/board-backend-all.jar .

ENTRYPOINT ["java", "-jar", "board-backend-all.jar"]