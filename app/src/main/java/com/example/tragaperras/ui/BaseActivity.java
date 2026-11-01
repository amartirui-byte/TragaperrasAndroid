package com.example.tragaperras.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;
import com.example.tragaperras.R;
import com.example.tragaperras.SlotApplication;
import com.example.tragaperras.domain.GameRepository;
import com.example.tragaperras.domain.GameRules;

/** Comportamiento visual común: barra de acción, márgenes del sistema, menú y mensajes. */
public abstract class BaseActivity extends Activity {
    private boolean navigationEnabled = true;

    /** Conecta la Toolbar nativa y evita que el contenido quede bajo las barras del sistema. */
    protected void configureScreen(int title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar); setTitle(title);
        View root = findViewById(R.id.screen_root);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
    }

    /** Obtiene la dependencia de datos sin construir una base nueva por pantalla. */
    protected GameRepository repository() { return ((SlotApplication) getApplication()).repository(); }

    /** Crea las opciones de navegación exigidas en el enunciado. */
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu); return true;
    }

    /** Bloquea el menú mientras se está preparando o animando una jugada. */
    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) menu.getItem(i).setEnabled(navigationEnabled);
        return super.onPrepareOptionsMenu(menu);
    }

    /** Permite a cada pantalla coordinar el menú con su estado actual. */
    protected void enableNavigation(boolean enabled) { navigationEnabled = enabled; invalidateOptionsMenu(); }

    /** Responde al menú mediante navegación Android o un diálogo de reglas. */
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (!navigationEnabled) return true;
        if (item.getItemId() == R.id.menu_home) {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            return true;
        }
        if (item.getItemId() == R.id.menu_history) {
            if (!(this instanceof HistoryActivity)) startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.menu_rules) {
            new AlertDialog.Builder(this).setTitle(R.string.rules_title).setMessage(getString(R.string.rules_message,
                    GameRules.INITIAL_COINS, GameRules.SPIN_COST, GameRules.THREE_EQUAL_PRIZE,
                    GameRules.TWO_EQUAL_PRIZE)).setPositiveButton(R.string.understood, null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Registra el detalle técnico en Logcat y muestra un aviso comprensible. */
    public void showError(Throwable error) {
        Log.e("Tragaperras", "No se pudo completar la operación", error);
        if (!isFinishing()) new AlertDialog.Builder(this).setTitle(R.string.error_title)
                .setMessage(R.string.error_message).setPositiveButton(R.string.understood, null).show();
    }
}
