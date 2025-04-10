package net.sf.javavp8decoder.imageio;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class WebPImageReaderSpi extends ImageReaderSpi {

    static final String[] extraImageMetadataFormatClassNames = null;

    static final String[] extraImageMetadataFormatNames = null;

    static final String[] extraStreamMetadataFormatClassNames = null;

    static final String[] extraStreamMetadataFormatNames = null;

    static final String[] MIMETypes = { "image/webp" };

    static final String[] names = { "webp" };

    static final String nativeImageMetadataFormatClassName = "net.sf.javavp8decoder.imageio.WebPMetadata_0.1";

    static final String nativeImageMetadataFormatName = "net.sf.javavp8decoder.imageio.WebPMetadata_0.1";

    static final String nativeStreamMetadataFormatClassName = null;

    static final String nativeStreamMetadataFormatName = null;

    static final String readerClassName = "net.sf.javavp8decoder.WebPImageReader";

    static final String[] suffixes = { "webp" };

    static final boolean supportsStandardImageMetadataFormat = false;

    static final boolean supportsStandardStreamMetadataFormat = false;

    static final String vendorName = "javavp8decoder";

    static final String version = "0.1";

    static final String[] writerSpiNames = { "net.sf.javavp8decoder.WebPImageReader" };

    public WebPImageReaderSpi() {
        super(vendorName, version, names, suffixes, MIMETypes, readerClassName, STANDARD_INPUT_TYPE, writerSpiNames, supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName, nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames, extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName, nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
    }

    public boolean canDecodeInput(Object input) {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream) input;
        byte[] b = new byte[8];
        try {
            stream.mark();
            stream.readFully(b);
            stream.reset();
        } catch (IOException e) {
            return false;
        }
        return (b[0] == (byte) 'R' && b[1] == (byte) 'I' && b[2] == (byte) 'F' && b[3] == (byte) 'F');
    }

    public ImageReader createReaderInstance(Object extension) {
        return new WebPImageReader(this);
    }

    public String getDescription(Locale locale) {
        return "Description goes here";
    }
}
