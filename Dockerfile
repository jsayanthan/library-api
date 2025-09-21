# ---- Build stage ---------------------------------------------------------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
# Pre-fetch dependencies to leverage Docker layer caching
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:17-jre
LABEL org.opencontainers.image.source="https://github.com/jsayanthan/library-api" \
      org.opencontainers.image.title="library-api" \
      org.opencontainers.image.description="Library API Spring Boot service" \
      org.opencontainers.image.licenses="Apache-2.0"

# Non-root user for security
RUN useradd -r -u 1001 appuser
WORKDIR /app
COPY --from=build /workspace/target/library-api-*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
