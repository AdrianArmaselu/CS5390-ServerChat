package edu.utdallas.cs5390.chat.framework.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Adisor on 10/1/2016.
 */
public class CLIReader {
    private BufferedReader bufferedReader;

    public CLIReader() {
        init();
    }

    private void init() {
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public String readInput() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void close() {
        Utils.closeResource(bufferedReader);
    }
}
