package com.example.tragaperras.domain;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

/** Comprueba combinaciones y límites sin un teléfono, un emulador ni SQLite. */
public final class GameRulesTest {
    private final GameRules rules = new GameRules();

    /** Recorre las 125 combinaciones: cinco tríos, sesenta parejas y sesenta pérdidas. */
    @Test public void allCombinationsHaveTheExpectedDistribution() {
        int triples = 0, pairs = 0, losses = 0;
        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 5; b++) {
                for (int c = 0; c < 5; c++) {
                    int prize = rules.calculatePrize(new int[]{a, b, c});
                    if (prize == 100) triples++;
                    else if (prize == 10) pairs++;
                    else { assertEquals(0, prize); losses++; }
                }
            }
        }
        assertEquals(5, triples);
        assertEquals(60, pairs);
        assertEquals(60, losses);
    }

    /** Detecta la pareja en cualquiera de sus tres posiciones. */
    @Test public void pairsPayTheSameRegardlessOfPosition() {
        assertEquals(10, rules.calculatePrize(new int[]{2, 2, 4}));
        assertEquals(10, rules.calculatePrize(new int[]{2, 4, 2}));
        assertEquals(10, rules.calculatePrize(new int[]{4, 2, 2}));
    }

    /** El coste se resta una sola vez y el trío no acumula el premio de pareja. */
    @Test public void finalBalanceIncludesCostAndGrossPrize() {
        assertEquals(190, rules.calculateFinalBalance(100, new int[]{1, 1, 1}));
        assertEquals(100, rules.calculateFinalBalance(100, new int[]{1, 1, 2}));
        assertEquals(90, rules.calculateFinalBalance(100, new int[]{0, 1, 2}));
        assertEquals(0, rules.calculateFinalBalance(10, new int[]{0, 1, 2}));
    }

    /** Impide jugar con un saldo inferior al precio del giro. */
    @Test public void insufficientBalanceIsRejected() {
        assertThrows(IllegalStateException.class,
                () -> rules.calculateFinalBalance(9, new int[]{1, 1, 1}));
        assertThrows(IllegalStateException.class,
                () -> rules.calculateFinalBalance(-1, new int[]{0, 1, 2}));
    }

    /** Rechaza resultados incompletos o con símbolos que no existen. */
    @Test public void malformedResultsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> rules.calculatePrize(null));
        assertThrows(IllegalArgumentException.class, () -> rules.calculatePrize(new int[]{0, 1}));
        assertThrows(IllegalArgumentException.class, () -> rules.calculatePrize(new int[]{0, 1, 5}));
        assertThrows(IllegalArgumentException.class, () -> rules.calculatePrize(new int[]{-1, 0, 1}));
    }

    /** Evita que un desbordamiento de enteros termine guardando un saldo negativo. */
    @Test public void balanceOverflowIsRejected() {
        assertThrows(ArithmeticException.class,
                () -> rules.calculateFinalBalance(Integer.MAX_VALUE, new int[]{0, 0, 0}));
    }

    /** Una fuente controlada permite comprobar que se realizan tres sorteos independientes. */
    @Test public void drawRequestsOneBoundedNumberPerReel() {
        Random controlled = new Random() {
            private int call;
            /** Devuelve valores conocidos y verifica el límite pedido por las reglas. */
            @Override public int nextInt(int bound) {
                assertEquals(5, bound);
                return new int[]{4, 0, 2}[call++];
            }
        };
        assertArrayEquals(new int[]{4, 0, 2}, rules.drawSymbols(controlled));
    }

    /** La interfaz no puede modificar el resultado a través del array recibido. */
    @Test public void spinResultProtectsItsSymbols() {
        int[] original = {0, 1, 2};
        SpinResult result = new SpinResult(1, 1, 0, original, 10, 0, 100, 90);
        original[0] = 4;
        result.getSymbols()[1] = 4;
        assertArrayEquals(new int[]{0, 1, 2}, result.getSymbols());
        assertEquals(-10, result.netChange());
        assertEquals(90, result.balanceAfterCost());
    }
}
