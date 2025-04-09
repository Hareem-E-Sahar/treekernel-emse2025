package regnumhelper.gui;

import java.awt.image.*;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import regnumhelper.Main;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.ImageIcon;
import java.util.Timer;
import java.util.TimerTask;

public class WindowOffsetAdjustment extends javax.swing.JPanel {

    private int offsetX = 0;

    private int offsetY = 0;

    private Robot robot = null;

    private ImageIcon upArrow = null;

    private ImageIcon downArrow = null;

    private ImageIcon leftArrow = null;

    private ImageIcon rightArrow = null;

    private RegnumHelperFrame frame = null;

    private boolean manualAdjustment = false;

    private boolean autoRepeatDirectionX = false;

    private boolean autoRepeatPlus = true;

    Timer autoRepeatTimer = null;

    /** Creates new form WindowOffsetAdjustment */
    public WindowOffsetAdjustment(RegnumHelperFrame frame) {
        this.frame = frame;
        initComponents();
        try {
            upArrow = new ImageIcon(ImageIO.read(Main.class.getResource("images/UpArrow.png")));
            downArrow = new ImageIcon(ImageIO.read(Main.class.getResource("images/DownArrow.png")));
            leftArrow = new ImageIcon(ImageIO.read(Main.class.getResource("images/LeftArrow.png")));
            rightArrow = new ImageIcon(ImageIO.read(Main.class.getResource("images/RightArrow.png")));
            lblExPicture.setIcon(new ImageIcon(ImageIO.read(Main.class.getResource("images/exampleUpperLeftCorner.png"))));
            butUp.setIcon(upArrow);
            butDown.setIcon(downArrow);
            butRight.setIcon(rightArrow);
            butLeft.setIcon(leftArrow);
            butUp.setText("");
            butDown.setText("");
            butRight.setText("");
            butLeft.setText("");
        } catch (IOException ex) {
            ex.printStackTrace();
            butUp.setText("Up");
            butDown.setText("Down");
            butRight.setText("Righ");
            butLeft.setText("Left");
        }
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    public void setData() {
        if (frame != null && frame.getMain() != null) {
            manualAdjustment = true;
            txtWidth.setValue(frame.getMain().getSettings().getRegnumWindowRectangle().width);
            txtHeight.setValue(frame.getMain().getSettings().getRegnumWindowRectangle().height);
            offsetX = frame.getMain().getSettings().getRegnumWindowRectangle().x;
            offsetY = frame.getMain().getSettings().getRegnumWindowRectangle().y;
            txtXoffset.setText(offsetX + "");
            txtYoffset.setText(offsetY + "");
            chkFullscreen.setSelected(frame.getMain().getSettings().isFullScreen());
            manualAdjustment = false;
        }
    }

    public void valueChanged() {
        if (frame != null && frame.getMain() != null) {
            int width = new Integer(txtWidth.getValue().toString()).intValue();
            int height = new Integer(txtHeight.getValue().toString()).intValue();
            Rectangle regnumWindow = new Rectangle(getOffsetX(), getOffsetY(), width, height);
            frame.getMain().getSettings().setRegnumWindowRectangle(regnumWindow);
            frame.getMain().getSettings().ResetAbsoluteRectangles();
            frame.getMain().getSettings().setFullScreen(chkFullscreen.isSelected());
            frame.updateCapturePosition();
        }
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jPanel2 = new javax.swing.JPanel();
        lblPicture = new javax.swing.JLabel();
        lblXoffset = new javax.swing.JLabel();
        txtXoffset = new javax.swing.JTextField();
        lblYoffset = new javax.swing.JLabel();
        txtYoffset = new javax.swing.JTextField();
        lblScreenRes = new javax.swing.JLabel();
        chkFullscreen = new javax.swing.JCheckBox();
        butCapture = new javax.swing.JButton();
        lblExPicture = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        panButtonControls = new javax.swing.JPanel();
        butUp = new javax.swing.JButton();
        butLeft = new javax.swing.JButton();
        butRight = new javax.swing.JButton();
        butDown = new javax.swing.JButton();
        txtWidth = new javax.swing.JFormattedTextField();
        txtHeight = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        setLayout(new java.awt.GridBagLayout());
        jPanel2.setPreferredSize(new java.awt.Dimension(300, 400));
        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jPanel2ComponentHidden(evt);
            }
        });
        jPanel2.setLayout(new java.awt.GridBagLayout());
        lblPicture.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lblPicture.setMaximumSize(new java.awt.Dimension(110, 110));
        lblPicture.setMinimumSize(new java.awt.Dimension(110, 110));
        lblPicture.setPreferredSize(new java.awt.Dimension(110, 110));
        lblPicture.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblPicture, gridBagConstraints);
        lblXoffset.setText("X Offset:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel2.add(lblXoffset, gridBagConstraints);
        txtXoffset.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtXoffsetActionPerformed(evt);
            }
        });
        txtXoffset.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                txtXoffsetFocusLost(evt);
            }
        });
        txtXoffset.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtXoffsetKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 8, 5);
        jPanel2.add(txtXoffset, gridBagConstraints);
        lblYoffset.setText("Y Offset:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel2.add(lblYoffset, gridBagConstraints);
        txtYoffset.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtYoffsetActionPerformed(evt);
            }
        });
        txtYoffset.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                txtYoffsetFocusLost(evt);
            }
        });
        txtYoffset.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtYoffsetKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 8, 5);
        jPanel2.add(txtYoffset, gridBagConstraints);
        lblScreenRes.setText("Game screen Resolution");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanel2.add(lblScreenRes, gridBagConstraints);
        chkFullscreen.setText("Fullscreen");
        chkFullscreen.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkFullscreen.setMargin(new java.awt.Insets(0, 0, 0, 0));
        chkFullscreen.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkFullscreenActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jPanel2.add(chkFullscreen, gridBagConstraints);
        butCapture.setText("Capture Picture");
        butCapture.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCaptureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 8, 5);
        jPanel2.add(butCapture, gridBagConstraints);
        lblExPicture.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lblExPicture.setMaximumSize(new java.awt.Dimension(110, 110));
        lblExPicture.setMinimumSize(new java.awt.Dimension(110, 110));
        lblExPicture.setPreferredSize(new java.awt.Dimension(110, 110));
        lblExPicture.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel2.add(lblExPicture, gridBagConstraints);
        jLabel1.setText("Example picture");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabel1, gridBagConstraints);
        panButtonControls.setLayout(new java.awt.GridBagLayout());
        butUp.setText("Up");
        butUp.setMaximumSize(new java.awt.Dimension(35, 35));
        butUp.setMinimumSize(new java.awt.Dimension(35, 35));
        butUp.setPreferredSize(new java.awt.Dimension(35, 35));
        butUp.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                butUpMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                butUpMouseReleased(evt);
            }
        });
        butUp.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 1, 1);
        panButtonControls.add(butUp, gridBagConstraints);
        butLeft.setText("Left");
        butLeft.setMaximumSize(new java.awt.Dimension(35, 35));
        butLeft.setMinimumSize(new java.awt.Dimension(35, 35));
        butLeft.setPreferredSize(new java.awt.Dimension(35, 35));
        butLeft.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                butLeftMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                butLeftMouseReleased(evt);
            }
        });
        butLeft.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLeftActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        panButtonControls.add(butLeft, gridBagConstraints);
        butRight.setText("Right");
        butRight.setMaximumSize(new java.awt.Dimension(35, 35));
        butRight.setMinimumSize(new java.awt.Dimension(35, 35));
        butRight.setPreferredSize(new java.awt.Dimension(35, 35));
        butRight.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                butRightMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                butRightMouseReleased(evt);
            }
        });
        butRight.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butRightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        panButtonControls.add(butRight, gridBagConstraints);
        butDown.setText("Down");
        butDown.setMaximumSize(new java.awt.Dimension(35, 35));
        butDown.setMinimumSize(new java.awt.Dimension(35, 35));
        butDown.setPreferredSize(new java.awt.Dimension(35, 35));
        butDown.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                butDownMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                butDownMouseReleased(evt);
            }
        });
        butDown.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        panButtonControls.add(butDown, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 5;
        jPanel2.add(panButtonControls, gridBagConstraints);
        txtWidth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtWidth.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtWidthActionPerformed(evt);
            }
        });
        txtWidth.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                txtWidthFocusLost(evt);
            }
        });
        txtWidth.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtWidthKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(txtWidth, gridBagConstraints);
        txtHeight.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtHeight.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtHeightActionPerformed(evt);
            }
        });
        txtHeight.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                txtHeightFocusLost(evt);
            }
        });
        txtHeight.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtHeightKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(txtHeight, gridBagConstraints);
        jLabel3.setText("X");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanel2.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel2, gridBagConstraints);
    }

    private void butCaptureActionPerformed(java.awt.event.ActionEvent evt) {
        refreshImage();
    }

    private void butUpActionPerformed(java.awt.event.ActionEvent evt) {
        setOffsetY(offsetY - 1);
    }

    private void butLeftActionPerformed(java.awt.event.ActionEvent evt) {
        setOffsetX(offsetX - 1);
    }

    private void butRightActionPerformed(java.awt.event.ActionEvent evt) {
        setOffsetX(offsetX + 1);
    }

    private void butDownActionPerformed(java.awt.event.ActionEvent evt) {
        setOffsetY(offsetY + 1);
    }

    private void butDownMousePressed(java.awt.event.MouseEvent evt) {
        startTimer(false, false);
    }

    public void startTimer(boolean xDirection, boolean plus) {
        if (autoRepeatTimer != null) {
            autoRepeatTimer.cancel();
            autoRepeatTimer = null;
        }
        autoRepeatTimer = new Timer();
        autoRepeatDirectionX = xDirection;
        autoRepeatPlus = plus;
        TimerTask autoRepeatTask = new TimerTask() {

            int counter = 0;

            public void run() {
                if (autoRepeatDirectionX) {
                    if (autoRepeatPlus) {
                        butRightActionPerformed(null);
                        if (counter >= 10) butRightActionPerformed(null);
                    } else {
                        butLeftActionPerformed(null);
                        if (counter >= 10) butLeftActionPerformed(null);
                    }
                } else {
                    if (autoRepeatPlus) {
                        butUpActionPerformed(null);
                        if (counter >= 10) butUpActionPerformed(null);
                    } else {
                        butDownActionPerformed(null);
                        if (counter >= 10) butDownActionPerformed(null);
                    }
                }
                counter++;
            }
        };
        autoRepeatTimer.schedule(autoRepeatTask, 500, 150);
    }

    public void stopTimer() {
        if (autoRepeatTimer != null) {
            autoRepeatTimer.cancel();
            autoRepeatTimer = null;
        }
    }

    private void butDownMouseReleased(java.awt.event.MouseEvent evt) {
        stopTimer();
    }

    private void txtXoffsetActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            setOffsetX(Integer.parseInt(txtXoffset.getText()));
        } catch (NumberFormatException exc) {
            txtXoffset.setText(offsetX + "");
        }
        txtXoffset.setForeground(Color.black);
    }

    private void txtXoffsetKeyPressed(java.awt.event.KeyEvent evt) {
        txtXoffset.setForeground(Color.red);
    }

    private void txtXoffsetFocusLost(java.awt.event.FocusEvent evt) {
        txtXoffsetActionPerformed(null);
    }

    private void txtYoffsetActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            setOffsetY(Integer.parseInt(txtYoffset.getText()));
        } catch (NumberFormatException exc) {
            txtYoffset.setText(offsetY + "");
        }
        txtYoffset.setForeground(Color.black);
    }

    private void txtYoffsetKeyPressed(java.awt.event.KeyEvent evt) {
        txtYoffset.setForeground(Color.red);
    }

    private void txtYoffsetFocusLost(java.awt.event.FocusEvent evt) {
        txtYoffsetActionPerformed(null);
    }

    private void chkFullscreenActionPerformed(java.awt.event.ActionEvent evt) {
        if (!manualAdjustment) {
            if (chkFullscreen.isSelected()) {
                setOffsetX(0);
                setOffsetY(0);
            } else {
            }
            valueChanged();
        }
    }

    private void butUpMousePressed(java.awt.event.MouseEvent evt) {
        startTimer(false, true);
    }

    private void butUpMouseReleased(java.awt.event.MouseEvent evt) {
        stopTimer();
    }

    private void butRightMousePressed(java.awt.event.MouseEvent evt) {
        startTimer(true, true);
    }

    private void butRightMouseReleased(java.awt.event.MouseEvent evt) {
        stopTimer();
    }

    private void butLeftMousePressed(java.awt.event.MouseEvent evt) {
        startTimer(true, false);
    }

    private void butLeftMouseReleased(java.awt.event.MouseEvent evt) {
        stopTimer();
    }

    private void txtWidthFocusLost(java.awt.event.FocusEvent evt) {
        try {
            txtWidth.commitEdit();
        } catch (ParseException ex) {
        }
        valueChanged();
    }

    private void txtHeightFocusLost(java.awt.event.FocusEvent evt) {
        try {
            txtHeight.commitEdit();
        } catch (ParseException ex) {
        }
        valueChanged();
    }

    private void txtWidthActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtHeightActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void txtHeightKeyTyped(java.awt.event.KeyEvent evt) {
    }

    private void txtWidthKeyTyped(java.awt.event.KeyEvent evt) {
    }

    private void jPanel2ComponentHidden(java.awt.event.ComponentEvent evt) {
        valueChanged();
    }

    private void refreshImage() {
        BufferedImage posImg = robot.createScreenCapture(new Rectangle(offsetX - 50, offsetY - 50, 100, 100));
        for (int i = 0; i < posImg.getWidth(); i++) {
            posImg.setRGB(i, 50, 16711680);
        }
        for (int i = 0; i < posImg.getHeight(); i++) {
            posImg.setRGB(50, i, 16711680);
        }
        ImageIcon icon = new ImageIcon(posImg);
        lblPicture.setIcon(icon);
        posImg = null;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
        txtXoffset.setText(offsetX + "");
        txtXoffset.repaint();
        refreshImage();
        valueChanged();
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        txtYoffset.setText(offsetY + "");
        txtYoffset.repaint();
        refreshImage();
        valueChanged();
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    private javax.swing.JButton butCapture;

    private javax.swing.JButton butDown;

    private javax.swing.JButton butLeft;

    private javax.swing.JButton butRight;

    private javax.swing.JButton butUp;

    private javax.swing.JCheckBox chkFullscreen;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JLabel lblExPicture;

    private javax.swing.JLabel lblPicture;

    private javax.swing.JLabel lblScreenRes;

    private javax.swing.JLabel lblXoffset;

    private javax.swing.JLabel lblYoffset;

    private javax.swing.JPanel panButtonControls;

    private javax.swing.JFormattedTextField txtHeight;

    private javax.swing.JFormattedTextField txtWidth;

    private javax.swing.JTextField txtXoffset;

    private javax.swing.JTextField txtYoffset;
}
