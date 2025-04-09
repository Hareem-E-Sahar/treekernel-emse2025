package com.openemulators.vnes;

import java.io.File;
import java.io.RandomAccessFile;
import javax.swing.JOptionPane;
import com.openemulators.vnes.mapper.Mapper001;
import com.openemulators.vnes.mapper.Mapper002;
import com.openemulators.vnes.mapper.Mapper003;
import com.openemulators.vnes.mapper.Mapper004;
import com.openemulators.vnes.mapper.Mapper007;
import com.openemulators.vnes.mapper.Mapper009;
import com.openemulators.vnes.mapper.Mapper010;
import com.openemulators.vnes.mapper.Mapper011;
import com.openemulators.vnes.mapper.Mapper015;
import com.openemulators.vnes.mapper.Mapper018;
import com.openemulators.vnes.mapper.Mapper021;
import com.openemulators.vnes.mapper.Mapper022;
import com.openemulators.vnes.mapper.Mapper023;
import com.openemulators.vnes.mapper.Mapper032;
import com.openemulators.vnes.mapper.Mapper033;
import com.openemulators.vnes.mapper.Mapper034;
import com.openemulators.vnes.mapper.Mapper048;
import com.openemulators.vnes.mapper.Mapper064;
import com.openemulators.vnes.mapper.Mapper066;
import com.openemulators.vnes.mapper.Mapper068;
import com.openemulators.vnes.mapper.Mapper071;
import com.openemulators.vnes.mapper.Mapper072;
import com.openemulators.vnes.mapper.Mapper075;
import com.openemulators.vnes.mapper.Mapper078;
import com.openemulators.vnes.mapper.Mapper079;
import com.openemulators.vnes.mapper.Mapper087;
import com.openemulators.vnes.mapper.Mapper094;
import com.openemulators.vnes.mapper.Mapper105;
import com.openemulators.vnes.mapper.Mapper140;
import com.openemulators.vnes.mapper.Mapper182;
import com.openemulators.vnes.mapper.Mapper232;
import com.openemulators.vnes.mapper.MemoryMapper;

public class ROM {

    public static final int VERTICAL_MIRRORING = 0;

    public static final int HORIZONTAL_MIRRORING = 1;

    public static final int FOURSCREEN_MIRRORING = 2;

    public static final int SINGLESCREEN_MIRRORING = 3;

    public static final int SINGLESCREEN_MIRRORING2 = 4;

    public static final int SINGLESCREEN_MIRRORING3 = 5;

    public static final int SINGLESCREEN_MIRRORING4 = 6;

    public static final int CHRROM_MIRRORING = 7;

    boolean failedSaveFile = false;

    boolean saveRamUpToDate = true;

    short[] header;

    short[][] rom;

    short[][] vrom;

    short[] saveRam;

    Tile[][] vromTile;

    NES nes;

    int romCount;

    int vromCount;

    int mirroring;

    boolean batteryRam;

    boolean trainer;

    boolean fourScreen;

    int mapperType;

    String fileName;

    RandomAccessFile raFile;

    boolean enableSave = true;

    boolean valid;

    static String[] mapperName;

    static boolean[] mapperSupported;

    protected long crc32 = 0;

