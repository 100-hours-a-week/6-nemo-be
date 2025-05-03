#!/bin/bash
set -euo pipefail
# -e: 에러 발생 시 즉시 종료
# -u: 정의되지 않은 변수 사용 시 에러
# -o pipefail: 파이프라인 중 하나라도 실패 시 전체 실패 처리

PORT=8080
HEALTH_URL="http://localhost:${PORT}/actuator/health"

echo "🔎 헬스체크 요청: $HEALTH_URL"

# 요청 → 상태코드가 200이면 성공
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL")

if [ "$STATUS" == "200" ]; then
  echo "✅ 헬스체크 통과 (200 OK)"
  exit 0
else
  echo "❌ 헬스체크 실패 (응답 코드: $STATUS)"
  exit 1
fi
