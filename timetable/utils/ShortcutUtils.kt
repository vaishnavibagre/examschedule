package com.ulan.timetable.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.ulan.timetable.R


@RequiresApi(25)
class ShortcutUtils {

    companion object {
        fun createShortcuts(context: Context) {

            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutList = mutableListOf<ShortcutInfo>()


            shortcutManager!!.dynamicShortcuts = shortcutList
        }


        private const val size = 256
        private const val padding = 65
        private fun createShortcut(context: Context, id: String, shortLabel: String, iconId: Int, intent: Intent): ShortcutInfo {
            val icon = ContextCompat.getDrawable(context, iconId)
            icon?.setTint(Color.WHITE)

            val background = ContextCompat.getDrawable(context, R.drawable.shortcuts_background)
            val combined = LayerDrawable(arrayOf(background, icon))
            combined.setLayerInset(1, padding, padding, padding, padding)

            val combinedIcon = if (Build.VERSION.SDK_INT > 25) Icon.createWithAdaptiveBitmap(combined.toBitmap(size, size)) else Icon.createWithBitmap(combined.toBitmap(size, size))

            return ShortcutInfo.Builder(context, id)
                    .setShortLabel(shortLabel)
                    .setIcon(combinedIcon)
                    .setIntent(intent)
                    .build()
        }





    }
}