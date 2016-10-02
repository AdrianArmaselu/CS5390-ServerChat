package edu.utdallas.cs5390.chat.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Adisor on 10/1/2016.
 */
public class Utils {

    public static int seconds(int seconds) {
        return seconds * 1000;
    }

    public static int megabytes(int megabytes) {
        return megabytes * 1024 * 1024;
    }

    public static int kilobytes(int kilobytes) {
        return kilobytes * 1024;
    }

    public static void closeResource(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