    static {
        mapperName = new String[255];
        mapperSupported = new boolean[255];
        for (int i = 0; i < 255; i++) {
            mapperName[i] = "Unknown Mapper";
        }
        mapperName[0] = "NROM";
        mapperName[1] = "Nintendo MMC1";
        mapperName[2] = "UxROM";
        mapperName[3] = "CNROM";
        mapperName[4] = "Nintendo MMC3";
        mapperName[5] = "Nintendo MMC5";
        mapperName[6] = "FFE F4xxx";
        mapperName[7] = "AxROM";
        mapperName[8] = "FFE F3xxx";
        mapperName[9] = "Nintendo MMC2";
        mapperName[10] = "Nintendo MMC4";
        mapperName[11] = "Color Dreams";
        mapperName[12] = "FFE F6xxx";
        mapperName[13] = "CPROM";
        mapperName[15] = "iNES Mapper #015";
        mapperName[16] = "Bandai";
        mapperName[17] = "FFE F8xxx";
        mapperName[18] = "Jaleco SS8806";
        mapperName[19] = "Namcot 106";
        mapperName[20] = "(Hardware) Famicom Disk System";
        mapperName[21] = "Konami VRC4a, VRC4c";
        mapperName[22] = "Konami VRC2a";
        mapperName[23] = "Konami VRC2b, VRC4e, VRC4f";
        mapperName[24] = "Konami VRC6a";
        mapperName[25] = "Konami VRC4b, VRC4d";
        mapperName[26] = "Konami VRC6b";
        mapperName[32] = "Irem G-101";
        mapperName[33] = "Taito TC0190, TC0350";
        mapperName[34] = "BxROM, NINA-001";
        mapperName[41] = "Caltron 6-in-1";
        mapperName[46] = "Rumblestation 15-in-1";
        mapperName[47] = "Nintendo MMC3 Multicart (Super Spike V'Ball + Nintendo World Cup)";
        mapperName[48] = "iNES Mapper #048";
        mapperName[64] = "Tengen RAMBO-1";
        mapperName[65] = "Irem H-3001";
        mapperName[66] = "GxROM";
        mapperName[67] = "Sunsoft 3";
        mapperName[68] = "Sunsoft 4";
        mapperName[69] = "Sunsoft FME-7";
        mapperName[70] = "iNES Mapper #070";
        mapperName[71] = "Camerica";
        mapperName[72] = "iNES Mapper #072";
        mapperName[73] = "Konami VRC3";
        mapperName[75] = "Konami VRC1";
        mapperName[76] = "iNES Mapper #076 (Digital Devil Monogatari - Megami Tensei)";
        mapperName[77] = "iNES Mapper #077 (Napoleon Senki)";
        mapperName[78] = "Irem 74HC161/32";
        mapperName[79] = "American Game Cartridges";
        mapperName[80] = "iNES Mapper #080";
        mapperName[82] = "iNES Mapper #082";
        mapperName[85] = "Konami VRC7a, VRC7b";
        mapperName[86] = "iNES Mapper #086 (Moero!! Pro Yakyuu)";
        mapperName[87] = "iNES Mapper #087";
        mapperName[88] = "iNES Mapper #088";
        mapperName[89] = "iNES Mapper #087 (Mito Koumon)";
        mapperName[92] = "iNES Mapper #092";
        mapperName[93] = "iNES Mapper #093 (Fantasy Zone)";
        mapperName[94] = "iNES Mapper #094 (Senjou no Ookami)";
        mapperName[95] = "iNES Mapper #095 (Dragon Buster) [MMC3 Derived]";
        mapperName[96] = "(Hardware) Oeka Kids Tablet";
        mapperName[97] = "iNES Mapper #097 (Kaiketsu Yanchamaru)";
        mapperName[105] = "NES-EVENT [MMC1 Derived]";
        mapperName[113] = "iNES Mapper #113";
        mapperName[115] = "iNES Mapper #115 (Yuu Yuu Hakusho Final) [MMC3 Derived]";
        mapperName[118] = "iNES Mapper #118 [MMC3 Derived]";
        mapperName[119] = "TQROM";
        mapperName[140] = "iNES Mapper #140 (Bio Senshi Dan)";
        mapperName[152] = "iNES Mapper #152";
        mapperName[154] = "iNES Mapper #152 (Devil Man)";
        mapperName[159] = "Bandai (Alternate of #016)";
        mapperName[180] = "(Hardware) Crazy Climber Controller";
        mapperName[182] = "iNES Mapper #182";
        mapperName[184] = "iNES Mapper #184";
        mapperName[185] = "iNES Mapper #185";
        mapperName[207] = "iNES Mapper #185 (Fudou Myouou Den)";
        mapperName[228] = "Active Enterprises";
        mapperName[232] = "Camerica (Quattro series)";
        mapperSupported[0] = true;
        mapperSupported[1] = true;
        mapperSupported[2] = true;
        mapperSupported[3] = true;
        mapperSupported[4] = true;
        mapperSupported[7] = true;
        mapperSupported[9] = true;
        mapperSupported[10] = true;
        mapperSupported[11] = true;
        mapperSupported[15] = true;
        mapperSupported[18] = true;
        mapperSupported[21] = true;
        mapperSupported[22] = true;
        mapperSupported[23] = true;
        mapperSupported[32] = true;
        mapperSupported[33] = true;
        mapperSupported[34] = true;
        mapperSupported[48] = true;
        mapperSupported[64] = true;
        mapperSupported[66] = true;
        mapperSupported[68] = true;
        mapperSupported[71] = true;
        mapperSupported[72] = true;
        mapperSupported[75] = true;
        mapperSupported[78] = true;
        mapperSupported[79] = true;
        mapperSupported[87] = true;
        mapperSupported[94] = true;
        mapperSupported[105] = true;
        mapperSupported[140] = true;
        mapperSupported[182] = true;
        mapperSupported[232] = true;
    }

