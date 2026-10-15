import React, { useState } from 'react';
import { EvidenceProvider } from './context/EvidenceContext';
import DisclaimerBanner from './components/DisclaimerBanner';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';

// Pages
import DashboardPage from './pages/DashboardPage';
import CatalogPage from './pages/CatalogPage';
import RegisterPage from './pages/RegisterPage';
import DetailsPage from './pages/DetailsPage';
import CustodyPage from './pages/CustodyPage';
import LedgerPage from './pages/LedgerPage';
import IntegrityPage from './pages/IntegrityPage';
import LoginPage from './pages/LoginPage';

export default function App() {
  const [currentPage, setCurrentPage] = useState('dashboard');
  const [selectedEvidenceId, setSelectedEvidenceId] = useState('EV-0001');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const renderCurrentPage = () => {
    switch (currentPage) {
      case 'dashboard':
        return (
          <DashboardPage 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
      case 'catalog':
        return (
          <CatalogPage 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
      case 'register':
        return (
          <RegisterPage 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
      case 'details':
        return (
          <DetailsPage 
            evidenceId={selectedEvidenceId} 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
      case 'custody':
        return (
          <CustodyPage 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
      case 'ledger':
        return <LedgerPage />;
      case 'verify':
        return (
          <IntegrityPage 
            initialEvidenceId={selectedEvidenceId} 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
      case 'login':
        return (
          <LoginPage 
            onLoginSuccess={() => setCurrentPage('dashboard')} 
          />
        );
      default:
        return (
          <DashboardPage 
            setCurrentPage={setCurrentPage} 
            setSelectedEvidenceId={setSelectedEvidenceId} 
          />
        );
    }
  };

  return (
    <EvidenceProvider>
      <div className="min-h-screen bg-navy-950 text-slate-100 flex flex-col font-sans selection:bg-cyan-500/30 selection:text-cyan-200">
        {/* Top Disclaimer Banner */}
        <DisclaimerBanner />

        <div className="flex-1 flex">
          {/* Responsive Sidebar */}
          <Sidebar
            currentPage={currentPage}
            setCurrentPage={setCurrentPage}
            isOpen={isSidebarOpen}
            setIsOpen={setIsSidebarOpen}
          />

          {/* Main Content Area */}
          <div className="flex-1 flex flex-col lg:pl-64 min-w-0 transition-all">
            <Navbar
              currentPage={currentPage}
              setCurrentPage={setCurrentPage}
              setIsSidebarOpen={setIsSidebarOpen}
            />

            <main className="flex-1 p-4 md:p-6 max-w-7xl w-full mx-auto">
              {renderCurrentPage()}
            </main>

            {/* Footer */}
            <footer className="border-t border-navy-800/80 px-6 py-4 text-center text-xs text-slate-500 font-mono">
              EvidenceChain Web SOC &bull; Client-Side Cryptographic Demonstration &bull; FIPS 180-4 SHA-256 Engine
            </footer>
          </div>
        </div>
      </div>
    </EvidenceProvider>
  );
}
