package com.upn.ecoconsumo.ui.despensa;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.dao.ProductoDao;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.data.repository.CategoriaRepository;
import com.upn.ecoconsumo.data.repository.ProductoRepository;
import com.upn.ecoconsumo.util.LiveDataUtil;
import com.upn.ecoconsumo.util.Preferencias;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.List;
import java.util.Objects;

/** Estado de la despensa: búsqueda (HU0010), categoría (HU0004) y estado de caducidad (HU0009). */
public class DespensaViewModel extends AndroidViewModel {

    /** Filtro inmutable: cada cambio emite uno nuevo y relanza la consulta. */
    public static final class Filtro {
        public final String texto;
        public final String categoria;
        public final int estado;
        public final int diasAviso;

        Filtro(String texto, String categoria, int estado, int diasAviso) {
            this.texto = texto;
            this.categoria = categoria;
            this.estado = estado;
            this.diasAviso = diasAviso;
        }

        public boolean esVacio() {
            return texto.isEmpty() && categoria == null && estado == ProductoDao.FILTRO_TODOS;
        }
    }

    private final int idHogar;
    private final MutableLiveData<Filtro> filtro;
    private final LiveData<List<Producto>> productos;
    private final LiveData<List<String>> categorias;

    public DespensaViewModel(@NonNull Application app) {
        super(app);
        ProductoRepository productoRepo = new ProductoRepository(app);
        idHogar = new SessionManager(app).getIdHogar();
        filtro = new MutableLiveData<>(new Filtro("", null, ProductoDao.FILTRO_TODOS, Preferencias.diasAviso(app)));
        productos = LiveDataUtil.switchMap(filtro, f ->
                productoRepo.observarFiltrados(idHogar, f.texto, f.categoria, f.estado, f.diasAviso));
        categorias = new CategoriaRepository(app).observarNombres(idHogar);
    }

    public LiveData<List<Producto>> getProductos() { return productos; }
    public LiveData<List<String>> getCategorias() { return categorias; }

    public Filtro getFiltro() {
        return filtro.getValue();
    }

    public void setTexto(String texto) {
        Filtro f = getFiltro();
        String limpio = texto == null ? "" : texto.trim();
        if (!limpio.equals(f.texto)) filtro.setValue(new Filtro(limpio, f.categoria, f.estado, f.diasAviso));
    }

    public void setCategoria(String categoria) {
        Filtro f = getFiltro();
        if (!Objects.equals(categoria, f.categoria)) {
            filtro.setValue(new Filtro(f.texto, categoria, f.estado, f.diasAviso));
        }
    }

    public void setEstado(int estado) {
        Filtro f = getFiltro();
        if (estado != f.estado) filtro.setValue(new Filtro(f.texto, f.categoria, estado, f.diasAviso));
    }

    /** Se llama al volver a la pantalla por si cambiaron los días de aviso en Ajustes. */
    public void refrescarDiasAviso() {
        Filtro f = getFiltro();
        int dias = Preferencias.diasAviso(getApplication());
        if (dias != f.diasAviso) filtro.setValue(new Filtro(f.texto, f.categoria, f.estado, dias));
    }
}
