#!/bin/bash
set -euo pipefail
# -e: 에러 발생 시 스크립트 즉시 종료
# -u: 정의되지 않은 변수 사용 시 에러
# -o pipefail: 파이프라인에서 실패한 명령어가 있을 때 전체 실패로 처리

echo "♻️ [ROLLBACK] 이전 버전으로 롤백 시작"

APP_NAME="nemo-backend"
PREV_JAR="previous.jar"

# 1. 기존 프로세스 종료
echo "🛑 롤백 전 기존 PM2 프로세스 종료: $APP_NAME"
pm2 delete "$APP_NAME" || echo "ℹ️ 실행 중인 프로세스 없음"

# 2. previous.jar 존재 여부 확인
if [ ! -f "$PREV_JAR" ]; then
  echo "❌ 롤백 실패: $PREV_JAR 파일이 존재하지 않음"
  exit 1
fi

# 3. 이전 JAR 실행
echo "🚀 이전 버전 실행 중: $PREV_JAR"
pm2 start "java -jar $PREV_JAR" --name "$APP_NAME" --env .env

# 4. 롤백 후 헬스체크 (최대 3회, 3초 간격)
echo "🩺 롤백 후 헬스체크 시작..."
for i in {1..3}; do
  if ./validate.sh; then
    echo "✅ 롤백 성공 및 헬스체크 통과"
    exit 0
  fi
  echo "⏳ 헬스체크 재시도 ($i/3)..."
  sleep 3
done

# 5. 롤백 후 헬스체크 실패
echo "❌ 롤백 후에도 헬스체크 실패 - 수동 점검 필요"
exit 1
