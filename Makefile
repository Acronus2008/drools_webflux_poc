.PHONY: build up down logs test clean help

# Variables
COMPOSE_FILE = docker-compose.yml
CONTAINER_NAME = rules-engine-poc
BASE_URL = http://localhost:8080

help: ## Mostrar esta ayuda
	@echo "Comandos disponibles:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Construir la imagen Docker
	docker-compose build

up: ## Levantar el contenedor
	docker-compose up -d

down: ## Detener el contenedor
	docker-compose down

logs: ## Ver logs del contenedor
	docker-compose logs -f $(CONTAINER_NAME)

restart: ## Reiniciar el contenedor
	docker-compose restart

clean: ## Limpiar contenedores e imágenes
	docker-compose down -v
	docker rmi rules-engine-poc-rules-engine 2>/dev/null || true

health: ## Verificar health check
	curl -s $(BASE_URL)/actuator/health | jq '.' || curl -s $(BASE_URL)/actuator/health

upload-rule: ## Subir una regla (uso: make upload-rule FILE=path/to/rule.drl)
	@if [ -z "$(FILE)" ]; then \
		echo "Error: Especifica el archivo con FILE=path/to/rule.drl"; \
		exit 1; \
	fi
	curl -X POST $(BASE_URL)/api/dynamic-rules/upload -F "file=@$(FILE)" | jq '.' || curl -X POST $(BASE_URL)/api/dynamic-rules/upload -F "file=@$(FILE)"

list-rules: ## Listar reglas dinámicas
	curl -s $(BASE_URL)/api/dynamic-rules/list | jq '.' || curl -s $(BASE_URL)/api/dynamic-rules/list

reload-rules: ## Recargar reglas dinámicas
	curl -X POST $(BASE_URL)/api/dynamic-rules/reload | jq '.' || curl -X POST $(BASE_URL)/api/dynamic-rules/reload

test-static: ## Probar reglas estáticas
	curl -X POST $(BASE_URL)/api/rules/evaluate?complexity=ALL \
		-H "Content-Type: application/json" \
		-d @example-transaction.json | jq '.' || curl -X POST $(BASE_URL)/api/rules/evaluate?complexity=ALL \
		-H "Content-Type: application/json" \
		-d @example-transaction.json

test-dynamic: ## Probar reglas dinámicas
	curl -X POST $(BASE_URL)/api/dynamic-rules/evaluate \
		-H "Content-Type: application/json" \
		-d @example-transaction.json | jq '.' || curl -X POST $(BASE_URL)/api/dynamic-rules/evaluate \
		-H "Content-Type: application/json" \
		-d @example-transaction.json

all: build up ## Construir y levantar todo
	@echo "Esperando a que el contenedor esté listo..."
	@sleep 10
	@make health

