import React, { useState } from 'react';
import { Copy, Check, ShieldCheck, ShieldAlert, Clock, Database } from 'lucide-react';
import { copyToClipboard, truncateHash } from '../utils/crypto';

export function StatusBadge({ status }) {
  if (status === 'Verified' || status === 'VALID' || status === 'OK') {
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
        <ShieldCheck className="w-3.5 h-3.5" />
        {status}
      </span>
    );
  }
  if (status === 'Compromised' || status === 'COMPROMISED' || status === 'FAILED' || status === 'MISMATCH') {
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-500/10 text-red-400 border border-red-500/30 animate-pulse">
        <ShieldAlert className="w-3.5 h-3.5" />
        {status}
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/30">
      <Clock className="w-3.5 h-3.5" />
      {status || 'Recorded'}
    </span>
  );
}

export function TypeBadge({ type }) {
  const typeMap = {
    'Disk Image': 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30',
    'Memory Dump': 'bg-purple-500/10 text-purple-400 border-purple-500/30',
    'Network Capture (PCAP)': 'bg-blue-500/10 text-blue-400 border-blue-500/30',
    'Mobile Extraction': 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    'Document / Log': 'bg-slate-500/10 text-slate-300 border-slate-500/30',
  };

  const styleClass = typeMap[type] || 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30';

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${styleClass}`}>
      {type}
    </span>
  );
}

export function HashPill({ hash, truncate = true, start = 8, end = 8 }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async (e) => {
    e.stopPropagation();
    if (!hash) return;
    const ok = await copyToClipboard(hash);
    if (ok) {
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    }
  };

  if (!hash) return <span className="text-slate-500 text-xs font-mono">None</span>;

  return (
    <button
      onClick={handleCopy}
      title="Click to copy full SHA-256 hash"
      className="inline-flex items-center gap-1.5 px-2 py-1 rounded bg-navy-950 hover:bg-navy-800 text-slate-300 hover:text-cyan-400 font-mono text-xs border border-navy-800 hover:border-cyan-500/40 transition-colors group cursor-pointer"
    >
      <span>{truncate ? truncateHash(hash, start, end) : hash}</span>
      {copied ? (
        <Check className="w-3.5 h-3.5 text-emerald-400" />
      ) : (
        <Copy className="w-3.5 h-3.5 text-slate-500 group-hover:text-cyan-400" />
      )}
    </button>
  );
}

export function DataSourceBadge({ isDemoItem }) {
  if (isDemoItem) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-mono bg-navy-800/80 text-slate-400 border border-navy-700" title="Pre-seeded demonstration record">
        <Database className="w-3 h-3 text-slate-500" />
        DEMO SEED
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-mono bg-cyan-500/10 text-cyan-400 border border-cyan-500/30" title="Created during your browser session">
      <span className="h-1.5 w-1.5 rounded-full bg-cyan-400 animate-pulse"></span>
      SESSION CREATED
    </span>
  );
}
