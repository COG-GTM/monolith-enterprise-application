# ---------------------------------------------------------------------------
# Stage 1 – Build the uber-jar with Maven
# ---------------------------------------------------------------------------
FROM maven:3.8-openjdk-11-slim AS build

WORKDIR /app

# Cache dependency resolution
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and build (skip tests – they require a running DB)
COPY src ./src
RUN mvn clean package -DskipTests -B \
    && cp target/Snowman-jar-with-dependencies.jar /app/snowman.jar

# ---------------------------------------------------------------------------
# Stage 2 – Lightweight runtime image
# ---------------------------------------------------------------------------
FROM eclipse-temurin:11-jre-alpine

LABEL maintainer="snowman-team"
LABEL description="Snowman Enterprise Application"

WORKDIR /app

COPY --from=build /app/snowman.jar ./snowman.jar

# Default application port
EXPOSE 8090

# Allow JVM tuning via JAVA_OPTS; default port via -Dport
ENV JAVA_OPTS=""
ENV APP_PORT=8090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dport=$APP_PORT -jar /app/snowman.jar"]
