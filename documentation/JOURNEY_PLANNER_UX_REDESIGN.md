# Journey Planner UX Redesign — Trainline-Inspired Implementation Plan

## Executive Summary

This document outlines the redesign of the Journey Planner screen to mirror the excellent UX patterns from the Trainline app, adapted for an alarm-based transit planning application. The goal is to provide a frictionless, intuitive experience for users setting up journey-triggered alarms.

**Visual Identity:** See `VisualIdentity_v2.md` for the complete design system (dark theme, deep red/gold, dyslexia-optimized typography).

**App Architecture:** Project Overlord is a **productivity suite** containing multiple tools. Journey Planner is one module alongside To-Do Lists, Notes, and future tools. All modules are accessible via a **sidebar navigation drawer**.

### User Mental Model

> "I need a number of reminders at different times to keep me on track to get to my destination on time."

### Core User Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│  STEP 1: WHERE                                                      │
│  ────────────────                                                   │
│  • Destination FIRST: "Where do I need to be?"                      │
│  • Origin SECOND: "Where am I leaving from?" (defaults to Home)     │
├─────────────────────────────────────────────────────────────────────┤
│  STEP 2: WHEN                                                       │
│  ────────────────                                                   │
│  • "What time do I need to ARRIVE?" (primary mode = Arrive By)      │
│  • Select date and arrival time                                     │
├─────────────────────────────────────────────────────────────────────┤
│  STEP 3: FIND ROUTES                                                │
│  ────────────────                                                   │
│  • Google Transit API returns journey options                       │
│  • User sees departure times, duration, transport modes             │
├─────────────────────────────────────────────────────────────────────|
│  STEP 4: SELECT JOURNEY                                             │
│  ────────────────                                                   │
│  • User picks their preferred route                                 │
│  • Departure time is now KNOWN                                      │
├─────────────────────────────────────────────────────────────────────┤
│  STEP 5: SELECT ALARM PROFILE                                       │
│  ────────────────                                                   │
│  • Profile = TEMPLATE containing MULTIPLE alarms                    │
│  • Each alarm has a purpose and time offset from departure          │
│  • Examples: Wake up (-90min), Shower (-60min), Pack (-30min), etc. │
├─────────────────────────────────────────────────────────────────────┤
│  STEP 6: REVIEW & SCHEDULE                                          │
│  ────────────────                                                   │
│  • See all alarms with absolute times                               │
│  • Optionally adjust individual alarm times                         │
│  • Confirm and schedule                                             │
└─────────────────────────────────────────────────────────────────────┘
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Destination-first input** | User thinks "where do I need to be?" not "where am I leaving from?" |
| **Arrive-by is primary time mode** | The question is "when do I need to arrive?" |
| **Alarm Profile selection AFTER route selection** | Profile calculates alarm times based on departure time |
| **Alarm Profiles are TEMPLATES** | Each profile contains multiple alarms with relative offsets |
| **Origin defaults to Home** | Most journeys start from home |

### Adaptations from Trainline

- Visual input order: Destination (●) above Origin (○) — reversed from Trainline
- Time mode defaults to "Arrive by" not "Depart at"
- "Passengers" section removed entirely from initial screen
- Alarm Profile selection appears on Route Selection/Confirmation screen
- No ticket types (Single/Return) — always single journey

---

## 1. Alarm Profile Concept (Critical)

### 1.1 What is an Alarm Profile?

An **Alarm Profile** is NOT a sound choice. It is a **template containing multiple alarms**, each with:

- A **purpose/label** (e.g., "Wake up", "Start shower", "Pack bag")
- A **relative time offset** from departure (e.g., -90 minutes, -60 minutes)
- A **sound/notification style** (optional customization)
- An **auto-dismiss setting**

### 1.2 Example Profiles

**Profile: "Standard Morning"**
```
┌────────────────────────────────────────────────────┐
│ Alarm 1: Wake Up              │ -90 min            │
│ Alarm 2: Get Out of Bed       │ -75 min            │
│ Alarm 3: Leave Home           │ -10 min            │
└────────────────────────────────────────────────────┘
```

**Profile: "Full Morning Routine"**
```
┌────────────────────────────────────────────────────┐
│ Alarm 1: Wake Up              │ -120 min           │
│ Alarm 2: Get Out of Bed       │ -105 min           │
│ Alarm 3: Start Shower         │ -90 min            │
│ Alarm 4: Getting Ready        │ -60 min            │
│ Alarm 5: Pack Reminder        │ -30 min            │
│ Alarm 6: 10-Min Warning       │ -10 min            │
│ Alarm 7: Leave Now!           │ 0 min              │
└────────────────────────────────────────────────────┘
```

**Profile: "Quick Trip" (no overnight stay)**
```
┌────────────────────────────────────────────────────┐
│ Alarm 1: 30-Min Warning       │ -30 min            │
│ Alarm 2: Leave Now            │ 0 min              │
└────────────────────────────────────────────────────┘
```

### 1.3 Alarm Profile Data Model

