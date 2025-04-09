public class Test {    @Override
    public void createPartControl(final Composite parent) {
        display = parent.getDisplay();
        useItemNr = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_ITEMNR");
        useDescription = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_DESCRIPTION");
        scaledPrices = Activator.getDefault().getPreferenceStore().getInt("PRODUCT_SCALED_PRICES");
        useWeight = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_WEIGHT");
        useNet = (Activator.getDefault().getPreferenceStore().getInt("PRODUCT_USE_NET_GROSS") != 2);
        useGross = (Activator.getDefault().getPreferenceStore().getInt("PRODUCT_USE_NET_GROSS") != 1);
        useVat = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_VAT");
        usePicture = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_PICTURE");
        useQuantity = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_QUANTITY");
        useQuantityUnit = Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_QUNIT");
        vatId = product.getIntValueByKey("vatid");
        try {
            vat = Data.INSTANCE.getVATs().getDatasetById(vatId).getDoubleValueByKey("value");
        } catch (IndexOutOfBoundsException e) {
            vat = 0.0;
        }
        top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
        Composite invisible = new Composite(top, SWT.NONE);
        invisible.setVisible(false);
        GridDataFactory.fillDefaults().hint(0, 0).span(2, 1).applyTo(invisible);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.PRODUCT_EDITOR);
        Group productDescGroup = new Group(top, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(productDescGroup);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(productDescGroup);
        productDescGroup.setText(_("Description"));
        Label labelItemNr = new Label(useItemNr ? productDescGroup : invisible, SWT.NONE);
        labelItemNr.setText(_("Item Number"));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelItemNr);
        textItemNr = new Text(useItemNr ? productDescGroup : invisible, SWT.BORDER);
        textItemNr.setText(product.getStringValueByKey("itemnr"));
        superviceControl(textItemNr, 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textItemNr);
        Label labelName = new Label(productDescGroup, SWT.NONE);
        labelName.setText(_("Name"));
        labelName.setToolTipText(_("Name of the product. This is used for the items in the document."));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
        textName = new Text(productDescGroup, SWT.BORDER);
        textName.setText(product.getStringValueByKey("name"));
        textName.setToolTipText(labelName.getToolTipText());
        superviceControl(textName, 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);
        Label labelCategory = new Label(productDescGroup, SWT.NONE);
        labelCategory.setText(_("Category"));
        labelCategory.setToolTipText(_("You can set a category to classify the products. This is also the web shop category."));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
        comboCategory = new Combo(productDescGroup, SWT.BORDER);
        comboCategory.setText(product.getStringValueByKey("category"));
        comboCategory.setToolTipText(labelCategory.getToolTipText());
        superviceControl(comboCategory);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
        TreeSet<String> categories = new TreeSet<String>();
        categories.addAll(Data.INSTANCE.getProducts().getCategoryStrings());
        for (Object category : categories) {
            comboCategory.add(category.toString());
        }
        Label labelDescription = new Label(useDescription ? productDescGroup : invisible, SWT.NONE);
        labelDescription.setText(_("Description"));
        labelDescription.setToolTipText(_("Additional description. Can be used for the item. This can be selected in preferences/documents."));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
        textDescription = new Text(useDescription ? productDescGroup : invisible, SWT.BORDER | SWT.MULTI);
        textDescription.setText(DataUtils.makeOSLineFeeds(product.getStringValueByKey("description")));
        textDescription.setToolTipText(labelDescription.getToolTipText());
        superviceControl(textDescription, 250);
        GridDataFactory.fillDefaults().hint(10, 80).grab(true, false).applyTo(textDescription);
        Label labelQuantityUnit = new Label(useQuantityUnit ? productDescGroup : invisible, SWT.NONE);
        labelQuantityUnit.setText(_("Quantity unit"));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelQuantityUnit);
        textQuantityUnit = new Text(useQuantityUnit ? productDescGroup : invisible, SWT.BORDER);
        textQuantityUnit.setText(product.getFormatedStringValueByKey("qunit"));
        superviceControl(textQuantityUnit, 16);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textQuantityUnit);
        Label labelPrice = new Label(productDescGroup, SWT.NONE);
        if (useNet && useGross) labelPrice.setText(_("Price")); else if (useNet) labelPrice.setText(_("Price (net)")); else if (useGross) labelPrice.setText(_("Price (gross)"));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPrice);
        Composite pricetable = new Composite(productDescGroup, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns((scaledPrices > 1) ? (useNet && useGross) ? 4 : 3 : 2).applyTo(pricetable);
        if ((scaledPrices >= 2) && useNet && useGross) {
            new Label(pricetable, SWT.NONE);
            new Label(pricetable, SWT.NONE);
        }
        if (useNet && useGross) {
            Label labelNet = new Label(pricetable, SWT.CENTER);
            labelNet.setText(_("Net"));
            Label labelGross = new Label(pricetable, SWT.CENTER);
            labelGross.setText(_("Gross"));
        }
        for (int i = 0; i < 5; i++) {
            String indexNr = Integer.toString(i + 1);
            net[i] = new UniData(UniDataType.STRING, product.getDoubleValueByKey("price" + indexNr));
            labelBlock[i] = new Label(((i < scaledPrices) && (scaledPrices >= 2)) ? pricetable : invisible, SWT.NONE);
            labelBlock[i].setText(_("from", "QUANTITY"));
            textBlock[i] = new Text(((i < scaledPrices) && (scaledPrices >= 2)) ? pricetable : invisible, SWT.BORDER | SWT.RIGHT);
            textBlock[i].setText(product.getFormatedStringValueByKey("block" + indexNr));
            superviceControl(textBlock[i], 6);
            GridDataFactory.swtDefaults().hint(40, SWT.DEFAULT).applyTo(textBlock[i]);
            if (useNet) {
                netText[i] = new NetText(this, (i < scaledPrices) ? pricetable : invisible, SWT.BORDER | SWT.RIGHT, net[i], vat);
                GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).applyTo(netText[i].getNetText());
            }
            if (useGross) {
                grossText[i] = new GrossText(this, (i < scaledPrices) ? pricetable : invisible, SWT.BORDER | SWT.RIGHT, net[i], vat);
                GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).applyTo(grossText[i].getGrossText());
            }
            if (useNet && useGross) {
                netText[i].setGrossText(grossText[i].getGrossText());
                grossText[i].setNetText(netText[i].getNetText());
            }
        }
        if (scaledPrices >= 2) setTabOrder(textDescription, textBlock[0]); else if (useNet) setTabOrder(textDescription, netText[0].getNetText()); else setTabOrder(textDescription, grossText[0].getGrossText());
        Label labelVat = new Label(useVat ? productDescGroup : invisible, SWT.NONE);
        labelVat.setText(_("VAT"));
        labelVat.setToolTipText(_("Tax rate of the product"));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVat);
        comboVat = new Combo(useVat ? productDescGroup : invisible, SWT.BORDER);
        comboVat.setToolTipText(labelVat.getToolTipText());
        comboViewer = new ComboViewer(comboVat);
        comboViewer.setContentProvider(new UniDataSetContentProvider());
        comboViewer.setLabelProvider(new UniDataSetLabelProvider());
        comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                if (!structuredSelection.isEmpty()) {
                    Object firstElement = structuredSelection.getFirstElement();
                    UniDataSet selectedVat = (UniDataSet) firstElement;
                    Double oldVat = vat;
                    vatId = selectedVat.getIntValueByKey("id");
                    vat = selectedVat.getDoubleValueByKey("value");
                    for (int i = 0; i < scaledPrices; i++) {
                        if (!useNet) {
                            net[i].setValue(net[i].getValueAsDouble() * ((1 + oldVat) / (1 + vat)));
                        }
                        if (netText[i] != null) netText[i].setVatValue(vat);
                        if (grossText[i] != null) grossText[i].setVatValue(vat);
                    }
                }
                checkDirty();
            }
        });
        comboViewer.setInput(Data.INSTANCE.getVATs().getActiveDatasetsPrefereCategory(DataSetVAT.getSalesTaxString()));
        try {
            comboViewer.setSelection(new StructuredSelection(Data.INSTANCE.getVATs().getDatasetById(vatId)), true);
        } catch (IndexOutOfBoundsException e) {
            comboVat.setText("invalid");
            vatId = -1;
        }
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboVat);
        Label labelWeight = new Label(useWeight ? productDescGroup : invisible, SWT.NONE);
        labelWeight.setText(_("Weight (kg)"));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWeight);
        textWeight = new Text(useWeight ? productDescGroup : invisible, SWT.BORDER);
        textWeight.setText(product.getStringValueByKey("weight"));
        superviceControl(textWeight, 16);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textWeight);
        Label labelQuantity = new Label(useQuantity ? productDescGroup : invisible, SWT.NONE);
        labelQuantity.setText(_("Quantity"));
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelQuantity);
        textQuantity = new Text(useQuantity ? productDescGroup : invisible, SWT.BORDER);
        textQuantity.setText(product.getFormatedStringValueByKey("quantity"));
        superviceControl(textQuantity, 16);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textQuantity);
        Group productPictureGroup = new Group(usePicture ? top : invisible, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(productPictureGroup);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(productPictureGroup);
        productPictureGroup.setText(_("Product Picture"));
        photoComposite = new Composite(productPictureGroup, SWT.BORDER);
        GridLayoutFactory.swtDefaults().margins(10, 10).numColumns(1).applyTo(photoComposite);
        GridDataFactory.fillDefaults().indent(0, 10).align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(photoComposite);
        photoComposite.setBackground(new Color(null, 255, 255, 255));
        labelProductPicture = new Label(photoComposite, SWT.NONE);
        pictureName = product.getStringValueByKey("picturename");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(labelProductPicture);
        textProductPicturePath = new Text(photoComposite, SWT.NONE);
        textProductPicturePath.setEditable(false);
        textProductPicturePath.setBackground(new Color(null, 255, 255, 255));
        superviceControl(textProductPicturePath, 250);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textProductPicturePath);
        createPicturePathFromPictureName();
        setPicture();
        Button selectPictureButton = new Button(productPictureGroup, SWT.PUSH);
        selectPictureButton.setText(_("Select a Picture"));
        selectPictureButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                fileDialog.setFilterPath(Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE"));
                fileDialog.setText(_("Select a Picture"));
                String selectedFile = fileDialog.open();
                if (selectedFile != null) {
                    createPictureName();
                    File directory = new File(picturePath);
                    if (!directory.exists()) directory.mkdirs();
                    File inputFile = new File(selectedFile);
                    File outputFile = new File(filename1 + filename2);
                    try {
                        FileOutputStream out = new FileOutputStream(outputFile);
                        FileInputStream ins = new FileInputStream(inputFile);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int c;
                        while ((c = ins.read()) != -1) {
                            byteArrayOutputStream.write((byte) c);
                        }
                        out.write(byteArrayOutputStream.toByteArray());
                        byteArrayOutputStream.close();
                        ins.close();
                        out.close();
                    } catch (IOException e1) {
                        Logger.logError(e1, "Error copying picture from " + selectedFile + " to " + filename1 + filename2);
                    }
                    setPicture();
                    checkDirty();
                }
            }
        });
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(selectPictureButton);
    }
}