import React, { useState } from 'react';
import { 
  FilePlus2, 
  UploadCloud, 
  FileCheck2, 
  Fingerprint, 
  ShieldCheck, 
  Cpu, 
  AlertCircle, 
  ArrowRight, 
  CheckCircle2, 
  Copy, 
  FolderArchive,
  HardDrive
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { calculateFileSha256, formatBytes } from '../utils/crypto';
import { HashPill } from '../components/Badge';

export default function RegisterPage({ setCurrentPage, setSelectedEvidenceId }) {
  const { registerEvidence, currentUser } = useEvidence();

  const [selectedFile, setSelectedFile] = useState(null);
  const [computingHash, setComputingHash] = useState(false);
  const [calculatedHash, setCalculatedHash] = useState('');
  const [hashCalculationTime, setHashCalculationTime] = useState(null);

  // Form State
  const [caseId, setCaseId] = useState('CASE-2026-1020');
  const [evidenceType, setEvidenceType] = useState('Disk Image');
  const [sourceDevice, setSourceDevice] = useState('');
  const [collector, setCollector] = useState(
    currentUser ? `${currentUser.name} (Badge #${currentUser.badgeNumber})` : 'Special Agent Sarah Connor'
  );
  const [description, setDescription] = useState('');
  const [notes, setNotes] = useState('');

  // Submission State
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [registeredResult, setRegisteredResult] = useState(null);
  const [dragActive, setDragActive] = useState(false);

  // Handle file selection and compute Web Crypto SHA-256
  const handleFileChange = async (file) => {
    if (!file) return;
    setSelectedFile(file);
    setComputingHash(true);
    setCalculatedHash('');

    const startTime = performance.now();
    try {
      const hash = await calculateFileSha256(file);
      const endTime = performance.now();
      setCalculatedHash(hash);
      setHashCalculationTime((endTime - startTime).toFixed(1));
      
      // Auto-populate source device if empty
      if (!sourceDevice) {
        setSourceDevice(`Physical Media / ${file.name}`);
      }
    } catch (err) {
      console.error('Web Crypto calculation error:', err);
    } finally {
      setComputingHash(false);
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileChange(e.dataTransfer.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!calculatedHash && !selectedFile) {
      alert('Please select a file to compute its cryptographic SHA-256 fingerprint.');
      return;
    }

    setIsSubmitting(true);
    try {
      const res = await registerEvidence(
        {
          caseId: caseId.trim() || 'CASE-GENERAL',
          evidenceType,
          sourceDevice: sourceDevice.trim() || (selectedFile ? selectedFile.name : 'Unknown Storage Device'),
          collector: collector.trim(),
          description: description.trim() || `Forensic acquisition of ${selectedFile ? selectedFile.name : 'digital media'}.`,
          notes: notes.trim(),
          sha256Hash: calculatedHash,
          fileSize: selectedFile ? selectedFile.size : 0,
          filePath: selectedFile ? `/evidence/vault/${selectedFile.name}` : '/evidence/manual/unknown.raw'
        },
        selectedFile
      );

      setRegisteredResult(res);
    } catch (err) {
      console.error('Registration failed:', err);
      alert('Failed to register evidence: ' + err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <FilePlus2 className="w-5 h-5 text-cyan-400" />
          <span>Register Digital Evidence & Compute Cryptographic Fingerprint</span>
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          Forensic intake protocol adhering to ISO/IEC 27037: Ingestion, automated client-side SHA-256 calculation, and ledger recording.
        </p>
      </div>

      {/* Success Notification Banner */}
      {registeredResult && (
        <div className="cyber-card p-6 border-cyan-500/50 bg-navy-900/90 shadow-cyan-glow animate-fade-in">
          <div className="flex items-start gap-4">
            <div className="p-3 rounded-full bg-cyan-500/20 text-cyan-400 border border-cyan-500/40">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between">
                <h3 className="text-base font-bold text-slate-100">
                  Evidence Ingested & Ledger Block #{registeredResult.block.blockIndex} Mined
                </h3>
                <span className="text-xs font-mono text-cyan-400 font-bold">
                  {registeredResult.evidence.evidenceId}
                </span>
              </div>
              <p className="text-xs text-slate-300 mt-1">
                The evidence item was registered and anchored into the blockchain-style custody ledger.
              </p>

              {/* Hash Display */}
              <div className="mt-3 p-3 bg-navy-950 border border-navy-800 rounded-lg">
                <div className="text-[10px] text-slate-400 uppercase tracking-wider font-semibold mb-1">
                  SHA-256 Fingerprint (Calculated via Web Crypto API)
                </div>
                <div className="font-mono text-xs text-cyan-300 break-all select-all">
                  {registeredResult.evidence.sha256Hash}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="mt-4 flex flex-wrap gap-2">
                <button
                  onClick={() => {
                    setSelectedEvidenceId(registeredResult.evidence.evidenceId);
                    setCurrentPage('details');
                  }}
                  className="px-3 py-1.5 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded text-xs transition-colors cursor-pointer"
                >
                  Inspect Evidence Record →
                </button>
                <button
                  onClick={() => setCurrentPage('ledger')}
                  className="px-3 py-1.5 bg-navy-800 hover:bg-navy-700 text-slate-200 border border-navy-700 rounded text-xs transition-colors cursor-pointer"
                >
                  View Ledger Block #{registeredResult.block.blockIndex}
                </button>
                <button
                  onClick={() => {
                    setRegisteredResult(null);
                    setSelectedFile(null);
                    setCalculatedHash('');
                  }}
                  className="px-3 py-1.5 bg-navy-950 hover:bg-navy-800 text-slate-400 rounded text-xs transition-colors cursor-pointer"
                >
                  Register Another Item
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Registration Form */}
      <form onSubmit={handleSubmit} className="cyber-card p-6 space-y-6">
        {/* Step 1: File Selection & Real-Time Hashing */}
        <div>
          <label className="block text-xs font-bold text-slate-200 uppercase tracking-wider mb-2 flex items-center justify-between">
            <span className="flex items-center gap-1.5">
              <HardDrive className="w-4 h-4 text-cyan-400" />
              1. Digital Evidence Artifact / File Selection
            </span>
            <span className="text-[10px] font-mono text-cyan-400 lowercase">
              client-side hashing &bull; zero cloud upload
            </span>
          </label>

          {/* Drag & Drop Box */}
          <div
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
            className={`
              relative border-2 border-dashed rounded-xl p-6 text-center transition-all cursor-pointer
              ${dragActive ? 'border-cyan-400 bg-cyan-500/10' : 'border-navy-750 bg-navy-950 hover:border-cyan-500/40'}
            `}
          >
            <input
              type="file"
              id="evidence-file-input"
              onChange={(e) => handleFileChange(e.target.files[0])}
              className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
            />

            <div className="flex flex-col items-center justify-center space-y-2">
              <div className="h-12 w-12 rounded-full bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
                <UploadCloud className="w-6 h-6" />
              </div>
              <div className="text-xs text-slate-300">
                <span className="font-bold text-cyan-400">Click to browse</span> or drag and drop raw evidence file here
              </div>
              <p className="text-[11px] text-slate-400">
                Supports DD, E01, PCAP, RAW, MEM, IMG, VMDK, JSON, ISO, or forensic archive files
              </p>
            </div>
          </div>

          {/* File Selected Status & Web Crypto SHA-256 Output */}
          {selectedFile && (
            <div className="mt-3 p-3 bg-navy-950 border border-navy-800 rounded-lg">
              <div className="flex items-center justify-between text-xs mb-2">
                <div className="flex items-center gap-2 text-slate-200 font-semibold truncate">
                  <FileCheck2 className="w-4 h-4 text-cyan-400 shrink-0" />
                  <span className="truncate">{selectedFile.name}</span>
                  <span className="text-slate-400 font-mono text-[11px]">({formatBytes(selectedFile.size)})</span>
                </div>
                {computingHash ? (
                  <span className="text-cyan-400 font-mono text-[11px] flex items-center gap-1">
                    <span className="animate-spin text-base">⟳</span> Computing SHA-256...
                  </span>
                ) : (
                  <span className="text-emerald-400 font-mono text-[11px] flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Computed in {hashCalculationTime} ms
                  </span>
                )}
              </div>

              {calculatedHash && (
                <div className="bg-navy-900 p-2.5 rounded border border-navy-800/80">
                  <div className="text-[10px] text-slate-400 font-mono mb-1">
                    Browser Web Crypto API Computed Digest:
                  </div>
                  <div className="font-mono text-xs text-cyan-300 break-all select-all font-medium">
                    {calculatedHash}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Step 2: Metadata Fields */}
        <div className="pt-4 border-t border-navy-800 space-y-4">
          <div className="text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
            <Fingerprint className="w-4 h-4 text-cyan-400" />
            2. Case & Forensics Metadata
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Case ID */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Case Identifier *</label>
              <input
                type="text"
                required
                value={caseId}
                onChange={(e) => setCaseId(e.target.value)}
                placeholder="e.g. CASE-2026-0814"
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 font-mono"
              />
            </div>

            {/* Evidence Type */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Artifact Type *</label>
              <select
                value={evidenceType}
                onChange={(e) => setEvidenceType(e.target.value)}
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
              >
                <option value="Disk Image">Disk Image (Raw DD / E01)</option>
                <option value="Memory Dump">Volatile Memory Dump (RAM)</option>
                <option value="Network Capture (PCAP)">Network Packet Capture (PCAP / PCAPNG)</option>
                <option value="Mobile Extraction">Mobile Physical Extraction (UFED / GrayKey)</option>
                <option value="Digital Storage">Digital Storage Media / USB / SSD</option>
                <option value="Document / Log">System Log / Forensic Report</option>
              </select>
            </div>

            {/* Source Device */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Source Device / Serial Number *</label>
              <input
                type="text"
                required
                value={sourceDevice}
                onChange={(e) => setSourceDevice(e.target.value)}
                placeholder="e.g. Samsung 980 Pro 1TB (S/N: S5GXNF0R...)"
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
              />
            </div>

            {/* Seizing Officer */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Seizing Officer / Collector *</label>
              <input
                type="text"
                required
                value={collector}
                onChange={(e) => setCollector(e.target.value)}
                placeholder="e.g. Agent Sarah Connor (Badge #4092)"
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Forensic Description & Acquisition Methodology</label>
            <textarea
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Acquired write-blocked physical image from suspect workstation under search warrant #2026-W-412."
              className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 resize-none"
            />
          </div>

          {/* Initial Custody Notes */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Initial Chain of Custody Intake Notes</label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Tamper-evident bag sealed with Barcode Tag #SEC-8921."
              className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
            />
          </div>
        </div>

        {/* Submit Button */}
        <div className="pt-4 border-t border-navy-800 flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-xs text-slate-400">
            <Cpu className="w-4 h-4 text-cyan-400" />
            <span>Automatic SHA-256 Ledger Block Creation</span>
          </div>

          <button
            type="submit"
            disabled={isSubmitting || computingHash || (!selectedFile && !calculatedHash)}
            className="px-6 py-2.5 bg-cyan-500 hover:bg-cyan-400 disabled:opacity-50 text-navy-950 font-bold rounded-lg text-xs flex items-center gap-2 transition-all shadow-cyan-glow cursor-pointer"
          >
            {isSubmitting ? (
              <span>Registering Artifact...</span>
            ) : (
              <>
                <span>Commit Evidence & Mine Block</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
