package com.upn.ecoconsumo.util;

import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.entity.Producto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Exporta el inventario y el historial a CSV (UTF-8 con BOM para que Excel muestre bien las tildes). */
public final class CsvExporter {

    private CsvExporter() {}

    public static void exportarInventario(OutputStream out, List<Producto> productos, int diasAviso) throws IOException {
        Writer w = abrir(out);
        fila(w, "Producto", "Categoría", "Cantidad", "Stock mínimo", "Fecha de compra",
                "Fecha de vencimiento", "Estado");
        for (Producto p : productos) {
            fila(w, p.nombreProducto, p.categoria, String.valueOf(p.cantidad), String.valueOf(p.stockMinimo),
                    p.fechaCompra, p.fechaVencimiento, textoEstado(p.estado(diasAviso)));
        }
        w.flush();
    }

    public static void exportarHistorial(OutputStream out, List<Movimiento> movimientos) throws IOException {
        Writer w = abrir(out);
        fila(w, "Fecha", "Tipo", "Producto", "Categoría", "Cantidad", "Motivo");
        for (Movimiento m : movimientos) {
            fila(w, m.fecha, m.tipo, m.nombreProducto, m.categoria, String.valueOf(m.cantidad),
                    m.motivo == null ? "" : m.motivo);
        }
        w.flush();
    }

    private static Writer abrir(OutputStream out) throws IOException {
        Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        w.write('﻿');
        return w;
    }

    private static void fila(Writer w, String... valores) throws IOException {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) w.write(',');
            w.write(escapar(valores[i]));
        }
        w.write("\r\n");
    }

    static String escapar(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    private static String textoEstado(int estado) {
        switch (estado) {
            case Producto.ESTADO_VENCIDO: return "Vencido";
            case Producto.ESTADO_POR_CADUCAR: return "Por caducar";
            default: return "Vigente";
        }
    }
}
