// Sakuwise — Aset hub + Accounts/Emas/Tanah/Deposito list & detail

// ─── Aset Hub (overview) ────────────────────────────────────

const AssetsHubScreen = ({ c, onNav, sub, setSub }) => {
  const [hide, setHide] = React.useState(false);
  // Compute totals
  const acctTotal = SW_ACCOUNTS.reduce((s, a) => s + a.balance, 0);
  const goldTotal = SW_GOLD.filter(g => g.status === 'held').reduce((s, g) => s + g.weight * SW_GOLD_PRICE, 0);
  const landTotal = SW_LAND.filter(l => l.status === 'held').reduce((s, l) => s + (l.currentValue || l.buyPrice), 0);
  const depTotal  = SW_DEPOSIT.reduce((s, d) => {
    const last = d.snapshots[d.snapshots.length - 1];
    return s + (last ? last.balance : 0);
  }, 0);
  const debtTotal = SW_DEBTS.filter(d => d.direction === 'i_owe' && d.status === 'open').reduce((s, d) => s + (d.principal - d.payments.reduce((x, p) => x + p.amount, 0)), 0);
  const netWorth = acctTotal + goldTotal + landTotal + depTotal - debtTotal;

  if (sub) {
    if (sub.kind === 'accounts')  return <AccountsListScreen c={c} onBack={() => setSub(null)} onOpen={(id) => setSub({ kind: 'account', id })} />;
    if (sub.kind === 'account')   return <AccountDetailScreen c={c} account={SW_ACCOUNTS.find(a => a.id === sub.id)} onBack={() => setSub({ kind: 'accounts' })} />;
    if (sub.kind === 'emas-list') return <EmasListScreen c={c} onBack={() => setSub(null)} onOpen={(id) => setSub({ kind: 'emas', id })} />;
    if (sub.kind === 'emas')      return <EmasDetailScreen c={c} item={SW_GOLD.find(g => g.id === sub.id)} onBack={() => setSub({ kind: 'emas-list' })} />;
    if (sub.kind === 'land-list') return <LandListScreen c={c} onBack={() => setSub(null)} onOpen={(id) => setSub({ kind: 'land', id })} />;
    if (sub.kind === 'land')      return <LandDetailScreen c={c} item={SW_LAND.find(l => l.id === sub.id)} onBack={() => setSub({ kind: 'land-list' })} />;
    if (sub.kind === 'dep-list')  return <DepositoListScreen c={c} onBack={() => setSub(null)} onOpen={(id) => setSub({ kind: 'dep', id })} />;
    if (sub.kind === 'dep')       return <DepositoDetailScreen c={c} item={SW_DEPOSIT.find(d => d.id === sub.id)} onBack={() => setSub({ kind: 'dep-list' })} />;
    if (sub.kind === 'debt-list') return <DebtListScreen c={c} onBack={() => setSub(null)} onOpen={(id) => setSub({ kind: 'debt', id })} />;
    if (sub.kind === 'debt')      return <DebtDetailScreen c={c} item={SW_DEBTS.find(d => d.id === sub.id)} onBack={() => setSub({ kind: 'debt-list' })} />;
  }

  const slices = [
    { name: 'Akun',     value: acctTotal,  color: c.primary, icon: 'wallet'  },
    { name: 'Emas',     value: goldTotal,  color: c.warning, icon: 'gold'    },
    { name: 'Properti', value: landTotal,  color: c.info,    icon: 'land'    },
    { name: 'Deposito', value: depTotal,   color: c.accent,  icon: 'deposit' },
  ];
  const totalPos = slices.reduce((s, x) => s + x.value, 0);

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Aset & Kekayaan" />

      {/* Net worth hero */}
      <div style={{ padding: '0 20px 16px' }}>
        <div style={{
          background: c.primary, color: c.onPrimary,
          borderRadius: 22, padding: '22px 22px 18px',
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -36, bottom: -36, opacity: 0.10 }}>
            <LogoA_Daun theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={180} />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12 }}>
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase', marginBottom: 2 }}>Total Kekayaan</div>
                {hide
                  ? <div style={{ fontFamily: SW_TYPE.family, fontSize: 36, fontWeight: 800, color: c.onPrimary, letterSpacing: '0.04em', lineHeight: 1, opacity: 0.85 }}>••••••••</div>
                  : <SW_Amount c={c} value={netWorth} size={36} weight={800} color={c.onPrimary} />
                }
              </div>
              <button onClick={() => setHide(h => !h)} className="sw-press" aria-label={hide ? 'Tampilkan saldo' : 'Sembunyikan saldo'} style={{
                width: 38, height: 38, borderRadius: 12, flex: '0 0 auto',
                background: 'rgba(255,255,255,0.12)', color: c.onPrimary, border: 'none',
                display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
                opacity: 0.9,
              }}>
                <Icon name={hide ? 'eye_off' : 'eye'} size={18} strokeWidth={1.8} />
              </button>
            </div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: 4, marginTop: 8, padding: '4px 10px', background: 'rgba(255,255,255,0.16)', borderRadius: 8, fontSize: 11, fontWeight: 700, whiteSpace: 'nowrap' }}>
              <Icon name="arrow_up_right" size={11} strokeWidth={2.5} />
              +12.4% sejak Jan
            </div>

            {/* Stacked bar */}
            <div style={{ display: 'flex', height: 8, borderRadius: 4, overflow: 'hidden', marginTop: 16, background: 'rgba(255,255,255,0.12)' }}>
              {slices.map(s => (
                <div key={s.name} style={{ width: `${(s.value / totalPos) * 100}%`, background: s.color, opacity: 0.92 }} />
              ))}
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 6, gap: 4, flexWrap: 'wrap' }}>
              {slices.map(s => (
                <div key={s.name} style={{ fontSize: 10, opacity: 0.85, display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                  <span style={{ width: 6, height: 6, borderRadius: 2, background: s.color }} />
                  {s.name}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Trend chart */}
      <NetWorthTrendCard c={c} hide={hide} />

      {/* Asset class cards */}
      <div style={{ padding: '0 20px 4px' }}>
        <SW_SectionLabel c={c}>Kelas Aset</SW_SectionLabel>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          <AssetCard c={c} icon="wallet"  color={c.primary} name="Akun"     count={`${SW_ACCOUNTS.length} aktif`}  value={acctTotal}  hide={hide} onClick={() => setSub({ kind: 'accounts' })} />
          <AssetCard c={c} icon="gold"    color={c.warning} name="Emas"     count={`${SW_GOLD.filter(g => g.status === 'held').reduce((s, g) => s + g.weight, 0)} g`} value={goldTotal} delta="+14.2%" hide={hide} onClick={() => setSub({ kind: 'emas-list' })} />
          <AssetCard c={c} icon="land"    color={c.info}    name="Properti" count={`${SW_LAND.length} aset`}        value={landTotal}  delta="+12.5%" hide={hide} onClick={() => setSub({ kind: 'land-list' })} />
          <AssetCard c={c} icon="deposit" color={c.accent}  name="Deposito" count={`${SW_DEPOSIT.length} aset`}      value={depTotal}   delta="+6.8%"  hide={hide} onClick={() => setSub({ kind: 'dep-list' })} />
        </div>
      </div>

      {/* Hutang section */}
      <div style={{ padding: '16px 20px 4px' }}>
        <SW_SectionLabel c={c}>Hutang</SW_SectionLabel>
        <SW_Card c={c} padding={0} onClick={() => setSub({ kind: 'debt-list' })}>
          <div style={{ padding: '14px 16px', display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{
              width: 40, height: 40, borderRadius: 12,
              background: c.dangerSoft, color: c.danger,
              display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
            }}>
              <Icon name="link" size={20} />
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: c.ink, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>Saya berhutang</div>
              <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>{SW_DEBTS.filter(d => d.direction === 'i_owe').length} hutang · outstanding</div>
            </div>
            <div style={{ textAlign: 'right', flex: '0 0 auto' }}>
              {hide
                ? <div style={{ fontFamily: SW_TYPE.family, fontSize: 15, fontWeight: 700, color: c.danger, letterSpacing: '0.04em' }}>••••••</div>
                : <SW_Amount c={c} value={debtTotal} size={15} weight={700} color={c.danger} />
              }
            </div>
            <Icon name="chevron_right" size={20} color={c.inkSubtle} />
          </div>
        </SW_Card>
      </div>
    </div>
  );
};

const AssetCard = ({ c, icon, color, name, count, value, delta, hide, onClick }) => (
  <button onClick={onClick} className="sw-press" style={{
    textAlign: 'left',
    background: c.surface, border: `1px solid ${c.border}`,
    borderRadius: 16, padding: 14, cursor: 'pointer',
    fontFamily: SW_TYPE.family,
    display: 'flex', flexDirection: 'column', gap: 10,
  }}>
    <div style={{
      width: 36, height: 36, borderRadius: 12,
      background: color + '22', color,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <Icon name={icon} size={20} strokeWidth={1.8} />
    </div>
    <div>
      <div style={{ fontSize: 13, fontWeight: 700, color: c.ink, marginBottom: 2 }}>{name}</div>
      <div style={{ fontSize: 10, color: c.inkSubtle, fontWeight: 500 }}>{count}</div>
    </div>
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 6, marginTop: 'auto' }}>
      <span style={{ fontSize: 15, fontWeight: 700, color: c.ink, fontVariantNumeric: 'tabular-nums', letterSpacing: hide ? '0.04em' : '-0.01em', whiteSpace: 'nowrap' }}>
        {hide ? '••••••' : SW_FORMAT.rpShort(value)}
      </span>
      {delta && !hide && <span style={{ fontSize: 10, fontWeight: 700, color: c.success, whiteSpace: 'nowrap', flex: '0 0 auto' }}>{delta}</span>}
    </div>
  </button>
);

// ─── Accounts list ──────────────────────────────────────────

const AccountsListScreen = ({ c, onBack, onOpen }) => (
  <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
    <SW_TopBar c={c} title="Akun" onBack={onBack} right={
      <button className="sw-press" aria-label="Tambah akun" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: c.primary, color: c.onPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Icon name="plus" size={20} strokeWidth={2.2} />
      </button>
    } />
    <div style={{ padding: '0 20px' }}>
      <SW_Card c={c} padding={0}>
        {SW_ACCOUNTS.map((a, i, arr) => {
          const snapshots = SW_ACCOUNT_SNAPSHOTS[a.id] || [];
          const last = snapshots[0];
          return (
            <button key={a.id} onClick={() => onOpen(a.id)} className="sw-press" style={{
              width: '100%', textAlign: 'left',
              display: 'flex', alignItems: 'center', gap: 14,
              padding: '14px 16px',
              background: 'transparent', border: 'none', cursor: 'pointer',
              borderBottom: i < arr.length - 1 ? `1px solid ${c.border}` : 'none',
              fontFamily: SW_TYPE.family,
            }}>
              <SW_AccountIcon c={c} account={a} size={44} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 15, fontWeight: 700, color: c.ink }}>{a.name}</div>
                <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>
                  {a.type}{last ? ` · rekon ${SW_FORMAT.dateRel(last.date)}` : ''}
                </div>
              </div>
              <SW_Amount c={c} value={a.balance} size={15} weight={700} />
              <Icon name="chevron_right" size={18} color={c.inkSubtle} />
            </button>
          );
        })}
      </SW_Card>
      <div style={{ marginTop: 16, padding: 14, background: c.infoSoft, border: `1px solid ${c.info}33`, borderRadius: 12, display: 'flex', gap: 10 }}>
        <Icon name="info" size={16} color={c.info} />
        <div style={{ fontSize: 11, color: c.inkMuted, lineHeight: 1.5, flex: 1 }}>
          <strong style={{ color: c.ink }}>Tap akun</strong> untuk lihat detail, snapshot history, dan rekonsiliasi.
        </div>
      </div>
    </div>
  </div>
);

// ─── Account Detail ─────────────────────────────────────────

const AccountDetailScreen = ({ c, account, onBack }) => {
  const [showActions, setShowActions] = React.useState(false);
  const [reconcile, setReconcile] = React.useState(false);
  const [showAllTxn, setShowAllTxn] = React.useState(false);

  if (!account) return null;
  const snapshots = SW_ACCOUNT_SNAPSHOTS[account.id] || [];
  const last = snapshots[0];
  const accountTxns = SW_TRANSACTIONS.filter(t => t.account === account.id);
  const recentTxns = showAllTxn ? accountTxns : accountTxns.slice(0, 5);

  if (reconcile) {
    return <ReconciliationFlow c={c} account={account} onClose={() => setReconcile(false)} onDone={() => setReconcile(false)} />;
  }

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={account.name} onBack={onBack} right={
        <button onClick={() => setShowActions(true)} className="sw-press" aria-label="Aksi akun" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: 'transparent', color: c.ink, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="more" size={22} />
        </button>
      } />

      {/* Hero */}
      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.primary, color: c.onPrimary,
          borderRadius: 22, padding: '20px 22px',
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -30, bottom: -30, opacity: 0.10 }}>
            <SW_AccountIconLarge c={c} icon={account.icon} />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase', marginBottom: 4 }}>
              Saldo · {account.type}
            </div>
            <SW_Amount c={c} value={account.balance} size={34} weight={800} color={c.onPrimary} />
            <div style={{ fontSize: 12, opacity: 0.78, marginTop: 6 }}>
              {last
                ? `Rekonsiliasi terakhir ${SW_FORMAT.dateRel(last.date)}${last.diff !== 0 ? ` · selisih ${last.diff >= 0 ? '+' : '−'} ${SW_FORMAT.rpShort(Math.abs(last.diff))}` : ''}`
                : 'Belum pernah direkonsiliasi'}
            </div>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div style={{ padding: '0 20px 16px', display: 'flex', gap: 10 }}>
        <SW_Button c={c} icon="check" onClick={() => setReconcile(true)}>Rekonsiliasi</SW_Button>
        <SW_Button c={c} variant="secondary" icon="edit" onClick={() => setShowActions(true)}>Edit</SW_Button>
      </div>

      {/* Snapshot history */}
      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c}>Riwayat Snapshot</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          {snapshots.length === 0 ? (
            <div style={{ padding: '24px 16px', textAlign: 'center', color: c.inkSubtle, fontSize: 12 }}>Belum ada snapshot rekonsiliasi.</div>
          ) : (
            <div>
              {/* Table header */}
              <div style={{ display: 'flex', padding: '10px 16px', gap: 8, borderBottom: `1px solid ${c.border}` }}>
                <span style={{ flex: 1, fontSize: 10, fontWeight: 700, color: c.inkSubtle, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Tanggal</span>
                <span style={{ width: 90, textAlign: 'right', fontSize: 10, fontWeight: 700, color: c.inkSubtle, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Saldo Asli</span>
                <span style={{ width: 70, textAlign: 'right', fontSize: 10, fontWeight: 700, color: c.inkSubtle, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Selisih</span>
              </div>
              {snapshots.map((s, i, arr) => (
                <SnapshotRow key={s.id} c={c} snap={s} last={i === arr.length - 1} />
              ))}
            </div>
          )}
        </SW_Card>
      </div>

      {/* Transaction history */}
      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c} right={
          <button onClick={() => setShowAllTxn(v => !v)} className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: 2, cursor: 'pointer', whiteSpace: 'nowrap' }}>
            {showAllTxn ? 'Lebih sedikit' : 'Semua'} <Icon name={showAllTxn ? 'chevron_up' : 'chevron_right'} size={14} strokeWidth={2.2} />
          </button>
        }>Transaksi di Akun Ini</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          {recentTxns.length === 0 ? (
            <div style={{ padding: '24px 16px', textAlign: 'center', color: c.inkSubtle, fontSize: 12 }}>Belum ada transaksi.</div>
          ) : (
            recentTxns.map((t, i, arr) => (
              <TxnItem key={t.id} c={c} txn={t} divider={i < arr.length - 1} />
            ))
          )}
        </SW_Card>
      </div>

      {/* Action sheet */}
      <SW_Sheet c={c} open={showActions} onClose={() => setShowActions(false)} title="Aksi Akun">
        <ActionRow c={c} icon="edit"  label="Edit nama & icon" sub="Ubah nama atau warna akun" onClick={() => setShowActions(false)} />
        <ActionRow c={c} icon="copy"  label="Lihat semua snapshot" sub="Riwayat lengkap dalam tabel" onClick={() => setShowActions(false)} />
        <ActionRow c={c} icon="receipt" label="Export transaksi akun" sub="CSV / PDF" onClick={() => setShowActions(false)} />
        <ActionRow c={c} icon="trash" label="Arsipkan akun" sub="Akun disembunyikan, history tetap tersimpan" danger onClick={() => setShowActions(false)} />
      </SW_Sheet>
    </div>
  );
};

const SnapshotRow = ({ c, snap, last }) => (
  <div style={{
    display: 'flex', padding: '12px 16px', gap: 8, alignItems: 'flex-start',
    borderBottom: last ? 'none' : `1px solid ${c.border}`,
  }}>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 13, fontWeight: 600, color: c.ink, whiteSpace: 'nowrap' }}>{SW_FORMAT.date(snap.date)}</div>
      {snap.note
        ? <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis' }}>{snap.note}</div>
        : <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 2 }}>App: <span style={{ fontVariantNumeric: 'tabular-nums' }}>{SW_FORMAT.rpShort(snap.appBalance)}</span></div>
      }
    </div>
    <span style={{ width: 90, textAlign: 'right', fontSize: 13, fontWeight: 700, color: c.ink, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>
      {SW_FORMAT.rpShort(snap.realBalance)}
    </span>
    <span style={{
      width: 70, textAlign: 'right', fontSize: 12, fontWeight: 700,
      color: snap.diff === 0 ? c.inkSubtle : snap.diff > 0 ? c.success : c.danger,
      fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap',
    }}>
      {snap.diff === 0 ? '—' : `${snap.diff > 0 ? '+' : '−'} ${SW_FORMAT.rpShort(Math.abs(snap.diff))}`}
    </span>
  </div>
);

const SW_AccountIconLarge = ({ c, icon }) => (
  <div style={{ color: c.onPrimary }}>
    <Icon name={icon} size={160} strokeWidth={1.4} />
  </div>
);

// ─── Emas list ──────────────────────────────────────────────

const EmasListScreen = ({ c, onBack, onOpen }) => {
  const [showPriceSheet, setShowPriceSheet] = React.useState(false);
  const heldRecords = SW_GOLD.filter(g => g.status === 'held');
  const heldGram = heldRecords.reduce((s, g) => s + g.weight, 0);
  const totalValue = heldGram * SW_GOLD_PRICE;
  const totalBuy = heldRecords.reduce((s, g) => s + g.buyPrice, 0);
  const profit = totalValue - totalBuy;
  const profitPct = totalBuy > 0 ? (profit / totalBuy) * 100 : 0;

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Emas" onBack={onBack} right={
        <button className="sw-press" aria-label="Tambah emas" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: c.primary, color: c.onPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="plus" size={20} strokeWidth={2.2} />
        </button>
      } />

      {/* Hero summary */}
      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.warning, color: '#fff',
          borderRadius: 20, padding: 18,
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -20, opacity: 0.18 }}>
            <Icon name="gold" size={140} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.85, textTransform: 'uppercase' }}>Nilai Saat Ini · {heldGram} gram</div>
            <SW_Amount c={c} value={totalValue} size={30} weight={800} color="#fff" />
            <div style={{ marginTop: 8, display: 'flex', gap: 12, fontSize: 12 }}>
              <span style={{ opacity: 0.85 }}>Profit <strong style={{ fontVariantNumeric: 'tabular-nums' }}>{profit >= 0 ? '+' : '−'} {SW_FORMAT.rpShort(Math.abs(profit))}</strong></span>
              <span style={{ opacity: 0.85 }}>·  <strong>{profitPct >= 0 ? '+' : '−'}{Math.abs(profitPct).toFixed(1)}%</strong></span>
            </div>
          </div>
        </div>
      </div>

      {/* Global gold price */}
      <div style={{ padding: '0 20px 14px' }}>
        <button onClick={() => setShowPriceSheet(true)} className="sw-press" style={{
          width: '100%', textAlign: 'left',
          display: 'flex', alignItems: 'center', gap: 12,
          padding: '12px 14px',
          background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 12, cursor: 'pointer', fontFamily: SW_TYPE.family,
        }}>
          <div style={{ width: 36, height: 36, borderRadius: 10, background: c.warningSoft, color: c.warning, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="sparkle" size={18} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: c.ink }}>Harga Jual Emas Global</div>
            <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1, fontVariantNumeric: 'tabular-nums' }}>{SW_FORMAT.rp(SW_GOLD_PRICE)} / gram · update manual</div>
          </div>
          <Icon name="edit" size={18} color={c.inkSubtle} />
        </button>
      </div>

      {/* Records list */}
      <div style={{ padding: '0 20px' }}>
        <SW_SectionLabel c={c}>{heldRecords.length} Batch Dipegang</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          {heldRecords.map((g, i, arr) => {
            const value = g.weight * SW_GOLD_PRICE;
            const p = value - g.buyPrice;
            const pp = (p / g.buyPrice) * 100;
            return (
              <button key={g.id} onClick={() => onOpen(g.id)} className="sw-press" style={{
                width: '100%', textAlign: 'left',
                display: 'flex', alignItems: 'center', gap: 14,
                padding: '14px 16px',
                background: 'transparent', border: 'none', cursor: 'pointer',
                borderBottom: i < arr.length - 1 ? `1px solid ${c.border}` : 'none',
                fontFamily: SW_TYPE.family,
              }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: c.warningSoft, color: c.warning, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Icon name="gold" size={22} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>{g.weight} gram · {g.serial}</div>
                  <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 2 }}>Beli {SW_FORMAT.date(g.date)}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <SW_Amount c={c} value={value} size={14} weight={700} />
                  <div style={{ fontSize: 10, color: p >= 0 ? c.success : c.danger, fontWeight: 700, marginTop: 2 }}>
                    {p >= 0 ? '+' : '−'}{Math.abs(pp).toFixed(1)}%
                  </div>
                </div>
              </button>
            );
          })}
        </SW_Card>
      </div>

      <SW_Sheet c={c} open={showPriceSheet} onClose={() => setShowPriceSheet(false)} title="Ubah Harga Emas Global" maxHeight="50%">
        <div style={{ fontSize: 13, color: c.inkMuted, marginBottom: 14, lineHeight: 1.5 }}>
          Update manual sesuai harga jual emas hari ini (Antam, Pegadaian, dll). Auto-fetch akan tersedia di V2.
        </div>
        <SW_Field c={c} label="Harga per gram" value="1.050.000" prefix="Rp" suffix="/ gram" />
        <SW_Button c={c} onClick={() => setShowPriceSheet(false)} size="lg">Simpan</SW_Button>
      </SW_Sheet>
    </div>
  );
};

