#
# Minify JavaScript (todo minify/compress html/css/assets)
#
FROM node:15 AS scribbleshare-frontend-build

WORKDIR /usr/src/app

COPY scribbleshare-frontend .

RUN chmod +x build-typescript.sh \
build-typescript.sh


#
# Runtime (Java 11)
#
FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk --no-cache add curl

WORKDIR /usr/app

# Copy HTTP stuff
# Everything here can be served by the HTTP server
COPY --from=scribbleshare-frontend-build /usr/src/app/build ./html

# Copy jar (should already be built with gradle shadowJar)
COPY scribbleshare-backend/build/libs/scribbleshare-backend-all.jar scribbleshare-backend.jar

HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD curl --fail http://localhost:80/ || exit 1

ENTRYPOINT ["java", "-jar", "scribbleshare-backend.jar"]