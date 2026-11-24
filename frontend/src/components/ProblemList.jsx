import React, { useState, useEffect } from 'react';
import { fetchProblems } from '../services/api';
import {
  Search, CheckCircle2, Circle, Clock, ChevronRight, ChevronLeft,
  Sparkles, BookOpen, Code2, Layers, Filter, Lightbulb
} from 'lucide-react';

const ITEMS_PER_PAGE = 20;

function DifficultyDot({ level }) {
  const colors = { EASY: '#34d399', MEDIUM: '#fbbf24', HARD: '#f87171' };
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
      <span style={{
        width: 7, height: 7, borderRadius: 'var(--radius-full)',
        background: colors[level] || '#64748b',
        display: 'inline-block',
        boxShadow: `0 0 6px ${colors[level] || '#64748b'}60`,
      }} aria-hidden="true" />
      <span className={`badge badge-${level.toLowerCase()}`}>{level}</span>
    </span>
  );
}

function StatPill({ icon: Icon, value, label, color }) {
  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: 10,
      padding: '0.55rem 0.9rem',
      background: 'var(--bg-card)',
      border: '1px solid var(--border-subtle)',
      borderRadius: 'var(--radius-md)',
      flex: '1 1 120px',
      minWidth: 110,
      transition: 'border-color 0.2s, transform 0.2s',
    }}
      onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--border-active)'; e.currentTarget.style.transform = 'translateY(-1px)'; }}
      onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border-subtle)'; e.currentTarget.style.transform = ''; }}
    >
      <div style={{
        width: 32, height: 32, borderRadius: 'var(--radius-md)',
        background: `${color}14`,
        border: `1px solid ${color}30`,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        flexShrink: 0,
      }}>
        <Icon size={15} color={color} />
      </div>
      <div>
        <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'white', lineHeight: 1 }}>{value}</div>
        <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 2, fontWeight: 600 }}>{label}</div>
      </div>
    </div>
  );
}

function getPageNumbers(current, total) {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }
  const pages = [1];
  if (current > 3) pages.push('...');
  const start = Math.max(2, current - 1);
  const end = Math.min(total - 1, current + 1);
  for (let i = start; i <= end; i++) pages.push(i);
  if (current < total - 2) pages.push('...');
  pages.push(total);
  return pages;
}

