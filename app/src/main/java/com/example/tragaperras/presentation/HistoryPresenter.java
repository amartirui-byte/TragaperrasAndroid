package com.example.tragaperras.presentation;

import com.example.tragaperras.domain.GameRepository;
import com.example.tragaperras.domain.SpinResult;
import java.util.List;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/** Obtiene el histórico y entrega objetos listos para representar. */
public final class HistoryPresenter {
    /** Contrato de la pantalla de histórico. */
    public interface View {
        /** Muestra la lista de jugadas, que también puede estar vacía. */ void showHistory(List<SpinResult> results);
        /** Informa del error al recuperar la lista. */ void showError(Throwable error);
    }
    private final GameRepository repository;
    private final Scheduler ui;
    private final CompositeDisposable subscriptions = new CompositeDisposable();

    /** Recibe el origen de datos y el planificador visual. */
    public HistoryPresenter(GameRepository repository, Scheduler ui) { this.repository = repository; this.ui = ui; }

    /** Inicia la consulta asíncrona y entrega la respuesta en el hilo visual. */
    public void attach(View view) {
        subscriptions.add(repository.loadHistory().observeOn(ui).subscribe(view::showHistory, view::showError));
    }

    /** Evita callbacks a una pantalla que ya no está visible. */
    public void detach() { subscriptions.clear(); }
}
