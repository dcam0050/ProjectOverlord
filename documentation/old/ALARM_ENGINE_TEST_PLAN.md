# Alarm Engine Test Plan

This document provides a comprehensive test plan for verifying the core alarm engine functionality of the Overlord app.

## Prerequisites

1. **Device Setup**:
   - Android device running API 24+ (Android 7.0+)
   - Device should be unlocked and accessible for testing
   - Ensure device is not in battery saver mode
   - Disable battery optimization for the app (Settings → Apps → Overlord → Battery → Unrestricted)

2. **App Setup**:
   - Build and install the debug APK
   - Grant all required permissions when prompted:
     - Location (Fine/Coarse)
     - Notifications
     - Exact Alarm (if Android 12+)
   - Ensure app is not force-stopped

3. **Database Setup**:
   - Default alarm profiles should be created automatically on first launch
   - Verify profiles exist: Check logs for "DatabaseInitializer" messages

## Test Scenarios

### Test 1: Basic Alarm Scheduling and Triggering

**Objective**: Verify that alarms can be scheduled and fire correctly.

**Steps**:
1. Open the app
2. Schedule a test alarm for 2 minutes from now:
   - You'll need to manually create an alarm instance (use ADB or add temporary UI)
   - Or modify `MainActivity` to schedule a test alarm on button press
3. Lock the device screen
4. Wait for the alarm to trigger
5. Verify:
   - Device wakes up and screen turns on
   - Full-screen alarm activity appears
   - Alarm sound plays
   - Device vibrates
   - Notification appears in status bar

**Expected Results**:
- ✅ Alarm triggers at scheduled time (±30 seconds tolerance)
- ✅ Screen wakes and shows full-screen alarm UI
- ✅ Sound and vibration start immediately
- ✅ Notification is visible

**Pass Criteria**: All expected results occur.

---

### Test 2: Alarm Dismissal

**Objective**: Verify that dismissing an alarm stops all alarm activities.

**Steps**:
1. Schedule an alarm for 1 minute from now
2. Wait for alarm to trigger
3. Tap "Dismiss" button
4. Verify:
   - Sound stops immediately
   - Vibration stops
   - Alarm activity closes
   - Notification disappears
   - Alarm is marked inactive in database

**Expected Results**:
- ✅ All alarm activities stop within 1 second
- ✅ UI returns to previous state
- ✅ Alarm is deactivated in database

**Pass Criteria**: All expected results occur.

---

### Test 3: Auto-Stop Functionality

**Objective**: Verify alarms with `requiresUserDismiss=false` auto-stop after timeout.

**Steps**:
1. Schedule an alarm with:
   - `requiresUserDismiss = false`
   - `autoStopAfterMillis = 30000` (30 seconds)
2. Wait for alarm to trigger
3. Do NOT interact with the alarm
4. Observe the countdown timer
5. Wait for auto-stop timeout
6. Verify:
   - Countdown decreases correctly
   - Alarm stops automatically after timeout
   - Sound and vibration stop
   - Activity closes

**Expected Results**:
- ✅ Countdown displays correctly
- ✅ Alarm auto-stops after exactly 30 seconds (±2 seconds tolerance)
- ✅ All alarm activities stop automatically

**Pass Criteria**: All expected results occur.

---

### Test 4: Multiple Alarms

**Objective**: Verify multiple alarms can be scheduled and trigger independently.

**Steps**:
1. Schedule three alarms:
   - Alarm 1: 1 minute from now
   - Alarm 2: 2 minutes from now
   - Alarm 3: 3 minutes from now
2. Wait for first alarm to trigger
3. Dismiss first alarm
4. Wait for second alarm to trigger
5. Dismiss second alarm
6. Wait for third alarm to trigger
7. Verify each alarm triggers independently

**Expected Results**:
- ✅ Each alarm triggers at its scheduled time
- ✅ Dismissing one alarm doesn't affect others
- ✅ All alarms work independently

**Pass Criteria**: All expected results occur.

---

### Test 5: Boot Recovery

**Objective**: Verify alarms are rescheduled after device reboot.

**Steps**:
1. Schedule an alarm for 5 minutes from now
2. Note the scheduled time
3. Reboot the device
4. Wait for device to fully boot
5. Wait for alarm to trigger
6. Verify alarm triggers at scheduled time

**Expected Results**:
- ✅ `BootCompletedReceiver` receives boot event
- ✅ Alarms are rescheduled from database
- ✅ Alarm triggers at correct time after reboot

**Pass Criteria**: All expected results occur.

**Note**: Check logs for "BootCompletedReceiver" and "Rescheduled alarm" messages.

---

### Test 6: Time Change Recovery

**Objective**: Verify alarms are rescheduled after system time changes.

**Steps**:
1. Schedule an alarm for 5 minutes from now
2. Note the scheduled time
3. Change device time forward by 1 hour (Settings → Date & Time)
4. Wait for alarm to trigger
5. Verify alarm still triggers correctly

**Expected Results**:
- ✅ `BootCompletedReceiver` receives TIME_SET event
- ✅ Alarms are recalculated based on new system time
- ✅ Alarm triggers at correct relative time

**Pass Criteria**: All expected results occur.

---

### Test 7: Foreground Service Behavior

**Objective**: Verify alarm service runs as foreground service correctly.

