package com.example.tragaperras.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.tragaperras.domain.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/** Implementa el contrato de datos con SQLite y ejecuta todas las llamadas mediante RxJava. */
public final class SqliteGameRepository implements GameRepository {
    private final GameDatabase helper;
    private final GameRules rules = new GameRules();
    private final Random random = new Random();
    // Un único hilo de datos serializa lecturas y escrituras sin bloquear la pantalla.
    private final Scheduler databaseScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    /** Recibe solo el contexto de aplicación para no retener ninguna pantalla. */
    public SqliteGameRepository(Context context) { helper = new GameDatabase(context.getApplicationContext()); }

    /** Envuelve una operación de datos y fija su ejecución fuera del hilo principal. */
    private <T> Single<T> inBackground(Callable<T> operation) {
        return Single.fromCallable(operation).subscribeOn(databaseScheduler);
    }

    /** Recupera el saldo y ordena las mejores jugadas por ganancia neta y fecha. */
    @Override public Single<HomeData> loadHome() {
        return inBackground(() -> {
            SQLiteDatabase db = helper.getReadableDatabase();
            return new HomeData(readPlayer(db), readSpins(db,
                    "SELECT * FROM spins ORDER BY (prize-cost) DESC, id DESC LIMIT 3", null));
        });
    }

    /** Abre SQLite en segundo plano y devuelve la partida activa. */
    @Override public Single<PlayerSnapshot> loadPlayer() {
        return inBackground(() -> readPlayer(helper.getReadableDatabase()));
    }

    /** Reinicia el saldo en una nueva sesión, sin borrar las jugadas anteriores. */
    @Override public Single<PlayerSnapshot> startNewSession() {
        return inBackground(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                PlayerSnapshot old = readPlayer(db);
                long now = System.currentTimeMillis();
                if (old.hasSession()) {
                    ContentValues ended = new ContentValues(); ended.put("closed_at", now);
                    db.update("sessions", ended, "id=?", new String[]{Long.toString(old.sessionId)});
                }
                ContentValues session = new ContentValues();
                session.put("started_at", now); session.put("initial_coins", GameRules.INITIAL_COINS);
                long sessionId = db.insertOrThrow("sessions", null, session);
                updatePlayer(db, GameRules.INITIAL_COINS, sessionId);
                db.setTransactionSuccessful();
                return new PlayerSnapshot(GameRules.INITIAL_COINS, sessionId, null);
            } finally { db.endTransaction(); }
        });
    }

    /** Guarda resultado y saldo juntos antes de la animación para resistir un cierre de la app. */
    @Override public Single<SpinResult> playSpin() {
        return inBackground(() -> {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                PlayerSnapshot player = readPlayer(db);
                if (!player.hasSession()) throw new IllegalStateException("No hay partida activa");
                int[] symbols = rules.drawSymbols(random);
                int prize = rules.calculatePrize(symbols);
                int after = rules.calculateFinalBalance(player.coins, symbols);
                long now = System.currentTimeMillis();
                ContentValues values = new ContentValues();
                values.put("session_id", player.sessionId); values.put("played_at", now);
                values.put("s0", symbols[0]); values.put("s1", symbols[1]); values.put("s2", symbols[2]);
                values.put("cost", GameRules.SPIN_COST); values.put("prize", prize);
                values.put("balance_before", player.coins); values.put("balance_after", after);
                long id = db.insertOrThrow("spins", null, values);
                updatePlayer(db, after, player.sessionId);
                db.setTransactionSuccessful();
                return new SpinResult(id, player.sessionId, now, symbols, GameRules.SPIN_COST,
                        prize, player.coins, after);
            } finally { db.endTransaction(); }
        });
    }

    /** Devuelve el histórico completo sin ejecutar consultas desde una Activity. */
    @Override public Single<List<SpinResult>> loadHistory() {
        return inBackground(() -> readSpins(helper.getReadableDatabase(),
                "SELECT * FROM spins ORDER BY id DESC", null));
    }

    /** Actualiza el único jugador; se llama dentro de la transacción de su operación. */
    private void updatePlayer(SQLiteDatabase db, int coins, long sessionId) {
        ContentValues values = new ContentValues();
        values.put("coins", coins); values.put("session_id", sessionId);
        if (db.update("player", values, "id=1", null) != 1) {
            throw new IllegalStateException("No se encontró el jugador");
        }
    }

    /** Traduce el registro del jugador y recupera su última jugada, si existe. */
    private PlayerSnapshot readPlayer(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT coins, session_id FROM player WHERE id=1", null)) {
            if (!cursor.moveToFirst()) throw new IllegalStateException("Falta el jugador");
            int coins = cursor.getInt(0);
            long sessionId = cursor.isNull(1) ? 0 : cursor.getLong(1);
            List<SpinResult> last = readSpins(db,
                    "SELECT * FROM spins WHERE session_id=? ORDER BY id DESC LIMIT 1",
                    new String[]{Long.toString(sessionId)});
            return new PlayerSnapshot(coins, sessionId, last.isEmpty() ? null : last.get(0));
        }
    }

    /** Ejecuta una consulta de jugadas y cierra siempre su cursor. */
    private List<SpinResult> readSpins(SQLiteDatabase db, String sql, String[] arguments) {
        List<SpinResult> results = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(sql, arguments)) {
            while (cursor.moveToNext()) results.add(readSpin(cursor));
        }
        return results;
    }

    /** Convierte las columnas SQL en un objeto del dominio sin incluir código visual. */
    private SpinResult readSpin(Cursor cursor) {
        return new SpinResult(cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("session_id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("played_at")),
                new int[]{cursor.getInt(cursor.getColumnIndexOrThrow("s0")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("s1")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("s2"))},
                cursor.getInt(cursor.getColumnIndexOrThrow("cost")),
                cursor.getInt(cursor.getColumnIndexOrThrow("prize")),
                cursor.getInt(cursor.getColumnIndexOrThrow("balance_before")),
                cursor.getInt(cursor.getColumnIndexOrThrow("balance_after")));
    }
}
