# Builder
FROM eclipse-temurin:17-jdk-jammy AS builder
LABEL authors="adarw"

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY gradle.properties .

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon || true

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/app/.gradle \
    --mount=type=cache,target=/app/build \
    ./gradlew shadowJar -PskipNanopb -x test --no-daemon && \
    cp build/libs/*-all.jar /app/app-release.jar

# Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/app-release.jar ./app.jar

CMD ["java", "-jar", "app.jar"]