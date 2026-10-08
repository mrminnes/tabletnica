package ru.prinyato.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Виджет 2×2 «Сегодня»: сколько приёмов отмечено и какой следующий.
 * Данные приходят из приложения (WidgetPlugin). Система перерисовывает виджет
 * раз в 30 минут — «следующий приём» и смена дня считаются по текущему времени.
 */
public class TodayWidget extends AppWidgetProvider {
    static final String PREFS = "prinyato_widget";
    private static final int COLOR_DUE = 0xFFF0B44C;   // пора принять — янтарный
    private static final int COLOR_NEXT = 0xFF8FE3C0;  // следующий / всё принято — мятный

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) manager.updateAppWidget(id, build(context));
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TodayWidget.class));
        for (int id : ids) manager.updateAppWidget(id, build(context));
    }

    static RemoteViews build(Context context) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_today);

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.widget_root, pi);

        String big = "—";
        String sub = "открой приложение";
        String label = "";
        String next = "";
        int labelColor = COLOR_NEXT;

        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("data", null);
        try {
            if (raw != null) {
                JSONObject d = new JSONObject(raw);
                Calendar c = Calendar.getInstance();
                SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String today = day.format(c.getTime());
                String now = new SimpleDateFormat("HH:mm", Locale.US).format(c.getTime());
                c.add(Calendar.DAY_OF_MONTH, -1);
                String yesterday = day.format(c.getTime());

                JSONArray list = null;
                JSONArray tomorrow = null;
                if (today.equals(d.optString("date"))) {
                    list = d.optJSONArray("today");
                    tomorrow = d.optJSONArray("tomorrow");
                } else if (yesterday.equals(d.optString("date"))) {
                    // Приложение не открывали с вчера — берём вчерашний план «на завтра»
                    list = d.optJSONArray("tomorrow");
                }

                if (list != null) {
                    int total = list.length();
                    int done = 0;
                    JSONObject pending = null;
                    for (int i = 0; i < total; i++) {
                        JSONObject it = list.getJSONObject(i);
                        if (it.optInt("ok") == 1) done++;
                        else if (pending == null) pending = it;
                    }
                    if (total == 0) {
                        big = "0";
                        sub = "приёмов сегодня нет";
                    } else {
                        big = done + " из " + total;
                        sub = "принято сегодня";
                    }
                    if (pending != null) {
                        String t = pending.optString("t");
                        boolean due = t.compareTo(now) <= 0;
                        label = due ? "Пора принять" : "Следующий приём";
                        labelColor = due ? COLOR_DUE : COLOR_NEXT;
                        next = t + "  " + pending.optString("nm");
                    } else if (total > 0) {
                        label = "Всё принято";
                        if (tomorrow != null && tomorrow.length() > 0) {
                            JSONObject first = tomorrow.getJSONObject(0);
                            next = "завтра " + first.optString("t") + "  " + first.optString("nm");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Повреждённые данные — показываем заглушку, приложение при открытии их перезапишет
        }

        v.setTextViewText(R.id.widget_big, big);
        v.setTextViewText(R.id.widget_sub, sub);
        v.setTextViewText(R.id.widget_label, label);
        v.setTextColor(R.id.widget_label, labelColor);
        v.setTextViewText(R.id.widget_next, next);
        v.setViewVisibility(R.id.widget_label, label.isEmpty() ? View.GONE : View.VISIBLE);
        v.setViewVisibility(R.id.widget_next, next.isEmpty() ? View.GONE : View.VISIBLE);
        return v;
    }
}
