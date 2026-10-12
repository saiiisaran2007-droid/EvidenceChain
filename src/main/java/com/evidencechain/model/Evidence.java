package com.evidencechain.model;

/**
 * Represents a digital evidence item registered in the EvidenceChain system.
 */
public class Evidence {

    private String evidenceId;
    private String caseId;
    private String evidenceType;
    private String description;
    private String collector;
    private String sourceDevice;
    private String recordedAt;
    private String filePath;
    private long fileSize;
    private String sha256Hash;

    public Evidence() {
    }

    public Evidence(String evidenceId, String caseId, String evidenceType, String description,
                    String collector, String sourceDevice, String recordedAt, String filePath,
                    long fileSize, String sha256Hash) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.evidenceType = evidenceType;
        this.description = description;
        this.collector = collector;
        this.sourceDevice = sourceDevice;
        this.recordedAt = recordedAt;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.sha256Hash = sha256Hash;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCollector() {
        return collector;
    }

    public void setCollector(String collector) {
        this.collector = collector;
    }

    public String getSourceDevice() {
        return sourceDevice;
    }

    public void setSourceDevice(String sourceDevice) {
        this.sourceDevice = sourceDevice;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    @Override
    public String toString() {
        return "Evidence{" +
                "evidenceId='" + evidenceId + '\'' +
                ", caseId='" + caseId + '\'' +
                ", evidenceType='" + evidenceType + '\'' +
                ", collector='" + collector + '\'' +
                ", sourceDevice='" + sourceDevice + '\'' +
                ", recordedAt='" + recordedAt + '\'' +
                ", sha256Hash='" + sha256Hash + '\'' +
                '}';
    }
}