    public ROM(NES nes) {
        this.nes = nes;
        valid = false;
    }

    public void load(String fileName) {
        this.fileName = fileName;
        FileLoader loader = new FileLoader();
        short[] b = loader.loadFile(fileName, nes.getGui());
        if (b == null || b.length == 0) {
            nes.gui.showErrorMsg("Unable to load ROM file.");
            valid = false;
        }
        header = new short[16];
        for (int i = 0; i < 16; i++) {
            header[i] = b[i];
        }
        String fcode = new String(new byte[] { (byte) b[0], (byte) b[1], (byte) b[2], (byte) b[3] });
        if (!fcode.equals("NES" + new String(new byte[] { 0x1A }))) {
            valid = false;
            return;
        }
        romCount = header[4];
        vromCount = header[5] * 2;
        mirroring = ((header[6] & 1) != 0 ? 1 : 0);
        batteryRam = (header[6] & 2) != 0;
        trainer = (header[6] & 4) != 0;
        fourScreen = (header[6] & 8) != 0;
        mapperType = (header[6] >> 4) | (header[7] & 0xF0);
        if (batteryRam) {
            loadBatteryRam();
        }
        boolean foundError = false;
        for (int i = 8; i < 16; i++) {
            if (header[i] != 0) {
                foundError = true;
                break;
            }
        }
        if (foundError) {
            mapperType &= 0xF;
        }
        rom = new short[romCount][16384];
        vrom = new short[vromCount][4096];
        vromTile = new Tile[vromCount][256];
        int offset = 16;
        for (int i = 0; i < romCount; i++) {
            for (int j = 0; j < 16384; j++) {
                if (offset + j >= b.length) {
                    break;
                }
                rom[i][j] = b[offset + j];
            }
            offset += 16384;
        }
        for (int i = 0; i < vromCount; i++) {
            for (int j = 0; j < 4096; j++) {
                if (offset + j >= b.length) {
                    break;
                }
                vrom[i][j] = b[offset + j];
            }
            offset += 4096;
        }
        for (int i = 0; i < vromCount; i++) {
            for (int j = 0; j < 256; j++) {
                vromTile[i][j] = new Tile();
            }
        }
        int tileIndex;
        int leftOver;
        for (int v = 0; v < vromCount; v++) {
            for (int i = 0; i < 4096; i++) {
                tileIndex = i >> 4;
                leftOver = i % 16;
                if (leftOver < 8) {
                    vromTile[v][tileIndex].setScanline(leftOver, vrom[v][i], vrom[v][i + 8]);
                } else {
                    vromTile[v][tileIndex].setScanline(leftOver - 8, vrom[v][i - 8], vrom[v][i]);
                }
            }
        }
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        byte[] tempArray = new byte[rom.length + vrom.length];
        crc.update(tempArray);
        crc32 = crc.getValue();
        tempArray = null;
        System.out.println("CRC Value: " + crc32 + "");
        valid = true;
    }

    public boolean isValid() {
        return valid;
    }

    public int getRomBankCount() {
        return romCount;
    }

    public int getVromBankCount() {
        return vromCount;
    }

    public short[] getHeader() {
        return header;
    }

    public short[] getRomBank(int bank) {
        return rom[bank];
    }

    public short[] getVromBank(int bank) {
        return vrom[bank];
    }

    public Tile[] getVromBankTiles(int bank) {
        return vromTile[bank];
    }

    public int getMirroringType() {
        if (fourScreen) {
            return FOURSCREEN_MIRRORING;
        }
        if (mirroring == 0) {
            return HORIZONTAL_MIRRORING;
        }
        return VERTICAL_MIRRORING;
    }

    public int getMapperType() {
        return mapperType;
    }

    public String getMapperName() {
        if (mapperType >= 0 && mapperType < mapperName.length) {
            return mapperName[mapperType];
        }
        return "Unknown Mapper, " + mapperType;
    }

    public boolean hasBatteryRam() {
        return batteryRam;
    }

    public boolean hasTrainer() {
        return trainer;
    }

    public String getFileName() {
        File f = new File(fileName);
        return f.getName();
    }

