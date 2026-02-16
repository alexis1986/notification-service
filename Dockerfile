FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean install -DskipTests

FROM amazoncorretto:21-alpine AS final
WORKDIR /app

RUN apk add --no-cache bash && \
    addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /bin/bash -D appuser

USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
