/**
 * EvidenceChain Demonstration Dataset
 * 
 * DISCLAIMER:
 * These records represent pre-populated demonstration data for web showcase
 * and local browser-based cryptographic simulation. In the desktop application,
 * persistent records reside in the local SQLite database (evidencechain.db).
 */

export const INITIAL_EVIDENCE = [
  {
    evidenceId: "EV-0001",
    caseId: "CASE-2026-0814",
    evidenceType: "Disk Image",
    description: "Full forensic raw image (DD) of suspect NVMe drive, extracted via Tableau forensic write blocker.",
    collector: "Agent Sarah Connor (Badge #4092)",
    sourceDevice: "Samsung 980 PRO 1TB NVMe SSD (S/N: S5GXNF0R102938J)",
    recordedAt: "2026-03-10 14:22:18",
    filePath: "/cases/2026-0814/images/suspect_nvme_drive0.dd",
    fileSize: 1048576000000, // 1 TB
    sha256Hash: "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
    status: "Verified",
    custodyHolder: "Forensic Lab Vault A-12",
    isDemoItem: true
  },
  {
    evidenceId: "EV-0002",
    caseId: "CASE-2026-0814",
    evidenceType: "Memory Dump",
    description: "Volatile RAM capture obtained via LiME kernel module before machine shutdown.",
    collector: "Analyst David Miller (Badge #3120)",
    sourceDevice: "Dell Precision 5820 Workstation (Host: FIN-WS-09)",
    recordedAt: "2026-03-10 15:05:42",
    filePath: "/cases/2026-0814/memory/ram_capture_live.raw",
    fileSize: 68719476736, // 64 GB
    sha256Hash: "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
    status: "Verified",
    custodyHolder: "Memory Analysis Unit",
    isDemoItem: true
  },
  {
    evidenceId: "EV-0003",
    caseId: "CASE-2026-0922",
    evidenceType: "Network Capture (PCAP)",
    description: "Promiscuous port mirror capture of perimeter firewall interface during exfiltration alert.",
    collector: "SOC Lead Elena Rostova (Badge #1108)",
    sourceDevice: "Palo Alto PA-5220 Interface eth1/2",
    recordedAt: "2026-03-12 08:44:11",
    filePath: "/cases/2026-0922/pcap/exfil_session_filtered.pcapng",
    fileSize: 4294967296, // 4 GB
    sha256Hash: "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a",
    status: "Verified",
    custodyHolder: "Network Forensics Server",
    isDemoItem: true
  },
  {
    evidenceId: "EV-0004",
    caseId: "CASE-2026-1005",
    evidenceType: "Mobile Extraction",
    description: "Cellebrite UFED physical extraction and file system dump of suspect encrypted smartphone.",
    collector: "Detective James Vance (Badge #7731)",
    sourceDevice: "Apple iPhone 14 Pro Max 256GB (IMEI: 359124098234190)",
    recordedAt: "2026-03-14 11:15:33",
    filePath: "/cases/2026-1005/mobile/ufed_physical_extraction.ufdr",
    fileSize: 137438953472, // 128 GB
    sha256Hash: "ef2d127de37b942baad06145e54b0c619a1f22327b2ebbcfbec78f5564afe39d",
    status: "Verified",
    custodyHolder: "Mobile Device Forensics Safe",
    isDemoItem: true
  }
];

export const INITIAL_CUSTODY_EVENTS = [
  {
    eventId: 1,
    evidenceId: "EV-0001",
    action: "COLLECTED_AT_SCENE",
    actorFrom: "Crime Scene Tech Jenkins",
    actorTo: "Agent Sarah Connor",
    timestamp: "2026-03-10 14:22:18",
    notes: "Physical drive removed and secured in anti-static Faraday transport pouch.",
    isDemoItem: true
  },
  {
    eventId: 2,
    evidenceId: "EV-0001",
    action: "IMAGED_BITSTREAM",
    actorFrom: "Agent Sarah Connor",
    actorTo: "Forensic Lab Vault A-12",
    timestamp: "2026-03-10 18:30:00",
    notes: "Forensic raw bitstream image verified with hardware SHA-256 match.",
    isDemoItem: true
  },
  {
    eventId: 3,
    evidenceId: "EV-0002",
    action: "LIVE_RAM_ACQUISITION",
    actorFrom: "Live Incident Response Team",
    actorTo: "Analyst David Miller",
    timestamp: "2026-03-10 15:05:42",
    notes: "Acquired via trusted kernel LiME driver before power termination.",
    isDemoItem: true
  },
  {
    eventId: 4,
    evidenceId: "EV-0002",
    action: "TRANSFERRED_FOR_ANALYSIS",
    actorFrom: "Analyst David Miller",
    actorTo: "Memory Analysis Unit",
    timestamp: "2026-03-11 09:12:15",
    notes: "Transferred securely to isolated Volatility 3 analysis sandbox.",
    isDemoItem: true
  },
  {
    eventId: 5,
    evidenceId: "EV-0003",
    action: "INGESTED_TO_EVIDENCE_LOCKER",
    actorFrom: "SOC Lead Elena Rostova",
    actorTo: "Network Forensics Server",
    timestamp: "2026-03-12 08:44:11",
    notes: "PCAP dump sealed with SHA-256 integrity checksum.",
    isDemoItem: true
  },
  {
    eventId: 6,
    evidenceId: "EV-0004",
    action: "REGISTERED_PHYSICAL_EXTRACTION",
    actorFrom: "Detective James Vance",
    actorTo: "Mobile Device Forensics Safe",
    timestamp: "2026-03-14 11:15:33",
    notes: "UFED report generated and registered into chain of custody.",
    isDemoItem: true
  }
];