```kotlin
data class AlarmProfileTemplate(
    val id: Long,
    val name: String,                           // "Full Morning Routine"
    val description: String?,                   // "For early starts with lots of prep"
    val isSystemDefault: Boolean = false,       // Pre-loaded profiles
    val createdAt: Instant,
    val alarms: List<AlarmTemplate>
)

data class AlarmTemplate(
    val id: Long,
    val profileId: Long,
    val label: String,                          // "Wake Up", "Pack Reminder"
    val offsetMinutes: Int,                     // -90 = 90 minutes before departure
    val sortOrder: Int,                         // Display order
    val soundSource: AlarmSource,               // LOCAL, SPOTIFY_TRACK, SPOTIFY_PLAYLIST
    val soundUri: String?,                      // Sound file path or Spotify URI
    val soundName: String?,                     // Display name: "Morning Vibes" or "alarm_loud.mp3"
    val requiresUserDismiss: Boolean,           // Must user actively dismiss?
    val autoStopAfterMinutes: Int?,             // Auto-stop after N minutes (null = never)
    val vibrationEnabled: Boolean = true
)

// When a profile is applied to a specific journey:
data class ScheduledAlarm(
    val id: Long,
    val journeyId: Long,
    val templateAlarmId: Long?,                 // Null if custom alarm added for this journey
    val label: String,                          // EDITABLE - can be customized per journey
    val scheduledTime: Instant,                 // Actual calculated time
    val isEnabled: Boolean = true,              // Can toggle on/off per journey
    val soundSource: AlarmSource,               // CUSTOMIZABLE per alarm
    val soundUri: String?,
    val soundName: String?,
    val requiresUserDismiss: Boolean,
    val autoStopAfterMinutes: Int?,
    val vibrationEnabled: Boolean
)

// Journey record with all scheduled alarms
data class ScheduledJourney(
    val id: Long,
    val origin: Place,
    val destination: Place,
    val departureTime: Instant,
    val arrivalTime: Instant,
    val routeSummary: String,                   // "Train via Kings Cross"
    val transportModes: List<String>,           // ["train", "walk"]
    val profileUsed: String,                    // Profile name at time of scheduling
    val alarms: List<ScheduledAlarm>,
    val createdAt: Instant,
    val status: JourneyStatus                   // UPCOMING, IN_PROGRESS, COMPLETED, CANCELLED
)

enum class JourneyStatus {
    UPCOMING,      // Future journey, alarms scheduled
    IN_PROGRESS,   // Currently happening (some alarms fired)
    COMPLETED,     // All alarms done, journey complete
    CANCELLED      // User cancelled
}

enum class AlarmSource {
    LOCAL,              // Built-in sound file
    SPOTIFY_TRACK,      // Single Spotify song
    SPOTIFY_PLAYLIST,   // Spotify playlist (shuffle)
    SYSTEM_DEFAULT      // Android default alarm sound
}
```

### 1.4 Profile Management Features

**Profile Selection Options:**
1. **Select existing profile** — Use as-is
2. **Duplicate & Modify** — Copy profile, make changes, optionally save as new
3. **Create new profile** — Start from scratch

**Per-Journey Customization (after profile selection):**
- **Toggle alarms on/off** — Disable specific alarms for this journey only
- **Adjust alarm times** — Move individual alarms earlier/later
- **Edit labels** — Customize text (e.g., "Pack laptop" instead of "Pack Reminder")
- **Change sounds** — Override sound for specific alarms

```
┌─────────────────────────────────────────────────────────────────────┐
│ PROFILE SELECTION FLOW                                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────┐                                                │
│  │ Select Profile  │                                                │
│  │ Bottom Sheet    │                                                │
│  └────────┬────────┘                                                │
│           │                                                         │
│     ┌─────┴─────┬────────────────┐                                  │
│     ▼           ▼                ▼                                  │
│  [Use As-Is] [Duplicate]    [Create New]                            │
│     │           │                │                                  │
│     │           ▼                ▼                                  │
│     │     ┌───────────┐    ┌───────────┐                            │
│     │     │ Edit Copy │    │ New Profile│                           │
│     │     │ Screen    │    │ Screen    │                            │
│     │     └─────┬─────┘    └─────┬─────┘                            │
│     │           │                │                                  │
│     │     ┌─────┴─────┐          │                                  │
│     │     ▼           ▼          │                                  │
│     │  [Use Once] [Save New]     │                                  │
│     │     │           │          │                                  │
│     └─────┴───────────┴──────────┘                                  │
│                   │                                                 │
│                   ▼                                                 │
│           ┌─────────────┐                                           │
│           │ Alarm Setup │                                           │
│           │ (Customize) │                                           │
│           └─────────────┘                                           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. App Architecture — Productivity Suite

### 2.1 Sidebar Navigation

Project Overlord is a **multi-tool productivity suite**. All modules are accessible via a persistent sidebar (navigation drawer).

```
┌─────────────────────────────────────────────────────────────────────┐
│ SIDEBAR (Navigation Drawer)                                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────┐                                                  │  │
│  │  │   B     │  User Avatar                                     │  │
│  │  │         │  (Initials or photo)                             │  │
│  │  └─────────┘                                                  │  │
│  │                                                               │  │
│  │  ┌─────────────────────────────────────────────────────────┐  │  │
│  │  │  Free                                                   │  │  │
│  │  │  Current plan                                           │  │  │
│  │  │  [  Upgrade  ]                                          │  │  │
│  │  └─────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  👤  Account                                             ▼   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  🔔  Activity                                            ▼   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  ⚙️  Settings                                            ▼   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  ℹ️  Resources                                           ▼   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  🔧  Support                                             ▼   │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ─────────────────────────────────────────────────────────────────  │
│                                                                     │
│  [Instagram] [Facebook] [Discord]                                   │
│                                                                     │
│  v1.0.0                                                             │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Bottom Navigation (Tools)

The bottom navigation provides quick access to the main productivity tools:

