# Visual Identity v2.0 — Project Overlord

## Design Philosophy

A **dark, sophisticated productivity suite** with deep red and gold accents. Inspired by premium apps with a focus on clarity and accessibility. Every design decision must support users with **dyslexia and dyscalculia**.

---

## 1. Color Palette

### 1.1 Core Colors

| Role | Color | Hex | Usage |
|------|-------|-----|-------|
| **Background** | Near Black | `#121212` | Main app background |
| **Surface** | Dark Grey | `#1E1E1E` | Cards, elevated surfaces |
| **Surface Variant** | Charcoal | `#2D2D2D` | Secondary cards, inputs |
| **Primary** | Deep Red/Maroon | `#8B1538` | Headers, accent areas, feature cards |
| **Primary Variant** | Crimson | `#A91D3A` | Hover states, active elements |
| **Secondary** | Gold/Amber | `#D4A853` | CTAs, buttons, highlights |
| **Secondary Variant** | Muted Gold | `#C49B47` | Secondary buttons |
| **On Background** | Off-White | `#F5F5F5` | Primary text on dark |
| **On Surface** | Light Grey | `#E0E0E0` | Body text on cards |
| **On Primary** | White | `#FFFFFF` | Text on red backgrounds |
| **Outline** | Medium Grey | `#404040` | Borders, dividers |
| **Error** | Bright Red | `#CF6679` | Error states |
| **Success** | Teal | `#00A88E` | Success states |

### 1.2 Color Usage Rules

```
┌─────────────────────────────────────────────────────────────────────┐
│  APP BACKGROUND (#121212)                                           │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  HEADER AREA (#8B1538 - Deep Red)                             │  │
│  │  White text, gold accent icons                                │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  FEATURE CARD (#8B1538 with slight transparency)              │  │
│  │  Title in white, body in light grey                           │  │
│  │  [Gold Button]                                      [+ icon]  │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  NEUTRAL CARD (#1E1E1E - Surface)                             │  │
│  │  For settings, lists, secondary content                       │  │
│  │  [Chevron >]                                                  │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  [    PRIMARY CTA - GOLD (#D4A853)    ]                       │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │ 🏠    📋    ✓    📄                                          │    │
│  │ BOTTOM NAVIGATION BAR (#1E1E1E)                              │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.3 Kotlin Color Definitions

```kotlin
// Primary Palette
val DeepRed = Color(0xFF8B1538)
val Crimson = Color(0xFFA91D3A)
val LightRed = Color(0xFFB84D6B)

// Secondary Palette  
val Gold = Color(0xFFD4A853)
val MutedGold = Color(0xFFC49B47)
val DarkGold = Color(0xFFB8923F)

// Neutrals
val Background = Color(0xFF121212)
val Surface = Color(0xFF1E1E1E)
val SurfaceVariant = Color(0xFF2D2D2D)
val Outline = Color(0xFF404040)

// Text
val OnBackground = Color(0xFFF5F5F5)
val OnSurface = Color(0xFFE0E0E0)
val OnPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)

