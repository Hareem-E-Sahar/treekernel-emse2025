package com.tscribble.bitleech.core.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ComputeDigest {

    private static final String hexDigits = "0123456789abcdef";

    /**
	 * Realiza um digest em um array de bytes atrav?s do algoritmo especificado
	 * 
	 * @param input -
	 *            O array de bytes a ser criptografado
	 * @param algoritmo -
	 *            O algoritmo a ser utilizado
	 * @return byte[] - O resultado da criptografia
	 * @throws NoSuchAlgorithmException -
	 *             Caso o algoritmo fornecido n?o seja v?lido
	 */
    private static byte[] digest(byte[] input, String algoritmo) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algoritmo);
        md.reset();
        return md.digest(input);
    }

    /**
	 * Converte o array de bytes em uma representa??o hexadecimal.
	 * 
	 * @param input -
	 *            O array de bytes a ser convertido.
	 * @return Uma String com a representa??o hexa do array
	 */
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            int j = ((int) b[i]) & 0x00FF;
            buf.append(hexDigits.charAt(j / 16));
            buf.append(hexDigits.charAt(j % 16));
        }
        return buf.toString();
    }

    /**
	 * Converte uma String hexa no array de bytes correspondente.
	 * 
	 * @param hexa -
	 *            A String hexa
	 * @return O vetor de bytes
	 * @throws IllegalArgumentException -
	 *             Caso a String n?o sej auma representa??o haxadecimal v?lida
	 */
    private static byte[] hexStringToByteArray(String hexa) throws IllegalArgumentException {
        if (hexa.length() % 2 != 0) {
            throw new IllegalArgumentException("String hexadecimal inv?lida");
        }
        byte[] b = new byte[hexa.length() / 2];
        for (int i = 0; i < hexa.length(); i += 2) {
            b[i / 2] = (byte) ((hexDigits.indexOf(hexa.charAt(i)) << 4) | (hexDigits.indexOf(hexa.charAt(i + 1))));
        }
        return b;
    }

    public static String calculate(String str, String algorithm) {
        try {
            byte[] b = ComputeDigest.digest(str.getBytes(), algorithm);
            return ComputeDigest.byteArrayToHexString(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
