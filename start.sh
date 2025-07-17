#!/bin/bash

exec java \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/heapdumps/ \
  -Xms${JAVA_MIN_HEAP:-256m} \
  -Xmx${JAVA_MAX_HEAP:-512m} \
  -javaagent:/app/opentelemetry-javaagent.jar \
  -Dotel.service.name=backend-service \
  -Dotel.exporter.otlp.endpoint=http://35.216.67.116:4317 \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.resource.attributes=deployment.environment=dev \
  -Dotel.instrumentation.jvm-metrics.enabled=true \
  -Dotel.instrumentation.runtime-telemetry.enabled=true \
  -jar app.jar "$@"