export const GENESIS_PREVIOUS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

// Pre-calculated seed blockchain ledger starting at Block 0 (Genesis)
export const INITIAL_LEDGER_BLOCKS = [
  {
    blockIndex: 0,
    timestamp: "2026-03-01 00:00:00",
    evidenceId: "GENESIS",
    action: "GENESIS_INITIALIZATION",
    dataHash: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    previousHash: GENESIS_PREVIOUS_HASH,
    blockHash: "11a5b8214371900115e58145a329d2f2d9369d7211ec1aa2377c08fa5f617637",
    isDemoItem: true
  },
  {
    blockIndex: 1,
    timestamp: "2026-03-10 14:22:18",
    evidenceId: "EV-0001",
    action: "REGISTERED",
    dataHash: "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
    previousHash: "11a5b8214371900115e58145a329d2f2d9369d7211ec1aa2377c08fa5f617637",
    blockHash: "4d7c0bb39e2467d1d2834b6b6697a2e5d9575239a06f3c559869a19d45802194",
    isDemoItem: true
  },
  {
    blockIndex: 2,
    timestamp: "2026-03-10 15:05:42",
    evidenceId: "EV-0002",
    action: "REGISTERED",
    dataHash: "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
    previousHash: "4d7c0bb39e2467d1d2834b6b6697a2e5d9575239a06f3c559869a19d45802194",
    blockHash: "8ab23c6f1122df4b03746ee453d10523bb88939029a1b55928a0e37bcbb91452",
    isDemoItem: true
  },
  {
    blockIndex: 3,
    timestamp: "2026-03-11 09:12:15",
    evidenceId: "EV-0002",
    action: "TRANSFERRED",
    dataHash: "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0",
    previousHash: "8ab23c6f1122df4b03746ee453d10523bb88939029a1b55928a0e37bcbb91452",
    blockHash: "c01892bc54a0f44389d0234bce719584d4361596200beee109845dafa12411cc",
    isDemoItem: true
  },
  {
    blockIndex: 4,
    timestamp: "2026-03-12 08:44:11",
    evidenceId: "EV-0003",
    action: "REGISTERED",
    dataHash: "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a",
    previousHash: "c01892bc54a0f44389d0234bce719584d4361596200beee109845dafa12411cc",
    blockHash: "0a89de41e6c3bc5e24391b10a514d4ef97621fec52077ef29302e11894a821e3",
    isDemoItem: true
  },
  {
    blockIndex: 5,
    timestamp: "2026-03-14 11:15:33",
    evidenceId: "EV-0004",
    action: "REGISTERED",
    dataHash: "ef2d127de37b942baad06145e54b0c619a1f22327b2ebbcfbec78f5564afe39d",
    previousHash: "0a89de41e6c3bc5e24391b10a514d4ef97621fec52077ef29302e11894a821e3",
    blockHash: "7b489a2bc1d84f09827e852d0619208a01fe9823467c523ab301ec980562e811",
    isDemoItem: true
  }
];

export const DEMO_USERS = [
  {
    id: "analyst-1",
    name: "Special Agent Sarah Connor",
    badgeNumber: "4092",
    role: "Senior Forensic Examiner",
    clearance: "TOP SECRET // FORENSIC-SCI",
    station: "Digital Forensics Unit (DFU)"
  },
  {
    id: "analyst-2",
    name: "David Miller",
    badgeNumber: "3120",
    role: "SOC Incident Responder",
    clearance: "SECRET // CIRT",
    station: "Cyber Defense Center (CDC)"
  },
  {
    id: "custodian-1",
    name: "Elena Rostova",
    badgeNumber: "1108",
    role: "Evidence Vault Custodian",
    clearance: "RESTRICTED // CHAIN-LEGAL",
    station: "Central Evidence Locker"
  }
];
