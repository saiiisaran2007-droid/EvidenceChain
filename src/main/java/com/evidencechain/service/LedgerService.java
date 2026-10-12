package com.evidencechain.service;

import com.evidencechain.model.LedgerBlock;
import com.evidencechain.model.LedgerVerificationResult;
import com.evidencechain.repository.LedgerRepository;
import com.evidencechain.util.FileHasher;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service managing the Blockchain-Style Hash-Linked Ledger.
 * Enforces cryptographic previous-hash linking and recalculation-based verification.
 */
public class LedgerService {

    public static final String GENESIS_PREVIOUS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LedgerRepository ledgerRepository;

    public LedgerService() {
        this.ledgerRepository = new LedgerRepository();
        ensureGenesisBlock();
    }

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
        ensureGenesisBlock();
    }

    /**
     * Ensures that Block 0 (Genesis block) exists in the database.
     */
    public synchronized void ensureGenesisBlock() {
        try {
            if (ledgerRepository.count() == 0) {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                String evidenceId = "GENESIS";
                String action = "GENESIS";
                String dataHash = FileHasher.calculateStringSHA256("EVIDENCECHAIN_GENESIS_ROOT_PAYLOAD");
                String previousHash = GENESIS_PREVIOUS_HASH;
                String blockHash = calculateBlockHash(0, timestamp, evidenceId, action, dataHash, previousHash);

                LedgerBlock genesisBlock = new LedgerBlock(
                        0,
                        timestamp,
                        evidenceId,
                        action,
                        dataHash,
                        previousHash,
                        blockHash
                );
                ledgerRepository.save(genesisBlock);
                System.out.println("[LedgerService] Genesis block initialized at index 0 with hash: " + blockHash);
            }
        } catch (SQLException e) {
            System.err.println("[LedgerService] Failed to ensure genesis block: " + e.getMessage());
        }
    }

    /**
     * Appends a new hash-linked block to the blockchain-style ledger.
     *
     * @param evidenceId the target evidence ID (e.g. EV-0001)
     * @param action     the forensic action (e.g. REGISTERED, TRANSFERRED)
     * @param dataHash   cryptographic hash of the event or evidence payload
     * @return the appended LedgerBlock
     * @throws SQLException if database write fails
     */
    public synchronized LedgerBlock recordBlock(String evidenceId, String action, String dataHash) throws SQLException {
        ensureGenesisBlock();

        Optional<LedgerBlock> latestOpt = ledgerRepository.getLatestBlock();
        if (latestOpt.isEmpty()) {
            throw new IllegalStateException("Genesis block missing from ledger.");
        }

        LedgerBlock latest = latestOpt.get();
        long newIndex = latest.getBlockIndex() + 1;
        String previousHash = latest.getBlockHash();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        String blockHash = calculateBlockHash(newIndex, timestamp, evidenceId, action, dataHash, previousHash);

        LedgerBlock newBlock = new LedgerBlock(
                newIndex,
                timestamp,
                evidenceId,
                action,
                dataHash,
                previousHash,
                blockHash
        );

        ledgerRepository.save(newBlock);
        System.out.println("[LedgerService] Appended Block #" + newIndex + " [" + action + " on " + evidenceId + "] Hash: " + blockHash);
        return newBlock;
    }

    /**
     * Computes the SHA-256 block hash over the block's concatenated payload fields.
     */
    public static String calculateBlockHash(long blockIndex, String timestamp, String evidenceId,
                                            String action, String dataHash, String previousHash) {
        String payload = blockIndex + ":" + timestamp + ":" + evidenceId + ":" + action + ":" + dataHash + ":" + previousHash;
        return FileHasher.calculateStringSHA256(payload);
    }

    /**
     * Cryptographically verifies the entire ledger chain.
     * Recalculates every block hash and checks the previousHash linking relationship.
     *
     * @return LedgerVerificationResult containing VALID or COMPROMISED diagnosis
     */
    public LedgerVerificationResult verifyLedger() {
        try {
            List<LedgerBlock> chain = ledgerRepository.findAll();
            if (chain.isEmpty()) {
                ensureGenesisBlock();
                chain = ledgerRepository.findAll();
            }

            if (chain.isEmpty()) {
                return LedgerVerificationResult.compromised(0, "No blocks exist in the ledger.", 0, "", "");
            }

            // 1. Verify Genesis Block
            LedgerBlock genesis = chain.get(0);
            if (genesis.getBlockIndex() != 0) {
                return LedgerVerificationResult.compromised(chain.size(),
                        "Genesis block index is not 0 (found " + genesis.getBlockIndex() + ")",
                        genesis.getBlockIndex(), "0", String.valueOf(genesis.getBlockIndex()));
            }

            if (!GENESIS_PREVIOUS_HASH.equals(genesis.getPreviousHash())) {
                return LedgerVerificationResult.compromised(chain.size(),
                        "Genesis block previous hash is invalid.",
                        0, GENESIS_PREVIOUS_HASH, genesis.getPreviousHash());
            }

            String recomputedGenesis = calculateBlockHash(
                    genesis.getBlockIndex(),
                    genesis.getTimestamp(),
                    genesis.getEvidenceId(),
                    genesis.getAction(),
                    genesis.getDataHash(),
                    genesis.getPreviousHash()
            );

            if (!recomputedGenesis.equals(genesis.getBlockHash())) {
                return LedgerVerificationResult.compromised(chain.size(),
                        "Genesis block content hash mismatch. Tampering detected in Block #0.",
                        0, recomputedGenesis, genesis.getBlockHash());
            }

            // 2. Verify all subsequent blocks in sequence
            for (int i = 1; i < chain.size(); i++) {
                LedgerBlock prev = chain.get(i - 1);
                LedgerBlock curr = chain.get(i);

                // Index sequence verification
                if (curr.getBlockIndex() != prev.getBlockIndex() + 1) {
                    return LedgerVerificationResult.compromised(chain.size(),
                            "Block index gap detected between Block #" + prev.getBlockIndex() + " and Block #" + curr.getBlockIndex(),
                            curr.getBlockIndex(), String.valueOf(prev.getBlockIndex() + 1), String.valueOf(curr.getBlockIndex()));
                }

                // Previous hash link verification
                if (!curr.getPreviousHash().equals(prev.getBlockHash())) {
                    return LedgerVerificationResult.compromised(chain.size(),
                            "Broken chain link at Block #" + curr.getBlockIndex() + ". Previous hash does not match hash of Block #" + prev.getBlockIndex(),
                            curr.getBlockIndex(), prev.getBlockHash(), curr.getPreviousHash());
                }

                // Content hash recomputation verification
                String recomputedHash = calculateBlockHash(
                        curr.getBlockIndex(),
                        curr.getTimestamp(),
                        curr.getEvidenceId(),
                        curr.getAction(),
                        curr.getDataHash(),
                        curr.getPreviousHash()
                );

                if (!recomputedHash.equals(curr.getBlockHash())) {
                    return LedgerVerificationResult.compromised(chain.size(),
                            "Cryptographic hash mismatch in Block #" + curr.getBlockIndex() + ". Block content has been tampered with.",
                            curr.getBlockIndex(), recomputedHash, curr.getBlockHash());
                }
            }

            return LedgerVerificationResult.valid(chain.size());

        } catch (SQLException e) {
            return LedgerVerificationResult.compromised(0,
                    "Database error while querying ledger: " + e.getMessage(), -1, "", "");
        }
    }

    public List<LedgerBlock> getAllBlocks() throws SQLException {
        return ledgerRepository.findAll();
    }

    public int getTotalBlockCount() throws SQLException {
        return ledgerRepository.count();
    }
}
