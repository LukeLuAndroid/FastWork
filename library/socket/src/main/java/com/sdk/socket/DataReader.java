package com.sdk.socket;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class DataReader {
    public static String readToEnd(InputStream io, String en) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(io, en));

            StringBuffer lines = new StringBuffer();

            String line = "";
            while ((line = reader.readLine()) != null) {
                lines.append(line);
            }
            return lines.toString();
        } catch (IOException e) {
        } finally {
            close(reader);
        }
        return "";
    }

    public static String readToEndWithOutClose(InputStream io, String en) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(io, en));
        StringBuffer lines = new StringBuffer();
        String line = "";

        while ((line = reader.readLine()) != null) {
            lines.append(line);
        }

        return lines.toString();
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
        }
    }

}
