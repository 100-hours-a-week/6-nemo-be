FROM eclipse-temurin:21-jre

WORKDIR /app

# 🔹 힙덤프 디렉토리 생성 및 OpenTelemetry Java Agent 다운로드
RUN mkdir -p /app/heapdumps
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.3.0/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT [
  "java",
  "-XX:+HeapDumpOnOutOfMemoryError",
  "-XX:HeapDumpPath=/app/heapdumps/",
  "-Xms${JAVA_MIN_HEAP:-256m}",
  "-Xmx${JAVA_MAX_HEAP:-512m}",
  "-javaagent:/app/opentelemetry-javaagent.jar",
  "-Dotel.service.name=backend-service",
  "-Dotel.exporter.otlp.endpoint=http://35.216.67.116:4317",
  "-Dotel.exporter.otlp.protocol=grpc",
  "-Dotel.resource.attributes=deployment.environment=dev",
  "-Dotel.instrumentation.jvm-metrics.enabled=true",
  "-Dotel.instrumentation.runtime-telemetry.enabled=true",
  "-jar", "app.jar"]