**Steps**:
1. Schedule an alarm for 1 minute from now
2. Wait for alarm to trigger
3. While alarm is active:
   - Swipe down notification drawer
   - Verify persistent notification is visible
   - Tap notification - should open app
4. Dismiss alarm
5. Verify notification disappears

**Expected Results**:
- ✅ Persistent notification appears when alarm is active
- ✅ Notification shows correct content
- ✅ Notification tap opens app
- ✅ Notification disappears when alarm stops

**Pass Criteria**: All expected results occur.

---

### Test 8: Screen Lock Behavior

**Objective**: Verify alarm works correctly when device is locked.

**Steps**:
1. Lock device screen
2. Schedule alarm for 1 minute from now
3. Wait for alarm to trigger
4. Verify:
   - Screen wakes up
   - Alarm UI appears over lock screen
   - Alarm can be dismissed without unlocking

**Expected Results**:
- ✅ Screen wakes automatically
- ✅ Alarm UI appears over lock screen
- ✅ Alarm can be dismissed without unlock

**Pass Criteria**: All expected results occur.

---

### Test 9: Alarm Cancellation

**Objective**: Verify scheduled alarms can be cancelled before triggering.

**Steps**:
1. Schedule an alarm for 5 minutes from now
2. Immediately cancel the alarm (via code or UI)
3. Wait for 5+ minutes
4. Verify alarm does NOT trigger

**Expected Results**:
- ✅ Alarm is removed from AlarmManager
- ✅ Alarm does not trigger
- ✅ Alarm is marked inactive in database

**Pass Criteria**: All expected results occur.

---

### Test 10: Alarm Persistence

**Objective**: Verify alarm data persists correctly in database.

**Steps**:
1. Schedule multiple alarms
2. Force-stop the app
3. Restart the app
4. Query database for active alarms
5. Verify all scheduled alarms are still present

**Expected Results**:
- ✅ Alarm data persists in Room database
- ✅ Alarms can be queried after app restart
- ✅ Alarm state (active/inactive) is preserved

**Pass Criteria**: All expected results occur.

---

## Manual Testing Helper Scripts

### Schedule Test Alarm via ADB

You can use this ADB command to insert a test alarm directly into the database:

```bash
# Connect to device
adb shell

# Open database shell (requires root or debug build)
adb shell run-as com.productions666.overlord

# Or use Room database inspector in Android Studio
```

### Quick Test Alarm Code

Add this temporary code to `MainActivity` for quick testing:

```kotlin
// In MainActivity.onCreate() - TEMPORARY TEST CODE
Button(onClick = {
    val alarm = AlarmInstance(
        id = 0,
        journeyId = null,
        scheduledTime = Instant.now().plusSeconds(60), // 1 minute from now
        type = AlarmType.WAKE_UP,
        profileId = 1,
        requiresUserDismiss = true,
        autoStopAfterMillis = null,
        label = "Test Alarm"
    )
    
    lifecycleScope.launch {
        val db = (application as OverlordApplication).database
        val entity = AlarmInstanceEntity(
            id = 0,
            journeyId = null,
            scheduledTime = alarm.scheduledTime.toEpochMilli(),
            type = alarm.type,
            profileId = alarm.profileId,
            requiresUserDismiss = alarm.requiresUserDismiss,
            autoStopAfterMillis = alarm.autoStopAfterMillis,
            label = alarm.label,
            isActive = true
        )
        val alarmId = db.alarmInstanceDao().insertAlarm(entity)
        
        val scheduler = AlarmScheduler(this@MainActivity)
        scheduler.scheduleAlarm(alarm.copy(id = alarmId))
    }
}) {
    Text("Schedule Test Alarm (1 min)")
}
```

## Log Monitoring

Monitor these log tags during testing:

- `AlarmBroadcastReceiver`: Alarm trigger events
- `AlarmService`: Service lifecycle and playback
- `AlarmScheduler`: Scheduling operations
- `BootCompletedReceiver`: Boot/time change events
- `DatabaseInitializer`: Database initialization

**View logs**:
```bash
adb logcat | grep -E "AlarmBroadcastReceiver|AlarmService|AlarmScheduler|BootCompletedReceiver|DatabaseInitializer"
```

## Known Limitations / TODOs

1. **Spotify Integration**: Not yet implemented - alarms will use local sounds only
2. **Alarm Profile Loading**: Service currently uses default system sounds - profile loading needs implementation
3. **Snooze Functionality**: Not yet implemented - snooze button currently dismisses alarm
4. **UI Screens**: Full UI for scheduling alarms not yet built - use test code above

## Test Results Template

For each test, record:

- **Test ID**: (e.g., Test 1)
- **Date**: 
- **Device**: (Model, Android version)
- **Result**: ✅ Pass / ❌ Fail
- **Notes**: (Any issues, deviations, or observations)
- **Logs**: (Relevant log snippets if failed)

---

## Quick Smoke Test (5 minutes)

If you need a quick verification:

1. ✅ App installs and launches
2. ✅ Permissions are requested
3. ✅ Database initializes (check logs)
4. ✅ Schedule test alarm for 1 minute
5. ✅ Alarm triggers, plays sound, shows UI
6. ✅ Dismiss works correctly

If all pass, core alarm engine is functional!

