package com.upn.ecoconsumo;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String correo;

    public Usuario(int idUsuario, String nombre, String correo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
}
