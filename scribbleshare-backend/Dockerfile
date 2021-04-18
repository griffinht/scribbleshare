FROM adoptopenjdk/openjdk11:alpine-jre

# Copy HTTP stuff
# Everything here can be served by the HTTP server
WORKDIR /usr/app/html

COPY --from=scribbleshare-frontend /usr/src/app/dist/ ./

# Copy actual jar
WORKDIR /usr/app

COPY --from=scribbleshare-gradle /usr/src/app/scribbleshare-backend.jar .

ENTRYPOINT ["java", "-jar", "scribbleshare-backend.jar"]