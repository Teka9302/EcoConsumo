package com.upn.ecoconsumo;

public class Producto {

    // Días de anticipación para considerar un producto "por caducar"
    public static final int DIAS_POR_CADUCAR = 3;

    public static final int ESTADO_VIGENTE = 0;
    public static final int ESTADO_POR_CADUCAR = 1;
    public static final int ESTADO_VENCIDO = 2;

    private int idProducto;
    private String nombreProducto;
    private String categoria;
    private int cantidad;
    private int stockMinimo;
    private String fechaCompra;
    private String fechaVencimiento;
    private int idUsuario;

    public Producto(int idProducto, String nombreProducto, String categoria, int cantidad, int stockMinimo,
                    String fechaCompra, String fechaVencimiento, int idUsuario) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
        this.fechaCompra = fechaCompra;
        this.fechaVencimiento = fechaVencimiento;
        this.idUsuario = idUsuario;
    }

    public int getIdProducto() { return idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public String getCategoria() { return categoria; }
    public int getCantidad() { return cantidad; }
    public int getStockMinimo() { return stockMinimo; }
    public String getFechaCompra() { return fechaCompra; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public int getIdUsuario() { return idUsuario; }

    public long getDiasParaVencer() {
        return FechaUtils.diasHasta(fechaVencimiento);
    }

    public int getEstado() {
        long dias = getDiasParaVencer();
        if (dias < 0) return ESTADO_VENCIDO;
        if (dias <= DIAS_POR_CADUCAR) return ESTADO_POR_CADUCAR;
        return ESTADO_VIGENTE;
    }

    public boolean isStockBajo() {
        return cantidad <= stockMinimo;
    }
}
