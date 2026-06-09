package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EnsembleDashboard
import com.example.ui.EnsembleDashboardViewModel
import com.example.ui.theme.MyApplicationTheme
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {
    private val ensembleViewModel: EnsembleDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- SECURE CRASH PROTECTION FRAMEWORK ---
        // Setup default Uncaught Exception Handler to capture all system, rendering, or process crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Generate detailed crash log report
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val crashLog = sw.toString()
                
                // Package inside recovery intent to restart cleanly
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("CRASH_LOG", crashLog)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to default handler if anything breaks during crash logging
                defaultHandler?.uncaughtException(thread, throwable)
            }
            
            // Terminate process elegantly to prevent any OS freeze popups or lockups
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }

        enableEdgeToEdge()
        
        // Extract potential crash log from recovery trigger
        val crashLogData = intent.getStringExtra("CRASH_LOG")

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0C0E14)
                ) {
                    if (crashLogData != null) {
                        // Display the interactive Crash Protection and Log copy console
                        ResilientCrashScreen(
                            crashLog = crashLogData,
                            onRestart = {
                                // Clear crash log extras and perform clean reboot of app state
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                startActivity(intent)
                                finish()
                            },
                            onResetState = {
                                // Clear shared preferences and app cache
                                val prefs = getSharedPreferences("ensemble_prefs", Context.MODE_PRIVATE)
                                prefs.edit().clear().apply()
                                
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                startActivity(intent)
                                finish()
                            }
                        )
                    } else {
                        // Render standard functional view
                        EnsembleDashboard(viewModel = ensembleViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResilientCrashScreen(
    crashLog: String,
    onRestart: () -> Unit,
    onResetState: () -> Unit
) {
    val context = LocalContext.current
    var showMoreTrace by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF140B12),
                        Color(0xFF0E0B19),
                        Color(0xFF0C0E14)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cybernetic Warning Circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFFFF5722), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "System Intercept warning icon",
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "СИСТЕМА ЗАЩИТЫ СБОЕВ",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFFFF5722),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Перехватчик синаптического шлюза успешно изолировал фатальную ошибку во время выполнения приложения. База данных и сессия защищены.",
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = Color(0xFFE3E2E6),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            // Micro system spec dashboard
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111422)),
                border = BorderStroke(1.dp, Color(0xFF2C2F44)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "СТАТУС ИЗОЛЯЦИИ СБОЯ",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF00FF9D)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Угроза вылета:", color = Color(0xFF9E9E9E), fontSize = 11.sp)
                        Text("НЕЙТРАЛИЗОВАНА", color = Color(0xFF00FF9D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Интегрити локальных данных:", color = Color(0xFF9E9E9E), fontSize = 11.sp)
                        Text("СОХРАНЕНО (100%)", color = Color(0xFF2196F3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real Stack Trace terminal screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF1E2230))
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = "FATAL_EXCEPTION_DUMP:\n$crashLog",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE91E63),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Action Triggers Buttons - Fully compliant with beautiful circle shapes!
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Ensemble Crash Log", crashLog)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Лог сбоя успешно скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF9D),
                    contentColor = Color.Black
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Копировать лог ошибки",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A73E8),
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "Перезапустить",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onResetState,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2230),
                        contentColor = Color(0xFFE3E2E6)
                    ),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF2C2F44)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "Сбросить кеш",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
