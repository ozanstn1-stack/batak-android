package com.batak.turkce

import android.app.Application
import com.batak.turkce.audio.SoundManager
import com.batak.turkce.audio.VibrationHelper
import com.batak.turkce.data.AppRepository

class BatakApplication : Application() {

    lateinit var repository: AppRepository
        private set

    lateinit var audio: SoundManager
        private set

    lateinit var vibration: VibrationHelper
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(this)
        audio = SoundManager(this)
        vibration = VibrationHelper(this)
    }
}
