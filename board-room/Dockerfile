#
# Build server with maven
#
FROM maven:3.6.0-jdk-11-slim AS board-room-java

# Fetch and cache dependencies
WORKDIR /usr/src/app

COPY pom.xml .
RUN mvn dependency:resolve

# Compile source code with cached dependencies
COPY src src
RUN mvn package

#
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY --from=board-room-java /usr/src/app/target/board-room-0.3.jar .

ENTRYPOINT ["java", "-jar", "board-room-0.3.jar"]