package br.edu.ufcg.lsd.seghidro.server.autentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Criptografia {

    public static String md5(String senha) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Ocorreu NoSuchAlgorithmException");
        }
        md.update(senha.getBytes());
        byte[] xx = md.digest();
        String n2 = null;
        StringBuffer resposta = new StringBuffer();
        for (int i = 0; i < xx.length; i++) {
            n2 = Integer.toHexString(0XFF & ((int) (xx[i])));
            if (n2.length() < 2) {
                n2 = "0" + n2;
            }
            resposta.append(n2);
        }
        return resposta.toString();
    }

    public static void main(String[] args) {
        System.out.println(Criptografia.md5("seg!@#hidro"));
    }
}
