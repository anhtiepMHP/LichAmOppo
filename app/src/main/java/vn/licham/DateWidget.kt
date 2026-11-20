package vn.licham

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class DateWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        for (id in ids) {
            val lock = mgr.getAppWidgetOptions(id)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1) ==
                    AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD
            val rv = RemoteViews(ctx.packageName, R.layout.widget)
            rv.setTextViewText(R.id.txt, todayText(lock))
            rv.setTextViewTextSize(R.id.txt, TypedValue.COMPLEX_UNIT_SP, if (lock) 14f else 16f)
            rv.setOnClickPendingIntent(R.id.root, open)
            mgr.updateAppWidget(id, rv)
        }
        scheduleMidnight(ctx)
    }

    override fun onAppWidgetOptionsChanged(
        ctx: Context, mgr: AppWidgetManager, id: Int, newOptions: Bundle
    ) {
        onUpdate(ctx, mgr, intArrayOf(id))
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        val a = intent.action
        if (a == Intent.ACTION_DATE_CHANGED || a == Intent.ACTION_TIME_CHANGED || a == Intent.ACTION_TIMEZONE_CHANGED) {
            val mgr = AppWidgetManager.getInstance(ctx)
            onUpdate(ctx, mgr, mgr.getAppWidgetIds(ComponentName(ctx, DateWidget::class.java)))
        }
    }

    private fun scheduleMidnight(ctx: Context) {
        val c = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5); set(Calendar.MILLISECOND, 0)
        }
        val pi = PendingIntent.getBroadcast(
            ctx, 1, Intent(ctx, DateWidget::class.java).setAction(Intent.ACTION_DATE_CHANGED),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .set(AlarmManager.RTC, c.timeInMillis, pi)
    }

    companion object {
        /** Màn hình chính: "Friday, 20/11 (12/10 AL)". Màn hình khóa: 3 dòng. */
        fun todayText(lock: Boolean = false): String {
            val now = Calendar.getInstance()
            val l = LunarCalc.convert(
                now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR)
            )
            val leap = if (l.leap) "n" else ""
            val lunar = "${l.day}/${l.month}$leap AL"
            return if (lock) {
                val day = SimpleDateFormat("EEEE", Locale.ENGLISH).format(now.time)
                val dm = SimpleDateFormat("dd/MM", Locale.ENGLISH).format(now.time)
                "$day\n$dm\n$lunar"
            } else {
                val solar = SimpleDateFormat("EEEE, dd/MM", Locale.ENGLISH).format(now.time)
                "$solar ($lunar)"
            }
        }
    }
}
