package com.example.tragaperras.presentation;

import com.example.tragaperras.domain.*;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/** Coordina la bienvenida sin conocer XML, Activity ni instrucciones SQL. */
public final class HomePresenter {
    /** Operaciones que la pantalla ofrece al presentador. */
    public interface View {
        /** Activa o bloquea los botones durante una operación. */ void setLoading(boolean loading);
        /** Dibuja los datos recibidos. */ void showHome(HomeData data);
        /** Abre la pantalla de juego. */ void openGame();
        /** Informa de un fallo sin mostrar una falsa confirmación. */ void showError(Throwable error);
    }
    private final GameRepository repository;
    private final Scheduler ui;
    private final CompositeDisposable subscriptions = new CompositeDisposable();
    private View view;
    private PlayerSnapshot player;
    private boolean busy;

    /** Recibe dependencias por constructor para poder sustituirlas en pruebas. */
    public HomePresenter(GameRepository repository, Scheduler ui) { this.repository = repository; this.ui = ui; }

    /** Conecta la pantalla y actualiza su saldo al volver desde el juego. */
    public void attach(View view) {
        this.view = view; busy = true; view.setLoading(true);
        subscriptions.add(repository.loadHome().observeOn(ui).subscribe(data -> {
            player = data.player; busy = false;
            if (this.view != null) { this.view.setLoading(false); this.view.showHome(data); }
        }, this::fail));
    }

    /** Permite a la vista decidir si debe pedir confirmación antes de reiniciar. */
    public boolean hasSession() { return player != null && player.hasSession(); }

    /** Continúa la partida existente o crea la primera. */
    public void continueGame() {
        if (busy || view == null) return;
        if (hasSession()) {
            // Mantiene el bloqueo hasta salir para evitar abrir dos pantallas por doble pulsación.
            busy = true; view.setLoading(true); view.openGame();
        } else newGame();
    }

    /** Solicita una nueva sesión; el repositorio conserva el histórico. */
    public void newGame() {
        if (busy || view == null) return;
        busy = true; view.setLoading(true);
        subscriptions.add(repository.startNewSession().observeOn(ui).subscribe(state -> {
            player = state;
            if (view != null) view.openGame();
        }, this::fail));
    }

    /** Libera suscripciones y referencias para no retener una Activity cerrada. */
    public void detach() { subscriptions.clear(); view = null; busy = false; }

    /** Restaura los controles e informa del error en la pantalla conectada. */
    private void fail(Throwable error) {
        busy = false;
        if (view != null) { view.setLoading(false); view.showError(error); }
    }
}