// ─── Emas detail ────────────────────────────────────────────

const EmasDetailScreen = ({ c, item, onBack }) => {
  if (!item) return null;
  const value = item.weight * SW_GOLD_PRICE;
  const profit = value - item.buyPrice;
  const profitPct = (profit / item.buyPrice) * 100;
  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={`Emas ${item.weight} gram`} onBack={onBack} right={
        <button className="sw-press" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: 'transparent', color: c.ink, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="more" size={22} />
        </button>
      } />

      {/* Hero */}
      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.warning, color: '#fff',
          borderRadius: 22, padding: '20px 22px',
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -30, opacity: 0.18 }}>
            <Icon name="gold" size={160} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.85, textTransform: 'uppercase' }}>Nilai saat ini</div>
            <SW_Amount c={c} value={value} size={34} weight={800} color="#fff" />
            <div style={{ marginTop: 10, padding: '6px 12px', background: 'rgba(255,255,255,0.18)', borderRadius: 8, display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 700, whiteSpace: 'nowrap' }}>
              <Icon name={profit >= 0 ? 'arrow_up_right' : 'arrow_down_left'} size={12} strokeWidth={2.5} />
              {profit >= 0 ? '+' : '−'} {SW_FORMAT.rpShort(Math.abs(profit))} · {profit >= 0 ? '+' : '−'}{Math.abs(profitPct).toFixed(1)}%
            </div>
          </div>
        </div>
      </div>

      {/* Details */}
      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c}>Detail</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          <DetailRow c={c} label="Berat" value={`${item.weight} gram`} />
          <DetailRow c={c} label="Tanggal beli" value={SW_FORMAT.date(item.date)} />
          <DetailRow c={c} label="Harga beli" value={SW_FORMAT.rp(item.buyPrice)} />
          <DetailRow c={c} label="Harga / gram saat beli" value={SW_FORMAT.rp(item.buyPrice / item.weight)} />
          <DetailRow c={c} label="Nomor seri" value={item.serial} last />
        </SW_Card>
      </div>

      <div style={{ padding: '0 20px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <SW_Button c={c} variant="secondary" icon="camera">Tambah foto</SW_Button>
        <SW_Button c={c} variant="outline" icon="arrow_up_right">Catat penjualan</SW_Button>
      </div>
    </div>
  );
};

