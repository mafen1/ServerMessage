# ServerMessage — backend мессенджера

![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-3.0-BF360C?style=flat-square&logo=ktor&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![Tests](https://img.shields.io/badge/tests-5%20integration-25A162?style=flat-square&logo=junit5&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-yellow?style=flat-square)

Backend для [MessageApp](https://github.com/mafen1/MessageApp): REST API + WebSocket real-time чат, JWT-аутентификация, хранилище обёрток ключей E2E с версионированием эпох, развёртывание одной командой через docker compose.

## Архитектура

```
 Android (MessageApp)                ServerMessage (Ktor :8081)           PostgreSQL
┌──────────────────────┐   HTTPS    ┌─────────────────────────────┐      ┌─────────────┐
│  Compose UI          │◄──────────►│ REST: login/register/friends│◄────►│ users       │
│  Room + WorkManager  │  JWT auth  │       news / images         │Exposed messages │
│  AES-GCM + RSA       │            │                             │      │ news        │
└──────────┬───────────┘            │  WebSocket /chat/{user}     │      │ friend_req  │
           │     WSS (JWT)          │  WebSocket /friendMessage   │      └─────────────┘
           └───────────────────────►│  + chat-keys (E2E epochs)   │
                                    └─────────────────────────────┘
 Сервер хранит только шифротексты и RSA-обёртки ключей — прочитать переписку он не может.
```

## 🚀 Технологии

- **Kotlin** — основной язык
- **Ktor** — серверный фреймворк
- **WebSockets** — real-time обмен сообщениями
- **Koin** — Dependency Injection
- **PostgreSQL** — основная база данных (Exposed ORM + HikariCP)
- **kotlinx.serialization** — сериализация JSON
- **JWT + bcrypt** — аутентификация и хранение паролей
- **Gradle (Kotlin DSL + version catalog)** — сборка проекта
- **Docker / docker-compose** — контейнеризация

## 📋 Подключенные плагины

| Плагин | Назначение |
|--------|-----------|
| `Routing` | Структурированная маршрутизация запросов |
| `WebSockets` | Двусторонняя связь в реальном времени |
| `Koin` | Внедрение зависимостей (DI) |
| `Content Negotiation` | Автоматическое согласование контента |
| `Call Logging` | Логирование HTTP-запросов |
| `Authentication JWT` | JWT-аутентификация |
| `RateLimit` | Ограничение частоты запросов |
| `CORS` | Настройка кросс-доменных запросов |

## 🛠 Установка и запуск

### Требования

- JDK 11+
- Gradle 8+
- PostgreSQL 12+

### Переменные окружения

| Переменная | Описание | Обязательная |
|------------|----------|--------------|
| `JWT_SECRET` | Секретный ключ для подписи JWT | Да |
| `DB_POSTGRES_URL` | URL подключения к PostgreSQL | Нет (default: `jdbc:postgresql://localhost:5432/server_message`) |
| `DB_POSTGRES_USER` | Пользователь БД | Нет |
| `DB_POSTGRES_PASSWORD` | Пароль БД | Нет |

### Запуск локально

```bash
git clone https://github.com/mafen1/ServerMessage.git
cd ServerMessage
export JWT_SECRET="your-very-secret-key-here"
./gradlew run
```

Сервер будет доступен по адресу: `http://0.0.0.0:8081`

### Запуск в Docker (сервер + PostgreSQL одной командой)

```bash
git clone https://github.com/mafen1/ServerMessage.git
cd ServerMessage
export JWT_SECRET="$(openssl rand -hex 32)"
docker compose up --build
```

Сервер будет доступен по адресу `http://localhost:8081`. Данные PostgreSQL и загруженные изображения сохраняются в docker-томах между перезапусками.

Альтернативно можно собрать только образ:

```bash
docker build -t servermessage .
docker run -p 8081:8081 -e JWT_SECRET="your-secret" servermessage
```

## 📁 Структура проекта

```
SERVERMESSAGE/
├── src/main/kotlin/
│   └── com/example/
│       ├── Application.kt              # Точка входа
│       ├── authentication/             # Настройка JWT-аутентификации
│       ├── data/database/              # Подключение к PostgreSQL
│       ├── di/                         # Koin-модули
│       ├── friend/                     # Друзья и WebSocket-уведомления
│       ├── login/                      # Регистрация и вход
│       ├── message/                    # Сообщения и чат WebSocket
│       ├── news/                       # Новости
│       ├── security/                   # Хеширование паролей, ключи E2E-шифрования
│       ├── user/                       # Пользователи
│       └── util/                       # Утилиты
├── src/main/resources/
│   └── application.conf                # Конфигурация приложения
├── build.gradle.kts
├── Dockerfile
└── docker-compose.yml
```

## 📡 API Endpoints

### Аутентификация

Большинство endpoint'ов требуют заголовок:

```
Authorization: Bearer <jwt-token>
```

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| `POST` | `/register` | Регистрация нового пользователя | Нет |
| `POST` | `/login` | Вход, возвращает JWT и пользователя | Нет |
| `GET`  | `/me` | Текущий авторизованный пользователь | Да |
| `GET`  | `/allUser` | Список всех пользователей | Да |
| `POST` | `/findUserByName` | Найти пользователя по username | Да |
| `POST` | `/findUserByStr` | Поиск пользователей по префиксу | Да |
| `POST` | `/updateProfile` | Обновить профиль | Да |

### Сообщения

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| `GET`  | `/messages/{user1}/{user2}` | История сообщений между двумя пользователями | Да |
| `POST` | `/uploadMessageImage` | Загрузить изображение для сообщения | Да |
| `GET`  | `/images/{name}?token=<jwt>` | Получить загруженное изображение | Да (query/header) |

### WebSocket

- `ws://localhost:8081/chat/{username}` — чат
- `ws://localhost:8081/friendMessage/{username}` — уведомления о заявках в друзья

JWT передаётся заголовком `Authorization: Bearer <jwt>` (query-параметр `?token=` поддерживается как fallback для старых клиентов). Доставка сообщений разрешена только между друзьями.

### Друзья

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| `POST` | `/requestFriend` | Отправить заявку в друзья | Да |
| `POST` | `/acceptFriend` | Принять заявку | Да |
| `POST` | `/rejectFriend` | Отклонить заявку | Да |
| `GET`  | `/friends/{username}` | Список друзей | Да |
| `GET`  | `/friendRequests/{username}` | Входящие заявки | Да |

### Новости

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| `GET`  | `/allNews` | Лента новостей | Да |
| `POST` | `/uploadNews` | Создать новость с изображением | Да |
| `POST` | `/uploadNewsWithOutImage` | Создать новость без изображения | Да |
| `POST` | `/news/like` | Лайк/анлайк | Да |
| `POST` | `/news/comment` | Комментарий | Да |

### Ключи E2E-шифрования

Сервер выступает доверенным хранилищем публичных ключей и обёрток (RSA-wrapped) AES-ключей чатов. Обёртки версионированы: каждая публикация инкрементит эпоху чата, что позволяет клиентам синхронизироваться после ротации ключей.

| Метод | Путь | Описание | Авторизация |
|-------|------|----------|-------------|
| `POST` | `/keys` | Загрузить свой публичный RSA-ключ | Да |
| `GET`  | `/keys/{username}` | Получить публичный ключ пользователя | Да |
| `POST` | `/chat-keys/publish` | Атомарно опубликовать обёртки чат-ключа для участников (возвращает назначенную эпоху) | Да |
| `POST` | `/chat-keys` | Legacy: одиночная публикация обёртки через ту же эпоху | Да |
| `GET`  | `/chat-keys/{chatId}?recipient=<я>` | Получить свою обёртку ключа чата с версией | Да |

## 🔧 Конфигурация БД

### PostgreSQL

Настройка подключения через переменные окружения:

```bash
export DB_POSTGRES_URL="jdbc:postgresql://localhost:5432/server_message"
export DB_POSTGRES_USER="postgres"
export DB_POSTGRES_PASSWORD="password"
```

## 🔐 Безопасность

- Пароли хранятся в виде bcrypt-хешей (cost 12).
- JWT-подпись требует `JWT_SECRET`; без него сервер не запустится.
- WebSocket-подключения требуют валидный JWT, имя в пути обязано совпадать с токеном.
- Историю чата может читать только его участник; доставка сообщений — только между друзьями.
- E2E-шифрование сообщений: сервер хранит только шифротексты и RSA-обёртки ключей; читать можно только собственную копию обёртки, публиковать — только участникам чата.
- Загрузка изображений ограничена по размеру (10 МБ) и типу (jpg, jpeg, png, webp), имена файлов генерируются на сервере (UUID).
- Статические изображения доступны только с валидным JWT.
- Rate limits: аутентификация (10/мин), загрузка файлов (20/мин), глобальный лимит (100/мин).
- Интеграционные тесты покрывают регистрацию/логин, JWT, проверку доступа к ключам E2E (5 сценариев).

## 📈 Возможности для расширения

- [ ] Групповые чаты
- [ ] Уведомления о доставке и прочтении
- [ ] Пагинация сообщений и новостей
- [ ] HTTPS/TLS в production
- [x] Unit-тесты (регистрация/логин, JWT, эндпоинты ключей E2E)

## 👨‍💻 Автор

**mafen1** — [GitHub](https://github.com/mafen1)

## 📄 Лицензия

MIT
