package com.upn.ecoconsumo.data.model;

/** Datos del usuario autenticado que se guardan en la sesión. */
public class SesionUsuario {
    public final int idUsuario;
    public final String nombre;
    public final String correo;
    public final int idHogar;

    public SesionUsuario(int idUsuario, String nombre, String correo, int idHogar) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.idHogar = idHogar;
    }
}
