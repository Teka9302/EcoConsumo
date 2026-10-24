package com.upn.ecoconsumo.util;

/**
 * Envuelve un valor de LiveData que debe procesarse una sola vez (un mensaje, una navegación),
 * para que no se repita al rotar la pantalla.
 */
public class Evento<T> {

    private final T contenido;
    private boolean manejado;

    public Evento(T contenido) {
        this.contenido = contenido;
    }

    /** Devuelve el contenido la primera vez y null en las siguientes. */
    public T consumir() {
        if (manejado) return null;
        manejado = true;
        return contenido;
    }
}
