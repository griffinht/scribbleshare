#
# Build server with maven
#
FROM maven:3.6.0-jdk-11-slim AS board-server
WORKDIR /usr/src/app

# Fetch and cache dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Compile source code with cached dependencies
COPY src src
RUN mvn package

#
# Add HTTP code
#
FROM scratch AS board-web-client

WORKDIR /usr/src/app

COPY board-web-client board-web-client

#
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY --from=board-server /usr/src/app/target/board-server-0.2.jar .

COPY --from=board-web-client /usr/src/app/board-web-client board-web-client

# This environment variable must be set to true or false.
# Using the default value here ("required") will make the server crash.
ENV board.ssl=required
ENV board.document.root.path=board-web-client
ENTRYPOINT ["java", "-jar", "board-server-0.2.jar"]