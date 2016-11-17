package edu.utdallas.cs5390.chat.framework.server;

import java.io.*;
import java.util.*;

public class UserInfo {
    public static void main(String args[]) {
        String filename = "";
        Map<String, String> map;

        //Accepts only one text file at a time
        if (args.length == 0) {
            System.out.println("Please, enter a file name!");
        } else {
            filename = args[0];
            map = getUserInfo(filename);
            System.out.println(map.values());
        }
    }

    public static Map<String, String> getUserInfo(String filename) {
        String line = "";
        String username = "";
        String password = "";
        Map<String, String> map = new HashMap<String, String>();

        try {
            FileInputStream fstream = new FileInputStream(filename);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fstream));
            line = buffer.readLine();
            System.out.println(line);
            while (line != null) {
                System.out.println(line);
                String[] split = line.split(" ");
                System.out.println(Arrays.toString(split));
                username = split[0];
                System.out.println(username);
                password = split[1];
                System.out.println(password);
                map.put(username, password);
                System.out.println(map.values());
                line = buffer.readLine();
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("Exception!!");
        }

        return map;
    }
}