const DetailRow = ({ c, label, value, last }) => (
  <div style={{
    display: 'flex', alignItems: 'center', gap: 12,
    padding: '12px 16px',
    borderBottom: last ? 'none' : `1px solid ${c.border}`,
  }}>
    <span style={{ fontSize: 13, color: c.inkMuted, fontWeight: 500, whiteSpace: 'nowrap', flex: '0 0 auto' }}>{label}</span>
    <span style={{ fontSize: 14, fontWeight: 700, color: c.ink, fontVariantNumeric: 'tabular-nums', textAlign: 'right', flex: '1 1 auto', minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{value}</span>
  </div>
);

Object.assign(window, {
  AssetsHubScreen, AssetCard, AccountsListScreen, AccountDetailScreen,
  SnapshotRow, NetWorthTrendCard,
  EmasListScreen, EmasDetailScreen, DetailRow,
});

// ─── Net worth trend chart ─────────────────────────────────

function NetWorthTrendCard({ c, hide }) {
  const [period, setPeriod] = React.useState('6M'); // 3M | 6M | 1Y | ALL
  const all = SW_NETWORTH_HISTORY;
  const months = { '3M': 3, '6M': 6, '1Y': 12, 'ALL': all.length };
  const data = all.slice(-months[period]);
  const first = data[0];
  const last = data[data.length - 1];
  const delta = last.value - first.value;
  const deltaPct = (delta / first.value) * 100;
  const months_id = ['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Agu','Sep','Okt','Nov','Des'];

  return (
    <div style={{ padding: '0 20px 14px' }}>
      <div style={{
        background: c.surface, border: `1px solid ${c.border}`,
        borderRadius: 18, padding: '16px 18px 12px',
      }}>
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 8, marginBottom: 4 }}>
          <span style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', whiteSpace: 'nowrap' }}>Tren Kekayaan</span>
          <span style={{ fontSize: 12, color: delta >= 0 ? c.success : c.danger, fontWeight: 700, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>
            {delta >= 0 ? '+' : '−'} {SW_FORMAT.rpShort(Math.abs(delta))} · {delta >= 0 ? '+' : '−'}{Math.abs(deltaPct).toFixed(1)}%
          </span>
        </div>

        {hide
          ? <div style={{ height: 110, display: 'flex', alignItems: 'center', justifyContent: 'center', color: c.inkSubtle, fontSize: 13 }}>Saldo tersembunyi</div>
          : <SnapshotChart c={c} snapshots={data} height={110} />
        }

        {/* X-axis hint: first and last labels only */}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, paddingLeft: 4, paddingRight: 4, fontSize: 10, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>
          <span>{months_id[first.date.getMonth()]} '{String(first.date.getFullYear()).slice(2)}</span>
          <span>{months_id[last.date.getMonth()]} '{String(last.date.getFullYear()).slice(2)}</span>
        </div>

        {/* Period selector */}
        <div style={{ display: 'flex', gap: 4, marginTop: 12, padding: 3, background: c.bg, borderRadius: 10 }}>
          {['3M', '6M', '1Y', 'Semua'].map((p, i) => {
            const pid = i === 3 ? 'ALL' : p;
            const active = period === pid;
            return (
              <button key={p} onClick={() => setPeriod(pid)} className="sw-press" style={{
                flex: 1, padding: '6px 0',
                background: active ? c.surface : 'transparent',
                color: active ? c.ink : c.inkMuted,
                border: 'none', borderRadius: 8,
                fontFamily: SW_TYPE.family, fontSize: 11, fontWeight: 700,
                cursor: 'pointer',
                boxShadow: active ? '0 1px 2px rgba(0,0,0,0.06)' : 'none',
              }}>{p}</button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
