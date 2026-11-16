import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { analyzeFreeformCode } from '../services/api';
import {
  Play, Sparkles, CheckCircle2, XCircle, FileCode2,
  RefreshCw, Zap, Bug, BarChart2, Shield, Eye, EyeOff, Lightbulb,
  Cpu, Clock, Circle, MemoryStick
} from 'lucide-react';

function SeverityBadge({ severity }) {
  const map = {
    HIGH:   { color: '#f87171', bg: 'rgba(239,68,68,0.1)',   border: 'rgba(239,68,68,0.25)' },
    MEDIUM: { color: '#fbbf24', bg: 'rgba(245,158,11,0.1)',  border: 'rgba(245,158,11,0.25)' },
    LOW:    { color: '#60a5fa', bg: 'rgba(59,130,246,0.1)',  border: 'rgba(59,130,246,0.2)' },
  };
  const s = map[severity] || map.LOW;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center',
      padding: '0.13rem 0.42rem', borderRadius: 4,
      fontSize: '0.67rem', fontWeight: 800,
      letterSpacing: '0.06em', textTransform: 'uppercase',
      background: s.bg, color: s.color, border: `1px solid ${s.border}`,
    }}>
      {severity}
    </span>
  );
}

function MetricBar({ label, score }) {
  const color = score >= 85 ? '#34d399' : score >= 65 ? '#fbbf24' : '#f87171';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div style={{ width: 104, fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600, flexShrink: 0 }}>{label}</div>
      <div className="progress-bar-track" style={{ flex: 1, height: 5 }}>
        <div className="progress-bar-fill" style={{ width: `${score}%`, background: color }} />
      </div>
      <div style={{ width: 26, textAlign: 'right', fontSize: '0.8rem', fontWeight: 700, color, flexShrink: 0 }}>{score}</div>
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

  return <span style={{ color: '#34d399', lineHeight: 1.4 }}>{text}</span>;
}

