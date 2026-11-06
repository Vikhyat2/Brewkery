# ☕ Brewkery: Coffee & Bakery Ordering App

A native Android app for ordering coffee and bakery items, built in **Kotlin + Jetpack Compose** for the Clickretina Android Developer take-home. All data comes from one static JSON API.

**Flow:** Menu → *(tap item)* → Item Detail → *(Add to Cart)* → Cart → *(Place Order)* → Order Status

---

## 📱 Features

### 1. Menu / Home
- Store info banner showing the delivery ETA and the flat delivery fee, both read from the API's `meta`
- **Active order card** that appears once an order is placed and opens the order status screen
- Search that matches item names, taglines, ingredients and categories
- Category chips: All Items, Hot Coffee, Cold Brews, Artisan Bakery
- Item cards with thumbnail, badge, name, rating, review count and base price
- Cart icon with a live item count

### 2. Item Detail
- Hero image with a badge overlay, description, prep time, calories and key ingredients
- Customizations built from the item's data: `sizes`, `milk_options` and `sugar_levels`. A section only appears if the item has those options (for example, Nitro Cascara has no milk options).
- **Live price recalculation** as options and quantity change
- Loads from `api/items/{id}.json` and falls back to the item from the already-loaded menu if that request fails

### 3. Cart
- Line items with their chosen options and a − / + quantity stepper. Pressing − on the last unit removes the line.
- The same item with the same options merges into one line
- Clear Cart button and an empty-cart state
- **Subtotal + $2.50 delivery + 8% tax = Total Payable**. The fee and tax rate come from the API, not hard-coded values.

### 4. Order Status
- Placing an order clears the cart and generates a ticket ID like `#BK-74921`
- Shows the **PREPARING** status, the estimated wait and the number of items

### Loading & errors
- A loading indicator for every network call
- A readable error message (for example, no internet or a server error) with a **Try again** button

---

## 🛠 Tech Stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | MVVM: `ViewModel` and `StateFlow` UI state, plus repositories |
| Dependency injection | A small manual `AppContainer` (Hilt would be overkill for four screens) |
| Networking | Retrofit, kotlinx.serialization converter, OkHttp, coroutines |
| Images | Coil (`AsyncImage`) |
| Build | Android Gradle Plugin 9 (built-in Kotlin), compileSdk 37, minSdk 27 |

**Money handling:** prices are stored as whole cents (`Long`), and tax is rounded half-up with `BigDecimal`. This avoids floating-point errors like `0.1 + 0.2 = 0.30000000000000004`.

**Persistence:** none. The brief makes this optional, so the cart is kept in memory and resets when the app is closed.

---

## 📂 Project Structure

```
app/src/main/java/com/bookverse/brewkery/
├── BrewkeryApp.kt              # Application class + AppContainer (Retrofit, repositories)
├── MainActivity.kt             # Single activity, edge-to-edge, hosts navigation
├── data/
│   ├── remote/
│   │   ├── BrewkeryApi.kt      # Retrofit endpoints
│   │   └── MenuDto.kt          # Serializable models matching the JSON
│   ├── MenuRepository.kt       # Fetching + in-memory cache, item endpoint with fallback
│   ├── CartRepository.kt       # Shared cart state
│   ├── OrderRepository.kt      # Ticket generation + active order
│   └── ErrorMapper.kt          # Exceptions → user-friendly messages
├── domain/
│   ├── Models.kt               # CartLine, Order, OrderSummary
│   └── PriceCalculator.kt      # Pricing, tax, formatting (pure Kotlin)
└── ui/
    ├── theme/                  # Colors, typography
    ├── components/             # Shared composables (cards, buttons, stepper, loading/error)
    ├── navigation/             # NavHost + routes
    ├── menu/                   # Screen 1
    ├── detail/                 # Screen 2
    ├── cart/                   # Screen 3
    └── order/                  # Screen 4
```

---

## 🌐 API

| Endpoint | Purpose |
|---|---|
| `GET https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json` | Store meta (fees, tax, ETA), categories and all 6 menu items |
| `GET https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json` | A single item by ID (1–6) |

---

## ▶️ Running the App

