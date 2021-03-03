#
# Build (maven)
#
FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /usr/src/app

# Fetch and cache dependencies
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

COPY --from=build /usr/src/app/target/board-server-0.2.jar .

# This environment variable must be set to true or false.
# Using the default value here ("required") will make the server crash.
ENV board.ssl=required
ENTRYPOINT ["java", "-jar", "board-server-0.2.jar"]