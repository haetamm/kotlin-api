
FROM gradle:8.10-jdk17-alpine AS builder


WORKDIR /app


COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .


RUN ./gradlew build --no-daemon || return 0


COPY src src


RUN ./gradlew bootJar -x test --no-daemon


FROM eclipse-temurin:17-jre-alpine


RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app


COPY --from=builder /app/build/libs/*.jar app.jar


EXPOSE 8080


ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar /app/app.jar"]