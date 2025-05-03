#!/bin/bash
set -euo pipefail
# -e: 에러 발생 시 스크립트 즉시 종료
# -u: 정의되지 않은 변수 사용 시 에러
# -o pipefail: 파이프라인 중 하나라도 실패 시 전체 실패 처리

echo "🚀 [START] 백엔드 서비스(Spring Boot) 배포 시작"

APP_NAME="nemo-backend"
JAR="latest.jar"

# 1. 기존 PM2 프로세스 종료
echo "🛑 기존 PM2 프로세스 종료: $APP_NAME"
pm2 delete "$APP_NAME" || echo "ℹ️ 기존 PM2 프로세스 없음"

# 2. 최신 JAR 실행 (.env에서 환경변수 로딩)
echo "🚀 최신 JAR 실행 중: $JAR"
pm2 start "java -jar $JAR" --name "$APP_NAME" --env .env

# 3. 헬스체크 (최대 5회, 3초 간격 재시도)
echo "🩺 헬스체크 시작..."
for i in {1..5}; do
  if ./validate.sh; then
    echo "✅ 배포 및 헬스체크 성공"
    exit 0
  fi
  echo "⏳ 헬스체크 재시도 ($i/5)..."
  sleep 3
done

# 4. 롤백
echo "❌ 헬스체크 5회 실패! 롤백 시도"
./rollback.sh

# 5. 실패 종료 (CD에서 실패로 인식되게)
exit 1
