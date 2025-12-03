# Project Overlord — Implementation Status

**Last Updated:** December 2, 2025  
**Session Summary:** Journey Planner Refactor - Trainline-style UX with destination-first input, new components

---

## Quick Start

```bash
# Build, install, and launch the app
./build_and_run.sh

# View logs
adb logcat -s Overlord:* AlarmScheduler:* AlarmService:*
```

---

## Completed Phases

### ✅ Phase 1: Visual Identity (Complete)

**New color scheme:** Dark theme with deep red (`#8B1538`) and gold (`#D4A853`) accents.

**Files created/modified:**
- `presentation/theme/Color.kt` — Full color palette (DeepRed, Gold, Surface variants, semantic colors)
- `presentation/theme/Type.kt` — Typography with Lexend font, dyslexia-optimized settings
- `presentation/theme/Theme.kt` — Dynamic font support, composition locals

**Fonts added:**
- `res/font/lexend_regular.ttf`
- `res/font/lexend_medium.ttf`
- `res/font/lexend_semibold.ttf`
- `res/font/lexend_bold.ttf`
- `res/font/opendyslexic_regular.otf`
- `res/font/opendyslexic_bold.otf`
- `res/font/opendyslexic_italic.otf`

**Features:**
- WCAG AA compliant contrast ratios
- Dyslexia-friendly typography (larger sizes, 1.5x+ line height, letter spacing)
- Dyscalculia support (large time displays, explicit AM/PM)

---

### ✅ Phase 2: App Architecture (Complete)

**Navigation structure implemented:**

```
┌─────────────────────────────────────────────────────────┐
│ Top Bar (Deep Red) with hamburger menu                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Content Area                                           │
│                                                         │
├─────────────────────────────────────────────────────────┤
│ Bottom Nav: Home | Journeys | Tasks | Notes             │
└─────────────────────────────────────────────────────────┘
```

**Files created:**
- `presentation/navigation/NavigationItems.kt` — Enums for bottom nav & drawer items, Routes object
- `presentation/navigation/OverlordDrawer.kt` — Sidebar with profile, plan badge, menu items
- `presentation/navigation/OverlordBottomNav.kt` — Bottom navigation bar
- `presentation/navigation/OverlordScaffold.kt` — Main scaffold + reusable components (PrimaryButton, SecondaryButton, FeatureCard, NeutralCard)

**Navigation routes defined:**
- `home` — Dashboard
- `journey_planner` — Plan a journey
- `route_list` — Select from routes
- `settings` — App settings
- `alarm_test` — Developer alarm testing

---

### ✅ Phase 3: Data Layer (Complete)

**New database entities for alarm profile templates:**

**Files created:**
- `data/database/entity/AlarmProfileTemplateEntity.kt`
  - `AlarmProfileTemplateEntity` — Profile container (name, description, isSystemDefault)
  - `AlarmTemplateEntity` — Individual alarm template (label, offsetMinutes, sound, etc.)
  - `ProfileWithAlarms` — Relation class

- `data/database/entity/ScheduledJourneyEntity.kt`
  - `ScheduledJourneyEntity` — Journey with route info, status
  - `ScheduledAlarmEntity` — Scheduled alarm instance (customizable label, time, sound)
  - `JourneyWithAlarms` — Relation class
  - `JourneyStatus` enum — UPCOMING, IN_PROGRESS, COMPLETED, CANCELLED

**DAOs created:**
- `data/database/dao/AlarmProfileTemplateDao.kt` — CRUD + `duplicateProfile()` transaction
- `data/database/dao/ScheduledJourneyDao.kt` — Journey & alarm management

**Database updated:**
- `OverlordDatabase.kt` — Version 3, migration from v2, seeds 3 default profiles on first run

**Default profiles seeded:**
1. "Standard Morning" — 3 alarms (Wake Up, Get Out of Bed, Leave Home)
2. "Full Morning Routine" — 7 alarms (includes Shower, Getting Ready, Pack Reminder)
3. "Quick Trip" — 2 alarms (30-Min Warning, Leave Now)

---

### ✅ Phase 4: Home Screen (Complete)

**Files created:**
- `presentation/screen/HomeScreen.kt`
  - Dashboard with upcoming journey cards
  - Empty state with "Plan a journey" prompt
  - Journey cards showing destination, arrival time, alarm count
  - Action buttons: View Details, Edit, Cancel

**Features:**
- Deep red journey cards following Visual Identity
- Gold accent for icons and buttons
- Sample journey data for testing

---

### ✅ Settings with Font Switching (Complete)

**Files created:**
- `data/preferences/UserPreferences.kt`
  - `AppFont` enum — LEXEND, OPEN_DYSLEXIC, SYSTEM_DEFAULT
  - `UserPreferencesData` — Preferences container
  - `UserPreferencesRepository` — DataStore-backed storage

