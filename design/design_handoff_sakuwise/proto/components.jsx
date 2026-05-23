// Sakuwise — shared UI components for the prototype.
// All accept a `c` (color theme) prop so they swap cleanly between light/dark.

// ─── PhoneFrame ───────────────────────────────────────────────
// Realistic Android phone shell with statusbar + nav handle.

const SW_PhoneFrame = ({ c, children, dim = false }) => (
  <div style={{
    width: 390, height: 800,
    background: c.bg,
    borderRadius: 40,
    boxShadow: c === SW_DARK
      ? '0 30px 80px rgba(0,0,0,0.55), 0 0 0 1px rgba(255,255,255,0.04)'
      : '0 30px 80px rgba(15, 76, 58, 0.18), 0 0 0 1px rgba(0,0,0,0.05)',
    overflow: 'hidden',
    position: 'relative',
    fontFamily: SW_TYPE.family,
    color: c.ink,
    filter: dim ? 'brightness(0.6)' : 'none',
    transition: 'filter 200ms ease',
  }}>
    {children}
  </div>
);

const SW_StatusBar = ({ c }) => (
  <div style={{
    height: 40, padding: '4px 28px 0',
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    fontSize: 14, fontWeight: 600, color: c.ink,
    fontVariantNumeric: 'tabular-nums',
  }}>
    <span>9:41</span>
    <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
      {/* signal */}
      <svg width="16" height="12" viewBox="0 0 16 12" fill={c.ink}>
        <rect x="1"  y="8"  width="2" height="3" rx="0.5" />
        <rect x="5"  y="6"  width="2" height="5" rx="0.5" />
        <rect x="9"  y="4"  width="2" height="7" rx="0.5" />
        <rect x="13" y="2"  width="2" height="9" rx="0.5" />
      </svg>
      {/* wifi */}
      <svg width="16" height="12" viewBox="0 0 16 12" fill="none" stroke={c.ink} strokeWidth="1.4" strokeLinecap="round">
        <path d="M2 4 C5 1 11 1 14 4" />
        <path d="M4 6.5 C6 5 10 5 12 6.5" />
        <circle cx="8" cy="9" r="0.9" fill={c.ink} />
      </svg>
      {/* battery */}
      <span style={{
        display: 'inline-flex', alignItems: 'center',
        width: 24, height: 12, border: `1.2px solid ${c.ink}`, borderRadius: 3,
        position: 'relative', padding: 1.5,
      }}>
        <span style={{ width: '78%', height: '100%', background: c.ink, borderRadius: 1 }} />
        <span style={{ position: 'absolute', right: -3, top: 3.5, width: 2, height: 5, background: c.ink, borderRadius: 1 }} />
      </span>
    </div>
  </div>
);

const SW_NavHandle = ({ c }) => (
  <div style={{
    position: 'absolute', bottom: 8, left: 0, right: 0,
    display: 'flex', justifyContent: 'center', pointerEvents: 'none',
  }}>
    <div style={{ width: 110, height: 4, background: c.ink, opacity: 0.3, borderRadius: 2 }} />
  </div>
);

// ─── TopBar ───────────────────────────────────────────────────

const SW_TopBar = ({ c, title, subtitle, onBack, right, transparent }) => (
  <div style={{
    padding: '6px 16px 12px',
    display: 'flex', alignItems: 'center', gap: 8,
    background: transparent ? 'transparent' : c.bg,
    position: 'sticky', top: 0, zIndex: 5,
  }}>
    {onBack && (
      <button onClick={onBack} className="sw-press" aria-label="Kembali" style={{
        width: 40, height: 40, borderRadius: 12, border: 'none', background: 'transparent',
        display: 'flex', alignItems: 'center', justifyContent: 'center', color: c.ink, cursor: 'pointer',
      }}>
        <Icon name="back" size={22} />
      </button>
    )}
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{
        fontSize: 19, fontWeight: 700, letterSpacing: '-0.01em', color: c.ink,
        whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
      }}>{title}</div>
      {subtitle && (
        <div style={{ fontSize: 12, color: c.inkMuted, marginTop: 1 }}>{subtitle}</div>
      )}
    </div>
    {right}
  </div>
);

