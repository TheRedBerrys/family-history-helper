package com.theredberrys.fhh;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        String username = "", password = "", startId = "", excludeId = "", personLimit = "";
        try {
            String filePath = new File("src/resources/user.txt").getAbsolutePath();
            System.out.println(filePath);
            File file = new File(filePath);
            Scanner reader = new Scanner(file);
            username = reader.nextLine();
            password = reader.nextLine();
            startId = reader.nextLine();
            excludeId = reader.nextLine();
            personLimit = reader.nextLine();
        } catch (Exception e) {
            System.out.println("You have to put your username and password in user.txt");
            System.exit(1);
        }

        List<PersonInfo> persons = FamilysearchHelper.run(username, password, startId, excludeId, Integer.parseInt(personLimit));

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/resources/persons.csv"));
            writer.write("id,hasRecordHints,isTempleReady");
            for (PersonInfo person : persons) {
                writer.newLine();
                writer.write(person.toString());
            }
            writer.close();
        } catch (Exception e) {

        }
    }
}