```
┌─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│    🏠           🚂           ✓           📋                         │
│   Home      Journeys      Tasks        Notes                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

| Tab | Tool | Description |
|-----|------|-------------|
| **Home** | Dashboard | Overview of upcoming alarms, tasks, quick actions |
| **Journeys** | Journey Planner | Plan journeys and set multi-alarm reminders |
| **Tasks** | To-Do Lists | Task management (future feature) |
| **Notes** | Quick Notes | Note-taking (future feature) |

### 2.3 Home Screen — Dashboard

The home screen shows **Journey Cards** for all scheduled journeys with their alarm sets.

```
┌─────────────────────────────────────────────────────────────────────┐
│ [☰]                      Overlord                        [Profile]  │
│─────────────────────────────────────────────────────────────────────│
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ UPCOMING JOURNEYS                                             │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  📍 London Kings Cross                                        │  │
│  │  Tomorrow • Arrive 09:00                                      │  │
│  │  ─────────────────────────────────────────────────────────────│  │
│  │  🔔 6 alarms scheduled                                        │  │
│  │  First alarm: 05:15 (Wake Up)                                 │  │
│  │  ─────────────────────────────────────────────────────────────│  │
│  │  [View Details]                           [Edit] [Cancel]     │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  📍 Manchester Piccadilly                                     │  │
│  │  Friday 6 Dec • Arrive 14:00                                  │  │
│  │  ─────────────────────────────────────────────────────────────│  │
│  │  🔔 3 alarms scheduled                                        │  │
│  │  First alarm: 11:30 (Get Ready)                               │  │
│  │  ─────────────────────────────────────────────────────────────│  │
│  │  [View Details]                           [Edit] [Cancel]     │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                                                               │  │
│  │  +  Plan a new journey                                        │  │
│  │                                                               │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│─────────────────────────────────────────────────────────────────────│
│    🏠           🚂           ✓           📋                         │
│   Home      Journeys      Tasks        Notes                        │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.4 Journey Card Component

Each scheduled journey appears as a card on the home screen.

```kotlin
@Composable
fun JourneyCard(
    journey: ScheduledJourney,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary  // Deep red
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Destination
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary  // Gold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = journey.destination.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date and arrival time
            Text(
                text = formatJourneyDateTime(journey),  // "Tomorrow • Arrive 09:00"
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
            
            // Alarm summary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${journey.alarms.count { it.isEnabled }} alarms scheduled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    val firstAlarm = journey.alarms
                        .filter { it.isEnabled }
                        .minByOrNull { it.scheduledTime }
                    firstAlarm?.let {
                        Text(
                            text = "First alarm: ${formatTime(it.scheduledTime)} (${it.label})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onViewDetails,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("View Details")
                }
                
                Row {
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
```

---

## 3. Screen Flow Architecture

### 2.1 Complete User Journey

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│   ┌─────────────────┐      ┌─────────────────┐      ┌─────────────┐  │
│   │ Journey Planner │ ───► │ Route Selection │ ───► │ Alarm Setup │  │
│   │     Screen      │      │     Screen      │      │   Screen    │  │
│   └─────────────────┘      └─────────────────┘      └─────────────┘  │
│                                                                      │
│   • Destination                • List of routes        • Profile     │
│   • Origin (Home)              • Times & duration        selection   │
│   • Arrival time               • Transport modes       • Review      │
│   • [Find Routes]              • [Select] per route      alarms      │
│                                                        • [Schedule]  │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

### 2.2 Screen 1: Journey Planner (Trainline-Style)

```
┌─────────────────────────────────────────┐
│         Where are you going?            │
│   Tell us your destination & arrival    │
├─────────────────────────────────────────┤
│                                         │
│  Where                                  │
│  ● Enter destination               [...]│  ← Destination FIRST
│  ┊                                      │
│  ○ Home                             ↕   │  ← Origin (default: Home)
│                                         │
├─────────────────────────────────────────┤
│                                         │
│  Arrive by                              │
│  → [Today ▼]        [Select time ▼]     │
│                                         │
├─────────────────────────────────────────┤
│                                         │
│  [        Find Routes        ]          │
│                                         │
└─────────────────────────────────────────┘
```

**Key Changes from Original Design:**
- Destination at TOP (filled circle ●)
- Origin at BOTTOM (hollow circle ○)
- Section title is "Arrive by" not "When"
- Time picker defaults to "Arrive" mode, not "Now"
- NO alarm profile selection on this screen

### 2.3 Screen 2: Route Selection

```
┌─────────────────────────────────────────┐
│ [←]  Routes to London                   │
│      Arrive by 09:00, Tue 3 Dec         │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────┐    │
│  │ Depart 07:15  →  Arrive 08:45   │    │
│  │ 1h 30min • Train, Walk          │    │
│  │ Sheffield → London St Pancras   │    │
│  │ [Select this route]             │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ┌─────────────────────────────────┐    │
│  │ Depart 07:45  →  Arrive 08:55   │    │
│  │ 1h 10min • Train                │    │
│  │ Sheffield → London Kings Cross  │    │
│  │ [Select this route]             │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ┌─────────────────────────────────┐    │
│  │ Depart 06:30  →  Arrive 08:30   │    │
│  │ 2h 00min • Bus, Train           │    │
│  │ Sheffield → London Euston       │    │
│  │ [Select this route]             │    │
│  └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘
```

### 3.4 Screen 3: Alarm Setup (After Route Selection)

**Full Customization Screen — Users can:**
- Toggle individual alarms on/off
- Adjust alarm times (tap time to edit)
- Edit labels (tap label to edit)
- Change sounds per alarm (tap sound icon)
- Duplicate profile or create new

