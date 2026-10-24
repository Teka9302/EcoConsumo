package com.upn.ecoconsumo.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

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
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERACIONES + ":" + b64.encodeToString(sal) + ":" + b64.encodeToString(hash);
    }

    public static boolean verificarContrasena(String contrasena, String almacenado) {
        String[] partes = almacenado.split(":");
        if (partes.length != 3) return false;
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            return MessageDigest.isEqual(esperado, pbkdf2(contrasena, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Mínimo 6 caracteres, con al menos una letra y un número. */
    public static boolean esContrasenaSegura(String contrasena) {
        return contrasena != null
                && contrasena.length() >= 6
                && contrasena.matches(".*[A-Za-z].*")
                && contrasena.matches(".*\\d.*");
    }

    private static byte[] pbkdf2(String contrasena, byte[] sal, int iteraciones) {
        try {
            KeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, LONGITUD_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
