public class Test {    public static String formatToStr(Format f) {
        if (f == null) return "null";
        final Class c = f.getClass();
        if (c == RGBFormat.class) {
            final RGBFormat o = (RGBFormat) f;
            return "new RGBFormat(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getBitsPerPixel() + ", " + CGUtils.toHexLiteral(o.getRedMask()) + ", " + CGUtils.toHexLiteral(o.getGreenMask()) + ", " + CGUtils.toHexLiteral(o.getBlueMask()) + ", " + o.getPixelStride() + ", " + o.getLineStride() + ", " + o.getFlipped() + ", " + o.getEndian() + ")";
        } else if (c == YUVFormat.class) {
            final YUVFormat o = (YUVFormat) f;
            return "new YUVFormat(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getYuvType() + ", " + o.getStrideY() + ", " + o.getStrideUV() + ", " + o.getOffsetY() + ", " + o.getOffsetU() + ", " + o.getOffsetV() + ")";
        } else if (c == JPEGFormat.class) {
            final JPEGFormat o = (JPEGFormat) f;
            return "new JPEGFormat(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getQFactor() + ", " + o.getDecimation() + ")";
        } else if (c == IndexedColorFormat.class) {
            final IndexedColorFormat o = (IndexedColorFormat) f;
            return "new IndexedColorFormat(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getLineStride() + ", " + o.getMapSize() + ", " + CGUtils.toLiteral(o.getRedValues()) + ", " + CGUtils.toLiteral(o.getGreenValues()) + ", " + CGUtils.toLiteral(o.getBlueValues()) + ")";
        } else if (c == H263Format.class) {
            final H263Format o = (H263Format) f;
            return "new H263Format(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getAdvancedPrediction() + ", " + o.getArithmeticCoding() + ", " + o.getErrorCompensation() + ", " + o.getHrDB() + ", " + o.getPBFrames() + ", " + o.getUnrestrictedVector() + ")";
        } else if (c == H261Format.class) {
            final H261Format o = (H261Format) f;
            return "new H261Format(" + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + o.getStillImageTransmission() + ")";
        } else if (c == AudioFormat.class) {
            final AudioFormat o = (AudioFormat) f;
            return "new AudioFormat(" + CGUtils.toLiteral(o.getEncoding()) + ", " + CGUtils.toLiteral(o.getSampleRate()) + ", " + CGUtils.toLiteral(o.getSampleSizeInBits()) + ", " + CGUtils.toLiteral(o.getChannels()) + ", " + CGUtils.toLiteral(o.getEndian()) + ", " + CGUtils.toLiteral(o.getSigned()) + ", " + CGUtils.toLiteral(o.getFrameSizeInBits()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ", " + dataTypeToStr(o.getDataType()) + ")";
        } else if (c == VideoFormat.class) {
            final VideoFormat o = (VideoFormat) f;
            return "new VideoFormat(" + CGUtils.toLiteral(o.getEncoding()) + ", " + toLiteral(o.getSize()) + ", " + o.getMaxDataLength() + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getFrameRate()) + ")";
        } else if (c == Format.class) {
            final Format o = f;
            return "new Format(" + CGUtils.toLiteral(o.getEncoding()) + ", " + dataTypeToStr(o.getDataType()) + ")";
        } else if (c == FileTypeDescriptor.class) {
            final FileTypeDescriptor o = (FileTypeDescriptor) f;
            return "new FileTypeDescriptor(" + CGUtils.toLiteral(o.getEncoding()) + ")";
        } else if (c == ContentDescriptor.class) {
            final ContentDescriptor o = (ContentDescriptor) f;
            return "new ContentDescriptor(" + CGUtils.toLiteral(o.getEncoding()) + ")";
        } else if (c == com.sun.media.format.WavAudioFormat.class) {
            final com.sun.media.format.WavAudioFormat o = (com.sun.media.format.WavAudioFormat) f;
            return "new com.sun.media.format.WavAudioFormat(" + CGUtils.toLiteral(o.getEncoding()) + ", " + CGUtils.toLiteral(o.getSampleRate()) + ", " + CGUtils.toLiteral(o.getSampleSizeInBits()) + ", " + CGUtils.toLiteral(o.getChannels()) + ", " + CGUtils.toLiteral(o.getFrameSizeInBits()) + ", " + CGUtils.toLiteral(o.getAverageBytesPerSecond()) + ", " + CGUtils.toLiteral(o.getEndian()) + ", " + CGUtils.toLiteral(o.getSigned()) + ", " + CGUtils.toLiteral((float) o.getFrameRate()) + ", " + dataTypeToStr(o.getDataType()) + ", " + CGUtils.toLiteral(o.getCodecSpecificHeader()) + ")";
        } else {
            throw new IllegalArgumentException("" + f.getClass());
        }
    }
}