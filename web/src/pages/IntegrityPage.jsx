import React, { useState, useEffect } from 'react';
import { 
  CheckCheck, 
  UploadCloud, 
  ShieldCheck, 
  ShieldAlert, 
  FileCheck2, 
  FileX2, 
  Cpu, 
  Fingerprint, 
  Copy, 
  Check, 
  RefreshCw,
  Sparkles,
  ArrowRight
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { calculateFileSha256, calculateStringSha256, formatBytes, copyToClipboard } from '../utils/crypto';
import { StatusBadge, TypeBadge } from '../components/Badge';

export default function IntegrityPage({ initialEvidenceId, setCurrentPage, setSelectedEvidenceId }) {
  const { evidenceList } = useEvidence();

  const [selectedEvidenceId, setLocalEvidenceId] = useState(
    initialEvidenceId || (evidenceList[0]?.evidenceId || '')
  );
  const [selectedFile, setSelectedFile] = useState(null);
  const [hashing, setHashing] = useState(false);
  const [computedFileHash, setComputedFileHash] = useState('');
  const [verificationResult, setVerificationResult] = useState(null);
  const [dragActive, setDragActive] = useState(false);

  // Current target evidence item
  const targetEvidence = evidenceList.find(e => e.evidenceId === selectedEvidenceId) || evidenceList[0];

  useEffect(() => {
    if (initialEvidenceId) {
      setLocalEvidenceId(initialEvidenceId);
    }
  }, [initialEvidenceId]);

  // When a file is chosen, compute hash using browser Web Crypto API
  const handleFile = async (file) => {
    if (!file) return;
    setSelectedFile(file);
    setHashing(true);
    setVerificationResult(null);

    const startTime = performance.now();
    try {
      const hash = await calculateFileSha256(file);
      const elapsed = (performance.now() - startTime).toFixed(1);
      setComputedFileHash(hash);

      // Compare with recorded fingerprint
      const isMatch = hash.toLowerCase() === targetEvidence.sha256Hash.toLowerCase();

      setVerificationResult({
        isMatch,
        computedHash: hash,
        expectedHash: targetEvidence.sha256Hash,
        fileSize: file.size,
        fileName: file.name,
        elapsedMs: elapsed,
        verifiedAt: new Date().toLocaleTimeString()
      });
    } catch (err) {
      console.error('File hashing error:', err);
    } finally {
      setHashing(false);
    }
  };

  /**
   * Helper: Generate a matching demo blob for convenient testing
   */
  const handleTestMatchingFile = async () => {
    if (!targetEvidence) return;
    // We create a sample test file that matches or simulates the item
    // For demo purposes, we create a file whose hash can be checked
    const dummyContent = `EvidenceChain Forensic Artifact: ${targetEvidence.evidenceId}\nRecorded: ${targetEvidence.recordedAt}\nDevice: ${targetEvidence.sourceDevice}`;
    const file = new File([dummyContent], `${targetEvidence.evidenceId}_test_sample.raw`, { type: 'application/octet-stream' });
    
    // Calculate hash of this file
    const hash = await calculateFileSha256(file);
    setSelectedFile(file);
    setComputedFileHash(hash);

    const isMatch = hash.toLowerCase() === targetEvidence.sha256Hash.toLowerCase();
    setVerificationResult({
      isMatch,
      computedHash: hash,
      expectedHash: targetEvidence.sha256Hash,
      fileSize: file.size,
      fileName: file.name,
      elapsedMs: '3.2',
      verifiedAt: new Date().toLocaleTimeString(),
      isSimulatedFile: true
    });
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <CheckCheck className="w-5 h-5 text-cyan-400" />
          <span>Cryptographic Integrity Verification Laboratory</span>
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          Perform bit-level validation of forensic images and media against recorded SHA-256 baseline hashes.
        </p>
      </div>

      {/* Target Evidence Selection */}
      <div className="cyber-card p-5 space-y-3">
        <label className="block text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center justify-between">
          <span>1. Select Baseline Evidence Record</span>
          <span className="text-[11px] font-mono text-cyan-400">Baseline Database Record</span>
        </label>

        <select
          value={targetEvidence?.evidenceId}
          onChange={(e) => {
            setLocalEvidenceId(e.target.value);
            setVerificationResult(null);
            setSelectedFile(null);
            setComputedFileHash('');
          }}
          className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 font-mono cursor-pointer"
        >
          {evidenceList.map(e => (
            <option key={e.evidenceId} value={e.evidenceId}>
              {e.evidenceId} &bull; Case: {e.caseId} &bull; {e.evidenceType} &bull; {e.sourceDevice.slice(0, 35)}
            </option>
          ))}
        </select>

        {targetEvidence && (
          <div className="p-3 bg-navy-950/70 border border-navy-800 rounded-lg text-xs space-y-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <span className="font-bold text-slate-200">{targetEvidence.sourceDevice}</span>
                <TypeBadge type={targetEvidence.evidenceType} />
              </div>
              <span className="text-slate-400 font-mono text-[11px]">
                Recorded Size: {formatBytes(targetEvidence.fileSize)}
              </span>
            </div>

            <div className="bg-navy-900 p-2 rounded border border-navy-800 font-mono text-xs text-cyan-300 break-all select-all">
              <span className="text-[10px] text-slate-400 block uppercase mb-0.5">Recorded Benchmark Hash (SHA-256):</span>
              {targetEvidence.sha256Hash}
            </div>
          </div>
        )}
      </div>

      {/* File Ingestion for Re-Hashing */}
      <div className="cyber-card p-5 space-y-4">
        <div className="flex items-center justify-between">
          <label className="block text-xs font-bold text-slate-200 uppercase tracking-wider flex items-center gap-2">
            <Fingerprint className="w-4 h-4 text-cyan-400" />
            <span>2. Upload or Drop Candidate Physical File to Verify</span>
          </label>

          <button
            onClick={handleTestMatchingFile}
            className="text-[11px] text-cyan-400 hover:text-cyan-300 flex items-center gap-1 font-mono underline cursor-pointer"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Quick Test File</span>
          </button>
        </div>

        {/* Drag Drop Area */}
        <div
          onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
          onDragLeave={() => setDragActive(false)}
          onDrop={(e) => {
            e.preventDefault();
            setDragActive(false);
            if (e.dataTransfer.files && e.dataTransfer.files[0]) {
              handleFile(e.dataTransfer.files[0]);
            }
          }}
          className={`relative border-2 border-dashed rounded-xl p-6 text-center transition-all cursor-pointer ${
            dragActive ? 'border-cyan-400 bg-cyan-500/10' : 'border-navy-750 bg-navy-950 hover:border-cyan-500/40'
          }`}
        >
          <input
            type="file"
            onChange={(e) => handleFile(e.target.files[0])}
            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
          />

          <div className="flex flex-col items-center justify-center space-y-2">
            <div className="h-10 w-10 rounded-full bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
              <UploadCloud className="w-5 h-5" />
            </div>
            <div className="text-xs text-slate-300">
              <span className="font-bold text-cyan-400">Select file from local storage</span> to compute real-time SHA-256
            </div>
            <p className="text-[11px] text-slate-400">
              Computed natively via window.crypto.subtle.digest (zero data uploaded)
            </p>
          </div>
        </div>

        {hashing && (
          <div className="p-3 bg-navy-950 border border-cyan-500/30 rounded-lg text-xs text-cyan-400 flex items-center justify-center gap-2 font-mono">
            <RefreshCw className="w-4 h-4 animate-spin" />
            <span>Reading file byte stream & computing Web Crypto SHA-256...</span>
          </div>
        )}
      </div>

      {/* Verification Result Card */}
      {verificationResult && (
        <div className={`cyber-card p-6 border transition-all ${
          verificationResult.isMatch
            ? 'border-emerald-500/50 bg-emerald-950/20 shadow-emerald-glow'
            : 'border-red-500/50 bg-red-950/20 shadow-red-glow'
        }`}>
          <div className="flex items-start gap-4">
            <div className={`p-3 rounded-xl border ${
              verificationResult.isMatch
                ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                : 'bg-red-500/20 text-red-400 border-red-500/40'
            }`}>
              {verificationResult.isMatch ? (
                <ShieldCheck className="w-8 h-8" />
              ) : (
                <ShieldAlert className="w-8 h-8 animate-bounce" />
              )}
            </div>

            <div className="flex-1 space-y-3">
              <div>
                <div className="flex items-center justify-between">
                  <h3 className={`text-base font-black tracking-wide ${
                    verificationResult.isMatch ? 'text-emerald-300' : 'text-red-300'
                  }`}>
                    {verificationResult.isMatch 
                      ? 'INTEGRITY VERIFIED: BIT-EXACT MATCH (100% IDENTICAL)' 
                      : 'INTEGRITY VIOLATION DETECTED: CRYPTOGRAPHIC MISMATCH'}
                  </h3>
                  <span className="text-[11px] font-mono text-slate-400">
                    Checked in {verificationResult.elapsedMs} ms
                  </span>
                </div>
                <p className="text-xs text-slate-300 mt-1">
                  {verificationResult.isMatch ? (
                    <span>The candidate file matches the registered digital evidence fingerprint with bit-for-bit authenticity. No tampering, degradation, or unauthorized alterations detected.</span>
                  ) : (
                    <span>Warning: The candidate file does NOT produce the expected SHA-256 hash. The physical file may have been modified, corrupted, or does not correspond to this registered record.</span>
                  )}
                </p>
              </div>

              {/* Side-by-Side Hash Comparison */}
              <div className="space-y-2 text-xs font-mono">
                {/* Expected */}
                <div className="p-2.5 rounded bg-navy-950 border border-navy-800">
                  <div className="text-[10px] text-slate-400 uppercase font-bold mb-0.5">
                    Expected Intake Baseline Hash:
                  </div>
                  <div className="text-slate-300 break-all select-all text-[11px]">
                    {verificationResult.expectedHash}
                  </div>
                </div>

                {/* Computed */}
                <div className={`p-2.5 rounded border ${
                  verificationResult.isMatch 
                    ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-300' 
                    : 'bg-red-950/40 border-red-500/40 text-red-300'
                }`}>
                  <div className="text-[10px] uppercase font-bold mb-0.5 flex items-center justify-between">
                    <span>Candidate File Computed SHA-256 Digest:</span>
                    <span>{verificationResult.isMatch ? '✓ EXACT MATCH' : '✗ HASH MISMATCH'}</span>
                  </div>
                  <div className="break-all select-all text-[11px] font-bold">
                    {verificationResult.computedHash}
                  </div>
                </div>
              </div>

              {/* Verified file metadata */}
              <div className="pt-2 border-t border-navy-800/80 flex flex-wrap items-center justify-between text-[11px] text-slate-400 font-mono">
                <span>File: {verificationResult.fileName} ({formatBytes(verificationResult.fileSize)})</span>
                <span>Verification Time: {verificationResult.verifiedAt}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
