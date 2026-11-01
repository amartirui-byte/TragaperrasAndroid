package com.example.tragaperras.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.tragaperras.R;
import com.example.tragaperras.domain.SpinResult;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/** Reutiliza filas del histórico; solo transforma objetos en texto visible. */
public final class SpinAdapter extends BaseAdapter {
    private final Context context;
    private final List<SpinResult> results;

    /** Conserva la lista ya cargada por el presentador, sin consultar SQLite. */
    public SpinAdapter(Context context, List<SpinResult> results) { this.context = context; this.results = results; }
    /** Número de jugadas que mostrará la lista. */
    @Override public int getCount() { return results.size(); }
    /** Devuelve la jugada de la fila solicitada. */
    @Override public SpinResult getItem(int position) { return results.get(position); }
    /** Identificador estable que proviene de SQLite. */
    @Override public long getItemId(int position) { return getItem(position).id; }

    /** Reutiliza una fila existente o infla una nueva y representa su contenido. */
    @Override public View getView(int position, View recycled, ViewGroup parent) {
        View row = recycled == null ? LayoutInflater.from(context).inflate(R.layout.row_spin, parent, false) : recycled;
        ((TextView) row.findViewById(R.id.row_summary)).setText(summary(context, getItem(position)));
        return row;
    }

    /** Formatea fecha, combinación, ganancia y saldo sin recalcular premios. */
    public static String summary(Context context, SpinResult spin) {
        String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(spin.playedAt));
        String net = spin.netChange() > 0 ? "+" + spin.netChange() : Integer.toString(spin.netChange());
        return context.getString(R.string.history_summary, date, SymbolArt.describe(context, spin.getSymbols()), net, spin.balanceAfter);
    }
}
