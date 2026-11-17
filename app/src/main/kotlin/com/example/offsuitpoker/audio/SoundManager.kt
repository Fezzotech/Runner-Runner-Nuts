package com.example.offsuitpoker.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundManager {
    private const val SAMPLE_RATE = 22050

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val audioFormat = AudioFormat.Builder()
        .setSampleRate(SAMPLE_RATE)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    // Precomputed sound tracks
    private var chipTrack: AudioTrack? = null
    private var cardTrack: AudioTrack? = null
    private var checkTrack: AudioTrack? = null
    private var foldTrack: AudioTrack? = null
    private var turnTrack: AudioTrack? = null
    private var winTrack: AudioTrack? = null

    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_SYSTEM, 90)
        } catch (_: Exception) {}

        try {
            chipTrack = buildTrack(generateChipClink())
            cardTrack = buildTrack(generateCardSnap())
            checkTrack = buildTrack(generateCheckKnock())
            foldTrack = buildTrack(generateFoldSweep())
            turnTrack = buildTrack(generateTurnPing())
            winTrack = buildTrack(generateWinFanfare())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildTrack(data: ShortArray): AudioTrack {
        val bufferSizeBytes = data.size * 2
        val track = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSizeBytes,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        track.write(data, 0, data.size)
        return track
    }

    private fun play(track: AudioTrack?, fallbackTone: Int = -1) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (track != null) {
                    track.stop()
                    track.reloadStaticData()
                    track.play()
                } else if (fallbackTone != -1) {
                    toneGen?.startTone(fallbackTone, 40)
                }
            } catch (e: Exception) {
                if (fallbackTone != -1) {
                    try { toneGen?.startTone(fallbackTone, 40) } catch (_: Exception) {}
                }
            }
        }
    }

    fun playChipBet() {
        play(chipTrack, ToneGenerator.TONE_PROP_BEEP)
    }

    fun playCardDeal() {
        play(cardTrack, ToneGenerator.TONE_PROP_ACK)
    }

    fun playCheck() {
        play(checkTrack, ToneGenerator.TONE_PROP_PROMPT)
    }

    fun playFold() {
        play(foldTrack, ToneGenerator.TONE_PROP_NACK)
    }

    fun playTurnAlert() {
        play(turnTrack, ToneGenerator.TONE_PROP_BEEP2)
    }

    fun playWinFanfare() {
        play(winTrack, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
    }

    private fun generateChipClink(): ShortArray {
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-i.toDouble() / (SAMPLE_RATE * 0.008))
            val wave = sin(2 * PI * 2600 * t) * 0.6 + sin(2 * PI * 3400 * t) * 0.4
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.75).toInt().toShort()
        }
        return samples
    }

    private fun generateCardSnap(): ShortArray {
        val durationMs = 40
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1400.0 - 700.0 * (i.toDouble() / numSamples)
            val envelope = exp(-i.toDouble() / (SAMPLE_RATE * 0.012))
            val wave = sin(2 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    private fun generateCheckKnock(): ShortArray {
        val durationMs = 120
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        val knock1Start = 0
        val knock2Start = (SAMPLE_RATE * 0.055).toInt()
        for (i in 0 until numSamples) {
            var sample = 0.0
            if (i >= knock1Start) {
                val idx = i - knock1Start
                val t = idx.toDouble() / SAMPLE_RATE
                val env = exp(-idx.toDouble() / (SAMPLE_RATE * 0.015))
                sample += sin(2 * PI * 220 * t) * env
            }
            if (i >= knock2Start) {
                val idx = i - knock2Start
                val t = idx.toDouble() / SAMPLE_RATE
                val env = exp(-idx.toDouble() / (SAMPLE_RATE * 0.015))
                sample += sin(2 * PI * 190 * t) * env * 0.85
            }
            samples[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.85).toInt().toShort()
        }
        return samples
    }

    private fun generateFoldSweep(): ShortArray {
        val durationMs = 70
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val freq = 600.0 - 420.0 * progress
            val envelope = exp(-progress * 2.5)
            val wave = sin(2 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    private fun generateTurnPing(): ShortArray {
        val durationMs = 130
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-i.toDouble() / (SAMPLE_RATE * 0.035))
            val wave = sin(2 * PI * 880 * t) * 0.6 + sin(2 * PI * 1320 * t) * 0.4
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.75).toInt().toShort()
        }
        return samples
    }

    private fun generateWinFanfare(): ShortArray {
        val durationMs = 400
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteDuration = (SAMPLE_RATE * 0.085).toInt()
        for (i in 0 until numSamples) {
            val noteIdx = (i / noteDuration).coerceAtMost(notes.size - 1)
            val noteOffset = i - (noteIdx * noteDuration)
            val t = noteOffset.toDouble() / SAMPLE_RATE
            val freq = notes[noteIdx]
            val decay = if (noteIdx == notes.size - 1) 0.07 else 0.025
            val env = exp(-noteOffset.toDouble() / (SAMPLE_RATE * decay))
            val wave = sin(2 * PI * freq * t)
            samples[i] = (wave * env * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return samples
    }
}
