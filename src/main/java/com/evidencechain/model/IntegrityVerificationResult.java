package com.evidencechain.model;

/**
 * Encapsulates the cryptographic verification outcome of a single evidence file on disk.
 */
public class IntegrityVerificationResult {

    public enum Status {
        INTEGRITY_VERIFIED,
        TAMPER_DETECTED,
        FILE_NOT_FOUND,
        READ_ERROR
    }

    private final String evidenceId;
    private final String filePath;
    private final String storedHash;
    private final String calculatedHash;
    private final Status status;
    private final String message;
    private final String verifiedAt;

    public IntegrityVerificationResult(String evidenceId, String filePath, String storedHash,
                                       String calculatedHash, Status status, String message, String verifiedAt) {
        this.evidenceId = evidenceId;
        this.filePath = filePath;
        this.storedHash = storedHash;
        this.calculatedHash = calculatedHash;
        this.status = status;
        this.message = message;
        this.verifiedAt = verifiedAt;
    }

    public static IntegrityVerificationResult verified(String evidenceId, String filePath, String hash, String verifiedAt) {
        return new IntegrityVerificationResult(
                evidenceId, filePath, hash, hash,
                Status.INTEGRITY_VERIFIED,
                "INTEGRITY VERIFIED — File SHA-256 exactly matches original registration fingerprint.",
                verifiedAt
        );
    }

    public static IntegrityVerificationResult tampered(String evidenceId, String filePath, String storedHash,
                                                       String currentHash, String verifiedAt) {
        return new IntegrityVerificationResult(
                evidenceId, filePath, storedHash, currentHash,
                Status.TAMPER_DETECTED,
                "TAMPER DETECTED — File SHA-256 differs from original registration fingerprint! The file has been altered.",
                verifiedAt
        );
    }

    public static IntegrityVerificationResult fileNotFound(String evidenceId, String filePath, String storedHash, String verifiedAt) {
        return new IntegrityVerificationResult(
                evidenceId, filePath, storedHash, null,
                Status.FILE_NOT_FOUND,
                "TAMPER DETECTED — File missing or inaccessible at stored path: " + filePath,
                verifiedAt
        );
    }

    public static IntegrityVerificationResult error(String evidenceId, String filePath, String storedHash, String errorMsg, String verifiedAt) {
        return new IntegrityVerificationResult(
                evidenceId, filePath, storedHash, null,
                Status.READ_ERROR,
                "TAMPER DETECTED — I/O Error while verifying file: " + errorMsg,
                verifiedAt
        );
    }

    public boolean isVerified() {
        return status == Status.INTEGRITY_VERIFIED;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStoredHash() {
        return storedHash;
    }

    public String getCalculatedHash() {
        return calculatedHash;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getVerifiedAt() {
        return verifiedAt;
    }
}
