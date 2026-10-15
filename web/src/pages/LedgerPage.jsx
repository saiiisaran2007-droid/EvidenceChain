import React, { useState } from 'react';
import { 
  Blocks, 
  ShieldCheck, 
  ShieldAlert, 
  RotateCcw, 
  AlertTriangle, 
  Link, 
  Check, 
  X, 
  Cpu, 
  Lock, 
  ArrowDown, 
  Radio, 
  Info,
  Bug
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { HashPill, DataSourceBadge } from '../components/Badge';

export default function LedgerPage() {
  const { 
    ledgerBlocks, 
    verifyLedger, 
    simulateTamper, 
    restoreOriginalLedger, 
    isTampered, 
    tamperedIndex 
  } = useEvidence();

  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);
  const [tamperTargetIndex, setTamperTargetIndex] = useState(1);
  const [showTamperModal, setShowTamperModal] = useState(false);

  const handleVerify = async () => {
    setVerifying(true);
    // Slight artificial delay to simulate sequential cryptographic verification
    setTimeout(async () => {
      const result = await verifyLedger();
      setVerificationResult(result);
      setVerifying(false);
    }, 400);
  };

  const handleSimulateTamper = () => {
    simulateTamper(tamperTargetIndex, "CORRUPTED_TAMPERED_HASH_PAYLOAD_9999");
    setShowTamperModal(false);
    setVerificationResult(null);
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Blocks className="w-5 h-5 text-purple-400" />
            <span>Hash-Linked Custody Blockchain Ledger</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Tamper-evident cryptographic ledger where each block anchors the SHA-256 digest of the previous block.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleVerify}
            disabled={verifying}
            className="px-4 py-2 bg-cyan-500 hover:bg-cyan-400 disabled:opacity-50 text-navy-950 font-bold rounded-lg text-xs flex items-center gap-2 transition-all shadow-cyan-glow cursor-pointer"
          >
            <ShieldCheck className="w-4 h-4" />
            <span>{verifying ? 'Recalculating Chain Hashes...' : 'Verify Entire Ledger Chain'}</span>
          </button>

          {isTampered ? (
            <button
              onClick={restoreOriginalLedger}
              className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-semibold rounded-lg text-xs flex items-center gap-1.5 transition-colors cursor-pointer"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Restore Genuine Chain</span>
            </button>
          ) : (
            <button
              onClick={() => setShowTamperModal(true)}
              className="px-3.5 py-2 bg-navy-800 hover:bg-red-500/20 text-slate-300 hover:text-red-400 border border-navy-700 hover:border-red-500/40 rounded-lg text-xs flex items-center gap-1.5 transition-colors cursor-pointer"
              title="Test tamper detection by deliberately altering a block"
            >
              <Bug className="w-4 h-4 text-red-400" />
              <span>Simulate Tamper Test</span>
            </button>
          )}
        </div>
      </div>

      {/* Security Scope Banner */}
      <div className="p-3.5 bg-navy-900 border border-navy-800 rounded-lg text-xs text-slate-300 flex items-start gap-3">
        <Info className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
        <div className="space-y-1">
          <span className="font-semibold text-slate-200">Demonstrative Hash-Linked Architecture:</span>
          <p className="text-[11px] text-slate-400 leading-relaxed">
            This page visualizes the mathematical mechanics of blockchain hash chaining (<code className="text-cyan-400 font-mono">Current_Hash = SHA256(Index + Timestamp + EvidenceID + Action + DataHash + Previous_Hash)</code>).
            Note: This web demonstration calculates hashes client-side in browser memory. A browser-only ledger is not a tamper-proof production backend without decentralized network consensus or hardware-protected storage.
          </p>
        </div>
      </div>

      {/* Verification Result Banner */}
      {verificationResult && (
        <div className={`cyber-card p-5 border transition-all ${
          verificationResult.isValid 
            ? 'bg-emerald-500/10 border-emerald-500/40 text-emerald-300 shadow-emerald-glow'
            : 'bg-red-500/10 border-red-500/40 text-red-300 shadow-red-glow'
        }`}>
          <div className="flex items-start gap-3">
            {verificationResult.isValid ? (
              <ShieldCheck className="w-6 h-6 text-emerald-400 shrink-0 mt-0.5" />
            ) : (
              <ShieldAlert className="w-6 h-6 text-red-400 shrink-0 mt-0.5 animate-bounce" />
            )}
            <div className="space-y-1 flex-1">
              <h3 className="text-sm font-bold flex items-center justify-between">
                <span>
                  {verificationResult.isValid ? 'Cryptographic Chain Validation PASSED' : 'Tamper Detection Triggered: Chain COMPROMISED'}
                </span>
                <span className="font-mono text-xs font-normal">
                  Checked {verificationResult.totalBlocks} Blocks
                </span>
              </h3>
              <p className="text-xs">
                {verificationResult.isValid ? (
                  <span>All sequential SHA-256 block hashes and previous-hash pointers match 100% of recalculated digests. No retroactive tampering detected.</span>
                ) : (
                  <span className="font-semibold text-red-200">{verificationResult.reason}</span>
                )}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Blockchain Blocks Flow */}
      <div className="space-y-4">
        {ledgerBlocks.map((block, index) => {
          const isGenesis = block.blockIndex === 0;
          const isCompromised = isTampered && tamperedIndex === block.blockIndex;

          return (
            <div key={block.blockIndex} className="relative">
              {/* Linking Line between blocks */}
              {index > 0 && (
                <div className="flex items-center justify-center my-1">
                  <div className="flex items-center gap-2 bg-navy-950 px-3 py-1 rounded-full border border-navy-800 text-[10px] font-mono text-slate-400">
                    <Link className="w-3 h-3 text-cyan-400" />
                    <span>points to Previous Block #{block.blockIndex - 1} Hash</span>
                    <ArrowDown className="w-3 h-3 text-cyan-400" />
                  </div>
                </div>
              )}

              {/* Block Card */}
              <div className={`cyber-card p-5 relative transition-all ${
                isCompromised 
                  ? 'border-red-500 bg-red-950/20 shadow-red-glow' 
                  : isGenesis
                    ? 'border-purple-500/40 bg-purple-950/10'
                    : 'hover:border-cyan-500/40'
              }`}>
                {/* Block Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 mb-3 border-b border-navy-800">
                  <div className="flex items-center gap-2.5">
                    <span className={`px-2.5 py-1 rounded text-xs font-mono font-bold ${
                      isGenesis 
                        ? 'bg-purple-500/20 text-purple-300 border border-purple-500/40'
                        : isCompromised
                          ? 'bg-red-500/20 text-red-400 border border-red-500/40'
                          : 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30'
                    }`}>
                      BLOCK #{block.blockIndex} {isGenesis && '(GENESIS)'}
                    </span>
                    <span className="font-mono text-xs text-slate-300">
                      Target: <strong className="text-cyan-300">{block.evidenceId}</strong>
                    </span>
                    <span className="bg-navy-950 px-2 py-0.5 rounded text-[11px] font-semibold text-slate-300 font-mono">
                      {block.action}
                    </span>
                    <DataSourceBadge isDemoItem={block.isDemoItem} />
                  </div>

                  <div className="text-xs text-slate-400 font-mono">
                    Timestamp: <span className="text-slate-300">{block.timestamp}</span>
                  </div>
                </div>

                {/* Hashes breakdown */}
                <div className="space-y-2.5 text-xs font-mono">
                  {/* Previous Block Hash */}
                  <div className="bg-navy-950 p-2.5 rounded border border-navy-800/80">
                    <div className="flex items-center justify-between text-[10px] text-slate-400 uppercase tracking-wider mb-1">
                      <span className="flex items-center gap-1">
                        <Link className="w-3 h-3 text-cyan-400" />
                        Previous Hash Pointer (Parent)
                      </span>
                      {isGenesis && <span className="text-purple-400">Root Anchor</span>}
                    </div>
                    <div className="text-slate-400 break-all select-all text-[11px]">
                      {block.previousHash}
                    </div>
                  </div>

                  {/* Payload Data Hash */}
                  <div className="bg-navy-950 p-2.5 rounded border border-navy-800/80">
                    <div className="text-[10px] text-slate-400 uppercase tracking-wider mb-1">
                      Payload SHA-256 (Artifact / Event Digest)
                    </div>
                    <div className="text-slate-300 break-all select-all text-[11px]">
                      {block.dataHash}
                    </div>
                  </div>

                  {/* Current Block Hash */}
                  <div className={`p-2.5 rounded border ${
                    isCompromised 
                      ? 'bg-red-950/40 border-red-500/50 text-red-300' 
                      : 'bg-navy-900 border-cyan-500/30 text-cyan-300'
                  }`}>
                    <div className="flex items-center justify-between text-[10px] uppercase tracking-wider mb-1">
                      <span className="font-bold flex items-center gap-1">
                        <Lock className="w-3 h-3 text-cyan-400" />
                        Current Block SHA-256 Digest
                      </span>
                      {isCompromised && (
                        <span className="text-red-400 font-bold animate-pulse">TAMPER DETECTED</span>
                      )}
                    </div>
                    <div className="font-semibold break-all select-all text-[11px] md:text-xs">
                      {block.blockHash}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Educational Tamper Modal */}
      {showTamperModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-navy-900 border border-navy-700 rounded-xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center gap-2 text-red-400 font-bold text-base">
              <Bug className="w-5 h-5" />
              <h3>Simulate Ledger Block Tampering</h3>
            </div>

            <p className="text-xs text-slate-300 leading-relaxed">
              This interactive demonstration simulates an attacker modifying historical forensic records in the database. 
              The cryptographic algorithm will immediately detect that the block hash doesn't match and that subsequent blocks' previous-hash links are broken.
            </p>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Select Block to Tamper With:
              </label>
              <select
                value={tamperTargetIndex}
                onChange={(e) => setTamperTargetIndex(Number(e.target.value))}
                className="w-full bg-navy-950 border border-navy-800 rounded px-3 py-2 text-xs text-slate-200 font-mono"
              >
                {ledgerBlocks.slice(1).map(b => (
                  <option key={b.blockIndex} value={b.blockIndex}>
                    Block #{b.blockIndex} ({b.evidenceId} - {b.action})
                  </option>
                ))}
              </select>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setShowTamperModal(false)}
                className="px-3 py-1.5 bg-navy-800 hover:bg-navy-700 text-slate-300 rounded text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleSimulateTamper}
                className="px-4 py-1.5 bg-red-600 hover:bg-red-500 text-white font-bold rounded text-xs flex items-center gap-1.5"
              >
                <span>Inject Tampered Payload</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
