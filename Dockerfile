#
# Build Gradle project
#
FROM gradle:jdk11 AS board-gradle-build

WORKDIR /usr/src/app

COPY . .
RUN gradle shadowJar

#
# Store resulting binaries in fresh layer (significantly cuts down on size) for other Docker images to use
#
FROM scratch

WORKDIR /usr/src/app

COPY --from=board-gradle-build /usr/src/app/board-backend/build/libs/board-backend-all.jar board-backend.jar
COPY --from=board-gradle-build /usr/src/app/board-room/build/libs/board-room-all.jar board-room.jar
# Example usage from other image
# COPY --from=board-gradle /usr/src/app/example.ext .