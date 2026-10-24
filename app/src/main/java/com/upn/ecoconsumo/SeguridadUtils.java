package com.upn.ecoconsumo;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hash de contraseñas con PBKDF2 + sal aleatoria; nunca se guarda la contraseña en texto plano. */
public final class SeguridadUtils {

    private static final int ITERACIONES = 20000;
    private static final int LONGITUD_BITS = 256;

    private SeguridadUtils() {}

    /** Devuelve "iteraciones:sal:hash" en Base64. */
    public static String hashContrasena(String contrasena) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] hash = pbkdf2(contrasena, sal, ITERACIONES);
        return ITERACIONES + ":" + b64(sal) + ":" + b64(hash);
    }

    public static boolean verificarContrasena(String contrasena, String almacenado) {
        String[] partes = almacenado.split(":");
        if (partes.length != 3) return false;
        int iteraciones = Integer.parseInt(partes[0]);
        byte[] sal = Base64.decode(partes[1], Base64.NO_WRAP);
        byte[] esperado = Base64.decode(partes[2], Base64.NO_WRAP);
        return MessageDigest.isEqual(esperado, pbkdf2(contrasena, sal, iteraciones));
    }

    private static byte[] pbkdf2(String contrasena, byte[] sal, int iteraciones) {
        try {
            KeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, LONGITUD_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String b64(byte[] datos) {
        return Base64.encodeToString(datos, Base64.NO_WRAP);
    }
}
