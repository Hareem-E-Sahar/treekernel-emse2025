package criptografia;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
* Criptografia/descriptografia usando Chaves Assim�tricas 
* (Agoritmo RSA) ou Sim�trica (Algoritmo DES)
* 
* @author Leandro Alexandre, S�rgio Daniel, Rafael Duarte, Thiago Rosa 
* @version 0.6
*/
public class CriptoImpl implements Cripto {

    /**
	 * Criptografa um array de bytes usando o algoritmo RSA.
	 * @param pubk 
	 * @param entrada
	 * @return array de bytes criptografado
	 */
    public byte[] criptografaRsa(PublicKey pubk, byte[] entrada) {
        return getCripto(pubk, entrada, "RSA", false);
    }

    /**
	 * Criptografa um array de bytes usando o algoritmo DES.
	 * @param key
	 * @param entrada
	 * @return array de bytes criptografado
	 */
    public byte[] criptografaDes(Key key, byte[] entrada) {
        return getCripto(key, entrada, "DES", false);
    }

    /**
	 * Criptografa um arquivo usando o algoritmo RSA.
	 * @param pubk
	 * @param arquivoEntrada
	 * @param arquivoSaida
	 *  
	 */
    public void criptografaRsa(PublicKey pubk, String arquivoEntrada, String arquivoSaida) {
        getCriptoFile(pubk, arquivoEntrada, arquivoSaida, "RSA", false);
    }

    /**
	 * Criptografa um arquivo usando o algoritmo DES.
	 * @param key
	 * @param arquivoEntrada
	 * @param arquivoSaida
	 *  
	 */
    public void criptografaDes(Key key, String arquivoEntrada, String arquivoSaida) {
        getCriptoFile(key, arquivoEntrada, arquivoSaida, "DES", false);
    }

    /**
	 * Descriptografa array de bytes usando o algoritmo RSA.
	 * @param privk
	 * @param entrada
	 * @return array de bytes descriptografado
	 *  
	 */
    public byte[] descriptografaRsa(PrivateKey privk, byte[] entrada) {
        return getCripto(privk, entrada, "RSA", true);
    }

    /**
	 * Descriptografa array de bytes usando o algoritmo RSA.
	 * @param key
	 * @param entrada
	 * @return array de bytes descriptografado
	 *  
	 */
    public byte[] descriptografaDes(Key key, byte[] entrada) {
        return getCripto(key, entrada, "DES", true);
    }

    /**
	 * Descriptografa um arquivo usando o algoritmo RSA.
	 * @param privk
	 * @param arquivoEntrada
	 * @param arquivoSaida
	 *  
	 */
    public void descriptografaRsa(PrivateKey privk, String arquivoEntrada, String arquivoSaida) {
        getCriptoFile(privk, arquivoEntrada, arquivoSaida, "RSA", true);
    }

    /**
	 * Descriptografa um arquivo usando o algoritmo DES.
	 * @param key
	 * @param arquivoEntrada
	 * @param arquivoSaida
	 *  
	 */
    public void descriptografaDes(Key key, String arquivoEntrada, String arquivoSaida) {
        getCriptoFile(key, arquivoEntrada, arquivoSaida, "DES", true);
    }

    /**
     * Criptografa/descriptografa sequencia de bytes.
     * 
     * @param senha chave de criptografia/descriptografia
     * @param entrada sequencia de bytes a ser criptografada/descriptografada
     * @param algoritmo algoritmo de critografia/descriptografia
     * @param descriptografa variavel booleana, que se verdadeira
     * executa a descriptografia. Se falsa, criptografa.
     * @return cadeia de bytes criptografada/descriptografada
     */
    private byte[] getCripto(Key senha, byte[] entrada, String algoritmo, boolean descriptografa) {
        Cipher cifra;
        try {
            cifra = Cipher.getInstance(algoritmo);
            if (descriptografa) {
                cifra.init(Cipher.DECRYPT_MODE, senha);
            } else {
                cifra.init(Cipher.ENCRYPT_MODE, senha);
            }
            return cifra.doFinal(entrada);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Criptografa/descriptografa arquivo
 	 * 
     * @param senha Chave de criptografia/descriptografia
     * @param arquivoEntrada arquivo a ser criptografado/descriptografado
     * @param arquivoSaida resutado da criptografia/descriptografia
     * @param algoritmo algoritmo de criptografia/descriptografia 
     * @param descriptografa se verdadeiro, executa a funcao de descriptografia,
     * caso contrario, criptografa.
     */
    private void getCriptoFile(Key senha, String arquivoEntrada, String arquivoSaida, String algoritmo, Boolean descriptografa) {
        Cipher cifra;
        try {
            cifra = Cipher.getInstance(algoritmo);
            FileInputStream fis2 = null;
            byte[] buffer2 = new byte[1];
            int bytesLidos = -1;
            fis2 = new FileInputStream(arquivoEntrada);
            bytesLidos = -1;
            int i = 0;
            while ((bytesLidos = fis2.read(buffer2)) != -1) {
                i++;
            }
            fis2.close();
            fis2 = new FileInputStream(arquivoEntrada);
            byte[] conteudoArquivo = new byte[i];
            fis2.read(conteudoArquivo);
            if (descriptografa) {
                cifra.init(Cipher.DECRYPT_MODE, senha);
            } else {
                cifra.init(Cipher.ENCRYPT_MODE, senha);
            }
            FileOutputStream fos = new FileOutputStream(arquivoSaida);
            fos.write(cifra.doFinal(conteudoArquivo));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converte array de bytes em string hexa
     * @param hash
     * @return string hexa
     */
    public String toHex(byte[] hash) {
        final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char strHash[] = new char[hash.length * 2];
        for (int i = 0, x = 0; i < hash.length; i++) {
            strHash[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            strHash[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(strHash);
    }
}
