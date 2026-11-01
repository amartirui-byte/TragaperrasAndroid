package com.example.tragaperras.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.tragaperras.R;
import com.example.tragaperras.domain.*;
import com.example.tragaperras.presentation.HomePresenter;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/** Vista de bienvenida: enlaza el XML con el presentador; no realiza consultas ni sorteos. */
public final class MainActivity extends BaseActivity implements HomePresenter.View {
    private HomePresenter presenter;
    private Button startButton, newButton, closeButton;
    private TextView balance, best;

    /** Infla la pantalla y convierte las pulsaciones en peticiones al presentador. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main); configureScreen(R.string.app_name);
        presenter = new HomePresenter(repository(), AndroidSchedulers.mainThread());
        startButton = findViewById(R.id.start_button); newButton = findViewById(R.id.new_button);
        closeButton = findViewById(R.id.close_button); balance = findViewById(R.id.home_coins);
        best = findViewById(R.id.best_results);
        ((TextView) findViewById(R.id.welcome_hint)).setText(getString(R.string.initial_hint, GameRules.INITIAL_COINS));
        startButton.setOnClickListener(view -> presenter.continueGame());
        newButton.setOnClickListener(view -> confirmNewGame());
        closeButton.setOnClickListener(view -> finishAndRemoveTask());
    }

    /** Recarga el saldo cada vez que la bienvenida se hace visible. */
    @Override protected void onStart() { super.onStart(); presenter.attach(this); }

    /** Desconecta la vista antes de dejar de ser visible. */
    @Override protected void onStop() { presenter.detach(); super.onStop(); }

    /** Confirma el reinicio únicamente cuando ya hay una partida que sustituir. */
    private void confirmNewGame() {
        if (!presenter.hasSession()) { presenter.newGame(); return; }
        new AlertDialog.Builder(this).setTitle(R.string.new_confirmation_title)
                .setMessage(getString(R.string.new_confirmation, GameRules.INITIAL_COINS))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.new_game, (dialog, which) -> presenter.newGame()).show();
    }

    /** Evita solicitudes repetidas mientras se leen o guardan datos. */
    @Override public void setLoading(boolean loading) {
        startButton.setEnabled(!loading); newButton.setEnabled(!loading);
        closeButton.setEnabled(!loading); enableNavigation(!loading);
    }

    /** Representa el saldo y las mejores jugadas sin alterar sus valores. */
    @Override public void showHome(HomeData data) {
        findViewById(R.id.home_balance_box).setVisibility(data.player.hasSession() ? View.VISIBLE : View.GONE);
        balance.setText(getString(R.string.coins_amount, data.player.coins));
        startButton.setText(data.player.hasSession() ? R.string.continue_game : R.string.start_game);
        StringBuilder text = new StringBuilder();
        for (SpinResult spin : data.bestSpins) text.append(SpinAdapter.summary(this, spin)).append("\n\n");
        best.setText(text.toString().trim());
        findViewById(R.id.best_group).setVisibility(data.bestSpins.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Abre la Activity de juego; el saldo se recuperará desde el repositorio. */
    @Override public void openGame() { startActivity(new Intent(this, GameActivity.class)); }
}
