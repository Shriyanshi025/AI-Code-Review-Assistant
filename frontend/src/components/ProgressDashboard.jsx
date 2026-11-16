import React, { useEffect, useState } from 'react';
import { fetchUserProgress, fetchBadges } from '../services/api';
import {
  Award, CheckCircle2, Target, TrendingUp, BarChart3, Code2,
  Layers, RefreshCw, Zap, Clock, Trophy, Lock
} from 'lucide-react';

function StatCard({ icon: Icon, iconBg, iconColor, label, value, sub }) {
  return (
    <div className="stat-card">
      <div className="stat-icon" style={{ background: iconBg }}>
        <Icon size={22} color={iconColor} />
      </div>
      <div>
        <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 2 }}>
          {label}
        </div>
        <div style={{ fontSize: '1.55rem', fontWeight: 800, color: 'white', lineHeight: 1, letterSpacing: '-0.02em' }}>
          {value}
        </div>
        {sub && (
          <div style={{ fontSize: '0.7rem', color: 'var(--text-dim)', marginTop: 3 }}>{sub}</div>
        )}
      </div>
    </div>
  );
}

function TopicBar({ topic, pct, idx }) {
  const colors = [
    ['#3b82f6', '#8b5cf6'],
    ['#059669', '#34d399'],
    ['#d97706', '#fbbf24'],
    ['#db2777', '#f472b6'],
  ];
  const [from, to] = colors[idx % colors.length];
  const label = pct >= 80 ? 'Expert' : pct >= 60 ? 'Proficient' : pct >= 40 ? 'Developing' : 'Beginner';
  const labelColor = pct >= 80 ? '#34d399' : pct >= 60 ? '#60a5fa' : pct >= 40 ? '#fbbf24' : '#f87171';

  return (
    <div style={{ marginBottom: '0.9rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 6 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--text-main)' }}>{topic}</span>
          <span style={{
            fontSize: '0.65rem', fontWeight: 700,
            padding: '0.1rem 0.4rem', borderRadius: 4,
            color: labelColor,
            background: `${labelColor}18`,
          }}>
            {label}
          </span>
        </div>
        <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'white' }}>{pct}%</span>
      </div>
      <div className="progress-bar-track" style={{ height: 8 }}>
        <div
          className="progress-bar-fill"
          style={{
            width: `${pct}%`,
            background: `linear-gradient(90deg, ${from}, ${to})`,
            animationDelay: `${idx * 0.08}s`,
          }}
        />
      </div>
    </div>
  );
}

function BadgeCard({ badge }) {
  const { name, description, icon, unlocked, unlockedAt, progress, target } = badge;
  const pct = Math.min(100, Math.round(((progress || 0) / (target || 1)) * 100));

  const formatDate = (isoStr) => {
    if (!isoStr) return 'Unlocked';
    try {
      const d = new Date(isoStr);
      return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
      return 'Unlocked';
    }
  };

  return (
    <div style={{
      padding: '1rem',
      borderRadius: '10px',
      background: unlocked ? 'rgba(28, 38, 56, 0.8)' : 'rgba(15, 23, 42, 0.4)',
      border: unlocked ? '1px solid rgba(251, 191, 36, 0.3)' : '1px solid rgba(255, 255, 255, 0.06)',
      boxShadow: unlocked ? '0 4px 16px rgba(251, 191, 36, 0.08), inset 0 1px 0 rgba(255,255,255,0.04)' : 'none',
      display: 'flex',
      flexDirection: 'column',
      gap: '0.6rem',
      position: 'relative',
      opacity: unlocked ? 1 : 0.7,
      transition: 'all 0.2s ease',
      cursor: 'default',
    }}>
      {/* Header row: icon + status */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div style={{
          fontSize: '1.5rem',
          width: 42, height: 42,
          borderRadius: '9px',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: unlocked ? 'rgba(251, 191, 36, 0.12)' : 'rgba(255, 255, 255, 0.04)',
          border: unlocked ? '1px solid rgba(251, 191, 36, 0.25)' : '1px solid rgba(255, 255, 255, 0.06)',
          filter: unlocked ? 'none' : 'grayscale(100%) opacity(0.5)',
          flexShrink: 0,
        }}>
          {icon || '🏆'}
        </div>

        {unlocked ? (
          <span style={{
            fontSize: '0.62rem', fontWeight: 700,
            padding: '0.18rem 0.5rem', borderRadius: 4,
            color: '#fbbf24',
            background: 'rgba(251, 191, 36, 0.1)',
            border: '1px solid rgba(251, 191, 36, 0.2)',
            whiteSpace: 'nowrap',
          }}>
            {formatDate(unlockedAt)}
          </span>
        ) : (
          <Lock size={13} color="var(--text-dim)" style={{ marginTop: 4 }} />
        )}
      </div>

      {/* Name + description */}
      <div>
        <div style={{ fontWeight: 700, fontSize: '0.88rem', color: unlocked ? 'white' : 'var(--text-secondary)', marginBottom: 3 }}>
          {name}
        </div>
        <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)', lineHeight: 1.45 }}>
          {description}
        </div>
      </div>

      {/* Progress bar (locked only) */}
      {!unlocked && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.68rem', color: 'var(--text-dim)', marginBottom: 5, fontWeight: 600 }}>
            <span>Progress</span>
            <span>{progress || 0} / {target}</span>
          </div>
          <div className="progress-bar-track" style={{ height: 5, background: 'rgba(255,255,255,0.06)' }}>
            <div
              className="progress-bar-fill"
              style={{ width: `${pct}%`, background: 'linear-gradient(90deg, #60a5fa, #8b5cf6)' }}
            />
          </div>
        </div>
      )}
    </div>
  );
}

