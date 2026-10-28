package com.example.fingerprintanimation

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope
import kotlin.math.min

private val NeonRed = Color(0xFFFF1744)
private val Background = Color(0xFF050505)
private val Panel = Color(0xFF141414)
private val Muted = Color(0xFFBDBDBD)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("fingerprint_animation", Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = NeonRed,
                    background = Background,
                    surface = Panel,
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                var showSettings by remember { mutableStateOf(false) }
                var selectedMode by remember {
                    mutableStateOf(prefs.getString("mode", "Manual app launch") ?: "Manual app launch")
                }
                var soundOn by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
                var vibrationOn by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
                var autoStart by remember { mutableStateOf(prefs.getBoolean("autostart", false)) }
                var speed by remember { mutableFloatStateOf(prefs.getFloat("speed", 1f)) }
                var customPin by remember { mutableStateOf(prefs.getString("pin", "") ?: "") }
                var backgroundChoice by remember { mutableStateOf(prefs.getString("background", "Black") ?: "Black") }
                var scanning by remember { mutableStateOf(false) }
                var unlocked by remember { mutableStateOf(false) }
                var showPinDialog by remember { mutableStateOf(false) }
                var enteredPin by remember { mutableStateOf("") }
                var pinError by remember { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(
                        selectedMode = selectedMode,
                        onModeChange = {
                            selectedMode = it
                            prefs.edit().putString("mode", it).apply()
                        },
                        soundOn = soundOn,
                        onSoundChange = { soundOn = it; prefs.edit().putBoolean("sound", it).apply() },
                        vibrationOn = vibrationOn,
                        onVibrationChange = { vibrationOn = it; prefs.edit().putBoolean("vibration", it).apply() },
                        autoStart = autoStart,
                        onAutoStartChange = { autoStart = it; prefs.edit().putBoolean("autostart", it).apply() },
                        speed = speed,
                        onSpeedChange = { speed = it; prefs.edit().putFloat("speed", it).apply() },
                        backgroundChoice = backgroundChoice,
                        onBackgroundChange = { backgroundChoice = it; prefs.edit().putString("background", it).apply() },
                        customPin = customPin,
                        onPinSave = { customPin = it; prefs.edit().putString("pin", it).apply() },
                        onBack = { showSettings = false }
                    )
                } else {
                    val pageBackground = when (backgroundChoice) {
                        "Deep red" -> Color(0xFF190006)
                        "Charcoal" -> Color(0xFF171717)
                        else -> Background
                    }
                    Box(
                        modifier = Modifier.fillMaxSize().background(pageBackground).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("FINGERPRINT", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                    Text("NEON ANIMATION", color = NeonRed, fontSize = 12.sp, letterSpacing = 2.sp)
                                }
                                TextButton(onClick = { showSettings = true }) { Text("SETTINGS", color = NeonRed) }
                            }
                            Spacer(Modifier.height(22.dp))
                            if (unlocked) {
                                Text("ACCESS GRANTED", color = NeonRed, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                                Text("Animation complete — this is not real biometric authentication.", color = Muted, fontSize = 13.sp)
                                Button(onClick = { unlocked = false; scanning = false }) { Text("Lock animation again") }
                            } else {
                                Text(
                                    when {
                                        scanning -> "SCANNING..."
                                        selectedMode == "Simulated lock-screen experience" -> "SIMULATED LOCK SCREEN"
                                        selectedMode == "Screen wake" -> "SCREEN-WAKE DEMO"
                                        else -> "TOUCH TO UNLOCK"
                                    },
                                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                                )
                                FingerprintScanner(
                                    scanning = scanning,
                                    speed = speed,
                                    onTap = {
                                        if (customPin.isNotBlank()) {
                                            showPinDialog = true
                                        } else {
                                            startScan(
                                                onStart = { scanning = true },
                                                onDone = { scanning = false; unlocked = true },
                                                activity = this@MainActivity,
                                                soundOn = soundOn,
                                                vibrationOn = vibrationOn,
                                                speed = speed
                                            )
                                        }
                                    }
                                )
                                Text(
                                    if (scanning) "Please wait for the animation" else "Tap the fingerprint to play the animation",
                                    color = Muted, fontSize = 13.sp
                                )
                                Text("SIMULATION ONLY • DOES NOT UNLOCK ANDROID", color = NeonRed, fontSize = 10.sp, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.weight(1f))
                            Text("Choose modes and effects in Settings", color = Muted, fontSize = 12.sp)
                        }
                    }
                }

                if (showPinDialog) {
                    AlertDialog(
                        onDismissRequest = { showPinDialog = false; enteredPin = ""; pinError = false },
                        title = { Text("App PIN") },
                        text = {
                            Column {
                                Text("Enter the demo PIN to continue. This PIN only gates this app; it is not your phone PIN.")
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = { enteredPin = it; pinError = false },
                                    label = { Text("PIN") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true
                                )
                                if (pinError) Text("PIN does not match.", color = NeonRed)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (enteredPin == customPin) {
                                    showPinDialog = false
                                    enteredPin = ""
                                    startScan(
                                        onStart = { scanning = true },
                                        onDone = { scanning = false; unlocked = true },
                                        activity = this@MainActivity,
                                        soundOn = soundOn,
                                        vibrationOn = vibrationOn,
                                        speed = speed
                                    )
                                } else pinError = true
                            }) { Text("Continue") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPinDialog = false; enteredPin = "" }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}

private fun startScan(
    onStart: () -> Unit,
    onDone: () -> Unit,
    activity: ComponentActivity,
    soundOn: Boolean,
    vibrationOn: Boolean,
    speed: Float
) {
    onStart()
    if (soundOn) {
        try { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70).startTone(ToneGenerator.TONE_PROP_BEEP, 140) } catch (_: Exception) {}
    }
    if (vibrationOn) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
                activity.getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(90, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }
    activity.lifecycleScope.launchWhenStarted {
        delay((1800L / speed.coerceIn(0.5f, 2f)).toLong())
        onDone()
    }
}

@Composable
private fun FingerprintScanner(scanning: Boolean, speed: Float, onTap: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "scanner")
    val pulse by transition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween((1000 / speed.coerceIn(0.5f, 2f)).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = Modifier.size(280.dp).clickable { if (!scanning) onTap() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxR = min(size.width, size.height) * 0.46f
            for (i in 0..2) {
                val radius = maxR * (0.48f + i * 0.22f) * if (scanning) pulse else 1f
                drawCircle(
                    color = NeonRed.copy(alpha = if (scanning) 0.7f else 0.28f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (scanning) 3.5f else 1.6f)
                )
            }
            // Stylized fingerprint made from curved arcs. This is decorative, not a biometric reader.
            val fpR = maxR * 0.43f
            for (i in 0..5) {
                val inset = i * fpR * 0.105f
                drawArc(
                    color = NeonRed.copy(alpha = if (scanning) 1f else 0.85f),
                    startAngle = 205f + i * 5f,
                    sweepAngle = 245f - i * 12f,
                    useCenter = false,
                    topLeft = Offset(center.x - fpR + inset, center.y - fpR + inset),
                    size = Size((fpR - inset) * 2, (fpR - inset) * 2),
                    style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                )
            }
            drawLine(NeonRed.copy(alpha = if (scanning) 0.95f else 0.35f),
                Offset(center.x - maxR * 0.7f, center.y),
                Offset(center.x + maxR * 0.7f, center.y),
                strokeWidth = 1.5f)
        }
    }
}

