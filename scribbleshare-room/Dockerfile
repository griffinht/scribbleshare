#
# Runtime
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/src

COPY build/libs/scribbleshare-room-all.jar scribbleshare-room.jar

ENTRYPOINT ["java", "-jar", "scribbleshare-room.jar"]