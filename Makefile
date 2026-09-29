.PHONY: up down test build
up:
	docker compose up --build
down:
	docker compose down
test:
	cd backend && mvn test
	cd frontend && npm test
build:
	cd backend && mvn -DskipTests package
	cd frontend && npm run build

