FROM gradle:7.3.3-jdk11-alpine AS GRADLE_BUILD

COPY ./ ./

RUN gradle clean build

RUN ls -la build/libs

FROM openjdk:11-jre-slim

EXPOSE 8080
COPY --from=GRADLE_BUILD /build/libs/tinkoffbot-1.0.0-TEST.jar /app.jar

ENTRYPOINT ["java", "-jar", "./app.jar"]
