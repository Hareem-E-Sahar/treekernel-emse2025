package javadata.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javadata.data.DataManager;
import javadata.util.Globals;
import javadata.encryption.CipherFactory;
import javadata.encryption.KeyManager;
import javadata.encryption.Password;

/**
 * <p>
 * <b>Title: </b>Class to read and write {@link javadata.data.DataManager} objects in XML files.
 * </p>
 *
 * <p>
 * <b>Description: </b>Class to read and write {@link javadata.data.DataManager} objects in XML files.
 * </p>
 * 
 * <p><b>Version: </b>1.0</p>
 * 
 * <p>
 * <b>Author: </b> Matthew Pearson, Copyright 2006, 2007
 * </p>
 * 
 * <p>
 * <b>License: </b>This file is part of JavaData.
 * </p>
 * <p>
 * JavaData is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * </p>
 * <p>
 * JavaData is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * </p>
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with JavaData.  If not, see 
 * <a href="http://www.gnu.org/licenses/">GNU licenses</a>.
 * </p> 
 * 
 */
public class IOXMLFile {

    /**
	 * Method to construct an file containing the XML information from
	 * the {@link javadata.data.DataManager} object.
	 * 
	 * If the <code>password</code> parameter is null, no encyption takes place. 
	 * 
	 * Note: Currently this method also zeros out the character password array.
	 * But just in case the implementation of this method changes in the future,
	 * it is recommended to manually zero the password character array after
	 * calling this method.
	 * 
	 * @param data The <code>DataManager</code> to use.
	 * @param filename The name of the file to write 
	 * @param password
	 * @throws IOException
	 */
    public void writeDataManager(DataManager dm, String filename, char[] password) throws IOException {
        OutputStream out = null;
        if (password != null) {
            Cipher cipher = CipherFactory.createCipher(Globals.getENCRYPTION_CIPHER(), Globals.getENCRYPTION_MODE(), Globals.getENCRYPTION_PADDING());
            SecretKey key = KeyManager.createKey(password);
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key, KeyManager.getParameterSpec());
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            out = new BufferedOutputStream(new CipherOutputStream(new FileOutputStream(filename), cipher));
        } else {
            out = new BufferedOutputStream(new FileOutputStream(filename));
        }
        this.mXmlStream.streamDataManagerOut(dm, out);
        out.flush();
        out.close();
    }

    /**
	 * Method to read an XML file containing a 
	 * {@link javadata.data.DataManager} structure.
	 * 
	 * If the <code>password</code> parameter is null then no decryption takes place.
	 * 
	 * Note: Currently this method also zeros out the character password array.
	 * But just in case the implementation of this method changes in the future,
	 * it is recommended to manually zero the password character array after
	 * calling this method.
	 * 
	 * @param filename
	 *            The name of the XML file.
	 * @param password
	 * @return DataManager
	 * @throws IOException
	 *             If file cannot be read.
	 */
    public DataManager readDataManager(String filename, char[] password) throws IOException {
        InputStream in = null;
        if (password != null) {
            Cipher cipher = CipherFactory.createCipher(Globals.getENCRYPTION_CIPHER(), Globals.getENCRYPTION_MODE(), Globals.getENCRYPTION_PADDING());
            SecretKey key = KeyManager.createKey(password);
            try {
                cipher.init(Cipher.DECRYPT_MODE, key, KeyManager.getParameterSpec());
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            in = new BufferedInputStream(new CipherInputStream(new FileInputStream(filename), cipher));
        } else {
            in = new FileInputStream(filename);
        }
        DataManager readDM = this.mXmlStream.streamDataManagerIn(in);
        in.close();
        return readDM;
    }

    private IOXMLStream mXmlStream = new IOXMLStream();
}