function FindingItem({ f }) {
  const [open, setOpen] = useState(true);
  const sevClass = (f.severity || 'LOW').toLowerCase();
  const isGuidedHint = f.hint && f.hint.includes("// Guided Code Hint");

  return (
    <div className={`finding-card ${sevClass}`} style={{ marginBottom: '0.6rem' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 6 }}>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', gap: 6, alignItems: 'center', marginBottom: 4, flexWrap: 'wrap' }}>
            <SeverityBadge severity={f.severity} />
            {f.lineStart && (
              <span style={{ fontSize: '0.68rem', fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--text-dim)', background: 'var(--border-subtle)', padding: '0.1rem 0.4rem', borderRadius: 4 }}>
                Line {f.lineStart}
              </span>
            )}
          </div>
          <div style={{ fontWeight: 700, fontSize: '0.83rem', color: 'var(--text-main)' }}>{f.title}</div>
        </div>
        <button onClick={() => setOpen(o => !o)} style={{ background: 'none', border: 'none', color: 'var(--text-dim)', cursor: 'pointer', padding: 3, flexShrink: 0 }}>
          {open ? <EyeOff size={13} /> : <Eye size={13} />}
        </button>
      </div>
      {open && (
        <div style={{ marginTop: 8, animation: 'fade-in 0.15s ease' }}>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: 1.55, marginBottom: 6 }}>{f.explanation}</p>
          {f.codeSnippet && (
            <pre style={{ background: 'var(--bg-dark)', padding: '0.45rem 0.65rem', borderRadius: 5, fontSize: '0.75rem', color: '#93c5fd', marginBottom: 6, overflow: 'auto' }}>
              {f.codeSnippet}
            </pre>
          )}
          {f.recommendation && (
            <div style={{ display: 'flex', gap: 6, alignItems: 'flex-start', background: 'rgba(52,211,153,0.07)', border: '1px solid rgba(52,211,153,0.15)', borderRadius: 5, padding: '0.5rem 0.65rem', fontSize: '0.76rem', marginTop: 6 }}>
              <Zap size={12} color="#34d399" style={{ flexShrink: 0, marginTop: 2 }} />
              <FormatSuggestionText text={f.recommendation} />
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
                {f.hint}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default function FreeformReviewView() {
  const [code, setCode]             = useState('');
  const [analyzing, setAnalyzing]   = useState(false);
  const [reviewResult, setResult]   = useState(null);
  const [error, setError]           = useState(null);
  const [activeTab, setActiveTab]   = useState('findings');

  const handleAnalyze = async () => {
    if (!code || !code.trim()) {
      setError('Add some Java code before starting the review.');
      return;
    }

    setAnalyzing(true);
    setResult(null);
    setError(null);
    try {
      const res = await analyzeFreeformCode(code);
      setResult(res);
      setActiveTab('findings');
    } catch (e) {
      setError('Analysis failed. Please check your code and try again.');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleClear = () => {
    setCode('');
    setResult(null);
    setError(null);
  };

  const overallScore = reviewResult?.overallScore;
  const scoreColor = overallScore >= 85 ? '#34d399' : overallScore >= 65 ? '#fbbf24' : '#f87171';

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '1.75rem 1.5rem', height: 'calc(100vh - 56px)', display: 'flex', flexDirection: 'column' }}>

      {/* ── Header ─────────────────────────────────────── */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1.25rem', gap: '1rem' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '0.25rem 0.65rem', borderRadius: 20, background: 'rgba(96,165,250,0.08)', border: '1px solid rgba(96,165,250,0.2)', color: '#60a5fa', fontSize: '0.73rem', fontWeight: 600, marginBottom: 8 }}>
            <FileCode2 size={12} /> Free-form Java Code Review
          </div>
          <h1 style={{ fontSize: '1.5rem', fontWeight: 900, color: 'white', letterSpacing: '-0.02em', lineHeight: 1.2 }}>
            Code Analyzer
          </h1>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: 4 }}>
            Paste or write Java code, then analyze it for quality, complexity, security, and maintainability.
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexShrink: 0 }}>
          {(code || reviewResult || error) && (
            <button
              onClick={handleClear}
              className="btn btn-ghost"
              style={{ fontSize: '0.8rem', padding: '0.5rem 0.85rem' }}
            >
              Clear
            </button>
          )}

          <button
            id="btn-analyze-freeform"
            onClick={handleAnalyze}
            disabled={analyzing}
            className="btn btn-primary"
            style={{ fontSize: '0.875rem', padding: '0.55rem 1.4rem', letterSpacing: '0.005em' }}
          >
            {analyzing ? <RefreshCw size={15} className="spin" /> : <Sparkles size={15} />}
            {analyzing ? 'Analyzing…' : 'Analyze Code'}
          </button>
        </div>
      </div>

      {/* ── Body: Split View ─────────────────────────── */}
      <div style={{ flex: 1, display: 'grid', gridTemplateColumns: reviewResult ? '1fr 1fr' : '1fr', gap: '1rem', minHeight: 0 }}>

        {/* Editor */}
        <div className="container-glass" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <div style={{
            padding: '0.55rem 0.85rem',
            borderBottom: '1px solid var(--border-subtle)',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 5 }}>
              <FileCode2 size={13} color="#60a5fa" /> Java Source
            </span>
            <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', fontFamily: 'var(--font-mono)' }}>
              {code.trim() ? `${code.split('\n').length} lines` : 'empty'}
            </span>
          </div>
          <div style={{ flex: 1 }}>
            <Editor
              height="100%"
              language="java"
              theme="vs-dark"
              value={code}
              onChange={v => setCode(v || '')}
              options={{
                fontSize: 13.5,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                automaticLayout: true,
                fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
                fontLigatures: true,
                padding: { top: 14, bottom: 14 },
                lineNumbersMinChars: 3,
                renderLineHighlight: 'all',
                cursorBlinking: 'expand',
                smoothScrolling: true,
                tabSize: 4,
              }}
            />
          </div>

          {error && (
            <div className="status-banner failed" style={{ margin: '0.5rem', borderRadius: 7 }}>
              <XCircle size={14} /> {error}
            </div>
          )}
        </div>

        {/* Results */}
        {reviewResult && (
          <div className="container-glass animate-slide-up" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            {/* Score header */}
            <div style={{
              padding: '0.85rem 1.1rem',
              borderBottom: '1px solid var(--border-subtle)',
              display: 'flex', alignItems: 'center', gap: '1rem',
            }}>
              <div className={`score-circle ${overallScore >= 85 ? 'green' : overallScore >= 65 ? 'yellow' : 'red'}`}
                style={{ width: 68, height: 68, flexShrink: 0 }}
              >
                <span className="number" style={{ fontSize: '1.4rem' }}>{overallScore}</span>
                <span className="label">Score</span>
              </div>

              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem', color: 'white', marginBottom: 4 }}>Code Review Results</div>
                <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', lineHeight: 1.45, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {reviewResult.summary}
                </p>
              </div>
            </div>

            {/* Tabs */}
            <div className="tab-bar" style={{ overflowX: 'auto', gap: 2 }}>
              <button className={`tab-btn ${activeTab === 'findings' ? 'active' : ''}`} onClick={() => setActiveTab('findings')}>
                <Bug size={12} /> Findings {reviewResult.findings?.length ? `(${reviewResult.findings.length})` : ''}
              </button>
              <button className={`tab-btn ${activeTab === 'execution' ? 'active' : ''}`} onClick={() => setActiveTab('execution')}>
                <Zap size={12} /> Execution {reviewResult.executionStatus ? `(${reviewResult.executionStatus})` : ''}
              </button>
              <button className={`tab-btn ${activeTab === 'performance' ? 'active' : ''}`} onClick={() => setActiveTab('performance')}>
                <Cpu size={12} /> Performance
              </button>
              <button className={`tab-btn ${activeTab === 'scores' ? 'active' : ''}`} onClick={() => setActiveTab('scores')}>
                <BarChart2 size={12} /> Scores
              </button>
              <button className={`tab-btn ${activeTab === 'security' ? 'active' : ''}`} onClick={() => setActiveTab('security')}>
                <Shield size={12} /> Security
              </button>
            </div>

            {/* Tab content */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '1rem' }}>
              {activeTab === 'findings' && (
                <div className="animate-slide-up">
                  {(!reviewResult.findings || reviewResult.findings.length === 0) ? (
                    <div style={{ display: 'flex', gap: 10, alignItems: 'center', padding: '1rem', background: 'rgba(52,211,153,0.06)', borderRadius: 8, border: '1px solid rgba(52,211,153,0.15)' }}>
                      <CheckCircle2 size={18} color="#34d399" />
                      <div>
                        <div style={{ fontWeight: 700, color: '#34d399', fontSize: '0.85rem' }}>No issues found</div>
                        <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: 2 }}>AST static scan found no rule violations.</div>
                      </div>
                    </div>
                  ) : (
                    reviewResult.findings.map((f, i) => <FindingItem key={f.id || f.ruleId || i} f={f} />)
                  )}
                </div>
              )}

              {activeTab === 'execution' && (
                <div className="animate-slide-up">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '0.85rem 1rem', background: 'var(--bg-dark)', borderRadius: 8, border: '1px solid var(--border-subtle)', marginBottom: '1rem' }}>
                    {reviewResult.executionStatus === 'PASSED' && (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: '0.2rem 0.65rem', borderRadius: 5, background: 'rgba(52,211,153,0.12)', color: '#34d399', border: '1px solid rgba(52,211,153,0.3)', fontWeight: 800, fontSize: '0.8rem' }}>
                        <CheckCircle2 size={14} /> PASSED
                      </span>
                    )}
                    {reviewResult.executionStatus === 'COMPILATION_ERROR' && (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: '0.2rem 0.65rem', borderRadius: 5, background: 'rgba(239,68,68,0.12)', color: '#f87171', border: '1px solid rgba(239,68,68,0.3)', fontWeight: 800, fontSize: '0.8rem' }}>
                        <XCircle size={14} /> COMPILATION ERROR
                      </span>
                    )}
                    {reviewResult.executionStatus === 'RUNTIME_ERROR' && (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: '0.2rem 0.65rem', borderRadius: 5, background: 'rgba(245,158,11,0.12)', color: '#fbbf24', border: '1px solid rgba(245,158,11,0.3)', fontWeight: 800, fontSize: '0.8rem' }}>
                        <XCircle size={14} /> RUNTIME ERROR
                      </span>
                    )}
                    {reviewResult.executionStatus === 'TIME_LIMIT_EXCEEDED' && (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: '0.2rem 0.65rem', borderRadius: 5, background: 'rgba(245,158,11,0.12)', color: '#fbbf24', border: '1px solid rgba(245,158,11,0.3)', fontWeight: 800, fontSize: '0.8rem' }}>
                        <Clock size={14} /> TIME LIMIT EXCEEDED
                      </span>
                    )}
                    {(!reviewResult.executionStatus || reviewResult.executionStatus === 'NOT_APPLICABLE') && (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: '0.2rem 0.65rem', borderRadius: 5, background: 'rgba(255,255,255,0.06)', color: 'var(--text-muted)', border: '1px solid var(--border-subtle)', fontWeight: 800, fontSize: '0.8rem' }}>
                        <Circle size={14} /> NOT APPLICABLE
                      </span>
                    )}

                    <div style={{ marginLeft: 'auto', fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Clock size={14} color="#60a5fa" />
                      Runtime: <strong>{reviewResult.executionTimeMs != null ? `${reviewResult.executionTimeMs} ms` : 'Not measured'}</strong>
                    </div>
                  </div>

                  {reviewResult.executionSummary && (
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.85rem', lineHeight: 1.5 }}>
                      {reviewResult.executionSummary}
                    </p>
                  )}

                  {reviewResult.executionStdout && (
                    <div style={{ marginBottom: '1rem' }}>
                      <div style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', color: '#34d399', marginBottom: 4, letterSpacing: '0.06em' }}>
                        Standard Output (stdout)
                      </div>
                      <pre style={{ background: 'var(--bg-dark)', padding: '0.75rem', borderRadius: 6, fontSize: '0.8rem', color: '#34d399', fontFamily: "'JetBrains Mono', monospace", border: '1px solid var(--border-subtle)', overflowX: 'auto', whiteSpace: 'pre-wrap' }}>
                        {reviewResult.executionStdout}
                      </pre>
                    </div>
                  )}

                  {reviewResult.executionStderr && (
                    <div style={{ marginBottom: '1rem' }}>
                      <div style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', color: '#f87171', marginBottom: 4, letterSpacing: '0.06em' }}>
                        Error Output (stderr / exception)
                      </div>
                      <pre style={{ background: 'var(--bg-dark)', padding: '0.75rem', borderRadius: 6, fontSize: '0.8rem', color: '#f87171', fontFamily: "'JetBrains Mono', monospace", border: '1px solid rgba(239,68,68,0.2)', overflowX: 'auto', whiteSpace: 'pre-wrap' }}>
                        {reviewResult.executionStderr}
                      </pre>
                    </div>
                  )}

                  {reviewResult.executionStatus === 'NOT_APPLICABLE' && (
                    <div style={{ padding: '1rem', background: 'rgba(96,165,250,0.06)', borderRadius: 8, border: '1px solid rgba(96,165,250,0.15)', fontSize: '0.82rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                      <strong style={{ color: '#60a5fa', display: 'block', marginBottom: 4 }}>Static Review Active</strong>
                      This snippet does not contain a runnable main entry point. Static AST analysis and AI code review are available above.
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'performance' && (
                <div className="animate-slide-up">
                  <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
                    <div className="metric-card" style={{ flex: 1, minWidth: 140 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 6 }}>
                        <Clock size={14} color="#60a5fa" />
                        <span className="metric-label">Time Complexity</span>
                      </div>
                      <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#60a5fa', fontFamily: 'var(--font-mono)', lineHeight: 1 }}>
                        {reviewResult.timeComplexity || 'O(N)'}
                      </div>
                    </div>
                    <div className="metric-card" style={{ flex: 1, minWidth: 140 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 6 }}>
                        <MemoryStick size={14} color="#34d399" />
                        <span className="metric-label">Space Complexity</span>
                      </div>
                      <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#34d399', fontFamily: 'var(--font-mono)', lineHeight: 1 }}>
                        {reviewResult.spaceComplexity || 'O(1)'}
                      </div>
                    </div>
                  </div>

                  {reviewResult.findings?.filter(f => f.category === 'PERFORMANCE' || f.category === 'COMPLEXITY' || f.ruleId?.includes('PERFORMANCE')).length > 0 ? (
                    <div>
                      <div style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: 8, letterSpacing: '0.06em' }}>
                        Optimization & Performance Opportunities
                      </div>
                      {reviewResult.findings
                        .filter(f => f.category === 'PERFORMANCE' || f.category === 'COMPLEXITY' || f.ruleId?.includes('PERFORMANCE'))
                        .map((f, i) => <FindingItem key={i} f={f} />)}
                    </div>
                  ) : (
                    <div style={{ display: 'flex', gap: 10, alignItems: 'center', padding: '1rem', background: 'rgba(52,211,153,0.06)', borderRadius: 8, border: '1px solid rgba(52,211,153,0.15)' }}>
                      <CheckCircle2 size={18} color="#34d399" />
                      <div>
                        <div style={{ fontWeight: 700, color: '#34d399', fontSize: '0.85rem' }}>✓ No significant performance issue detected</div>
                        <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', marginTop: 2 }}>
                          Estimated Time Complexity: <strong>{reviewResult.timeComplexity || 'O(N)'}</strong> | Space Complexity: <strong>{reviewResult.spaceComplexity || 'O(1)'}</strong>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {activeTab === 'scores' && (
                <div className="animate-slide-up" style={{ display: 'flex', flexDirection: 'column', gap: 7 }}>
                  {[
                    ['Correctness',     reviewResult.correctnessScore],
                    ['Code Quality',    reviewResult.qualityScore],
                    ['Complexity',      reviewResult.complexityScore],
                    ['Maintainability', reviewResult.maintainabilityScore],
                    ['Performance',     reviewResult.performanceScore],
                    ['Testing',         reviewResult.testingScore],
                  ].map(([l, s]) => s != null && <MetricBar key={l} label={l} score={s} />)}
                </div>
              )}

              {activeTab === 'security' && (
                <div className="animate-slide-up">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '0.85rem 1rem', background: `${reviewResult.securityScore >= 80 ? 'rgba(52,211,153,0.06)' : 'rgba(239,68,68,0.06)'}`, border: `1px solid ${reviewResult.securityScore >= 80 ? 'rgba(52,211,153,0.15)' : 'rgba(239,68,68,0.15)'}`, borderRadius: 8, marginBottom: '1rem' }}>
                    <Shield size={22} color={reviewResult.securityScore >= 80 ? '#34d399' : '#f87171'} />
                    <div>
                      <div style={{ fontWeight: 700, color: 'white', fontSize: '0.85rem' }}>Security Score</div>
                      <div style={{ fontSize: '1.5rem', fontWeight: 800, color: reviewResult.securityScore >= 80 ? '#34d399' : '#f87171', lineHeight: 1 }}>
                        {reviewResult.securityScore ?? '—'}<span style={{ fontSize: '0.75rem', opacity: 0.6 }}>/100</span>
                      </div>
                    </div>
                  </div>

                  {reviewResult.findings?.filter(f => f.category === 'SECURITY' || f.ruleId?.includes('SECRET')).length > 0 ? (
                    reviewResult.findings
                      .filter(f => f.category === 'SECURITY' || f.ruleId?.includes('SECRET'))
                      .map((f, i) => <FindingItem key={i} f={f} />)
                  ) : (
                    <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>No security-specific findings detected.</p>
                  )}

                  {reviewResult.testSuggestions && reviewResult.testSuggestions.length > 0 && (
                    <div style={{ marginTop: '1rem' }}>
                      <div style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.07em', color: 'var(--text-muted)', marginBottom: 8 }}>Test Suggestions</div>
                      {reviewResult.testSuggestions.map((t, i) => (
                        <div key={i} style={{ fontSize: '0.78rem', padding: '0.5rem 0.75rem', background: 'var(--bg-surface)', borderRadius: 6, marginBottom: 5, border: '1px solid var(--border-subtle)' }}>
                          <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{t.category}:</span>{' '}
                          <span style={{ color: 'var(--text-secondary)' }}>{t.description}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>

  );
}
