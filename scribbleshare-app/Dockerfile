#
# Minify JavaScript (todo minify/compress html/css/assets)
#
FROM node:15 AS scribbleshare-frontend-build

WORKDIR /usr/src/app

COPY scribbleshare-frontend .

RUN chmod +x build.sh
RUN ./build.sh

#
# Runtime (Java 11)
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

# Copy HTTP stuff
# Everything here can be served by the HTTP server
COPY --from=scribbleshare-frontend-build /usr/src/app/build ./html

# Copy jar (should already be built with gradle shadowJar)
COPY scribbleshare-backend/build/libs/scribbleshare-backend-all.jar scribbleshare-backend.jar

ENTRYPOINT ["java", "-jar", "scribbleshare-backend.jar"]