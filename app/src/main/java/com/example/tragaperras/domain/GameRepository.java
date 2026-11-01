package com.example.tragaperras.domain;

import java.util.List;
import io.reactivex.rxjava3.core.Single;

/** Contrato de datos: los presentadores no necesitan saber cómo se utiliza SQLite. */
public interface GameRepository {
    /** Lee el saldo y las tres mejores jugadas. */
    Single<HomeData> loadHome();
    /** Recupera el estado de la partida activa. */
    Single<PlayerSnapshot> loadPlayer();
    /** Crea una partida con saldo inicial y conserva el histórico anterior. */
    Single<PlayerSnapshot> startNewSession();
    /** Sortea y guarda una jugada y su saldo en una única operación atómica. */
    Single<SpinResult> playSpin();
    /** Lee todas las jugadas, ordenadas de la más reciente a la más antigua. */
    Single<List<SpinResult>> loadHistory();
}
