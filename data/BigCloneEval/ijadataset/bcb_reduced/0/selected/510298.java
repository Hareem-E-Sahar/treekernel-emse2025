package lv.accenture.ex04;

import java.util.zip.CRC32;

/**
 * (1) Aptaujaat sveshus URL-us : pieprasiit url + izvilkt XPath/regexp.
 * (Glossary use-case; Title atrod no references URL-a) ---> integraacijaa 
 * (2) Util metodes, kuras apstraadaa String (CRC/MD5)  
 * (3) Math izteiksmes - LaTeX ) --> sisteemas izsaukumi 
 * (4) Darbina LilyPond/LilyBook ) --> sisteemas izsaukumi
 * (5) Upload uz Webiski redzamu failu sisteemu /home/ftp/Training.JavaEim/*.ppt
 * + uploadeetam failam velkas liidzi metainformaacija + 
 * ziimee satura raadiitaaju ---> satura repozitorija veidošana
 * (6) Velocity šabloni, lai sagatavotu XAR priekš importēšanas
 * ---> Datu migraacijas uzdevums
 * (7) Zīmē Simile Exhibit lietas ar filtru - t.sk. dziesmiņas.  
 */
public class CRCTest {

    public static void main(String[] args) {
        CRC32 crc = new CRC32();
        crc.update("/path/file.html".getBytes());
        long val = crc.getValue();
        System.out.println(Long.toHexString(val));
    }
}
