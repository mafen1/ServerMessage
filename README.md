# SERVERMESSAGE

Backend-сервер для мессенджера, разработанный на **Ktor** (Kotlin) с поддержкой WebSocket для real-time коммуникации.

## 🚀 Технологии

- **Kotlin** — основной язык
- **Ktor** — серверный фреймворк
- **WebSockets** — real-time обмен сообщениями
- **Koin** — Dependency Injection
- **PostgreSQL** — основная база данных
- **MongoDB** — документо-ориентированная БД
- **Gson / kotlinx.serialization** — сериализация JSON
- **Gradle (Kotlin DSL)** — сборка проекта
- **Docker** — контейнеризация

## 📋 Подключенные плагины

| Плагин | Назначение |
|--------|-----------|
| `Routing` | Структурированная маршрутизация запросов |
| `WebSockets` | Двусторонняя связь в реальном времени |
| `Koin` | Внедрение зависимостей (DI) |
| `Content Negotiation` | Автоматическое согласование контента |
| `Call Logging` | Логирование HTTP-запросов |
| `Call ID` | Идентификация запросов |
| `Request Validation` | Валидация входящих данных |
| `Authentication Basic` | Базовая HTTP-аутентификация |
| `Sessions` | Управление сессиями |
| `Caching Headers` | Кэширование на клиенте |
| `Static Content` | Раздача статических файлов |

## 🛠 Установка и запуск

### Требования

- JDK 11+
- Gradle 7+

### Запуск локально

```bash
git clone https://github.com/mafen1/SERVERMESSAGE.git
cd SERVERMESSAGE
./gradlew run
```

Сервер будет доступен по адресу: `http://0.0.0.0:8080`

### Запуск в Docker

```bash
./gradlew buildImage
docker run -p 8080:8080 servermessage
```

## 📁 Структура проекта

```
SERVERMESSAGE/
├── src/main/kotlin/
│   └── com/mafen/servermessage/
│       ├── Application.kt      # Точка входа
│       ├── plugins/            # Подключенные плагины Ktor
│       └── routing/            # Маршруты API
├── src/main/resources/
│   └── application.conf        # Конфигурация приложения
├── build.gradle.kts
└── Dockerfile
```

## 📡 API Endpoints

### WebSocket

- `ws://localhost:8080/ws` — подключение для обмена сообщениями в реальном времени

### HTTP

- `GET /` — проверка работоспособности сервера

## 🔧 Конфигурация БД

### PostgreSQL

Настройка подключения в `application.conf`:

```hocon
postgres {
    host = "localhost"
    port = 5432
    database = "message_db"
    user = "postgres"
    password = "password"
}
```

### MongoDB

```hocon
mongodb {
    connection = "mongodb://localhost:27017"
    database = "message_db"
}
```

## 📈 Возможности для расширения

- [ ] Реализация REST API для управления пользователями
- [ ] Авторизация и регистрация через JWT
- [ ] Хранение истории сообщений в БД
- [ ] Групповые чаты
- [ ] Уведомления о доставке и прочтении
- [ ] Unit-тесты

## 👨‍💻 Автор

**mafen1** — [GitHub](https://github.com/mafen1)

## 📄 Лицензия

MIT
