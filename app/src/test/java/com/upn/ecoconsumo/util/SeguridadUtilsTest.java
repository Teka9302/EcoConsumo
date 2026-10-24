package com.upn.ecoconsumo.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SeguridadUtilsTest {

    @Test
    public void hashVerificaLaContrasenaCorrecta() {
        String hash = SeguridadUtils.hashContrasena("clave123");
        assertTrue(SeguridadUtils.verificarContrasena("clave123", hash));
        assertFalse(SeguridadUtils.verificarContrasena("clave124", hash));
    }

    @Test
    public void hashNoContieneLaContrasenaYUsaSalDistinta() {
        String a = SeguridadUtils.hashContrasena("clave123");
        String b = SeguridadUtils.hashContrasena("clave123");
        assertFalse(a.contains("clave123"));
        assertNotEquals(a, b);
    }

    @Test
    public void hashMalFormado_noSeAcepta() {
        assertFalse(SeguridadUtils.verificarContrasena("clave123", "texto-plano"));
        assertFalse(SeguridadUtils.verificarContrasena("clave123", "x:y:z"));
    }

    @Test
    public void reglasDeContrasenaSegura() {
        assertTrue(SeguridadUtils.esContrasenaSegura("clave123"));
        assertFalse(SeguridadUtils.esContrasenaSegura("123456"));
        assertFalse(SeguridadUtils.esContrasenaSegura("abcdef"));
        assertFalse(SeguridadUtils.esContrasenaSegura("ab1"));
        assertFalse(SeguridadUtils.esContrasenaSegura(null));
    }
}
