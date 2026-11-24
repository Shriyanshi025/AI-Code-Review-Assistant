import React from 'react';
import { CheckCircle2, XCircle, AlertTriangle, Info } from 'lucide-react';

function getSyntaxStatusInfo(review) {
  if (!review) return null;
  const rawStatus = review.syntaxStatus ? String(review.syntaxStatus).toUpperCase().trim() : null;

  // 1. Explicit syntaxStatus takes priority
  if (rawStatus === 'VALID') {
    return {
      status: 'VALID',
      message: 'Syntax verified successfully.',
      color: '#34d399',
      bg: 'rgba(52, 211, 153, 0.08)',
      border: 'rgba(52, 211, 153, 0.25)',
      Icon: CheckCircle2,
    };
  }

  if (rawStatus === 'INVALID') {
    return {
      status: 'INVALID',
      message: 'Syntax errors detected.',
      color: '#f87171',
      bg: 'rgba(239, 68, 68, 0.08)',
      border: 'rgba(239, 68, 68, 0.25)',
      Icon: XCircle,
    };
  }

  if (rawStatus === 'UNAVAILABLE') {
    return {
      status: 'UNAVAILABLE',
      message: 'Syntax could not be verified because the compiler/analyzer is unavailable.',
      color: '#fbbf24',
      bg: 'rgba(245, 158, 11, 0.08)',
      border: 'rgba(245, 158, 11, 0.25)',
      Icon: AlertTriangle,
    };
  }

  // 2. If syntaxStatus is absent, handle legacy responses
  if (review.syntaxValid === true) {
    return {
      status: 'VALID',
      message: 'Syntax verified successfully.',
      color: '#34d399',
      bg: 'rgba(52, 211, 153, 0.08)',
      border: 'rgba(52, 211, 153, 0.25)',
      Icon: CheckCircle2,
    };
  }

  // Legacy fallback: syntaxValid === false in older records might only mean analyzer was unavailable.
  // Must NOT automatically map to INVALID; display neutral message instead.
  if (review.syntaxValid === false) {
    return {
      status: 'UNAVAILABLE',
      message: 'Syntax status unavailable for this review.',
      color: '#94a3b8',
      bg: 'rgba(148, 163, 184, 0.08)',
      border: 'rgba(148, 163, 184, 0.25)',
      Icon: Info,
    };
  }

  // Missing/null values must never imply invalid syntax.
  return null;
}

export default function SyntaxStatusBadge({ review, style }) {
  const info = getSyntaxStatusInfo(review);
  if (!info) return null;

  const { Icon, color, bg, border, message } = info;

  return (
    <div
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 6,
        padding: '0.25rem 0.65rem',
        borderRadius: 6,
        background: bg,
        border: `1px solid ${border}`,
        color: color,
        fontSize: '0.74rem',
        fontWeight: 600,
        lineHeight: 1.4,
        ...style,
      }}
      title={message}
    >
      <Icon size={13} color={color} style={{ flexShrink: 0 }} />
      <span>{message}</span>
    </div>
  );
}
