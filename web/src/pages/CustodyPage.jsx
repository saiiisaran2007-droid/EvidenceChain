import React, { useState } from 'react';
import { 
  History, 
  ArrowRightLeft, 
  Plus, 
  Calendar, 
  User, 
  CheckCircle2, 
  Filter, 
  ShieldCheck, 
  Blocks,
  ArrowRight
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { DataSourceBadge } from '../components/Badge';

export default function CustodyPage({ setCurrentPage, setSelectedEvidenceId }) {
  const { custodyEvents, evidenceList, transferCustody, currentUser } = useEvidence();

  const [filterEvidenceId, setFilterEvidenceId] = useState('ALL');
  const [showTransferForm, setShowTransferForm] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  // Form State
  const [selectedEvidence, setSelectedEvidence] = useState(evidenceList[0]?.evidenceId || '');
  const [action, setAction] = useState('TRANSFERRED_FOR_ANALYSIS');
  const [actorFrom, setActorFrom] = useState(
    currentUser ? `${currentUser.name} (Badge #${currentUser.badgeNumber})` : 'Special Agent Sarah Connor'
  );
  const [actorTo, setActorTo] = useState('Forensic Lab Vault A-12');
  const [notes, setNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const filteredEvents = custodyEvents.filter(e => {
    return filterEvidenceId === 'ALL' || e.evidenceId === filterEvidenceId;
  });

  const handleTransferSubmit = async (e) => {
    e.preventDefault();
    if (!selectedEvidence || !actorFrom || !actorTo) return;

    setIsSubmitting(true);
    try {
      const res = await transferCustody(
        selectedEvidence,
        action,
        actorFrom.trim(),
        actorTo.trim(),
        notes.trim()
      );

      setSuccessMessage(`Custody transfer recorded! Blockchain Block #${res.block.blockIndex} mined.`);
      setShowTransferForm(false);
      setNotes('');
      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (err) {
      console.error('Custody transfer error:', err);
      alert('Error recording custody event: ' + err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <History className="w-5 h-5 text-cyan-400" />
            <span>Chain of Custody Event History</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Tamper-evident legal audit log tracking every transfer, examination, and change of custody.
          </p>
        </div>

        <button
          onClick={() => setShowTransferForm(!showTransferForm)}
          className="px-4 py-2 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded-lg text-xs flex items-center gap-2 transition-all shadow-cyan-glow self-start sm:self-auto cursor-pointer"
        >
          <ArrowRightLeft className="w-4 h-4" />
          <span>{showTransferForm ? 'Cancel Handoff' : 'Log Custody Transfer'}</span>
        </button>
      </div>

      {/* Success Notification */}
      {successMessage && (
        <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-lg text-xs text-emerald-300 flex items-center justify-between animate-fade-in">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span>{successMessage}</span>
          </div>
          <button
            onClick={() => setCurrentPage('ledger')}
            className="text-cyan-400 hover:underline font-semibold"
          >
            Inspect Block in Ledger →
          </button>
        </div>
      )}

      {/* Transfer Form (Collapsible) */}
      {showTransferForm && (
        <form onSubmit={handleTransferSubmit} className="cyber-card p-5 border-cyan-500/40 bg-navy-900/90 shadow-cyan-glow space-y-4">
          <div className="flex items-center gap-2 pb-2 border-b border-navy-800 text-cyan-400 text-xs font-bold uppercase tracking-wider">
            <ArrowRightLeft className="w-4 h-4" />
            <span>Record Authorized Custody Transfer / Forensic Event</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Target Evidence */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Target Evidence Item *</label>
              <select
                value={selectedEvidence}
                onChange={(e) => setSelectedEvidence(e.target.value)}
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 font-mono"
              >
                {evidenceList.map(e => (
                  <option key={e.evidenceId} value={e.evidenceId}>
                    {e.evidenceId} - {e.sourceDevice.slice(0, 30)}...
                  </option>
                ))}
              </select>
            </div>

            {/* Action Type */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Custodial Action / Event *</label>
              <select
                value={action}
                onChange={(e) => setAction(e.target.value)}
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
              >
                <option value="TRANSFERRED_FOR_ANALYSIS">Transferred for Forensic Analysis</option>
                <option value="CHECKED_INTO_VAULT">Checked Into Secure Evidence Vault</option>
                <option value="CHECKED_OUT_FOR_COURT">Checked Out for Judicial Court Presentation</option>
                <option value="EXAMINED_IN_LAB">Examined in Isolated Sandbox Lab</option>
                <option value="SEALED_STORAGE">Physical Evidence Resealed with Barcode</option>
              </select>
            </div>

            {/* Actor From */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Releasing Custodian (Actor From) *</label>
              <input
                type="text"
                required
                value={actorFrom}
                onChange={(e) => setActorFrom(e.target.value)}
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
              />
            </div>

            {/* Actor To */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Receiving Custodian / Unit (Actor To) *</label>
              <input
                type="text"
                required
                value={actorTo}
                onChange={(e) => setActorTo(e.target.value)}
                className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          {/* Notes */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Handoff Authorization & Security Notes</label>
            <input
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Authorized under Forensic Transfer Order #4102. Anti-static seal verified intact."
              className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setShowTransferForm(false)}
              className="px-4 py-2 bg-navy-800 hover:bg-navy-700 text-slate-300 rounded text-xs cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded text-xs flex items-center gap-2 cursor-pointer shadow-cyan-glow"
            >
              <span>Commit Transfer & Sign Block</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </form>
      )}

      {/* Filter Bar */}
      <div className="cyber-card p-3 flex items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <Filter className="w-4 h-4 text-cyan-400" />
          <span>Filter by Evidence:</span>
        </div>

        <select
          value={filterEvidenceId}
          onChange={(e) => setFilterEvidenceId(e.target.value)}
          className="bg-navy-950 border border-navy-800 rounded-lg px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 font-mono cursor-pointer"
        >
          <option value="ALL">All Evidence Artifacts ({custodyEvents.length} events)</option>
          {evidenceList.map(item => (
            <option key={item.evidenceId} value={item.evidenceId}>
              {item.evidenceId} ({item.sourceDevice.slice(0, 20)}...)
            </option>
          ))}
        </select>
      </div>

      {/* Events Timeline List */}
      <div className="space-y-3">
        {filteredEvents.map((event, index) => (
          <div key={event.eventId || index} className="cyber-card p-4 hover:border-cyan-500/40 transition-all">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2 mb-2 border-b border-navy-800/80">
              <div className="flex items-center gap-2">
                <span className="font-mono text-xs font-bold text-cyan-400 bg-navy-950 px-2 py-0.5 rounded border border-navy-800">
                  {event.evidenceId}
                </span>
                <span className="font-bold text-slate-200 text-xs">
                  {event.action}
                </span>
                <DataSourceBadge isDemoItem={event.isDemoItem} />
              </div>

              <div className="flex items-center gap-3 text-xs text-slate-400 font-mono">
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-cyan-400" />
                  {event.timestamp}
                </span>
                <span className="text-slate-600">|</span>
                <span className="text-[11px] text-slate-500">Event #{event.eventId}</span>
              </div>
            </div>

            {/* Handoff details */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs mb-2">
              <div className="bg-navy-950/60 p-2 rounded border border-navy-800/60">
                <span className="text-[10px] text-slate-500 block uppercase font-mono">Releasing Actor (From)</span>
                <span className="font-medium text-slate-200">{event.actorFrom}</span>
              </div>
              <div className="bg-navy-950/60 p-2 rounded border border-navy-800/60">
                <span className="text-[10px] text-slate-500 block uppercase font-mono">Receiving Actor (To)</span>
                <span className="font-medium text-cyan-300">{event.actorTo || 'Archive'}</span>
              </div>
            </div>

            {/* Notes */}
            {event.notes && (
              <p className="text-xs text-slate-400 italic bg-navy-950/30 p-2 rounded">
                &ldquo;{event.notes}&rdquo;
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
