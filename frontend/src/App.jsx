import React, { useState } from 'react';
import Navbar from './components/Navbar';
import ProblemList from './components/ProblemList';
import CodingWorkspace from './components/CodingWorkspace';
import FreeformReviewView from './components/FreeformReviewView';
import ProgressDashboard from './components/ProgressDashboard';

export default function App() {
  const [activeTab, setActiveTab] = useState('problems');
  const [selectedProblem, setSelectedProblem] = useState(null);

  const handleSelectProblem = (problem) => {
    setSelectedProblem(problem);
    setActiveTab('workspace');
  };

  const handleBackToProblems = () => {
    setSelectedProblem(null);
    setActiveTab('problems');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'var(--bg-dark)' }}>
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {activeTab === 'problems' && (
          <div style={{ flex: 1, overflowY: 'auto' }}>
            <ProblemList onSelectProblem={handleSelectProblem} />
          </div>
        )}

        {activeTab === 'workspace' && selectedProblem && (
          <CodingWorkspace problem={selectedProblem} onBack={handleBackToProblems} />
        )}

        {activeTab === 'freeform' && (
          <FreeformReviewView />
        )}

        {activeTab === 'progress' && (
          <div style={{ flex: 1, overflowY: 'auto' }}>
            <ProgressDashboard />
          </div>
        )}
      </main>
    </div>
  );
}
