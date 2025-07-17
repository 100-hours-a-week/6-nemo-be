FROM eclipse-temurin:21-jre

WORKDIR /app

# 🔹 힙덤프 디렉토리 생성 및 OpenTelemetry Java Agent 다운로드
RUN mkdir -p /app/heapdumps
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.3.0/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

COPY build/libs/*.jar app.jar

EXPOSE 8080

# 환경 변수 기본값 설정
ENV OTEL_SERVICE_NAME=backend-service
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://35.216.67.116:4317
ENV OTEL_ENVIRONMENT=dev
ENV JAVA_MIN_HEAP=256m
ENV JAVA_MAX_HEAP=512m

ENTRYPOINT sh -c "java \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/heapdumps/ \
  -Xms\${JAVA_MIN_HEAP} \
  -Xmx\${JAVA_MAX_HEAP} \
  -javaagent:/app/opentelemetry-javaagent.jar \
  -Dotel.service.name=\${OTEL_SERVICE_NAME} \
  -Dotel.exporter.otlp.endpoint=\${OTEL_EXPORTER_OTLP_ENDPOINT} \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.resource.attributes=deployment.environment=\${OTEL_ENVIRONMENT} \
  -Dotel.instrumentation.jvm-metrics.enabled=true \
  -Dotel.instrumentation.runtime-telemetry.enabled=true \
  -Dotel.metric.export.interval=5000 \
  -jar app.jar"
