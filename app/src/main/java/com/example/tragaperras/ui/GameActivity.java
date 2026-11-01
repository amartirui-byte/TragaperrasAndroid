package com.example.tragaperras.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tragaperras.R;
import com.example.tragaperras.domain.*;
import com.example.tragaperras.presentation.GamePresenter;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/** Pantalla de juego: solo dibuja, anima y comunica eventos al presentador. */
public final class GameActivity extends BaseActivity implements GamePresenter.View {
    private GamePresenter presenter;
    private final ReelView[] reels = new ReelView[3];
    private final Handler animationClock = new Handler(Looper.getMainLooper());
    private Button spinButton, stopButton, leaveButton;
    private TextView balance, message;
    private int stoppedReels;

    /** Localiza las vistas del XML y conecta los botones a su comportamiento. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_game); configureScreen(R.string.app_name);
        presenter = new GamePresenter(repository(), AndroidSchedulers.mainThread());
        reels[0] = findViewById(R.id.reel_one); reels[1] = findViewById(R.id.reel_two); reels[2] = findViewById(R.id.reel_three);
        balance = findViewById(R.id.game_coins); message = findViewById(R.id.result_message);
        spinButton = findViewById(R.id.spin_button); stopButton = findViewById(R.id.stop_button); leaveButton = findViewById(R.id.leave_button);
        ((TextView) findViewById(R.id.spin_cost)).setText(getString(R.string.spin_cost, GameRules.SPIN_COST));
        spinButton.setOnClickListener(view -> presenter.spin());
        stopButton.setOnClickListener(view -> presenter.stop());
        leaveButton.setOnClickListener(view -> finish());
    }

    /** Recupera el estado guardado al abrir la pantalla o volver desde segundo plano. */
    @Override protected void onStart() { super.onStart(); presenter.attach(this); }

    /** Cancela animaciones visuales; el resultado ya confirmado permanece en SQLite. */
    @Override protected void onStop() {
        animationClock.removeCallbacksAndMessages(null);
        for (ReelView reel : reels) reel.cancelAnimation();
        presenter.detach(); super.onStop();
    }

    /** Aplica las acciones permitidas sin decidir las reglas del juego. */
    @Override public void controls(boolean canSpin, boolean canStop, boolean canLeave) {
        spinButton.setEnabled(canSpin); stopButton.setEnabled(canStop); leaveButton.setEnabled(canLeave);
        enableNavigation(canLeave);
    }

    /** Restaura saldo y última combinación después de una reapertura o rotación. */
    @Override public void showPlayer(PlayerSnapshot player) {
        if (!player.hasSession()) { finish(); return; }
        balance.setText(getString(R.string.coins_amount, player.coins));
        int[] symbols = player.lastSpin == null ? new int[]{0, 3, 4} : player.lastSpin.getSymbols();
        for (int i = 0; i < reels.length; i++) reels[i].setSymbol(symbols[i]);
        message.setText(player.coins < GameRules.SPIN_COST ? R.string.no_coins : R.string.ready_message);
    }

    /** Desplaza los tres rodillos y programa la detención automática si no se pulsa Parar. */
    @Override public void startReels(SpinResult result) {
        balance.setText(getString(R.string.coins_amount, result.balanceAfterCost()));
        message.setText(R.string.spinning_message);
        for (ReelView reel : reels) reel.startSpinning();
        animationClock.postDelayed(() -> presenter.stop(), 2400);
    }

    /** Frena cada rodillo con distinta duración para que se detengan uno tras otro. */
    @Override public void stopReels(int[] symbols) {
        animationClock.removeCallbacksAndMessages(null); stoppedReels = 0;
        for (int i = 0; i < reels.length; i++) reels[i].stopAt(symbols[i], 650 + i * 250L, this::oneReelStopped);
    }

    /** Notifica al presentador únicamente cuando los tres rodillos están detenidos. */
    private void oneReelStopped() {
        stoppedReels++;
        if (stoppedReels == reels.length) presenter.animationFinished();
    }

    /** Muestra el premio y el cambio neto, que ya se calcularon fuera de la vista. */
    @Override public void showResult(SpinResult result) {
        balance.setText(getString(R.string.coins_amount, result.balanceAfter));
        String net = result.netChange() > 0 ? "+" + result.netChange() : Integer.toString(result.netChange());
        message.setText(getString(R.string.result_message, result.prize, net)
                + (result.balanceAfter < GameRules.SPIN_COST ? "\n" + getString(R.string.no_coins) : ""));
    }

    /** Evita interrumpir la navegación interna antes de terminar la representación de la jugada. */
    @Override public void onBackPressed() {
        if (presenter.canLeave()) super.onBackPressed();
        else Toast.makeText(this, R.string.wait_for_reels, Toast.LENGTH_SHORT).show();
    }
}
