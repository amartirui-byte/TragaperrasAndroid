package com.example.tragaperras.ui;

import android.os.Bundle;
import android.widget.ListView;
import com.example.tragaperras.R;
import com.example.tragaperras.domain.SpinResult;
import com.example.tragaperras.presentation.HistoryPresenter;
import java.util.List;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/** Vista auxiliar del histórico, accesible desde el menú de la barra de acción. */
public final class HistoryActivity extends BaseActivity implements HistoryPresenter.View {
    private HistoryPresenter presenter;
    private ListView list;

    /** Prepara la lista y su mensaje cuando aún no hay jugadas. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_history); configureScreen(R.string.history_title);
        list = findViewById(R.id.history_list); list.setEmptyView(findViewById(R.id.history_empty));
        findViewById(R.id.history_back).setOnClickListener(view -> finish());
        presenter = new HistoryPresenter(repository(), AndroidSchedulers.mainThread());
    }
    /** Solicita los datos al entrar en la pantalla. */
    @Override protected void onStart() { super.onStart(); presenter.attach(this); }
    /** Cancela la recepción de resultados al salir. */
    @Override protected void onStop() { presenter.detach(); super.onStop(); }
    /** Muestra los objetos recibidos mediante filas reciclables. */
    @Override public void showHistory(List<SpinResult> results) { list.setAdapter(new SpinAdapter(this, results)); }
}
