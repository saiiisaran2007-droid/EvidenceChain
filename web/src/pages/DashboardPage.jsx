import React from 'react';
import { 
  FolderArchive, 
  History, 
  Blocks, 
  ShieldCheck, 
  ShieldAlert, 
  FilePlus2, 
  CheckCheck, 
  ArrowUpRight, 
  HardDrive, 
  Activity, 
  Cpu, 
  Database,
  Lock,
  ExternalLink
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { StatusBadge, TypeBadge, HashPill, DataSourceBadge } from '../components/Badge';
import { formatBytes } from '../utils/crypto';

export default function DashboardPage({ setCurrentPage, setSelectedEvidenceId }) {
  const { evidenceList, custodyEvents, ledgerBlocks, isTampered, currentUser } = useEvidence();

  const totalBytes = evidenceList.reduce((acc, item) => acc + (item.fileSize || 0), 0);
  const recentEvidence = evidenceList.slice(0, 4);
  const recentEvents = [...custodyEvents].reverse().slice(0, 5);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-xl p-5 shadow-lg relative overflow-hidden">
        <div className="absolute right-0 top-0 h-full w-1/3 bg-cyan-500/5 blur-2xl pointer-events-none"></div>
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 relative z-10">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-cyan-400 mb-1">
              <span className="h-2 w-2 rounded-full bg-cyan-400 animate-ping"></span>
              SECURE FORENSIC REPOSITORY ACTIVE
            </div>
            <h1 className="text-xl md:text-2xl font-black text-slate-100">
              Security Operations Dashboard
            </h1>
            <p className="text-xs text-slate-400 mt-1 max-w-2xl">
              Logged in as <span className="text-cyan-300 font-semibold">{currentUser?.name || 'Authorized Examiner'}</span> &bull; 
              Tamper-evident chain of custody, cryptographic SHA-256 validation, and blockchain audit ledger.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setCurrentPage('register')}
              className="px-3.5 py-2 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded-lg text-xs flex items-center gap-1.5 transition-all shadow-cyan-glow cursor-pointer"
            >
              <FilePlus2 className="w-4 h-4" />
              <span>Register Evidence</span>
            </button>
            <button
              onClick={() => setCurrentPage('verify')}
              className="px-3.5 py-2 bg-navy-800 hover:bg-navy-750 text-slate-200 border border-navy-700 hover:border-cyan-500/40 rounded-lg text-xs flex items-center gap-1.5 transition-all cursor-pointer"
            >
              <CheckCheck className="w-4 h-4 text-cyan-400" />
              <span>Verify Integrity</span>
            </button>
          </div>
        </div>
      </div>

      {/* KPI Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Metric 1 */}
        <div className="cyber-card p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Evidence</span>
            <FolderArchive className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100">{evidenceList.length}</div>
          <div className="text-[11px] text-slate-400 mt-1 flex items-center justify-between font-mono">
            <span>Volume: {formatBytes(totalBytes)}</span>
            <span className="text-cyan-400">Indexed</span>
          </div>
        </div>

        {/* Metric 2 */}
        <div className="cyber-card p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Custody Events</span>
            <History className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100">{custodyEvents.length}</div>
          <div className="text-[11px] text-slate-400 mt-1 flex items-center justify-between font-mono">
            <span>Handoffs Recorded</span>
            <span className="text-blue-400">100% Signed</span>
          </div>
        </div>

        {/* Metric 3 */}
        <div className="cyber-card p-4">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Ledger Height</span>
            <Blocks className="w-4 h-4 text-purple-400" />
          </div>
          <div className="text-2xl font-bold text-slate-100 font-mono">#{ledgerBlocks.length}</div>
          <div className="text-[11px] text-slate-400 mt-1 flex items-center justify-between font-mono">
            <span>Genesis to Tip</span>
            <span className="text-purple-400">Linked</span>
          </div>
        </div>

        {/* Metric 4 */}
        <div className={`p-4 rounded-lg border transition-all ${
          isTampered
            ? 'bg-red-500/10 border-red-500/40 text-red-200 shadow-red-glow'
            : 'bg-emerald-500/10 border-emerald-500/40 text-emerald-200 shadow-emerald-glow'
        }`}>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Chain Security</span>
            {isTampered ? (
              <ShieldAlert className="w-4 h-4 text-red-400 animate-bounce" />
            ) : (
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
            )}
          </div>
          <div className="text-2xl font-bold">
            {isTampered ? 'COMPROMISED' : '100% INTACT'}
          </div>
          <div className="text-[11px] mt-1 flex items-center justify-between font-mono">
            <span>{isTampered ? 'Tamper Detected' : 'All Hashes Valid'}</span>
            <button
              onClick={() => setCurrentPage('ledger')}
              className="underline text-[10px] font-semibold cursor-pointer"
            >
              Audit Chain →
            </button>
          </div>
        </div>
      </div>

      {/* Main Grid: Recent Evidence & Custody Timeline */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Recent Registered Evidence (2 cols) */}
        <div className="lg:col-span-2 cyber-card p-5">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-navy-800">
            <div className="flex items-center gap-2">
              <HardDrive className="w-4 h-4 text-cyan-400" />
              <h3 className="font-bold text-slate-100 text-sm">Recent Registered Evidence</h3>
            </div>
            <button
              onClick={() => setCurrentPage('catalog')}
              className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1 font-medium cursor-pointer"
            >
              <span>View Full Catalog</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[11px] text-slate-400 uppercase bg-navy-950/70 border-b border-navy-800 font-mono">
                <tr>
                  <th className="py-2.5 px-3">Evidence ID</th>
                  <th className="py-2.5 px-3">Case ID</th>
                  <th className="py-2.5 px-3">Type</th>
                  <th className="py-2.5 px-3">SHA-256 Fingerprint</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-800/60 text-slate-300">
                {recentEvidence.map((item) => (
                  <tr key={item.evidenceId} className="hover:bg-navy-800/40 transition-colors">
                    <td className="py-3 px-3 font-mono font-bold text-cyan-400">
                      {item.evidenceId}
                    </td>
                    <td className="py-3 px-3 font-mono text-slate-300">
                      {item.caseId}
                    </td>
                    <td className="py-3 px-3">
                      <TypeBadge type={item.evidenceType} />
                    </td>
                    <td className="py-3 px-3">
                      <HashPill hash={item.sha256Hash} />
                    </td>
                    <td className="py-3 px-3">
                      <StatusBadge status={item.status} />
                    </td>
                    <td className="py-3 px-3 text-right">
                      <button
                        onClick={() => {
                          setSelectedEvidenceId(item.evidenceId);
                          setCurrentPage('details');
                        }}
                        className="text-cyan-400 hover:text-cyan-300 p-1 hover:bg-navy-700 rounded transition-colors"
                        title="Inspect Evidence Details"
                      >
                        <ExternalLink className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right Column: Custody Feed & System Integrity Card */}
        <div className="space-y-6">
          {/* Custody Activity Feed */}
          <div className="cyber-card p-5">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-navy-800">
              <div className="flex items-center gap-2">
                <Activity className="w-4 h-4 text-blue-400" />
                <h3 className="font-bold text-slate-100 text-sm">Chain of Custody Feed</h3>
              </div>
              <button
                onClick={() => setCurrentPage('custody')}
                className="text-xs text-blue-400 hover:text-blue-300 font-medium"
              >
                All Events →
              </button>
            </div>

            <div className="space-y-3">
              {recentEvents.map((evt) => (
                <div key={evt.eventId} className="p-2.5 rounded bg-navy-950/80 border border-navy-800/80 text-xs">
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-mono text-cyan-400 font-semibold">{evt.evidenceId}</span>
                    <span className="text-[10px] text-slate-500 font-mono">{evt.timestamp}</span>
                  </div>
                  <div className="font-semibold text-slate-200 text-[11px] mb-1">
                    {evt.action}
                  </div>
                  <div className="text-[11px] text-slate-400 flex items-center justify-between">
                    <span>From: {evt.actorFrom}</span>
                    <span>To: {evt.actorTo || 'Archive'}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Forensic System Standards Info */}
          <div className="p-4 rounded-lg bg-navy-900 border border-navy-800 text-xs space-y-2">
            <div className="flex items-center gap-2 text-cyan-400 font-bold">
              <Cpu className="w-4 h-4" />
              <span>NIST SP 800-86 Forensic Verification</span>
            </div>
            <p className="text-slate-400 text-[11px] leading-relaxed">
              Every registered evidence item receives an immutable SHA-256 fingerprint upon intake.
              Subsequent transfers and analyses are sequentially linked into the hash chain to prevent undetected retroactive modification.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
