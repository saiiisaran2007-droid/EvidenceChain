package com.evidencechain.service;

import com.evidencechain.util.FileHasher;

import java.io.File;
import java.io.IOException;

/**
 * Service providing SHA-256 cryptographic hashing operations for files and metadata.
 */
public class HashService {

    /**
     * Calculates the SHA-256 hash of a local file using java.security.MessageDigest.
     *
     * @param file the target file
     * @return 64-character lowercase hexadecimal hash
     * @throws IOException if the file does not exist or cannot be read
     */
    public String calculateFileHash(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Target file cannot be null");
        }
        return FileHasher.calculateSHA256(file);
    }

    /**
     * Computes the SHA-256 hash of a string payload (used for blockchain block verification).
     *
     * @param data the data string to hash
     * @return 64-character lowercase hexadecimal hash
     */
    public String calculateDataHash(String data) {
        return FileHasher.calculateStringSHA256(data);
    }
}