    public boolean mapperSupported() {
        if (mapperType < mapperSupported.length && mapperType >= 0) {
            return mapperSupported[mapperType];
        }
        return false;
    }

    public MemoryMapper createMapper() {
        if (mapperSupported()) {
            switch(mapperType) {
                case 0:
                    {
                        return new MapperDefault();
                    }
                case 1:
                    {
                        return new Mapper001();
                    }
                case 2:
                    {
                        return new Mapper002();
                    }
                case 3:
                    {
                        return new Mapper003();
                    }
                case 4:
                    {
                        return new Mapper004();
                    }
                case 7:
                    {
                        return new Mapper007();
                    }
                case 9:
                    {
                        return new Mapper009();
                    }
                case 10:
                    {
                        return new Mapper010();
                    }
                case 11:
                    {
                        return new Mapper011();
                    }
                case 15:
                    {
                        return new Mapper015();
                    }
                case 18:
                    {
                        return new Mapper018();
                    }
                case 21:
                    {
                        return new Mapper021();
                    }
                case 22:
                    {
                        return new Mapper022();
                    }
                case 23:
                    {
                        return new Mapper023();
                    }
                case 32:
                    {
                        return new Mapper032();
                    }
                case 33:
                    {
                        return new Mapper033();
                    }
                case 34:
                    {
                        return new Mapper034();
                    }
                case 48:
                    {
                        return new Mapper048();
                    }
                case 64:
                    {
                        return new Mapper064();
                    }
                case 66:
                    {
                        return new Mapper066();
                    }
                case 68:
                    {
                        return new Mapper068();
                    }
                case 71:
                    {
                        return new Mapper071();
                    }
                case 72:
                    {
                        return new Mapper072();
                    }
                case 75:
                    {
                        return new Mapper075();
                    }
                case 78:
                    {
                        return new Mapper078();
                    }
                case 79:
                    {
                        return new Mapper079();
                    }
                case 87:
                    {
                        return new Mapper087();
                    }
                case 94:
                    {
                        return new Mapper094();
                    }
                case 105:
                    {
                        return new Mapper105();
                    }
                case 140:
                    {
                        return new Mapper140();
                    }
                case 182:
                    {
                        return new Mapper182();
                    }
                case 232:
                    {
                        return new Mapper232();
                    }
            }
        }
        nes.gui.showErrorMsg("Warning: Mapper not supported yet.");
        return new MapperDefault();
    }

    public void setSaveState(boolean enableSave) {
        if (enableSave && !batteryRam) {
            loadBatteryRam();
        }
    }

    public short[] getBatteryRam() {
        return saveRam;
    }

    private void loadBatteryRam() {
        if (batteryRam) {
            try {
                saveRam = new short[0x2000];
                saveRamUpToDate = true;
                String encodedData = JOptionPane.showInputDialog("Returning players insert Save Code here.");
                if (encodedData == null) {
                    return;
                }
                encodedData = encodedData.replaceAll("[^\\p{XDigit}]", "");
                if (encodedData.length() != saveRam.length * 2) {
                    return;
                }
                for (int i = 0; i < saveRam.length; i++) {
                    String hexByte = encodedData.substring(i * 2, i * 2 + 2);
                    saveRam[i] = Short.parseShort(hexByte, 16);
                }
                if (nes.getMemoryMapper() != null) {
                    nes.getMemoryMapper().loadBatteryRam();
                }
            } catch (Exception e) {
                failedSaveFile = true;
            }
        }
    }

    public void writeBatteryRam(int address, short value) {
        if (!failedSaveFile && !batteryRam && enableSave) {
            loadBatteryRam();
        }
        if (batteryRam && enableSave && !failedSaveFile) {
            saveRam[address - 0x6000] = value;
            saveRamUpToDate = false;
        }
    }

    public void closeRom() {
        if (batteryRam && !saveRamUpToDate) {
            try {
                StringBuilder sb = new StringBuilder(saveRam.length * 2 + saveRam.length / 38);
                for (int i = 0; i < saveRam.length; i++) {
                    String hexByte = String.format("%02x", saveRam[i] & 0xFF);
                    if (i % 38 == 0 && i != 0) {
                        sb.append(" ");
                    }
                    sb.append(hexByte);
                }
                String encodedData = sb.toString();
                JOptionPane.showInputDialog("Save Code for Resuming Game.", encodedData);
                saveRamUpToDate = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy() {
        closeRom();
        nes = null;
    }
}
