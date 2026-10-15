import React, { useState } from 'react';
import { 
  Shield, 
  Lock, 
  UserCheck, 
  AlertTriangle, 
  CheckCircle2, 
  ArrowRight,
  Fingerprint,
  Cpu,
  BadgeCheck
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { DEMO_USERS } from '../data/mockData';

export default function LoginPage({ onLoginSuccess }) {
  const { currentUser, login } = useEvidence();
  const [selectedUser, setSelectedUser] = useState(DEMO_USERS[0]);
  const [customName, setCustomName] = useState('');
  const [customBadge, setCustomBadge] = useState('');
  const [useCustom, setUseCustom] = useState(false);

  const handleSignIn = (e) => {
    e?.preventDefault();
    if (useCustom && customName.trim()) {
      login({
        id: `custom-${Date.now()}`,
        name: customName.trim(),
        badgeNumber: customBadge.trim() || '9999',
        role: 'Field Forensic Analyst',
        clearance: 'SESSION-TEMPORARY',
        station: 'Web SOC Console'
      });
    } else {
      login(selectedUser);
    }
    onLoginSuccess();
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center p-4">
      <div className="max-w-xl w-full bg-navy-900 border border-navy-800 rounded-xl p-6 md:p-8 shadow-2xl relative">
        {/* Glow accent */}
        <div className="absolute top-0 left-1/2 -translate-x-1/2 h-1 w-3/4 bg-gradient-to-r from-transparent via-cyan-500 to-transparent"></div>

        {/* Brand Header */}
        <div className="text-center mb-6">
          <div className="h-14 w-14 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 mx-auto flex items-center justify-center mb-3 shadow-cyan-glow">
            <Fingerprint className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-black text-slate-100 tracking-wide">
            EvidenceChain <span className="text-cyan-400">SOC Console</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Digital Forensics Evidence Management & Cryptographic Chain of Custody
          </p>
        </div>

        {/* Zero-Trust / Demo Notice */}
        <div className="bg-navy-950 border border-cyan-500/20 rounded-lg p-3.5 mb-6 text-xs text-slate-300">
          <div className="flex items-center gap-2 text-cyan-400 font-semibold mb-1">
            <Shield className="w-4 h-4" />
            <span>Zero-Trust Analyst Session (Demonstration)</span>
          </div>
          <p className="text-slate-400 leading-relaxed text-[11px]">
            In compliance with strict cybersecurity protocols, this client-side demonstration 
            does not transmit or store passwords in plaintext. Select an authorized Forensic Persona 
            below to initialize your local cryptographic inspection session.
          </p>
        </div>

        {/* Persona Selector */}
        <div className="space-y-3 mb-6">
          <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
            Select Forensic Analyst Persona
          </label>
          
          <div className="grid gap-2">
            {DEMO_USERS.map((user) => {
              const isSelected = !useCustom && selectedUser.id === user.id;
              return (
                <div
                  key={user.id}
                  onClick={() => {
                    setSelectedUser(user);
                    setUseCustom(false);
                  }}
                  className={`p-3 rounded-lg border text-left cursor-pointer transition-all flex items-center justify-between ${
                    isSelected
                      ? 'bg-cyan-500/10 border-cyan-500 text-slate-100 shadow-cyan-glow'
                      : 'bg-navy-950/70 border-navy-800 text-slate-400 hover:border-navy-700 hover:text-slate-200'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className={`h-8 w-8 rounded-full flex items-center justify-center font-bold text-xs ${
                      isSelected ? 'bg-cyan-500 text-navy-950' : 'bg-navy-800 text-slate-300'
                    }`}>
                      {user.name.charAt(0)}
                    </div>
                    <div>
                      <div className="text-xs font-bold text-slate-200 flex items-center gap-2">
                        {user.name}
                        <span className="text-[10px] font-mono text-cyan-400 font-normal">#{user.badgeNumber}</span>
                      </div>
                      <div className="text-[11px] text-slate-400">{user.role} &bull; <span className="font-mono text-[10px] text-slate-400">{user.clearance}</span></div>
                    </div>
                  </div>
                  {isSelected && <BadgeCheck className="w-5 h-5 text-cyan-400" />}
                </div>
              );
            })}
          </div>
        </div>

        {/* Custom Persona Toggle */}
        <div className="mb-6">
          <button
            type="button"
            onClick={() => setUseCustom(!useCustom)}
            className="text-xs text-cyan-400 hover:text-cyan-300 underline font-medium cursor-pointer"
          >
            {useCustom ? '← Back to preconfigured personas' : '+ Enter custom examiner credentials'}
          </button>

          {useCustom && (
            <div className="mt-3 p-3 bg-navy-950 border border-navy-800 rounded-lg space-y-3">
              <div>
                <label className="block text-[11px] text-slate-400 mb-1">Examiner Full Name</label>
                <input
                  type="text"
                  placeholder="e.g. Agent Alex Miller"
                  value={customName}
                  onChange={(e) => setCustomName(e.target.value)}
                  className="w-full bg-navy-900 border border-navy-700 rounded px-3 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-cyan-400"
                />
              </div>
              <div>
                <label className="block text-[11px] text-slate-400 mb-1">Badge / Officer ID</label>
                <input
                  type="text"
                  placeholder="e.g. 5501"
                  value={customBadge}
                  onChange={(e) => setCustomBadge(e.target.value)}
                  className="w-full bg-navy-900 border border-navy-700 rounded px-3 py-1.5 text-xs text-slate-100 focus:outline-none focus:border-cyan-400"
                />
              </div>
            </div>
          )}
        </div>

        {/* Sign In CTA */}
        <button
          onClick={handleSignIn}
          className="w-full py-2.5 px-4 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded-lg text-sm transition-all flex items-center justify-center gap-2 shadow-cyan-glow cursor-pointer"
        >
          <span>Authenticate & Access Console</span>
          <ArrowRight className="w-4 h-4" />
        </button>

        {/* Forensic Standards Footnote */}
        <div className="mt-6 pt-4 border-t border-navy-800 flex items-center justify-between text-[10px] text-slate-400">
          <span>Standards: ISO/IEC 27037 &bull; NIST SP 800-86</span>
          <span className="font-mono text-cyan-400">SHA-256 Enabled</span>
        </div>
      </div>
    </div>
  );
}
