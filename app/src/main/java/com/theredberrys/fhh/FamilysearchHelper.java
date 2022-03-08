package com.theredberrys.fhh;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FamilysearchHelper {
    private final WebDriver webDriver;
    private static final String startingId = "L2HY-CN3"; //""KWX7-QHX";
    private static final String excludeId = "LFGN-QHK";

    public FamilysearchHelper() {
        webDriver = new FirefoxDriver();
    }

    public void start(String username, String password) {
        login(username, password);

        Set<String> ids = new HashSet<>();
        ids.add(startingId);

        List<PersonInfo> persons = new ArrayList<>();
        persons.add(new PersonInfo(startingId));

        for (int i = 0; i < persons.size(); i++) {
            PersonInfo person = persons.get(i);
            updateRecordHints(person);
            updateTemple(person);
            System.out.println(person);
        }
    }

    private void login(String username, String password) {
        goTo("https://www.familysearch.org");

        WebElement input = webDriver.findElement(By.linkText("SIGN IN"));
        input.click();

        WebElement usernameBox = webDriver.findElement(By.id("userName"));
        usernameBox.sendKeys(username);

        WebElement passwordBox = webDriver.findElement(By.id("password"));
        passwordBox.sendKeys(password);

        passwordBox.sendKeys(Keys.ENTER);
        try {
            Thread.sleep(3000); // Give it a bit to load the session
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateRecordHints(PersonInfo person) {
        String url = "https://www.familysearch.org/service/tree/tree-data/record-matches/" +
                person.getId() +
                "/all?hintsOnLiving=true";
        JSONObject json = getJson(url);
        Boolean hasMatches = !json.getJSONObject("data").getJSONArray("matches").isEmpty();
        person.setHasRecordHints(hasMatches);
    }

    private void updateTemple(PersonInfo person) {
        String url = "https://www.familysearch.org/service/tree/tree-data/v8/person/" +
                person.getId() +
                "/details?locale=en&includeTempleRollupStatus=true";
        JSONObject json = getJson(url);
        if (!json.getString("templeRollupStatus").equals("READY")) {
            person.setTempleReady(false);
            return;
        }

        url = "https://www.familysearch.org/service/tree/tree-data/reservations/person/" +
                person.getId() +
                "/ordinances";
        json = getJson(url);
        boolean needsPermission = json.getJSONObject("data").getBoolean("requiresPermission");
        person.setTempleReady(!needsPermission);
    }

    private JSONObject getJson(String url) {
        goTo("view-source:" + url);
        WebElement json = webDriver.findElement(By.tagName("pre"));
        return new JSONObject(json.getText());
    }

    private void goTo(String url) {
        webDriver.navigate().to(url);
    }

    public void quit() {
        webDriver.quit();
    }
}
