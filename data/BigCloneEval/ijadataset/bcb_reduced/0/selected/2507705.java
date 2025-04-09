package org.unicef.doc.ibis.nut;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import org.unicef.doc.ibis.nut.exceptions.CreationTimeException;
import org.unicef.doc.ibis.nut.exceptions.ModificationTimeException;
import org.unicef.doc.ibis.nut.exceptions.NUTException;
import org.unicef.doc.ibis.nut.persistence.*;
import org.unicef.doc.ibis.nut.controllers.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import org.jdesktop.beansbinding.Binding;

/**
 *
 * @author ngroupp
 */
public class ImageDisplayPanel extends javax.swing.JPanel {

    Config fConfig = null;

    ObjectFactory fFactory = null;

    org.unicef.doc.ibis.nut.persistence.File fFile = null;

    /** Creates new form ImageDisplayPanel
     * @param pImage
     */
    public ImageDisplayPanel(org.unicef.doc.ibis.nut.persistence.Image pImage) {
        this();
        setImage(pImage);
    }

    public ImageDisplayPanel() {
        super();
        fConfig = Config.getInstance();
        if (fConfig != null) {
            fFactory = fConfig.getObjectFactory();
        }
        initImage();
        initComponents();
        bindingGroup.addBindingListener(new StateChangeListener());
        if (fConfig.getDocumentManager() != null) {
            fConfig.getDocumentManager().addPanel(this);
        }
    }

