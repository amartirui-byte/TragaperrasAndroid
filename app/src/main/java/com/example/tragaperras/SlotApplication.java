package com.example.tragaperras;

import android.app.Application;
import com.example.tragaperras.data.SqliteGameRepository;
import com.example.tragaperras.domain.GameRepository;

/** Mantiene un repositorio compartido durante la vida de la aplicación. */
public final class SlotApplication extends Application {
    private GameRepository repository;

    /** Construye las dependencias; todavía no abre ni consulta SQLite. */
    @Override public void onCreate() {
        super.onCreate(); repository = new SqliteGameRepository(this);
    }

    /** Entrega el contrato de datos para inyectarlo en los presentadores. */
    public GameRepository repository() { return repository; }
}
