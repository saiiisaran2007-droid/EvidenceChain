package com.evidencechain.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for computing cryptographic SHA-256 hashes of files and arbitrary data.
 * Uses Java's standard java.security.MessageDigest.
 */
public class FileHasher {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    private FileHasher() {
        // Utility class
    }

    /**
     * Calculates the SHA-256 checksum of the specified file.
     * Uses buffered streaming to avoid loading large files into memory.
     *
     * @param file the local file to hash
     * @return 64-character lowercase hexadecimal SHA-256 string
     * @throws IOException if the file cannot be read
     */
    public static String calculateSHA256(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IOException("File does not exist or is not a valid file: " + (file != null ? file.getAbsolutePath() : "null"));
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Calculates the SHA-256 checksum of a plain string.
     *
     * @param input string to hash
     * @return 64-character lowercase hexadecimal SHA-256 string
     */
    public static String calculateStringSHA256(String input) {
        if (input == null) {
            input = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
