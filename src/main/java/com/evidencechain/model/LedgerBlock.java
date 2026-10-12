package com.evidencechain.model;

/**
 * Represents an immutable block in the blockchain-style hash-linked custody ledger.
 */
public class LedgerBlock {

    private long blockIndex;
    private String timestamp;
    private String evidenceId;
    private String action;
    private String dataHash;
    private String previousHash;
    private String blockHash;

    public LedgerBlock() {
    }

    public LedgerBlock(long blockIndex, String timestamp, String evidenceId, String action,
                       String dataHash, String previousHash, String blockHash) {
        this.blockIndex = blockIndex;
        this.timestamp = timestamp;
        this.evidenceId = evidenceId;
        this.action = action;
        this.dataHash = dataHash;
        this.previousHash = previousHash;
        this.blockHash = blockHash;
    }

    public long getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(long blockIndex) {
        this.blockIndex = blockIndex;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public String toString() {
        return "LedgerBlock{" +
                "blockIndex=" + blockIndex +
                ", timestamp='" + timestamp + '\'' +
                ", evidenceId='" + evidenceId + '\'' +
                ", action='" + action + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", blockHash='" + blockHash + '\'' +
                '}';
    }
}
