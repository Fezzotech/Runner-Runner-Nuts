# Runner Runner Nuts

A modern, high-performance Texas Hold'em Poker application for Android, built from the ground up using **Jetpack Compose**, **Material 3**, and pure **Kotlin**.

Designed for an authentic, fluid poker experience with deep stack dynamics, multi-way pot resolution, and intuitive UI.

---

## Screenshots

<p align="center">
  <img src="docs/screenshots/01_lobby.png" width="23%" alt="Lobby Screen" />
  <img src="docs/screenshots/02_table_action.png" width="23%" alt="Table Action" />
  <img src="docs/screenshots/03_allin_showdown.jpg" width="23%" alt="All-In Showdown" />
  <img src="docs/screenshots/04_poker_slang.png" width="23%" alt="Poker Slang Dictionary" />
</p>

---

## Features

- **Prestige Star Economy (High Stakes, Zero Real Money)**: Traditional free poker apps suffer from reckless "bingo poker" because play-money has no value. *Runner Runner Nuts* introduces a **Star Reputation System**: running out of chips requires sacrificing scarce, earned Stars to rebuy. This attaches genuine emotional weight and strategic consequence to every chip and fold—delivering authentic, high-tension poker psychology completely free from real-money gambling.
- **Practice Arena vs. Competitive Tables**: Offers a dedicated, risk-free Practice Arena with instant chip recovery to test strategies, alongside high-stakes tables where stack preservation and star management truly matter.
- **AMOLED Dark Theme**: Pure `#08080A` styling engineered for OLED displays and battery efficiency.
- **Smart Bot AI**: Opponents make mathematically sound decisions based on pot odds, hand equity, position, and texture, with realistic bet sizing.
- **Side Pot & All-In Engine**: Accurate multi-way pot resolution with comprehensive winner breakdowns (Main Pot vs. Side Pots).
- **Diagnostic Log Exporter**: In-app debug logging and one-click log export / clipboard sharing for troubleshooting.
- **Audio & Haptics**: Immersive sound effects for chip clicks, shuffles, card deals, and winning fanfares.
- **Pure Offline & Local Play**: Play anywhere without mandatory internet connectivity.

---

## Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Android Jetpack Compose with Material 3
- **Architecture**: MVI / StateFlow-driven unidirectional data flow
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)
- **Compatibility**: Android 8.0 (API level 26) and above

---

## Building from Source

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 or JDK 21
- Android SDK 35

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/Fezzotech/Runner-Runner-Nuts.git
cd Runner-Runner-Nuts

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```
The output APK will be generated at `app/build/outputs/apk/release/app-release.apk`.

---

## License

This project is open-source software licensed under the MIT License.
