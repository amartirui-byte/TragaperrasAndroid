package com.example.tragaperras.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Datos necesarios para dibujar la bienvenida: saldo y mejores resultados. */
public final class HomeData {
    public final PlayerSnapshot player;
    public final List<SpinResult> bestSpins;

    /** Protege la lista para que la pantalla no modifique los datos recibidos. */
    public HomeData(PlayerSnapshot player, List<SpinResult> bestSpins) {
        this.player = player;
        this.bestSpins = Collections.unmodifiableList(new ArrayList<>(bestSpins));
    }
}
