package vn.licham

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val tv = TextView(this).apply {
            text = DateWidget.todayText() +
                "\n\nGiữ lâu màn hình chính → Tiện ích (Widgets) → Lịch Âm - Ngày"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#3E2F23"))
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        setContentView(tv)
    }
}
