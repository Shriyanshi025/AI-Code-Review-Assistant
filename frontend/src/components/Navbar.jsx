import React from 'react';
import { Cpu, Code2, Terminal, Award, Zap } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab }) {
  const tabs = [
    { id: 'problems', label: 'Challenges', icon: Code2, alsoActive: ['workspace'] },
    { id: 'freeform', label: 'Free Review', icon: Terminal },
    { id: 'progress', label: 'My Progress', icon: Award },
  ];

  return (
    <header className="navbar">
      {/* Brand */}
      <div className="navbar-brand">
        <div className="navbar-logo">
          <Cpu size={18} color="white" />
        </div>
        <div>
          <div style={{
            fontSize: '0.95rem',
            fontWeight: 800,
            background: 'linear-gradient(90deg, #ffffff 30%, #93c5fd 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            letterSpacing: '-0.01em',
            lineHeight: 1.1,
          }}>
            CodeEval AI
          </div>
          <div style={{ fontSize: '0.6rem', color: 'var(--text-dim)', letterSpacing: '0.08em', marginTop: 1 }}>
            INTELLIGENT CODE REVIEW
          </div>
        </div>
      </div>

      {/* Nav Tabs */}
      <nav className="navbar-nav">
        {tabs.map(({ id, label, icon: Icon, alsoActive = [] }) => {
          const isActive = activeTab === id || alsoActive.includes(activeTab);
          return (
            <button
              key={id}
              className={`nav-tab ${isActive ? 'active' : ''}`}
              onClick={() => setActiveTab(id)}
              id={`nav-${id}`}
            >
              <Icon size={14} />
              {label}
            </button>
          );
        })}
      </nav>

      {/* Status indicator */}
      <div className="navbar-status-badge" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
        <div style={{
          display: 'inline-flex', alignItems: 'center', gap: 6,
          padding: '0.3rem 0.7rem',
          borderRadius: 'var(--radius-full)',
          background: 'rgba(16,185,129,0.08)',
          border: '1px solid rgba(16,185,129,0.2)',
          fontSize: '0.75rem',
          fontWeight: 600,
          color: 'var(--accent-green-bright)',
        }}>
          <Zap size={12} aria-hidden="true" />
          <span>AST Engine · Java 21</span>
        </div>
      </div>
    </header>
  );
}