export default function ProblemList({ onSelectProblem }) {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [diffFilter, setDiffFilter] = useState('ALL');
  const [catFilter, setCatFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    fetchProblems()
      .then(data => { setProblems(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const categories = ['ALL', ...new Set(problems.map(p => p.category))];

  const filtered = problems.filter(p => {
    const q = search.toLowerCase();
    const matchSearch = !search || p.title.toLowerCase().includes(q) || p.category.toLowerCase().includes(q);
    const matchDiff = diffFilter === 'ALL' || p.difficulty === diffFilter;
    const matchCat = catFilter === 'ALL' || p.category === catFilter;
    return matchSearch && matchDiff && matchCat;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / ITEMS_PER_PAGE));
  const safePage = Math.min(currentPage, totalPages);
  const startIdx = (safePage - 1) * ITEMS_PER_PAGE;
  const paginated = filtered.slice(startIdx, startIdx + ITEMS_PER_PAGE);
  const pageNumbers = getPageNumbers(safePage, totalPages);

  const stats = {
    total:  problems.length,
    easy:   problems.filter(p => p.difficulty === 'EASY').length,
    medium: problems.filter(p => p.difficulty === 'MEDIUM').length,
    hard:   problems.filter(p => p.difficulty === 'HARD').length,
  };

  const diffBtns = [
    { id: 'ALL',    label: 'All' },
    { id: 'EASY',   label: 'Easy' },
    { id: 'MEDIUM', label: 'Medium' },
    { id: 'HARD',   label: 'Hard' },
  ];

  return (
    <div className="problem-list-container" style={{ maxWidth: 1020, margin: '0 auto', padding: '1.25rem 1.5rem' }}>

      {/* ── Hero Header ─────────────────────────────── */}
      <div style={{ marginBottom: '1rem' }}>
        <div style={{
          display: 'inline-flex', alignItems: 'center', gap: 6,
          padding: '0.24rem 0.7rem',
          borderRadius: 'var(--radius-full)',
          background: 'rgba(139,92,246,0.1)',
          border: '1px solid rgba(139,92,246,0.25)',
          color: '#d8b4fe',
          fontSize: '0.75rem',
          fontWeight: 600,
          marginBottom: '0.45rem',
        }}>
          <Sparkles size={12} aria-hidden="true" /> Intelligent DSA Practice · Deep Code Analysis
        </div>

        <h1 style={{
          fontSize: '1.65rem',
          fontWeight: 900,
          color: 'white',
          letterSpacing: '-0.02em',
          lineHeight: 1.2,
          marginBottom: '0.3rem',
        }}>
          Coding Challenges
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', maxWidth: 600 }}>
          Write Java or C++ solutions, get instant AST structural analysis, complexity estimation,
          security scans, and progressive hint guidance.
        </p>
      </div>

      {/* ── Stats Row ───────────────────────────────── */}
      <div style={{ display: 'flex', gap: '0.65rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
        <StatPill icon={Layers}   value={stats.total}  label="Total Problems" color="var(--primary-bright)" />
        <StatPill icon={BookOpen} value={stats.easy}   label="Easy"           color="#34d399" />
        <StatPill icon={BookOpen} value={stats.medium} label="Medium"         color="#fbbf24" />
        <StatPill icon={BookOpen} value={stats.hard}   label="Hard"           color="#f87171" />
      </div>

      {/* ── Filters ─────────────────────────────────── */}
      <div style={{ display: 'flex', gap: '0.65rem', marginBottom: '0.85rem', flexWrap: 'wrap', alignItems: 'center' }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: '1 1 240px', maxWidth: 340 }}>
          <label htmlFor="problem-search" className="sr-only">Search challenges by title or topic</label>
          <Search size={15} style={{
            position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)',
            color: 'var(--text-dim)', pointerEvents: 'none',
          }} aria-hidden="true" />
          <input
            id="problem-search"
            type="text"
            placeholder="Search by title or topic..."
            value={search}
            onChange={e => { setSearch(e.target.value); setCurrentPage(1); }}
            className="code-input"
          />
        </div>

        {/* Difficulty filter */}
        <div style={{ display: 'flex', gap: 3, background: 'var(--bg-card)', borderRadius: 'var(--radius-md)', padding: 3, border: '1px solid var(--border-subtle)' }}>
          {diffBtns.map(d => (
            <button
              key={d.id}
              onClick={() => { setDiffFilter(d.id); setCurrentPage(1); }}
              style={{
                padding: '0.3rem 0.7rem',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.78rem',
                fontWeight: 600,
                border: 'none',
                background: diffFilter === d.id ? 'var(--bg-card-hover)' : 'transparent',
                color: diffFilter === d.id ? 'var(--text-main)' : 'var(--text-muted)',
                cursor: 'pointer',
                transition: 'all 0.15s',
                boxShadow: diffFilter === d.id ? '0 1px 4px rgba(0,0,0,0.35)' : 'none',
              }}
            >
              {d.label}
            </button>
          ))}
        </div>

        {/* Category filter */}
        {categories.length > 1 && (
          <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
            <label htmlFor="category-filter-select" className="sr-only">Filter challenges by topic</label>
            <Filter size={13} style={{ position: 'absolute', left: 9, color: 'var(--text-dim)', pointerEvents: 'none' }} aria-hidden="true" />
            <select
              id="category-filter-select"
              value={catFilter}
              onChange={e => { setCatFilter(e.target.value); setCurrentPage(1); }}
              style={{
                paddingLeft: '1.75rem',
                paddingRight: '0.75rem',
                paddingTop: '0.4rem',
                paddingBottom: '0.4rem',
                borderRadius: 'var(--radius-sm)',
                fontSize: '0.78rem',
                fontWeight: 600,
                border: '1px solid var(--border-color)',
                background: 'var(--bg-card)',
                color: 'var(--text-main)',
                cursor: 'pointer',
                outline: 'none',
                appearance: 'none',
              }}
            >
              <option value="ALL">All Topics ({problems.length})</option>
              {categories.filter(c => c !== 'ALL').map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* ── Problem Table ───────────────────────────── */}
      {loading ? (
        <div style={{ padding: '1.5rem 0' }}>
          {[1,2,3,4,5].map(i => (
            <div key={i} style={{
              height: 48, background: 'var(--bg-card)', borderRadius: 'var(--radius-md)',
              marginBottom: 6,
              backgroundImage: 'linear-gradient(90deg, var(--border-subtle) 25%, var(--border-color) 50%, var(--border-subtle) 75%)',
              backgroundSize: '200% auto',
              animation: 'shimmer 1.5s linear infinite',
              animationDelay: `${i * 0.07}s`,
            }} />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="container-glass">
          <div className="empty-state">
            <Code2 size={32} className="empty-state-icon" aria-hidden="true" />
            <div className="empty-state-title">No challenges found</div>
            <div className="empty-state-sub">
              {search ? `No challenges match "${search}"` : 'Try adjusting your filters.'}
            </div>
          </div>
        </div>
      ) : (
        <div className="container-glass" style={{ overflow: 'hidden' }}>
          <div className="table-scroll-container">
            <table className="problem-table">
              <caption className="sr-only">Coding challenges list with difficulty, category, and completion status</caption>
              <thead>
                <tr>
                  <th scope="col" style={{ width: 44 }}>#</th>
                  <th scope="col">Problem</th>
                  <th scope="col" style={{ width: 112 }}>Difficulty</th>
                  <th scope="col" style={{ width: 145 }}>Category</th>
                  <th scope="col" style={{ width: 115 }}>Status</th>
                  <th scope="col" style={{ width: 36 }}><span className="sr-only">Action</span></th>
                </tr>
              </thead>
              <tbody>
                {paginated.map((problem, idx) => {
                  const hintCount = problem.hints?.length || 0;
                  return (
                    <tr
                      key={problem.id}
                      className="problem-row"
                      onClick={() => onSelectProblem(problem)}
                      id={`problem-row-${problem.id}`}
                    >
                      <td style={{ color: 'var(--text-dim)', fontSize: '0.8rem', fontFamily: 'var(--font-mono)', fontWeight: 600 }}>
                        {String(problem.id || startIdx + idx + 1).padStart(2, '0')}
                      </td>

                      <td>
                        <button
                          type="button"
                          onClick={() => onSelectProblem(problem)}
                          style={{
                            background: 'transparent',
                            border: 'none',
                            padding: 0,
                            margin: 0,
                            font: 'inherit',
                            color: 'inherit',
                            textAlign: 'left',
                            cursor: 'pointer',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: 8,
                            flexWrap: 'wrap',
                            width: '100%',
                          }}
                        >
                          <span className="problem-title">{problem.title}</span>
                          {hintCount > 0 && (
                            <span
                              style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: 3,
                                padding: '0.1rem 0.4rem',
                                borderRadius: 'var(--radius-xs)',
                                background: 'rgba(245, 158, 11, 0.1)',
                                border: '1px solid rgba(245, 158, 11, 0.25)',
                                fontSize: '0.75rem',
                                fontWeight: 600,
                                color: '#fbbf24',
                                lineHeight: 1.2,
                                flexShrink: 0,
                              }}
                              title={`${hintCount} hints available`}
                            >
                              <Lightbulb size={11} aria-hidden="true" />
                              <span>{hintCount}</span>
                              <span className="sr-only">hints available</span>
                            </span>
                          )}
                        </button>
                      </td>

                      <td><DifficultyDot level={problem.difficulty} /></td>

                      <td>
                        <span className="category-chip">{problem.category}</span>
                      </td>

                      <td>
                        {problem.userStatus === 'SOLVED' ? (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, color: '#34d399', fontSize: '0.78rem', fontWeight: 700 }} title="Solved / Accepted">
                            <CheckCircle2 size={14} color="#34d399" aria-hidden="true" /> Solved
                          </span>
                        ) : problem.userStatus === 'ATTEMPTED' ? (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, color: '#fbbf24', fontSize: '0.78rem', fontWeight: 700 }} title="Attempted">
                            <Clock size={14} color="#fbbf24" aria-hidden="true" /> Attempted
                          </span>
                        ) : (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, color: 'var(--text-dim)', fontSize: '0.78rem' }} title="Not Attempted">
                            <Circle size={14} color="var(--text-dim)" aria-hidden="true" /> Unattempted
                          </span>
                        )}
                      </td>

                      <td>
                        <ChevronRight size={15} className="problem-row-arrow" aria-hidden="true" />
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* List Container Footer: Challenge Count + Accessible Pagination */}
          <div className="problem-list-footer">
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              Showing {startIdx + 1}–{Math.min(startIdx + ITEMS_PER_PAGE, filtered.length)} of {filtered.length} challenges
              {filtered.length !== problems.length ? ` (${problems.length} total)` : ''}
            </div>

            {totalPages > 1 && (
              <nav className="pagination-nav" aria-label="Challenges pagination">
                <button
                  type="button"
                  className="pagination-btn"
                  onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                  disabled={safePage === 1}
                  aria-label="Previous page"
                >
                  <ChevronLeft size={14} aria-hidden="true" />
                </button>

                {pageNumbers.map((pageNum, idx) =>
                  pageNum === '...' ? (
                    <span
                      key={`ellipsis-${idx}`}
                      style={{ padding: '0 0.25rem', color: 'var(--text-dim)', fontSize: '0.78rem' }}
                      aria-hidden="true"
                    >
                      …
                    </span>
                  ) : (
                    <button
                      key={pageNum}
                      type="button"
                      className={`pagination-btn ${pageNum === safePage ? 'active' : ''}`}
                      onClick={() => setCurrentPage(pageNum)}
                      aria-label={`Page ${pageNum}`}
                      aria-current={pageNum === safePage ? 'page' : undefined}
                    >
                      {pageNum}
                    </button>
                  )
                )}

                <button
                  type="button"
                  className="pagination-btn"
                  onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                  disabled={safePage === totalPages}
                  aria-label="Next page"
                >
                  <ChevronRight size={14} aria-hidden="true" />
                </button>
              </nav>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
