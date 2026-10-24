package com.upn.ecoconsumo.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.function.Function;

public final class LiveDataUtil {

    private LiveDataUtil() {}

    /**
     * Cada vez que cambia {@code origen}, se deja de observar la consulta anterior y se observa
     * la nueva que devuelve {@code consulta} (por ejemplo, al cambiar un filtro).
     */
    public static <X, Y> LiveData<Y> switchMap(LiveData<X> origen, Function<X, LiveData<Y>> consulta) {
        MediatorLiveData<Y> resultado = new MediatorLiveData<>();
        resultado.addSource(origen, new Observer<X>() {
            private LiveData<Y> actual;

            @Override
            public void onChanged(X valor) {
                LiveData<Y> nueva = consulta.apply(valor);
                if (actual == nueva) return;
                if (actual != null) resultado.removeSource(actual);
                actual = nueva;
                if (actual != null) resultado.addSource(actual, resultado::setValue);
            }
        });
        return resultado;
    }
}
