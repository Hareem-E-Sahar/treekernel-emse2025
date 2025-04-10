public class Test {    public OggVorbisDialog(JFrame parent, String title, OggVorbisInfo mi) {
        super(parent, title);
        initComponents();
        _vorbisinfo = mi;
        int size = _vorbisinfo.getLocation().length();
        locationLabel.setText(size > 50 ? ("..." + _vorbisinfo.getLocation().substring(size - 50)) : _vorbisinfo.getLocation());
        if ((_vorbisinfo.getTitle() != null) && ((!_vorbisinfo.getTitle().equals("")))) textField.append("Title=" + _vorbisinfo.getTitle() + "\n");
        if ((_vorbisinfo.getArtist() != null) && ((!_vorbisinfo.getArtist().equals("")))) textField.append("Artist=" + _vorbisinfo.getArtist() + "\n");
        if ((_vorbisinfo.getAlbum() != null) && ((!_vorbisinfo.getAlbum().equals("")))) textField.append("Album=" + _vorbisinfo.getAlbum() + "\n");
        if (_vorbisinfo.getTrack() > 0) textField.append("Track=" + _vorbisinfo.getTrack() + "\n");
        if ((_vorbisinfo.getYear() != null) && ((!_vorbisinfo.getYear().equals("")))) textField.append("Year=" + _vorbisinfo.getYear() + "\n");
        if ((_vorbisinfo.getGenre() != null) && ((!_vorbisinfo.getGenre().equals("")))) textField.append("Genre=" + _vorbisinfo.getGenre() + "\n");
        java.util.List comments = _vorbisinfo.getComment();
        for (int i = 0; i < comments.size(); i++) textField.append(comments.get(i) + "\n");
        int secondsAmount = Math.round(_vorbisinfo.getPlayTime());
        if (secondsAmount < 0) secondsAmount = 0;
        int minutes = secondsAmount / 60;
        int seconds = secondsAmount - (minutes * 60);
        lengthLabel.setText("Length : " + minutes + ":" + seconds);
        bitrateLabel.setText("Average bitrate : " + _vorbisinfo.getAverageBitrate() / 1000 + " kbps");
        DecimalFormat df = new DecimalFormat("#,###,###");
        sizeLabel.setText("File size : " + df.format(_vorbisinfo.getSize()) + " bytes");
        nominalbitrateLabel.setText("Nominal bitrate : " + (_vorbisinfo.getBitRate() / 1000) + " kbps");
        maxbitrateLabel.setText("Max bitrate : " + _vorbisinfo.getMaxBitrate() / 1000 + " kbps");
        minbitrateLabel.setText("Min bitrate : " + _vorbisinfo.getMinBitrate() / 1000 + " kbps");
        channelsLabel.setText("Channel : " + _vorbisinfo.getChannels());
        samplerateLabel.setText("Sampling rate : " + _vorbisinfo.getSamplingRate() + " Hz");
        serialnumberLabel.setText("Serial number : " + _vorbisinfo.getSerial());
        versionLabel.setText("Version : " + _vorbisinfo.getVersion());
        vendorLabel.setText("Vendor : " + _vorbisinfo.getVendor());
        buttonsPanel.add(_close);
        pack();
    }
}