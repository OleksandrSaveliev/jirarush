# JiraRush — Implementation Tasks

> **Source:** `C:\Users\asave\Downloads\ua.javarush.spring.presentation.level25 (1).html` (JavaRush University final project brief, in Ukrainian)
> **Purpose:** Extracted, reorganized task list. This file is a reference for planning — no implementation plan yet.
> **Delivery note:** The brief requires listing in `README.md` which of the following tasks were completed.

## Project Background

- JiraRush is a "board of tasks" (Jira/Trello-like) for tracking activity: projects, sprints, tasks, tags, activities, attachments, users.
- You do **not** write it from scratch — you modify existing code, add missing functionality, and configure infrastructure without breaking anything.
- The project follows a **Spring Modulith-like** structure: `internal` packages are not accessible from outside the top-level package (designed with future microservice splitting in mind).
- DB schema and dictionaries are applied by **Liquibase** on startup. If you change the table structure, delete the two service tables `databasechangelog` and `databasechangeloglock` so Liquibase re-runs the init script.
- **Swagger** UI at `http://localhost:8080/doc` — documentation only, no tasks on it.
- **Caffeine cache** — no tasks on it, just understand how it's wired.
- You will be using **JPQL** (Spring Data JPA), not native SQL.
- Test roles: work through the app as `ADMIN`, `MANAGER`, and `DEV`.

## Environment / DB Setup (context)

Two Postgres containers (documented in the brief and `doc/postgres-docker.md`):

```
docker run -p 5432:5432 --name postgres-db -e POSTGRES_USER=jira -e POSTGRES_PASSWORD=JiraRush -e POSTGRES_DB=jira -e PGDATA=/var/lib/postgresql/data/pgdata -v ./pgdata:/var/lib/postgresql/data -d postgres
docker run -p 5433:5432 --name postgres-db-test -e POSTGRES_USER=jira -e POSTGRES_PASSWORD=JiraRush -e POSTGRES_DB=jira-test -e PGDATA=/var/lib/postgresql/data/pgdata -v ./pgdata-test:/var/lib/postgresql/data -d postgres
```

- `5432/jira` → app runs with the **prod** profile; `5433/jira-test` → tests (**test** profile).
- Build: `mvn clean install`. Run: `JiraRushApplication` with profile `prod`.
- The DB is populated with structure + dictionaries on startup; run `resources/data4dev/data.sql` to get demo data.

---

## Task List (grouped by difficulty)

### 🟢 Onboarding / Easy

#### Task 1 — Onboarding: understand the project
- **What:** Read through the codebase before touching anything: what problem the project solves, dependencies (`pom.xml`), DB structure, entity structure, API/controllers, and the responsibility of each service.
- **Guidance (from brief):** examine in order — task of the project → dependencies → DB structure → entities → API/controllers → services.
- **Files to study:** `pom.xml`, `src/main/resources/db/changelog.sql`, all entity classes, all `*Controller` / `*UIController`, services (`TaskService`, `ActivityService`, `ReferenceService`, `MailService`, handlers in `Handlers.java`).
- **Existing:** covered in `AGENTS.md` + `.opencode/` skills.

#### Task 2 — Remove VK and Yandex social login (Easy)
- **What:** Remove the `vk` and `yandex` OAuth2 providers so only Google, GitHub, GitLab remain.
- **Current state:**
  - `src/main/resources/application.yaml` — has `spring.security.oauth2.client.registration.{vk,yandex}` + `provider.{vk,yandex}` blocks.
  - `src/main/java/com/javarush/jira/login/internal/sociallogin/handler/VkOAuth2UserDataHandler.java` and `YandexOAuth2UserDataHandler.java` — provider handlers.
  - `resources/view/login.html` and `resources/view/unauth/register.html` — social buttons for vk/yandex.
- **Files likely affected:** `application.yaml`, the two handler classes (remove), `login.html`, `register.html`, and any related OAuth2 data handler wiring.

#### Task 3 — Move sensitive config to environment variables (Easy)
- **What:** Externalize secrets from `application.yaml` into machine environment variables, read at server startup:
  - DB login + password
  - OAuth2 client IDs and secrets (registration/authorization)
  - Mail (SMTP) settings
- **Current state:** `application.yaml` has hardcoded committed secrets (DB password `JiraRush`, OAuth client secrets, Gmail SMTP password). The prod config `config/_application-prod.yaml` already provides an override pattern to follow.
- **Files likely affected:** `application.yaml` (use `${ENV_VAR:default}` placeholders), possibly `config/_application-prod.yaml`, `README.md`/docs.
- **Note (security rule):** this is explicitly a repo hygiene task — no new secrets should be added to tracked files.

#### Task 4 — Refactor `FileUtil#upload` to modern NIO (Easy)
- **What:** Rework `com.javarush.jira.bugtracking.attachment.FileUtil#upload` to use the modern `java.nio.file` approach instead of the legacy `java.io` API.
- **Current state:** `src/main/java/com/javarush/jira/bugtracking/attachment/FileUtil.java` uses `java.io.File`, `FileOutputStream`, and `multipartFile.getBytes()` (whole-file buffering). Modern approach: `Path`, `Files.copy(...)` / `Files.newOutputStream(...)`, `MultipartFile.transferTo(...)`.
- **Files likely affected:** `FileUtil.java` (+ tests if any).

---

### 🟡 Medium

