package com.hadeslee.yoyoplayer.setting;

import com.hadeslee.yoyoplayer.util.Config;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author  hadeslee
 */
public class AboutPanel extends javax.swing.JPanel {

    private boolean click;

    /** Creates new form AboutPanel */
    public AboutPanel() {
        initComponents();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        author = new javax.swing.JLabel();
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/hadeslee/yoyoplayer/pic/logo.png")));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/hadeslee/yoyoplayer/pic/name.png")));
        jLabel3.setText(Config.getResource("AboutPanel.author"));
        jLabel4.setText(Config.getResource("AboutPanel.copyRight"));
        jLabel5.setText(Config.getResource("AboutPanel.version"));
        jLabel6.setText(Config.getResource("AboutPanel.completeDate"));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(Config.getResource("AboutPanel.brief")));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("    YOYOPlayer是一个用JAVA编写的,跨平台的音乐播放软件.是一个集播放,歌词显示于一体的音频播放软件.\n    由于JAVA的跨平台性,您可以在几乎任何平台下使用此软件,这样可以免去您每个平台装一种特定播放软件的烦恼.\n    YOYOPlayer的主要定位是Linux下的用户,因为Linux下几乎没有一款集成性高的音频播放软件,并且读取中文标签经常会出现乱码,由于YOYOPlayer是国人开发的,所以对中文的支持绝对可以放心,并且可以自定义标签的读取和写入编码.\n    支持snd,aifc,aif,wav,au,mp1,mp2,mp3,ogg,spx,flac,ape,mac等音频格式音乐。支持10波段均衡器.\n    支持ID3v1/v2、APE和Vorbis标签的读取和写入，支持设置标签编码,支持以标签重命名文件。\n    支持同步歌词滚动显示和拖动定位播放，并且支持在线歌词搜索功能。\n    支持多播放列表，支持多种视觉效果.\n    真正永久免费并且开放源代码，也不存在任何功能或时间限制。在使用过程中,有任何问题,欢迎到\n    http://www.blogjava.net/hadeslee上留言.");
        jScrollPane1.setViewportView(jTextArea1);
        author.setForeground(new java.awt.Color(0, 0, 255));
        author.setText(Config.getResource("AboutPanel.authorName"));
        author.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                authorMouseClicked(evt);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                authorMouseEntered(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                authorMouseExited(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel2).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jLabel3).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(author)).addComponent(jLabel5)).addGap(59, 59, 59).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel6).addComponent(jLabel4)))))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel2).addComponent(jLabel1)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(jLabel4).addComponent(author)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel5).addComponent(jLabel6)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE).addContainerGap()));
    }

    private void authorMouseEntered(java.awt.event.MouseEvent evt) {
        author.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        author.setForeground(Color.RED);
    }

    private void authorMouseExited(java.awt.event.MouseEvent evt) {
        author.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        if (click) {
            author.setForeground(new Color(128, 0, 128));
        } else {
            author.setForeground(Color.BLUE);
        }
    }

    private void authorMouseClicked(java.awt.event.MouseEvent evt) {
        click = true;
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("http://www.blogjava.net/hadeslee"));
            } catch (URISyntaxException ex) {
            } catch (IOException ex) {
            }
        }
    }

    private javax.swing.JLabel author;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextArea jTextArea1;
}
