import React, { useState } from 'react';
import { AlertTriangle, Info, X, ShieldAlert, Cpu } from 'lucide-react';

export default function DisclaimerBanner() {
  const [showModal, setShowModal] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  return (
    <>
      <div className="bg-navy-900 border-b border-navy-800 text-xs px-4 py-2 flex items-center justify-between transition-all">
        <div className="flex items-center gap-2 text-slate-300">
          <span className="flex h-2 w-2 relative">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-cyan-500"></span>
          </span>
          <span className="font-semibold text-cyan-400 uppercase tracking-wider">Web Architecture Notice:</span>
          <span className="hidden md:inline text-slate-300">
            Client-Side Cryptographic Demonstration. Hashes computed in-browser using Web Crypto API.
          </span>
          <span className="bg-amber-500/10 text-amber-400 border border-amber-500/20 px-1.5 py-0.5 rounded text-[10px] font-mono font-medium">
            SIMULATION MODE
          </span>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowModal(true)}
            className="text-cyan-400 hover:text-cyan-300 underline font-medium flex items-center gap-1 cursor-pointer"
          >
            <Info className="w-3.5 h-3.5" />
            <span>Architecture & Security Disclaimer</span>
          </button>
        </div>
      </div>

      {/* Detailed Modal on Architecture & Security */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-navy-900 border border-navy-800 rounded-lg max-w-2xl w-full p-6 shadow-2xl relative text-slate-200">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-100 p-1 rounded-md hover:bg-navy-800"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3 mb-4 text-cyan-400">
              <ShieldAlert className="w-6 h-6" />
              <h3 className="text-lg font-bold text-slate-100">EvidenceChain Architecture & Scope Disclosure</h3>
            </div>

            <div className="space-y-4 text-sm text-slate-300">
              <div className="p-3 bg-navy-950 border border-navy-800 rounded-md">
                <h4 className="font-semibold text-cyan-300 flex items-center gap-1.5 mb-1">
                  <Cpu className="w-4 h-4" /> 1. JavaFX Desktop Application vs. Web Client
                </h4>
                <p className="text-xs text-slate-400 leading-relaxed">
                  The primary application of EvidenceChain is a high-assurance JavaFX desktop system backed by an embedded SQLite database (<code className="text-cyan-400 font-mono">evidencechain.db</code>) and local hardware access. Vercel is a serverless frontend hosting platform that cannot run the Java desktop runtime.
                </p>
              </div>

              <div className="p-3 bg-navy-950 border border-navy-800 rounded-md">
                <h4 className="font-semibold text-amber-300 flex items-center gap-1.5 mb-1">
                  <AlertTriangle className="w-4 h-4" /> 2. Web Cryptographic Simulation & Ledger Scope
                </h4>
                <p className="text-xs text-slate-400 leading-relaxed">
                  This web edition operates purely on client-side Web Crypto APIs (<code className="text-cyan-400 font-mono">window.crypto.subtle.digest</code>) to compute genuine cryptographic SHA-256 fingerprints. However, <strong>a browser-only ledger is not a tamper-proof production backend</strong>; records here are maintained in browser session / LocalStorage for demonstration, user testing, and portfolio evaluation.
                </p>
              </div>

              <div className="p-3 bg-navy-950 border border-navy-800 rounded-md">
                <h4 className="font-semibold text-blue-300 flex items-center gap-1.5 mb-1">
                  <Info className="w-4 h-4" /> 3. Authentication & Zero-Trust Notice
                </h4>
                <p className="text-xs text-slate-400 leading-relaxed">
                  Analyst profiles are simulated for role-based demonstration (Examiner, Custodian, Legal Auditor). In compliance with cybersecurity best practices, no plaintext credentials are stored or transmitted.
                </p>
              </div>
            </div>

            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setShowModal(false)}
                className="bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-semibold px-4 py-2 rounded-md text-sm transition-colors"
              >
                Acknowledge & Close
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