export default function ProgressDashboard() {
  const [progress, setProgress] = useState(null);
  const [badges, setBadges]     = useState([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchUserProgress(), fetchBadges()])
      .then(([progData, badgeData]) => {
        setProgress(progData);
        setBadges(badgeData || []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 'calc(100vh - 56px)', gap: 10, color: 'var(--text-muted)' }}>
        <RefreshCw size={18} className="spin" /> Loading analytics...
      </div>
    );
  }

  if (!progress) {
    return (
      <div style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        Failed to load progress data.
      </div>
    );
  }

  const topics = (() => {
    try { return JSON.parse(progress.topicMasteryJson || '{}'); }
    catch { return {}; }
  })();

  const attempted   = progress.attemptedCount    || 0;
  const solved      = progress.solvedCount        || 0;
  const submissions = progress.totalSubmissions   || 0;
  const hasScore    = progress.averageScore !== null && progress.averageScore !== undefined;
  const hasAttempted   = attempted   > 0;
  const hasSubmissions = submissions > 0;

  const solveRate        = hasAttempted ? Math.round((solved / attempted) * 100) : null;
  const solveRateDisplay = solveRate !== null ? `${solveRate}%` : '—';
  const avgScoreDisplay  = hasScore ? progress.averageScore : '—';

  const unlockedCount = badges.filter(b => b.unlocked).length;
  const defaultFeaturedCategories = ['Arrays', 'Strings', 'Dynamic Programming', 'Trees & Graphs', 'Sorting & Searching'];

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '2rem 1.5rem' }}>

      {/* ── Header ─────────────────────────────────────── */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{
          display: 'inline-flex', alignItems: 'center', gap: 6,
          padding: '0.25rem 0.65rem', borderRadius: 20,
          background: 'rgba(251,191,36,0.08)', border: '1px solid rgba(251,191,36,0.2)',
          color: '#fbbf24', fontSize: '0.73rem', fontWeight: 600, marginBottom: 8,
        }}>
          <Award size={12} /> Your Progress
        </div>
        <h1 style={{ fontSize: '1.8rem', fontWeight: 900, color: 'white', letterSpacing: '-0.02em', lineHeight: 1.2 }}>
          Performance & Metrics
        </h1>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 6, maxWidth: 520 }}>
          Track your problem-solving progress, review quality scores, and topic mastery over time.
        </p>
      </div>

      {/* ── Stat Cards ─────────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.85rem', marginBottom: '2rem' }}>
        <StatCard
          icon={CheckCircle2}
          iconBg="rgba(16,185,129,0.12)" iconColor="#34d399"
          label="Problems Solved"
          value={solved}
          sub={hasAttempted ? `of ${attempted} attempted` : 'no attempts yet'}
        />
        <StatCard
          icon={Target}
          iconBg="rgba(59,130,246,0.12)" iconColor="#60a5fa"
          label="Total Submissions"
          value={submissions}
          sub={hasSubmissions ? 'all attempts included' : 'no submissions yet'}
        />
        <StatCard
          icon={TrendingUp}
          iconBg="rgba(245,158,11,0.12)" iconColor="#fbbf24"
          label="Avg. Review Score"
          value={avgScoreDisplay}
          sub={hasScore ? 'out of 100' : 'no reviews yet'}
        />
        <StatCard
          icon={Zap}
          iconBg="rgba(139,92,246,0.12)" iconColor="#c084fc"
          label="Solve Rate"
          value={solveRateDisplay}
          sub={hasAttempted ? 'solved / attempted' : 'no attempts yet'}
        />
      </div>

      {/* ── Main Content ───────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(260px, 300px)', gap: '1rem', alignItems: 'start' }}>

        {/* Topic Mastery */}
        <div className="container-glass" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: '1.5rem' }}>
            <BarChart3 size={17} color="#60a5fa" />
            <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'white' }}>Topic Mastery</h2>
          </div>

          {Object.keys(topics).length > 0 ? (
            <div>
              {Object.entries(topics).map(([topic, pct], i) => (
                <TopicBar key={topic} topic={topic} pct={pct} idx={i} />
              ))}
              {defaultFeaturedCategories
                .filter(cat => !Object.prototype.hasOwnProperty.call(topics, cat))
                .map(cat => (
                  <div key={cat} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.6rem 0.8rem', marginBottom: '0.6rem', background: 'rgba(255,255,255,0.02)', borderRadius: 6, border: '1px solid rgba(255,255,255,0.04)' }}>
                    <span style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{cat}</span>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', fontStyle: 'italic' }}>No attempts yet</span>
                  </div>
                ))}
            </div>
          ) : (
            <div>
              {defaultFeaturedCategories.map(cat => (
                <div key={cat} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.6rem 0.8rem', marginBottom: '0.6rem', background: 'rgba(255,255,255,0.02)', borderRadius: 6, border: '1px solid rgba(255,255,255,0.04)' }}>
                  <span style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{cat}</span>
                  <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', fontStyle: 'italic' }}>No attempts yet</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Side panel */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>

          {/* Platform Info */}
          <div className="container-glass" style={{ padding: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: '0.85rem' }}>
              <Code2 size={15} color="#60a5fa" />
              <span style={{ fontWeight: 700, fontSize: '0.875rem', color: 'white' }}>Platform</span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {[
                ['Language',  'Java 21 (JDK)'],
                ['Engine',    'AST Static Analyzer'],
                ['Review',    'Deterministic + AI'],
                ['Version',   'V1 (Stable)'],
              ].map(([k, v]) => (
                <div key={k} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>{k}</span>
                  <span style={{ color: 'var(--text-main)', fontWeight: 600 }}>{v}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Score breakdown mini */}
          <div className="container-glass" style={{ padding: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: '0.85rem' }}>
              <Layers size={15} color="#c084fc" />
              <span style={{ fontWeight: 700, fontSize: '0.875rem', color: 'white' }}>Review Score</span>
            </div>
            <div className="score-circle" style={{ margin: '0 auto 1rem', width: 80, height: 80 }}>
              <span className="number" style={{ fontSize: '1.5rem' }}>{avgScoreDisplay}</span>
              <span className="label">{hasScore ? 'Avg Score' : 'No Reviews'}</span>
            </div>
            <p style={{ fontSize: '0.76rem', color: 'var(--text-muted)', textAlign: 'center', lineHeight: 1.5 }}>
              {!hasScore
                ? 'Submit a challenge to receive AI-powered review metrics.'
                : progress.averageScore >= 85
                ? 'Excellent! Your code quality is above average.'
                : progress.averageScore >= 70
                ? 'Good progress! Focus on edge cases and code quality.'
                : 'Keep practicing! Review the AI findings to improve.'}
            </p>
          </div>

          {/* Recent Activity */}
          <div className="container-glass" style={{ padding: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: '0.85rem' }}>
              <Clock size={15} color="#fbbf24" />
              <span style={{ fontWeight: 700, fontSize: '0.875rem', color: 'white' }}>Activity</span>
            </div>

            {!hasSubmissions ? (
              <div style={{ textAlign: 'center', padding: '0.75rem 0.5rem', color: 'var(--text-dim)', fontSize: '0.8rem' }}>
                No activity yet
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Solved</span>
                  <span style={{ color: '#34d399', fontWeight: 700 }}>{solved} problems</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Submissions</span>
                  <span style={{ color: 'white', fontWeight: 700 }}>{submissions} total</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Solve Rate</span>
                  <span style={{ color: '#60a5fa', fontWeight: 700 }}>{solveRateDisplay}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── Achievements ─────────────────────────────────── */}
      <div className="container-glass" style={{ padding: '1.5rem', marginTop: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Trophy size={18} color="#fbbf24" />
            <h2 style={{ fontSize: '1rem', fontWeight: 700, color: 'white' }}>Achievements</h2>
          </div>
          <span style={{
            fontSize: '0.75rem', fontWeight: 700,
            padding: '0.2rem 0.65rem', borderRadius: 12,
            background: 'rgba(251, 191, 36, 0.1)', border: '1px solid rgba(251, 191, 36, 0.25)',
            color: '#fbbf24'
          }}>
            {unlockedCount} / {badges.length} Unlocked
          </span>
        </div>

        {badges.length === 0 ? (
          <div className="empty-state" style={{ padding: '2rem 1rem' }}>
            <Trophy size={28} className="empty-state-icon" />
            <div className="empty-state-title">No badges available</div>
            <div className="empty-state-sub">Complete challenges to earn achievement badges.</div>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '0.85rem' }}>
            {badges
              .sort((a, b) => (b.unlocked ? 1 : 0) - (a.unlocked ? 1 : 0))
              .map(badge => (
                <BadgeCard key={badge.code} badge={badge} />
              ))}
          </div>
        )}
      </div>

    </div>
  );
}
