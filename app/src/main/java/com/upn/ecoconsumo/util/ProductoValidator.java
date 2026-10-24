package com.upn.ecoconsumo.util;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/** Reglas de validación del formulario de producto (HU0001, HU0002). Sin dependencias de Android. */
public final class ProductoValidator {

    public enum Campo { NOMBRE, CANTIDAD, STOCK_MINIMO, FECHA_COMPRA, FECHA_VENCIMIENTO }

    public enum ErrorValidacion {
        OBLIGATORIO, NUMERO_INVALIDO, COMPRA_FUTURA, VENCE_ANTES_DE_COMPRA, VENCIMIENTO_PASADO
    }

    private ProductoValidator() {}

    /**
     * @param vencimientoOriginal fecha de vencimiento guardada (edición) o null (producto nuevo).
     *                            Una fecha ya vencida solo se acepta si no se modificó.
     * @return errores por campo; vacío si todo es válido
     */
    public static Map<Campo, ErrorValidacion> validar(String nombre, String cantidad, String stockMinimo,
                                                      String fechaCompra, String fechaVencimiento,
                                                      String vencimientoOriginal, LocalDate hoy) {
        Map<Campo, ErrorValidacion> errores = new EnumMap<>(Campo.class);

        if (nombre == null || nombre.trim().isEmpty()) {
            errores.put(Campo.NOMBRE, ErrorValidacion.OBLIGATORIO);
        }
        if (parseCantidad(cantidad) == null) {
            errores.put(Campo.CANTIDAD, ErrorValidacion.NUMERO_INVALIDO);
        }
        if (parseCantidad(stockMinimo) == null) {
            errores.put(Campo.STOCK_MINIMO, ErrorValidacion.NUMERO_INVALIDO);
        }

        LocalDate compra = FechaUtils.parse(fechaCompra);
        LocalDate vencimiento = FechaUtils.parse(fechaVencimiento);
        if (compra == null) {
            errores.put(Campo.FECHA_COMPRA, ErrorValidacion.OBLIGATORIO);
        } else if (compra.isAfter(hoy)) {
            errores.put(Campo.FECHA_COMPRA, ErrorValidacion.COMPRA_FUTURA);
        }

        boolean vencimientoCambiado = vencimientoOriginal == null || !vencimientoOriginal.equals(fechaVencimiento);
        if (vencimiento == null) {
            errores.put(Campo.FECHA_VENCIMIENTO, ErrorValidacion.OBLIGATORIO);
        } else if (compra != null && vencimiento.isBefore(compra)) {
            errores.put(Campo.FECHA_VENCIMIENTO, ErrorValidacion.VENCE_ANTES_DE_COMPRA);
        } else if (vencimientoCambiado && vencimiento.isBefore(hoy)) {
            errores.put(Campo.FECHA_VENCIMIENTO, ErrorValidacion.VENCIMIENTO_PASADO);
        }
        return errores;
    }

    /** Entero entre 0 y 99999, o null si el texto no es válido. */
    public static Integer parseCantidad(String texto) {
        if (texto == null) return null;
        try {
            int valor = Integer.parseInt(texto.trim());
            return valor >= 0 && valor <= 99999 ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
