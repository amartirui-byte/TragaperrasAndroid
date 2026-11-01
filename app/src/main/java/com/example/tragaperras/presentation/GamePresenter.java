package com.example.tragaperras.presentation;

import com.example.tragaperras.domain.*;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/** Máquina de estados de la partida: coordina datos y animación sin dibujar. */
public final class GamePresenter {
    /** Interfaz gráfica mínima que el presentador necesita. */
    public interface View {
        /** Actualiza qué acciones están disponibles. */ void controls(boolean canSpin, boolean canStop, boolean canLeave);
        /** Restaura el saldo y los símbolos guardados. */ void showPlayer(PlayerSnapshot player);
        /** Comienza el movimiento visual con el coste ya descontado. */ void startReels(SpinResult result);
        /** Frena los rodillos hasta el resultado ya sorteado. */ void stopReels(int[] symbols);
        /** Revela el premio y el saldo final. */ void showResult(SpinResult result);
        /** Presenta el fallo de una operación de datos. */ void showError(Throwable error);
    }
    private enum Phase { LOADING, READY, SAVING, SPINNING, STOPPING }
    private final GameRepository repository;
    private final Scheduler ui;
    private final CompositeDisposable subscriptions = new CompositeDisposable();
    private View view;
    private Phase phase = Phase.LOADING;
    private int coins;
    private SpinResult activeSpin;

    /** Recibe los datos y el planificador de la interfaz, sin importar clases Android. */
    public GamePresenter(GameRepository repository, Scheduler ui) { this.repository = repository; this.ui = ui; }

    /** Recupera el último estado, incluso si la app se interrumpió durante una animación. */
    public void attach(View view) {
        this.view = view; phase = Phase.LOADING; coins = 0; view.controls(false, false, false);
        subscriptions.add(repository.loadPlayer().observeOn(ui).subscribe(player -> {
            coins = player.coins; phase = Phase.READY;
            if (this.view != null) { this.view.showPlayer(player); updateControls(); }
        }, this::fail));
    }

    /** Bloquea dobles pulsaciones y pide guardar el giro antes de representarlo. */
    public void spin() {
        if (view == null || phase != Phase.READY || coins < GameRules.SPIN_COST) return;
        phase = Phase.SAVING; updateControls();
        subscriptions.add(repository.playSpin().observeOn(ui).subscribe(result -> {
            activeSpin = result; coins = result.balanceAfter; phase = Phase.SPINNING;
            if (view != null) { updateControls(); view.startReels(result); }
        }, this::fail));
    }

    /** Parar cambia la duración visual, nunca los símbolos ni el premio. */
    public void stop() {
        if (view == null || phase != Phase.SPINNING) return;
        phase = Phase.STOPPING; updateControls(); view.stopReels(activeSpin.getSymbols());
    }

    /** Finaliza la representación cuando los tres rodillos han terminado de frenar. */
    public void animationFinished() {
        if (view == null || phase != Phase.STOPPING) return;
        phase = Phase.READY; view.showResult(activeSpin); activeSpin = null; updateControls();
    }

    /** Impide navegar mientras una pulsación todavía está resolviéndose. */
    public boolean canLeave() { return phase == Phase.READY; }

    /** Cancela observadores visuales; una transacción ya completada sigue guardada. */
    public void detach() { subscriptions.clear(); view = null; activeSpin = null; phase = Phase.LOADING; }

    /** Convierte el estado interno en controles de interfaz disponibles. */
    private void updateControls() {
        if (view != null) view.controls(phase == Phase.READY && coins >= GameRules.SPIN_COST,
                phase == Phase.SPINNING, phase == Phase.READY);
    }

    /** Permite salir y muestra el error; ninguna animación anuncia un guardado fallido. */
    private void fail(Throwable error) {
        phase = Phase.READY;
        if (view != null) { updateControls(); view.showError(error); }
    }
}
