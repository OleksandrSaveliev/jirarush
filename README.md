## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

## Required Environment Variables

The following environment variables must be set (or provided via `config/_application-prod.yaml`):

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | Required (no default) |
| `DB_USERNAME` | DB username | Required (no default) |
| `DB_PASSWORD` | DB password | Required (no default) |
| `APP_HOST_URL` | Application public URL | `http://localhost:8080` |
| `APP_TEST_MAIL` | Test notification email | Optional |
| `MAIL_USERNAME` | SMTP username (Gmail) | Optional |
| `MAIL_PASSWORD` | SMTP password (Gmail) | Optional |
| `OAUTH_GITHUB_CLIENT_ID` | GitHub OAuth2 client ID | Optional |
| `OAUTH_GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret | Optional |
| `OAUTH_GOOGLE_CLIENT_ID` | Google OAuth2 client ID | Optional |
| `OAUTH_GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | Optional |
| `OAUTH_GITLAB_CLIENT_ID` | GitLab OAuth2 client ID | Optional |
| `OAUTH_GITLAB_CLIENT_SECRET` | GitLab OAuth2 client secret | Optional |

For test profile, additionally:
| `TEST_DB_URL` | Test PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5433/jira-test` |
| `TEST_DB_USERNAME` | Test DB username | `jira` |
| `TEST_DB_PASSWORD` | Test DB password | Required (no default) |

- Есть 2 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем
      проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

Список выполненных задач:
...