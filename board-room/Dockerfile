#
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY build/libs/board-room-all.jar .

ENTRYPOINT ["java", "-jar", "board-room-all.jar"]