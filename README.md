# 🌍 LifeOS: Your Intelligent Life Assistant

[![Platform](https://img.shields.io/badge/Platform-Android%20Native-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![AI](https://img.shields.io/badge/AI-Gemini%203.5%20Flash-orange.svg)](https://deepmind.google/technologies/gemini/)
[![Storage](https://img.shields.io/badge/Storage-Room%20Database-teal.svg)](https://developer.android.com/training/data-storage/room)

**LifeOS** is a global all-in-one AI-powered personal life assistant designed to help people organize, plan, and manage their everyday lives from one simple, cohesive native Android application.

Instead of navigating through dozens of disconnected apps, you simply describe what you want in natural language, and LifeOS synthesizes an organized, practical, and actionable plan.

---

## 🌎 Core Capabilities

### 🧠 Smart Dashboard
- **Glanceable Overview**: Tasks pending, important reminders, money spent today, weekly goal completion ring.
- **Natural Language Command Bar**: Instant prompt bar right at your fingertips to ask or plan anything.
- **Quick Action Presets**: One-tap access to common life planning sprints.

### 🤖 AI Life Assistant & Command Center
- **Natural Language Parsing**: Turn complex prompts like *"I'm traveling to Turkey next month for 7 days with a budget of $800"* into structured, day-by-day itineraries, budget breakdowns, checklists, and local translations.
- **One-Tap LifeOS Import**: Directly save and populate generated action tasks, trip plans, study milestones, and savings goals into your local database with one tap!
- **Gemini 3.5 Flash & Offline Intelligence**: Online dynamic AI planning with automatic offline fallback so you are never stranded without your plan.

### ✈️ Travel Hub
- Complete destination scheduling and day-by-day itineraries.
- Budget allocation tracking (Flights, Hotels, Food, Activities).
- Interactive packing checklist with checkboxes.
- Key local phrases and instant translations (e.g. Turkish, French, Spanish, Arabic).

### 🎓 Study Hub
- Course and subject organization.
- Exam countdown timers.
- Spaced repetition and active recall study schedules.
- Visual progress mastery slider (0% to 100%).

### 💼 Work & Career Hub
- Job search tracker and application status pipeline (Saved, Applied, Interviewing, Offer).
- Interview preparation roadmaps and technical checklists.
- Professional goals and skill certification milestones.

### 💰 Personal Finance Hub
- Cash flow tracker: Income vs. Expense ledger with visual indicators.
- Category breakdowns (Food, Transport, Housing, Shopping, Travel).
- Savings Goals with interactive quick-deposit buttons.
- Real-time travel currency conversion calculator (USD, TRY, EUR, GBP, SAR).
- Educational & organizational focus.

### 📅 Tasks & Organization
- Comprehensive to-do list with category filtering (General, Study, Work, Travel, Finance, Shopping).
- Priority badges (HIGH, MEDIUM, LOW) and due dates.
- Interactive checkboxes with completion tracking.

### 🛒 Shopping & Wishlists
- Categorized shopping lists (Grocery, Tech, Home, Clothing).
- Separate personal wishlist tracker.
- Automatic total and pending budget calculation.

### 🌐 Global Multilingual Support
Built-in instant language switcher supporting:
- 🇺🇸 English
- 🇸🇦 Arabic (العربية - with native RTL layout support)
- 🇫🇷 French (Français)
- 🇪🇸 Spanish (Español)
- 🇩🇪 German (Deutsch)
- 🇹🇷 Turkish (Türkçe)
- 🇵🇹 Portuguese (Português)
- 🇮🇹 Italian (Italiano)
- 🇨🇳 Chinese (中文)
- 🇯🇵 Japanese (日本語)

### 🔐 Privacy & Local Security
- 100% on-device private Room database storage.
- No third-party data tracking or selling of your schedule.
- Instant "Wipe All Local Data" button for total user control.

### 💎 Free + Pro Tier Architecture
- Free tier: Generous access to all core planning modules.
- Pro tier: Unlimited AI synthesis, deeper analytics, and ad-free experience.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.2+
- **UI Toolkit:** Jetpack Compose with Material Design 3 (M3)
- **Local Persistence:** Room Database with KSP & Coroutines Flow
- **Networking & API:** OkHttp, Retrofit & Moshi
- **AI Model:** Gemini 3.5 Flash (`gemini-3.5-flash`)
- **Image Loading:** Coil Compose
- **Design System:** Custom Dark/Light theme with modern glassmorphism aesthetic

---

## 🚀 Building & Running

1. Clone the repository:
   ```bash
   git clone https://github.com/Ahmedbecett/LifeOS.git
   ```
2. Open the project in **Android Studio Meerkat** or newer.
3. Configure your `GEMINI_API_KEY` in `.env` (optional, smart offline engine works out of the box).
4. Run on an Android device or emulator with API Level 24+ (Android 7.0+).
