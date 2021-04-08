#
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY --from=board-gradle /usr/src/app/board-room.jar .

ENTRYPOINT ["java", "-jar", "board-room.jar"]