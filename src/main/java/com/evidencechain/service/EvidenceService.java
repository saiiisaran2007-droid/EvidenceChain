package com.evidencechain.service;

import com.evidencechain.model.CustodyEvent;
import com.evidencechain.model.Evidence;
import com.evidencechain.model.IntegrityVerificationResult;
import com.evidencechain.repository.CustodyRepository;
import com.evidencechain.repository.EvidenceRepository;
import com.evidencechain.util.FileHasher;
import com.evidencechain.util.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service encapsulating evidence validation, SHA-256 fingerprinting, ID generation,
 * custody tracking, and blockchain-style ledger synchronization.
 */
public class EvidenceService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EvidenceRepository evidenceRepository;
    private final HashService hashService;
    private final CustodyRepository custodyRepository;
    private final LedgerService ledgerService;

    public EvidenceService() {
        this.evidenceRepository = new EvidenceRepository();
        this.hashService = new HashService();
        this.custodyRepository = new CustodyRepository();
        this.ledgerService = new LedgerService();
    }

    public EvidenceService(EvidenceRepository evidenceRepository, HashService hashService) {
        this.evidenceRepository = evidenceRepository;
        this.hashService = hashService;
        this.custodyRepository = new CustodyRepository();
        this.ledgerService = new LedgerService();
    }

    public EvidenceService(EvidenceRepository evidenceRepository, HashService hashService,
                           CustodyRepository custodyRepository, LedgerService ledgerService) {
        this.evidenceRepository = evidenceRepository;
        this.hashService = hashService;
        this.custodyRepository = custodyRepository;
        this.ledgerService = ledgerService;
    }

    /**
     * Registers a new digital evidence item with real cryptographic SHA-256 fingerprinting,
     * logs the initial custody event, and anchors the transaction in the hash-linked ledger.
     *
     * @param caseId       the case identifier (e.g. CASE-2026-01)
     * @param evidenceType type of digital media (e.g. Disk Image, Memory Dump)
     * @param description  forensic description/notes
     * @param collector    name of seizing investigator/analyst
     * @param sourceDevice source device or hardware origin
     * @param file         the local file on disk
     * @return the persisted Evidence record
     * @throws IllegalArgumentException if required fields are missing or invalid
     * @throws IOException              if the file cannot be read or hashed
     * @throws SQLException             if database persistence fails
     */
    public Evidence registerEvidence(String caseId, String evidenceType, String description,
                                     String collector, String sourceDevice, File file)
            throws IOException, SQLException {

        // 1. Input Validation
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Case ID is required.");
        }
        if (evidenceType == null || evidenceType.trim().isEmpty()) {
            throw new IllegalArgumentException("Evidence Type is required.");
        }
        if (collector == null || collector.trim().isEmpty()) {
            throw new IllegalArgumentException("Collector / Investigator name is required.");
        }
        if (sourceDevice == null || sourceDevice.trim().isEmpty()) {
            throw new IllegalArgumentException("Source device is required.");
        }
        if (file == null) {
            throw new IllegalArgumentException("A target file must be selected.");
        }
        if (!file.exists()) {
            throw new IOException("The selected file does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("The selected path is a directory, not a regular file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("The file cannot be read due to file system permissions: " + file.getAbsolutePath());
        }

        // 2. Real SHA-256 Fingerprint calculation
        String sha256 = hashService.calculateFileHash(file);

        // 3. Unique Evidence ID generation (e.g. EV-0001)
        int nextSeq = evidenceRepository.getNextEvidenceSequenceNumber();
        String evidenceId = IdGenerator.formatEvidenceId(nextSeq);

        // Guarantee collision safety
        while (evidenceRepository.existsById(evidenceId)) {
            nextSeq++;
            evidenceId = IdGenerator.formatEvidenceId(nextSeq);
        }

        // 4. Construct Entity
        String recordedAt = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        long fileSize = file.length();
        String filePath = file.getAbsolutePath();

        Evidence evidence = new Evidence(
                evidenceId,
                caseId.trim(),
                evidenceType.trim(),
                description != null ? description.trim() : "",
                collector.trim(),
                sourceDevice.trim(),
                recordedAt,
                filePath,
                fileSize,
                sha256
        );

        // 5. Persist to SQLite
        evidenceRepository.save(evidence);
        System.out.println("[EvidenceService] Registered evidence: " + evidenceId + " with SHA-256: " + sha256);

        // 6. Record Initial Custody Event
        CustodyEvent initialCustody = new CustodyEvent(
                evidenceId,
                "REGISTERED",
                collector.trim(),
                "Secure Evidence Vault",
                recordedAt,
                "Initial forensic intake and cryptographic SHA-256 anchoring."
        );
        custodyRepository.save(initialCustody);

        // 7. Commit Block to Blockchain-Style Ledger
        String dataHash = FileHasher.calculateStringSHA256(evidenceId + "|REGISTERED|" + sha256 + "|" + recordedAt);
        ledgerService.recordBlock(evidenceId, "REGISTERED", dataHash);

        return evidence;
    }

    /**
     * Adds a chain-of-custody transfer or event for an existing evidence item,
     * persisting to SQLite and generating a linked ledger block.
     *
     * @param evidenceId target evidence ID
     * @param action     forensic action (e.g. TRANSFERRED, ANALYSIS STARTED, RETURNED)
     * @param actorFrom  relinquishing party / user
     * @param actorTo    receiving party / user
     * @param notes      operational notes / location
     * @return created CustodyEvent
     * @throws SQLException if database error occurs
     */
    public CustodyEvent addCustodyEvent(String evidenceId, String action, String actorFrom,
                                       String actorTo, String notes) throws SQLException {
        if (evidenceId == null || evidenceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Evidence ID is required.");
        }
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Custody action is required.");
        }
        if (actorFrom == null || actorFrom.trim().isEmpty()) {
            throw new IllegalArgumentException("From / Relinquishing user is required.");
        }

        Optional<Evidence> evidenceOpt = evidenceRepository.findById(evidenceId.trim());
        if (evidenceOpt.isEmpty()) {
            throw new IllegalArgumentException("Evidence with ID " + evidenceId + " not found.");
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        CustodyEvent event = new CustodyEvent(
                evidenceId.trim(),
                action.trim(),
                actorFrom.trim(),
                actorTo != null ? actorTo.trim() : "",
                timestamp,
                notes != null ? notes.trim() : ""
        );

        custodyRepository.save(event);

        // Generate data hash and record in ledger
        String dataPayload = evidenceId + "|" + action + "|" + actorFrom + "|" + (actorTo != null ? actorTo : "") + "|" + timestamp;
        String dataHash = FileHasher.calculateStringSHA256(dataPayload);
        ledgerService.recordBlock(evidenceId, action, dataHash);

        System.out.println("[EvidenceService] Recorded custody event: " + action + " on " + evidenceId);
        return event;
    }

    /**
     * Cryptographically verifies the integrity of an evidence record against the physical file on disk.
     * Reads the file, computes new SHA-256 via MessageDigest, and compares to original stored hash.
     *
     * @param evidence the evidence entity to verify
     * @return IntegrityVerificationResult
     */
    public IntegrityVerificationResult verifyEvidenceIntegrity(Evidence evidence) {
        if (evidence == null) {
            throw new IllegalArgumentException("Evidence cannot be null.");
        }

        String now = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        File file = new File(evidence.getFilePath());

        if (!file.exists() || !file.isFile()) {
            return IntegrityVerificationResult.fileNotFound(
                    evidence.getEvidenceId(),
                    evidence.getFilePath(),
                    evidence.getSha256Hash(),
                    now
            );
        }

        try {
            String calculatedHash = hashService.calculateFileHash(file);
            if (calculatedHash.equalsIgnoreCase(evidence.getSha256Hash())) {
                return IntegrityVerificationResult.verified(
                        evidence.getEvidenceId(),
                        evidence.getFilePath(),
                        evidence.getSha256Hash(),
                        now
                );
            } else {
                return IntegrityVerificationResult.tampered(
                        evidence.getEvidenceId(),
                        evidence.getFilePath(),
                        evidence.getSha256Hash(),
                        calculatedHash,
                        now
                );
            }
        } catch (IOException e) {
            return IntegrityVerificationResult.error(
                    evidence.getEvidenceId(),
                    evidence.getFilePath(),
                    evidence.getSha256Hash(),
                    e.getMessage(),
                    now
            );
        }
    }

    /**
     * Calculates the SHA-256 hash for a selected file without saving.
     */
    public String previewFileHash(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IOException("Invalid file for hash preview.");
        }
        return hashService.calculateFileHash(file);
    }

    public List<Evidence> getAllEvidence() throws SQLException {
        return evidenceRepository.findAll();
    }

    public Optional<Evidence> getEvidenceById(String evidenceId) throws SQLException {
        if (evidenceId == null || evidenceId.trim().isEmpty()) {
            return Optional.empty();
        }
        return evidenceRepository.findById(evidenceId.trim());
    }

    public List<Evidence> getRecentEvidence(int limit) throws SQLException {
        return evidenceRepository.findRecent(limit);
    }

    public List<CustodyEvent> getCustodyEventsForEvidence(String evidenceId) throws SQLException {
        return custodyRepository.findByEvidenceId(evidenceId);
    }

    public List<CustodyEvent> getRecentCustodyEvents(int limit) throws SQLException {
        return custodyRepository.findRecent(limit);
    }

    public String getLatestEvidenceStatus(String evidenceId) {
        try {
            return custodyRepository.getLatestAction(evidenceId);
        } catch (SQLException e) {
            return "ACTIVE";
        }
    }

    public int getTotalEvidenceCount() throws SQLException {
        return evidenceRepository.count();
    }

    public int getTotalCasesCount() throws SQLException {
        return evidenceRepository.countDistinctCases();
    }

    public int getVerifiedEvidenceCount() {
        try {
            List<Evidence> list = evidenceRepository.findAll();
            int count = 0;
            for (Evidence e : list) {
                File f = new File(e.getFilePath());
                if (f.exists() && f.isFile()) {
                    String hash = hashService.calculateFileHash(f);
                    if (hash.equalsIgnoreCase(e.getSha256Hash())) {
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    public LedgerService getLedgerService() {
        return ledgerService;
    }

    public CustodyRepository getCustodyRepository() {
        return custodyRepository;
    }
}
