package ntr.ttme;

import java.io.IOException;
import java.io.InputStream;

public class TrueTypeDefinition extends Object {

    private byte[] fontData;

    private TTGlyph[] glyphs;

    private byte[] cmap;

    private int[] glyphOffsetArray;

    private TTKernPair[] kernPairs;

    private short cmapFormat;

    private short numTables;

    private short numberOfHMetrics;

    private short numKernPairs;

    private short indexToLocFormat;

    private short numGlyphs;

    private byte[] copyright;

    private byte[] familyName;

    private byte[] fullName;

    private byte[] subfamilyName;

    private byte[] uniqueName;

    private byte[] versionName;

    private short xMax;

    private short xMin;

    private short yMax;

    private short yMin;

    private short unitsPerMSqr;

    private short ascender;

    private short descender;

    private short lineGap;

    private short platformID;

    private short specificID;

    private short languageID;

    private String[] specificList;

    private short[] PIDList;

    private short[] SIDList;

    private int specificListTotal;

    private boolean loadingCompleted;

    public TrueTypeDefinition(InputStream inputStream, short platformID, short specificID, short languageID) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        this.platformID = platformID;
        this.specificID = specificID;
        this.languageID = languageID;
        readFontData(inputStream);
        processFontHeaderTable();
        processMaximumProfileTable();
        processNamingTable();
        processIndexToLocationTable();
        processCharacterMappingTable();
        processGlyphDataTable();
        processHorizontalHeaderTable();
        processHorizontalMetricsTable();
        try {
            processKerningTable();
        } catch (TrueTypeKerningBusinessException ignore) {
        }
        if (fontData != null) {
            fontData = null;
        }
        loadingCompleted = true;
    }

    public TrueTypeDefinition(InputStream inputStream, boolean completeWithDefaults) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        readFontData(inputStream);
        processFontHeaderTable();
        processMaximumProfileTable();
        processNamingTableNoQuestionsAsked();
        processIndexToLocationTable();
        retrieveSpecificList();
        if (completeWithDefaults) {
            if (specificListTotal == 0) {
                throw new TrueTypeBusinessException("Specific list is empty, unable to complete loading!");
            }
            completeLoading(PIDList[0], SIDList[0]);
        }
    }

    public void completeLoading(short platformID, short specificID) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        if (loadingCompleted) {
            throw new IllegalStateException("Loading already completed!");
        }
        this.platformID = platformID;
        this.specificID = specificID;
        processCharacterMappingTable();
        processGlyphDataTable();
        processHorizontalHeaderTable();
        processHorizontalMetricsTable();
        try {
            processKerningTable();
        } catch (TrueTypeKerningBusinessException ignore) {
        }
        if (fontData != null) {
            fontData = null;
        }
        loadingCompleted = true;
    }

    /**
     * Loads contents of TTF file into <code>fontData</code> array (instance
     * variable of this class).
     * <p>
     * After loading fontData contains: - offsetTable - tableDir (each entry==4
     * long), 4th long-value is table length - tables ("tableDataSize" bytes in
     * length), LONG-aligned memory blocks
     * </p>
     */
    private void readFontData(InputStream in) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            int fileLength = 0;
            byte[] offsetTableData = new byte[TTConstants.OFFSET_TABLE_SIZE];
            for (int i = 0; i < TTConstants.OFFSET_TABLE_SIZE; i++) {
                int b = in.read();
                if (b != -1) {
                    offsetTableData[i] = (byte) b;
                    fileLength++;
                } else {
                    throw new TrueTypeBusinessException("Could not read " + TTConstants.OFFSET_TABLE_SIZE + " bytes of offset-table information! (File length: " + fileLength + ")");
                }
            }
            numTables = Bitwise.toUSHORT(offsetTableData[4], offsetTableData[5]);
            int tableDirSize = TTConstants.SIZE_OF_ULONG * 4 * numTables;
            byte[] tableDir = new byte[tableDirSize];
            int tableDataSize = 0;
            for (int i = 0; i < tableDirSize; i += 16) {
                int totalBytesRead = in.read(tableDir, i, 16);
                fileLength += totalBytesRead;
                if (totalBytesRead != 16) {
                    throw new TrueTypeBusinessException("Could not read 16 bytes of tableDir information! Instead, read returned " + totalBytesRead + " (File length: " + fileLength + ")");
                }
                long tableLength = Bitwise.toULONG(tableDir[i + 12], tableDir[i + 13], tableDir[i + 14], tableDir[i + 15]);
                if ((tableLength / 4) * 4 != tableLength) {
                    tableLength = (tableLength / 4 + 1) * 4;
                }
                tableDataSize += tableLength;
            }
            int fontDataSize = TTConstants.OFFSET_TABLE_SIZE + tableDirSize + tableDataSize;
            fontData = new byte[fontDataSize];
            System.arraycopy(offsetTableData, 0, fontData, 0, TTConstants.OFFSET_TABLE_SIZE);
            System.arraycopy(tableDir, 0, fontData, TTConstants.OFFSET_TABLE_SIZE, tableDirSize);
            int start = TTConstants.OFFSET_TABLE_SIZE + tableDirSize;
            int restBytesToRead = fontDataSize - start;
            int totalBytesRead = in.read(fontData, start, restBytesToRead);
            fileLength += totalBytesRead;
            if (totalBytesRead != restBytesToRead) {
                throw new TrueTypeBusinessException("Could not read " + restBytesToRead + " bytes of tableDir information! Instead, read returned " + totalBytesRead + " (File length: " + fileLength + ")");
            }
        } catch (IOException e) {
            throw new TrueTypeTechnicalException("IOException!" + e.getMessage());
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Searches for requested tag in table-dir,
        and locates its param-checksum, param-offset and param-length for tag-table.
        This function, however, returns only the param-offset.
     */
    private int getTableDirEntryParamOffset(long tag) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            int pos = TTConstants.OFFSET_TABLE_SIZE;
            for (int i = 0; i < numTables; i++) {
                long currentTag = Bitwise.toULONG(fontData[pos], fontData[pos + 1], fontData[pos + 2], fontData[pos + 3]);
                if (currentTag == tag) {
                    return Bitwise.toULONG(fontData[pos + 8], fontData[pos + 9], fontData[pos + 10], fontData[pos + 11]);
                }
                pos += 16;
            }
            throw new TrueTypeBusinessException("TableDirEntry with tag=" + tag + " NOT found!");
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'head' table.
        Retrieves indexToLocFormat (0 or 1), and unitsPerMSqr.
     */
    private void processFontHeaderTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'h', (byte) 'e', (byte) 'a', (byte) 'd');
            int pos = getTableDirEntryParamOffset(tag);
            pos += TTConstants.SIZE_OF_FIXED;
            pos += TTConstants.SIZE_OF_FIXED;
            pos += TTConstants.SIZE_OF_ULONG;
            pos += TTConstants.SIZE_OF_ULONG;
            pos += TTConstants.SIZE_OF_USHORT;
            unitsPerMSqr = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            pos += 8;
            pos += 8;
            xMin = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            yMin = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            xMax = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            yMax = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            indexToLocFormat = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'maxp' table.
        Retrieves numGlyphs.
     */
    private void processMaximumProfileTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'm', (byte) 'a', (byte) 'x', (byte) 'p');
            int pos = getTableDirEntryParamOffset(tag);
            pos += TTConstants.SIZE_OF_FIXED;
            numGlyphs = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'name' table.
        Retrieves font name-fields, according to platform and language.
     */
    private void processNamingTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'n', (byte) 'a', (byte) 'm', (byte) 'e');
            int paramOffset = getTableDirEntryParamOffset(tag);
            int pos = paramOffset;
            pos += TTConstants.SIZE_OF_USHORT;
            short numNameRecords = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            int stringStorageOffset = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            stringStorageOffset += paramOffset;
            boolean processedCopyright = false;
            boolean processedFamily = false;
            boolean processedSubfamily = false;
            boolean processedUniqueId = false;
            boolean processedFullName = false;
            boolean processedVersion = false;
            for (int i = 0; i < numNameRecords; i++) {
                short currentPlatformID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentSpecificID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentLanguageID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentNameID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentStringLength = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentStringOffset = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                if (currentPlatformID != platformID || currentSpecificID != specificID || currentLanguageID != languageID) {
                    continue;
                }
                boolean nameFound = false;
                switch(currentNameID) {
                    case TTConstants.NAME_ID_COPYRIGHT:
                        if (!processedCopyright) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_FAMILY:
                        if (!processedFamily) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_SUBFAMILY:
                        if (!processedSubfamily) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_UNIQUEID:
                        if (!processedUniqueId) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_FULLNAME:
                        if (!processedFullName) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_VERSION:
                        if (!processedVersion) {
                            nameFound = true;
                        }
                        break;
                }
                if (nameFound) {
                    byte[] stringToFill;
                    if (currentPlatformID == TTConstants.PLATFORM_ID_MICROSOFT) {
                        stringToFill = new byte[currentStringLength / 2 + 1];
                        int c;
                        for (c = 1; c < currentStringLength; c += 2) {
                            stringToFill[c / 2] = fontData[stringStorageOffset + currentStringOffset + c];
                        }
                        stringToFill[c / 2] = 0;
                    } else {
                        stringToFill = new byte[currentStringLength + 1];
                        int c;
                        for (c = 0; c < currentStringLength; c++) {
                            stringToFill[c] = fontData[stringStorageOffset + currentStringOffset + c];
                        }
                        stringToFill[c] = 0;
                    }
                    switch(currentNameID) {
                        case TTConstants.NAME_ID_COPYRIGHT:
                            copyright = stringToFill;
                            break;
                        case TTConstants.NAME_ID_FAMILY:
                            familyName = stringToFill;
                            break;
                        case TTConstants.NAME_ID_SUBFAMILY:
                            subfamilyName = stringToFill;
                            break;
                        case TTConstants.NAME_ID_UNIQUEID:
                            uniqueName = stringToFill;
                            break;
                        case TTConstants.NAME_ID_FULLNAME:
                            fullName = stringToFill;
                            break;
                        case TTConstants.NAME_ID_VERSION:
                            versionName = stringToFill;
                            break;
                    }
                }
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'name' table (simplified - no interaction required).
        Retrieves font name-fields, according to first found platform and language.
     */
    private void processNamingTableNoQuestionsAsked() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'n', (byte) 'a', (byte) 'm', (byte) 'e');
            int paramOffset = getTableDirEntryParamOffset(tag);
            int pos = paramOffset;
            pos += TTConstants.SIZE_OF_USHORT;
            short numNameRecords = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            int stringStorageOffset = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            stringStorageOffset += paramOffset;
            boolean processedCopyright = false;
            boolean processedFamily = false;
            boolean processedSubfamily = false;
            boolean processedUniqueId = false;
            boolean processedFullName = false;
            boolean processedVersion = false;
            for (int i = 0; i < numNameRecords; i++) {
                short currentPlatformID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentNameID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentStringLength = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentStringOffset = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                boolean nameFound = false;
                switch(currentNameID) {
                    case TTConstants.NAME_ID_COPYRIGHT:
                        if (!processedCopyright) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_FAMILY:
                        if (!processedFamily) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_SUBFAMILY:
                        if (!processedSubfamily) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_UNIQUEID:
                        if (!processedUniqueId) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_FULLNAME:
                        if (!processedFullName) {
                            nameFound = true;
                        }
                        break;
                    case TTConstants.NAME_ID_VERSION:
                        if (!processedVersion) {
                            nameFound = true;
                        }
                        break;
                }
                if (nameFound) {
                    byte[] stringToFill;
                    if (currentPlatformID == TTConstants.PLATFORM_ID_MICROSOFT) {
                        stringToFill = new byte[currentStringLength / 2 + 1];
                        int c;
                        for (c = 1; c < currentStringLength; c += 2) {
                            stringToFill[c / 2] = fontData[stringStorageOffset + currentStringOffset + c];
                        }
                        stringToFill[c / 2] = 0;
                    } else {
                        stringToFill = new byte[currentStringLength + 1];
                        int c;
                        for (c = 0; c < currentStringLength; c++) {
                            stringToFill[c] = fontData[stringStorageOffset + currentStringOffset + c];
                        }
                        stringToFill[c] = 0;
                    }
                    switch(currentNameID) {
                        case TTConstants.NAME_ID_COPYRIGHT:
                            copyright = stringToFill;
                            processedCopyright = true;
                            break;
                        case TTConstants.NAME_ID_FAMILY:
                            familyName = stringToFill;
                            processedFamily = true;
                            break;
                        case TTConstants.NAME_ID_SUBFAMILY:
                            subfamilyName = stringToFill;
                            processedSubfamily = true;
                            break;
                        case TTConstants.NAME_ID_UNIQUEID:
                            uniqueName = stringToFill;
                            processedUniqueId = true;
                            break;
                        case TTConstants.NAME_ID_FULLNAME:
                            fullName = stringToFill;
                            processedFullName = true;
                            break;
                        case TTConstants.NAME_ID_VERSION:
                            versionName = stringToFill;
                            processedVersion = true;
                            break;
                    }
                }
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'loca' table.
        Retrieves glyphOffsetArray.
     */
    private void processIndexToLocationTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'l', (byte) 'o', (byte) 'c', (byte) 'a');
            int pos = getTableDirEntryParamOffset(tag);
            glyphOffsetArray = new int[numGlyphs + 1];
            for (int i = 0; i < numGlyphs + 1; i++) {
                int currentOffset;
                if (indexToLocFormat == 0) {
                    currentOffset = Bitwise.toULONG((byte) 0, (byte) 0, fontData[pos], fontData[pos + 1]);
                    currentOffset *= 2;
                    pos += TTConstants.SIZE_OF_USHORT;
                } else {
                    currentOffset = Bitwise.toULONG(fontData[pos], fontData[pos + 1], fontData[pos + 2], fontData[pos + 3]);
                    pos += TTConstants.SIZE_OF_ULONG;
                }
                glyphOffsetArray[i] = currentOffset;
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'hhea' table.
        Retrieves ascender, descender, lineGap, numberOfHMetrics (for 'hmtx' table).
     */
    private void processHorizontalHeaderTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'h', (byte) 'h', (byte) 'e', (byte) 'a');
            int pos = getTableDirEntryParamOffset(tag);
            pos += TTConstants.SIZE_OF_ULONG;
            ascender = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            descender = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            lineGap = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            pos += 5 * TTConstants.SIZE_OF_SHORT;
            pos += TTConstants.SIZE_OF_SHORT;
            numberOfHMetrics = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'hmtx' table.
        Retrieves (for all glyphs): advanceWidth, leftSideBearing, rightSideBearing.
     */
    private void processHorizontalMetricsTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'h', (byte) 'm', (byte) 't', (byte) 'x');
            int pos = getTableDirEntryParamOffset(tag);
            int g;
            for (g = 0; g < numberOfHMetrics; g++) {
                glyphs[g].advanceWidth = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                glyphs[g].leftSideBearing = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                glyphs[g].rightSideBearing = (short) (glyphs[g].advanceWidth - glyphs[g].leftSideBearing - glyphs[g].xMax + glyphs[g].xMin);
            }
            if (g == numGlyphs) {
                return;
            }
            short lastAdvanceWidth = glyphs[g].advanceWidth;
            for (int i = numberOfHMetrics; i < numGlyphs; i++) {
                glyphs[i].advanceWidth = lastAdvanceWidth;
                glyphs[i].leftSideBearing = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                glyphs[i].rightSideBearing = (short) (glyphs[i].advanceWidth - glyphs[i].leftSideBearing - glyphs[i].xMax + glyphs[i].xMin);
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'kern' table.
        Retrieves number of kernPairs.
        Retrieves (for all kernPairs): left, right and value.
     */
    private void processKerningTable() throws TrueTypeKerningBusinessException, TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'k', (byte) 'e', (byte) 'r', (byte) 'n');
            int pos = getTableDirEntryParamOffset(tag);
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            byte coverageHi = fontData[pos];
            byte coverageLo = fontData[pos + 1];
            pos += TTConstants.SIZE_OF_USHORT;
            if (Bitwise.isBitSet(coverageLo, 1) || coverageHi != 0) {
                throw new TrueTypeKerningBusinessException("Unknown Kern-format!");
            }
            numKernPairs = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            pos += TTConstants.SIZE_OF_USHORT;
            kernPairs = new TTKernPair[numKernPairs];
            for (int i = 0; i < numKernPairs; i++) {
                kernPairs[i] = new TTKernPair();
            }
            for (int i = 0; i < numKernPairs; i++) {
                kernPairs[i].left = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                kernPairs[i].right = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                kernPairs[i].value = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
            }
        } catch (TrueTypeKerningBusinessException e) {
            throw e;
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'cmap' table.
        Retrieves platform-specific options stored in TTF
     */
    private void retrieveSpecificList() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'c', (byte) 'm', (byte) 'a', (byte) 'p');
            int paramOffset = getTableDirEntryParamOffset(tag);
            int pos = paramOffset;
            pos += TTConstants.SIZE_OF_USHORT;
            short numSubtables = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            if (numSubtables <= 0) {
                throw new TrueTypeBusinessException("Character map NOT found! (numTables must be > 0)");
            }
            specificList = new String[numSubtables];
            PIDList = new short[numSubtables];
            SIDList = new short[numSubtables];
            specificListTotal = 0;
            boolean subtableFound = false;
            int i = 0;
            while (!subtableFound && i < numSubtables) {
                short currentPlatformID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentSpecificID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                Bitwise.toULONG(fontData[pos], fontData[pos + 1], fontData[pos + 2], fontData[pos + 3]);
                pos += TTConstants.SIZE_OF_ULONG;
                String tmpStr = null;
                if (currentPlatformID >= TTConstants.MIN_PLATFORM && currentPlatformID <= TTConstants.MAX_PLATFORM) {
                    if (currentPlatformID == TTConstants.PLATFORM_ID_APPLEUNICODE) {
                        tmpStr = TTConstants.PLATFORM_NAMES[currentPlatformID];
                    } else if (currentPlatformID == TTConstants.PLATFORM_ID_MACINTOSH) {
                        if (currentSpecificID >= TTConstants.MIN_MAC_SPECIFIC && currentSpecificID <= TTConstants.MAX_MAC_SPECIFIC) {
                            tmpStr = TTConstants.PLATFORM_NAMES[currentPlatformID] + "/" + TTConstants.MAC_SPECIFIC_NAMES[currentSpecificID];
                        }
                    } else if (currentPlatformID == TTConstants.PLATFORM_ID_ISO) {
                        if (currentSpecificID >= TTConstants.MIN_ISO_SPECIFIC && currentSpecificID <= TTConstants.MAX_ISO_SPECIFIC) {
                            tmpStr = TTConstants.PLATFORM_NAMES[currentPlatformID] + "/" + TTConstants.ISO_SPECIFIC_NAMES[currentSpecificID];
                        }
                    } else {
                        if (currentSpecificID >= TTConstants.MIN_MS_SPECIFIC && currentSpecificID <= TTConstants.MAX_MS_SPECIFIC) {
                            tmpStr = TTConstants.PLATFORM_NAMES[currentPlatformID] + "/" + TTConstants.MS_SPECIFIC_NAMES[currentSpecificID];
                        }
                    }
                    if (tmpStr == null) {
                        tmpStr = TTConstants.PLATFORM_NAMES[currentPlatformID] + "/" + "Unknown";
                    }
                } else {
                    tmpStr = "Unknown" + "/" + "Unknown";
                }
                int cmapPos = paramOffset + 0;
                cmapFormat = Bitwise.toUSHORT(fontData[cmapPos], fontData[cmapPos + 1]);
                if (cmapFormat == TTConstants.CMAP_FORMAT0 || cmapFormat == TTConstants.CMAP_FORMAT4) {
                    specificList[specificListTotal] = tmpStr;
                    PIDList[specificListTotal] = currentPlatformID;
                    SIDList[specificListTotal] = currentSpecificID;
                    specificListTotal++;
                }
                i++;
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Analyzing 'cmap' table.
        Retrieves platform-specific character encoding subtable into cmap array.
     */
    private void processCharacterMappingTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'c', (byte) 'm', (byte) 'a', (byte) 'p');
            int paramOffset = getTableDirEntryParamOffset(tag);
            int pos = paramOffset;
            pos += TTConstants.SIZE_OF_USHORT;
            short numSubtables = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            int subtableOffset = 0;
            boolean subtableFound = false;
            int i = 0;
            while (!subtableFound && i < numSubtables) {
                short currentPlatformID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                short currentSpecificID = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                int currentSubtableOffset = Bitwise.toULONG(fontData[pos], fontData[pos + 1], fontData[pos + 2], fontData[pos + 3]);
                pos += TTConstants.SIZE_OF_ULONG;
                if (currentPlatformID == platformID && currentSpecificID == specificID) {
                    subtableOffset = currentSubtableOffset;
                    subtableFound = true;
                }
                i++;
            }
            if (!subtableFound) {
                throw new TrueTypeBusinessException("Character map subtable NOT found! (numSubtables=" + numSubtables + "; platformID=" + platformID + "; specificID=" + specificID + ")");
            }
            pos = paramOffset + subtableOffset;
            cmapFormat = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            short subtableLength = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
            pos += TTConstants.SIZE_OF_USHORT;
            if (cmapFormat != TTConstants.CMAP_FORMAT0 && cmapFormat != TTConstants.CMAP_FORMAT4) {
                throw new TrueTypeBusinessException("Unsupported character map! Only formats TTConstants.CMAP_FORMAT0 and TTConstants.CMAP_FORMAT4 are supported. (" + cmapFormat + ")");
            }
            cmap = new byte[subtableLength];
            for (int k = 0; k < subtableLength; k++) {
                cmap[k] = fontData[paramOffset + subtableOffset + k];
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Building glyph/contour structure. Result is the glyph array for object.
     */
    private void processGlyphDataTable() throws TrueTypeBusinessException, TrueTypeTechnicalException {
        try {
            long tag = Bitwise.toULONG((byte) 'g', (byte) 'l', (byte) 'y', (byte) 'f');
            int paramOffset = getTableDirEntryParamOffset(tag);
            glyphs = new TTGlyph[numGlyphs];
            for (int gi = 0; gi < numGlyphs; gi++) {
                glyphs[gi] = new TTGlyph();
            }
            for (int gi = 0; gi < numGlyphs; gi++) {
                int currGlyphOffset = glyphOffsetArray[gi];
                int nextGlyphOffset = glyphOffsetArray[gi + 1];
                int currGlyphLength = nextGlyphOffset - currGlyphOffset;
                if (currGlyphLength == 0) {
                    continue;
                }
                int pos = paramOffset + currGlyphOffset;
                glyphs[gi].numContours = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                short numberOfContours = glyphs[gi].numContours;
                glyphs[gi].xMin = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                glyphs[gi].yMin = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                glyphs[gi].xMax = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                glyphs[gi].yMax = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_SHORT;
                if (numberOfContours < 0) {
                    glyphs[gi].numContours = 0;
                    glyphs[gi].contour = null;
                    continue;
                }
                short[] endPtsOfContours = new short[numberOfContours];
                for (int i = 0; i < numberOfContours; i++) {
                    endPtsOfContours[i] = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                    pos += TTConstants.SIZE_OF_USHORT;
                }
                glyphs[gi].contour = new TTContour[numberOfContours];
                for (int i = 0; i < numberOfContours; i++) {
                    glyphs[gi].contour[i] = new TTContour();
                }
                short numberOfPoints = (short) (endPtsOfContours[numberOfContours - 1] + 1);
                short instructionLength = Bitwise.toUSHORT(fontData[pos], fontData[pos + 1]);
                pos += TTConstants.SIZE_OF_USHORT;
                pos += instructionLength;
                byte[] flags = new byte[numberOfPoints];
                for (int i = 0; i < numberOfPoints; i++) {
                    flags[i] = fontData[pos];
                    pos++;
                    if (Bitwise.isBitSet(flags[i], 3)) {
                        short repeatCount = fontData[pos];
                        if (repeatCount < 0) {
                            repeatCount = (short) (256 + repeatCount);
                        }
                        pos++;
                        for (; repeatCount > 0; repeatCount--) {
                            i++;
                            flags[i] = flags[i - 1];
                        }
                    }
                }
                short startPoint = 0;
                short endPoint;
                for (int i = 0; i < numberOfContours; i++) {
                    endPoint = endPtsOfContours[i];
                    glyphs[gi].contour[i].numPoints = (short) (endPoint - startPoint + 1);
                    glyphs[gi].contour[i].point = new TTPoint[endPoint - startPoint + 1];
                    for (int j = startPoint; j <= endPoint; j++) {
                        glyphs[gi].contour[i].point[j - startPoint] = new TTPoint();
                    }
                    for (int j = startPoint; j <= endPoint; j++) {
                        byte currentFlag = flags[j];
                        if (Bitwise.isBitSet(currentFlag, 0)) {
                            glyphs[gi].contour[i].point[j - startPoint].type = TTPoint.TYPE_ON_CURVE;
                        } else {
                            glyphs[gi].contour[i].point[j - startPoint].type = TTPoint.TYPE_OFF_CURVE;
                        }
                        if (Bitwise.isBitSet(currentFlag, 1)) {
                            short xByte = fontData[pos];
                            if (xByte < 0) {
                                xByte = (short) (256 + xByte);
                            }
                            pos++;
                            if (Bitwise.isBitSet(currentFlag, 4)) {
                                glyphs[gi].contour[i].point[j - startPoint].x = xByte;
                            } else {
                                glyphs[gi].contour[i].point[j - startPoint].x = (short) (-xByte);
                            }
                        } else {
                            if (Bitwise.isBitSet(currentFlag, 4)) {
                                glyphs[gi].contour[i].point[j - startPoint].x = 0;
                            } else {
                                short xWord = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                                pos += TTConstants.SIZE_OF_SHORT;
                                glyphs[gi].contour[i].point[j - startPoint].x = xWord;
                            }
                        }
                    }
                    startPoint = (short) (endPoint + 1);
                }
                startPoint = 0;
                for (int i = 0; i < numberOfContours; i++) {
                    endPoint = endPtsOfContours[i];
                    for (int j = startPoint; j <= endPoint; j++) {
                        byte currentFlag = flags[j];
                        if (Bitwise.isBitSet(currentFlag, 2)) {
                            short yByte = fontData[pos];
                            if (yByte < 0) {
                                yByte = (short) (256 + yByte);
                            }
                            pos++;
                            if (Bitwise.isBitSet(currentFlag, 5)) {
                                glyphs[gi].contour[i].point[j - startPoint].y = yByte;
                            } else {
                                glyphs[gi].contour[i].point[j - startPoint].y = (short) (-yByte);
                            }
                        } else {
                            if (Bitwise.isBitSet(currentFlag, 5)) {
                                glyphs[gi].contour[i].point[j - startPoint].y = 0;
                            } else {
                                short yWord = Bitwise.toSHORT(fontData[pos], fontData[pos + 1]);
                                pos += TTConstants.SIZE_OF_SHORT;
                                glyphs[gi].contour[i].point[j - startPoint].y = yWord;
                            }
                        }
                    }
                    startPoint = (short) (endPoint + 1);
                }
            }
            for (int i = 0; i < numGlyphs; i++) {
                short tx = 0;
                short ty = 0;
                if (glyphs[i].numContours > 0) {
                    for (int j = 0; j < glyphs[i].numContours; j++) {
                        for (int k = 0; k < glyphs[i].contour[j].numPoints; k++) {
                            tx += glyphs[i].contour[j].point[k].x;
                            ty += glyphs[i].contour[j].point[k].y;
                            glyphs[i].contour[j].point[k].x = tx;
                            glyphs[i].contour[j].point[k].y = ty;
                        }
                    }
                }
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Search through cmap array to find glyph mapped to char c.
     */
    public short getGlyphIndex(short c) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        try {
            if (cmap == null) {
                throw new TrueTypeBusinessException("Character map is NULL!");
            }
            switch(cmapFormat) {
                case TTConstants.CMAP_FORMAT0:
                    int glyphIdArrayCMAPindex = 6;
                    short result;
                    if (c < 256) {
                        result = (short) cmap[glyphIdArrayCMAPindex + c];
                    } else {
                        result = 0;
                    }
                    if (result < 0) {
                        result = (short) (256 + result);
                    }
                    return result;
                case TTConstants.CMAP_FORMAT4:
                    short segCount = (short) (Bitwise.toUSHORT(cmap[6], cmap[7]) / 2);
                    int endCountCMAPindex = 14;
                    int startCountCMAPindex = 16 + 2 * segCount;
                    int idDeltaCMAPindex = 16 + 4 * segCount;
                    int idRangeOffsetCMAPindex = 16 + 6 * segCount;
                    int seg = 0;
                    int end = Bitwise.toUSHORT(cmap[endCountCMAPindex], cmap[endCountCMAPindex + 1]);
                    while (end < c) {
                        seg++;
                        end = Bitwise.toUSHORT(cmap[endCountCMAPindex + seg * 2], cmap[endCountCMAPindex + seg * 2 + 1]);
                    }
                    int start = Bitwise.toUSHORT(cmap[startCountCMAPindex + seg * 2], cmap[startCountCMAPindex + seg * 2 + 1]);
                    int delta = Bitwise.toUSHORT(cmap[idDeltaCMAPindex + seg * 2], cmap[idDeltaCMAPindex + seg * 2 + 1]);
                    int range = Bitwise.toUSHORT(cmap[idRangeOffsetCMAPindex + seg * 2], cmap[idRangeOffsetCMAPindex + seg * 2 + 1]);
                    if (start > c) {
                        return 0;
                    }
                    int index;
                    if (range == 0) {
                        index = (short) c + (short) delta;
                    } else {
                        index = range + (c - start) * 2 + ((int) (16 + 6 * segCount) + seg * 2);
                        index = Bitwise.toUSHORT(cmap[index], cmap[index + 1]);
                        if (index != 0) {
                            index = (short) index + (short) delta;
                        }
                    }
                    return (short) index;
            }
            return 0;
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    public short getUnitsPerMSqr() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return unitsPerMSqr;
    }

    public short getXMax() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return xMax;
    }

    public short getXMin() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return xMin;
    }

    public short getYMax() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return yMax;
    }

    public short getYMin() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return yMin;
    }

    public short getAscender() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return ascender;
    }

    public short getDescender() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return descender;
    }

    public short getLineGap() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return lineGap;
    }

    public byte[] getCopyright() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return copyright;
    }

    public byte[] getFamilyName() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return familyName;
    }

    public byte[] getFullName() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return fullName;
    }

    public byte[] getSubfamilyName() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return subfamilyName;
    }

    public byte[] getUniqueName() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return uniqueName;
    }

    public byte[] getVersionName() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return versionName;
    }

    public short getNumGlyphs() {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return numGlyphs;
    }

    public short getNumContours(short glyphIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs) {
            return glyphs[glyphIndex].numContours;
        } else {
            return 0;
        }
    }

    public short getNumPoints(short glyphIndex, short contourIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs && glyphs[glyphIndex].numContours > 0 && contourIndex < glyphs[glyphIndex].numContours) {
            return glyphs[glyphIndex].contour[contourIndex].numPoints;
        } else {
            return 0;
        }
    }

    public int getFontPointX(short glyphIndex, short contourIndex, short pointIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs && glyphs[glyphIndex].numContours > 0 && contourIndex < glyphs[glyphIndex].numContours && glyphs[glyphIndex].contour[contourIndex].numPoints > 0) {
            return glyphs[glyphIndex].contour[contourIndex].point[pointIndex].x;
        } else {
            return 0;
        }
    }

    public int getFontPointY(short glyphIndex, short contourIndex, short pointIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs && glyphs[glyphIndex].numContours > 0 && contourIndex < glyphs[glyphIndex].numContours && glyphs[glyphIndex].contour[contourIndex].numPoints > 0) {
            return glyphs[glyphIndex].contour[contourIndex].point[pointIndex].y;
        } else {
            return 0;
        }
    }

    public short getFontPointType(short glyphIndex, short contourIndex, short pointIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs && glyphs[glyphIndex].numContours > 0 && contourIndex < glyphs[glyphIndex].numContours && glyphs[glyphIndex].contour[contourIndex].numPoints > 0) {
            return glyphs[glyphIndex].contour[contourIndex].point[pointIndex].type;
        } else {
            return TTPoint.TYPE_NONE;
        }
    }

    public short getGlyphAdvanceWidth(short glyphIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs) {
            return glyphs[glyphIndex].advanceWidth;
        } else {
            return 0;
        }
    }

    public short getGlyphXMin(short glyphIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs) {
            return glyphs[glyphIndex].xMin;
        } else {
            return 0;
        }
    }

    public short getGlyphYMin(short glyphIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs) {
            return glyphs[glyphIndex].yMin;
        } else {
            return 0;
        }
    }

    public short getGlyphXMax(short glyphIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs) {
            return glyphs[glyphIndex].xMax;
        } else {
            return 0;
        }
    }

    public short getGlyphYMax(short glyphIndex) {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        if (numGlyphs > 0 && 0 <= glyphIndex && glyphIndex < numGlyphs) {
            return glyphs[glyphIndex].yMax;
        } else {
            return 0;
        }
    }

    public short mapCharacterToGlyph(short c) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        return getGlyphIndex(c);
    }

    /**
        Returns kernPair value if match is found.
     */
    public short findKerning(short idx1, short idx2) throws TrueTypeTechnicalException {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        try {
            if (numKernPairs == 0) {
                return 0;
            }
            int combined = idx1 * 0x10000 + idx2;
            int beg = 0;
            int end = numKernPairs;
            int mid = 0;
            boolean found = false;
            while (!found && beg <= end) {
                mid = (end + beg) / 2;
                short currentLeft = kernPairs[mid].left;
                short currentRight = kernPairs[mid].right;
                int currentCombined = currentLeft * 0x10000 + currentRight;
                if (combined == currentCombined) {
                    found = true;
                    break;
                }
                if (combined < currentCombined) {
                    end = mid - 1;
                } else {
                    beg = mid + 1;
                }
            }
            if (found == true) {
                return kernPairs[mid].value;
            } else {
                return 0;
            }
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }

    /**
        Returns glyph object mapped to a char with the given code.
     */
    public TTGlyph getGlyph(short code) throws TrueTypeBusinessException, TrueTypeTechnicalException {
        if (!loadingCompleted) {
            throw new IllegalStateException("Loading NOT completed!");
        }
        try {
            if (glyphs == null) {
                return null;
            } else {
                return glyphs[getGlyphIndex(code)];
            }
        } catch (TrueTypeBusinessException e) {
            throw e;
        } catch (TrueTypeTechnicalException e) {
            throw e;
        } catch (Throwable t) {
            throw new TrueTypeTechnicalException("Throwable!" + t.getMessage());
        }
    }
}
