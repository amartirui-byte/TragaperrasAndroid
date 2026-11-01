package com.example.tragaperras.presentation;

import com.example.tragaperras.domain.*;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import org.junit.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

/** Verifica la coordinación del giro con operaciones de datos que pueden tardar o fallar. */
public final class GamePresenterTest {
    private final FakeRepository repository = new FakeRepository();
    private final RecordingView view = new RecordingView();
    private final GamePresenter presenter = new GamePresenter(repository, Schedulers.trampoline());

    /** Dos pulsaciones durante el guardado producen una única jugada y una única animación. */
    @Test public void repeatedSpinIsBlockedUntilAnimationFinishes() {
        presenter.attach(view);
        presenter.spin();
        presenter.spin();
        assertEquals(1, repository.spinCalls);
        assertEquals(0, view.starts);
        assertFalse(view.canSpin);
        assertFalse(view.canStop);
        assertFalse(presenter.canLeave());
        repository.pending.onSuccess(result());
        assertEquals(1, view.starts);
        assertTrue(view.canStop);
        presenter.spin();
        assertEquals(1, repository.spinCalls);
    }

    /** Parar entrega el resultado guardado; solo la llegada de los tres rodillos cierra el giro. */
    @Test public void stoppingDoesNotRedrawTheResultOrChargeAgain() {
        presenter.attach(view);
        presenter.spin();
        repository.pending.onSuccess(result());
        presenter.animationFinished(); // Una notificación prematura no debe cambiar el estado.
        assertEquals(0, view.results);
        presenter.stop();
        presenter.stop();
        assertEquals(1, view.stops);
        assertArrayEquals(new int[]{0, 0, 2}, view.stopSymbols);
        assertFalse(presenter.canLeave());
        presenter.animationFinished();
        assertEquals(1, view.results);
        assertEquals(1, repository.spinCalls);
        assertTrue(view.canSpin);
        assertTrue(presenter.canLeave());
        assertFalse(view.canStop);
    }

    /** Un error de SQLite se comunica sin empezar una animación ni anunciar un resultado. */
    @Test public void failedWriteShowsAnErrorAndUnlocksNavigation() {
        presenter.attach(view);
        presenter.spin();
        repository.pending.onError(new IllegalStateException("Fallo de escritura simulado"));
        assertEquals(1, view.errors);
        assertEquals(0, view.starts);
        assertEquals(0, view.results);
        assertTrue(presenter.canLeave());
    }

    /** Al salir de la pantalla se libera su observador y no se envían actualizaciones tardías. */
    @Test public void detachedViewReceivesNoLateAnimation() {
        presenter.attach(view);
        presenter.spin();
        assertTrue(repository.pending.hasObservers());
        presenter.detach();
        assertFalse(repository.pending.hasObservers());
        repository.pending.onSuccess(result());
        assertEquals(0, view.starts);
    }

    /** El saldo agotado permite salir, pero impide pedir un giro al repositorio. */
    @Test public void emptyBalanceCannotStartASpin() {
        repository.player = new PlayerSnapshot(0, 1, null);
        presenter.attach(view);
        presenter.spin();
        assertFalse(view.canSpin);
        assertTrue(presenter.canLeave());
        assertEquals(0, repository.spinCalls);
    }

    /** Después de reconectar se muestra el estado persistido sin repetir la apuesta anterior. */
    @Test public void reattachedViewLoadsTheSavedResult() {
        presenter.attach(view);
        presenter.detach();
        repository.player = new PlayerSnapshot(100, 1, result());
        presenter.attach(view);
        assertNotNull(view.player.lastSpin);
        assertEquals(100, view.player.coins);
        assertEquals(0, repository.spinCalls);
        assertEquals(0, view.starts);
    }

    /** Crea una pareja conocida: cuesta diez y devuelve diez, por lo que el saldo se mantiene. */
    private static SpinResult result() {
        return new SpinResult(1, 1, 0, new int[]{0, 0, 2}, 10, 10, 100, 100);
    }

    /** Sustituye SQLite por respuestas controladas, incluidas operaciones pendientes. */
    private static final class FakeRepository implements GameRepository {
        PlayerSnapshot player = new PlayerSnapshot(100, 1, null);
        final SingleSubject<SpinResult> pending = SingleSubject.create();
        int spinCalls;
        /** Devuelve el estado preparado por la prueba. */
        @Override public Single<PlayerSnapshot> loadPlayer() { return Single.just(player); }
        /** Cuenta solicitudes y deja que la prueba decida cuándo termina el guardado. */
        @Override public Single<SpinResult> playSpin() { spinCalls++; return pending; }
        /** Satisface el contrato de bienvenida sin utilizar Android. */
        @Override public Single<HomeData> loadHome() { return Single.just(new HomeData(player, Collections.emptyList())); }
        /** Devuelve una sesión controlada si una prueba necesitase crearla. */
        @Override public Single<PlayerSnapshot> startNewSession() { return Single.just(player); }
        /** Satisface el contrato de histórico con una lista vacía. */
        @Override public Single<List<SpinResult>> loadHistory() { return Single.just(Collections.emptyList()); }
    }

    /** Registra llamadas visuales sin crear Activity, botones ni animadores Android. */
    private static final class RecordingView implements GamePresenter.View {
        boolean canSpin, canStop;
        int starts, stops, results, errors;
        int[] stopSymbols;
        PlayerSnapshot player;
        /** Conserva el estado de los controles que interesa comprobar. */
        @Override public void controls(boolean canSpin, boolean canStop, boolean canLeave) {
            this.canSpin = canSpin; this.canStop = canStop;
        }
        /** Conserva el estado recuperado para comprobar la reapertura. */
        @Override public void showPlayer(PlayerSnapshot player) { this.player = player; }
        /** Cuenta comienzos de animación. */
        @Override public void startReels(SpinResult result) { starts++; }
        /** Registra la combinación que deberían representar los rodillos. */
        @Override public void stopReels(int[] symbols) { stops++; stopSymbols = symbols; }
        /** Cuenta resultados anunciados. */
        @Override public void showResult(SpinResult result) { results++; }
        /** Cuenta fallos comunicados a la persona que juega. */
        @Override public void showError(Throwable error) { errors++; }
    }
}
