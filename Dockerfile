FROM amazoncorretto:21-alpine AS build
WORKDIR /app

# Mismo Maven que local y CI (wrapper); las dependencias quedan en una capa propia.
# Con sh y no ./mvnw: un build desde Windows no conserva el bit de ejecución.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sh mvnw -B -q dependency:go-offline

COPY src ./src
RUN sh mvnw -B -q package -DskipTests && \
    java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

FROM amazoncorretto:21-alpine AS final
WORKDIR /app

RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /sbin/nologin -D appuser

# Capas de menos a más cambiante: un cambio de código solo invalida la última
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

USER appuser

EXPOSE 8080

# El heap se dimensiona según el límite de memoria del contenedor, no el del nodo
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# Kubernetes usa sus propios probes; este healthcheck es para docker/compose
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
