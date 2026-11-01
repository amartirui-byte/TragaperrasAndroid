package com.example.tragaperras.domain;

/** Copia del estado persistido del jugador, sin referencias a Android. */
public final class PlayerSnapshot {
    public final int coins;
    public final long sessionId;
    public final SpinResult lastSpin;

    /** Agrupa el saldo, la partida activa y su última jugada. */
    public PlayerSnapshot(int coins, long sessionId, SpinResult lastSpin) {
        this.coins = coins; this.sessionId = sessionId; this.lastSpin = lastSpin;
    }

    /** Indica si el usuario ya ha empezado una partida. */
    public boolean hasSession() { return sessionId > 0; }
}