1. Clone the repo and open it in **Android Studio**.
2. Let Gradle sync.
3. Run it on an emulator or a device running Android 8.1 or later.

**Build a debug APK:**
```bash
./gradlew assembleDebug
```
The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

---

## ✅ Unit Tests

```bash
./gradlew testDebugUnitTest
```

| Test | What it covers |
|---|---|
| `PriceCalculatorTest` | Size and milk surcharges, cents rounding, and the exact checkout mock: $9.40 + $2.50 + $0.75 tax = **$12.65** |
| `CartRepositoryTest` | Merging identical lines, separate lines for different options, removing a line at zero, ticket ID format |
| `MenuViewModelTest` | Initial load, category and search filtering, and an error followed by a successful retry, using a fake repository and `kotlinx-coroutines-test` |

---

## 🤖 How I Used AI

### Tools
- **Claude**: my main pair-programmer for planning the architecture, writing the data layer, Compose screens and tests, and debugging build and runtime errors.
- **Android Studio's built-in AI (Gemini)** *(edit if you didn't use it)*: inline suggestions and the "Fix with AI" option on build errors.

### Prompts I actually sent

**1. Kickoff.** I pasted the full assignment brief along with the four mock screenshots:
> "Create a new Android project in Kotlin. Add Retrofit + coroutines and fetch the JSON from the API… Screen 1 — Home / Menu… Screen 2 — Item Detail… Screen 3 — Cart… Screen 4 — Order Status… Write at least one unit test… Add a README."

**2. Debugging a Gradle sync failure.** I pasted the full stack trace:
> "An exception occurred applying plugin request [id: 'org.jetbrains.kotlin.android', version: '2.2.10'] > Cannot add extension with name 'kotlin', as there is an extension already registered with that name."

**3. Debugging a runtime crash:**
> "java.lang.ClassCastException: android.app.Application cannot be cast to com.bookverse.brewkery.BrewkeryApp at MenuViewModel.Factory…"

### ✅ What AI got right
- **Money as integer cents.** It kept every price in cents and rounded tax with `BigDecimal`, then wrote a test that reproduces the mock's exact numbers ($9.40 → $0.75 tax → $12.65). Doing the math with `Double` would have risked being off by a cent.
- **Defensive JSON parsing.** Every DTO field has a default value and the JSON parser uses `coerceInputValues`. An item with no milk options (like Nitro Cascara) therefore parses to an empty list and that section simply doesn't show, instead of crashing.
- **Clean separation of concerns.** All pricing logic lives in a plain Kotlin `PriceCalculator`, which made it easy to unit test without Android.

### ❌ What AI got wrong, and how I fixed it
1. **Its build setup was written for an older Android Gradle Plugin.** The AI generated a build that applied the `org.jetbrains.kotlin.android` plugin, but my project uses **AGP 9, which compiles Kotlin by itself**. Applying both registered the `kotlin` extension twice and the sync failed. **Fix:** I removed `kotlin-android` from the app and root build files and from the version catalog, and kept only the Compose and serialization compiler plugins.
2. **The version catalog was incomplete when merged into my project.** The AI's build file referenced `libs.androidx.lifecycle.runtime.compose`, but that entry wasn't in my `libs.versions.toml`, which caused `Unresolved reference 'compose'`. It had also pinned lifecycle to 2.8.7 while my template used 2.11.0. **Fix:** I added the missing catalog entries and aligned all lifecycle libraries on one version.
3. **The app crashed on launch with a `ClassCastException`.** The ViewModel factories cast the application to `BrewkeryApp`, but my template's `AndroidManifest.xml` never registered that class, so Android created a plain `Application`. **Fix:** I added `android:name=".BrewkeryApp"` to the `<application>` tag. That taught me that the AI's code assumed its own manifest, and that merging generated code into an existing project needs a careful check of the manifest and package name.

### What I learned
AI was very fast at producing the architecture and UI, but it couldn't see my local setup: the AGP version, my version catalog and my template manifest. Every bug came from the gap between the generated code and my existing project. I fixed them by reading the stack traces, giving the AI exact error output, and checking that each fix made sense before applying it.

---

*Built for the Clickretina Android Developer take-home assignment.*