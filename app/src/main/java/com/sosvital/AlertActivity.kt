package com.sosvital

import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.sosvital.databinding.ActivityAlertBinding

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding

    private var countDownTimer: CountDownTimer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var ringtone: android.media.Ringtone? = null
    private var originalAlarmVolume = 0
    private var helpSent = false

    companion object {
        private const val COUNTDOWN_MS = 30_000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupOverLockScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        acquireWakeLock()
        startAlarm()
        vibrate()
        startCountdown()

        binding.btnEstoyBien.setOnClickListener { cancelAlert() }
        binding.btnAyudaYa.setOnClickListener { dispatchHelp() }

        // Block back button — user must explicitly tap "Estoy bien"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })
    }

    // -------------------------------------------------------------------------
    // Lock-screen overlay
    // -------------------------------------------------------------------------

    private fun setupOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // -------------------------------------------------------------------------
    // Wake lock — keeps CPU alive so the countdown and SMS dispatch run
    // -------------------------------------------------------------------------

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SOSvital:AlertLock")
        wakeLock?.acquire(45_000L) // 30s countdown + 15s buffer for SMS
    }

    // -------------------------------------------------------------------------
    // Alarm sound — uses STREAM_ALARM, bypasses Do Not Disturb
    // -------------------------------------------------------------------------

    private fun startAlarm() {
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager
        originalAlarmVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        audio.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        ringtone = RingtoneManager.getRingtone(this, uri)
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone?.play()
    }

    // -------------------------------------------------------------------------
    // Vibration
    // -------------------------------------------------------------------------

    private fun vibrate() {
        val pattern = longArrayOf(0, 500, 300, 500, 300, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    private fun stopVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator.cancel()
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).cancel()
        }
    }

    // -------------------------------------------------------------------------
    // 30-second countdown
    // -------------------------------------------------------------------------

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(COUNTDOWN_MS, 1000) {
            override fun onTick(remaining: Long) {
                val secs = (remaining / 1000).toInt()
                binding.tvCountdown.text = secs.toString()
                if (secs <= 10) binding.tvCountdown.setTextColor(Color.YELLOW)
                if (secs <= 5) binding.tvCountdown.setTextColor(Color.parseColor("#FF4444"))
            }
            override fun onFinish() {
                binding.tvCountdown.text = "0"
                dispatchHelp()
            }
        }.start()
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    private fun cancelAlert() {
        stopEverything()
        finish()
    }

    private fun dispatchHelp() {
        if (helpSent) return
        helpSent = true
        stopEverything()

        binding.btnEstoyBien.isEnabled = false
        binding.btnAyudaYa.isEnabled = false
        binding.tvStatus.visibility = View.VISIBLE
        binding.tvStatus.text = getString(R.string.getting_location)

        LocationHelper.getCurrentLocation(this) { location ->
            val contacts = ContactsManager.getContacts(this)
            SmsHelper.sendEmergencySms(this, contacts, location)

            runOnUiThread {
                binding.tvStatus.text = getString(R.string.help_sent, contacts.size)
            }
            // Give user 3 seconds to see the confirmation, then close
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3_000L)
        }
    }

    private fun stopEverything() {
        countDownTimer?.cancel()
        stopVibration()
        ringtone?.stop()
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager
        audio.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
        wakeLock?.let { if (it.isHeld) it.release() }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopEverything()
    }
}
