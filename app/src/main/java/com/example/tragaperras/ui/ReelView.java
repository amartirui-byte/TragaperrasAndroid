package com.example.tragaperras.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.example.tragaperras.R;

/** Dibuja un rodillo desplazando una tira de imágenes verticalmente. No calcula premios. */
public final class ReelView extends View {
    private final Drawable[] images = new Drawable[SymbolArt.DRAWABLES.length];
    private final Paint shade = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    // La posición está expresada en símbolos, no en píxeles: funciona con cualquier tamaño.
    private float position = 0f;
    private float cellHeight;
    private int iconSize;

    /** Carga los recursos una sola vez y prepara el recorte de los bordes redondeados. */
    public ReelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < images.length; i++) images[i] = context.getDrawable(SymbolArt.DRAWABLES[i]).mutate();
        setBackgroundResource(R.drawable.bg_reel);
        setClipToOutline(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setSymbol(0);
    }

    /** Recalcula tamaños y sombreado cuando cambia el espacio disponible. */
    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        cellHeight = height * 0.65f;
        iconSize = (int) Math.min(width * 0.68f, cellHeight * 0.75f);
        shade.setShader(new LinearGradient(0, 0, 0, height,
                new int[]{0x99E8D7B8, 0x00FFFFFF, 0x00FFFFFF, 0x99E8D7B8},
                new float[]{0f, 0.23f, 0.77f, 1f}, Shader.TileMode.CLAMP));
    }

    /** Dibuja el símbolo central y los vecinos que entran y salen del recuadro. */
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int base = (int) Math.floor(position);
        float fraction = position - base;
        for (int offset = -2; offset <= 2; offset++) {
            Drawable image = images[Math.floorMod(base + offset, images.length)];
            float center = getHeight() / 2f + (offset - fraction) * cellHeight;
            int left = (getWidth() - iconSize) / 2;
            int top = Math.round(center - iconSize / 2f);
            image.setBounds(left, top, left + iconSize, top + iconSize);
            image.draw(canvas);
        }
        canvas.drawRect(0, 0, getWidth(), getHeight(), shade);
    }

    /** Inicia el desplazamiento continuo; recicla los cinco dibujos sin cargar imágenes por fotograma. */
    public void startSpinning() {
        cancelAnimation();
        float initial = position % images.length;
        animator = ValueAnimator.ofFloat(initial, initial + images.length);
        animator.setDuration(560);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(value -> { position = (float) value.getAnimatedValue(); invalidate(); });
        setContentDescription(getContext().getString(R.string.reel_spinning));
        animator.start();
    }

    /** Frena progresivamente y alinea el símbolo pedido; el presentador ya ha elegido ese resultado. */
    public void stopAt(int symbol, long duration, Runnable onFinished) {
        cancelAnimation();
        float initial = position;
        int nextCycle = ((int) Math.floor(initial / images.length) + 2) * images.length;
        float target = nextCycle + symbol;
        animator = ValueAnimator.ofFloat(initial, target);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator(1.7f));
        animator.addUpdateListener(value -> { position = (float) value.getAnimatedValue(); invalidate(); });
        animator.addListener(new AnimatorListenerAdapter() {
            /** Notifica una única vez que este rodillo está centrado en el resultado. */
            @Override public void onAnimationEnd(Animator animation) {
                position = symbol; updateDescription(symbol); invalidate(); onFinished.run();
            }
        });
        animator.start();
    }

    /** Presenta directamente un símbolo guardado al abrir o recuperar la pantalla. */
    public void setSymbol(int symbol) {
        cancelAnimation(); position = symbol; updateDescription(symbol); invalidate();
    }

    /** Actualiza el nombre accesible únicamente en reposo, evitando anuncios durante cada fotograma. */
    private void updateDescription(int symbol) {
        setContentDescription(getResources().getStringArray(R.array.symbol_names)[symbol]);
    }

    /** Quita callbacks antes de cancelar para no confundir una cancelación con un giro completado. */
    public void cancelAnimation() {
        if (animator != null) {
            animator.removeAllListeners(); animator.removeAllUpdateListeners(); animator.cancel(); animator = null;
        }
    }

    /** Libera el animador cuando la vista deja de pertenecer a una ventana. */
    @Override protected void onDetachedFromWindow() { cancelAnimation(); super.onDetachedFromWindow(); }
}
