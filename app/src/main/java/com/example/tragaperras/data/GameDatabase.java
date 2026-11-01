package com.example.tragaperras.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Define el esquema de SQLite. No contiene cálculos de premios ni lógica visual. */
final class GameDatabase extends SQLiteOpenHelper {
    /** Prepara el ayudante; la base se abre después, en el hilo de datos. */
    GameDatabase(Context context) { super(context, "tragaperras.db", null, 1); }

    /** Activa las relaciones entre partidas, jugador y jugadas. */
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

    /** Crea las tablas y el jugador inicial, que aún no tiene partida activa. */
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "started_at INTEGER NOT NULL, closed_at INTEGER, initial_coins INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE player (id INTEGER PRIMARY KEY CHECK(id=1), "
                + "coins INTEGER NOT NULL CHECK(coins>=0), session_id INTEGER REFERENCES sessions(id))");
        db.execSQL("CREATE TABLE spins (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "session_id INTEGER NOT NULL REFERENCES sessions(id), played_at INTEGER NOT NULL, "
                + "s0 INTEGER NOT NULL, s1 INTEGER NOT NULL, s2 INTEGER NOT NULL, "
                + "cost INTEGER NOT NULL, prize INTEGER NOT NULL, balance_before INTEGER NOT NULL, "
                + "balance_after INTEGER NOT NULL CHECK(balance_after>=0))");
        db.execSQL("CREATE INDEX spins_session ON spins(session_id, id)");
        db.execSQL("INSERT INTO player(id,coins,session_id) VALUES(1,0,NULL)");
    }

    /** Evita borrar datos silenciosamente: una futura versión necesitará una migración. */
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Falta definir la migración de la base de datos");
    }
}
