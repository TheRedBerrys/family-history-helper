package com.theredberrys.fhh;


public class PersonInfo {
    private String id;
    private Boolean hasRecordHints;
    private Boolean isTempleReady;

    public PersonInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getHasRecordHints() {
        return hasRecordHints;
    }

    public void setHasRecordHints(Boolean hasRecordHints) {
        this.hasRecordHints = hasRecordHints;
    }

    public Boolean getTempleReady() {
        return isTempleReady;
    }

    public void setTempleReady(Boolean templeReady) {
        isTempleReady = templeReady;
    }
}
