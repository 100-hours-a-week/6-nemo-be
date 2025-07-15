FROM eclipse-temurin:21-jre

WORKDIR /app

# 🔹 OpenTelemetry Java Agent 다운로드
RUN curl -L -o opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.3.0/opentelemetry-javaagent.jar

COPY build/libs/*.jar app.jar

EXPOSE 8080

ARG OTEL_ENVIRONMENT

ENV OTEL_SERVICE_NAME=backend-service
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://35.216.67.116:4317
ENV OTEL_ENVIRONMENT=${OTEL_ENVIRONMENT}


ENTRYPOINT sh -c "java \
  -javaagent:/app/opentelemetry-javaagent.jar \
  -Dotel.service.name=\${OTEL_SERVICE_NAME} \
  -Dotel.exporter.otlp.endpoint=\${OTEL_EXPORTER_OTLP_ENDPOINT} \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.resource.attributes=deployment.environment=\${OTEL_ENVIRONMENT} \
  -Dotel.instrumentation.jvm-metrics.enabled=true \
  -Dotel.instrumentation.runtime-telemetry.enabled=true \
  -Dotel.metric.export.interval=5000 \
  -jar app.jar"

