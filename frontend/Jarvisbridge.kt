package com.jarvis.assistant

import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject

class JarvisBridge(
    private val context: Context
) {

    @JavascriptInterface
    fun execute(json: String): String {

        return try {

            val command =
                JSONObject(json)

            val action =
                command.optString("action")

            when (action) {

                "open_app" -> {

                    val app =
                        command.optString("app")

                    AppLauncher.openApp(
                        context,
                        app
                    )

                }

                "device_command" -> {

                    val deviceCommand =
                        command.optString("command")

                    AppLauncher.deviceCommand(
                        context,
                        deviceCommand
                    )
                }

                else -> {

                    "Unknown JARVIS action"
                }
            }

        } catch (e: Exception) {

            "Android bridge error: ${e.message}"
        }
    }
}
