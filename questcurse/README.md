# Quest & Curse (NeoForge 1.21.11)

Каждые X минут игроку выдаётся задание (добыть предмет / выкопать блоки / убить мобов).
- Выполнил: шанс 80% на случайное благословение (баф).
- Провалил: 50/50 — «потеря органа» (дебаф на время) ИЛИ спавн мобов / TNT / молний.
- Проклятие проходит само через `curseMinutes` или снимается за алмазы.

Команды: `/quest` (статус), `/quest cleanse` (снять проклятия за алмазы),
`/quest new` (тест, только если `debugCommand = true` в конфиге).
Конфиг: `config/questcurse-common.toml`. Состояние сохраняется между перезапусками.

## Сборка
1. Нужна Java 21. В папке проекта нет gradle wrapper — либо возьми `gradlew`, `gradlew.bat`
   и папку `gradle/` из официального MDK (github.com/NeoForgeMDKs/MDK-1.21.11-ModDevGradle),
   либо выполни `gradle wrapper` с установленным Gradle.
2. `./gradlew build` → jar в `build/libs/`. Тест: `./gradlew runClient`.
3. В `gradle.properties` при необходимости уточни `neo_version`
   (список: projects.neoforged.net/neoforged/neoforge).
