package com.jarvis.assistant

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.abs

class ClapDetector {

    private var running = false

    fun start(
        onClap: () -> Unit
    ) {

        if (running) return

        running = true

        Thread {

            val sampleRate = 16000

            val minBuffer =
                AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

            val recorder =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuffer * 2
                )

            val buffer =
                ShortArray(1024)

            recorder.startRecording()

            var lastClap =
                0L


            while (running) {

                val read =
                    recorder.read(
                        buffer,
                        0,
                        buffer.size
                    )

                if (read <= 0) continue


                var energy = 0L

                for (i in 0 until read) {

                    energy +=
                        abs(buffer[i].toInt())
                }

                val average =
                    energy / read


                /*
                 * Basic prototype threshold.
                 *
                 * This should be calibrated for
                 * the phone/environment.
                 */

                if (average > 5000) {

                    val now =
                        System.currentTimeMillis()

                    if (
                        now - lastClap in
                        150..900
                    ) {

                        onClap()
                    }

                    lastClap = now
                }
            }

            recorder.stop()
            recorder.release()

        }.start()
    }


    fun stop() {

        running = false
    }
}
