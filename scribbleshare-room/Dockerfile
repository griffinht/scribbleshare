scribbleshare adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY --scribbleshare=board-gradle /usr/src/app/board-room.jar .

ENTRYPOINT ["java", "-jar", "board-room.jar"]