    public void setImage(org.unicef.doc.ibis.nut.persistence.Image pImage) {
        javax.swing.ImageIcon img = null;
        java.util.List<org.unicef.doc.ibis.nut.persistence.File> files = null;
        org.unicef.doc.ibis.nut.persistence.File thefile = null;
        BufferedImage simg = null;
        org.unicef.doc.ibis.nut.persistence.File f = null;
        java.io.ByteArrayInputStream in = null;
        java.awt.Image dest = null;
        Graphics2D g = null;
        if (pImage == null) {
            return;
        }
        try {
            new ImageController(pImage);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (CreationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (ModificationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (NUTException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        image = pImage;
        if (image.isSetMedia()) {
            thefile = image.getMedia();
            if (thefile == null) {
                return;
            }
            file = thefile;
            in = new ByteArrayInputStream(thefile.getContent());
            try {
                simg = javax.imageio.ImageIO.read(in);
                if (simg.getWidth() > simg.getHeight()) {
                    dest = simg.getScaledInstance(200, 140, BufferedImage.SCALE_SMOOTH);
                } else {
                    dest = simg.getScaledInstance(150, 190, BufferedImage.SCALE_SMOOTH);
                    jLabel1.setPreferredSize(new Dimension(150, 190));
                }
            } catch (IOException ex) {
                Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            img = new javax.swing.ImageIcon(dest);
            if (img == null) {
                return;
            }
            jLabel1.setIcon(img);
            this.jbSelect.setVisible(false);
            this.jbSaveAs.setVisible(true);
        }
        rebind();
        updateSharedFields();
        if (this.getTopLevelAncestor() != null) {
            javax.swing.SwingUtilities.updateComponentTreeUI(this.getTopLevelAncestor());
        }
    }

    public org.unicef.doc.ibis.nut.persistence.Image getImage() {
        persistBindings();
        try {
            new ImageController(image);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CreationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModificationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NUTException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (image);
    }

    public void rebind() {
        org.jdesktop.beansbinding.Binding binding = null;
        if (bindingGroup == null) {
            bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
            bindingGroup.addBindingListener(new StateChangeListener());
        } else {
            bindingGroup.unbind();
        }
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${copyright}"), tfImageCopyright, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${caption}"), taImageCaption, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${altText}"), tfImageAltText, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${dimensions.height}"), jtfHeight, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue(BigInteger.ZERO);
        binding.setSourceUnreadableValue(BigInteger.ZERO);
        binding.setConverter(new StringToBigIntegerConverter());
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${dimensions.width}"), jtfWidth, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue(BigInteger.ZERO);
        binding.setSourceUnreadableValue(BigInteger.ZERO);
        binding.setConverter(new StringToBigIntegerConverter());
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, file, org.jdesktop.beansbinding.ELProperty.create("${name}"), tfFilename, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue(null);
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${optimized}"), jcbIsOptimized, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${storyPhoto}"), jcbIsStoryPhoto, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${storyPosition}"), jspStoryOrder, org.jdesktop.beansbinding.BeanProperty.create("value"));
        binding.setSourceNullValue(BigInteger.ZERO);
        binding.setSourceUnreadableValue(BigInteger.ZERO);
        binding.setConverter(new IntegerToBigIntegerConverter());
        bindingGroup.addBinding(binding);
        bindingGroup.bind();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
        image = initImage();
        dimensionsType = new org.unicef.doc.ibis.nut.persistence.DimensionsType();
        file = fFactory.createFile();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jlImageCopyright = new javax.swing.JLabel();
        tfImageCopyright = new javax.swing.JTextField();
        jlImageCaption = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taImageCaption = new javax.swing.JTextArea();
        jlImageAltText = new javax.swing.JLabel();
        tfImageAltText = new javax.swing.JTextField();
        jtfHeight = new javax.swing.JTextField();
        jtfWidth = new javax.swing.JTextField();
        jlHeight = new javax.swing.JLabel();
        jlWidth = new javax.swing.JLabel();
        jlFilename = new javax.swing.JLabel();
        tfFilename = new javax.swing.JTextField();
        jcbIsOptimized = new javax.swing.JCheckBox();
        jcbIsStoryPhoto = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jspStoryOrder = new javax.swing.JSpinner();
        jbSelect = new javax.swing.JButton();
        jbSaveAs = new javax.swing.JButton();
        setName("Form");
        setLayout(new java.awt.BorderLayout());
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.unicef.doc.ibis.nut.Main.class).getContext().getResourceMap(ImageDisplayPanel.class);
        jPanel1.setBackground(resourceMap.getColor("jPanel1.background"));
        jPanel1.setName("jPanel1");
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel2.setName("jPanel2");
        jPanel2.setLayout(new java.awt.GridBagLayout());
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title")));
        jPanel3.setMinimumSize(new java.awt.Dimension(250, 190));
        jPanel3.setName("jPanel3");
        jPanel3.setPreferredSize(new java.awt.Dimension(250, 190));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jLabel1.setIcon(resourceMap.getIcon("jlImage.icon"));
        jLabel1.setText(resourceMap.getString("jlImage.text"));
        jLabel1.setMaximumSize(new java.awt.Dimension(220, 160));
        jLabel1.setName("jlImage");
        jLabel1.setOpaque(true);
        jLabel1.setPreferredSize(new java.awt.Dimension(220, 160));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });
        jPanel3.add(jLabel1, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel2.add(jPanel3, gridBagConstraints);
        jPanel4.setName("jPanel4");
        jPanel4.setLayout(new java.awt.GridBagLayout());
        jlImageCopyright.setText(resourceMap.getString("jlImageCopyright.text"));
        jlImageCopyright.setName("jlImageCopyright");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jlImageCopyright, gridBagConstraints);
        tfImageCopyright.setMinimumSize(new java.awt.Dimension(200, 25));
        tfImageCopyright.setName("tfImageCopyright");
        tfImageCopyright.setPreferredSize(new java.awt.Dimension(200, 25));
        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${copyright}"), tfImageCopyright, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(tfImageCopyright, gridBagConstraints);
        jlImageCaption.setText(resourceMap.getString("jlImageCaption.text"));
        jlImageCaption.setName("jlImageCaption");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jlImageCaption, gridBagConstraints);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(401, 301));
        jScrollPane1.setName("jScrollPane1");
        jScrollPane1.setPreferredSize(new java.awt.Dimension(401, 301));
        taImageCaption.setColumns(20);
        taImageCaption.setLineWrap(true);
        taImageCaption.setRows(5);
        taImageCaption.setWrapStyleWord(true);
        taImageCaption.setMinimumSize(new java.awt.Dimension(400, 300));
        taImageCaption.setName("taImageCaption");
        taImageCaption.setPreferredSize(new java.awt.Dimension(400, 300));
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${caption}"), taImageCaption, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        jScrollPane1.setViewportView(taImageCaption);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jScrollPane1, gridBagConstraints);
        jlImageAltText.setText(resourceMap.getString("jlImageAltText.text"));
        jlImageAltText.setName("jlImageAltText");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jlImageAltText, gridBagConstraints);
        tfImageAltText.setEditable(false);
        tfImageAltText.setEnabled(false);
        tfImageAltText.setMinimumSize(new java.awt.Dimension(200, 25));
        tfImageAltText.setName("tfImageAltText");
        tfImageAltText.setPreferredSize(new java.awt.Dimension(200, 25));
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${altText}"), tfImageAltText, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(tfImageAltText, gridBagConstraints);
        jtfHeight.setEditable(false);
        jtfHeight.setEnabled(false);
        jtfHeight.setName("jtfHeight");
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${dimensions.height}"), jtfHeight, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue(BigInteger.ZERO);
        binding.setSourceUnreadableValue(BigInteger.ZERO);
        binding.setConverter(new StringToBigIntegerConverter());
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jtfHeight, gridBagConstraints);
        jtfWidth.setEditable(false);
        jtfWidth.setEnabled(false);
        jtfWidth.setName("jtfWidth");
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${dimensions.width}"), jtfWidth, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue(BigInteger.ZERO);
        binding.setSourceUnreadableValue(BigInteger.ZERO);
        binding.setConverter(new StringToBigIntegerConverter());
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jtfWidth, gridBagConstraints);
        jlHeight.setText(resourceMap.getString("jlHeight.text"));
        jlHeight.setName("jlHeight");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jlHeight, gridBagConstraints);
        jlWidth.setText(resourceMap.getString("jlWidth.text"));
        jlWidth.setName("jlWidth");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jlWidth, gridBagConstraints);
        jlFilename.setText(resourceMap.getString("jlFilename.text"));
        jlFilename.setName("jlFilename");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jlFilename, gridBagConstraints);
        tfFilename.setEnabled(false);
        tfFilename.setName("tfFilename");
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, file, org.jdesktop.beansbinding.ELProperty.create("${name}"), tfFilename, org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue(null);
        binding.setSourceUnreadableValue(null);
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(tfFilename, gridBagConstraints);
        jcbIsOptimized.setText(resourceMap.getString("jcbIsOptimized.text"));
        jcbIsOptimized.setName("jcbIsOptimized");
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${optimized}"), jcbIsOptimized, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jcbIsOptimized, gridBagConstraints);
        jcbIsStoryPhoto.setText(resourceMap.getString("jcbIsStoryPhoto.text"));
        jcbIsStoryPhoto.setName("jcbIsStoryPhoto");
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${storyPhoto}"), jcbIsStoryPhoto, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jcbIsStoryPhoto, gridBagConstraints);
        jLabel2.setText(resourceMap.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jLabel2, gridBagConstraints);
        jLabel3.setText(resourceMap.getString("jLabel3.text"));
        jLabel3.setName("jLabel3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jLabel3, gridBagConstraints);
        jLabel4.setText(resourceMap.getString("jLabel4.text"));
        jLabel4.setName("jLabel4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jLabel4, gridBagConstraints);
        jspStoryOrder.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        jspStoryOrder.setName("jspStoryOrder");
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, image, org.jdesktop.beansbinding.ELProperty.create("${storyPosition}"), jspStoryOrder, org.jdesktop.beansbinding.BeanProperty.create("value"));
        binding.setSourceNullValue(BigInteger.ZERO);
        binding.setSourceUnreadableValue(BigInteger.ZERO);
        binding.setConverter(new IntegerToBigIntegerConverter());
        bindingGroup.addBinding(binding);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel4.add(jspStoryOrder, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jPanel4, gridBagConstraints);
        jbSelect.setText(resourceMap.getString("jbSelect.text"));
        jbSelect.setName("jbSelect");
        jbSelect.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSelectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jbSelect, gridBagConstraints);
        jbSaveAs.setText(resourceMap.getString("jbSaveAs.text"));
        jbSaveAs.setName("jbSaveAs");
        jbSaveAs.setVisible(false);
        jbSaveAs.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbSaveAsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        jPanel2.add(jbSaveAs, gridBagConstraints);
        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);
        add(jPanel1, java.awt.BorderLayout.CENTER);
        bindingGroup.bind();
    }

    private void jbSelectActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.ImageIcon icon = null;
        BufferedImage simg = null;
        java.awt.Image dest = null;
        javax.swing.JFileChooser jc = new javax.swing.JFileChooser();
        java.io.File f = null;
        Graphics2D g = null;
        java.io.ByteArrayInputStream in = null;
        ImageController imgc = null;
        org.unicef.doc.ibis.nut.persistence.Image img = null;
        org.unicef.doc.ibis.nut.persistence.DimensionsType dims = null;
        org.unicef.doc.ibis.nut.controllers.DimensionsTypeController dimc = null;
        WebStoryController wsc = null;
        org.unicef.doc.ibis.nut.persistence.File thefile = null;
        javax.swing.filechooser.FileFilter filter = null;
        jc.setCurrentDirectory(new java.io.File(fConfig.getWorkingDirectory()));
        filter = new javax.swing.filechooser.FileNameExtensionFilter("Portable network graphics", "png");
        jc.addChoosableFileFilter(filter);
        filter = new javax.swing.filechooser.FileNameExtensionFilter("JPEG", "jpg", "jpeg");
        jc.addChoosableFileFilter(filter);
        filter = new javax.swing.filechooser.FileNameExtensionFilter("All supported image formats (.png, .jpg)", "png", "jpg", "jpeg");
        jc.addChoosableFileFilter(filter);
        jc.showOpenDialog(getTopLevelAncestor());
        try {
            img = fFactory.createImage();
            dims = fFactory.createDimensionsType();
            imgc = new ImageController(image);
            dimc = new org.unicef.doc.ibis.nut.controllers.DimensionsTypeController(dims);
        } catch (CreationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModificationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NUTException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        f = jc.getSelectedFile();
        if (f == null) {
            return;
        }
        if (fConfig == null) {
            fConfig = Config.getInstance();
        }
        if (f != null) {
            fConfig.setWorkingDirectory(f.getPath());
        }
        try {
            thefile = imgc.setFile(f);
            in = new ByteArrayInputStream(thefile.getContent());
            try {
                simg = javax.imageio.ImageIO.read(in);
                if (simg.getWidth() > simg.getHeight()) {
                    dest = simg.getScaledInstance(200, 140, BufferedImage.SCALE_SMOOTH);
                } else {
                    dest = simg.getScaledInstance(150, 190, BufferedImage.SCALE_SMOOTH);
                    jLabel1.setPreferredSize(new Dimension(150, 190));
                }
            } catch (IOException ex) {
                Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            icon = new javax.swing.ImageIcon(dest);
            if (img == null) {
                return;
            }
            jLabel1.setIcon(icon);
            if (simg != null) {
                dimc.setHeight(simg.getHeight());
                dimc.setWidth(simg.getWidth());
                dimc.setUnit("pixels");
                imgc.setDimensions(dims.getHeight().intValue(), dims.getWidth().intValue(), dims.getUnit());
            }
            file = thefile;
            updateSharedFields();
            rebind();
            taImageCaption.setEnabled(true);
            taImageCaption.setEditable(true);
            taImageCaption.setBackground(Color.WHITE);
            tfImageAltText.setEnabled(true);
            tfImageAltText.setEditable(true);
            tfImageCopyright.setEnabled(true);
            tfImageCopyright.setEditable(true);
            jbSelect.setVisible(false);
            jbSaveAs.setVisible(true);
            this.jcbIsStoryPhoto.setSelected(true);
            ((JPanel) this.getParent()).setName(((JPanel) this.getParent()).getName() + " - " + thefile.getName());
        } catch (CreationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ModificationTimeException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NUTException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        javax.swing.SwingUtilities.updateComponentTreeUI(this);
    }

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {
        String key = null;
        org.unicef.doc.ibis.nut.persistence.File ifile = null;
        java.io.File ofile = null;
        java.io.FileOutputStream fout = null;
        java.awt.Desktop theDesktop = null;
        if (!java.awt.Desktop.isDesktopSupported() || this.image == null || !image.isSetMedia() || !image.getMedia().isSetContent() || !image.getMedia().isSetName()) {
            return;
        }
        key = image.getMedia().getName();
        try {
            theDesktop = java.awt.Desktop.getDesktop();
            ofile = new java.io.File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + java.util.UUID.randomUUID().toString() + key);
            ofile.deleteOnExit();
            fout = new java.io.FileOutputStream(ofile);
            ifile = image.getMedia();
            fout.write(ifile.getContent());
            fout.flush();
            fout.close();
            theDesktop.open(ofile);
        } catch (IOException ioe) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    private void jbSaveAsActionPerformed(java.awt.event.ActionEvent evt) {
        org.unicef.doc.ibis.nut.persistence.File ifile = null;
        java.io.File ofile = null;
        java.io.FileOutputStream fout = null;
        javax.swing.JFileChooser jc = null;
        try {
            ifile = image.getMedia();
            if (ifile != null && ifile.isSetURI() && !ifile.isSetContent()) {
                return;
            }
            jc = new javax.swing.JFileChooser();
            jc.setCurrentDirectory(new java.io.File(fConfig.getWorkingDirectory()));
            ofile = new java.io.File(jc.getCurrentDirectory().getAbsolutePath() + System.getProperty("file.separator") + ifile.getName());
            jc.setSelectedFile(ofile);
            jc.showSaveDialog(this);
            ofile = jc.getSelectedFile();
            if (ofile != null) {
                fout = new java.io.FileOutputStream(ofile);
                fout.write(ifile.getContent());
                fout.flush();
                fout.close();
                if (ofile.isDirectory()) {
                    fConfig.setWorkingDirectory(ofile.getAbsolutePath());
                } else {
                    if (ofile.getParentFile() != null) {
                        fConfig.setWorkingDirectory(ofile.getParentFile().getAbsolutePath());
                    }
                }
            }
        } catch (IOException ioe) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, ioe);
        } catch (RuntimeException rte) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, rte);
        } catch (Exception e) {
            Logger.getLogger(ImageDisplayPanel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private org.unicef.doc.ibis.nut.persistence.DimensionsType dimensionsType;

    private org.unicef.doc.ibis.nut.persistence.File file;

    private org.unicef.doc.ibis.nut.persistence.Image image;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JButton jbSaveAs;

    private javax.swing.JButton jbSelect;

    private javax.swing.JCheckBox jcbIsOptimized;

    private javax.swing.JCheckBox jcbIsStoryPhoto;

    private javax.swing.JLabel jlFilename;

    private javax.swing.JLabel jlHeight;

    private javax.swing.JLabel jlImageAltText;

    private javax.swing.JLabel jlImageCaption;

    private javax.swing.JLabel jlImageCopyright;

    private javax.swing.JLabel jlWidth;

    private javax.swing.JSpinner jspStoryOrder;

    private javax.swing.JTextField jtfHeight;

    private javax.swing.JTextField jtfWidth;

    private javax.swing.JTextArea taImageCaption;

    private javax.swing.JTextField tfFilename;

    private javax.swing.JTextField tfImageAltText;

    private javax.swing.JTextField tfImageCopyright;

    private org.jdesktop.beansbinding.BindingGroup bindingGroup;

    private org.unicef.doc.ibis.nut.persistence.Image initImage() {
        org.unicef.doc.ibis.nut.persistence.Image img = null;
        try {
            if (fFactory == null) {
                fFactory = new org.unicef.doc.ibis.nut.persistence.ObjectFactory();
            }
            if (fFactory != null) {
                img = fFactory.createImage();
            }
            new org.unicef.doc.ibis.nut.controllers.ImageController(img);
        } catch (Throwable ignore) {
        }
        return (img);
    }

    public void persistBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            Binding.SyncFailure failure = null;
            if (!b.isManaged()) {
                failure = b.saveAndNotify();
                if (failure != null) {
                    Logger.getLogger(VideoPanel.class.getName()).log(Level.SEVERE, null, failure.toString());
                }
            }
        }
    }

    private void updateSharedFields() {
        Iterator<JPanel> pnlenum = null;
        JPanel pnl = null;
        if (fConfig.getDocumentManager() != null) {
            pnlenum = fConfig.getDocumentManager().getPanels();
            while (pnlenum.hasNext()) {
                pnl = pnlenum.next();
                if (pnl instanceof HomepageElementsPanel) {
                    if (image.isSetMedia() && image.getMedia().isSetName()) {
                        ((HomepageElementsPanel) pnl).addImage(image.getMedia().getName());
                    }
                    javax.swing.SwingUtilities.updateComponentTreeUI(pnl);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return (true);
        } else {
            return (false);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.image != null ? this.image.hashCode() : 0);
        return hash;
    }
}
