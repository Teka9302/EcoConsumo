package com.upn.ecoconsumo;

public class ItemCompra {
    private int idItem;
    private String nombreProducto;
    private String categoria;
    private int cantidad;
    private boolean comprado;
    private Integer idProducto;

    public ItemCompra(int idItem, String nombreProducto, String categoria, int cantidad,
                      boolean comprado, Integer idProducto) {
        this.idItem = idItem;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.comprado = comprado;
        this.idProducto = idProducto;
    }

    public int getIdItem() { return idItem; }
    public String getNombreProducto() { return nombreProducto; }
    public String getCategoria() { return categoria; }
    public int getCantidad() { return cantidad; }
    public boolean isComprado() { return comprado; }
    public void setComprado(boolean comprado) { this.comprado = comprado; }
    public Integer getIdProducto() { return idProducto; }
}
