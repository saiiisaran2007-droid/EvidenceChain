import React, { useState } from 'react';
import { 
  FileSearch, 
  Copy, 
  Check, 
  CheckCheck, 
  ArrowRightLeft, 
  Calendar, 
  HardDrive, 
  User, 
  ShieldCheck, 
  Blocks, 
  History, 
  Download,
  ExternalLink
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { StatusBadge, TypeBadge, DataSourceBadge } from '../components/Badge';
import { formatBytes, copyToClipboard } from '../utils/crypto';

export default function DetailsPage({ evidenceId, setCurrentPage, setSelectedEvidenceId }) {
  const { evidenceList, custodyEvents, ledgerBlocks } = useEvidence();
  const [copiedHash, setCopiedHash] = useState(false);

  // If evidenceId not provided or not found, default to first item
  const selectedItem = evidenceList.find(item => item.evidenceId === evidenceId) || evidenceList[0];

  if (!selectedItem) {
    return (
      <div className="cyber-card p-12 text-center text-slate-400">
        <FileSearch className="w-10 h-10 mx-auto mb-3 text-cyan-400" />
        <h2 className="text-lg font-bold text-slate-200">No Evidence Found</h2>
        <p className="text-xs text-slate-500 mt-1">Register new evidence or choose an item from the catalog.</p>
        <button
          onClick={() => setCurrentPage('catalog')}
          className="mt-4 px-4 py-2 bg-cyan-500 text-navy-950 font-bold rounded-lg text-xs"
        >
          View Evidence Catalog
        </button>
      </div>
    );
  }

  // Filter custody history and ledger blocks for this specific evidence
  const itemCustodyEvents = custodyEvents.filter(e => e.evidenceId === selectedItem.evidenceId);
  const itemLedgerBlocks = ledgerBlocks.filter(b => b.evidenceId === selectedItem.evidenceId);

  const handleCopyHash = async () => {
    const ok = await copyToClipboard(selectedItem.sha256Hash);
    if (ok) {
      setCopiedHash(true);
      setTimeout(() => setCopiedHash(false), 2000);
    }
  };

  const handleExportJson = () => {
    const exportData = {
      evidence: selectedItem,
      custodyHistory: itemCustodyEvents,
      ledgerBlocks: itemLedgerBlocks,
      exportedAt: new Date().toISOString(),
      standard: "ISO/IEC 27037 Digital Evidence Metadata"
    };
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Evidence_${selectedItem.evidenceId}_metadata.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-6">
      {/* Top Bar with Evidence Selector and Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-lg bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
            <FileSearch className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-cyan-400 font-bold text-lg">{selectedItem.evidenceId}</span>
              <StatusBadge status={selectedItem.status} />
              <DataSourceBadge isDemoItem={selectedItem.isDemoItem} />
            </div>
            <div className="text-xs text-slate-400 font-mono">
              Case Ref: <strong className="text-slate-200">{selectedItem.caseId}</strong>
            </div>
          </div>
        </div>

        {/* Quick Item Switcher Dropdown */}
        <div className="flex items-center gap-2">
          <select
            value={selectedItem.evidenceId}
            onChange={(e) => setSelectedEvidenceId(e.target.value)}
            className="bg-navy-950 border border-navy-800 rounded-lg px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 font-mono cursor-pointer"
          >
            {evidenceList.map(item => (
              <option key={item.evidenceId} value={item.evidenceId}>
                {item.evidenceId} - {item.sourceDevice.slice(0, 25)}...
              </option>
            ))}
          </select>

          <button
            onClick={handleExportJson}
            className="px-3 py-1.5 bg-navy-800 hover:bg-navy-700 text-slate-200 rounded border border-navy-700 text-xs font-medium flex items-center gap-1.5 transition-colors cursor-pointer"
            title="Export full cryptographic manifest to JSON"
          >
            <Download className="w-3.5 h-3.5 text-cyan-400" />
            <span className="hidden sm:inline">Export JSON</span>
          </button>
        </div>
      </div>

      {/* Cryptographic SHA-256 Card */}
      <div className="cyber-card p-5 border-cyan-500/30 bg-navy-950/70">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2 text-cyan-400 text-xs font-bold uppercase tracking-wider">
            <ShieldCheck className="w-4 h-4" />
            <span>Cryptographic SHA-256 Seal</span>
          </div>
          <button
            onClick={handleCopyHash}
            className="flex items-center gap-1.5 text-xs font-mono text-cyan-400 hover:text-cyan-300 bg-navy-900 px-2.5 py-1 rounded border border-navy-800 hover:border-cyan-500/40 transition-colors cursor-pointer"
          >
            {copiedHash ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
            <span>{copiedHash ? 'Hash Copied!' : 'Copy SHA-256'}</span>
          </button>
        </div>

        <div className="p-3 bg-navy-900 border border-navy-800 rounded-lg font-mono text-xs md:text-sm text-cyan-300 break-all select-all font-semibold tracking-wide">
          {selectedItem.sha256Hash}
        </div>

        <div className="mt-3 flex flex-wrap items-center justify-between text-[11px] text-slate-400 font-mono pt-1">
          <span>Algorithm: FIPS 180-4 SHA-256 (256-bit digest)</span>
          <button
            onClick={() => {
              setSelectedEvidenceId(selectedItem.evidenceId);
              setCurrentPage('verify');
            }}
            className="text-cyan-400 hover:underline flex items-center gap-1 font-sans font-semibold cursor-pointer"
          >
            <CheckCheck className="w-3.5 h-3.5" />
            <span>Re-verify physical file against this hash →</span>
          </button>
        </div>
      </div>

      {/* Metadata Specification Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Artifact Specifications */}
        <div className="cyber-card p-5 space-y-4">
          <div className="flex items-center gap-2 pb-2 border-b border-navy-800">
            <HardDrive className="w-4 h-4 text-cyan-400" />
            <h3 className="font-bold text-slate-100 text-sm">Hardware & Acquisition Specifications</h3>
          </div>

          <div className="grid grid-cols-2 gap-3 text-xs">
            <div>
              <span className="text-slate-500 block text-[11px]">Artifact Type</span>
              <span className="font-medium text-slate-200 mt-0.5 inline-block">
                <TypeBadge type={selectedItem.evidenceType} />
              </span>
            </div>

            <div>
              <span className="text-slate-500 block text-[11px]">File Size</span>
              <span className="font-mono text-slate-200 mt-0.5 block">
                {formatBytes(selectedItem.fileSize)} ({selectedItem.fileSize?.toLocaleString()} bytes)
              </span>
            </div>

            <div className="col-span-2">
              <span className="text-slate-500 block text-[11px]">Source Device / Hardware Model</span>
              <span className="font-semibold text-slate-200 mt-0.5 block">{selectedItem.sourceDevice}</span>
            </div>

            <div className="col-span-2">
              <span className="text-slate-500 block text-[11px]">Forensic File Path</span>
              <span className="font-mono text-slate-300 bg-navy-950 p-1.5 rounded border border-navy-800 text-[11px] block break-all">
                {selectedItem.filePath}
              </span>
            </div>

            <div className="col-span-2">
              <span className="text-slate-500 block text-[11px]">Forensic Description</span>
              <p className="text-slate-300 mt-0.5 text-xs leading-relaxed">
                {selectedItem.description}
              </p>
            </div>
          </div>
        </div>

        {/* Custodial Context */}
        <div className="cyber-card p-5 space-y-4">
          <div className="flex items-center gap-2 pb-2 border-b border-navy-800">
            <User className="w-4 h-4 text-blue-400" />
            <h3 className="font-bold text-slate-100 text-sm">Custody & Ingestion Context</h3>
          </div>

          <div className="space-y-3 text-xs">
            <div>
              <span className="text-slate-500 block text-[11px]">Primary Seizing Officer / Collector</span>
              <span className="font-semibold text-slate-200 mt-0.5 block">{selectedItem.collector}</span>
            </div>

            <div>
              <span className="text-slate-500 block text-[11px]">Ingestion Timestamp</span>
              <span className="font-mono text-slate-300 mt-0.5 block flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-cyan-400" />
                {selectedItem.recordedAt}
              </span>
            </div>

            <div>
              <span className="text-slate-500 block text-[11px]">Current Custody Location / Vault</span>
              <span className="font-semibold text-cyan-300 mt-0.5 block">
                {selectedItem.custodyHolder || 'Central Forensic Locker'}
              </span>
            </div>

            <div className="pt-2">
              <button
                onClick={() => {
                  setSelectedEvidenceId(selectedItem.evidenceId);
                  setCurrentPage('custody');
                }}
                className="w-full py-2 bg-navy-800 hover:bg-navy-700 text-cyan-400 rounded-lg border border-navy-700 text-xs font-semibold flex items-center justify-center gap-2 transition-colors cursor-pointer"
              >
                <ArrowRightLeft className="w-4 h-4" />
                <span>Transfer Custody of this Artifact</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Associated Custody Timeline for this item */}
      <div className="cyber-card p-5">
        <div className="flex items-center justify-between mb-4 pb-3 border-b border-navy-800">
          <div className="flex items-center gap-2">
            <History className="w-4 h-4 text-cyan-400" />
            <h3 className="font-bold text-slate-100 text-sm">Chain of Custody Timeline ({itemCustodyEvents.length} Events)</h3>
          </div>
          <button
            onClick={() => setCurrentPage('custody')}
            className="text-xs text-cyan-400 hover:underline"
          >
            All Events →
          </button>
        </div>

        <div className="relative pl-6 space-y-6 before:content-[''] before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-navy-800">
          {itemCustodyEvents.map((evt, idx) => (
            <div key={evt.eventId || idx} className="relative">
              <div className="absolute -left-6 top-1 h-3.5 w-3.5 rounded-full bg-navy-950 border-2 border-cyan-400 shadow-cyan-glow"></div>
              
              <div className="p-3 bg-navy-950/70 border border-navy-800 rounded-lg text-xs space-y-1">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-200">{evt.action}</span>
                  <span className="font-mono text-[10px] text-slate-500">{evt.timestamp}</span>
                </div>
                <div className="text-slate-400 flex items-center gap-2">
                  <span>From: <strong className="text-slate-300">{evt.actorFrom}</strong></span>
                  <span>&rarr;</span>
                  <span>To: <strong className="text-cyan-400">{evt.actorTo}</strong></span>
                </div>
                {evt.notes && (
                  <p className="text-[11px] text-slate-400 italic pt-1">{evt.notes}</p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
