import React from 'react';
import { 
  Shield, 
  LayoutDashboard, 
  FolderArchive, 
  FilePlus2, 
  FileSearch, 
  History, 
  Blocks, 
  CheckCheck, 
  UserCheck, 
  LogOut,
  Radio,
  AlertOctagon,
  ChevronRight
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';

export default function Sidebar({ currentPage, setCurrentPage, isOpen, setIsOpen }) {
  const { currentUser, logout, isTampered, ledgerBlocks } = useEvidence();

  const navigation = [
    { id: 'dashboard', name: 'SOC Dashboard', icon: LayoutDashboard, badge: null },
    { id: 'catalog', name: 'Evidence Catalog', icon: FolderArchive, badge: null },
    { id: 'register', name: 'Register Evidence', icon: FilePlus2, badge: 'SHA-256' },
    { id: 'details', name: 'Evidence Inspector', icon: FileSearch, badge: null },
    { id: 'custody', name: 'Chain of Custody', icon: History, badge: null },
    { id: 'ledger', name: 'Hash-Linked Ledger', icon: Blocks, badge: isTampered ? 'ALERT' : `${ledgerBlocks.length} BLOCKS` },
    { id: 'verify', name: 'Integrity Verification', icon: CheckCheck, badge: 'REALTIME' },
  ];

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black/60 z-30 lg:hidden backdrop-blur-xs" 
          onClick={() => setIsOpen(false)}
        />
      )}

      <aside className={`
        fixed top-0 bottom-0 left-0 z-40 w-64 bg-navy-900 border-r border-navy-800 flex flex-col justify-between
        transition-transform duration-300 ease-in-out
        ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        {/* Brand Header */}
        <div>
          <div className="p-4 border-b border-navy-800 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-lg bg-cyan-500/10 border border-cyan-500/40 flex items-center justify-center text-cyan-400 shadow-cyan-glow">
                <Shield className="w-5 h-5" />
              </div>
              <div>
                <h1 className="font-bold text-slate-100 tracking-wide text-base flex items-center gap-1.5">
                  EvidenceChain
                </h1>
                <div className="flex items-center gap-1.5 text-[10px] text-cyan-400 font-mono uppercase tracking-wider">
                  <span className="h-1.5 w-1.5 rounded-full bg-cyan-400 animate-pulse"></span>
                  FORENSIC WEB SOC
                </div>
              </div>
            </div>
          </div>

          {/* Ledger Health Status Pill */}
          <div className="px-3 pt-3">
            <div className={`p-2.5 rounded-md border text-xs flex items-center justify-between ${
              isTampered 
                ? 'bg-red-500/10 border-red-500/30 text-red-300' 
                : 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
            }`}>
              <div className="flex items-center gap-2 font-mono">
                {isTampered ? (
                  <AlertOctagon className="w-4 h-4 text-red-400 animate-bounce" />
                ) : (
                  <Radio className="w-4 h-4 text-emerald-400 animate-pulse" />
                )}
                <span>{isTampered ? 'LEDGER COMPROMISED' : 'CHAIN INTEGRITY OK'}</span>
              </div>
              <span className="text-[10px] bg-navy-950 px-1.5 py-0.5 rounded font-mono">
                #{ledgerBlocks.length - 1}
              </span>
            </div>
          </div>

          {/* Navigation Items */}
          <nav className="p-3 space-y-1">
            <div className="px-2 py-1 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              Forensic Navigation
            </div>
            {navigation.map((item) => {
              const Icon = item.icon;
              const isActive = currentPage === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    setCurrentPage(item.id);
                    if (window.innerWidth < 1024) setIsOpen(false);
                  }}
                  className={`
                    w-full flex items-center justify-between px-3 py-2 rounded-md text-xs font-medium transition-all group cursor-pointer
                    ${isActive 
                      ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 shadow-xs' 
                      : 'text-slate-400 hover:text-slate-100 hover:bg-navy-800 border border-transparent'}
                  `}
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className={`w-4 h-4 transition-colors ${
                      isActive ? 'text-cyan-400' : 'text-slate-400 group-hover:text-slate-200'
                    }`} />
                    <span>{item.name}</span>
                  </div>
                  {item.badge && (
                    <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded ${
                      item.badge === 'ALERT'
                        ? 'bg-red-500/20 text-red-400 border border-red-500/30 font-bold'
                        : isActive
                          ? 'bg-cyan-500/20 text-cyan-300'
                          : 'bg-navy-950 text-slate-400'
                    }`}>
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>

        {/* User Session Footer */}
        <div className="p-3 border-t border-navy-800 bg-navy-950/60">
          {currentUser ? (
            <div>
              <div 
                onClick={() => setCurrentPage('login')}
                className="p-2 rounded-md hover:bg-navy-800 cursor-pointer transition-colors flex items-center justify-between mb-2"
                title="Switch Analyst Persona"
              >
                <div className="flex items-center gap-2.5 overflow-hidden">
                  <div className="h-8 w-8 rounded-full bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-300 font-bold text-xs shrink-0">
                    {currentUser.name.charAt(0)}
                  </div>
                  <div className="truncate">
                    <div className="text-xs font-semibold text-slate-200 truncate">{currentUser.name}</div>
                    <div className="text-[10px] text-slate-400 font-mono truncate">Badge #{currentUser.badgeNumber}</div>
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-500 shrink-0" />
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => setCurrentPage('login')}
                  className="flex-1 py-1.5 px-2 bg-navy-900 hover:bg-navy-800 border border-navy-800 hover:border-cyan-500/30 text-slate-300 hover:text-cyan-300 rounded text-[11px] font-medium flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                >
                  <UserCheck className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Switch Role</span>
                </button>
                <button
                  onClick={logout}
                  className="py-1.5 px-2 bg-navy-900 hover:bg-red-500/10 border border-navy-800 hover:border-red-500/30 text-slate-400 hover:text-red-400 rounded text-[11px] font-medium flex items-center justify-center transition-colors cursor-pointer"
                  title="Log out"
                >
                  <LogOut className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ) : (
            <button
              onClick={() => setCurrentPage('login')}
              className="w-full py-2 px-3 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded text-xs flex items-center justify-center gap-2 transition-colors cursor-pointer"
            >
              <UserCheck className="w-4 h-4" />
              <span>Analyst Sign In</span>
            </button>
          )}

          <div className="mt-2 text-center text-[10px] text-slate-400 font-mono">
            EvidenceChain Web v1.0.0
          </div>
        </div>
      </aside>
    </>
  );
}
