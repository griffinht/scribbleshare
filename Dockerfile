#
# Build Gradle project
#
# todo gradle 7 currently won't build with some wierd error
FROM gradle:6.7-jdk11 AS scribbleshare-gradle-build

WORKDIR /usr/src/app

COPY . .
RUN gradle shadowJar

#
# Store resulting binaries in fresh layer (significantly cuts down on size) for other Docker images to use
#
FROM scratch

WORKDIR /usr/src/app

COPY --from=scribbleshare-gradle-build /usr/src/app/scribbleshare-backend/build/libs/scribbleshare-backend-all.jar scribbleshare-backend.jar
COPY --from=scribbleshare-gradle-build /usr/src/app/scribbleshare-room/build/libs/scribbleshare-room-all.jar scribbleshare-room.jar
# Example usage from other image
# COPY --from=scribbleshare-gradle /usr/src/app/example.ext .

CMD sleep 0