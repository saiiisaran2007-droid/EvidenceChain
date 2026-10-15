import React, { createContext, useContext, useState, useEffect } from 'react';
import { 
  INITIAL_EVIDENCE, 
  INITIAL_CUSTODY_EVENTS, 
  INITIAL_LEDGER_BLOCKS, 
  DEMO_USERS,
  GENESIS_PREVIOUS_HASH
} from '../data/mockData';
import { 
  calculateFileSha256, 
  calculateStringSha256, 
  calculateBlockHash, 
  formatTimestamp 
} from '../utils/crypto';

const EvidenceContext = createContext(null);

const STORAGE_KEYS = {
  EVIDENCE: 'evidencechain_web_evidence_v1',
  CUSTODY: 'evidencechain_web_custody_v1',
  LEDGER: 'evidencechain_web_ledger_v1',
  USER: 'evidencechain_web_user_v1'
};

export function EvidenceProvider({ children }) {
  // Initialize state from localStorage or seed mock data
  const [evidenceList, setEvidenceList] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.EVIDENCE);
      return saved ? JSON.parse(saved) : INITIAL_EVIDENCE;
    } catch {
      return INITIAL_EVIDENCE;
    }
  });

  const [custodyEvents, setCustodyEvents] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.CUSTODY);
      return saved ? JSON.parse(saved) : INITIAL_CUSTODY_EVENTS;
    } catch {
      return INITIAL_CUSTODY_EVENTS;
    }
  });

  const [ledgerBlocks, setLedgerBlocks] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.LEDGER);
      return saved ? JSON.parse(saved) : INITIAL_LEDGER_BLOCKS;
    } catch {
      return INITIAL_LEDGER_BLOCKS;
    }
  });

  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.USER);
      return saved ? JSON.parse(saved) : DEMO_USERS[0];
    } catch {
      return DEMO_USERS[0];
    }
  });

  const [isTampered, setIsTampered] = useState(false);
  const [tamperedIndex, setTamperedIndex] = useState(null);

  // Sync to localStorage
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.EVIDENCE, JSON.stringify(evidenceList));
    } catch (e) {
      console.warn('LocalStorage write failed:', e);
    }
  }, [evidenceList]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.CUSTODY, JSON.stringify(custodyEvents));
    } catch (e) {
      console.warn('LocalStorage write failed:', e);
    }
  }, [custodyEvents]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.LEDGER, JSON.stringify(ledgerBlocks));
    } catch (e) {
      console.warn('LocalStorage write failed:', e);
    }
  }, [ledgerBlocks]);

  useEffect(() => {
    try {
      if (currentUser) {
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(currentUser));
      } else {
        localStorage.removeItem(STORAGE_KEYS.USER);
      }
    } catch (e) {
      console.warn('LocalStorage user write failed:', e);
    }
  }, [currentUser]);

  /**
   * Register a new digital evidence item with real-time SHA-256 calculation
   */
  const registerEvidence = async (data, file) => {
    const timestamp = formatTimestamp();
    
    // 1. Calculate genuine SHA-256 hash using browser Web Crypto API
    let fileHash = data.sha256Hash;
    let fileSize = data.fileSize || 0;
    let filePath = data.filePath || (file ? `/evidence/uploads/${file.name}` : '/evidence/manual/unknown.bin');

    if (file) {
      fileHash = await calculateFileSha256(file);
      fileSize = file.size;
      filePath = `/evidence/uploads/${file.name}`;
    }

    // 2. Generate Evidence ID (e.g., EV-0005)
    const nextNum = evidenceList.length + 1;
    const evidenceId = data.evidenceId || `EV-${String(nextNum).padStart(4, '0')}`;

    const newEvidence = {
      evidenceId,
      caseId: data.caseId || 'CASE-2026-GENERAL',
      evidenceType: data.evidenceType || 'Digital Storage',
      description: data.description || 'Forensic artifact registered via Web Console.',
      collector: data.collector || (currentUser ? `${currentUser.name} (Badge #${currentUser.badgeNumber})` : 'Forensics Officer'),
      sourceDevice: data.sourceDevice || 'Unknown Storage Device',
      recordedAt: timestamp,
      filePath,
      fileSize,
      sha256Hash: fileHash,
      status: 'Verified',
      custodyHolder: data.collector || 'Evidence Intake Locker',
      isDemoItem: false // user created in this session!
    };

    // 3. Create initial custody log event
    const newCustodyEvent = {
      eventId: custodyEvents.length + 1,
      evidenceId,
      action: 'REGISTERED_INTO_SYSTEM',
      actorFrom: data.collector || 'Intake Officer',
      actorTo: 'Evidence Intake Vault',
      timestamp,
      notes: data.notes || `Initial forensic ingestion with cryptographic SHA-256 fingerprint: ${fileHash.slice(0, 16)}...`,
      isDemoItem: false
    };

    // 4. Append Block to Hash-Linked Ledger
    const latestBlock = ledgerBlocks[ledgerBlocks.length - 1];
    const newBlockIndex = latestBlock ? latestBlock.blockIndex + 1 : 0;
    const previousHash = latestBlock ? latestBlock.blockHash : GENESIS_PREVIOUS_HASH;
    
    const blockHash = await calculateBlockHash(
      newBlockIndex,
      timestamp,
      evidenceId,
      'REGISTERED',
      fileHash,
      previousHash
    );

    const newLedgerBlock = {
      blockIndex: newBlockIndex,
      timestamp,
      evidenceId,
      action: 'REGISTERED',
      dataHash: fileHash,
      previousHash,
      blockHash,
      isDemoItem: false
    };

    setEvidenceList(prev => [newEvidence, ...prev]);
    setCustodyEvents(prev => [...prev, newCustodyEvent]);
    setLedgerBlocks(prev => [...prev, newLedgerBlock]);

    return { evidence: newEvidence, block: newLedgerBlock };
  };

  /**
   * Transfer chain of custody for an item and append audit block to the ledger
   */
  const transferCustody = async (evidenceId, action, actorFrom, actorTo, notes) => {
    const timestamp = formatTimestamp();
    const eventId = custodyEvents.length + 1;

    const newEvent = {
      eventId,
      evidenceId,
      action: action.toUpperCase().replace(/\s+/g, '_'),
      actorFrom,
      actorTo,
      timestamp,
      notes: notes || 'Chain of custody transfer logged via Web Console.',
      isDemoItem: false
    };

    // Calculate event payload hash for cryptographic ledger
    const payloadString = `${evidenceId}:${action}:${actorFrom}:${actorTo}:${timestamp}`;
    const eventDataHash = await calculateStringSha256(payloadString);

    // Append to Blockchain Ledger
    const latestBlock = ledgerBlocks[ledgerBlocks.length - 1];
    const newBlockIndex = latestBlock ? latestBlock.blockIndex + 1 : 0;
    const previousHash = latestBlock ? latestBlock.blockHash : GENESIS_PREVIOUS_HASH;

    const blockHash = await calculateBlockHash(
      newBlockIndex,
      timestamp,
      evidenceId,
      newEvent.action,
      eventDataHash,
      previousHash
    );

    const newLedgerBlock = {
      blockIndex: newBlockIndex,
      timestamp,
      evidenceId,
      action: newEvent.action,
      dataHash: eventDataHash,
      previousHash,
      blockHash,
      isDemoItem: false
    };

    // Update evidence holder
    setEvidenceList(prev => prev.map(item => {
      if (item.evidenceId === evidenceId) {
        return { ...item, custodyHolder: actorTo };
      }
      return item;
    }));

    setCustodyEvents(prev => [...prev, newEvent]);
    setLedgerBlocks(prev => [...prev, newLedgerBlock]);

    return { event: newEvent, block: newLedgerBlock };
  };

  /**
   * Cryptographically verifies the entire ledger chain.
   * Recalculates every block hash and validates previousHash linking.
   */
  const verifyLedger = async () => {
    const verificationLogs = [];
    let isValid = true;
    let compromisedIndex = null;
    let reason = null;

    for (let i = 0; i < ledgerBlocks.length; i++) {
      const block = ledgerBlocks[i];

      // 1. Recalculate block hash
      const recalculatedHash = await calculateBlockHash(
        block.blockIndex,
        block.timestamp,
        block.evidenceId,
        block.action,
        block.dataHash,
        block.previousHash
      );

      // Verify block hash consistency
      const hashValid = (recalculatedHash.toLowerCase() === block.blockHash.toLowerCase());

      // 2. Verify previousHash chain linking (for blocks after Genesis)
      let linkValid = true;
      if (i > 0) {
        const prevBlock = ledgerBlocks[i - 1];
        linkValid = (block.previousHash.toLowerCase() === prevBlock.blockHash.toLowerCase());
      } else {
        linkValid = (block.previousHash === GENESIS_PREVIOUS_HASH);
      }

      const blockOk = hashValid && linkValid;
      verificationLogs.push({
        blockIndex: block.blockIndex,
        evidenceId: block.evidenceId,
        action: block.action,
        recordedHash: block.blockHash,
        recalculatedHash,
        hashValid,
        linkValid,
        isOk: blockOk
      });

      if (!blockOk && isValid) {
        isValid = false;
        compromisedIndex = block.blockIndex;
        if (!hashValid) {
          reason = `Cryptographic mismatch at Block #${block.blockIndex}: Recalculated SHA-256 does not match recorded block hash!`;
        } else if (!linkValid) {
          reason = `Broken chain linkage at Block #${block.blockIndex}: Previous hash pointer does not match Block #${block.blockIndex - 1}'s block hash!`;
        }
      }
    }

    return {
      isValid,
      totalBlocks: ledgerBlocks.length,
      compromisedIndex,
      reason,
      logs: verificationLogs
    };
  };

  /**
   * Educational simulation: Intentional modification of a block to demonstrate tamper detection
   */
  const simulateTamper = (targetIndex, alteredPayload) => {
    setLedgerBlocks(prev => prev.map(block => {
      if (block.blockIndex === targetIndex) {
        return {
          ...block,
          dataHash: alteredPayload || 'TAMPERED_0000000000000000000000000000000000000000000000000000000000',
          action: 'COMPROMISED_ACTION'
        };
      }
      return block;
    }));
    setIsTampered(true);
    setTamperedIndex(targetIndex);
  };

  /**
   * Restore ledger back to genuine seed records or clean state
   */
  const restoreOriginalLedger = () => {
    setLedgerBlocks(INITIAL_LEDGER_BLOCKS);
    setIsTampered(false);
    setTamperedIndex(null);
  };

  /**
   * Reset all data back to factory demonstration dataset
   */
  const resetAllData = () => {
    localStorage.removeItem(STORAGE_KEYS.EVIDENCE);
    localStorage.removeItem(STORAGE_KEYS.CUSTODY);
    localStorage.removeItem(STORAGE_KEYS.LEDGER);
    setEvidenceList(INITIAL_EVIDENCE);
    setCustodyEvents(INITIAL_CUSTODY_EVENTS);
    setLedgerBlocks(INITIAL_LEDGER_BLOCKS);
    setIsTampered(false);
    setTamperedIndex(null);
  };

  /**
   * Login simulation for SOC Analysts
   */
  const login = (user) => {
    setCurrentUser(user);
  };

  const logout = () => {
    setCurrentUser(null);
  };

  return (
    <EvidenceContext.Provider
      value={{
        evidenceList,
        custodyEvents,
        ledgerBlocks,
        currentUser,
        isTampered,
        tamperedIndex,
        registerEvidence,
        transferCustody,
        verifyLedger,
        simulateTamper,
        restoreOriginalLedger,
        resetAllData,
        login,
        logout
      }}
    >
      {children}
    </EvidenceContext.Provider>
  );
}

export function useEvidence() {
  const context = useContext(EvidenceContext);
  if (!context) {
    throw new Error('useEvidence must be used within an EvidenceProvider');
  }
  return context;
}