- `presentation/screen/SettingsScreen.kt`
  - Font picker with live preview
  - Sections: Accessibility, Locations, Notifications, Developer, About
  - Alarm Test link in Developer section

**Font switching flow:**
1. User opens Settings → taps Font
2. Dialog shows all 3 fonts with previews
3. Selection saves to DataStore
4. Theme recomposes with new font immediately

---

## Current Navigation Flow

```
App Launch
    ↓
Permissions Screen (if needed)
    ↓
Home Screen (Dashboard)
    ├── Sidebar Drawer (swipe left or tap ☰)
    │   ├── Account
    │   ├── Activity  
    │   ├── Settings → Settings Screen
    │   │                 └── Alarm Test → AlarmTestScreen
    │   ├── Resources
    │   └── Support
    │
    ├── Bottom Nav
    │   ├── Home (current)
    │   ├── Journeys → Journey Planner
    │   ├── Tasks (TODO)
    │   └── Notes (TODO)
    │
    └── "Plan a journey" card → Journey Planner
                                    ↓
                               Route List
                                    ↓
                            (Alarm Setup - TODO)
```

---

## Current Phase

### ✅ Phase 5: Journey Planner Refactor (Complete)
**Goal:** Implement Trainline-style UX with destination-first input

**Completed:**
- ✅ `WhereCard.kt` — Destination-first location input with dotted connector
- ✅ `ArriveByCard.kt` — Time selection with date/time dropdowns
- ✅ `TimePickerBottomSheet.kt` — Modal time picker with Arrive/Depart toggle
- ✅ `DatePickerBottomSheet.kt` — Modal date picker with quick select buttons
- ✅ `JourneyPlannerViewModel` refactored — New state shape, Arrive-by default
- ✅ `JourneyPlannerScreen` refactored — Uses new components

**Deferred to Phase 6:**
- 🔲 Google Places API integration (LocationSearchScreen)
- 🔲 Save/load Home address from UserPreferences
- 🔲 Recent places in location search

---

### ✅ Phase 7: Alarm Setup Screen (Complete)
**Goal:** Profile selection and alarm customization after route selection

**Completed:**
- ✅ `AlarmSetupViewModel.kt` — State management for alarm setup flow
- ✅ `JourneySummaryCard.kt` — Route summary display
- ✅ `ProfileSelectionBottomSheet.kt` — Profile selection with preview
- ✅ `ProfileSelectionCard.kt` — Compact profile display card
- ✅ `AlarmRowItem.kt` — Individual alarm row with toggle, time, label editing
- ✅ `AlarmReviewList.kt` — List of all alarms
- ✅ `AlarmTimeAdjustBottomSheet.kt` — Quick adjust alarm times
- ✅ `AlarmLabelEditDialog.kt` — Edit alarm labels
- ✅ `AlarmSetupScreen.kt` — Main alarm setup screen
- ✅ Navigation wired up from RouteListScreen → AlarmSetupScreen → Home

**Files created:**
```
presentation/viewmodel/
└── AlarmSetupViewModel.kt       (state management)

presentation/components/
├── JourneySummaryCard.kt        (route summary)
├── ProfileSelectionBottomSheet.kt (profile picker)
└── AlarmRowItem.kt              (alarm row + editing components)

presentation/screen/
└── AlarmSetupScreen.kt          (main screen)
```

**Features implemented:**
- Profile selection from database-seeded templates
- Auto-calculation of alarm times based on departure
- Toggle individual alarms on/off
- Quick-adjust alarm times (+/- 5, 15, 30 minutes)
- Edit alarm labels
- Schedule all alarms to database
- Navigation flow: Journey Planner → Route List → Alarm Setup → Home

**Still needed (future phases):**
- 🔲 Spotify integration for alarm sounds
- 🔲 Sound picker bottom sheet
- 🔲 Duplicate & Edit profile flow
- 🔲 Create new profile from scratch
- 🔲 Actual AlarmManager scheduling (fires Android alarms)

---

## Pending Phases

---

### 🔲 Phase 6: Google Places API
**Goal:** Autocomplete for location search

**Files to create:**
- `data/repository/PlacesRepository.kt`
- `data/api/PlacesApiService.kt`
- `data/model/PlacePrediction.kt`
- `presentation/screen/LocationSearchScreen.kt`

**API key:** Already configured in `local.properties` as `GOOGLE_MAPS_API_KEY`

---

### 🔲 Phase 8: Profile Management
**Goal:** Create, edit, and manage alarm profile templates

**Features needed:**
- Profile list screen
- Profile editor (add/remove/reorder alarms)
- Per-alarm sound selection (local + Spotify)
- Delete profile with confirmation

---

## File Structure Summary

