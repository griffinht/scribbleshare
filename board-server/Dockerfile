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
# Runtime (alpine)
#
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /usr/app

COPY --from=board-server /usr/src/app/target/board-server-0.2.jar .

COPY --from=board-web-client /usr/src/app/board-web-client board-web-client

# Failure to set required environment variables will cause the server to crash
ENV board.ssl=required
ENV board.postgres=required
ENV board.document.root.path=board-web-client
ENTRYPOINT ["java", "-jar", "board-server-0.2.jar"]