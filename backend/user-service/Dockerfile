# ===============================
# 1️⃣ Build stage
# ===============================
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

# 캐시 최적화
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon

# 소스 복사
COPY . .

# 테스트 제외 빌드 (CI에서는 test 따로)
RUN gradle clean build -x test --no-daemon


# ===============================
# 2️⃣ Runtime stage
# ===============================
FROM amazoncorretto:21

WORKDIR /app

# timezone
ENV TZ=Asia/Seoul

# jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]