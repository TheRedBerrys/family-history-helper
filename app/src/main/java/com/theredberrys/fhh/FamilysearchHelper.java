package com.theredberrys.fhh;

import com.google.common.base.Strings;
import org.json.JSONArray;
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
import java.util.concurrent.TimeUnit;

public class FamilysearchHelper {
    private final WebDriver webDriver;
    private final String username, password;
    private final List<PersonInfo> persons;
    private final Set<String> ids;
    private final int personLimit;

    public static List<PersonInfo> run(String username, String password,
                           String startingId, String excludeId,
                           int personLimit) {
        FamilysearchHelper instance = new FamilysearchHelper(username, password, startingId, excludeId, personLimit);
        return instance.start();
    }

    private FamilysearchHelper(String username, String password,
                               String startingId, String excludeId,
                               int personLimit) {
        this.username = username;
        this.password = password;

        this.webDriver = new FirefoxDriver();
        this.persons = new ArrayList<>();
        this.ids = new HashSet<>();

        this.ids.add(startingId);
        this.persons.add(new PersonInfo(startingId));
        this.ids.add("UNKNOWN"); // Used for empty parents/spouses
        if (!Strings.isNullOrEmpty(excludeId)) {
            this.ids.add(excludeId);
        }

        this.personLimit = personLimit;
    }

    private List<PersonInfo> start() {
        login(username, password);

        for (int i = 0; i < persons.size() && i < personLimit; i++) {
            PersonInfo person = persons.get(i);
            updateDetails(person);
            updateRecordHints(person);
            updateTemple(person);
            System.out.println(person);
        }

        quit();

        return persons;
    }

    private void login(String username, String password) {
        goTo("https://www.familysearch.org", 1000);

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

    private void updateDetails(PersonInfo person) {
        String url = "https://www.familysearch.org/service/tree/tree-data/v8/person/" +
                person.getId() +
                "/details?locale=en&includeTempleRollupStatus=true";
        JSONObject json = getJson(url);
        if (!json.getString("templeRollupStatus").equals("READY")) {
            person.setTempleReady(false);
        }

        JSONArray parents = json.optJSONArray("parents");
        for (int i = 0; parents != null && i < parents.length(); i++) {
            JSONObject parent1 = parents.getJSONObject(i).optJSONObject("parent1");
            if (parent1 != null) {
                add(parent1.getString("id"));
            }
            JSONObject parent2 = parents.getJSONObject(i).optJSONObject("parent2");
            if (parent2 != null) {
                add(parent2.getString("id"));
            }
        }

        JSONArray spouses = json.optJSONArray("spouses");
        for (int i = 0; spouses != null && i < spouses.length(); i++) {
            JSONObject parent1 = spouses.getJSONObject(i).optJSONObject("parent1");
            if (parent1 != null) {
                add(parent1.getString("id"));
            }
            JSONObject parent2 = spouses.getJSONObject(i).optJSONObject("parent2");
            if (parent2 != null) {
                add(parent2.getString("id"));
            }

            JSONArray children = spouses.getJSONObject(i).optJSONArray("children");
            for (int j = 0; children != null && j < children.length(); j++) {
                JSONObject child = children.getJSONObject(j).optJSONObject("child");
                if (child != null) {
                    add(child.getString("id"));
                }
            }
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
        if (person.getTempleReady() != null && !person.getTempleReady()) {
            return;
        }

        String url = "https://www.familysearch.org/service/tree/tree-data/reservations/person/" +
                person.getId() +
                "/ordinances";
        JSONObject json = getJson(url);
        boolean needsPermission = json.getJSONObject("data").getBoolean("requiresPermission");
        person.setTempleReady(!needsPermission);
    }

    private void add(String id) {
        if (Strings.isNullOrEmpty(id) || ids.contains(id)) {
            return;
        }

        ids.add(id);

        String url = "https://www.familysearch.org/service/tree/tree-data/user-relationship/person/" + id;
        JSONObject json = getJson(url, 2);
        if (json == null) {
            return;
        }

        persons.add(new PersonInfo(id));
    }

    private JSONObject getJson(String url) {
        return getJson(url, 1000);
    }

    private JSONObject getJson(String url, long timeout) {
        try {
            goTo("view-source:" + url, timeout);
        } catch (Exception ex) {
            return null;
        }
        WebElement json = webDriver.findElement(By.tagName("pre"));
        try {
            return new JSONObject(json.getText());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void goTo(String url, long timeout) {
        webDriver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
        webDriver.navigate().to(url);
    }

    public void quit() {
        webDriver.quit();
    }
}