// Semantic
val Success = Color(0xFF00A88E)
val Error = Color(0xFFCF6679)
val Warning = Color(0xFFFFB74D)
```

---

## 2. Typography — Dyslexia-Optimized

### 2.1 Font Selection

**Primary Font: Lexend**
- Designed specifically for reading fluency
- Variable font with adjustable weight
- Excellent letter distinction (b/d, p/q differentiation)
- Available on Google Fonts

**Alternative: Atkinson Hyperlegible**
- Designed by Braille Institute
- Maximizes letter differentiation
- Free and open source

**Fallback: System Default**
- If custom fonts unavailable

### 2.2 Typography Rules for Dyslexia

| Rule | Implementation |
|------|----------------|
| **Minimum font size** | 16sp for body text, never smaller |
| **Line height** | 1.5x font size minimum (24sp for 16sp text) |
| **Letter spacing** | +0.5sp to +1sp for body text |
| **Word spacing** | Slightly increased |
| **Line length** | Max 60-70 characters per line |
| **Alignment** | LEFT-aligned only (never justified) |
| **Paragraph spacing** | 1.5x line height between paragraphs |
| **Font weight** | Medium (500) for body, SemiBold (600) for headers |
| **Contrast** | Minimum 4.5:1, prefer 7:1 |

### 2.3 Type Scale

```kotlin
val OverlordTypography = Typography(
    // Display - Hero text, very large
    displayLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headlines - Screen titles
    headlineLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // Titles - Card titles, section headers
    titleLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Body - Main content
    bodyLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,          // Larger than typical
        lineHeight = 28.sp,        // 1.55x line height
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,        // 1.5x line height
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Labels - Buttons, chips, tags
    labelLarge = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LexendFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

## 3. Dyscalculia Considerations

### 3.1 Number Display Rules

| Rule | Implementation |
|------|----------------|
| **Large numbers** | Use 24sp+ for times and quantities |
| **Number spacing** | Extra letter-spacing for digit sequences |
| **Time format** | Always show hours AND minutes (09:00, not 9:00) |
| **Duration format** | Use words: "1 hour 30 mins" not "1:30" or "90 mins" |
| **Grouping** | Visual separators for long numbers |
| **Color coding** | Use color + icons, never color alone |
| **AM/PM** | Always explicit, never assume 24h understanding |

### 3.2 Time Display Component

```kotlin
@Composable
fun AccessibleTimeDisplay(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        // Hour - Extra large
        Text(
            text = time.hour.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontFeatureSettings = "tnum"  // Tabular numbers
        )
        
        // Colon - Slightly smaller, vertically centered
        Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        // Minutes
        Text(
            text = time.minute.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontFeatureSettings = "tnum"
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // AM/PM indicator - Always visible
        Text(
            text = if (time.hour < 12) "AM" else "PM",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
```

### 3.3 Duration Display

```kotlin
@Composable
fun AccessibleDurationDisplay(
    durationMinutes: Int,
    modifier: Modifier = Modifier
) {
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    
    Row(modifier = modifier) {
        if (hours > 0) {
            Text(
                text = "$hours",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (hours == 1) " hour" else " hours",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        if (minutes > 0) {
            Text(
                text = "$minutes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (minutes == 1) " min" else " mins",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

---

## 4. Layout Spacing — Accessibility Focus

### 4.1 Spacing Scale

```kotlin
object Spacing {
    val xxs = 4.dp      // Minimal internal padding
    val xs = 8.dp       // Icon to text gap
    val sm = 12.dp      // Tight grouping
    val md = 16.dp      // Default padding
    val lg = 24.dp      // Section spacing
    val xl = 32.dp      // Major section breaks
    val xxl = 48.dp     // Screen-level spacing
}
```

### 4.2 Touch Target Rules

| Element | Minimum Size | Recommended |
|---------|--------------|-------------|
| Buttons | 48dp × 48dp | 56dp height |
| List items | 48dp height | 64dp height |
| Icons (tappable) | 44dp × 44dp | 48dp × 48dp |
| Checkboxes | 44dp × 44dp | 48dp × 48dp |
| Bottom nav items | 48dp × 48dp | Full width segments |

### 4.3 Card Specifications

```kotlin
@Composable
fun FeatureCard(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),  // Minimum touch target
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 4.dp else 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPrimary) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPrimary) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = if (isPrimary) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
    }
}
```

---

## 5. Component Library

### 5.1 Buttons

```kotlin
// Primary CTA - Gold
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),  // Larger touch target
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),  // Pill shape
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Background,
            disabledContainerColor = Gold.copy(alpha = 0.4f),
            disabledContentColor = Background.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// Secondary Button - Outlined
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, Gold),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Gold
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
```

### 5.2 Navigation Drawer (Sidebar)

```kotlin
@Composable
fun OverlordDrawer(
    selectedItem: DrawerItem,
    onItemClick: (DrawerItem) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Surface,
        modifier = Modifier.width(300.dp)
    ) {
        // Profile Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepRed)
                .padding(24.dp)
        ) {
            Column {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "B",  // Initial
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Plan Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Gold,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                    ) {
                        Text(
                            text = "Free",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Background
                        )
                        Text(
                            text = "Current plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Background.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(
                            onClick = { /* Upgrade */ },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Background.copy(alpha = 0.2f),
                                contentColor = Background
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Upgrade")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation Items
        DrawerItem.entries.forEach { item ->
            NavigationDrawerItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                label = { 
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                selected = selectedItem == item,
                onClick = { onItemClick(item) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = DeepRed.copy(alpha = 0.2f),
                    selectedTextColor = Gold,
                    selectedIconColor = Gold,
                    unselectedTextColor = OnSurface,
                    unselectedIconColor = OnSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Social icons
            IconButton(onClick = { /* Instagram */ }) {
                Icon(
                    painter = painterResource(R.drawable.ic_instagram),
                    contentDescription = "Instagram",
                    tint = OnSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = { /* Discord */ }) {
                Icon(
                    painter = painterResource(R.drawable.ic_discord),
                    contentDescription = "Discord",
                    tint = OnSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
        )
    }
}

enum class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Default.Home, "home"),
    JOURNEY_PLANNER("Journey Planner", Icons.Default.DirectionsTransit, "journey_planner"),
    TODO_LISTS("To-Do Lists", Icons.Default.Checklist, "todo_lists"),
    SETTINGS("Settings", Icons.Default.Settings, "settings"),
    SUPPORT("Support", Icons.Default.Help, "support")
}
```

### 5.3 Bottom Navigation Bar

```kotlin
@Composable
fun OverlordBottomNav(
    selectedItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        contentColor = OnSurface,
        tonalElevation = 0.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = if (selectedItem == item) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    unselectedIconColor = OnSurface.copy(alpha = 0.6f),
                    unselectedTextColor = OnSurface.copy(alpha = 0.6f),
                    indicatorColor = DeepRed.copy(alpha = 0.3f)
                )
            )
        }
    }
}

