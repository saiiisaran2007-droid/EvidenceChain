package com.evidencechain.model;

/**
 * Represents a chain-of-custody event for an evidence item.
 */
public class CustodyEvent {

    private long eventId;
    private String evidenceId;
    private String action;
    private String actorFrom;
    private String actorTo;
    private String timestamp;
    private String notes;

    public CustodyEvent() {
    }

    public CustodyEvent(long eventId, String evidenceId, String action, String actorFrom,
                        String actorTo, String timestamp, String notes) {
        this.eventId = eventId;
        this.evidenceId = evidenceId;
        this.action = action;
        this.actorFrom = actorFrom;
        this.actorTo = actorTo;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public CustodyEvent(String evidenceId, String action, String actorFrom,
                        String actorTo, String timestamp, String notes) {
        this.evidenceId = evidenceId;
        this.action = action;
        this.actorFrom = actorFrom;
        this.actorTo = actorTo;
        this.timestamp = timestamp;
        this.notes = notes;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActorFrom() {
        return actorFrom;
    }

    public void setActorFrom(String actorFrom) {
        this.actorFrom = actorFrom;
    }

    public String getActorTo() {
        return actorTo;
    }

    public void setActorTo(String actorTo) {
        this.actorTo = actorTo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CustodyEvent{" +
                "eventId=" + eventId +
                ", evidenceId='" + evidenceId + '\'' +
                ", action='" + action + '\'' +
                ", actorFrom='" + actorFrom + '\'' +
                ", actorTo='" + actorTo + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
