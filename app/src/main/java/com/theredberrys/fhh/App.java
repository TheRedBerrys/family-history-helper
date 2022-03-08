package com.theredberrys.fhh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        String username = "", password = "";
        try {
            String filePath = new File("src/resources/user.txt").getAbsolutePath();
            System.out.println(filePath);
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            username = reader.nextLine();
            password = reader.nextLine();
        } catch (Exception e) {
            System.out.println("You have to put your username and password in user.txt");
            System.exit(1);
        }

        FamilysearchHelper helper = new FamilysearchHelper();
        helper.start(username, password);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        helper.quit();
    }
}