```
app/src/main/
├── java/com/productions666/overlord/
│   ├── data/
│   │   ├── database/
│   │   │   ├── dao/
│   │   │   │   ├── AlarmInstanceDao.kt
│   │   │   │   ├── AlarmProfileDao.kt
│   │   │   │   ├── AlarmProfileTemplateDao.kt    [NEW]
│   │   │   │   ├── JourneyDao.kt
│   │   │   │   └── ScheduledJourneyDao.kt        [NEW]
│   │   │   ├── entity/
│   │   │   │   ├── AlarmInstanceEntity.kt
│   │   │   │   ├── AlarmProfileEntity.kt
│   │   │   │   ├── AlarmProfileTemplateEntity.kt [NEW]
│   │   │   │   ├── JourneyEntity.kt
│   │   │   │   └── ScheduledJourneyEntity.kt     [NEW]
│   │   │   ├── Converters.kt                     [UPDATED]
│   │   │   └── OverlordDatabase.kt               [UPDATED - v3]
│   │   ├── preferences/
│   │   │   └── UserPreferences.kt                [NEW]
│   │   └── ...
│   ├── presentation/
│   │   ├── components/                           [NEW - Phase 5]
│   │   │   ├── WhereCard.kt                      [NEW - destination-first]
│   │   │   ├── ArriveByCard.kt                   [NEW - time selection]
│   │   │   ├── TimePickerBottomSheet.kt          [NEW - modal picker]
│   │   │   └── DatePickerBottomSheet.kt          [NEW - modal picker]
│   │   ├── navigation/
│   │   │   ├── NavigationItems.kt                [NEW]
│   │   │   ├── OverlordBottomNav.kt              [NEW]
│   │   │   ├── OverlordDrawer.kt                 [NEW]
│   │   │   └── OverlordScaffold.kt               [NEW]
│   │   ├── screen/
│   │   │   ├── AlarmTestScreen.kt
│   │   │   ├── HomeScreen.kt                     [NEW]
│   │   │   ├── JourneyPlannerScreen.kt           [REFACTORED - Phase 5]
│   │   │   ├── PermissionRequestScreen.kt
│   │   │   ├── RouteListScreen.kt
│   │   │   └── SettingsScreen.kt                 [NEW]
│   │   ├── theme/
│   │   │   ├── Color.kt                          [NEW]
│   │   │   ├── Theme.kt                          [REWRITTEN]
│   │   │   └── Type.kt                           [REWRITTEN]
│   │   ├── viewmodel/
│   │   │   └── JourneyPlannerViewModel.kt        [REFACTORED - Phase 5]
│   │   └── MainActivity.kt                       [UPDATED]
│   └── ...
├── res/
│   └── font/
│       ├── lexend_regular.ttf                    [NEW]
│       ├── lexend_medium.ttf                     [NEW]
│       ├── lexend_semibold.ttf                   [NEW]
│       ├── lexend_bold.ttf                       [NEW]
│       ├── opendyslexic_regular.otf              [NEW]
│       ├── opendyslexic_bold.otf                 [NEW]
│       └── opendyslexic_italic.otf               [NEW]
└── ...

documentation/
├── JOURNEY_PLANNER_UX_REDESIGN.md                [Full UX spec]
├── VisualIdentity_v2.md                          [Design system]
├── IMPLEMENTATION_STATUS.md                      [This file]
└── ...

build_and_run.sh                                  [NEW - Build script]
```

---

## Key Design Documents

| Document | Description |
|----------|-------------|
| `JOURNEY_PLANNER_UX_REDESIGN.md` | Complete UX specification for Trainline-style journey planner |
| `VisualIdentity_v2.md` | Design system with colors, typography, components, accessibility guidelines |
| `IMPLEMENTATION_STATUS.md` | This status document |

---

## Known Issues / Tech Debt

1. **Unused parameters warnings** in MainActivity (exactAlarmSettingsLauncher, overlayPermissionLauncher)
2. **Sample journey data** in HomeScreen needs to be replaced with real ViewModel data
3. **Social icons** in drawer are placeholders (need custom icons)
4. **Tasks and Notes tabs** not implemented yet

---

## How to Continue

### To resume Phase 5 (Journey Planner Refactor):

1. Read `JOURNEY_PLANNER_UX_REDESIGN.md` Section 2 (Screen Flow) and Section 3.2-3.3
2. Create new components in `presentation/components/`:
   - `WhereCard.kt` (destination-first)
   - `ArriveByCard.kt`
   - `TimePickerBottomSheet.kt`
   - `DatePickerBottomSheet.kt`
3. Refactor `JourneyPlannerScreen.kt` to use new components
4. Update `JourneyPlannerViewModel.kt` with new state shape

### To test current build:

```bash
./build_and_run.sh
```

### To verify database migration:

Clear app data and reinstall to test default profile seeding.

---

*Document generated: December 2, 2025*

