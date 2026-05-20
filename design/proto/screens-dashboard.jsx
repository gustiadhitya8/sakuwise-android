// Sakuwise — Dashboard screen
// All 9 tiles per PRD §7.6, in order, with subtle animations.

const Dashboard_Greeting = ({ c, name = 'Gusti' }) => {
  const hour = 9;
  const greet = hour < 11 ? 'Selamat pagi' : hour < 15 ? 'Selamat siang' : hour < 19 ? 'Selamat sore' : 'Selamat malam';
  return (
    <div style={{ padding: '0 20px', marginBottom: 14 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, marginBottom: 4 }}>
        <div style={{ display: 'flex', flexDirection: 'column', minWidth: 0, flex: 1 }}>
          <span style={{ fontSize: 13, color: c.inkMuted, fontWeight: 500, lineHeight: 1.3 }}>{greet},</span>
          <span style={{ fontSize: 24, fontWeight: 700, color: c.ink, letterSpacing: '-0.02em', lineHeight: 1.2 }}>{name}</span>
        </div>
        <button className="sw-press" aria-label="Notifikasi" style={{
          width: 44, height: 44, borderRadius: 14,
          background: c.surface, border: `1px solid ${c.border}`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: c.ink, cursor: 'pointer', position: 'relative',
        }}>
          <Icon name="bell" size={20} />
          <span style={{ position: 'absolute', top: 10, right: 10, width: 8, height: 8, background: c.danger, borderRadius: '50%', border: `2px solid ${c.surface}` }} />
        </button>
      </div>
      <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8, marginTop: 8,
        padding: '6px 12px', background: c.primaryContainer, color: c.onPrimaryContainer,
        borderRadius: 99, fontSize: 12, fontWeight: 600, whiteSpace: 'nowrap',
      }}>
        <Icon name="calendar" size={14} strokeWidth={2} />
        <span>Plan Mei · sisa {SW_PERIOD.daysLeft} hari</span>
      </div>
    </div>
  );
};

