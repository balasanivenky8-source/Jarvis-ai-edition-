package com.jarvis.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object AppLauncher {

    fun openApp(
        context: Context,
        appName: String
    ): String {

        val packageName = when (
            appName.lowercase()
        ) {

            "youtube" ->
                "com.google.android.youtube"

            "instagram" ->
                "com.instagram.android"

            "snapchat" ->
                "com.snapchat.android"

            "google" ->
                "com.google.android.googlequicksearchbox"

            "chrome" ->
                "com.android.chrome"

            else ->
                return "I don't know that application."
        }


        val manager =
            context.packageManager

        val launchIntent =
            manager.getLaunchIntentForPackage(
                packageName
            )


        if (launchIntent == null) {

            Toast.makeText(
                context,
                "$appName is not installed",
                Toast.LENGTH_SHORT
            ).show()

            return "$appName is not installed."
        }


        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        context.startActivity(
            launchIntent
        )

        return "Opening $appName."
    }


    fun deviceCommand(
        context: Context,
        command: String
    ): String {

        when (command) {

            "open_camera" -> {

                val intent =
                    Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(intent)

                return "Opening camera."
            }


            "open_settings" -> {

                val intent =
                    Intent(
                        Settings.ACTION_SETTINGS
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(intent)

                return "Opening settings."
            }


            "open_wifi_settings" -> {

                val intent =
                    Intent(
                        Settings.ACTION_WIFI_SETTINGS
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(intent)

                return "Opening Wi-Fi settings."
            }


            "open_bluetooth_settings" -> {

                val intent =
                    Intent(
                        Settings.ACTION_BLUETOOTH_SETTINGS
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(intent)

                return "Opening Bluetooth settings."
            }


            else -> {

                return "Unsupported device command."
            }
        }
    }
}
