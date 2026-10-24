package com.upn.ecoconsumo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.upn.ecoconsumo.util.ProductoValidator.Campo;
import com.upn.ecoconsumo.util.ProductoValidator.ErrorValidacion;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Map;

public class ProductoValidatorTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 24);

    private static Map<Campo, ErrorValidacion> validar(String compra, String vence, String venceOriginal) {
        return ProductoValidator.validar("Leche", "2", "1", compra, vence, venceOriginal, HOY);
    }

    @Test
    public void productoValido_sinErrores() {
        assertTrue(validar("2026-10-20", "2026-11-01", null).isEmpty());
    }

    @Test
    public void camposVacios_sonObligatorios() {
        Map<Campo, ErrorValidacion> errores =
                ProductoValidator.validar(" ", "", "abc", null, null, null, HOY);
        assertEquals(ErrorValidacion.OBLIGATORIO, errores.get(Campo.NOMBRE));
        assertEquals(ErrorValidacion.NUMERO_INVALIDO, errores.get(Campo.CANTIDAD));
        assertEquals(ErrorValidacion.NUMERO_INVALIDO, errores.get(Campo.STOCK_MINIMO));
        assertEquals(ErrorValidacion.OBLIGATORIO, errores.get(Campo.FECHA_COMPRA));
        assertEquals(ErrorValidacion.OBLIGATORIO, errores.get(Campo.FECHA_VENCIMIENTO));
    }

    @Test
    public void compraFutura_esError() {
        assertEquals(ErrorValidacion.COMPRA_FUTURA,
                validar("2026-10-25", "2026-11-01", null).get(Campo.FECHA_COMPRA));
    }

    @Test
    public void vencimientoAntesDeCompra_esError() {
        assertEquals(ErrorValidacion.VENCE_ANTES_DE_COMPRA,
                validar("2026-10-20", "2026-10-19", null).get(Campo.FECHA_VENCIMIENTO));
    }

    @Test
    public void vencimientoPasadoEnProductoNuevo_esError() {
        assertEquals(ErrorValidacion.VENCIMIENTO_PASADO,
                validar("2026-10-01", "2026-10-23", null).get(Campo.FECHA_VENCIMIENTO));
    }

    @Test
    public void venceHoy_esValido() {
        assertNull(validar("2026-10-20", "2026-10-24", null).get(Campo.FECHA_VENCIMIENTO));
    }

    @Test
    public void edicionSinCambiarVencimientoPasado_esValida() {
        // Un producto ya vencido se puede editar (p. ej. la cantidad) sin cambiar su fecha
        assertNull(validar("2026-10-01", "2026-10-10", "2026-10-10").get(Campo.FECHA_VENCIMIENTO));
    }

    @Test
    public void edicionCambiandoAFechaPasada_esError() {
        assertEquals(ErrorValidacion.VENCIMIENTO_PASADO,
                validar("2026-10-01", "2026-10-12", "2026-10-10").get(Campo.FECHA_VENCIMIENTO));
    }

    @Test
    public void parseCantidad_aceptaSoloEnterosEnRango() {
        assertEquals(Integer.valueOf(0), ProductoValidator.parseCantidad("0"));
        assertEquals(Integer.valueOf(15), ProductoValidator.parseCantidad(" 15 "));
        assertNull(ProductoValidator.parseCantidad("-1"));
        assertNull(ProductoValidator.parseCantidad("100000"));
        assertNull(ProductoValidator.parseCantidad("2.5"));
        assertNull(ProductoValidator.parseCantidad(null));
    }
}
