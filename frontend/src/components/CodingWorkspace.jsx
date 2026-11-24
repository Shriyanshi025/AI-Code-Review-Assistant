import React, { useState, useCallback, useRef, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { analyzeProblemCode, submitSolution, fetchProblemSubmissions } from '../services/api';
import SyntaxStatusBadge from './SyntaxStatusBadge';
import InputFormatGuide from './InputFormatGuide';
import {
  Play, Send, Lightbulb, CheckCircle2, XCircle, AlertTriangle,
  Shield, Cpu, RefreshCw, ChevronLeft, FileCode2, Clock, MemoryStick,
  Eye, EyeOff, Info, Zap, BarChart2, Bug, History, RotateCcw, Circle, X,
  PanelLeftClose, PanelLeftOpen, PanelBottomClose, PanelBottomOpen,
  ChevronRight, ChevronUp, ChevronDown
} from 'lucide-react';

/* ── Helpers ─────────────────────────────────────────────────── */
function getScoreColor(s) {
  if (s >= 85) return 'var(--accent-green-bright)';
  if (s >= 65) return 'var(--accent-yellow-bright)';
  return 'var(--accent-red-bright)';
}
function getScoreClass(s) {
  if (s >= 85) return 'green';
  if (s >= 65) return 'yellow';
  return 'red';
}

function SeverityBadge({ severity }) {
  const map = {
    HIGH:   { label: 'HIGH',   color: '#f87171', bg: 'rgba(239,68,68,0.1)', border: 'rgba(239,68,68,0.25)' },
    MEDIUM: { label: 'MEDIUM', color: '#fbbf24', bg: 'rgba(245,158,11,0.1)', border: 'rgba(245,158,11,0.25)' },
    LOW:    { label: 'LOW',    color: '#60a5fa', bg: 'rgba(59,130,246,0.1)', border: 'rgba(59,130,246,0.2)' },
  };
  const s = map[severity] || map.LOW;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', padding: '0.15rem 0.45rem',
      borderRadius: 5, fontSize: '0.67rem', fontWeight: 800,
      letterSpacing: '0.06em', textTransform: 'uppercase',
      background: s.bg, color: s.color, border: `1px solid ${s.border}`,
    }}>
      {s.label}
    </span>
  );
}

/* ── MetricBar ───────────────────────────────────────────────── */
function MetricBar({ label, score }) {
  const color = score >= 85 ? '#34d399' : score >= 65 ? '#fbbf24' : '#f87171';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div style={{ width: 96, fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600, flexShrink: 0 }}>{label}</div>
      <div className="progress-bar-track" style={{ flex: 1, height: 5 }}>
        <div
          className="progress-bar-fill"
          style={{ width: `${score}%`, background: color, animationDuration: '0.5s' }}
        />
      </div>
      <div style={{ width: 28, textAlign: 'right', fontSize: '0.8rem', fontWeight: 700, color, flexShrink: 0 }}>{score}</div>
    </div>
  );
}

function FormatSuggestionText({ text }) {
  if (!text) return null;
  const lines = text.split('\n').map(l => l.trim()).filter(Boolean);
  const bulletLines = lines.filter(l => l.startsWith('•') || l.startsWith('-') || l.startsWith('*'));

  if (bulletLines.length > 0) {
    const headerLines = lines.filter(l => !l.startsWith('•') && !l.startsWith('-') && !l.startsWith('*'));
    return (
      <div style={{ marginTop: 2, flex: 1 }}>
        {headerLines.length > 0 && (
          <div style={{ fontSize: '0.74rem', color: '#34d399', marginBottom: 4, fontWeight: 700 }}>
            {headerLines.join(' ')}
          </div>
        )}
        <ul style={{
          margin: 0,
          paddingLeft: '1.1rem',
          listStyleType: 'disc',
          display: 'flex',
          flexDirection: 'column',
          gap: '0.3rem',
        }}>
          {bulletLines.map((line, idx) => {
            let clean = line.replace(/^[•\-\*]\s*/, '');
            clean = clean.replace(/^Consider testing\s+/i, '');
            clean = clean.replace(/^Consider\s+/i, '');
            clean = clean.charAt(0).toUpperCase() + clean.slice(1);
            return (
              <li key={idx} style={{ fontSize: '0.78rem', color: 'var(--text-main)', lineHeight: 1.45 }}>
                {clean}
              </li>
            );
          })}
        </ul>
      </div>
    );
  }

  return <span style={{ color: 'var(--text-main)', lineHeight: 1.45 }}>{text}</span>;
}