#### Task 5 — Write tests for `ProfileRestController` (all public methods)
- **What:** Cover all public methods of `ProfileRestController` with controller tests. Only 2 methods, but write more test cases: **successful and unsuccessful paths** (unauthorized, invalid payload, etc.).
- **Current state:** `src/main/java/com/javarush/jira/profile/internal/web/ProfileRestController.java` has 2 methods:
  - `GET /api/profile` — returns the current user's `ProfileTo`.
  - `PUT /api/profile` — updates the current user's profile (`@Valid @RequestBody ProfileTo`).
  - Existing test: `src/test/java/com/javarush/jira/profile/internal/web/ProfileRestControllerTest.java` + `ProfileTestData.java` (extend coverage).
- **Files likely affected:** `ProfileRestControllerTest.java` (new test cases), possibly `ProfileTestData.java`.

#### Task 6 — Add task tags feature (REST API + service) (Medium)
- **What:** Add the ability to attach **tags** to a task: REST API + implementation on the service layer. Frontend is optional.
- **Current state:**
  - Table `task_tag` **already exists** in `src/main/resources/db/changelog.sql` (line ~153): columns `TASK_ID`, `TAG`, unique constraint `UK_TASK_TAG(TASK_ID, TAG)`, FK to `TASK` with cascade delete.
  - **No** `TaskTag` entity, repository, mapper, TO, service method, or endpoint exists yet.
- **Files likely affected (see `.opencode/skills/add-entity/SKILL.md`):** new `TaskTag` entity, repository, mapper/TO, service logic in `TaskService` (or a dedicated service), REST endpoints in `TaskController` (e.g. add/remove tags on `/api/tasks/{id}`), tests (`TaskControllerTest` + `TaskTestData`), maybe `data.sql` seed rows.

#### Task 7 — Calculate time a task spent in "in work" and "in testing" (Medium)
- **What:** Two service-level methods that take a task and return the elapsed time:
  1. **Time in work** — `ready_for_review` activity timestamp minus `in_progress` activity timestamp.
  2. **Time in testing** — `done` activity timestamp minus `ready_for_review` activity timestamp.
- **Current state:**
  - Status flow in `REFERENCE` (ref_type 3): `todo → in_progress → ready_for_review → review → ready_for_test → test → done` (and `canceled`).
  - No `ACTIVITY` seed rows in `changelog.sql` yet — the brief requires adding **3 records** to the end of `src/main/resources/db/changelog.sql` into table `ACTIVITY` (columns `ID, AUTHOR_ID, TASK_ID, UPDATED, STATUS_CODE`) with statuses:
    - work start → `in_progress`
    - dev finished → `ready_for_review`
    - testing finished → `done`
- **Files likely affected:** `changelog.sql` (3 ACTIVITY inserts), `ActivityService`/`TaskService` (2 new methods), `ActivityRepository` (query for status timestamps), tests.

#### Task 8 — Write a `Dockerfile` for the main server (Medium)
- **What:** Containerize the Spring Boot server.
- **Current state:** no `Dockerfile` in the repo. Build is Maven; app is `JiraRushApplication` (Spring Boot 3 / Java 17). Consider a multi-stage build (Maven build stage → JRE run stage).
- **Files likely affected:** new `Dockerfile`, possibly `.dockerignore`.

#### Task 9 — Localization (at least 2 languages) for mail templates + start page (Medium)
- **What:** Add i18n for **at least two languages** for:
  - mail templates in `resources/mails/` (`email-confirmation.html`, `password-reset.html`)
  - the start page `resources/view/index.html`
- **Current state:** templates are single-language (Ukrainian/English mix). No `messages*.properties` resources found yet; Thymeleaf is configured (`ThymeleafConfig`, `templates-update-cache`). The existing `layout/header.html`, `index.html`, and `MailService`/mail listeners build mail content.
- **Files likely affected:** new `messages.properties` + `messages_XX.properties`, `ThymeleafConfig` (message source), mail templates, `index.html` and its layout fragments, `MailService` (localized subjects/bodies), docs.

---

### 🔴 Hard

#### Task 10 — `docker-compose` for server + DB + nginx (Hard)
- **What:** Write a `docker-compose` file that runs the **server container together with PostgreSQL and nginx**. Use `config/nginx.conf` for nginx (may edit the config if needed).
- **Current state:** `config/nginx.conf` and `config/_application-prod.yaml` already exist; `doc/config-nginx.md` documents the nginx setup. No `docker-compose.yml` yet.
- **Files likely affected:** new `docker-compose.yml`, possibly `config/nginx.conf` and `config/_application-prod.yaml`, `Dockerfile` (from Task 8).

#### Task 11 — Switch auth from `JSESSIONID` to `JWT` (Very hard)
- **What:** Replace the session-cookie ("own/foreign" recognition) mechanism between frontend and backend with **JWT**.
- **Current state:** `SecurityConfig.java` uses session-based form login (`JSESSIONID` cookie) for the UI chain and HTTP Basic for `/api/**`. Frontend sends regular form posts / AJAX with cookies (`resources/static/js/*.js`).
- **Main difficulty:** the frontend must be reworked so every form submission / AJAX request adds the JWT authorization header.
- **Files likely affected:** new JWT provider/filter + `SecurityConfig`, new auth/login endpoint issuing tokens, `common.js`/`dashboard.js`/`users.js`/`references.js`/etc. (attach `Authorization` header), login page JS, `application.yaml` (JWT secret config), tests (auth strategy in `@WithUserDetails`-based controller tests).

---

## Delivery Checklist

- [ ] Update `README.md` with the list of completed tasks (required by the brief).
- [ ] All chosen features covered by tests where applicable (`mvn test -Dtest=<Class>`).
- [ ] No regressions in the existing test suite (`mvn test`).
