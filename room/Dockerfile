#
# Runtime
#
FROM adoptopenjdk/openjdk11:alpine-jre
RUN apk --no-cache add curl

WORKDIR /usr/src

COPY build/libs/scribbleshare-room-all.jar scribbleshare-room.jar

HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD curl --fail http://localhost:80/healthcheck || exit 1
ENTRYPOINT ["java", "-jar", "scribbleshare-room.jar"]