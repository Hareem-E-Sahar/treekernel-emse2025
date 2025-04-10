public class Test {    private static boolean checkConversion(AudioFormat srcFormat, AudioFormat targetFormat, boolean neg) {
        AudioInputStream srcStream = new AudioInputStream(new ByteArrayInputStream(new byte[0]), srcFormat, -1);
        boolean couldConvert = true;
        try {
            AudioInputStream targetStream = AudioSystem.getAudioInputStream(targetFormat, srcStream);
            targetFormat = targetStream.getFormat();
            if (!isSameBitsChannelSampleRate(srcFormat, targetFormat)) {
                System.out.println("ERROR");
                System.out.println("  converted stream has " + targetFormat.getChannels() + " channels, " + targetFormat.getSampleSizeInBits() + " bits, and " + targetFormat.getSampleRate() + "Hz, " + " but source stream had " + srcFormat.getChannels() + " channels, " + srcFormat.getSampleSizeInBits() + " bits, and " + srcFormat.getSampleRate() + "Hz");
                return false;
            }
        } catch (Exception e) {
            couldConvert = false;
        }
        if (couldConvert == neg) {
            System.out.println("ERROR");
            System.out.println("  can" + ((!couldConvert) ? "not" : "") + " convert from " + srcFormat + " to " + targetFormat);
            return false;
        }
        System.out.println("OK");
        return true;
    }
}