// ─── TabBar ──────────────────────────────────────────────────

const SW_TabBar = ({ c, active, onChange, onAdd }) => {
  const tabs = [
    { id: 'home', label: 'Beranda', icon: 'home' },
    { id: 'plan', label: 'Plan', icon: 'plan' },
    { id: 'add', label: '', icon: 'plus', isAdd: true },
    { id: 'assets', label: 'Aset', icon: 'assets' },
    { id: 'me', label: 'Saya', icon: 'me' },
  ];
  return (
    <div style={{
      position: 'absolute', bottom: 0, left: 0, right: 0,
      background: c.surface,
      borderTop: `1px solid ${c.border}`,
      paddingBottom: 18,
      paddingTop: 6,
      display: 'flex',
      justifyContent: 'space-around',
      zIndex: 4,
    }}>
      {tabs.map(t => {
        if (t.isAdd) {
          return (
            <button key={t.id} onClick={onAdd} className="sw-press" aria-label="Tambah transaksi" style={{
              width: 56, height: 56, marginTop: -16,
              borderRadius: 18, background: c.primary,
              border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: c.onPrimary, cursor: 'pointer',
              boxShadow: `0 8px 20px ${c === SW_DARK ? 'rgba(123,196,164,0.25)' : 'rgba(15,76,58,0.25)'}`,
            }}>
              <Icon name="plus" size={26} strokeWidth={2.2} />
            </button>
          );
        }
        const on = active === t.id;
        return (
          <button key={t.id} onClick={() => onChange(t.id)} className="sw-press" style={{
            flex: 1, padding: '4px 0 2px',
            background: 'transparent', border: 'none',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
            color: on ? c.primary : c.inkSubtle, cursor: 'pointer',
          }}>
            {on ? <IconFilled name={t.icon} size={24} /> : <Icon name={t.icon} size={24} strokeWidth={1.8} />}
            <span style={{ fontSize: 10, fontWeight: on ? 700 : 500, letterSpacing: '0.01em' }}>{t.label}</span>
          </button>
        );
      })}
    </div>
  );
};

// ─── Progress bar ────────────────────────────────────────────

const SW_Bar = ({ c, used, plan, color, height = 8, animate = true }) => {
  const pct = plan > 0 ? Math.min((used / plan) * 100, 100) : 0;
  const over = plan > 0 && used > plan;
  const overflowPct = over ? Math.min(((used - plan) / plan) * 100, 30) : 0;
  return (
    <div style={{
      width: '100%', height,
      background: c === SW_DARK ? '#2A332E' : '#EDE5CF',
      borderRadius: height,
      overflow: 'hidden',
      display: 'flex',
    }}>
      <div className={animate ? 'sw-fill' : ''} style={{ width: `${pct}%`, background: over ? c.danger : (color || c.primary), height: '100%', transition: 'width 400ms cubic-bezier(.2,.7,.3,1)' }} />
      {over && (
        <div className={animate ? 'sw-fill' : ''} style={{ width: `${overflowPct}%`, background: c.danger, height: '100%', opacity: 0.7, marginLeft: 1 }} />
      )}
    </div>
  );
};

// ─── Card ─────────────────────────────────────────────────────

const SW_Card = ({ c, children, padding = 16, onClick, style, noBorder = false }) => (
  <div onClick={onClick} className={onClick ? 'sw-press' : ''} style={{
    background: c.surface,
    border: noBorder ? 'none' : `1px solid ${c.border}`,
    borderRadius: 18,
    padding,
    cursor: onClick ? 'pointer' : 'default',
    ...style,
  }}>{children}</div>
);

const SW_SectionLabel = ({ c, children, right, style }) => (
  <div style={{
    display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 8,
    margin: '4px 4px 8px', ...style,
  }}>
    <span style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', minWidth: 0, flex: 1 }}>{children}</span>
    {right}
  </div>
);

// ─── Button ──────────────────────────────────────────────────