```
┌─────────────────────────────────────────────────────────────────────┐
│ [←]  Set Your Alarms                                                │
│      Departing 07:15, Tue 3 Dec                                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Journey Summary                                                    │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  Sheffield → London St Pancras                                │  │
│  │  Depart 07:15 • Arrive 08:45 • 1 hour 30 mins                 │  │
│  │  🚂 Train                                                     │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Alarm Profile                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  Full Morning Routine                                    >    │  │
│  │  6 alarms • First at 05:15                                    │  │
│  │  [Duplicate & Edit]              [Change Profile]             │  │
│  └───────────────────────────────────────────────────────────────┘  │
│  + Create new profile                                          >    │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Your Alarms (tap to customize)                                     │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ [✓]  05:15 AM                                    🔊  🎵      │  │
│  │      Wake Up                                                  │  │
│  │      ──────────────────────────────────────────────────────── │  │
│  │ [✓]  05:30 AM                                    🔊  🎵      │  │
│  │      Get Out of Bed                                           │  │
│  │      ──────────────────────────────────────────────────────── │  │
│  │ [✓]  05:45 AM                                    🔊  🎵      │  │
│  │      Start Shower                                             │  │
│  │      ──────────────────────────────────────────────────────── │  │
│  │ [✓]  06:15 AM                                    🔊  🎵      │  │
│  │      Getting Ready                                            │  │
│  │      ──────────────────────────────────────────────────────── │  │
│  │ [✓]  06:45 AM                                    🔊  🎵      │  │
│  │      Pack laptop ✏️                              (edited)     │  │
│  │      ──────────────────────────────────────────────────────── │  │
│  │ [✓]  07:05 AM                                    🔊  🎵      │  │
│  │      10-Min Warning                                           │  │
│  │      ──────────────────────────────────────────────────────── │  │
│  │ [✓]  07:15 AM                                    🔊  🎵      │  │
│  │      Leave Now!                                               │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  + Add custom alarm for this journey                                │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  [          Schedule All Alarms          ]                          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.5 Alarm Row Interactions

Each alarm row supports multiple tap targets:

```
┌─────────────────────────────────────────────────────────────────────┐
│ ALARM ROW COMPONENT                                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────┐  ┌──────────┐  ┌─────────────────────────┐  ┌────┐ ┌────┐  │
│  │ ✓  │  │ 05:15 AM │  │ Wake Up                 │  │ 🔊 │ │ 🎵 │  │
│  └────┘  └──────────┘  └─────────────────────────┘  └────┘ └────┘  │
│    │          │                    │                   │      │     │
│    │          │                    │                   │      │     │
│    ▼          ▼                    ▼                   ▼      ▼     │
│ Toggle    Edit Time           Edit Label           Sound  Spotify  │
│ On/Off    (Time Picker)       (Text Input)         Picker  Search  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**Interaction Details:**

| Tap Target | Action | UI Element |
|------------|--------|------------|
| **Checkbox** | Toggle alarm on/off | Immediate toggle, greyed if off |
| **Time** | Adjust time | Time picker bottom sheet |
| **Label** | Edit label text | Inline text field or dialog |
| **Sound icon** | Change local sound | Sound picker bottom sheet |
| **Spotify icon** | Select Spotify track | Spotify search bottom sheet |

### 3.6 Edit Alarm Time Bottom Sheet

```
┌─────────────────────────────────────────┐
│              ━━━━━                       │
├─────────────────────────────────────────┤
│ Adjust alarm time                       │
│                                         │
│ Wake Up                                 │
│ Original: 2 hours before departure      │
│                                         │
├─────────────────────────────────────────┤
│                                         │
│         04         00                   │
│       ──────     ──────                 │
│    ►   05    :    15        AM          │
│       ──────     ──────                 │
│         06         30                   │
│                                         │
├─────────────────────────────────────────┤
│                                         │
│ Quick adjust:                           │
│ [-30 min]  [-15 min]  [+15 min]  [+30]  │
│                                         │
├─────────────────────────────────────────┤
│ [            Done            ]          │
└─────────────────────────────────────────┘
```

### 3.7 Sound Picker Bottom Sheet

```
┌─────────────────────────────────────────┐
│              ━━━━━                       │
├─────────────────────────────────────────┤
│ Select Sound                            │
│                                         │
│ For: Wake Up alarm                      │
│                                         │
├─────────────────────────────────────────┤
│                                         │
│ Built-in Sounds                         │
│ ┌─────────────────────────────────────┐ │
│ │ ▶️  Gentle Chime            [✓]     │ │
│ │ ▶️  Classic Alarm                   │ │
│ │ ▶️  Radar                           │ │
│ │ ▶️  Beacon                          │ │
│ │ ▶️  Waves                           │ │
│ │ ▶️  Digital                         │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ─────────────────────────────────────── │
│                                         │
│ 🎵 Spotify                         >    │
│    Search for a track or playlist       │
│                                         │
│ ─────────────────────────────────────── │
│                                         │
│ 📁 From device                     >    │
│    Choose audio file                    │
│                                         │
├─────────────────────────────────────────┤
│ [            Done            ]          │
└─────────────────────────────────────────┘
```

### 3.8 Spotify Search Bottom Sheet

```
┌─────────────────────────────────────────┐
│              ━━━━━                       │
├─────────────────────────────────────────┤
│ Search Spotify                          │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 🔍 Search songs, playlists...      │ │
│ └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│                                         │
│ Recently Used                           │
│ ┌─────────────────────────────────────┐ │
│ │ 🎵 Morning Vibes Playlist     [✓]  │ │
│ │    Spotify • Playlist               │ │
│ │ 🎵 Eye of the Tiger               │ │
│ │    Survivor • Track                 │ │
│ │ 🎵 Wake Me Up                      │ │
│ │    Avicii • Track                   │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Search Results                          │
│ (shows after typing)                    │
│                                         │
├─────────────────────────────────────────┤
│ [            Done            ]          │
└─────────────────────────────────────────┘
```

### 3.9 Profile Selection Bottom Sheet (with Duplicate)

```
┌─────────────────────────────────────────┐
│              ━━━━━                       │
├─────────────────────────────────────────┤
│ Select Alarm Profile                    │
├─────────────────────────────────────────┤
│                                         │
│ ✓ Full Morning Routine                  │
│   6 alarms • First: 05:15               │
│   [Use]          [Duplicate & Edit]     │
│ ─────────────────────────────────────── │
│   Standard Morning                      │
│   3 alarms • First: 05:45               │
│   [Use]          [Duplicate & Edit]     │
│ ─────────────────────────────────────── │
│   Quick Trip                            │
│   2 alarms • First: 06:45               │
│   [Use]          [Duplicate & Edit]     │
│                                         │
├─────────────────────────────────────────┤
│ + Create new profile from scratch       │
└─────────────────────────────────────────┘
```

