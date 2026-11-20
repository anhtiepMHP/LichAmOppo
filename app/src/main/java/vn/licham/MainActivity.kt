package vn.licham

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import java.util.Calendar

private val RED = Color.parseColor("#FF9E91")
private val DIM = Color.parseColor("#D9C3A8")
private val GOLD = Color.parseColor("#FFD27D")

class MainActivity : Activity() {
    private var year = 0
    private var month = 0
    private lateinit var title: TextView
    private lateinit var grid: GridLayout

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val now = Calendar.getInstance()
        year = now.get(Calendar.YEAR); month = now.get(Calendar.MONTH) + 1

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#3E2F23"))
            setPadding(dp(10), dp(16), dp(10), dp(10))
        }
        title = TextView(this).apply {
            textSize = 20f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                val n = Calendar.getInstance()
                year = n.get(Calendar.YEAR); month = n.get(Calendar.MONTH) + 1; render()
            }
        }
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        head.addView(nav("‹", -1), LinearLayout.LayoutParams(dp(56), dp(48)))
        head.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        head.addView(nav("›", 1), LinearLayout.LayoutParams(dp(56), dp(48)))
        grid = GridLayout(this).apply { columnCount = 7 }
        val foot = TextView(this).apply {
            text = "Hôm nay: " + DateWidget.todayText() + "\nChạm vào tiêu đề để về tháng hiện tại"
            textSize = 14f; setTextColor(Color.parseColor("#E0CDB8")); setPadding(0, dp(16), 0, 0)
        }
        val sw = Switch(this).apply {
            text = "Hiện ngày âm ở màn hình khóa (thông báo)"
            setTextColor(Color.WHITE)
            isChecked = DateNotifier.isOn(this@MainActivity)
            setPadding(0, dp(16), 0, 0)
            setOnCheckedChangeListener { _, on -> DateNotifier.setOn(this@MainActivity, on) }
        }
        root.addView(head); root.addView(grid); root.addView(foot); root.addView(sw)
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        render()
    }

    override fun onResume() {
        super.onResume()
        DateNotifier.refresh(this)
    }

    private fun nav(s: String, d: Int) = TextView(this).apply {
        text = s; textSize = 28f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        setOnClickListener {
            month += d
            if (month > 12) { month = 1; year++ } else if (month < 1) { month = 12; year-- }
            render()
        }
    }

    private fun cellView(t: CharSequence, color: Int, today: Boolean) = TextView(this).apply {
        text = t; setTextColor(color); textSize = 17f; gravity = Gravity.CENTER
        if (today) background = GradientDrawable().apply {
            cornerRadius = dp(12).toFloat(); setColor(Color.parseColor("#66E9C892"))
        }
    }

    private fun lp(row: Int, col: Int, h: Int) = GridLayout.LayoutParams(
        GridLayout.spec(row), GridLayout.spec(col, 1, GridLayout.FILL, 1f)
    ).apply { width = 0; height = h; setMargins(dp(1), dp(1), dp(1), dp(1)) }

    private fun render() {
        title.text = "Tháng $month/$year"
        grid.removeAllViews()
        val names = arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        for (i in 0..6) grid.addView(
            cellView(SpannableStringBuilder(names[i]), if (i == 6) RED else DIM, false), lp(0, i, dp(32))
        )
        val first = Calendar.getInstance().apply { clear(); set(year, month - 1, 1) }
        val offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val days = first.getActualMaximum(Calendar.DAY_OF_MONTH)
        val t = Calendar.getInstance()
        for (d in 1..days) {
            val pos = offset + d - 1
            val l = LunarCalc.convert(d, month, year)
            val lt = if (l.day == 1) "${l.day}/${l.month}" else "${l.day}"
            val sb = SpannableStringBuilder("$d\n$lt")
            val s = d.toString().length + 1
            sb.setSpan(RelativeSizeSpan(0.62f), s, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(
                ForegroundColorSpan(if (l.day == 1 || l.day == 15) GOLD else DIM),
                s, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val today = t.get(Calendar.YEAR) == year && t.get(Calendar.MONTH) + 1 == month &&
                    t.get(Calendar.DAY_OF_MONTH) == d
            grid.addView(
                cellView(sb, if (pos % 7 == 6) RED else Color.WHITE, today),
                lp(1 + pos / 7, pos % 7, dp(54))
            )
        }
    }
}
