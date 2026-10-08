package ru.prinyato.app;

import android.content.Context;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Принимает из веб-части JSON с приёмами на сегодня и завтра и перерисовывает виджеты. */
@CapacitorPlugin(name = "TodayWidget")
public class WidgetPlugin extends Plugin {
    @PluginMethod
    public void update(PluginCall call) {
        String data = call.getString("data");
        if (data == null) {
            call.reject("Нет данных для виджета");
            return;
        }
        getContext().getSharedPreferences(TodayWidget.PREFS, Context.MODE_PRIVATE)
                .edit().putString("data", data).apply();
        TodayWidget.updateAll(getContext());
        call.resolve();
    }
}
