#
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

# Copy HTTP stuff
# Everything here can be served by the HTTP server
WORKDIR /usr/app/html

COPY --from=board-frontend /usr/src/app/dist/ ./

# Copy actual jar
WORKDIR /usr/app

COPY --from=board-gradle /usr/src/app/board-backend.jar .

ENTRYPOINT ["java", "-jar", "board-backend.jar"]