**Duplicate & Edit Flow:**
1. User taps "Duplicate & Edit" on a profile
2. Opens profile editor with copy of selected profile
3. User modifies alarms (add/remove/reorder/rename)
4. User can either:
   - "Use for this journey only" (doesn't save profile)
   - "Save as new profile" (saves for future use)

---

## 3. UX Analysis: Trainline Reference

### 3.1 Main Booking Screen (Screenshot 2)

**Original Trainline Structure:**
```
┌─────────────────────────────────────────┐
│ [X]    Tickets    Flexi & Seasons       │
├─────────────────────────────────────────┤
│ Where                                   │
│ ○ Sheffield                      [...] │  ← Origin FIRST
│ ┊                                       │
│ ● London (Any)                    ↕    │  ← Destination SECOND
├─────────────────────────────────────────┤
│ When                                    │
│ [Single] [Return] [Open Return]         │
│ → [Today ▼]        [Now ▼]              │
├─────────────────────────────────────────┤
│ Passengers                         >    │
│ Daniel                                  │
├─────────────────────────────────────────┤
│ [    Find times and prices    ]         │
└─────────────────────────────────────────┘
```

**Our Adapted Structure:**
```
┌─────────────────────────────────────────┐
│         Where are you going?            │
│   Tell us your destination & arrival    │
├─────────────────────────────────────────┤
│ Where                                   │
│ ● Enter destination              [...]  │  ← Destination FIRST (reversed!)
│ ┊                                       │
│ ○ Home                             ↕    │  ← Origin SECOND
├─────────────────────────────────────────┤
│ Arrive by                               │  ← Changed from "When"
│ → [Today ▼]    [Select time ▼]          │  ← "Arrive" mode default
├─────────────────────────────────────────┤
│ [        Find Routes        ]           │  ← NO profile selection here
└─────────────────────────────────────────┘
```

### 3.2 Time Picker Modal (Screenshot 1)

**Original Trainline:**
```
┌─────────────────────────────────────────┐
│ [Now]   [Depart]   [Arrive]             │  ← Three options
├─────────────────────────────────────────┤
│           14         20                 │
│         ──────     ──────               │
│    ►     15    :    25                  │
│         ──────     ──────               │
│           16         30                 │
├─────────────────────────────────────────┤
│ [          Done          ]              │
└─────────────────────────────────────────┘
```

**Our Adapted Version:**
```
┌─────────────────────────────────────────┐
│         [Arrive]   [Depart]             │  ← TWO options only
│                                         │     "Arrive" selected by default
├─────────────────────────────────────────┤
│           08         45                 │
│         ──────     ──────               │
│    ►     09    :    00                  │  ← Typical arrival time
│         ──────     ──────               │
│           10         15                 │
├─────────────────────────────────────────┤
│ [          Done          ]              │
└─────────────────────────────────────────┘
```

**Changes:**
- Remove "Now" option (doesn't make sense for "arrive by")
- Default to "Arrive" selected
- "Depart" available for users who think departure-first

### 3.3 Location Search (Screenshot 4)

**Original Trainline:**
```
┌─────────────────────────────────────────┐
│ [X]                                     │
│ ┌─ From ─────────────────────────────┐  │
│ │ Sheffield                      [X] │  │
│ └────────────────────────────────────┘  │
│ ┌─ To ───────────────────────────────┐  │
│ │ london|                        [X] │  │
│ └────────────────────────────────────┘  │
├─────────────────────────────────────────┤
│ [🏠 Home]  [💼 Work]                    │
├─────────────────────────────────────────┤
│ Stations / Places...                    │
└─────────────────────────────────────────┘
```

**Our Adapted Version:**
```
┌─────────────────────────────────────────┐
│ [X]                                     │
│ ┌─ Destination ──────────────────────┐  │  ← DESTINATION first
│ │ london|                        [X] │  │     (focused by default)
│ └────────────────────────────────────┘  │
│ ┌─ From ─────────────────────────────┐  │
│ │ Home (23 High Street...)       [X] │  │  ← FROM second
│ └────────────────────────────────────┘  │
├─────────────────────────────────────────┤
│ [🏠 Home]  [💼 Work]  [📍 Current]      │  ← Quick selections
├─────────────────────────────────────────┤
│ Recent                                  │
│ 📍 London Kings Cross                   │
│ Suggestions                             │
│ 📍 ...                                  │
└─────────────────────────────────────────┘
```

**Changes:**
- "Destination" field at top (focused on open)
- "From" field below
- Added "Current Location" quick button
- Using Google Maps autocomplete (not just train stations)

---

## 4. Component Specifications

### 4.1 WhereCard (Reversed Order)

```kotlin
@Composable
fun WhereCard(
    destination: Place?,           // Shows FIRST (top)
    origin: Place?,                // Shows SECOND (bottom)
    onDestinationClick: () -> Unit,
    onOriginClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Visual Layout:**
```
┌─────────────────────────────────────────┐
│ Where                                   │  ← Section title
│                                         │
│ ● London Kings Cross             [...] │  ← DESTINATION (filled circle)
│   London, UK                            │     Primary + secondary text
│ ┊                                       │  ← Dotted connector
│ ┊                                       │
│ ○ Home                             ↕    │  ← ORIGIN (hollow circle)
│   23 High Street, Sheffield             │     Swap button
│                                         │
└─────────────────────────────────────────┘
```

### 4.2 ArriveByCard (Renamed from WhenCard)

```kotlin
@Composable
fun ArriveByCard(
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    timeMode: TimeMode,            // ARRIVE_BY (default) or DEPART_AT
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Visual Layout:**
```
┌─────────────────────────────────────────┐
│ Arrive by                               │  ← Section title changes with mode
│                                         │
│ →  [Today ▼]        [09:00 ▼]           │  ← Date and time dropdowns
│                                         │
└─────────────────────────────────────────┘
```

### 4.3 Time Picker Bottom Sheet (Arrive-First)

```kotlin
@Composable
fun TimePickerBottomSheet(
    visible: Boolean,
    initialMode: TimeMode = TimeMode.ARRIVE_BY,  // Default to arrive
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (TimeMode, LocalTime) -> Unit
)

enum class TimeMode {
    ARRIVE_BY,    // "I need to arrive by this time"
    DEPART_AT     // "I want to leave at this time"
}
```

**Visual Layout:**
```
┌─────────────────────────────────────────┐
│              ━━━━━                       │
├─────────────────────────────────────────┤
│      [Arrive by]    [Depart at]         │  ← Two options only
├─────────────────────────────────────────┤
│           08         45                 │
│         ──────     ──────               │
│    ►     09    :    00                  │
│         ──────     ──────               │
│           10         15                 │
├─────────────────────────────────────────┤
│ [            Done            ]          │
└─────────────────────────────────────────┘
```

### 4.4 Profile Selection Bottom Sheet (Shows on Alarm Setup Screen)

```kotlin
@Composable
fun ProfileSelectionBottomSheet(
    visible: Boolean,
    profiles: List<AlarmProfileTemplate>,
    selectedProfileId: Long?,
    departureTime: Instant,            // Needed to calculate preview times
    onProfileSelected: (AlarmProfileTemplate) -> Unit,
    onCreateProfile: () -> Unit,
    onDismiss: () -> Unit
)
```

**Visual Layout:**
```
┌─────────────────────────────────────────┐
│              ━━━━━                       │
├─────────────────────────────────────────┤
│ Select Alarm Profile                    │
├─────────────────────────────────────────┤
│                                         │
│ ✓ Full Morning Routine                  │  ← Selected
│   6 alarms • First: 05:15 (Wake Up)     │     Shows preview
│ ─────────────────────────────────────── │
│   Standard Morning                      │
│   3 alarms • First: 05:45 (Wake Up)     │
│ ─────────────────────────────────────── │
│   Quick Trip                            │
│   2 alarms • First: 06:45 (Warning)     │
│                                         │
├─────────────────────────────────────────┤
│ + Create new profile                    │
└─────────────────────────────────────────┘
```

### 4.5 Alarm Review List

```kotlin
@Composable
fun AlarmReviewList(
    alarms: List<ScheduledAlarm>,
    onAlarmTimeAdjust: (alarmId: Long, newTime: Instant) -> Unit,
    onAlarmToggle: (alarmId: Long, enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier
)
```

**Visual Layout:**
```
┌─────────────────────────────────────────┐
│ Your Alarms                             │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🔔  05:15                           │ │
│ │     Wake Up                    [✓]  │ │  ← Toggle on/off
│ ├─────────────────────────────────────┤ │
│ │ 🔔  05:30                           │ │
│ │     Get Out of Bed             [✓]  │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔔  05:45                           │ │
│ │     Start Shower               [✓]  │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔔  06:15                           │ │
│ │     Getting Ready              [✓]  │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔔  06:45                           │ │
│ │     Pack Reminder              [✓]  │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔔  07:05                           │ │
│ │     10-Min Warning             [✓]  │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔔  07:15                           │ │
│ │     Leave Now!                 [✓]  │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

## 5. Technical Implementation Plan

### Phase 1: Data Layer (Week 1)

#### 5.1.1 Alarm Profile Template Model

**New Files:**
- `data/database/entity/AlarmProfileTemplateEntity.kt`
- `data/database/entity/AlarmTemplateEntity.kt`
- `data/database/dao/AlarmProfileTemplateDao.kt`
- `domain/model/AlarmProfileTemplate.kt`
- `domain/model/AlarmTemplate.kt`

```kotlin
// AlarmProfileTemplateEntity.kt
@Entity(tableName = "alarm_profile_templates")
data class AlarmProfileTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val isDefault: Boolean = false
)

// AlarmTemplateEntity.kt
@Entity(
    tableName = "alarm_templates",
    foreignKeys = [
        ForeignKey(
            entity = AlarmProfileTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AlarmTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val label: String,
    val offsetMinutes: Int,              // Negative = before departure
    val sortOrder: Int,
    val soundSource: String,             // "LOCAL", "SPOTIFY_TRACK", etc.
    val soundUri: String?,
    val requiresUserDismiss: Boolean,
    val autoStopAfterMinutes: Int?
)

// DAO
@Dao
interface AlarmProfileTemplateDao {
    @Query("SELECT * FROM alarm_profile_templates ORDER BY isDefault DESC, name ASC")
    fun getAllProfiles(): Flow<List<AlarmProfileTemplateEntity>>
    
    @Query("SELECT * FROM alarm_templates WHERE profileId = :profileId ORDER BY sortOrder")
    fun getAlarmsForProfile(profileId: Long): Flow<List<AlarmTemplateEntity>>
    
    @Transaction
    @Query("SELECT * FROM alarm_profile_templates WHERE id = :profileId")
    fun getProfileWithAlarms(profileId: Long): Flow<ProfileWithAlarms>
    
    @Insert
    suspend fun insertProfile(profile: AlarmProfileTemplateEntity): Long
    
    @Insert
    suspend fun insertAlarm(alarm: AlarmTemplateEntity): Long
    
    @Update
    suspend fun updateProfile(profile: AlarmProfileTemplateEntity)
    
    @Delete
    suspend fun deleteProfile(profile: AlarmProfileTemplateEntity)
}

data class ProfileWithAlarms(
    @Embedded val profile: AlarmProfileTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val alarms: List<AlarmTemplateEntity>
)
```

#### 5.1.2 Google Places API Integration

**New Files:**
- `data/repository/PlacesRepository.kt`
- `data/api/PlacesApiService.kt`
- `data/model/PlacePrediction.kt`

```kotlin
// PlacesRepository.kt
class PlacesRepository(
    private val apiKey: String,
    private val httpClient: OkHttpClient
) {
    private var sessionToken: String = UUID.randomUUID().toString()
    
    suspend fun searchPlaces(
        query: String,
        location: LatLng? = null
    ): Result<List<PlacePrediction>>
    
    suspend fun getPlaceDetails(placeId: String): Result<Place>
    
    fun newSession() {
        sessionToken = UUID.randomUUID().toString()
    }
}

// PlacePrediction.kt
data class PlacePrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val types: List<String>
)
```

#### 5.1.3 User Settings Storage

**New Files:**
- `data/datastore/UserPreferences.kt`
- `data/repository/UserSettingsRepository.kt`

```kotlin
data class UserSettings(
    val homeAddress: Place?,
    val workAddress: Place?,
    val defaultProfileId: Long?,
    val recentPlaces: List<Place>
)

class UserSettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<UserSettings>
    
    suspend fun setHomeAddress(place: Place)
    suspend fun setWorkAddress(place: Place)
    suspend fun setDefaultProfile(profileId: Long)
    suspend fun addRecentPlace(place: Place)
}
```

### Phase 2: UI Components (Week 2)

#### 5.2.1 Journey Planner Screen Components

**New Files:**
```
presentation/components/
├── WhereCard.kt              (Destination-first)
├── ArriveByCard.kt           (Renamed from WhenCard)
├── LocationRow.kt
├── DottedConnector.kt
├── TimePickerBottomSheet.kt  (Arrive/Depart only)
├── DatePickerBottomSheet.kt
└── LocationSearchScreen.kt
```

#### 5.2.2 Alarm Setup Screen Components

**New Files:**
```
presentation/components/
├── JourneySummaryCard.kt
├── ProfileSelectionCard.kt
├── ProfileSelectionBottomSheet.kt
├── AlarmReviewList.kt
└── AlarmRowItem.kt

presentation/screen/
├── RouteSelectionScreen.kt   [NEW]
└── AlarmSetupScreen.kt       [NEW]
```

### Phase 3: ViewModels (Week 3)

#### 5.3.1 JourneyPlannerViewModel (Refactored)

```kotlin
data class JourneyPlannerUiState(
    // Location
    val destination: Place? = null,    // Primary input
    val origin: Place? = null,         // Defaults to home
    
    // Time
    val timeMode: TimeMode = TimeMode.ARRIVE_BY,  // Default to arrive
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime? = null,
    
    // User settings
    val homeAddress: Place? = null,
    val workAddress: Place? = null,
    val recentPlaces: List<Place> = emptyList(),
    
    // API state
    val isLoading: Boolean = false,
    val routes: List<Route> = emptyList(),
    val error: String? = null,
    
    // UI state
    val showLocationSearch: Boolean = false,
    val locationSearchFocus: LocationField = LocationField.DESTINATION,
    val showTimePicker: Boolean = false,
    val showDatePicker: Boolean = false
)

enum class LocationField { DESTINATION, ORIGIN }

class JourneyPlannerViewModel(
    private val routingRepository: RoutingRepository,
    private val placesRepository: PlacesRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {
    
    fun setDestination(place: Place)
    fun setOrigin(place: Place)
    fun setTimeMode(mode: TimeMode)
    fun setArrivalTime(date: LocalDate, time: LocalTime)
    fun swapLocations()
    fun findRoutes()
    
    // UI actions
    fun showLocationSearch(focus: LocationField)
    fun dismissLocationSearch()
    fun showTimePicker()
    fun dismissTimePicker()
}
```

#### 5.3.2 AlarmSetupViewModel (New)

```kotlin
data class AlarmSetupUiState(
    // Selected journey
    val selectedRoute: Route? = null,
    val departureTime: Instant? = null,
    
    // Profile selection
    val availableProfiles: List<AlarmProfileTemplate> = emptyList(),
    val selectedProfile: AlarmProfileTemplate? = null,
    
    // Calculated alarms
    val scheduledAlarms: List<ScheduledAlarm> = emptyList(),
    
    // UI state
    val showProfileSelection: Boolean = false,
    val isScheduling: Boolean = false,
    val schedulingComplete: Boolean = false,
    val error: String? = null
)

class AlarmSetupViewModel(
    private val profileRepository: AlarmProfileRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    
    fun setRoute(route: Route)
    fun selectProfile(profile: AlarmProfileTemplate)
    fun calculateAlarms()
    fun toggleAlarm(alarmId: Long, enabled: Boolean)
    fun adjustAlarmTime(alarmId: Long, newTime: Instant)
    fun scheduleAllAlarms()
    
    // UI actions
    fun showProfileSelection()
    fun dismissProfileSelection()
    fun navigateToCreateProfile()
}
```

### Phase 4: Navigation & Integration (Week 4)

#### 5.4.1 Navigation Graph

```kotlin
sealed class Screen(val route: String) {
    object JourneyPlanner : Screen("journey_planner")
    object LocationSearch : Screen("location_search/{focus}") {
        fun createRoute(focus: LocationField) = "location_search/${focus.name}"
    }
    object RouteSelection : Screen("route_selection")
    object AlarmSetup : Screen("alarm_setup/{routeId}") {
        fun createRoute(routeId: String) = "alarm_setup/$routeId"
    }
    object CreateProfile : Screen("create_profile")
}
```

#### 5.4.2 Data Flow

```
JourneyPlannerScreen
        │
        │ findRoutes()
        ▼
RouteSelectionScreen
        │
        │ selectRoute(route)
        ▼
AlarmSetupScreen
        │
        │ selectProfile(profile)
        │ calculateAlarms()
        │ scheduleAllAlarms()
        ▼
   [Alarms Scheduled]
```

---

## 6. Default Data (Seed Profiles)

### 6.1 Pre-loaded Alarm Profiles

```kotlin
object DefaultProfiles {
    val standardMorning = AlarmProfileTemplate(
        id = 1,
        name = "Standard Morning",
        description = "Basic wake up and leave routine",
        alarms = listOf(
            AlarmTemplate(label = "Wake Up", offsetMinutes = -90),
            AlarmTemplate(label = "Get Out of Bed", offsetMinutes = -75),
            AlarmTemplate(label = "Leave Home", offsetMinutes = -10)
        )
    )
    
    val fullMorningRoutine = AlarmProfileTemplate(
        id = 2,
        name = "Full Morning Routine",
        description = "For mornings with shower and prep time",
        alarms = listOf(
            AlarmTemplate(label = "Wake Up", offsetMinutes = -120),
            AlarmTemplate(label = "Get Out of Bed", offsetMinutes = -105),
            AlarmTemplate(label = "Start Shower", offsetMinutes = -90),
            AlarmTemplate(label = "Getting Ready", offsetMinutes = -60),
            AlarmTemplate(label = "Pack Reminder", offsetMinutes = -30),
            AlarmTemplate(label = "10-Min Warning", offsetMinutes = -10),
            AlarmTemplate(label = "Leave Now!", offsetMinutes = 0)
        )
    )
    
    val quickTrip = AlarmProfileTemplate(
        id = 3,
        name = "Quick Trip",
        description = "When you're already ready, just need a reminder",
        alarms = listOf(
            AlarmTemplate(label = "30-Min Warning", offsetMinutes = -30),
            AlarmTemplate(label = "Leave Now", offsetMinutes = 0)
        )
    )
    
    val allProfiles = listOf(standardMorning, fullMorningRoutine, quickTrip)
}
```

---

## 7. File Structure Summary

```
app/src/main/java/com/productions666/overlord/
├── data/
│   ├── api/
│   │   └── PlacesApiService.kt                    [NEW]
│   ├── database/
│   │   ├── dao/
│   │   │   └── AlarmProfileTemplateDao.kt         [NEW]
│   │   ├── entity/
│   │   │   ├── AlarmProfileTemplateEntity.kt      [NEW]
│   │   │   └── AlarmTemplateEntity.kt             [NEW]
│   │   └── OverlordDatabase.kt                    [UPDATE]
│   ├── datastore/
│   │   └── UserPreferences.kt                     [NEW]
│   ├── model/
│   │   ├── Place.kt                               [EXISTING]
│   │   └── PlacePrediction.kt                     [NEW]
│   └── repository/
│       ├── AlarmProfileRepository.kt              [NEW]
│       ├── PlacesRepository.kt                    [NEW]
│       ├── RoutingRepository.kt                   [EXISTING]
│       └── UserSettingsRepository.kt              [NEW]
├── domain/
│   └── model/
│       ├── AlarmProfileTemplate.kt                [NEW]
│       ├── AlarmTemplate.kt                       [NEW]
│       └── ScheduledAlarm.kt                      [NEW]
├── presentation/
│   ├── components/
│   │   ├── WhereCard.kt                           [NEW]
│   │   ├── ArriveByCard.kt                        [NEW]
│   │   ├── LocationRow.kt                         [NEW]
│   │   ├── DottedConnector.kt                     [NEW]
│   │   ├── TimePickerBottomSheet.kt               [NEW]
│   │   ├── DatePickerBottomSheet.kt               [NEW]
│   │   ├── JourneySummaryCard.kt                  [NEW]
│   │   ├── ProfileSelectionCard.kt                [NEW]
│   │   ├── ProfileSelectionBottomSheet.kt         [NEW]
│   │   ├── AlarmReviewList.kt                     [NEW]
│   │   └── AlarmRowItem.kt                        [NEW]
│   ├── screen/
│   │   ├── JourneyPlannerScreen.kt                [REFACTOR]
│   │   ├── LocationSearchScreen.kt                [NEW]
│   │   ├── RouteSelectionScreen.kt                [NEW]
│   │   ├── AlarmSetupScreen.kt                    [NEW]
│   │   └── CreateProfileScreen.kt                 [NEW]
│   └── viewmodel/
│       ├── JourneyPlannerViewModel.kt             [REFACTOR]
│       ├── PlacesSearchViewModel.kt               [NEW]
│       └── AlarmSetupViewModel.kt                 [NEW]
└── navigation/
    └── AppNavigation.kt                           [NEW/UPDATE]
```

---

## 8. Acceptance Criteria

### 8.1 Must Have (MVP)

- [ ] Destination-first input on Journey Planner screen
- [ ] Origin defaults to home address (with fallback to "Set home")
- [ ] "Arrive by" as default time mode
- [ ] Google Maps autocomplete for location search
- [ ] Route selection screen showing multiple journey options
- [ ] Alarm profile selection on Alarm Setup screen (after route selection)
- [ ] Profiles contain multiple alarms with time offsets
- [ ] Alarm review showing calculated times
- [ ] Schedule all alarms button

### 8.2 Should Have

- [ ] 3 default profiles pre-loaded
- [ ] Create new profile option
- [ ] Recent places in location search
- [ ] Home/Work quick selection buttons
- [ ] Toggle individual alarms on/off
- [ ] Haptic feedback on time picker

### 8.3 Nice to Have

- [ ] Adjust individual alarm times before scheduling
- [ ] Profile editing
- [ ] Journey favorites/history
- [ ] Voice input for location

---

## 9. Open Questions (Resolved)

| Question | Resolution |
|----------|------------|
| When to prompt for home address? | On first journey setup, or in onboarding |
| Profile selection timing? | AFTER route selection (when departure time is known) |
| Alarm profile = single sound? | NO — profile is a TEMPLATE of multiple alarms |
| Input order? | Destination FIRST, then origin |
| Default time mode? | "Arrive by" (not "Now" or "Depart at") |

---

## 10. Timeline Estimate

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| Phase 1: Data Layer | 3-4 days | Profile templates, Places API, Settings storage |
| Phase 2: UI Components | 4-5 days | All cards, bottom sheets, search screen |
| Phase 3: ViewModels | 3-4 days | Journey planner, Alarm setup state management |
| Phase 4: Integration | 3-4 days | Navigation, end-to-end flow, polish |

**Total Estimate: 2-3 weeks**

---

*Document Version: 2.0*
*Created: December 2, 2025*
*Updated: December 2, 2025 — Major revision based on user mental model clarification*
*Author: AI Assistant based on Trainline UX analysis and user requirements*
