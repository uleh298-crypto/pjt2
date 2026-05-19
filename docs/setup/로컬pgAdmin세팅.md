# 로컬 개발 환경 설정

이 문서는 공개 저장소 기준의 로컬 PostgreSQL, Redis, RabbitMQ, pgAdmin 실행 방법만 다룹니다. 운영 서버 주소, SSH 키 이름, 운영 DB 비밀번호는 저장소에 두지 않습니다.

## 1. 환경 변수 준비

프로젝트 루트에서 예제 파일을 복사해 로컬 전용 `.env`를 만듭니다.

```bash
cp .env.example .env
```

`.env`의 `change-me-*` 값을 로컬에서만 사용할 임의의 값으로 바꾸세요. `.env`는 `.gitignore`에 포함되어 Git에 올라가지 않습니다.

## 2. Docker Compose 실행

```bash
docker compose -f docker-compose-local.yml up -d
```

처음 실행하면 이미지 다운로드 때문에 몇 분이 걸릴 수 있습니다.

## 3. pgAdmin 접속

브라우저에서 `http://localhost:5050`을 엽니다.

로그인 정보는 `.env`의 값을 사용합니다.

- Email: `PGADMIN_DEFAULT_EMAIL`
- Password: `PGADMIN_DEFAULT_PASSWORD`

## 4. PostgreSQL 서버 등록

pgAdmin에서 새 서버를 등록할 때 Docker 네트워크 내부 이름을 사용합니다.

| 항목 | 값 |
|---|---|
| Host name/address | `postgres` |
| Port | `5432` |
| Maintenance database | `.env`의 `POSTGRES_DB` |
| Username | `.env`의 `POSTGRES_USER` |
| Password | `.env`의 `POSTGRES_PASSWORD` |

## 5. Spring Boot 로컬 설정

백엔드 서비스도 로컬 전용 환경 파일을 사용합니다.

```bash
cp backend/user-service/.env.example backend/user-service/.env
```

복사한 `.env`의 DB, JWT, OAuth, 메일, RabbitMQ 값을 로컬 환경에 맞게 채운 뒤 실행하세요.

## 6. Android 로컬 설정

Android 앱은 `local.properties`를 직접 커밋하지 않습니다.

```bash
cp frontend/WYE/local.properties.example frontend/WYE/local.properties
```

`KAKAO_NATIVE_APP_KEY`에는 개인 개발자 콘솔에서 발급받은 로컬 앱 키를 넣습니다.

## 7. 자주 쓰는 명령

```bash
docker compose -f docker-compose-local.yml up -d
docker compose -f docker-compose-local.yml down
docker compose -f docker-compose-local.yml down -v
docker logs wye-postgres
docker logs wye-pgadmin
docker logs wye-rabbitmq
```

`down -v`는 로컬 DB 볼륨을 삭제합니다. 운영 데이터나 공유 DB에는 사용하지 마세요.
