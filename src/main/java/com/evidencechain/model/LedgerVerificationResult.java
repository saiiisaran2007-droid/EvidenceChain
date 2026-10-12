package com.evidencechain.model;

/**
 * Encapsulates the cryptographic verification outcome of the blockchain-style ledger.
 */
public class LedgerVerificationResult {

    private final boolean valid;
    private final int totalBlocks;
    private final String statusMessage;
    private final long failedBlockIndex;
    private final String expectedHash;
    private final String actualHash;

    public LedgerVerificationResult(boolean valid, int totalBlocks, String statusMessage) {
        this(valid, totalBlocks, statusMessage, -1, null, null);
    }

    public LedgerVerificationResult(boolean valid, int totalBlocks, String statusMessage,
                                  long failedBlockIndex, String expectedHash, String actualHash) {
        this.valid = valid;
        this.totalBlocks = totalBlocks;
        this.statusMessage = statusMessage;
        this.failedBlockIndex = failedBlockIndex;
        this.expectedHash = expectedHash;
        this.actualHash = actualHash;
    }

    public static LedgerVerificationResult valid(int totalBlocks) {
        return new LedgerVerificationResult(true, totalBlocks, "LEDGER VALID — All cryptographic links and block hashes verified successfully.");
    }

    public static LedgerVerificationResult compromised(int totalBlocks, String reason, long blockIndex,
                                                      String expectedHash, String actualHash) {
        return new LedgerVerificationResult(false, totalBlocks,
                "LEDGER COMPROMISED — " + reason, blockIndex, expectedHash, actualHash);
    }

    public boolean isValid() {
        return valid;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public long getFailedBlockIndex() {
        return failedBlockIndex;
    }

    public String getExpectedHash() {
        return expectedHash;
    }

    public String getActualHash() {
        return actualHash;
    }

    @Override
    public String toString() {
        return "LedgerVerificationResult{" +
                "valid=" + valid +
                ", totalBlocks=" + totalBlocks +
                ", statusMessage='" + statusMessage + '\'' +
                ", failedBlockIndex=" + failedBlockIndex +
                '}';
    }
}
