package com.example.tragaperras.ui;

import android.content.Context;
import com.example.tragaperras.R;

/** Relaciona los identificadores numéricos del juego con sus dibujos y nombres. */
public final class SymbolArt {
    // El orden debe coincidir con los índices 0..4 que utiliza GameRules.
    public static final int[] DRAWABLES = {R.drawable.symbol_cherry, R.drawable.symbol_apple,
            R.drawable.symbol_bell, R.drawable.symbol_star, R.drawable.symbol_gem};

    /** Impide crear una instancia de esta utilidad gráfica. */
    private SymbolArt() { }

    /** Describe una combinación para el histórico y la accesibilidad. */
    public static String describe(Context context, int[] symbols) {
        String[] names = context.getResources().getStringArray(R.array.symbol_names);
        return names[symbols[0]] + " · " + names[symbols[1]] + " · " + names[symbols[2]];
    }
}