@Composable
private fun SettingsScreen(
    selectedMode: String,
    onModeChange: (String) -> Unit,
    soundOn: Boolean,
    onSoundChange: (Boolean) -> Unit,
    vibrationOn: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    autoStart: Boolean,
    onAutoStartChange: (Boolean) -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    backgroundChoice: String,
    onBackgroundChange: (String) -> Unit,
    customPin: String,
    onPinSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var pinDraft by remember(customPin) { mutableStateOf(customPin) }
    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("SETTINGS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("DONE", color = NeonRed) }
        }
        Text("Select one mode at a time. Screen-wake mode is best-effort and cannot replace Android's secure lock screen.", color = Muted, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(14.dp)) {
                Text("Unlock experience", color = NeonRed, fontWeight = FontWeight.Bold)
                listOf("Manual app launch", "Screen wake", "Simulated lock-screen experience").forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedMode == mode, onClick = { onModeChange(mode) }, colors = RadioButtonDefaults.colors(selectedColor = NeonRed))
                        Column(Modifier.weight(1f)) {
                            Text(mode, color = Color.White)
                            if (mode == "Screen wake") Text("Best effort only; Android/OEM may restrict it.", color = Muted, fontSize = 11.sp)
                            if (mode == "Simulated lock-screen experience") Text("Runs inside this app, not over system security.", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Effects", color = NeonRed, fontWeight = FontWeight.Bold)
                SettingSwitch("Sound effects", soundOn, onSoundChange)
                SettingSwitch("Vibration", vibrationOn, onVibrationChange)
                SettingSwitch("Auto-start where supported", autoStart, onAutoStartChange)
                Text("Auto-start is a saved preference in this starter. Background launch requires additional platform-compliant implementation and may not be allowed.", color = Muted, fontSize = 11.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Animation speed", color = NeonRed, fontWeight = FontWeight.Bold)
                Text(when { speed < 0.8f -> "Slow"; speed > 1.3f -> "Fast"; else -> "Normal" }, color = Muted)
                Slider(value = speed, onValueChange = onSpeedChange, valueRange = 0.5f..2f, colors = SliderDefaults.colors(thumbColor = NeonRed, activeTrackColor = NeonRed))
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Background", color = NeonRed, fontWeight = FontWeight.Bold)
                listOf("Black", "Deep red", "Charcoal").forEach { bg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = backgroundChoice == bg, onClick = { onBackgroundChange(bg) }, colors = RadioButtonDefaults.colors(selectedColor = NeonRed))
                        Text(bg, color = Color.White)
                    }
                }
                Text("Custom image picker is not included in this starter; these are built-in background presets.", color = Muted, fontSize = 11.sp)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Optional in-app PIN", color = NeonRed, fontWeight = FontWeight.Bold)
                Text("Demo only. This is not Android's screen lock or biometric authentication.", color = Muted, fontSize = 12.sp)
                OutlinedTextField(value = pinDraft, onValueChange = { pinDraft = it }, label = { Text("Set app PIN (blank disables)") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                Button(onClick = { onPinSave(pinDraft) }, colors = ButtonDefaults.buttonColors(containerColor = NeonRed)) { Text("Save PIN") }
            }
        }
        Text("Neon red • Centered scanner • Simulated animation only", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonRed))
    }
}
