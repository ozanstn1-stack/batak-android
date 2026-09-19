package com.batak.turkce.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.batak.turkce.R

class SoundManager(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val idMap = mutableMapOf<Int, Int>()
    private val loaded = mutableSetOf<Int>()

    var enabled: Boolean = true

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded.add(sampleId)
        }
        load(context, R.raw.card_deal)
        load(context, R.raw.card_play)
        load(context, R.raw.button_click)
        load(context, R.raw.bid)
        load(context, R.raw.trick_win)
        load(context, R.raw.round_win)
        load(context, R.raw.round_lose)
    }

    private fun load(context: Context, resId: Int) {
        idMap[resId] = pool.load(context, resId, 1)
    }

    fun play(resId: Int, volume: Float = 1f, rate: Float = 1f) {
        if (!enabled) return
        val sampleId = idMap[resId] ?: return
        if (sampleId !in loaded) return
        pool.play(sampleId, volume, volume, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    fun release() {
        pool.release()
    }
}
