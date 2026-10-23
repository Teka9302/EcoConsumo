package com.upn.ecoconsumo;

public class Producto {
    private int idProducto;
    private String nombreProducto;
    private String categoria;
    private String cantidad;
    private String fechaCompra;
    private String fechaVencimiento;

    public Producto(int idProducto, String nombreProducto, String categoria, String cantidad, String fechaCompra, String fechaVencimiento) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.fechaCompra = fechaCompra;
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getCategoria() { return categoria; }
    public String getCantidad() { return cantidad; }
    public String getFechaCompra() { return fechaCompra; }
    public String getFechaVencimiento() { return fechaVencimiento; }
}
