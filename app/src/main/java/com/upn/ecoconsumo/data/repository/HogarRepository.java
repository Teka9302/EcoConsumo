package com.upn.ecoconsumo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.AppDatabase;
import com.upn.ecoconsumo.data.Tareas;
import com.upn.ecoconsumo.data.dao.HogarDao;
import com.upn.ecoconsumo.data.entity.Hogar;
import com.upn.ecoconsumo.data.entity.MiembroHogar;
import com.upn.ecoconsumo.data.entity.Usuario;
import com.upn.ecoconsumo.data.model.MiembroDetalle;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

/** Despensa compartida: hogares, miembros, roles y códigos de invitación. */
public class HogarRepository {

    public enum EstadoUnion { OK, CODIGO_INVALIDO, MISMO_HOGAR }

    public static class ResultadoUnion {
        public final EstadoUnion estado;
        public final int idHogar;

        ResultadoUnion(EstadoUnion estado, int idHogar) {
            this.estado = estado;
            this.idHogar = idHogar;
        }
    }

    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_CODIGO = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppDatabase db;
    private final HogarDao hogarDao;

    public HogarRepository(Context context) {
        db = AppDatabase.getInstance(context);
        hogarDao = db.hogarDao();
    }

    public LiveData<Hogar> observarHogar(int idHogar) {
        return hogarDao.observar(idHogar);
    }

    public LiveData<MiembroHogar> observarMembresia(int idUsuario) {
        return hogarDao.observarMembresia(idUsuario);
    }

    public LiveData<List<MiembroDetalle>> observarMiembros(int idHogar) {
        return hogarDao.observarMiembros(idHogar);
    }

    public void renombrar(int idHogar, String nombre) {
        Tareas.ejecutar(() -> hogarDao.renombrar(idHogar, nombre));
    }

    public void regenerarCodigo(int idHogar) {
        Tareas.ejecutar(() -> hogarDao.cambiarCodigo(idHogar, generarCodigoUnico(hogarDao)));
    }

    /**
     * Une al usuario al hogar del código. Si era el único miembro de su hogar anterior,
     * sus productos, categorías, lista de compras e historial se trasladan al nuevo hogar.
     */
    public void unirse(int idUsuario, String codigo, Tareas.Callback<ResultadoUnion> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> {
            Hogar destino = hogarDao.buscarPorCodigo(codigo.trim().toUpperCase(Locale.ROOT));
            if (destino == null) return new ResultadoUnion(EstadoUnion.CODIGO_INVALIDO, 0);

            MiembroHogar actual = hogarDao.obtenerMembresia(idUsuario);
            if (actual != null && actual.idHogar == destino.idHogar) {
                return new ResultadoUnion(EstadoUnion.MISMO_HOGAR, destino.idHogar);
            }
            if (actual != null) {
                if (hogarDao.contarMiembros(actual.idHogar) == 1) {
                    hogarDao.moverProductos(actual.idHogar, destino.idHogar);
                    hogarDao.moverCategorias(actual.idHogar, destino.idHogar);
                    hogarDao.moverListaCompra(actual.idHogar, destino.idHogar);
                    hogarDao.moverMovimientos(actual.idHogar, destino.idHogar);
                    hogarDao.eliminar(actual.idHogar);
                } else if (actual.esAdmin()) {
                    promoverSucesor(actual.idHogar, idUsuario);
                }
            }
            hogarDao.guardarMiembro(nuevaMembresia(idUsuario, destino.idHogar, MiembroHogar.ROL_MIEMBRO));
            return new ResultadoUnion(EstadoUnion.OK, destino.idHogar);
        }), callback);
    }

    /**
     * El usuario deja el hogar compartido y pasa a uno propio y vacío.
     * Devuelve el id del nuevo hogar, o null si era el único miembro.
     */
    public void salir(int idUsuario, Tareas.Callback<Integer> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> separarDelHogar(idUsuario)), callback);
    }

    /** El administrador quita a un miembro, que pasa a tener su propio hogar. */
    public void expulsar(int idUsuario, Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> separarDelHogar(idUsuario) != null), callback);
    }

    private Integer separarDelHogar(int idUsuario) {
        MiembroHogar actual = hogarDao.obtenerMembresia(idUsuario);
        Usuario usuario = db.usuarioDao().obtener(idUsuario);
        if (actual == null || usuario == null || hogarDao.contarMiembros(actual.idHogar) <= 1) return null;
        if (actual.esAdmin()) promoverSucesor(actual.idHogar, idUsuario);
        return crearHogarPersonal(db, idUsuario, usuario.nombre);
    }

    private void promoverSucesor(int idHogar, int idUsuarioSaliente) {
        MiembroHogar sucesor = hogarDao.miembroMasAntiguo(idHogar, idUsuarioSaliente);
        if (sucesor != null) hogarDao.cambiarRol(sucesor.idUsuario, MiembroHogar.ROL_ADMIN);
    }

    /** Crea un hogar con el usuario como administrador. Debe llamarse dentro de una transacción. */
    static int crearHogarPersonal(AppDatabase db, int idUsuario, String nombreUsuario) {
        HogarDao dao = db.hogarDao();
        Hogar hogar = new Hogar();
        hogar.nombre = "Hogar de " + primerNombre(nombreUsuario);
        hogar.codigoInvitacion = generarCodigoUnico(dao);
        int idHogar = (int) dao.insertar(hogar);
        dao.guardarMiembro(nuevaMembresia(idUsuario, idHogar, MiembroHogar.ROL_ADMIN));
        return idHogar;
    }

    private static MiembroHogar nuevaMembresia(int idUsuario, int idHogar, String rol) {
        MiembroHogar miembro = new MiembroHogar();
        miembro.idUsuario = idUsuario;
        miembro.idHogar = idHogar;
        miembro.rol = rol;
        miembro.fechaUnion = System.currentTimeMillis();
        return miembro;
    }

    private static String generarCodigoUnico(HogarDao dao) {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
            for (int i = 0; i < LONGITUD_CODIGO; i++) {
                sb.append(CARACTERES_CODIGO.charAt(RANDOM.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (dao.buscarPorCodigo(codigo) != null);
        return codigo;
    }

    private static String primerNombre(String nombre) {
        String limpio = nombre.trim();
        int espacio = limpio.indexOf(' ');
        return espacio > 0 ? limpio.substring(0, espacio) : limpio;
    }
}
