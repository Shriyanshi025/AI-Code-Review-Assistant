import React, { useState, useMemo } from 'react';
import { Terminal, AlertCircle, Copy, Check } from 'lucide-react';

/**
 * Normalizes any raw problem input string to the exact stdin format passed
 * to the execution engine (matching backend prepareStdinInput).
 */
function formatStdinSample(raw) {
  if (raw == null) return '';
  return String(raw).replace(/^[a-zA-Z0-9_]+\s*=\s*/, '').trim();
}

/**
 * Reusable problem input pattern configurations.
 * Classifies problem types (1D array, 2D matrix, string, multiple inputs, general)
 * and supplies beginner-friendly instructions and cautionary notes.
 */
const INPUT_TYPE_CONFIGS = [
  {
    type: '2d-matrix',
    match: (sample, category) =>
      sample.startsWith('[[') ||
      (category && (category.toLowerCase().includes('matrix') || category.toLowerCase().includes('2d'))),
    defaultInstruction: 'Enter the 2D matrix as nested bracketed lists of comma-separated integers.',
    defaultNote: 'Do not enter matrix dimensions (rows and columns) separately.',
    badge: '2D Matrix (stdin)',
  },
  {
    type: '1d-array',
    match: (sample) =>
      sample.startsWith('[') && !sample.startsWith('[[') && !sample.includes('],'),
    defaultInstruction: 'Enter the array as a bracketed list of comma-separated integers.',
    defaultNote: 'Do not enter the array size separately.',
    badge: 'Array (stdin)',
  },
  {
    type: 'multiple-inputs',
    match: (sample) =>
      sample.includes(',') && (sample.includes('],') || !sample.startsWith('[')),
    defaultInstruction: 'Enter all arguments in order, separated by commas.',
    defaultNote: 'Do not enter parameter labels or separate size arguments.',
    badge: 'Multiple Inputs (stdin)',
  },
  {
    type: 'string',
    match: (sample, category) =>
      (category && category.toLowerCase().includes('string')) ||
      (!sample.startsWith('[') && isNaN(Number(sample)) && !sample.includes(' ') && sample.length > 0),
    defaultInstruction: 'Enter the string directly as standard text input.',
    defaultNote: 'Do not enter the string length separately and do not include quotes.',
    badge: 'String (stdin)',
  },
  {
    type: 'general',
    match: () => true,
    defaultInstruction: 'Enter the input directly as formatted for standard input.',
    defaultNote: 'Do not enter variable names or extra labels.',
    badge: 'Standard Input (stdin)',
  },
];

function resolveInputGuidance(problem, sampleInput = '') {
  const category = problem?.category || '';
  const matched = INPUT_TYPE_CONFIGS.find(cfg => cfg.match(sampleInput, category)) || INPUT_TYPE_CONFIGS[INPUT_TYPE_CONFIGS.length - 1];

  // If problem.inputFormat is explicitly an instruction (e.g., starts with "Enter " or mentions "bracketed"),
  // prioritize the problem's own explicit metadata.
  const rawFormat = (problem?.inputFormat || '').trim();
  const isExplicitInstruction = rawFormat.toLowerCase().startsWith('enter') ||
    rawFormat.toLowerCase().includes('bracketed') ||
    rawFormat.toLowerCase().includes('comma-separated');

  const instruction = isExplicitInstruction ? rawFormat : matched.defaultInstruction;
  const note = problem?.inputNote || matched.defaultNote;
  const badge = matched.badge;

  return { instruction, note, badge, type: matched.type };
}

