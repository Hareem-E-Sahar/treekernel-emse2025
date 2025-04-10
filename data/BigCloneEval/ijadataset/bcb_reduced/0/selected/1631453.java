package com.garmin.fit.csv;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;

public class CSVWriter {

    private String fileName;

    private File file;

    private BufferedWriter writer;

    private ArrayList<String> headers;

    private ArrayList<String> values;

    public CSVWriter(String fileName) {
        this.fileName = fileName;
        this.headers = new ArrayList<String>();
        this.values = new ArrayList<String>();
    }

    public void close() {
        try {
            if (writer != null) {
                BufferedReader reader;
                writer.close();
                writer = new BufferedWriter(new FileWriter(fileName));
                for (int i = 0; i < headers.size(); i++) writer.write(headers.get(i) + ",");
                writer.write("\n");
                reader = new BufferedReader(new FileReader(file));
                while (reader.ready()) writer.write(reader.readLine() + "\n");
                reader.close();
                writer.close();
                file.delete();
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        for (int i = 0; i < values.size(); i++) values.set(i, new String(""));
    }

    public void set(String header, Object value) {
        if (header == null) header = "null";
        if (value == null) value = "null";
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).compareTo(header) == 0) {
                values.set(i, value.toString());
                return;
            }
        }
        headers.add(header.toString());
        values.add(value.toString());
    }

    public void writeln() {
        try {
            if (writer == null) {
                file = new File(fileName + ".tmp");
                writer = new BufferedWriter(new FileWriter(file));
            }
            for (int i = 0; i < values.size(); i++) writer.write(values.get(i) + ",");
            writer.write("\n");
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
