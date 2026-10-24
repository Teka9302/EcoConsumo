package com.upn.ecoconsumo.ui.despensa;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.data.repository.CategoriaRepository;
import com.upn.ecoconsumo.data.repository.ProductoRepository;
import com.upn.ecoconsumo.util.Evento;
import com.upn.ecoconsumo.util.FechaUtils;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.List;

/** Registro y edición de productos: HU0001, HU0002, HU0005 y HU0011. */
public class ProductoFormViewModel extends AndroidViewModel {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final int idHogar;
    private final int idUsuario;
    private final LiveData<List<String>> categorias;

    private final MutableLiveData<Evento<Producto>> productoCargado = new MutableLiveData<>();
    private final MutableLiveData<Evento<Boolean>> guardado = new MutableLiveData<>();
    private final MutableLiveData<Evento<Integer>> mensaje = new MutableLiveData<>();

    // Estado del formulario que sobrevive a la rotación de pantalla
    private boolean inicializado;
    private Producto original;
    private String fechaCompra;
    private String fechaVencimiento;
    private String categoriaSeleccionada;

    public ProductoFormViewModel(@NonNull Application app) {
        super(app);
        productoRepo = new ProductoRepository(app);
        categoriaRepo = new CategoriaRepository(app);
        SessionManager session = new SessionManager(app);
        idHogar = session.getIdHogar();
        idUsuario = session.getIdUsuario();
        categorias = categoriaRepo.observarNombres(idHogar);
    }

    public LiveData<List<String>> getCategorias() { return categorias; }
    public LiveData<Evento<Producto>> getProductoCargado() { return productoCargado; }
    public LiveData<Evento<Boolean>> getGuardado() { return guardado; }
    public LiveData<Evento<Integer>> getMensaje() { return mensaje; }

    public String getFechaCompra() { return fechaCompra; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaCompra(String iso) { fechaCompra = iso; }
    public void setFechaVencimiento(String iso) { fechaVencimiento = iso; }
    public String getCategoriaSeleccionada() { return categoriaSeleccionada; }
    public void setCategoriaSeleccionada(String categoria) { categoriaSeleccionada = categoria; }

    /** Vencimiento guardado antes de editar; null si es un producto nuevo. */
    public String getVencimientoOriginal() {
        return original == null ? null : original.fechaVencimiento;
    }

    public void inicializar(int idProducto) {
        if (inicializado) return;
        inicializado = true;
        if (idProducto == -1) {
            fechaCompra = FechaUtils.hoy();
            return;
        }
        productoRepo.obtener(idProducto, p -> {
            if (p == null || p.idHogar != idHogar) {
                mensaje.setValue(new Evento<>(R.string.msg_producto_no_encontrado));
                guardado.setValue(new Evento<>(false));
                return;
            }
            original = p;
            fechaCompra = p.fechaCompra;
            fechaVencimiento = p.fechaVencimiento;
            categoriaSeleccionada = p.categoria;
            productoCargado.setValue(new Evento<>(p));
        });
    }

    /** Los datos ya fueron validados por ProductoValidator en la vista. */
    public void guardar(String nombre, String categoria, int cantidad, int stockMinimo) {
        Producto p = original != null ? original : new Producto();
        p.nombreProducto = nombre;
        p.categoria = categoria;
        p.cantidad = cantidad;
        p.stockMinimo = stockMinimo;
        p.fechaCompra = fechaCompra;
        p.fechaVencimiento = fechaVencimiento;
        p.idHogar = idHogar;
        if (original == null) p.idUsuarioRegistro = idUsuario;

        boolean esNuevo = original == null;
        productoRepo.guardar(p, idUsuario, ok -> {
            mensaje.setValue(new Evento<>(esNuevo ? R.string.msg_producto_guardado : R.string.msg_producto_actualizado));
            guardado.setValue(new Evento<>(true));
        });
    }

    public void agregarCategoria(String nombre) {
        // Se selecciona antes de insertar porque la lista puede actualizarse antes que el callback
        String anterior = categoriaSeleccionada;
        categoriaSeleccionada = nombre;
        categoriaRepo.agregar(idHogar, nombre, creada -> {
            if (!creada) {
                categoriaSeleccionada = anterior;
                mensaje.setValue(new Evento<>(R.string.err_categoria_existe));
            }
        });
    }
}