export default function InputFormatGuide({ problem, examples = [] }) {
  const [selectedCaseIdx, setSelectedCaseIdx] = useState(0);
  const [copied, setCopied] = useState(false);

  // Extract all available cases from testCases or examples
  const cases = useMemo(() => {
    const list = [];

    if (problem?.testCases && Array.isArray(problem.testCases) && problem.testCases.length > 0) {
      problem.testCases.forEach((tc, idx) => {
        list.push({
          label: tc.isHidden ? `Case ${idx + 1} (Hidden)` : `Case ${idx + 1}`,
          rawInput: tc.input || '',
          sampleInput: formatStdinSample(tc.input),
          expectedOutput: tc.expectedOutput || '',
          explanation: tc.explanation || '',
        });
      });
    } else if (examples && examples.length > 0) {
      examples.forEach((ex, idx) => {
        list.push({
          label: `Example ${idx + 1}`,
          rawInput: ex.input || '',
          sampleInput: formatStdinSample(ex.input),
          expectedOutput: ex.output || '',
          explanation: ex.explanation || '',
        });
      });
    }

    // Fallback if no cases provided
    if (list.length === 0) {
      list.push({
        label: 'Sample 1',
        rawInput: '',
        sampleInput: '',
        expectedOutput: '',
        explanation: '',
      });
    }

    return list;
  }, [problem, examples]);

  const activeCase = cases[selectedCaseIdx] || cases[0];
  const guidance = useMemo(() => {
    return resolveInputGuidance(problem, activeCase.sampleInput);
  }, [problem, activeCase.sampleInput]);

  const handleCopy = () => {
    if (!activeCase.sampleInput) return;
    navigator.clipboard?.writeText(activeCase.sampleInput).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    }).catch(() => {});
  };

  return (
    <div
      className="how-to-provide-input-guide"
      style={{
        background: 'rgba(255, 255, 255, 0.02)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 8,
        padding: '0.85rem 1rem',
        marginBottom: '1.25rem',
        animation: 'fade-in 0.2s ease',
      }}
    >
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.65rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Terminal size={14} color="#60a5fa" />
          <span style={{
            fontSize: '0.74rem',
            fontWeight: 800,
            color: 'var(--text-main)',
            letterSpacing: '0.05em',
            textTransform: 'uppercase',
          }}>
            How to Provide Input
          </span>
        </div>
        <span style={{
          fontSize: '0.67rem',
          fontWeight: 700,
          padding: '0.15rem 0.45rem',
          borderRadius: 4,
          background: 'rgba(59, 130, 246, 0.1)',
          color: '#93c5fd',
          border: '1px solid rgba(59, 130, 246, 0.25)',
          textTransform: 'uppercase',
          letterSpacing: '0.04em',
        }}>
          {guidance.badge}
        </span>
      </div>

      {/* Input Instruction */}
      <div style={{ fontSize: '0.81rem', color: 'var(--text-secondary)', marginBottom: '0.75rem', lineHeight: 1.5 }}>
        <strong style={{ color: 'var(--text-main)', fontWeight: 600 }}>Input instruction: </strong>
        <span>{guidance.instruction}</span>
      </div>

      {/* Multiple Test Cases Switcher */}
      {cases.length > 1 && (
        <div style={{ display: 'flex', gap: 6, marginBottom: '0.65rem', overflowX: 'auto', paddingBottom: 2 }}>
          {cases.map((c, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => setSelectedCaseIdx(idx)}
              style={{
                fontSize: '0.68rem',
                padding: '0.2rem 0.55rem',
                borderRadius: 4,
                border: 'none',
                cursor: 'pointer',
                background: selectedCaseIdx === idx ? 'var(--primary)' : 'rgba(255, 255, 255, 0.05)',
                color: selectedCaseIdx === idx ? '#ffffff' : 'var(--text-dim)',
                fontWeight: selectedCaseIdx === idx ? 700 : 500,
                transition: 'all 0.12s ease',
              }}
            >
              {c.label}
            </button>
          ))}
        </div>
      )}

      {/* Sample Input & Expected Output Box */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '0.6rem',
        background: 'var(--bg-dark)',
        borderRadius: 6,
        padding: '0.65rem 0.8rem',
        border: '1px solid var(--border-subtle)',
        marginBottom: '0.65rem',
      }}>
        {/* Sample Input */}
        <div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 4 }}>
            <span style={{
              fontSize: '0.67rem',
              color: 'var(--text-dim)',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}>
              Sample Input
            </span>
            {activeCase.sampleInput && (
              <button
                type="button"
                onClick={handleCopy}
                title="Copy sample input"
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: copied ? '#34d399' : 'var(--text-dim)',
                  cursor: 'pointer',
                  padding: '1px 3px',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: 3,
                  fontSize: '0.67rem',
                }}
              >
                {copied ? <Check size={11} /> : <Copy size={11} />}
                {copied ? 'Copied' : 'Copy'}
              </button>
            )}
          </div>
          <pre style={{
            margin: 0,
            fontSize: '0.8rem',
            color: '#93c5fd',
            fontFamily: 'var(--font-mono)',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-all',
          }}>
            {activeCase.sampleInput || '(No input required)'}
          </pre>
        </div>

        {/* Expected Output */}
        <div>
          <div style={{ marginBottom: 4 }}>
            <span style={{
              fontSize: '0.67rem',
              color: 'var(--text-dim)',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
            }}>
              Expected Output
            </span>
          </div>
          <pre style={{
            margin: 0,
            fontSize: '0.8rem',
            color: '#34d399',
            fontFamily: 'var(--font-mono)',
            whiteSpace: 'pre-wrap',
            wordBreak: 'break-all',
          }}>
            {activeCase.expectedOutput || '(None)'}
          </pre>
        </div>
      </div>

      {/* Note Callout */}
      {guidance.note && (
        <div style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: 6,
          background: 'rgba(245, 158, 11, 0.08)',
          border: '1px solid rgba(245, 158, 11, 0.25)',
          borderRadius: 6,
          padding: '0.45rem 0.65rem',
          fontSize: '0.78rem',
          lineHeight: 1.45,
          color: '#fde68a',
        }}>
          <AlertCircle size={13} style={{ flexShrink: 0, marginTop: 2, color: '#fbbf24' }} />
          <div>
            <strong style={{ color: '#fbbf24', fontWeight: 700 }}>Note: </strong>
            <span>{guidance.note}</span>
          </div>
        </div>
      )}
    </div>
  );
}
