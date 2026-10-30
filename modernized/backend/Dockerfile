# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# Maven Central rate-limits shared egress IPs (429); mirror through Google's cache.
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<settings><mirrors><mirror><id>gcs-central</id><mirrorOf>central</mirrorOf>' \
    '<url>https://maven-central.storage-download.googleapis.com/maven2/</url>' \
    '</mirror></mirrors></settings>' > /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/carddemo-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
