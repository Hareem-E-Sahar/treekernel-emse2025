package com.hadeslee.yoyoplayer.tag;

import com.hadeslee.audiotag.audio.flac.FlacTagWriter;
import com.hadeslee.audiotag.audio.flac.metadatablock.MetadataBlockDataPicture;
import com.hadeslee.audiotag.audio.mp3.MP3File;
import com.hadeslee.audiotag.audio.ogg.OggVorbisTagWriter;
import com.hadeslee.audiotag.tag.ape.APEv2Tag;
import com.hadeslee.audiotag.tag.flac.FlacTag;
import com.hadeslee.audiotag.tag.id3.ID3v1Tag;
import com.hadeslee.audiotag.tag.id3.ID3v23Tag;
import com.hadeslee.audiotag.tag.vorbiscomment.VorbisCommentTag;
import com.hadeslee.yoyoplayer.playlist.PlayListItem;
import com.hadeslee.yoyoplayer.util.Config;
import com.hadeslee.yoyoplayer.util.Util;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author  hadeslee
 */
public class SongInfoDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 20071214L;

    private static Logger log = Logger.getLogger(SongInfoDialog.class.getName());

    private PlayListItem item;

    private boolean valid;

    private static final int[] writeMode = { Config.WRITEMODE_ID3v1, Config.WRITEMODE_ID3v2, Config.WRITEMODE_APEv2, Config.WRITEMODE_APEv2 | Config.WRITEMODE_ID3v1, Config.WRITEMODE_ID3v1 | Config.WRITEMODE_ID3v2, Config.WRITEMODE_APEv2 | Config.WRITEMODE_ID3v2 };

    private static final int[] readOrder = { Config.APEv2_ID3v2_ID3v1, Config.ID3v2_APEv2_ID3v1, Config.ID3v1_APEv2_ID3v2, Config.ID3v1_ID3v2_APEv2 };

    /** Creates new form SongInfoDialog */
    public SongInfoDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initOther();
    }

    public SongInfoDialog(Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initOther();
    }

    public SongInfoDialog(Frame parent, boolean modal, PlayListItem item) {
        this(parent, modal);
        this.item = item;
        init();
    }

    public SongInfoDialog(Dialog parent, boolean modal, PlayListItem item) {
        this(parent, modal);
        this.item = item;
        init();
    }

    private void initOther() {
        int index = 0;
        for (int i = 0; i < readOrder.length; i++) {
            int order = Config.getConfig().getReadTagOrder();
            if (order == readOrder[i]) {
                index = i;
                break;
            }
        }
        jComboBox1.setSelectedIndex(index);
        index = 0;
        for (int i = 0; i < writeMode.length; i++) {
            int mode = Config.getConfig().getWriteTagMode();
            if (mode == writeMode[i]) {
                index = i;
                break;
            }
        }
        jComboBox2.setSelectedIndex(index);
        this.setLocationRelativeTo(null);
    }

    /**
     * 初始化
     */
    private void init() {
        try {
            fileName.setText(item.getLocation());
            title.setText(item.getTitle());
            artist.setText(item.getArtist());
            album.setText(item.getAlbum());
            genre.setText(item.getGenre());
            track.setText(item.getTrack());
            comment.setText(item.getComment());
            year.setText(item.getYear());
        } catch (Exception ex) {
            Logger.getLogger(SongInfoDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initComponents() {
        jLabel5 = new javax.swing.JLabel();
        fileName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        title = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        artist = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        album = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        track = new javax.swing.JTextField();
        year = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        genre = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        comment = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("歌曲信息");
        jLabel5.setText(Config.getResource("SongInfoDialog.fileName"));
        fileName.setEditable(false);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(Config.getResource("SongInfoDialog.tag")));
        jLabel1.setText(Config.getResource("SongInfoDialog.title"));
        jLabel2.setText(Config.getResource("SongInfoDialog.artist"));
        jLabel3.setText(Config.getResource("SongInfoDialog.album"));
        jLabel4.setText(Config.getResource("SongInfoDialog.year"));
        jLabel6.setText(Config.getResource("SongInfoDialog.track"));
        jLabel7.setText(Config.getResource("SongInfoDialog.genre"));
        jLabel8.setText(Config.getResource("SongInfoDialog.comment"));
        comment.setColumns(20);
        comment.setFont(new java.awt.Font("Dialog", 0, 12));
        comment.setLineWrap(true);
        comment.setRows(5);
        jScrollPane1.setViewportView(comment);
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(artist, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE).addContainerGap()).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel3).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(album, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel7).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(genre, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel4).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(year, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel6).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(track, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()).addGroup(jPanel1Layout.createSequentialGroup().addComponent(jLabel8).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE).addContainerGap()))));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(artist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(track, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel6).addComponent(album, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel4).addComponent(year, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel7).addComponent(genre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel8).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))));
        jButton1.setText(Config.getResource("SongInfoDialog.reReadFile"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton2.setText(Config.getResource("SongInfoDialog.saveToFile"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jButton3.setText(Config.getResource("SongInfoDialog.cancel"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(Config.getResource("SongInfoDialog.mp3tag")));
        jLabel9.setText(Config.getResource("SongInfoDialog.readOrder"));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "APEv2 > ID3v2 > ID3v1", "ID3v2 > APEv2 > ID3v1", "ID3v1 > APEv2 > ID3v2", "ID3v1 > ID3v2 > APEv2" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jLabel10.setText(Config.getResource("SongInfoDialog.writeMode"));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ID3v1", "ID3v2", "APEv2", "ID3v1&APEv2", "ID3v1&ID3v2", "ID3v2&APEv2" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel9).addContainerGap(203, Short.MAX_VALUE)).addComponent(jComboBox1, 0, 251, Short.MAX_VALUE).addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel10).addContainerGap()).addComponent(jComboBox2, 0, 251, Short.MAX_VALUE));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel9).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel10).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(jLabel5).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(fileName, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(jButton1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton3)).addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel5).addComponent(fileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jButton2).addComponent(jButton3).addComponent(jButton1)).addContainerGap()));
        pack();
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (title.getText() == null || title.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Config.getResource("title.empty"));
            return;
        }
        doSave();
        item.reRead();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        item.reRead();
        init();
    }

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        int index = jComboBox1.getSelectedIndex();
        Config.getConfig().setReadTagOrder(readOrder[index]);
    }

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {
        int index = jComboBox2.getSelectedIndex();
        Config.getConfig().setWriteTagMode(writeMode[index]);
    }

    private void doSave() {
        log.log(Level.INFO, "调用了doSave");
        item.setArtist(artist.getText());
        item.setAlbum(album.getText());
        item.setComment(comment.getText());
        item.setTitle(title.getText());
        item.setYear(year.getText());
        item.setGenre(genre.getText());
        item.setTrack(track.getText());
        item.refresh();
        if (item.isFile()) {
            try {
                String type = item.getType();
                if (type.equalsIgnoreCase("mp3")) {
                    log.log(Level.INFO, "写入MP3标签");
                    doSaveMP3();
                } else if (type.equalsIgnoreCase("flac")) {
                    log.log(Level.INFO, "写入FLAC格式标签");
                    doSaveFlac();
                } else if (type.equalsIgnoreCase("ogg")) {
                    log.log(Level.INFO, "写入Ogg标签");
                    doSaveOgg();
                } else if (type.equalsIgnoreCase("ape")) {
                    log.log(Level.INFO, "写入APE标签");
                    doSaveApe();
                } else {
                    log.log(Level.INFO, "忽略,不写入任何标签");
                }
            } catch (Exception ex) {
                Logger.getLogger(SongInfoDialog.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this, ex.toString());
            }
        }
        this.dispose();
        Config.getConfig().getPlWindow().repaint();
    }

    private void doSaveApe() throws Exception {
        APEv2Tag tag = new APEv2Tag();
        tag.setAlbum(album.getText());
        tag.setArtist(artist.getText());
        tag.setComment(comment.getText());
        tag.setGenre(genre.getText());
        tag.setTitle(title.getText());
        tag.setTrack(track.getText());
        tag.setYear(year.getText());
        RandomAccessFile raf = new RandomAccessFile(item.getLocation(), "rw");
        try {
            tag.write(raf, false);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
    }

    private void doSaveFlac() throws Exception {
        RandomAccessFile raf = null, temp = null;
        File f = null;
        try {
            raf = new RandomAccessFile(item.getLocation(), "rw");
            f = File.createTempFile("yoyo", "tmp");
            temp = new RandomAccessFile(f, "rw");
            FlacTagWriter w = new FlacTagWriter();
            VorbisCommentTag vt = new VorbisCommentTag();
            vt.setAlbum(album.getText());
            vt.setArtist(artist.getText());
            vt.setComment(comment.getText());
            vt.setGenre(genre.getText());
            vt.setTitle(title.getText());
            vt.setTrack(track.getText());
            vt.setYear(year.getText());
            vt.setVendor("hadeslee");
            FlacTag tag = new FlacTag(vt, new ArrayList<MetadataBlockDataPicture>());
            w.write(tag, raf, temp);
        } finally {
            if (raf != null) {
                raf.close();
            }
            if (temp != null) {
                temp.close();
            }
            if (f != null) {
                f.delete();
            }
        }
    }

    private void doSaveOgg() throws Exception {
        RandomAccessFile raf = null, temp = null;
        File f = null;
        try {
            raf = new RandomAccessFile(item.getLocation(), "rw");
            f = File.createTempFile("temp", "fix");
            log.log(Level.INFO, "临时文件是:" + f.getPath());
            temp = new RandomAccessFile(f, "rw");
            OggVorbisTagWriter w = new OggVorbisTagWriter();
            VorbisCommentTag vt = new VorbisCommentTag();
            vt.setAlbum(album.getText());
            vt.setArtist(artist.getText());
            vt.setComment(comment.getText());
            vt.setGenre(genre.getText());
            vt.setTitle(title.getText());
            vt.setTrack(track.getText());
            vt.setYear(year.getText());
            vt.setVendor("hadeslee");
            w.write(vt, raf, temp);
            temp.seek(0);
            raf.setLength(temp.length());
            raf.seek(0);
            raf.getChannel().transferFrom(temp.getChannel(), 0, temp.length());
            log.log(Level.INFO, "完毕");
        } finally {
            if (raf != null) {
                raf.close();
            }
            if (temp != null) {
                temp.close();
            }
            if (f != null) {
                f.delete();
            }
        }
    }

    private void doSaveMP3() throws Exception {
        MP3File mp3 = new MP3File(new File(item.getLocation()), 0);
        int mode = Config.getConfig().getWriteTagMode();
        String encoding = Config.getConfig().getEncoding();
        if ((mode & Config.WRITEMODE_APEv2) == Config.WRITEMODE_APEv2) {
            APEv2Tag tag = new APEv2Tag();
            tag.setAlbum(album.getText());
            tag.setArtist(artist.getText());
            tag.setComment(comment.getText());
            tag.setGenre(genre.getText());
            tag.setTitle(title.getText());
            tag.setTrack(track.getText());
            tag.setYear(year.getText());
            mp3.setAPEv2Tag(tag);
            log.log(Level.INFO, "写出APEv2标签");
        }
        if ((mode & Config.WRITEMODE_ID3v1) == Config.WRITEMODE_ID3v1) {
            ID3v1Tag tag = new ID3v1Tag();
            tag.setAlbum(Util.convertString(album.getText(), encoding, "ISO8859-1"));
            tag.setArtist(Util.convertString(artist.getText(), encoding, "ISO8859-1"));
            tag.setComment(Util.convertString(comment.getText(), encoding, "ISO8859-1"));
            tag.setGenre(Util.convertString(genre.getText(), encoding, "ISO8859-1"));
            tag.setTitle(Util.convertString(title.getText(), encoding, "ISO8859-1"));
            tag.setYear(Util.convertString(year.getText(), encoding, "ISO8859-1"));
            mp3.setID3v1Tag(tag);
            log.log(Level.INFO, "写出ID3v1标签");
        }
        if ((mode & Config.WRITEMODE_ID3v2) == Config.WRITEMODE_ID3v2) {
            ID3v23Tag tag = new ID3v23Tag();
            tag.setAlbum(Util.convertString(album.getText(), encoding, "ISO8859-1"));
            tag.setArtist(Util.convertString(artist.getText(), encoding, "ISO8859-1"));
            tag.setComment(Util.convertString(comment.getText(), encoding, "ISO8859-1"));
            tag.setGenre(Util.convertString(genre.getText(), encoding, "ISO8859-1"));
            tag.setTitle(Util.convertString(title.getText(), encoding, "ISO8859-1"));
            tag.setTrack(Util.convertString(track.getText(), encoding, "ISO8859-1"));
            tag.setYear(Util.convertString(year.getText(), encoding, "ISO8859-1"));
            mp3.setID3v2TagOnly(tag);
            log.log(Level.INFO, "写出ID3v2标签");
        }
        mp3.save();
    }

    private javax.swing.JTextField album;

    private javax.swing.JTextField artist;

    private javax.swing.JTextArea comment;

    private javax.swing.JTextField fileName;

    private javax.swing.JTextField genre;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton2;

    private javax.swing.JButton jButton3;

    private javax.swing.JComboBox jComboBox1;

    private javax.swing.JComboBox jComboBox2;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel10;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel8;

    private javax.swing.JLabel jLabel9;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextField title;

    private javax.swing.JTextField track;

    private javax.swing.JTextField year;
}
