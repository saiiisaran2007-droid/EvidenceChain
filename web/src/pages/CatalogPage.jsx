import React, { useState, useMemo } from 'react';
import { 
  FolderArchive, 
  Search, 
  Filter, 
  ExternalLink, 
  CheckCheck, 
  ArrowRightLeft, 
  Plus, 
  FileText,
  Database
} from 'lucide-react';
import { useEvidence } from '../context/EvidenceContext';
import { StatusBadge, TypeBadge, HashPill, DataSourceBadge } from '../components/Badge';
import { formatBytes } from '../utils/crypto';

export default function CatalogPage({ setCurrentPage, setSelectedEvidenceId }) {
  const { evidenceList } = useEvidence();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('ALL');
  const [selectedCase, setSelectedCase] = useState('ALL');

  // Extract unique cases
  const uniqueCases = useMemo(() => {
    const set = new Set(evidenceList.map(item => item.caseId));
    return Array.from(set);
  }, [evidenceList]);

  // Filter evidence
  const filteredEvidence = useMemo(() => {
    return evidenceList.filter(item => {
      const matchesSearch = 
        item.evidenceId.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.caseId.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.collector.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.sourceDevice.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.sha256Hash.toLowerCase().includes(searchQuery.toLowerCase());

      const matchesType = selectedType === 'ALL' || item.evidenceType === selectedType;
      const matchesCase = selectedCase === 'ALL' || item.caseId === selectedCase;

      return matchesSearch && matchesType && matchesCase;
    });
  }, [evidenceList, searchQuery, selectedType, selectedCase]);

  return (
    <div className="space-y-6">
      {/* Header with Title and Registration CTA */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <FolderArchive className="w-5 h-5 text-cyan-400" />
            <span>Digital Evidence Catalog</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Browse, search, and verify custody status of all cataloged forensic artifacts.
          </p>
        </div>

        <button
          onClick={() => setCurrentPage('register')}
          className="px-4 py-2 bg-cyan-500 hover:bg-cyan-400 text-navy-950 font-bold rounded-lg text-xs flex items-center gap-2 transition-all shadow-cyan-glow self-start sm:self-auto cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Register New Evidence</span>
        </button>
      </div>

      {/* Search and Filters Bar */}
      <div className="cyber-card p-4 space-y-3">
        <div className="flex flex-col md:flex-row gap-3">
          {/* Search Input */}
          <div className="flex-1 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by Evidence ID, Case, Device, Collector, or SHA-256..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-navy-950 border border-navy-800 rounded-lg pl-9 pr-4 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-400 transition-colors"
            />
          </div>

          {/* Type Filter */}
          <div className="w-full md:w-56">
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 cursor-pointer"
            >
              <option value="ALL">All Evidence Types</option>
              <option value="Disk Image">Disk Image</option>
              <option value="Memory Dump">Memory Dump</option>
              <option value="Network Capture (PCAP)">Network Capture (PCAP)</option>
              <option value="Mobile Extraction">Mobile Extraction</option>
              <option value="Digital Storage">Digital Storage</option>
              <option value="Document / Log">Document / Log</option>
            </select>
          </div>

          {/* Case Filter */}
          <div className="w-full md:w-48">
            <select
              value={selectedCase}
              onChange={(e) => setSelectedCase(e.target.value)}
              className="w-full bg-navy-950 border border-navy-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-400 cursor-pointer font-mono"
            >
              <option value="ALL">All Cases</option>
              {uniqueCases.map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Filter Stats Pill */}
        <div className="flex items-center justify-between text-xs text-slate-400 pt-1">
          <span>Showing <strong className="text-cyan-400">{filteredEvidence.length}</strong> of {evidenceList.length} evidence records</span>
          {(searchQuery || selectedType !== 'ALL' || selectedCase !== 'ALL') && (
            <button
              onClick={() => {
                setSearchQuery('');
                setSelectedType('ALL');
                setSelectedCase('ALL');
              }}
              className="text-cyan-400 hover:underline text-[11px] cursor-pointer"
            >
              Reset Filters
            </button>
          )}
        </div>
      </div>

      {/* Evidence Table */}
      <div className="cyber-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="text-[11px] text-slate-400 uppercase bg-navy-950/80 border-b border-navy-800 font-mono">
              <tr>
                <th className="py-3 px-4">Evidence ID</th>
                <th className="py-3 px-4">Case ID</th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Source Device / Description</th>
                <th className="py-3 px-4">SHA-256 Fingerprint</th>
                <th className="py-3 px-4">File Size</th>
                <th className="py-3 px-4">Origin Tag</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-800/60 text-slate-300">
              {filteredEvidence.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    <FolderArchive className="w-8 h-8 mx-auto mb-2 opacity-50" />
                    <p className="text-sm font-medium">No matching evidence found.</p>
                    <p className="text-xs text-slate-400 mt-1">Try adjusting your search terms or filter selections.</p>
                  </td>
                </tr>
              ) : (
                filteredEvidence.map((item) => (
                  <tr key={item.evidenceId} className="hover:bg-navy-800/40 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-cyan-400">
                      {item.evidenceId}
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-200">
                      {item.caseId}
                    </td>
                    <td className="py-3 px-4">
                      <TypeBadge type={item.evidenceType} />
                    </td>
                    <td className="py-3 px-4 max-w-xs">
                      <div className="font-semibold text-slate-200 truncate">{item.sourceDevice}</div>
                      <div className="text-[11px] text-slate-400 truncate">{item.description}</div>
                    </td>
                    <td className="py-3 px-4">
                      <HashPill hash={item.sha256Hash} />
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-400">
                      {formatBytes(item.fileSize)}
                    </td>
                    <td className="py-3 px-4">
                      <DataSourceBadge isDemoItem={item.isDemoItem} />
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setSelectedEvidenceId(item.evidenceId);
                            setCurrentPage('details');
                          }}
                          className="px-2 py-1 bg-navy-800 hover:bg-navy-700 text-slate-200 hover:text-cyan-300 rounded border border-navy-700 text-[11px] font-medium flex items-center gap-1 transition-colors cursor-pointer"
                          title="Inspect Metadata & Custody"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                          <span>Inspect</span>
                        </button>
                        <button
                          onClick={() => {
                            setSelectedEvidenceId(item.evidenceId);
                            setCurrentPage('verify');
                          }}
                          className="px-2 py-1 bg-navy-800 hover:bg-cyan-500/20 text-cyan-400 rounded border border-navy-700 hover:border-cyan-500/40 text-[11px] font-medium flex items-center gap-1 transition-colors cursor-pointer"
                          title="Verify SHA-256 Against Physical File"
                        >
                          <CheckCheck className="w-3.5 h-3.5" />
                          <span>Verify</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