const SW_Button = ({ c, children, onClick, variant = 'primary', size = 'md', icon, full = true, disabled }) => {
  const sizes = {
    sm: { h: 36, px: 14, fs: 13, gap: 6, ico: 16 },
    md: { h: 48, px: 18, fs: 15, gap: 8, ico: 18 },
    lg: { h: 56, px: 22, fs: 16, gap: 10, ico: 20 },
  }[size];
  const variants = {
    primary:    { bg: c.primary, fg: c.onPrimary, br: 'transparent' },
    secondary:  { bg: c.primaryContainer, fg: c.onPrimaryContainer, br: 'transparent' },
    outline:    { bg: 'transparent', fg: c.ink, br: c.borderStrong },
    ghost:      { bg: 'transparent', fg: c.primary, br: 'transparent' },
    danger:     { bg: c.danger, fg: '#fff', br: 'transparent' },
  }[variant];
  return (
    <button onClick={onClick} disabled={disabled} className="sw-press" style={{
      width: full ? '100%' : 'auto',
      height: sizes.h, padding: `0 ${sizes.px}px`,
      background: variants.bg, color: variants.fg,
      border: `1.5px solid ${variants.br}`,
      borderRadius: 14,
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: sizes.gap,
      fontFamily: SW_TYPE.family, fontSize: sizes.fs, fontWeight: 600, letterSpacing: '-0.005em',
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.5 : 1,
    }}>
      {icon && <Icon name={icon} size={sizes.ico} strokeWidth={2} />}
      {children}
    </button>
  );
};

// ─── Field (input) ───────────────────────────────────────────

const SW_Field = ({ c, label, value, onChange, placeholder, prefix, suffix, type = 'text', readOnly, onClick, hint, error }) => (
  <div style={{ marginBottom: 14 }}>
    {label && <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 6, letterSpacing: '0.01em' }}>{label}</div>}
    <div onClick={onClick} style={{
      display: 'flex', alignItems: 'center',
      background: c.surface,
      border: `1.5px solid ${error ? c.danger : c.border}`,
      borderRadius: 12, height: 52, padding: '0 14px',
      cursor: readOnly || onClick ? 'pointer' : 'text',
      transition: 'border-color 120ms ease',
    }}>
      {prefix && <span style={{ color: c.inkMuted, marginRight: 8, fontSize: 15, fontWeight: 600 }}>{prefix}</span>}
      <input
        type={type} value={value || ''} onChange={onChange ? (e) => onChange(e.target.value) : undefined}
        placeholder={placeholder} readOnly={readOnly}
        style={{
          flex: 1, minWidth: 0,
          border: 'none', outline: 'none', background: 'transparent',
          fontFamily: SW_TYPE.family, fontSize: 16, fontWeight: 500, color: c.ink,
          fontVariantNumeric: type === 'number' || prefix === 'Rp' ? 'tabular-nums' : 'normal',
        }}
      />
      {suffix && <span style={{ color: c.inkMuted, marginLeft: 8, fontSize: 14 }}>{suffix}</span>}
    </div>
    {hint && <div style={{ fontSize: 11, color: error ? c.danger : c.inkSubtle, marginTop: 4, marginLeft: 4 }}>{hint}</div>}
  </div>
);

// ─── BottomSheet ─────────────────────────────────────────────

