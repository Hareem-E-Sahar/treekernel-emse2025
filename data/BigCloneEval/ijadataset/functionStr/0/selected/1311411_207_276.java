public class Test {    public void testSave() throws Exception {
        MobileDB database = new MobileDB();
        database.setName("workout");
        database.setModificationDate(Timestamp.valueOf("2006-12-08 00:00:00"));
        database.setCreationDate(Timestamp.valueOf("2006-12-08 00:00:00"));
        PreferencesRecord preferencesRecord = new PreferencesRecord();
        preferencesRecord.setNote("Workout database.");
        database.setPreferencesRecord(preferencesRecord);
        FieldDisplaySizesRecord fieldDisplaySizesRecord = new FieldDisplaySizesRecord();
        database.setFieldDisplaySizesRecord(fieldDisplaySizesRecord);
        FieldLabelsRecord fieldLabelsRecord = new FieldLabelsRecord();
        ArrayList<String> fieldLabels = new ArrayList<String>();
        fieldLabels.add("Sequence");
        fieldLabels.add("Exercise");
        fieldLabels.add("Weight");
        fieldLabels.add("Repetitions");
        fieldLabels.add("Notes");
        fieldLabels.add("Date");
        fieldLabels.add("Time");
        fieldLabelsRecord.setFieldLabels(fieldLabels);
        database.setFieldLabelsRecord(fieldLabelsRecord);
        FieldDefinitionsRecord fieldDefinitionsRecord = new FieldDefinitionsRecord();
        ArrayList<Definition> definitions = new ArrayList<Definition>();
        SequenceDefinition sequenceDefinition = new SequenceDefinition();
        sequenceDefinition.setIncrement(1d);
        sequenceDefinition.setInitialValue(2d);
        definitions.add(sequenceDefinition);
        ListDefinition exercises = new ListDefinition();
        ArrayList<ListOption> options = new ArrayList<ListOption>();
        options.add(new ListOption("Barbell Bench Press"));
        options.add(new ListOption("Weighted Chest Dip"));
        options.add(new ListOption("Dumbbell Fly"));
        options.add(new ListOption("Lever Pec Deck Fly"));
        options.add(new ListOption("New"));
        exercises.setOptions(options);
        definitions.add(exercises);
        definitions.add(new NumberDefinition());
        definitions.add(new NumberDefinition());
        definitions.add(new TextDefinition());
        DateDefinition dateDefinition = new DateDefinition();
        dateDefinition.setDefaultToCurrentDate(true);
        definitions.add(dateDefinition);
        TimeDefinition timeDefinition = new TimeDefinition();
        timeDefinition.setDefaultToCurrentTime(true);
        definitions.add(timeDefinition);
        fieldDefinitionsRecord.setFieldDefinitions(definitions);
        database.setFieldDefinitionsRecord(fieldDefinitionsRecord);
        DataRecord dataRecord = new DataRecord();
        ArrayList<Type> dataFields = new ArrayList<Type>();
        dataFields.add(new Number(1));
        dataFields.add(new Text("Barbell Bench Press"));
        dataFields.add(new Number(165));
        dataFields.add(new Number(10));
        dataFields.add(new Text(""));
        dataFields.add(new Date(Timestamp.valueOf("2006-12-08 00:00:00")));
        dataFields.add(new Time(53408));
        dataRecord.setFields(dataFields);
        database.setDataRecords(Arrays.asList(dataRecord));
        PalmWriter writer = new PalmWriter();
        writer.save(new FileOutputStream(FILE), database);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        FileInputStream stream = new FileInputStream(new File(FILE));
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = stream.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        String hash = Utilities.byteArrayToHexString(digest.digest());
        assertEquals(MD5SUM, hash);
    }
}