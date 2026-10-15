/**
 * EvidenceChain Web Crypto Utilities
 * Uses the native browser Web Crypto API (SubtleCrypto) for cryptographic SHA-256 calculation.
 */

/**
 * Calculates the SHA-256 hash of a string using browser Web Crypto API.
 * @param {string} text
 * @returns {Promise<string>} Hexadecimal SHA-256 hash
 */
export async function calculateStringSha256(text) {
  const encoder = new TextEncoder();
  const data = encoder.encode(text);
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

/**
 * Calculates the SHA-256 hash of a File object using browser Web Crypto API.
 * @param {File|Blob} file
 * @returns {Promise<string>} Hexadecimal SHA-256 hash
 */
export async function calculateFileSha256(file) {
  const arrayBuffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

/**
 * Matches Java's LedgerService.calculateBlockHash algorithm:
 * blockIndex + ":" + timestamp + ":" + evidenceId + ":" + action + ":" + dataHash + ":" + previousHash
 * @param {number} blockIndex
 * @param {string} timestamp
 * @param {string} evidenceId
 * @param {string} action
 * @param {string} dataHash
 * @param {string} previousHash
 * @returns {Promise<string>}
 */
export async function calculateBlockHash(blockIndex, timestamp, evidenceId, action, dataHash, previousHash) {
  const payload = `${blockIndex}:${timestamp}:${evidenceId}:${action}:${dataHash}:${previousHash}`;
  return await calculateStringSha256(payload);
}

/**
 * Formats byte size into human readable string (KB, MB, GB).
 * @param {number} bytes
 * @returns {string}
 */
export function formatBytes(bytes) {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * Formats a Date object or timestamp into standard Forensic Timestamp (YYYY-MM-DD HH:mm:ss).
 * @param {Date|string|number} [dateInput]
 * @returns {string}
 */
export function formatTimestamp(dateInput) {
  const date = dateInput ? new Date(dateInput) : new Date();
  const pad = (n) => String(n).padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const seconds = pad(date.getSeconds());
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * Truncates a 64-char SHA-256 hash for display.
 * @param {string} hash
 * @param {number} [startChars=8]
 * @param {number} [endChars=8]
 * @returns {string}
 */
export function truncateHash(hash, startChars = 8, endChars = 8) {
  if (!hash) return '';
  if (hash.length <= startChars + endChars) return hash;
  return `${hash.slice(0, startChars)}...${hash.slice(-endChars)}`;
}

/**
 * Copies string to clipboard and returns status.
 * @param {string} text
 * @returns {Promise<boolean>}
 */
export async function copyToClipboard(text) {
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
    // Fallback
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    return true;
  } catch (err) {
    console.error('Failed to copy to clipboard', err);
    return false;
  }
}