const SW_Sheet = ({ c, open, onClose, title, children, maxHeight = '78%' }) => {
  if (!open) return null;
  return (
    <div style={{
      position: 'absolute', inset: 0, zIndex: 10,
      display: 'flex', flexDirection: 'column', justifyContent: 'flex-end',
    }}>
      <div onClick={onClose} style={{
        position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.5)',
        animation: 'sw-sheet-bg 200ms ease',
      }} />
      <div style={{
        position: 'relative',
        background: c.surface,
        borderRadius: '24px 24px 0 0',
        padding: '8px 0 24px',
        maxHeight,
        display: 'flex', flexDirection: 'column',
        animation: 'sw-sheet-in 280ms cubic-bezier(.2,.7,.3,1)',
        boxShadow: '0 -8px 30px rgba(0,0,0,0.18)',
      }}>
        <div style={{ display: 'flex', justifyContent: 'center', padding: '6px 0 4px' }}>
          <div style={{ width: 44, height: 4, background: c.borderStrong, borderRadius: 2 }} />
        </div>
        {title && (
          <div style={{ padding: '8px 20px 14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
            <div style={{ fontSize: 18, fontWeight: 700, color: c.ink, letterSpacing: '-0.01em', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', flex: 1, minWidth: 0 }}>{title}</div>
            <button onClick={onClose} className="sw-press" style={{ width: 36, height: 36, borderRadius: 10, border: 'none', background: 'transparent', color: c.inkMuted, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
              <Icon name="close" size={20} />
            </button>
          </div>
        )}
        <div className="sw-scroll" style={{ overflowY: 'auto', padding: '0 20px 8px' }}>{children}</div>
      </div>
    </div>
  );
};

// ─── ChipRow (horiz scroll chips) ────────────────────────────

const SW_Chip = ({ c, label, active, onClick, count }) => (
  <button onClick={onClick} className="sw-press" style={{
    flex: '0 0 auto', height: 36, padding: '0 14px',
    background: active ? c.primary : c.surface,
    color: active ? c.onPrimary : c.ink,
    border: `1px solid ${active ? c.primary : c.border}`,
    borderRadius: 18,
    fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 600,
    cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 6,
  }}>
    {label}
    {count != null && (
      <span style={{ fontSize: 11, opacity: 0.75, fontVariantNumeric: 'tabular-nums' }}>{count}</span>
    )}
  </button>
);

// ─── AmountText (big tabular numbers) ────────────────────────

const SW_Amount = ({ c, value, size = 28, weight = 700, color, prefix = 'Rp', sign }) => {
  const formatted = new Intl.NumberFormat('id-ID').format(Math.abs(Math.round(value)));
  return (
    <span style={{
      fontFamily: SW_TYPE.family, fontSize: size, fontWeight: weight,
      fontVariantNumeric: 'tabular-nums', color: color || c.ink,
      letterSpacing: '-0.02em', lineHeight: 1,
    }}>
      {sign === '+' && <span style={{ opacity: 0.7 }}>+ </span>}
      {sign === '-' && <span style={{ opacity: 0.7 }}>− </span>}
      <span style={{ fontSize: size * 0.62, fontWeight: 500, opacity: 0.78, marginRight: size * 0.12 }}>{prefix}</span>
      {formatted}
    </span>
  );
};

// ─── Account icon ─────────────────────────────────────────────

const SW_AccountIcon = ({ c, account, size = 40 }) => {
  const bg = c === SW_DARK ? c.primaryContainer : c.primaryContainer;
  return (
    <div style={{
      width: size, height: size,
      background: bg, color: c.onPrimaryContainer,
      borderRadius: size * 0.3,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      flex: '0 0 auto',
    }}>
      <Icon name={account.icon} size={size * 0.5} strokeWidth={1.8} />
    </div>
  );
};

// ─── Category dot (small colored circle with first letter) ────

const SW_CategoryDot = ({ c, name, size = 36, color }) => {
  const letter = (name || '?').charAt(0).toUpperCase();
  const palette = [c.primary, c.accent, c.info, c.warning, c.success];
  const idx = (name || '').split('').reduce((s, ch) => s + ch.charCodeAt(0), 0) % palette.length;
  const fill = color || palette[idx];
  return (
    <div style={{
      width: size, height: size,
      background: fill + '20', color: fill,
      borderRadius: size * 0.3,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontSize: size * 0.42, fontWeight: 700, letterSpacing: '-0.01em',
      flex: '0 0 auto',
    }}>
      {letter}
    </div>
  );
};

Object.assign(window, {
  SW_PhoneFrame, SW_StatusBar, SW_NavHandle, SW_TopBar, SW_TabBar,
  SW_Bar, SW_Card, SW_SectionLabel, SW_Button, SW_Field, SW_Sheet, SW_Chip,
  SW_Amount, SW_AccountIcon, SW_CategoryDot,
});
