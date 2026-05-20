// Sakuwise — brand applied in context. Phone-frame dashboard preview to
// show how the visual system feels in a real screen.

const fmt = (n) => 'Rp ' + new Intl.NumberFormat('id-ID').format(n);

// Tiny chevron-right
const ChevR = ({ c, size = 16 }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none">
    <path d="M9 6 L15 12 L9 18" stroke={c} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const PhoneFrame = ({ children, theme }) => (
  <div style={{
    width: 360, height: 720,
    background: theme.bg,
    borderRadius: 36,
    boxShadow: theme === SW_DARK
      ? '0 24px 60px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.04)'
      : '0 24px 60px rgba(15, 76, 58, 0.14), 0 0 0 1px rgba(0,0,0,0.05)',
    overflow: 'hidden',
    position: 'relative',
    fontFamily: SW_TYPE.family,
    color: theme.ink,
  }}>
    {children}
  </div>
);

const StatusBar = ({ theme }) => (
  <div style={{
    height: 36, padding: '0 22px',
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    fontSize: 13, fontWeight: 600, color: theme.ink,
  }}>
    <span>9:41</span>
    <div style={{ display: 'flex', gap: 6, alignItems: 'center', fontSize: 11 }}>
      <span style={{ letterSpacing: '0.04em' }}>5G</span>
      <span>􀛬</span>
      <span style={{
        display: 'inline-block', width: 22, height: 10, border: `1.2px solid ${theme.ink}`, borderRadius: 2,
        position: 'relative',
      }}>
        <span style={{ position: 'absolute', inset: 1, background: theme.ink, width: '70%', borderRadius: 1 }} />
      </span>
    </div>
  </div>
);

const ProgressBar = ({ pct, color, bg, height = 8, overflow = 0 }) => (
  <div style={{ width: '100%', height, background: bg, borderRadius: height, overflow: 'hidden', position: 'relative', display: 'flex' }}>
    <div style={{ width: `${Math.min(pct, 100)}%`, background: color, height: '100%' }} />
    {overflow > 0 && <div style={{ width: `${Math.min(overflow, 30)}%`, background: 'currentColor', height: '100%', opacity: 0.9 }} />}
  </div>
);

const AllocRow = ({ theme, label, used, plan, color }) => {
  const pct = (used / plan) * 100;
  const over = pct > 100;
  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 6 }}>
        <span style={{ fontSize: 13, fontWeight: 600, color: theme.ink }}>{label}</span>
        <span style={{ fontSize: 12, color: theme.inkMuted, fontVariantNumeric: 'tabular-nums' }}>
          <span style={{ color: over ? theme.danger : theme.ink, fontWeight: 600 }}>{fmt(used)}</span>
          <span style={{ color: theme.inkSubtle }}> / {fmt(plan)}</span>
        </span>
      </div>
      <div style={{ color: over ? theme.danger : color }}>
        <ProgressBar pct={pct} color={over ? theme.danger : color} bg={theme === SW_DARK ? '#2A332E' : '#EDE5CF'} />
      </div>
    </div>
  );
};

const TxnRow = ({ theme, icon, iconBg, label, sub, amount, neg = true }) => (
  <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0' }}>
    <div style={{
      width: 36, height: 36, borderRadius: 10, background: iconBg,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontSize: 16, fontWeight: 700, color: theme.primary,
    }}>{icon}</div>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 14, fontWeight: 500, color: theme.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{label}</div>
      <div style={{ fontSize: 11, color: theme.inkSubtle }}>{sub}</div>
    </div>
    <div style={{ fontSize: 14, fontWeight: 600, color: neg ? theme.ink : theme.success, fontVariantNumeric: 'tabular-nums' }}>
      {neg ? '−' : '+'} {fmt(amount)}
    </div>
  </div>
);

