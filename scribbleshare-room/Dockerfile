FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY --from=scribbleshare-gradle /usr/src/app/scribbleshare-room.jar .

ENTRYPOINT ["java", "-jar", "scribbleshare-room.jar"]