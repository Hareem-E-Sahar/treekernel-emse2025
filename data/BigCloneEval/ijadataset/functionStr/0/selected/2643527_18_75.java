public class Test {    public GenericAudioHeader read(RandomAccessFile raf) throws CannotReadException, IOException {
        GenericAudioHeader info = new GenericAudioHeader();
        logger.fine("Started");
        long oldPos;
        byte[] b = new byte[OggPageHeader.CAPTURE_PATTERN.length];
        raf.read(b);
        if (!(Arrays.equals(b, OggPageHeader.CAPTURE_PATTERN))) {
            throw new CannotReadException(ErrorMessage.OGG_HEADER_CANNOT_BE_FOUND.getMsg(new String(b)));
        }
        raf.seek(0);
        double pcmSamplesNumber = -1;
        raf.seek(raf.length() - 2);
        while (raf.getFilePointer() >= 4) {
            if (raf.read() == OggPageHeader.CAPTURE_PATTERN[3]) {
                raf.seek(raf.getFilePointer() - OggPageHeader.FIELD_CAPTURE_PATTERN_LENGTH);
                byte[] ogg = new byte[3];
                raf.readFully(ogg);
                if (ogg[0] == OggPageHeader.CAPTURE_PATTERN[0] && ogg[1] == OggPageHeader.CAPTURE_PATTERN[1] && ogg[2] == OggPageHeader.CAPTURE_PATTERN[2]) {
                    raf.seek(raf.getFilePointer() - 3);
                    oldPos = raf.getFilePointer();
                    raf.seek(raf.getFilePointer() + OggPageHeader.FIELD_PAGE_SEGMENTS_POS);
                    int pageSegments = raf.readByte() & 0xFF;
                    raf.seek(oldPos);
                    b = new byte[OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + pageSegments];
                    raf.readFully(b);
                    OggPageHeader pageHeader = new OggPageHeader(b);
                    raf.seek(0);
                    pcmSamplesNumber = pageHeader.getAbsoluteGranulePosition();
                    break;
                }
            }
            raf.seek(raf.getFilePointer() - 2);
        }
        if (pcmSamplesNumber == -1) {
            throw new CannotReadException(ErrorMessage.OGG_VORBIS_NO_SETUP_BLOCK.getMsg());
        }
        OggPageHeader pageHeader = OggPageHeader.read(raf);
        byte[] vorbisData = new byte[pageHeader.getPageLength()];
        raf.read(vorbisData);
        VorbisIdentificationHeader vorbisIdentificationHeader = new VorbisIdentificationHeader(vorbisData);
        info.setPreciseLength((float) (pcmSamplesNumber / vorbisIdentificationHeader.getSamplingRate()));
        info.setChannelNumber(vorbisIdentificationHeader.getChannelNumber());
        info.setSamplingRate(vorbisIdentificationHeader.getSamplingRate());
        info.setEncodingType(vorbisIdentificationHeader.getEncodingType());
        info.setExtraEncodingInfos("");
        if (vorbisIdentificationHeader.getNominalBitrate() != 0 && vorbisIdentificationHeader.getMaxBitrate() == vorbisIdentificationHeader.getNominalBitrate() && vorbisIdentificationHeader.getMinBitrate() == vorbisIdentificationHeader.getNominalBitrate()) {
            info.setBitrate(vorbisIdentificationHeader.getNominalBitrate() / 1000);
            info.setVariableBitRate(false);
        } else if (vorbisIdentificationHeader.getNominalBitrate() != 0 && vorbisIdentificationHeader.getMaxBitrate() == 0 && vorbisIdentificationHeader.getMinBitrate() == 0) {
            info.setBitrate(vorbisIdentificationHeader.getNominalBitrate() / 1000);
            info.setVariableBitRate(true);
        } else {
            info.setBitrate(computeBitrate(info.getTrackLength(), raf.length()));
            info.setVariableBitRate(true);
        }
        logger.fine("Finished");
        return info;
    }
}