const DashboardPreview = ({ theme, variant = 'A' }) => {
  const c = theme;
  return (
    <PhoneFrame theme={c}>
      <StatusBar theme={c} />
      {/* Top bar with mini-mark */}
      <div style={{ padding: '6px 20px 12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Lockup theme={c} variant={variant} size={26} />
        <div style={{
          width: 36, height: 36, borderRadius: '50%', background: c.primaryContainer,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: c.onPrimaryContainer, fontWeight: 700, fontSize: 13,
        }}>G</div>
      </div>

      {/* Scroll body */}
      <div style={{ padding: '0 20px 20px', overflow: 'auto', height: 'calc(100% - 88px)' }}>
        {/* Greeting */}
        <div style={{ marginBottom: 8 }}>
          <div style={{ fontSize: 13, color: c.inkMuted, marginBottom: 2 }}>Selamat pagi,</div>
          <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: '-0.01em' }}>Gusti</div>
        </div>
        <div style={{ fontSize: 12, color: c.inkSubtle, marginBottom: 16, display: 'inline-flex', alignItems: 'center', gap: 6 }}>
          <span style={{ width: 6, height: 6, borderRadius: '50%', background: c.accent }} />
          Plan Mei 2026 · sisa 16 hari
        </div>

        {/* Hero summary */}
        <div style={{
          background: c.primary, color: c.onPrimary,
          borderRadius: 20, padding: '18px 18px 16px',
          marginBottom: 14,
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{
            position: 'absolute', right: -30, bottom: -30, opacity: 0.12,
          }}>
            {React.createElement(LOGO_MAP[variant], { theme: { ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }, size: 140 })}
          </div>
          <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: '0.08em', opacity: 0.85, textTransform: 'uppercase', marginBottom: 4 }}>Sisa Anggaran</div>
          <div style={{ fontSize: 32, fontWeight: 800, letterSpacing: '-0.025em', fontVariantNumeric: 'tabular-nums', lineHeight: 1.1 }}>{fmt(4250000)}</div>
          <div style={{ fontSize: 12, opacity: 0.78, marginTop: 4 }}>≈ Rp 265.625/hari sampai akhir periode</div>
        </div>

        {/* Allocation card */}
        <div style={{
          background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 16, padding: 16, marginBottom: 14,
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 14 }}>
            <span style={{ fontSize: 15, fontWeight: 600 }}>Alokasi</span>
            <span style={{ fontSize: 11, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>50 · 30 · 20</span>
          </div>
          <AllocRow theme={c} label="Needs" used={3200000} plan={5000000} color={c.primary} />
          <AllocRow theme={c} label="Wants" used={2100000} plan={3000000} color={c.accent} />
          <AllocRow theme={c} label="Investment" used={1800000} plan={2000000} color={c.info} />
        </div>

        {/* Account chips */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 14, overflowX: 'auto' }}>
          {[
            { name: 'Mandiri', bal: 12450000, t: 'Bank' },
            { name: 'GoPay', bal: 280000, t: 'E-Wallet' },
            { name: 'Tunai', bal: 540000, t: 'Tunai' },
          ].map(a => (
            <div key={a.name} style={{
              flex: '0 0 auto', padding: '10px 14px', borderRadius: 12,
              background: c.surface, border: `1px solid ${c.border}`,
              minWidth: 110,
            }}>
              <div style={{ fontSize: 10, color: c.inkSubtle, marginBottom: 2, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{a.t}</div>
              <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 2 }}>{a.name}</div>
              <div style={{ fontSize: 13, fontWeight: 600, color: c.primary, fontVariantNumeric: 'tabular-nums' }}>{fmt(a.bal)}</div>
            </div>
          ))}
        </div>

        {/* Recent transactions */}
        <div style={{
          background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 16, padding: '6px 16px 8px',
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0 4px' }}>
            <span style={{ fontSize: 15, fontWeight: 600 }}>Transaksi Terbaru</span>
            <span style={{ fontSize: 12, color: c.primary, fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 2 }}>
              Semua <ChevR c={c.primary} size={14} />
            </span>
          </div>
          <TxnRow theme={c} icon="K" iconBg={c.primaryContainer} label="Kopi Kenangan" sub="Hari ini · Wants · GoPay" amount={28000} />
          <TxnRow theme={c} icon="L" iconBg={c.primaryContainer} label="Listrik PLN" sub="Kemarin · Needs · Mandiri" amount={485000} />
          <TxnRow theme={c} icon="G" iconBg={c.primaryContainer} label="Gaji Mei" sub="14 Mei · Pemasukan" amount={15000000} neg={false} />
        </div>

        <div style={{ height: 12 }} />
      </div>
    </PhoneFrame>
  );
};

const InContextArtboard = ({ theme, variant = 'A', width = 440, height = 800 }) => {
  const c = theme;
  return (
    <div style={{
      width, height, padding: 30, background: c === SW_DARK ? '#0a0c0b' : '#ECE6D5',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: SW_TYPE.family,
    }}>
      <DashboardPreview theme={c} variant={variant} />
    </div>
  );
};

Object.assign(window, { DashboardPreview, InContextArtboard });