// — Hero summary tile: Pemasukan / Pengeluaran / Sisa with daily remaining
const Dashboard_Hero = ({ c, variant = 'A', hide = false, onToggleHide }) => {
  const totalExpense = SW_ALLOCATIONS.reduce((s, a) => s + a.used, 0);
  const remaining = SW_INCOME_MONTH - totalExpense;
  const dailyLeft = remaining / SW_PERIOD.daysLeft;
  const masked = '••••••••';
  return (
    <div style={{
      margin: '0 20px 14px',
      background: c.primary, color: c.onPrimary,
      borderRadius: 22, padding: '20px 22px 18px',
      position: 'relative', overflow: 'hidden',
    }}>
      <div style={{ position: 'absolute', right: -36, bottom: -36, opacity: 0.10 }}>
        <LogoA_Daun theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={180} />
      </div>

      <div style={{ position: 'relative', display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12, marginBottom: 10 }}>
        <div style={{ minWidth: 0, flex: 1 }}>
          <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase', marginBottom: 2 }}>Sisa Anggaran</div>
          {hide
            ? <div style={{ fontFamily: SW_TYPE.family, fontSize: 36, fontWeight: 800, color: c.onPrimary, letterSpacing: '0.04em', lineHeight: 1, opacity: 0.85 }}>{masked}</div>
            : <SW_Amount c={c} value={remaining} size={36} weight={800} color={c.onPrimary} />
          }
        </div>
        <button onClick={onToggleHide} className="sw-press" aria-label={hide ? 'Tampilkan saldo' : 'Sembunyikan saldo'} style={{
          width: 38, height: 38, borderRadius: 12, flex: '0 0 auto',
          background: 'rgba(255,255,255,0.12)', color: c.onPrimary, border: 'none',
          display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
          opacity: 0.9,
        }}>
          <Icon name={hide ? 'eye_off' : 'eye'} size={18} strokeWidth={1.8} />
        </button>
      </div>

      <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 16, padding: '12px 14px', background: 'rgba(255,255,255,0.10)', borderRadius: 12 }}>
        <div style={{
          width: 36, height: 36, borderRadius: 12,
          background: 'rgba(255,255,255,0.15)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon name="sparkle" size={20} color={c.accent} strokeWidth={1.6} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 12, opacity: 0.78, marginBottom: 2 }}>Anggaran harian</div>
          <div style={{ fontSize: 16, fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>
            {hide ? '•••••' : SW_FORMAT.rp(dailyLeft)}<span style={{ opacity: 0.6, fontWeight: 500 }}> / hari</span>
          </div>
        </div>
      </div>

      <div style={{ position: 'relative', display: 'flex', justifyContent: 'space-between', marginTop: 14, paddingTop: 12, borderTop: '1px solid rgba(255,255,255,0.15)' }}>
        <Dashboard_MetricSmall c={c} label="Pemasukan" value={SW_INCOME_MONTH} sign="+" tint={c.accent} hide={hide} />
        <div style={{ width: 1, background: 'rgba(255,255,255,0.18)' }} />
        <Dashboard_MetricSmall c={c} label="Pengeluaran" value={totalExpense} sign="−" hide={hide} />
      </div>
    </div>
  );
};

const Dashboard_MetricSmall = ({ c, label, value, sign, tint, hide }) => (
  <div style={{ flex: 1, padding: '0 4px' }}>
    <div style={{ fontSize: 11, opacity: 0.7, fontWeight: 500, marginBottom: 2 }}>{label}</div>
    <div style={{ fontSize: 17, fontWeight: 700, fontVariantNumeric: 'tabular-nums', color: tint || c.onPrimary, letterSpacing: '-0.01em' }}>
      <span style={{ opacity: 0.7, marginRight: 2 }}>{sign}</span>
      {hide ? '•••' : SW_FORMAT.rpShort(value)}
    </div>
  </div>
);

// — Allocation progress (Needs / Wants / Investment)
const Dashboard_Alloc = ({ c, onTap }) => (
  <div style={{ padding: '0 20px', marginBottom: 14 }}>
    <SW_SectionLabel c={c} right={
      <button onClick={onTap} className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 2, cursor: 'pointer', whiteSpace: 'nowrap' }}>
        Detail <Icon name="chevron_right" size={14} strokeWidth={2.2} />
      </button>
    }>Alokasi</SW_SectionLabel>
    <SW_Card c={c}>
      {SW_ALLOCATIONS.map((a, i) => {
        const pct = (a.used / a.plan) * 100;
        const over = a.used > a.plan;
        const allocColor = { needs: c.primary, wants: c.accent, invest: c.info }[a.id];
        return (
          <div key={a.id} style={{ marginTop: i ? 14 : 0 }}>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 6 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ width: 8, height: 8, borderRadius: '50%', background: allocColor }} />
                <span style={{ fontSize: 14, fontWeight: 600, color: c.ink }}>{a.name}</span>
                <span style={{ fontSize: 11, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>{a.target}%</span>
              </div>
              <div style={{ fontSize: 12, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap', flex: '0 0 auto' }}>
                <span style={{ color: over ? c.danger : c.ink, fontWeight: 700 }}>{SW_FORMAT.rpShort(a.used)}</span>
                <span style={{ color: c.inkSubtle }}> / {SW_FORMAT.rpShort(a.plan)}</span>
              </div>
            </div>
            <SW_Bar c={c} used={a.used} plan={a.plan} color={allocColor} />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
              <span style={{ fontSize: 10, color: c.inkSubtle, fontVariantNumeric: 'tabular-nums' }}>{Math.round(pct)}% terpakai</span>
              {over && (
                <span style={{ fontSize: 10, color: c.danger, fontWeight: 600 }}>
                  Over {SW_FORMAT.rpShort(a.used - a.plan)}
                </span>
              )}
            </div>
          </div>
        );
      })}
    </SW_Card>
  </div>
);

// — Top 5 spending (compact bar)
const Dashboard_TopSpend = ({ c }) => {
  const max = Math.max(...SW_TOP_CATEGORIES.map(t => t.amount));
  return (
    <div style={{ padding: '0 20px', marginBottom: 14 }}>
      <SW_SectionLabel c={c}>Pengeluaran Teratas</SW_SectionLabel>
      <SW_Card c={c} padding={14}>
        {SW_TOP_CATEGORIES.map((t, i) => {
          const pct = (t.amount / max) * 100;
          const tone = c[t.color] || c.primary;
          return (
            <div key={t.name} style={{ marginTop: i ? 10 : 0 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                <span style={{ fontSize: 13, fontWeight: 500, color: c.ink, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', flex: 1 }}>{t.name}</span>
                <span style={{ fontSize: 12, color: c.ink, fontWeight: 600, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap', flex: '0 0 auto' }}>{SW_FORMAT.rpShort(t.amount)}</span>
              </div>
              <div className="sw-fill" style={{ height: 6, background: c === SW_DARK ? '#2A332E' : '#EDE5CF', borderRadius: 6, overflow: 'hidden' }}>
                <div style={{ width: `${pct}%`, height: '100%', background: tone, borderRadius: 6, transition: 'width 400ms cubic-bezier(.2,.7,.3,1)' }} />
              </div>
            </div>
          );
        })}
      </SW_Card>
    </div>
  );
};

// — Accounts horizontal scroller
const Dashboard_Accounts = ({ c, onTapAccount }) => {
  const total = SW_ACCOUNTS.reduce((s, a) => s + a.balance, 0);
  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{ padding: '0 20px' }}>
        <SW_SectionLabel c={c} right={
          <span style={{ fontSize: 13, fontWeight: 700, color: c.ink, fontVariantNumeric: 'tabular-nums' }}>
            {SW_FORMAT.rp(total)}
          </span>
        }>Akun</SW_SectionLabel>
      </div>
      <div className="sw-scroll" style={{ overflowX: 'auto', padding: '0 20px 4px', display: 'flex', gap: 10 }}>
        {SW_ACCOUNTS.map(a => (
          <div key={a.id} onClick={() => onTapAccount && onTapAccount(a)} className="sw-press" style={{
            flex: '0 0 auto', minWidth: 152,
            background: c.surface, border: `1px solid ${c.border}`,
            borderRadius: 16, padding: 14, cursor: 'pointer',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
              <SW_AccountIcon c={c} account={a} size={36} />
              <span style={{ fontSize: 10, color: c.inkSubtle, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{a.type}</span>
            </div>
            <div style={{ fontSize: 13, fontWeight: 600, color: c.ink, marginBottom: 2 }}>{a.name}</div>
            <div style={{ fontSize: 16, fontWeight: 700, color: c.primary, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em' }}>
              {SW_FORMAT.rpShort(a.balance)}
            </div>
          </div>
        ))}
        <button className="sw-press" style={{
          flex: '0 0 auto', minWidth: 60,
          background: 'transparent', border: `1.5px dashed ${c.borderStrong}`,
          borderRadius: 16, cursor: 'pointer', color: c.inkMuted,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon name="plus" size={20} />
        </button>
      </div>
    </div>
  );
};

// — Net worth tile
const Dashboard_NetWorth = ({ c }) => {
  const acctTotal = SW_ACCOUNTS.reduce((s, a) => s + a.balance, 0);
  const slices = [
    { name: 'Akun', value: acctTotal, color: c.primary },
    { name: 'Emas', value: SW_INVEST.gold.value, color: c.warning },
    { name: 'Properti', value: SW_INVEST.land.value, color: c.info },
    { name: 'Deposito', value: SW_INVEST.deposit.value, color: c.accent },
    { name: 'Hutang', value: -SW_DEBT.iOwe, color: c.danger },
  ];
  const totalPos = slices.filter(s => s.value > 0).reduce((s, x) => s + x.value, 0);
  return (
    <div style={{ padding: '0 20px', marginBottom: 14 }}>
      <SW_SectionLabel c={c}>Total Kekayaan</SW_SectionLabel>
      <SW_Card c={c} padding={18}>
        <div style={{ marginBottom: 14 }}>
          <SW_Amount c={c} value={SW_NETWORTH} size={32} weight={800} />
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: 4, marginLeft: 8, padding: '3px 8px', background: c.successSoft, color: c.success, borderRadius: 8, fontSize: 11, fontWeight: 700 }}>
            <Icon name="arrow_up_right" size={11} strokeWidth={2.5} />
            +12.4% YTD
          </div>
        </div>

        {/* Stacked bar */}
        <div style={{ display: 'flex', height: 10, borderRadius: 5, overflow: 'hidden', marginBottom: 12, background: c === SW_DARK ? '#2A332E' : '#EDE5CF' }}>
          {slices.filter(s => s.value > 0).map(s => (
            <div key={s.name} style={{ width: `${(s.value / totalPos) * 100}%`, background: s.color }} />
          ))}
        </div>

        {/* Legend */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '6px 14px' }}>
          {slices.map(s => (
            <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 11, minWidth: 0 }}>
              <span style={{ width: 8, height: 8, borderRadius: 2, background: s.color, flex: '0 0 auto' }} />
              <span style={{ color: c.inkMuted, flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{s.name}</span>
              <span style={{ color: c.ink, fontWeight: 600, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap', flex: '0 0 auto' }}>
                {s.value < 0 ? '−' : ''}{SW_FORMAT.rpShort(Math.abs(s.value))}
              </span>
            </div>
          ))}
        </div>
      </SW_Card>
    </div>
  );
};

// — Recent transactions
const Dashboard_RecentTxns = ({ c, onTapAll }) => (
  <div style={{ padding: '0 20px', marginBottom: 14 }}>
    <SW_SectionLabel c={c} right={
      <button onClick={onTapAll} className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 2, cursor: 'pointer' }}>
        Semua <Icon name="chevron_right" size={14} strokeWidth={2.2} />
      </button>
    }>Transaksi Terbaru</SW_SectionLabel>
    <SW_Card c={c} padding={0}>
      {SW_TRANSACTIONS.slice(0, 6).map((t, i, arr) => (
        <TxnItem key={t.id} c={c} txn={t} divider={i < arr.length - 1} />
      ))}
    </SW_Card>
  </div>
);

const TxnItem = ({ c, txn, divider }) => {
  const negative = txn.type === 'expense';
  const positive = txn.type === 'income';
  const transfer = txn.type === 'transfer';
  const account = SW_ACCOUNTS.find(a => a.id === txn.account);
  const iconName = positive ? 'income' : transfer ? 'transfer' : 'expense';
  const tone = positive ? c.success : transfer ? c.info : c.ink;
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '12px 16px',
      borderBottom: divider ? `1px solid ${c.border}` : 'none',
    }}>
      <SW_CategoryDot c={c} name={txn.cat} size={38} color={tone === c.ink ? undefined : tone} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 14, fontWeight: 600, color: c.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {txn.merchant || txn.cat}
        </div>
        <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 1, display: 'flex', alignItems: 'center', gap: 6 }}>
          <span>{SW_FORMAT.dateRel(txn.date)}</span>
          <span style={{ width: 2, height: 2, background: c.inkSubtle, borderRadius: 1 }} />
          <span>{account?.name || txn.cat}</span>
        </div>
      </div>
      <div style={{ fontSize: 14, fontWeight: 700, color: tone, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em' }}>
        {negative ? '−' : positive ? '+' : ''} {SW_FORMAT.rpShort(txn.amount)}
      </div>
    </div>
  );
};

// — Backup banner
const Dashboard_Banner = ({ c }) => (
  <div style={{ padding: '0 20px 20px' }}>
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '14px 16px',
      background: c.warningSoft,
      border: `1px solid ${c.warning}33`,
      borderRadius: 14,
    }}>
      <div style={{
        width: 36, height: 36, borderRadius: 10,
        background: c.warning, color: '#fff',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        flex: '0 0 auto',
      }}>
        <Icon name="shield" size={18} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, fontWeight: 700, color: c.ink, marginBottom: 1 }}>Backup tertunda 34 hari</div>
        <div style={{ fontSize: 11, color: c.inkMuted }}>Amankan data uangmu — backup sekarang.</div>
      </div>
      <button className="sw-press" style={{
        height: 32, padding: '0 12px',
        background: c.warning, color: '#fff', border: 'none',
        borderRadius: 10, fontFamily: SW_TYPE.family, fontSize: 12, fontWeight: 700, cursor: 'pointer',
      }}>Backup</button>
    </div>
  </div>
);

// — Mini link to Aset tab (replaces detail-rich Account scroller + Net Worth)
const Dashboard_AssetsLink = ({ c, onTap, hide }) => {
  const acctCount = SW_ACCOUNTS.length;
  const acctTotal = SW_ACCOUNTS.reduce((s, a) => s + a.balance, 0);
  return (
    <div style={{ padding: '0 20px', marginBottom: 14 }}>
      <SW_Card c={c} padding={0}>
        <button onClick={onTap} className="sw-press" style={{
          width: '100%', textAlign: 'left',
          display: 'flex', alignItems: 'center', gap: 14,
          padding: '14px 16px',
          background: 'transparent', border: 'none', cursor: 'pointer',
          fontFamily: SW_TYPE.family,
        }}>
          <div style={{
            width: 40, height: 40, borderRadius: 12,
            background: c.primaryContainer, color: c.onPrimaryContainer,
            display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
          }}>
            <Icon name="assets" size={20} strokeWidth={1.8} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>Aset &amp; Kekayaan</div>
            <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>
              {acctCount} akun aktif{hide ? '' : ` · ${SW_FORMAT.rpShort(acctTotal)} di akun`} · detail di tab Aset
            </div>
          </div>
          <Icon name="chevron_right" size={20} color={c.inkSubtle} />
        </button>
      </SW_Card>
    </div>
  );
};

// — Dashboard composer
const DashboardScreen = ({ c, onNav }) => {
  const [hide, setHide] = React.useState(false);
  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <div style={{ padding: '8px 0 12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingLeft: 20, paddingRight: 20 }}>
        <Lockup theme={c} variant="A" size={26} />
        <button className="sw-press" onClick={() => onNav('me')} style={{ width: 36, height: 36, borderRadius: '50%', background: c.primaryContainer, color: c.onPrimaryContainer, border: 'none', cursor: 'pointer', fontFamily: SW_TYPE.family, fontSize: 14, fontWeight: 700 }}>G</button>
      </div>
      <Dashboard_Greeting c={c} />
      <Dashboard_Hero c={c} hide={hide} onToggleHide={() => setHide(h => !h)} />
      <Dashboard_Alloc c={c} onTap={() => onNav('plan')} />
      <Dashboard_TopSpend c={c} />
      <Dashboard_AssetsLink c={c} onTap={() => onNav('assets')} hide={hide} />
      <Dashboard_RecentTxns c={c} onTapAll={() => onNav('plan')} />
      <Dashboard_Banner c={c} />
    </div>
  );
};

Object.assign(window, { DashboardScreen, TxnItem });
