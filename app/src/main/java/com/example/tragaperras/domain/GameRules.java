package com.example.tragaperras.domain;

import java.util.Random;

/** Reglas puras de Java. No conoce pantallas, animaciones, Android ni SQLite. */
public final class GameRules {
    public static final int INITIAL_COINS = 100;
    public static final int SPIN_COST = 10;
    public static final int SYMBOL_COUNT = 5;
    public static final int THREE_EQUAL_PRIZE = 100;
    public static final int TWO_EQUAL_PRIZE = 10;

    /** Sortea tres símbolos independientes; todos tienen la misma probabilidad. */
    public int[] drawSymbols(Random random) {
        return new int[]{random.nextInt(SYMBOL_COUNT), random.nextInt(SYMBOL_COUNT),
                random.nextInt(SYMBOL_COUNT)};
    }

    /** Devuelve el premio bruto. Un trío no cobra también el premio de pareja. */
    public int calculatePrize(int[] symbols) {
        validateSymbols(symbols);
        if (symbols[0] == symbols[1] && symbols[1] == symbols[2]) return THREE_EQUAL_PRIZE;
        if (symbols[0] == symbols[1] || symbols[0] == symbols[2] || symbols[1] == symbols[2]) {
            return TWO_EQUAL_PRIZE;
        }
        return 0;
    }

    /** Descuenta el coste una vez y añade el premio; rechaza un saldo insuficiente. */
    public int calculateFinalBalance(int balanceBefore, int[] symbols) {
        if (balanceBefore < SPIN_COST) throw new IllegalStateException("Saldo insuficiente");
        return Math.addExact(balanceBefore - SPIN_COST, calculatePrize(symbols));
    }

    /** Comprueba que el resultado tenga exactamente tres símbolos válidos. */
    private void validateSymbols(int[] symbols) {
        if (symbols == null || symbols.length != 3) {
            throw new IllegalArgumentException("El resultado debe contener tres símbolos");
        }
        for (int symbol : symbols) {
            if (symbol < 0 || symbol >= SYMBOL_COUNT) {
                throw new IllegalArgumentException("Símbolo fuera del intervalo permitido");
            }
        }
    }
}
