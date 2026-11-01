package com.example.tragaperras.domain;

/** Resultado inmutable de una jugada, independiente de cómo se dibuja. */
public final class SpinResult {
    public final long id, sessionId, playedAt;
    public final int cost, prize, balanceBefore, balanceAfter;
    private final int[] symbols;

    /** Copia el resultado para evitar que la interfaz cambie los símbolos guardados. */
    public SpinResult(long id, long sessionId, long playedAt, int[] symbols, int cost,
                      int prize, int balanceBefore, int balanceAfter) {
        this.id = id; this.sessionId = sessionId; this.playedAt = playedAt;
        this.symbols = symbols.clone(); this.cost = cost; this.prize = prize;
        this.balanceBefore = balanceBefore; this.balanceAfter = balanceAfter;
    }

    /** Entrega una copia de los símbolos; el resultado original permanece inmutable. */
    public int[] getSymbols() { return symbols.clone(); }

    /** Calcula la ganancia o pérdida neta, incluyendo el coste del giro. */
    public int netChange() { return prize - cost; }

    /** Saldo que se muestra mientras giran las ruedas, antes de revelar el premio. */
    public int balanceAfterCost() { return balanceBefore - cost; }
}
