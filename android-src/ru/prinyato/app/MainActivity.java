package ru.prinyato.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Свой плагин: приложение передаёт расписание виджету на рабочем столе
        registerPlugin(WidgetPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
