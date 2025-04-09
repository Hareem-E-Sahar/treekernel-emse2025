package org.framework.crypt;

import java.util.zip.CRC32;

/**
 * Classe utilitaire permettant de calculer le checksum d'une chaine de
 * caract�res
 * 
 * @author Eric Reboisson
 * 
 */
public class ComputeCRC32 {

    public static long getCheckSum(String chaine) {
        CRC32 crc32 = new CRC32();
        crc32.update(chaine.getBytes());
        return crc32.getValue();
    }
}
