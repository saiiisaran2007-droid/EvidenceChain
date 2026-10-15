import React, { useState } from 'react';
import { 
  Menu, 
  RotateCcw, 
  ShieldCheck, 
  Search, 
  Cpu, 
  CheckCircle2, 
  AlertCircle
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';

export default function Navbar({ currentPage, setCurrentPage, setIsSidebarOpen }) {
  const { resetAllData, isTampered } = useEvidence();
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [resetSuccess, setResetSuccess] = useState(false);

  const pageTitles = {
    dashboard: 'Security Operations Center Dashboard',
    catalog: 'Digital Forensics Evidence Catalog',
    register: 'Register Digital Evidence (In-Browser SHA-256)',
    details: 'Evidence Inspector & Cryptographic Metadata',
    custody: 'Chain of Custody Event History',
    ledger: 'Blockchain-Style Hash-Linked Ledger',
    verify: 'Cryptographic Integrity Verification Lab',
    login: 'SOC Analyst Authentication & Role Selector'
  };

  const handleReset = () => {
    resetAllData();
    setShowResetConfirm(false);
    setResetSuccess(true);
    setTimeout(() => setResetSuccess(false), 2500);
  };

  return (
    <header className="bg-navy-900 border-b border-navy-800 px-4 py-3 flex items-center justify-between sticky top-0 z-20 shadow-md">
      {/* Left: Mobile hamburger & Breadcrumb */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => setIsSidebarOpen(prev => !prev)}
          className="lg:hidden p-1.5 rounded-md hover:bg-navy-800 text-slate-300 hover:text-cyan-400"
          aria-label="Toggle menu"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div>
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <span>EvidenceChain</span>
            <span>/</span>
            <span className="text-cyan-400 font-medium capitalize">{currentPage}</span>
          </div>
          <h2 className="text-sm md:text-base font-bold text-slate-100 flex items-center gap-2">
            {pageTitles[currentPage] || 'Forensic Console'}
          </h2>
        </div>
      </div>

      {/* Right: Engine badge, Reset Demo Data, and Quick Actions */}
      <div className="flex items-center gap-3">
        {/* Subtle Web Crypto Indicator */}
        <div className="hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded bg-navy-950 border border-navy-800 text-xs font-mono text-slate-300">
          <Cpu className="w-3.5 h-3.5 text-cyan-400" />
          <span className="text-slate-400">Engine:</span>
          <span className="text-cyan-400 font-semibold">WebCrypto / SHA-256</span>
        </div>

        {/* Reset Demo Data Button */}
        <div className="relative">
          <button
            onClick={() => setShowResetConfirm(true)}
            title="Reset to factory demonstration dataset"
            className="flex items-center gap-1.5 px-2.5 py-1.5 rounded bg-navy-800 hover:bg-navy-700 text-slate-300 hover:text-white border border-navy-700 text-xs font-medium transition-colors cursor-pointer"
          >
            <RotateCcw className="w-3.5 h-3.5 text-cyan-400" />
            <span className="hidden sm:inline">Reset Demo Data</span>
          </button>

          {/* Reset Confirmation Dialog */}
          {showResetConfirm && (
            <div className="absolute right-0 mt-2 w-72 bg-navy-900 border border-navy-700 rounded-lg p-3 shadow-xl z-50 text-xs text-slate-200">
              <div className="flex items-start gap-2 mb-2 text-amber-400">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                <span>Reset all session evidence and restore original sample ledger?</span>
              </div>
              <div className="flex justify-end gap-2 mt-3">
                <button
                  onClick={() => setShowResetConfirm(false)}
                  className="px-2.5 py-1 rounded bg-navy-800 hover:bg-navy-700 text-slate-300"
                >
                  Cancel
                </button>
                <button
                  onClick={handleReset}
                  className="px-2.5 py-1 rounded bg-red-600 hover:bg-red-500 text-white font-medium"
                >
                  Confirm Reset
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Reset Success Toast notification */}
        {resetSuccess && (
          <div className="fixed bottom-4 right-4 bg-emerald-500/90 text-navy-950 font-semibold text-xs px-3 py-2 rounded shadow-lg flex items-center gap-2 z-50 animate-fade-in">
            <CheckCircle2 className="w-4 h-4" />
            <span>Dataset restored to factory state!</span>
          </div>
        )}
      </div>
    </header>
  );
}
