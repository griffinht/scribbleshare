#
# Build Gradle project
#
FROM gradle:jdk11 AS board-gradle-build

WORKDIR /usr/src/app

COPY . .
#RUN rm -rf gradle
RUN ls .
RUN gradle shadowJar --stacktrace

FROM scratch

WORKDIR /usr/src/app

COPY --from=board-gradle-build /usr/src/app/board-backend/build/libs/board-backend-all.jar board-backend.jar
COPY --from=board-gradle-build /usr/src/app/board-room/build/libs/board-room-all.jar board-room.jar