/* ── FindingCard ─────────────────────────────────────────────── */
function FindingCard({ finding }) {
  const [open, setOpen] = useState(true);
  const sevClass = (finding.severity || 'LOW').toLowerCase();
  const suggestionText = finding.recommendation;
  const isGuidedHint = finding.hint && finding.hint.includes("// Guided Code Hint");

  return (
    <div className={`finding-card ${sevClass}`} style={{ marginBottom: '0.65rem' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 8 }}>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4, flexWrap: 'wrap' }}>
            <SeverityBadge severity={finding.severity} />
            {finding.category && (
              <span style={{
                fontSize: '0.68rem', fontWeight: 700, letterSpacing: '0.05em',
                color: 'var(--text-muted)', background: 'rgba(255,255,255,0.05)',
                padding: '0.1rem 0.4rem', borderRadius: 4, textTransform: 'uppercase'
              }}>
                {finding.category}
              </span>
            )}
            {finding.lineStart && (
              <span style={{
                fontSize: '0.7rem', fontFamily: 'var(--font-mono)', fontWeight: 600,
                color: 'var(--text-dim)', background: 'var(--border-subtle)',
                padding: '0.1rem 0.4rem', borderRadius: 4,
              }}>
                Line {finding.lineStart}
              </span>
            )}
          </div>
          <div style={{ fontWeight: 700, fontSize: '0.85rem', color: 'var(--text-main)', marginBottom: 3 }}>
            {finding.title}
          </div>
        </div>
        <button
          onClick={() => setOpen(o => !o)}
          aria-label={open ? "Collapse finding details" : "Expand finding details"}
          style={{ background: 'none', border: 'none', color: 'var(--text-dim)', cursor: 'pointer', padding: '2px 4px', flexShrink: 0 }}
        >
          {open ? <EyeOff size={13} /> : <Eye size={13} />}
        </button>
      </div>

      {open && (
        <div style={{ animation: 'fade-in 0.15s ease', marginTop: 4 }}>
          {finding.explanation && (
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.55, marginBottom: 6 }}>
              {finding.explanation}
            </p>
          )}
          {finding.codeSnippet && (
            <pre style={{
              background: 'var(--bg-dark)', padding: '0.5rem 0.75rem', borderRadius: 6,
              fontSize: '0.78rem', color: '#93c5fd', marginBottom: 6, overflow: 'auto',
            }}>
              {finding.codeSnippet}
            </pre>
          )}
          {suggestionText && (
            <div style={{
              display: 'flex', gap: 6, alignItems: 'flex-start',
              background: 'rgba(52,211,153,0.07)', border: '1px solid rgba(52,211,153,0.15)',
              borderRadius: 6, padding: '0.55rem 0.75rem', fontSize: '0.78rem', marginTop: 6,
            }}>
              <Zap size={13} color="#34d399" style={{ flexShrink: 0, marginTop: 2 }} />
              <FormatSuggestionText text={suggestionText} />
            </div>
          )}
          {isGuidedHint && (
            <div style={{
              marginTop: 8, padding: '0.6rem 0.75rem',
              background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.25)',
              borderRadius: 6,
            }}>
              <div style={{ fontSize: '0.72rem', fontWeight: 700, color: '#fbbf24', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 4, display: 'flex', alignItems: 'center', gap: 5 }}>
                <Lightbulb size={12} /> Guided Code Hint (You are facing repeated attempts)
              </div>
              <pre style={{ margin: 0, background: 'var(--bg-dark)', padding: '0.5rem', borderRadius: 5, fontSize: '0.75rem', color: '#fbbf24', fontFamily: "'JetBrains Mono', monospace", whiteSpace: 'pre-wrap' }}>
                {finding.hint}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

/* ── SubmissionHistoryList Component ───────────────────────────────────────── */
function SubmissionHistoryList({ history, loading, onSelectSubmission }) {
  if (loading) {
    return (
      <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
        <RefreshCw size={16} className="spin" style={{ marginBottom: 6 }} />
        <div>Loading submission history...</div>
      </div>
    );
  }

  if (!history || history.length === 0) {
    return (
      <div style={{ padding: '2rem 1rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        <History size={24} style={{ marginBottom: 8, opacity: 0.4 }} />
        <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>No Submissions Yet</div>
        <div style={{ fontSize: '0.78rem', color: 'var(--text-dim)', marginTop: 4 }}>
          Submit your solution to record submission history for this challenge.
        </div>
      </div>
    );
  }

  const getStatusBadge = (status) => {
    const isPassed = status === 'PASSED' || status === 'SOLVED' || status === 'ACCEPTED';
    const isRuntimeErr = status === 'RUNTIME_ERROR';

    if (isPassed) {
      return (
        <span style={{
          display: 'inline-flex', alignItems: 'center', gap: 4,
          background: 'rgba(52,211,153,0.1)', color: '#34d399',
          border: '1px solid rgba(52,211,153,0.25)', borderRadius: 5,
          padding: '0.15rem 0.5rem', fontSize: '0.72rem', fontWeight: 700
        }}>
          <CheckCircle2 size={12} /> Accepted
        </span>
      );
    } else if (isRuntimeErr) {
      return (
        <span style={{
          display: 'inline-flex', alignItems: 'center', gap: 4,
          background: 'rgba(251,191,36,0.1)', color: '#fbbf24',
          border: '1px solid rgba(251,191,36,0.25)', borderRadius: 5,
          padding: '0.15rem 0.5rem', fontSize: '0.72rem', fontWeight: 700
        }}>
          <AlertTriangle size={12} /> Runtime Error
        </span>
      );
    } else {
      return (
        <span style={{
          display: 'inline-flex', alignItems: 'center', gap: 4,
          background: 'rgba(248,113,113,0.1)', color: '#f87171',
          border: '1px solid rgba(248,113,113,0.25)', borderRadius: 5,
          padding: '0.15rem 0.5rem', fontSize: '0.72rem', fontWeight: 700
        }}>
          <XCircle size={12} /> {status === 'COMPILATION_ERROR' ? 'Compilation Error' : 'Wrong Answer'}
        </span>
      );
    }
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return 'Recently';
    try {
      const d = new Date(isoStr);
      return d.toLocaleString(undefined, {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
      });
    } catch {
      return isoStr;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
      {history.map((sub, idx) => (
        <div
          key={sub.id || idx}
          onClick={(e) => onSelectSubmission(sub, e.currentTarget.querySelector('button') || e.currentTarget)}
          style={{
            background: 'var(--bg-dark)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 8,
            padding: '0.75rem 0.9rem',
            cursor: 'pointer',
            transition: 'all 0.15s ease',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 10,
          }}
          onMouseEnter={(e) => e.currentTarget.style.borderColor = 'var(--border-active)'}
          onMouseLeave={(e) => e.currentTarget.style.borderColor = 'var(--border-subtle)'}
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              {getStatusBadge(sub.status)}
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                {formatDate(sub.submittedAt)}
              </span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              {sub.overallScore != null && (
                <span>Score: <strong style={{ color: getScoreColor(sub.overallScore) }}>{sub.overallScore}/100</strong></span>
              )}
              {sub.passedTestCases != null && (
                <span>Tests: {sub.passedTestCases}/{sub.totalTestCases}</span>
              )}
              {sub.executionTimeMs != null && (
                <span>Runtime: {sub.executionTimeMs}ms</span>
              )}
              <span>{sub.language || 'Java'}</span>
            </div>
          </div>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onSelectSubmission(sub, e.currentTarget);
            }}
            className="btn btn-ghost"
            style={{ padding: '0.25rem 0.55rem', fontSize: '0.72rem', flexShrink: 0 }}
            aria-label={`Inspect submission #${sub.id}`}
          >
            <Eye size={12} /> Inspect
          </button>
        </div>
      ))}
    </div>
  );
}

/* ── SubmissionDetailModal Component ────────────────────────────────────────── */
function SubmissionDetailModal({ submission, onClose, onLoadIntoEditor, triggerRef }) {
  const modalRef = useRef(null);
  const closeButtonRef = useRef(null);

  useEffect(() => {
    if (!submission) return;

    const triggerEl = triggerRef?.current;

    // 1. Move focus inside when modal opens
    const timer = setTimeout(() => {
      if (closeButtonRef.current) {
        closeButtonRef.current.focus();
      } else if (modalRef.current) {
        modalRef.current.focus();
      }
    }, 50);

    // 2. Keyboard trap & Escape handler
    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onClose();
        return;
      }

      if (e.key === 'Tab') {
        if (!modalRef.current) return;
        const focusableElements = modalRef.current.querySelectorAll(
          'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
        );
        if (!focusableElements || focusableElements.length === 0) return;

        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (e.shiftKey) {
          if (document.activeElement === firstElement || document.activeElement === modalRef.current) {
            e.preventDefault();
            lastElement.focus();
          }
        } else {
          if (document.activeElement === lastElement) {
            e.preventDefault();
            firstElement.focus();
          }
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);

    return () => {
      clearTimeout(timer);
      window.removeEventListener('keydown', handleKeyDown);
      // 3. Restore focus to exact trigger on close
      if (triggerEl) {
        try {
          triggerEl.focus();
        } catch {
          // ignore if unmounted
        }
      }
    };
  }, [submission, onClose, triggerRef]);

  if (!submission) return null;

  const isPassed = submission.status === 'PASSED' || submission.status === 'SOLVED' || submission.status === 'ACCEPTED';
  const isRuntimeErr = submission.status === 'RUNTIME_ERROR';

  return (
    <div
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
      style={{
        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
        background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(4px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 1000, padding: '1rem',
      }}
    >
      <div
        ref={modalRef}
        tabIndex="-1"
        role="dialog"
        aria-modal="true"
        aria-labelledby="submission-modal-title"
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-active)',
          borderRadius: 12,
          width: '100%', maxWidth: 750,
          maxHeight: '90vh',
          display: 'flex', flexDirection: 'column',
          boxShadow: '0 20px 40px rgba(0,0,0,0.6)',
          overflow: 'hidden',
          animation: 'slide-up 0.2s ease',
          outline: 'none',
        }}
      >
        {/* Modal Header */}
        <div style={{
          padding: '1rem 1.25rem',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          background: 'var(--bg-surface)',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <h2 id="submission-modal-title" style={{ fontWeight: 800, color: 'white', fontSize: '0.95rem', margin: 0 }}>
              Submission #{submission.id}
            </h2>
            <span style={{
              display: 'inline-flex', alignItems: 'center', gap: 4,
              padding: '0.15rem 0.55rem', borderRadius: 5, fontSize: '0.72rem', fontWeight: 800,
              background: isPassed ? 'rgba(52,211,153,0.12)' : isRuntimeErr ? 'rgba(251,191,36,0.12)' : 'rgba(248,113,113,0.12)',
              color: isPassed ? '#34d399' : isRuntimeErr ? '#fbbf24' : '#f87171',
              border: `1px solid ${isPassed ? 'rgba(52,211,153,0.3)' : isRuntimeErr ? 'rgba(251,191,36,0.3)' : 'rgba(248,113,113,0.3)'}`,
            }}>
              {isPassed ? 'ACCEPTED' : isRuntimeErr ? 'RUNTIME ERROR' : submission.status}
            </span>
          </div>
          <button
            ref={closeButtonRef}
            onClick={onClose}
            className="btn-icon-ghost"
            title="Close Modal"
            aria-label="Close modal"
          >
            <X size={16} />
          </button>
        </div>

        {/* Modal Details Summary */}
        <div style={{
          padding: '0.75rem 1.25rem',
          background: 'var(--bg-dark)',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex', gap: 20, flexWrap: 'wrap', alignItems: 'center',
          fontSize: '0.8rem', color: 'var(--text-secondary)',
        }}>
          <div>Submitted: <strong style={{ color: 'white' }}>{new Date(submission.submittedAt).toLocaleString()}</strong></div>
          {submission.overallScore != null && (
            <div>Review Score: <strong style={{ color: getScoreColor(submission.overallScore) }}>{submission.overallScore}/100</strong></div>
          )}
          {submission.passedTestCases != null && (
            <div>Tests Passed: <strong style={{ color: 'white' }}>{submission.passedTestCases}/{submission.totalTestCases}</strong></div>
          )}
          {submission.executionTimeMs != null && (
            <div>Runtime: <strong style={{ color: 'white' }}>{submission.executionTimeMs} ms</strong></div>
          )}
          <SyntaxStatusBadge review={submission} />
        </div>

        {/* Modal Code Content */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '1rem 1.25rem' }}>
          <div style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: 6, letterSpacing: '0.06em' }}>
            Submitted Source Code ({submission.language || 'JAVA'})
          </div>
          <pre style={{
            background: 'var(--bg-dark)',
            padding: '1rem',
            borderRadius: 8,
            border: '1px solid var(--border-subtle)',
            fontSize: '0.83rem',
            fontFamily: "'JetBrains Mono', monospace",
            color: '#93c5fd',
            overflowX: 'auto',
            whiteSpace: 'pre-wrap',
            lineHeight: 1.5,
          }}>
            {submission.code}
          </pre>

          {submission.summary && (
            <div style={{ marginTop: '1rem', padding: '0.75rem 1rem', background: 'rgba(59,130,246,0.06)', borderRadius: 8, border: '1px solid rgba(59,130,246,0.15)' }}>
              <div style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', color: '#60a5fa', marginBottom: 4 }}>
                Review Summary
              </div>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-main)', lineHeight: 1.5 }}>
                {submission.summary}
              </p>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div style={{
          padding: '0.85rem 1.25rem',
          borderTop: '1px solid var(--border-subtle)',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          background: 'var(--bg-surface)',
        }}>
          <button onClick={onClose} className="btn btn-ghost" style={{ fontSize: '0.8rem' }}>
            Close
          </button>

          <button
            onClick={() => onLoadIntoEditor(submission.code, submission.language)}
            className="btn btn-primary"
            style={{ fontSize: '0.8rem', padding: '0.4rem 0.9rem' }}
          >
            <RotateCcw size={13} /> Load into Editor
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Main Component ─────────────────────────────────────────── */
export default function CodingWorkspace({ problem, onBack }) {
  const getCppStarterTemplate = useCallback((prob) => {
    return `#include <iostream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

// Solution for: ${prob?.title || 'Challenge'}
int main() {
    // Read input from standard input (cin)
    // Print result to standard output (cout)

    return 0;
}
`;
  }, []);

  const [language, setLanguage] = useState(() => {
    return localStorage.getItem('workspace_selected_language') || 'JAVA';
  });

  const [codeDrafts, setCodeDrafts] = useState(() => ({
    JAVA: problem?.initialCode || '',
    CPP: getCppStarterTemplate(problem)
  }));

  const [code, setCode] = useState(() => {
    const savedLang = localStorage.getItem('workspace_selected_language') || 'JAVA';
    return savedLang === 'CPP' ? getCppStarterTemplate(problem) : (problem?.initialCode || '');
  });

  const handleLanguageChange = (newLang) => {
    setCodeDrafts(prev => ({ ...prev, [language]: code }));
    setLanguage(newLang);
    localStorage.setItem('workspace_selected_language', newLang);
    const nextCode = codeDrafts[newLang] || (newLang === 'CPP' ? getCppStarterTemplate(problem) : (problem?.initialCode || ''));
    setCode(nextCode);
  };

  const [activeLeftTab, setActiveLeftTab] = useState('statement');
  const [revealedHintIdx, setRevealedHintIdx] = useState(-1);
  const [analyzing, setAnalyzing]     = useState(false);
  const [submitting, setSubmitting]   = useState(false);
  const [reviewResult, setReviewResult]   = useState(null);
  const [executionResult, setExecutionResult] = useState(null);
  const [activeReviewTab, setActiveReviewTab] = useState('overview');
  const [error, setError]             = useState(null);

  /* Submission History & Status State */
  const [currentStatus, setCurrentStatus] = useState(problem.userStatus || 'NOT_ATTEMPTED');
  const [submissionsHistory, setSubmissionsHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [selectedSubmission, setSelectedSubmission] = useState(null);
  const modalTriggerRef = useRef(null);
  const handleOpenSubmissionModal = useCallback((sub, triggerEl) => {
    modalTriggerRef.current = triggerEl || document.activeElement;
    setSelectedSubmission(sub);
  }, []);
  const [unlockedBadgeToast, setUnlockedBadgeToast] = useState(null);

  const loadHistory = useCallback(async () => {
    if (!problem?.id) return;
    setHistoryLoading(true);
    try {
      const history = await fetchProblemSubmissions(problem.id);
      setSubmissionsHistory(history);
    } catch (err) {
      console.warn('Failed to load submission history', err);
    } finally {
      setHistoryLoading(false);
    }
  }, [problem?.id]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  useEffect(() => {
    if (unlockedBadgeToast) {
      const timer = setTimeout(() => {
        setUnlockedBadgeToast(null);
      }, 6000);
      return () => clearTimeout(timer);
    }
  }, [unlockedBadgeToast]);

  /* Panel Layout State with LocalStorage Persistence */
  const editorRef = useRef(null);
  const [isDraggingLeft, setIsDraggingLeft] = useState(false);
  const [isDraggingBottom, setIsDraggingBottom] = useState(false);

  const [problemPanelWidth, setProblemPanelWidth] = useState(() => {
    const saved = localStorage.getItem('workspace_problem_width');
    return saved ? Math.min(Math.max(parseInt(saved, 10), 280), 600) : 380;
  });

  const [problemPanelVisible, setProblemPanelVisible] = useState(() => {
    const saved = localStorage.getItem('workspace_problem_visible');
    return saved !== null ? saved === 'true' : true;
  });

  const [resultsPanelHeight, setResultsPanelHeight] = useState(() => {
    const saved = localStorage.getItem('workspace_results_height');
    return saved ? Math.min(Math.max(parseInt(saved, 10), 120), 500) : 260;
  });

  const [resultsPanelVisible, setResultsPanelVisible] = useState(() => {
    const saved = localStorage.getItem('workspace_results_visible');
    return saved !== null ? saved === 'true' : true;
  });

  useEffect(() => {
    localStorage.setItem('workspace_problem_width', problemPanelWidth.toString());
  }, [problemPanelWidth]);

  useEffect(() => {
    localStorage.setItem('workspace_problem_visible', problemPanelVisible.toString());
  }, [problemPanelVisible]);

  useEffect(() => {
    localStorage.setItem('workspace_results_height', resultsPanelHeight.toString());
  }, [resultsPanelHeight]);

  useEffect(() => {
    localStorage.setItem('workspace_results_visible', resultsPanelVisible.toString());
  }, [resultsPanelVisible]);

  const handleEditorMount = useCallback((editor) => {
    editorRef.current = editor;
  }, []);

  // Update Monaco layout on panel state change
  useEffect(() => {
    if (editorRef.current) {
      requestAnimationFrame(() => {
        editorRef.current?.layout();
      });
    }
  }, [problemPanelWidth, problemPanelVisible, resultsPanelHeight, resultsPanelVisible]);

  // Window resize listener
  useEffect(() => {
    const handleResize = () => {
      editorRef.current?.layout();
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Drag Left (Problem panel width)
  const handleMouseDownLeft = useCallback((e) => {
    e.preventDefault();
    setIsDraggingLeft(true);
    document.body.style.userSelect = 'none';
    document.body.style.cursor = 'col-resize';

    const startX = e.clientX;
    const startWidth = problemPanelWidth;

    const handleMouseMove = (moveEvent) => {
      const deltaX = moveEvent.clientX - startX;
      const maxAllowedWidth = Math.min(600, window.innerWidth * 0.55);
      const newWidth = Math.min(Math.max(startWidth + deltaX, 280), maxAllowedWidth);
      setProblemPanelWidth(newWidth);
      if (editorRef.current) editorRef.current.layout();
    };

    const handleMouseUp = () => {
      setIsDraggingLeft(false);
      document.body.style.userSelect = '';
      document.body.style.cursor = '';
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
      if (editorRef.current) editorRef.current.layout();
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
  }, [problemPanelWidth]);

  // Drag Bottom (Results panel height)
  const handleMouseDownBottom = useCallback((e) => {
    e.preventDefault();
    setIsDraggingBottom(true);
    document.body.style.userSelect = 'none';
    document.body.style.cursor = 'row-resize';

    const startY = e.clientY;
    const startHeight = resultsPanelHeight;

    const handleMouseMove = (moveEvent) => {
      const deltaY = startY - moveEvent.clientY;
      const maxAllowedHeight = Math.min(550, window.innerHeight * 0.65);
      const newHeight = Math.min(Math.max(startHeight + deltaY, 120), maxAllowedHeight);
      setResultsPanelHeight(newHeight);
      if (editorRef.current) editorRef.current.layout();
    };

    const handleMouseUp = () => {
      setIsDraggingBottom(false);
      document.body.style.userSelect = '';
      document.body.style.cursor = '';
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
      if (editorRef.current) editorRef.current.layout();
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
  }, [resultsPanelHeight]);

  /* Parse examples once */
  const examples = React.useMemo(() => {
    try { return JSON.parse(problem.examplesJson || '[]'); }
    catch { return []; }
  }, [problem.examplesJson]);

  const handleRunAnalyze = useCallback(async () => {
    setAnalyzing(true);
    setReviewResult(null);
    setExecutionResult(null);
    setError(null);
    try {
      const review = await analyzeProblemCode(problem.id, code, language);
      setReviewResult(review);
      setActiveReviewTab('overview');
      setResultsPanelVisible(true);
    } catch (err) {
      setError('Analysis failed. Please try again.');
    } finally {
      setAnalyzing(false);
    }
  }, [problem.id, code, language]);

  const handleSubmit = useCallback(async () => {
    setSubmitting(true);
    setReviewResult(null);
    setExecutionResult(null);
    setError(null);
    try {
      const res = await submitSolution(problem.id, code, language);
      setReviewResult(res.reviewSession);
      setExecutionResult(res.executionResult);
      setActiveReviewTab('overview');
      setResultsPanelVisible(true);

      if (res.newlyUnlockedBadges && res.newlyUnlockedBadges.length > 0) {
        setUnlockedBadgeToast(res.newlyUnlockedBadges[0]);
      }

      if (res.executionResult?.status === 'PASSED' || res.submission?.status === 'PASSED') {
        setCurrentStatus('SOLVED');
      } else if (currentStatus !== 'SOLVED') {
        setCurrentStatus('ATTEMPTED');
      }

      loadHistory();
    } catch (err) {
      setError(err?.message || 'Submission failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }, [problem.id, code, language, currentStatus, loadHistory]);

  const handleLoadIntoEditor = (submittedCode, submittedLang) => {
    if (code.trim() !== submittedCode.trim()) {
      const confirmed = window.confirm("Loading this submission into the editor will replace your current editor code. Do you want to continue?");
      if (!confirmed) return;
    }
    if (submittedLang && submittedLang !== language) {
      setLanguage(submittedLang);
      localStorage.setItem('workspace_selected_language', submittedLang);
      setCodeDrafts(prev => ({ ...prev, [submittedLang]: submittedCode }));
    }
    setCode(submittedCode);
    setSelectedSubmission(null);
  };

  const busy = analyzing || submitting;

  const reviewTabs = [
    { id: 'overview',   label: 'Score',       icon: BarChart2 },
    { id: 'findings',   label: `Findings ${reviewResult?.findings?.length ? `(${reviewResult.findings.length})` : ''}`, icon: Bug },
    { id: 'complexity', label: 'Complexity',  icon: Cpu },
    { id: 'history',    label: `History (${submissionsHistory.length})`, icon: History },
  ];

  return (
    <div className="workspace-root">

      {/* ── Top Action Bar ───────────────────────────────────── */}
      <div className="workspace-topbar">
        {/* Left: back + problem info */}
        <div className="workspace-topbar-left">
          <button onClick={onBack} className="btn btn-ghost" style={{ padding: '0.3rem 0.65rem', fontSize: '0.78rem' }}>
            <ChevronLeft size={14} /> Problems
          </button>
          <div style={{ width: 1, height: 20, background: 'var(--border-subtle)' }} />
          <h1 style={{
            fontWeight: 700,
            fontSize: '0.9rem',
            color: 'white',
            margin: 0,
            padding: 0,
            display: 'inline',
            lineHeight: 'inherit',
          }}>
            {problem.title}
          </h1>
          <span className={`badge badge-${problem.difficulty.toLowerCase()}`}>{problem.difficulty}</span>
          <span className="category-chip">{problem.category}</span>
          {currentStatus === 'SOLVED' ? (
            <span style={{
              display: 'inline-flex', alignItems: 'center', gap: 4,
              padding: '0.15rem 0.55rem', borderRadius: 12,
              background: 'rgba(52,211,153,0.12)', border: '1px solid rgba(52,211,153,0.3)',
              color: '#34d399', fontSize: '0.73rem', fontWeight: 700
            }} title="Solved / Accepted">
              <CheckCircle2 size={12} /> Solved
            </span>
          ) : currentStatus === 'ATTEMPTED' ? (
            <span style={{
              display: 'inline-flex', alignItems: 'center', gap: 4,
              padding: '0.15rem 0.55rem', borderRadius: 12,
              background: 'rgba(251,191,36,0.12)', border: '1px solid rgba(251,191,36,0.3)',
              color: '#fbbf24', fontSize: '0.73rem', fontWeight: 700
            }} title="Attempted">
              <Clock size={12} /> Attempted
            </span>
          ) : (
            <span style={{
              display: 'inline-flex', alignItems: 'center', gap: 4,
              padding: '0.15rem 0.55rem', borderRadius: 12,
              background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-subtle)',
              color: 'var(--text-dim)', fontSize: '0.73rem', fontWeight: 600
            }} title="Not Attempted">
              <Circle size={12} /> Unattempted
            </span>
          )}
        </div>

        {/* Right: controls & actions */}
        <div className="workspace-topbar-right">

          {/* Panel Toggle Controls */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginRight: 6 }}>
            <button
              onClick={() => setProblemPanelVisible(v => !v)}
              className={`btn-icon-ghost ${problemPanelVisible ? 'active' : ''}`}
              title={problemPanelVisible ? "Hide Description Panel" : "Show Description Panel"}
              aria-label={problemPanelVisible ? "Hide Description Panel" : "Show Description Panel"}
            >
              {problemPanelVisible ? <PanelLeftClose size={15} /> : <PanelLeftOpen size={15} color="#60a5fa" />}
            </button>
            {reviewResult && (
              <button
                onClick={() => setResultsPanelVisible(v => !v)}
                className={`btn-icon-ghost ${resultsPanelVisible ? 'active' : ''}`}
                title={resultsPanelVisible ? "Hide Results Panel" : "Show Results Panel"}
                aria-label={resultsPanelVisible ? "Hide Results Panel" : "Show Results Panel"}
              >
                {resultsPanelVisible ? <PanelBottomClose size={15} /> : <PanelBottomOpen size={15} color="#60a5fa" />}
              </button>
            )}
          </div>

          <div style={{ width: 1, height: 20, background: 'var(--border-subtle)', marginRight: 4 }} />

          {/* Language Selector */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <FileCode2 size={13} color="#60a5fa" />
            <select
              id="select-language"
              value={language}
              onChange={(e) => handleLanguageChange(e.target.value)}
              className="language-selector"
              style={{
                background: 'rgba(96,165,250,0.08)',
                border: '1px solid rgba(96,165,250,0.25)',
                color: '#60a5fa',
                padding: '0.28rem 0.6rem',
                borderRadius: 6,
                fontSize: '0.76rem',
                fontWeight: 600,
                cursor: 'pointer',
                outline: 'none',
              }}
              title="Select Programming Language"
              aria-label="Select Programming Language"
            >
              <option value="JAVA" style={{ background: '#18181b', color: '#f4f4f5' }}>Java 21 (.java)</option>
              <option value="CPP" style={{ background: '#18181b', color: '#f4f4f5' }}>C++ 17 (.cpp)</option>
            </select>
            <button
              onClick={() => {
                if (window.confirm(`Reset ${language === 'CPP' ? 'C++' : 'Java'} code to default starter template?`)) {
                  const defaultCode = language === 'CPP' ? getCppStarterTemplate(problem) : (problem.initialCode || '');
                  setCode(defaultCode);
                  setCodeDrafts(prev => ({ ...prev, [language]: defaultCode }));
                }
              }}
              className="btn-icon-ghost"
              title="Reset code to default starter template"
              aria-label="Reset code to default starter template"
              style={{ padding: '0.3rem 0.45rem' }}
            >
              <RotateCcw size={12} color="#94a3b8" />
            </button>
          </div>

          <button
            id="btn-run-analyze"
            onClick={handleRunAnalyze}
            disabled={busy}
            className="btn btn-ghost"
            style={{ fontSize: '0.8rem', padding: '0.38rem 0.9rem' }}
          >
            {analyzing ? <RefreshCw size={13} className="spin" /> : <Play size={13} color="#34d399" />}
            Run & Analyze
          </button>

          <button
            id="btn-submit"
            onClick={handleSubmit}
            disabled={busy}
            className="btn btn-success"
            style={{ fontSize: '0.8rem', padding: '0.38rem 1rem' }}
          >
            {submitting ? <RefreshCw size={13} className="spin" /> : <Send size={13} />}
            Submit
          </button>
        </div>
      </div>

      {/* ── Main Body ───────────────────────────────────────── */}
      <div className="workspace-body">

        {/* ── LEFT: Problem Panel ──────────────────────────── */}
        {problemPanelVisible && (
          <div
            className="workspace-left"
            style={{ width: `${problemPanelWidth}px`, flexShrink: 0 }}
          >
            {/* Tab bar */}
            <div className="tab-bar" style={{ justifyContent: 'space-between', paddingRight: '0.5rem' }}>
              <div style={{ display: 'flex' }}>
                <button
                  className={`tab-btn ${activeLeftTab === 'statement' ? 'active' : ''}`}
                  onClick={() => setActiveLeftTab('statement')}
                >
                  <Info size={12} /> Description
                </button>
                <button
                  className={`tab-btn ${activeLeftTab === 'hints' ? 'active-yellow' : ''}`}
                  style={{ color: activeLeftTab === 'hints' ? 'var(--accent-yellow-bright)' : undefined }}
                  onClick={() => setActiveLeftTab('hints')}
                >
                  <Lightbulb size={12} />
                  Hints {problem.hints?.length ? `(${problem.hints.length})` : ''}
                </button>
                {examples.length > 0 && (
                  <button
                    className={`tab-btn ${activeLeftTab === 'examples' ? 'active-green' : ''}`}
                    style={{ color: activeLeftTab === 'examples' ? 'var(--accent-green-bright)' : undefined }}
                    onClick={() => setActiveLeftTab('examples')}
                  >
                    Examples
                  </button>
                )}
                <button
                  className={`tab-btn ${activeLeftTab === 'history' ? 'active' : ''}`}
                  onClick={() => setActiveLeftTab('history')}
                >
                  <History size={12} /> History {submissionsHistory.length ? `(${submissionsHistory.length})` : ''}
                </button>
              </div>
              <button
                onClick={() => setProblemPanelVisible(false)}
                className="btn-icon-ghost"
                title="Hide Description Panel"
                aria-label="Hide Description Panel"
              >
                <PanelLeftClose size={14} />
              </button>
            </div>

            {/* Tab content */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '1.25rem' }}>

              {/* Statement */}
              {activeLeftTab === 'statement' && (
                <div className="animate-slide-up">
                  <div className="problem-statement-title" style={{ fontSize: '1.25rem', fontWeight: 800, color: 'white', marginBottom: '0.75rem', lineHeight: 1.3 }}>
                    {problem.title}
                  </div>
                  <p style={{ fontSize: '0.875rem', lineHeight: 1.65, color: 'var(--text-main)', whiteSpace: 'pre-line', marginBottom: '1.25rem' }}>
                    {problem.description}
                  </p>

                  {/* Reusable "How to Provide Input" Guide */}
                  <InputFormatGuide problem={problem} examples={examples} />

                  {problem.outputFormat && (
                    <div style={{ marginBottom: '1rem' }}>
                      <h2 style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-muted)', marginBottom: 4 }}>Output Format</h2>
                      <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', lineHeight: 1.55 }}>{problem.outputFormat}</p>
                    </div>
                  )}

                  {problem.constraints && (
                    <div style={{ marginBottom: '1rem' }}>
                      <h2 style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-muted)', marginBottom: 6 }}>Constraints</h2>
                      <pre style={{
                        background: 'var(--bg-dark)', padding: '0.65rem 0.85rem',
                        borderRadius: 7, fontSize: '0.8rem', color: '#93c5fd',
                        border: '1px solid var(--border-subtle)',
                      }}>
                        {problem.constraints}
                      </pre>
                    </div>
                  )}

                  {/* Quick examples on statement tab */}
                  {examples.length > 0 && (
                    <div>
                      <div style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-muted)', marginBottom: 8 }}>Example</div>
                      {examples.slice(0, 1).map((ex, i) => (
                        <div key={i} style={{ background: 'var(--bg-dark)', borderRadius: 8, padding: '0.75rem', border: '1px solid var(--border-subtle)', marginBottom: 6 }}>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: 4 }}>Input</div>
                          <pre style={{ fontSize: '0.8rem', color: '#93c5fd', marginBottom: 8 }}>{ex.input}</pre>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', marginBottom: 4 }}>Output</div>
                          <pre style={{ fontSize: '0.8rem', color: '#34d399' }}>{ex.output}</pre>
                          {ex.explanation && (
                            <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: 8, lineHeight: 1.5 }}>{ex.explanation}</p>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Hints */}
              {activeLeftTab === 'hints' && (
                <div className="animate-slide-up">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: '1rem' }}>
                    <Lightbulb size={16} color="#fbbf24" />
                    <span style={{ fontWeight: 700, color: 'white', fontSize: '0.9rem' }}>Progressive Hints</span>
                  </div>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1.25rem', lineHeight: 1.5 }}>
                    Hints are revealed step-by-step — try to solve the problem first before unlocking them.
                  </p>

                  {(problem.hints && problem.hints.length > 0) ? problem.hints.map((hint, idx) => {
                    const revealed = idx <= revealedHintIdx;
                    return (
                      <div key={hint.id || idx} style={{
                        marginBottom: '0.75rem',
                        border: `1px solid ${revealed ? 'rgba(245,158,11,0.25)' : 'var(--border-subtle)'}`,
                        borderRadius: 9,
                        background: revealed ? 'rgba(245,158,11,0.04)' : 'var(--bg-dark)',
                        overflow: 'hidden',
                        transition: 'all 0.2s',
                      }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.75rem 1rem' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
                            <div style={{
                              width: 22, height: 22, borderRadius: '50%',
                              background: revealed ? 'rgba(251,191,36,0.15)' : 'var(--border-subtle)',
                              display: 'flex', alignItems: 'center', justifyContent: 'center',
                              fontSize: '0.7rem', fontWeight: 800,
                              color: revealed ? '#fbbf24' : 'var(--text-dim)',
                            }}>
                              {idx + 1}
                            </div>
                            <span style={{ fontWeight: 600, fontSize: '0.82rem', color: revealed ? '#fbbf24' : 'var(--text-muted)' }}>
                              {hint.title}
                            </span>
                          </div>
                          {!revealed && (
                            <button
                              onClick={() => setRevealedHintIdx(idx)}
                              style={{
                                background: 'none', border: '1px solid rgba(245,158,11,0.3)',
                                color: '#fbbf24', padding: '0.2rem 0.6rem', borderRadius: 5,
                                fontSize: '0.72rem', fontWeight: 600, cursor: 'pointer',
                                transition: 'background 0.15s',
                              }}
                            >
                              Reveal
                            </button>
                          )}
                        </div>
                        {revealed && (
                          <div style={{ padding: '0 1rem 0.85rem', animation: 'slide-up 0.2s ease' }}>
                            <p style={{ fontSize: '0.82rem', color: 'var(--text-main)', lineHeight: 1.6 }}>{hint.content}</p>
                          </div>
                        )}
                      </div>
                    );
                  }) : (
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>No hints available for this problem.</p>
                  )}
                </div>
              )}

              {/* Examples tab */}
              {activeLeftTab === 'examples' && (
                <div className="animate-slide-up">
                  <InputFormatGuide problem={problem} examples={examples} />
                  {examples.map((ex, i) => (
                    <div key={i} style={{ marginBottom: '1.25rem' }}>
                      <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-dim)', marginBottom: 8 }}>
                        Example {i + 1}
                      </div>
                      <div style={{ background: 'var(--bg-dark)', borderRadius: 8, border: '1px solid var(--border-subtle)', overflow: 'hidden' }}>
                        <div style={{ padding: '0.65rem 0.85rem', borderBottom: '1px solid var(--border-subtle)' }}>
                          <div style={{ fontSize: '0.68rem', color: 'var(--text-dim)', fontWeight: 700, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Input</div>
                          <pre style={{ fontSize: '0.8rem', color: '#93c5fd' }}>{ex.input}</pre>
                        </div>
                        <div style={{ padding: '0.65rem 0.85rem', borderBottom: ex.explanation ? '1px solid var(--border-subtle)' : 'none' }}>
                          <div style={{ fontSize: '0.68rem', color: 'var(--text-dim)', fontWeight: 700, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Output</div>
                          <pre style={{ fontSize: '0.8rem', color: '#34d399' }}>{ex.output}</pre>
                        </div>
                        {ex.explanation && (
                          <div style={{ padding: '0.65rem 0.85rem', background: 'rgba(59,130,246,0.04)' }}>
                            <div style={{ fontSize: '0.68rem', color: 'var(--text-dim)', fontWeight: 700, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Explanation</div>
                            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>{ex.explanation}</p>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* History tab */}
              {activeLeftTab === 'history' && (
                <div className="animate-slide-up">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: '1rem' }}>
                    <History size={16} color="#60a5fa" />
                    <span style={{ fontWeight: 700, color: 'white', fontSize: '0.9rem' }}>Submission History</span>
                  </div>
                  <SubmissionHistoryList
                    history={submissionsHistory}
                    loading={historyLoading}
                    onSelectSubmission={handleOpenSubmissionModal}
                  />
                </div>
              )}
            </div>
          </div>
        )}

        {/* ── Draggable Divider between Left Problem Panel & Editor ── */}
        {problemPanelVisible && (
          <div
            className={`resize-handle-col ${isDraggingLeft ? 'dragging' : ''}`}
            onMouseDown={handleMouseDownLeft}
            title="Drag to resize Problem Panel"
          />
        )}

        {/* ── RIGHT: Editor + Results ──────────────────────── */}
        <div className="workspace-right">
          {/* Editor */}
          <div className="workspace-editor-area">
            <Editor
              onMount={handleEditorMount}
              height="100%"
              language={language === 'CPP' ? 'cpp' : 'java'}
              theme="vs-dark"
              value={code}
              onChange={val => {
                const nextVal = val || '';
                setCode(nextVal);
                setCodeDrafts(prev => ({ ...prev, [language]: nextVal }));
              }}
              options={{
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
                fontLigatures: true,
                padding: { top: 16, bottom: 16 },
                lineNumbersMinChars: 3,
                glyphMargin: false,
                renderLineHighlight: 'all',
                cursorBlinking: 'expand',
                smoothScrolling: true,
                tabSize: 4,
              }}
            />
          </div>

          {/* Error banner */}
          {error && (
            <div className="status-banner failed" style={{ margin: '0.5rem 1rem' }}>
              <XCircle size={15} /> {error}
            </div>
          )}

          {/* Loading indicator */}
          {busy && (
            <div style={{
              padding: '0.6rem 1rem', display: 'flex', alignItems: 'center', gap: 8,
              borderTop: '2px solid var(--primary)',
              background: 'rgba(59,130,246,0.05)',
              fontSize: '0.82rem', color: 'var(--primary-bright)',
              animation: 'fade-in 0.2s ease',
            }}>
              <RefreshCw size={13} className="spin" />
              <span>{analyzing ? 'Running AST static analysis…' : 'Evaluating submission against test cases…'}</span>
            </div>
          )}

          {/* ── Draggable Divider above Results Panel ── */}
          {reviewResult && resultsPanelVisible && (
            <div
              className={`resize-handle-row ${isDraggingBottom ? 'dragging' : ''}`}
              onMouseDown={handleMouseDownBottom}
              title="Drag to resize Results Panel"
            />
          )}

          {/* Results Panel */}
          {reviewResult && resultsPanelVisible && (
            <div
              className="workspace-results"
              style={{ height: `${resultsPanelHeight}px` }}
            >
              {/* Execution result banner */}
              {executionResult && (
                <div className={`status-banner ${executionResult.status === 'PASSED' ? 'passed' : 'failed'}`}
                  style={{ margin: '0.5rem 1rem 0', borderRadius: 7, padding: '0.5rem 0.75rem' }}
                >
                  {executionResult.status === 'PASSED'
                    ? <CheckCircle2 size={14} />
                    : <XCircle size={14} />
                  }
                  <span>{executionResult.userFacingSummary}</span>
                  {executionResult.passedTestCases != null && (
                    <span style={{ marginLeft: 'auto', fontSize: '0.75rem', opacity: 0.8 }}>
                      {executionResult.passedTestCases}/{executionResult.totalTestCases} tests passed
                    </span>
                  )}
                </div>
              )}

              {/* Review Tab Bar */}
              <div className="tab-bar" style={{ paddingLeft: '0.5rem', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  {reviewTabs.map(({ id, label, icon: Icon }) => (
                    <button
                      key={id}
                      className={`tab-btn ${activeReviewTab === id ? 'active' : ''}`}
                      onClick={() => setActiveReviewTab(id)}
                    >
                      <Icon size={12} /> {label}
                    </button>
                  ))}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', paddingRight: '0.5rem' }}>
                  <span style={{
                    fontSize: '0.72rem', fontWeight: 700,
                    color: getScoreColor(reviewResult.overallScore),
                  }}>
                    Score: {reviewResult.overallScore}/100
                  </span>
                  <button
                    onClick={() => setResultsPanelVisible(false)}
                    className="btn-icon-ghost"
                    title="Hide Results Panel"
                    aria-label="Hide Results Panel"
                  >
                    <PanelBottomClose size={14} />
                  </button>
                </div>
              </div>

              {/* Review Content */}
              <div style={{ flex: 1, overflowY: 'auto', padding: '1rem' }}>

                {/* Overview Tab */}
                {activeReviewTab === 'overview' && (
                  <div className="animate-slide-up" style={{ display: 'flex', gap: '1.25rem', alignItems: 'flex-start' }}>
                    <div className={`score-circle ${getScoreClass(reviewResult.overallScore)}`}
                      style={{ flexShrink: 0 }}
                    >
                      <span className="number">{reviewResult.overallScore}</span>
                      <span className="label">Overall</span>
                    </div>

                    <div style={{ flex: 1, minWidth: 0 }}>
                      <SyntaxStatusBadge review={reviewResult} style={{ marginBottom: '0.75rem' }} />
                      {reviewResult.summary && (
                        <p style={{
                          fontSize: '0.82rem', color: 'var(--text-secondary)',
                          lineHeight: 1.55, marginBottom: '0.85rem',
                          borderLeft: '2px solid var(--border-active)',
                          paddingLeft: '0.75rem',
                        }}>
                          {reviewResult.summary}
                        </p>
                      )}
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                        {[
                          ['Correctness',     reviewResult.correctnessScore],
                          ['Code Quality',    reviewResult.qualityScore],
                          ['Complexity',      reviewResult.complexityScore],
                          ['Maintainability', reviewResult.maintainabilityScore],
                          ['Security',        reviewResult.securityScore],
                          ['Performance',     reviewResult.performanceScore],
                        ].map(([l, s]) => s != null && (
                          <MetricBar key={l} label={l} score={s} />
                        ))}
                      </div>
                    </div>
                  </div>
                )}

                {/* Findings Tab */}
                {activeReviewTab === 'findings' && (
                  <div className="animate-slide-up">
                    {(!reviewResult.findings || reviewResult.findings.length === 0) ? (
                      <div style={{ display: 'flex', gap: 10, alignItems: 'center', padding: '1rem', background: 'rgba(52,211,153,0.06)', borderRadius: 8, border: '1px solid rgba(52,211,153,0.15)' }}>
                        <CheckCircle2 size={18} color="#34d399" />
                        <div>
                          <div style={{ fontWeight: 700, color: '#34d399', fontSize: '0.85rem' }}>No issues detected</div>
                          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 2 }}>AST static analysis found no rule violations.</div>
                        </div>
                      </div>
                    ) : (
                      reviewResult.findings.map((f, i) => <FindingCard key={f.id || f.ruleId || i} finding={f} />)
                    )}

                    {/* Test suggestions */}
                    {reviewResult.testSuggestions && reviewResult.testSuggestions.length > 0 && (
                      <div style={{ marginTop: '1rem' }}>
                        <div style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.07em', color: 'var(--text-muted)', marginBottom: 8 }}>
                          Suggested Test Cases
                        </div>
                        {reviewResult.testSuggestions.map((t, i) => (
                          <div key={i} style={{
                            fontSize: '0.78rem', padding: '0.5rem 0.75rem',
                            background: 'var(--bg-surface)', borderRadius: 6, marginBottom: 5,
                            border: '1px solid var(--border-subtle)',
                          }}>
                            <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{t.category}:</span>{' '}
                            <span style={{ color: 'var(--text-secondary)' }}>{t.description}</span>
                            {t.sampleInput && (
                              <code style={{ marginLeft: 6, background: 'var(--bg-dark)', padding: '0.1rem 0.35rem', borderRadius: 4, color: '#93c5fd' }}>
                                {t.sampleInput}
                              </code>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {/* Complexity Tab */}
                {activeReviewTab === 'complexity' && (
                  <div className="animate-slide-up" style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                    <div className="metric-card" style={{ flex: 1, minWidth: 120 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 6 }}>
                        <Clock size={14} color="#60a5fa" />
                        <span className="metric-label">Time Complexity</span>
                      </div>
                      <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#60a5fa', fontFamily: 'var(--font-mono)', lineHeight: 1 }}>
                        {reviewResult.timeComplexity || '—'}
                      </div>
                    </div>
                    <div className="metric-card" style={{ flex: 1, minWidth: 120 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 6 }}>
                        <MemoryStick size={14} color="#34d399" />
                        <span className="metric-label">Space Complexity</span>
                      </div>
                      <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#34d399', fontFamily: 'var(--font-mono)', lineHeight: 1 }}>
                        {reviewResult.spaceComplexity || '—'}
                      </div>
                    </div>
                    {executionResult?.executionTimeMs != null && (
                      <div className="metric-card" style={{ flex: 1, minWidth: 120 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 6 }}>
                          <Zap size={14} color="#fbbf24" />
                          <span className="metric-label">Runtime</span>
                        </div>
                        <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#fbbf24', fontFamily: 'var(--font-mono)', lineHeight: 1 }}>
                          {executionResult.executionTimeMs} <span style={{ fontSize: '0.8rem' }}>ms</span>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* History Tab */}
                {activeReviewTab === 'history' && (
                  <div className="animate-slide-up">
                    <SubmissionHistoryList
                      history={submissionsHistory}
                      loading={historyLoading}
                      onSelectSubmission={setSelectedSubmission}
                    />
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Restorable Bottom Bar when Results Panel is Collapsed */}
          {reviewResult && !resultsPanelVisible && (
            <div
              onClick={() => setResultsPanelVisible(true)}
              onMouseEnter={e => { e.currentTarget.style.background = 'var(--bg-card-hover)'; }}
              onMouseLeave={e => { e.currentTarget.style.background = 'var(--bg-card)'; }}
              style={{
                display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                padding: '0.45rem 1rem', background: 'var(--bg-card)',
                borderTop: '1px solid var(--border-active)', cursor: 'pointer',
                fontSize: '0.78rem', color: 'var(--text-secondary)', transition: 'background 0.15s',
              }}
              title="Click to show Results Panel"
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <PanelBottomOpen size={14} color="#60a5fa" />
                <span style={{ fontWeight: 600, color: 'white' }}>Review Results</span>
                <span style={{
                  fontSize: '0.7rem', fontWeight: 700,
                  color: getScoreColor(reviewResult.overallScore),
                  background: 'rgba(255,255,255,0.06)', padding: '0.1rem 0.45rem', borderRadius: 4,
                  border: '1px solid rgba(255,255,255,0.08)',
                }}>
                  {reviewResult.overallScore}/100
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#60a5fa', fontWeight: 600, fontSize: '0.75rem' }}>
                <ChevronUp size={13} /> Expand
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Submission Detail Modal */}
      <SubmissionDetailModal
        submission={selectedSubmission}
        onClose={() => setSelectedSubmission(null)}
        onLoadIntoEditor={handleLoadIntoEditor}
        triggerRef={modalTriggerRef}
      />

      {/* Achievement Unlocked Toast Notification */}
      {unlockedBadgeToast && (
        <div style={{
          position: 'fixed',
          bottom: 28,
          right: 28,
          zIndex: 9999,
          background: 'rgba(20, 30, 50, 0.97)',
          border: '1px solid rgba(251, 191, 36, 0.45)',
          boxShadow: '0 16px 40px rgba(0, 0, 0, 0.6), 0 0 24px rgba(251, 191, 36, 0.15)',
          borderRadius: '14px',
          padding: '1rem 1.1rem',
          display: 'flex',
          alignItems: 'center',
          gap: 12,
          maxWidth: 340,
          animation: 'toast-in 0.35s cubic-bezier(0.16, 1, 0.3, 1)',
          backdropFilter: 'blur(12px)',
          WebkitBackdropFilter: 'blur(12px)',
        }}>
          {/* Glow line on left */}
          <div style={{
            position: 'absolute', left: 0, top: '15%', bottom: '15%',
            width: 3, borderRadius: '0 2px 2px 0',
            background: 'linear-gradient(180deg, #fbbf24, #f59e0b)',
          }} />
          <div style={{
            fontSize: '1.6rem',
            background: 'rgba(251, 191, 36, 0.12)',
            width: 44, height: 44,
            borderRadius: '10px',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            border: '1px solid rgba(251, 191, 36, 0.25)',
            flexShrink: 0,
            marginLeft: 8,
          }}>
            {unlockedBadgeToast.icon || '🏆'}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: '0.64rem', fontWeight: 800, color: '#fbbf24', textTransform: 'uppercase', letterSpacing: '0.07em', marginBottom: 2 }}>
              Achievement Unlocked
            </div>
            <div style={{ fontSize: '0.9rem', fontWeight: 800, color: 'white', lineHeight: 1.2 }}>
              {unlockedBadgeToast.name}
            </div>
            <div style={{ fontSize: '0.73rem', color: 'var(--text-muted)', marginTop: 3, lineHeight: 1.35 }}>
              {unlockedBadgeToast.description}
            </div>
          </div>
          <button
            onClick={() => setUnlockedBadgeToast(null)}
            className="btn-icon-ghost"
            style={{ flexShrink: 0 }}
            title="Dismiss"
            aria-label="Dismiss achievement notification"
          >
            <X size={14} />
          </button>
        </div>
      )}
    </div>
  );
}