enum class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "home"),
    JOURNEYS("Journeys", Icons.Filled.DirectionsTransit, Icons.Outlined.DirectionsTransit, "journeys"),
    TASKS("Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "tasks"),
    NOTES("Notes", Icons.Filled.Description, Icons.Outlined.Description, "notes")
}
```

---

## 6. Screen Templates

### 6.1 Standard Screen Layout

```kotlin
@Composable
fun OverlordScreenScaffold(
    title: String,
    onMenuClick: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepRed,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
                )
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = Background,
        contentColor = OnBackground,
        content = content
    )
}
```

### 6.2 Feature Header Section

```kotlin
@Composable
fun FeatureHeader(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepRed,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = OnPrimary,
                fontStyle = FontStyle.Italic  // Matching screenshot style
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = OnPrimary.copy(alpha = 0.9f),
                lineHeight = 26.sp
            )
        }
    }
}
```

---

## 7. Shapes

```kotlin
val OverlordShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),    // Chips, small badges
    small = RoundedCornerShape(12.dp),        // Small cards, inputs
    medium = RoundedCornerShape(16.dp),       // Standard cards
    large = RoundedCornerShape(20.dp),        // Feature cards
    extraLarge = RoundedCornerShape(28.dp)    // Buttons, prominent elements
)
```

---

## 8. Complete Theme Definition

```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = DeepRed,
    onPrimary = OnPrimary,
    primaryContainer = Crimson,
    onPrimaryContainer = OnPrimary,
    
    secondary = Gold,
    onSecondary = Background,
    secondaryContainer = MutedGold,
    onSecondaryContainer = Background,
    
    tertiary = Success,
    onTertiary = OnPrimary,
    
    background = Background,
    onBackground = OnBackground,
    
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurface.copy(alpha = 0.8f),
    
    outline = Outline,
    outlineVariant = Outline.copy(alpha = 0.5f),
    
    error = Error,
    onError = OnPrimary,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error
)

@Composable
fun OverlordTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = OverlordTypography,
        shapes = OverlordShapes,
        content = content
    )
}
```

---

## 9. Accessibility Checklist

### 9.1 Must-Have Features

- [ ] All text meets WCAG AA contrast (4.5:1 minimum)
- [ ] Touch targets minimum 48dp × 48dp
- [ ] Content descriptions on all icons
- [ ] Screen reader support for all interactive elements
- [ ] No information conveyed by color alone
- [ ] Text scalable up to 200% without breaking layout
- [ ] Reduced motion option respected

### 9.2 Dyslexia-Specific

- [ ] Lexend or similar font loaded
- [ ] Line height ≥ 1.5x font size
- [ ] Letter spacing increased
- [ ] Left-aligned text only
- [ ] No justified text
- [ ] Maximum 70 characters per line
- [ ] Clear visual hierarchy

### 9.3 Dyscalculia-Specific

- [ ] Large number displays (24sp+)
- [ ] Time always shows full format (09:00 not 9:00)
- [ ] Duration in words (1 hour 30 mins)
- [ ] AM/PM always explicit
- [ ] Visual grouping for number sequences
- [ ] Icons accompany numerical information

---

## 10. Assets Required

### 10.1 Fonts

```
fonts/
├── lexend/
│   ├── Lexend-Regular.ttf
│   ├── Lexend-Medium.ttf
│   ├── Lexend-SemiBold.ttf
│   └── Lexend-Bold.ttf
```

### 10.2 Icons (Custom)

```
drawable/
├── ic_instagram.xml
├── ic_facebook.xml
├── ic_discord.xml
├── ic_journey.xml
├── ic_alarm_wake.xml
├── ic_alarm_shower.xml
├── ic_alarm_pack.xml
└── ic_alarm_leave.xml
```

---

*Document Version: 2.0*
*Created: December 2, 2025*
*Design System: Dark theme with deep red/gold, optimized for dyslexia and dyscalculia*

