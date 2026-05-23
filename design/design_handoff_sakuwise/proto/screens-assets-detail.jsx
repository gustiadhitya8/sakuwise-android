// Sakuwise — Tanah / Properti list & detail with tax payment sub-list

const LandListScreen = ({ c, onBack, onOpen }) => {
  const total = SW_LAND.reduce((s, l) => s + (l.currentValue || l.buyPrice), 0);
  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Tanah & Properti" onBack={onBack} right={
        <button className="sw-press" aria-label="Tambah properti" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: c.primary, color: c.onPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="plus" size={20} strokeWidth={2.2} />
        </button>
      } />

      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.info, color: '#fff',
          borderRadius: 20, padding: 18,
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -30, opacity: 0.18 }}>
            <Icon name="land" size={140} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.85, textTransform: 'uppercase' }}>Total Nilai Estimasi</div>
            <SW_Amount c={c} value={total} size={30} weight={800} color="#fff" />
            <div style={{ marginTop: 8, fontSize: 12, opacity: 0.85 }}>{SW_LAND.length} aset · update nilai manual</div>
          </div>
        </div>
      </div>

      <div style={{ padding: '0 20px' }}>
        <SW_SectionLabel c={c}>Properti</SW_SectionLabel>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {SW_LAND.map(l => {
            const value = l.currentValue || l.buyPrice;
            const profit = value - l.buyPrice;
            const profitPct = (profit / l.buyPrice) * 100;
            return (
              <button key={l.id} onClick={() => onOpen(l.id)} className="sw-press" style={{
                background: c.surface, border: `1px solid ${c.border}`,
                borderRadius: 16, padding: 14, cursor: 'pointer',
                fontFamily: SW_TYPE.family, textAlign: 'left',
              }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, marginBottom: 12 }}>
                  <div style={{ width: 44, height: 44, borderRadius: 12, background: c.infoSoft, color: c.info, display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
                    <Icon name="land" size={22} />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 15, fontWeight: 700, color: c.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{l.name}</div>
                    <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{l.location} · {l.size} m²</div>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 8 }}>
                  <SW_Amount c={c} value={value} size={18} weight={700} />
                  <div style={{ display: 'inline-flex', alignItems: 'center', gap: 4, padding: '3px 8px', background: profit >= 0 ? c.successSoft : c.dangerSoft, color: profit >= 0 ? c.success : c.danger, borderRadius: 8, fontSize: 11, fontWeight: 700 }}>
                    <Icon name={profit >= 0 ? 'arrow_up_right' : 'arrow_down_left'} size={11} strokeWidth={2.5} />
                    {profit >= 0 ? '+' : '−'}{Math.abs(profitPct).toFixed(1)}%
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

const LandDetailScreen = ({ c, item, onBack }) => {
  const [showTaxSheet, setShowTaxSheet] = React.useState(false);
  if (!item) return null;
  const value = item.currentValue || item.buyPrice;
  const profit = value - item.buyPrice;
  const profitPct = (profit / item.buyPrice) * 100;
  const totalTax = item.taxes.reduce((s, t) => s + t.amount, 0);

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={item.name} onBack={onBack} right={
        <button className="sw-press" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: 'transparent', color: c.ink, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="more" size={22} />
        </button>
      } />

      {/* Hero */}
      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.info, color: '#fff',
          borderRadius: 22, padding: '20px 22px',
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -30, opacity: 0.18 }}>
            <Icon name="land" size={160} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.85, textTransform: 'uppercase' }}>Nilai Estimasi</div>
            <SW_Amount c={c} value={value} size={32} weight={800} color="#fff" />
            <div style={{ marginTop: 10, padding: '6px 12px', background: 'rgba(255,255,255,0.18)', borderRadius: 8, display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 700, whiteSpace: 'nowrap' }}>
              <Icon name="arrow_up_right" size={12} strokeWidth={2.5} />
              +{SW_FORMAT.rpShort(profit)} sejak beli · +{profitPct.toFixed(1)}%
            </div>
          </div>
        </div>
      </div>

      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c}>Detail</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          <DetailRow c={c} label="Lokasi" value={item.location} />
          <DetailRow c={c} label="Luas" value={`${item.size} m²`} />
          <DetailRow c={c} label="No. Sertifikat" value={item.shm} />
          <DetailRow c={c} label="Harga beli" value={SW_FORMAT.rp(item.buyPrice)} />
          <DetailRow c={c} label="Nilai saat ini" value={SW_FORMAT.rp(value)} last />
        </SW_Card>
      </div>

      {/* Tax payments */}
      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c} right={
          <button onClick={() => setShowTaxSheet(true)} className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: 2, cursor: 'pointer', whiteSpace: 'nowrap' }}>
            <Icon name="plus" size={14} strokeWidth={2.4} />
            Tambah
          </button>
        }>Pembayaran Pajak (PBB)</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          {item.taxes.map((t, i, arr) => (
            <div key={t.id} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              padding: '12px 16px',
              borderBottom: i < arr.length - 1 ? `1px solid ${c.border}` : 'none',
            }}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: c.warningSoft, color: c.warning, display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
                <Icon name="receipt" size={16} />
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>{t.note}</div>
                <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 1 }}>{SW_FORMAT.date(t.date)}</div>
              </div>
              <SW_Amount c={c} value={t.amount} size={14} weight={700} />
            </div>
          ))}
          <div style={{ padding: '12px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: c.bg, borderRadius: '0 0 18px 18px' }}>
            <span style={{ fontSize: 11, color: c.inkSubtle, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>Total dibayar</span>
            <SW_Amount c={c} value={totalTax} size={14} weight={700} />
          </div>
        </SW_Card>
      </div>

      <SW_Sheet c={c} open={showTaxSheet} onClose={() => setShowTaxSheet(false)} title="Tambah Pembayaran PBB" maxHeight="68%">
        <SW_Field c={c} label="Nominal" value="" prefix="Rp" placeholder="0" />
        <SW_Field c={c} label="Tanggal bayar" value="15 Mei 2026" readOnly />
        <SW_Field c={c} label="Catatan" value="" placeholder="Mis. PBB 2026" />
        <div style={{ marginBottom: 14 }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 6 }}>Akun pembayar (opsional)</div>
          <button className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', gap: 10,
            background: c.surface, border: `1.5px solid ${c.border}`,
            borderRadius: 12, padding: '10px 14px', minHeight: 52, cursor: 'pointer',
            fontFamily: SW_TYPE.family,
          }}>
            <Icon name="wallet" size={20} color={c.inkMuted} />
            <span style={{ color: c.inkMuted, fontSize: 15, flex: 1 }}>Pilih akun untuk catat transaksi tertaut</span>
            <Icon name="chevron_right" size={18} color={c.inkSubtle} />
          </button>
        </div>
        <SW_Button c={c} onClick={() => setShowTaxSheet(false)} size="lg" icon="check">Simpan Pembayaran</SW_Button>
      </SW_Sheet>
    </div>
  );
};

// ─── Deposito list ──────────────────────────────────────────

const DepositoListScreen = ({ c, onBack, onOpen }) => {
  const total = SW_DEPOSIT.reduce((s, d) => {
    const last = d.snapshots[d.snapshots.length - 1];
    return s + (last ? last.balance : 0);
  }, 0);
  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Deposito & Pensiun" onBack={onBack} right={
        <button className="sw-press" aria-label="Tambah aset" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: c.primary, color: c.onPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="plus" size={20} strokeWidth={2.2} />
        </button>
      } />

      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.accent, color: c.onPrimaryContainer,
          borderRadius: 20, padding: 18,
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -30, opacity: 0.20 }}>
            <Icon name="deposit" size={140} color={c.onPrimaryContainer} />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase' }}>Saldo Snapshot Terakhir</div>
            <SW_Amount c={c} value={total} size={30} weight={800} color={c.onPrimaryContainer} />
            <div style={{ marginTop: 8, fontSize: 12, opacity: 0.75 }}>{SW_DEPOSIT.length} aset · update saat ada slip</div>
          </div>
        </div>
      </div>

      <div style={{ padding: '0 20px' }}>
        <SW_SectionLabel c={c}>Aset Deposito / Pensiun</SW_SectionLabel>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {SW_DEPOSIT.map(d => {
            const last = d.snapshots[d.snapshots.length - 1];
            const first = d.snapshots[0];
            const growth = last && first ? ((last.balance - first.balance) / first.balance) * 100 : 0;
            return (
              <button key={d.id} onClick={() => onOpen(d.id)} className="sw-press" style={{
                background: c.surface, border: `1px solid ${c.border}`,
                borderRadius: 16, padding: 14, cursor: 'pointer',
                fontFamily: SW_TYPE.family, textAlign: 'left',
                display: 'flex', alignItems: 'center', gap: 12,
              }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: c.accentSoft, color: c.accent === c.primary ? c.primary : (c === SW_DARK ? c.accent : '#0A2E22'), display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
                  <Icon name="deposit" size={22} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 14, fontWeight: 700, color: c.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{d.name}</span>
                    <span style={{ fontSize: 10, fontFamily: SW_TYPE.mono, color: c.inkSubtle, letterSpacing: '0.04em', padding: '2px 6px', background: c.bg, borderRadius: 4 }}>{d.type}</span>
                  </div>
                  <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 2 }}>{d.snapshots.length} snapshot · terakhir {last && SW_FORMAT.date(last.date)}</div>
                </div>
                <div style={{ textAlign: 'right', flex: '0 0 auto' }}>
                  <SW_Amount c={c} value={last ? last.balance : 0} size={14} weight={700} />
                  <div style={{ fontSize: 10, color: c.success, fontWeight: 700, marginTop: 2 }}>+{growth.toFixed(1)}%</div>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

// ─── Deposito detail with line chart ────────────────────────

const DepositoDetailScreen = ({ c, item, onBack }) => {
  const [showSheet, setShowSheet] = React.useState(false);
  if (!item) return null;
  const last = item.snapshots[item.snapshots.length - 1];
  const first = item.snapshots[0];
  const growth = ((last.balance - first.balance) / first.balance) * 100;

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={item.name} onBack={onBack} />

      {/* Hero with chart */}
      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 22, padding: '20px 22px 14px',
        }}>
          <div style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase' }}>Saldo Terbaru</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
            <SW_Amount c={c} value={last.balance} size={30} weight={800} />
            <span style={{ fontSize: 12, color: c.success, fontWeight: 700 }}>+{growth.toFixed(1)}%</span>
          </div>
          <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 2 }}>Per {SW_FORMAT.date(last.date)}</div>
          <SnapshotChart c={c} snapshots={item.snapshots} />
        </div>
      </div>

      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c}>Detail</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          <DetailRow c={c} label="Tipe" value={item.type} />
          <DetailRow c={c} label="Institusi" value={item.institution} last />
        </SW_Card>
      </div>

      {/* Snapshot history */}
      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c} right={
          <button onClick={() => setShowSheet(true)} className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: 2, cursor: 'pointer', whiteSpace: 'nowrap' }}>
            <Icon name="plus" size={14} strokeWidth={2.4} />
            Snapshot baru
          </button>
        }>Riwayat Snapshot</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          {[...item.snapshots].reverse().map((s, i, arr) => {
            const prev = arr[i + 1];
            const diff = prev ? s.balance - prev.balance : 0;
            return (
              <div key={i} style={{
                display: 'flex', alignItems: 'center', gap: 12,
                padding: '12px 16px',
                borderBottom: i < arr.length - 1 ? `1px solid ${c.border}` : 'none',
              }}>
                <div style={{ width: 36, height: 36, borderRadius: 10, background: c.primaryContainer, color: c.onPrimaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
                  <Icon name="calendar" size={16} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>{SW_FORMAT.date(s.date)}</div>
                  {prev && (
                    <div style={{ fontSize: 11, color: diff >= 0 ? c.success : c.danger, marginTop: 1, fontWeight: 600 }}>
                      {diff >= 0 ? '+' : '−'} {SW_FORMAT.rpShort(Math.abs(diff))} vs sebelumnya
                    </div>
                  )}
                </div>
                <SW_Amount c={c} value={s.balance} size={14} weight={700} />
              </div>
            );
          })}
        </SW_Card>
      </div>

      <SW_Sheet c={c} open={showSheet} onClose={() => setShowSheet(false)} title="Snapshot Baru" maxHeight="60%">
        <SW_Field c={c} label="Saldo terbaru" value="" prefix="Rp" placeholder="0" />
        <SW_Field c={c} label="Tanggal snapshot" value="15 Mei 2026" readOnly />
        <SW_Field c={c} label="Catatan (opsional)" value="" placeholder="" />
        <SW_Button c={c} onClick={() => setShowSheet(false)} size="lg" icon="check">Simpan Snapshot</SW_Button>
      </SW_Sheet>
    </div>
  );
};

// Compact line chart for snapshot/series data
const SnapshotChart = ({ c, snapshots, lineColor, height = 110 }) => {
  const W = 320, H = height, padX = 4, padY = 14;
  if (snapshots.length < 2) {
    return <div style={{ height: H, display: 'flex', alignItems: 'center', justifyContent: 'center', color: c.inkSubtle, fontSize: 12 }}>Butuh ≥2 data point untuk grafik</div>;
  }
  const xs = snapshots.map((s, i) => i);
  const ys = snapshots.map(s => s.balance ?? s.value);
  const minY = Math.min(...ys), maxY = Math.max(...ys);
  const sx = (i) => padX + (i / (xs.length - 1)) * (W - padX * 2);
  const sy = (v) => padY + (1 - (v - minY) / (maxY - minY || 1)) * (H - padY * 2);
  const lineColor_ = lineColor || c.primary;
  const linePath = snapshots.map((s, i) => `${i === 0 ? 'M' : 'L'} ${sx(i).toFixed(1)} ${sy(s.balance ?? s.value).toFixed(1)}`).join(' ');
  const areaPath = linePath + ` L ${sx(xs.length - 1).toFixed(1)} ${H - padY} L ${sx(0).toFixed(1)} ${H - padY} Z`;
  const gradId = 'sw-chart-' + (lineColor_).replace('#', '');
  return (
    <svg viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none" style={{ width: '100%', height: H, marginTop: 12, display: 'block' }}>
      <defs>
        <linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%"  stopColor={lineColor_} stopOpacity="0.22" />
          <stop offset="100%" stopColor={lineColor_} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={areaPath} fill={`url(#${gradId})`} />
      <path d={linePath} fill="none" stroke={lineColor_} strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
      {snapshots.map((s, i) => (
        <circle key={i} cx={sx(i)} cy={sy(s.balance ?? s.value)} r={i === snapshots.length - 1 ? 4 : 2.5} fill={c.surface} stroke={lineColor_} strokeWidth="2" />
      ))}
    </svg>
  );
};

// ─── Hutang list ────────────────────────────────────────────

const DebtListScreen = ({ c, onBack, onOpen }) => {
  const iOwe = SW_DEBTS.filter(d => d.direction === 'i_owe');
  const owedToMe = SW_DEBTS.filter(d => d.direction === 'owed_to_me');
  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Hutang" onBack={onBack} right={
        <button className="sw-press" aria-label="Tambah hutang" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: c.primary, color: c.onPrimary, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="plus" size={20} strokeWidth={2.2} />
        </button>
      } />

      <DebtListSection c={c} label="Saya Berhutang" debts={iOwe} negative onOpen={onOpen} />
      <DebtListSection c={c} label="Dipinjamkan ke Orang" debts={owedToMe} onOpen={onOpen} />
    </div>
  );
};

const DebtListSection = ({ c, label, debts, negative, onOpen }) => {
  if (!debts.length) return null;
  const total = debts.reduce((s, d) => s + (d.principal - d.payments.reduce((x, p) => x + p.amount, 0)), 0);
  return (
    <div style={{ padding: '0 20px 18px' }}>
      <SW_SectionLabel c={c} right={
        <span style={{ fontSize: 13, fontWeight: 700, color: negative ? c.danger : c.success, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>
          {negative ? '−' : '+'} {SW_FORMAT.rpShort(total)}
        </span>
      }>{label}</SW_SectionLabel>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {debts.map(d => {
          const paid = d.payments.reduce((s, p) => s + p.amount, 0);
          const outstanding = d.principal - paid;
          const pct = (paid / d.principal) * 100;
          return (
            <button key={d.id} onClick={() => onOpen(d.id)} className="sw-press" style={{
              background: c.surface, border: `1px solid ${c.border}`,
              borderRadius: 16, padding: 14, cursor: 'pointer',
              fontFamily: SW_TYPE.family, textAlign: 'left',
            }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 10, marginBottom: 10 }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 14, fontWeight: 700, color: c.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{d.counterparty}</div>
                  <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 2 }}>
                    Mulai {SW_FORMAT.date(d.startDate)}{d.dueDate ? ` · jatuh tempo ${SW_FORMAT.date(d.dueDate)}` : ''}
                  </div>
                </div>
                <div style={{ textAlign: 'right', flex: '0 0 auto' }}>
                  <SW_Amount c={c} value={outstanding} size={16} weight={700} color={negative ? c.danger : c.success} />
                  <div style={{ fontSize: 10, color: c.inkSubtle, marginTop: 2 }}>outstanding</div>
                </div>
              </div>
              <SW_Bar c={c} used={paid} plan={d.principal} color={negative ? c.danger : c.success} height={6} animate={false} />
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, marginTop: 6 }}>
                <span style={{ fontSize: 11, color: c.inkMuted, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>Dibayar {SW_FORMAT.rpShort(paid)}</span>
                <span style={{ fontSize: 11, color: c.inkMuted, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>{Math.round(pct)}% / {SW_FORMAT.rpShort(d.principal)}</span>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};

// ─── Hutang detail ──────────────────────────────────────────

const DebtDetailScreen = ({ c, item, onBack }) => {
  const [showPaymentSheet, setShowPaymentSheet] = React.useState(false);
  if (!item) return null;
  const paid = item.payments.reduce((s, p) => s + p.amount, 0);
  const outstanding = item.principal - paid;
  const negative = item.direction === 'i_owe';
  const tone = negative ? c.danger : c.success;
  const pct = (paid / item.principal) * 100;

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={item.counterparty} onBack={onBack} right={
        <button className="sw-press" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: 'transparent', color: c.ink, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Icon name="more" size={22} />
        </button>
      } />

      {/* Hero */}
      <div style={{ padding: '0 20px 14px' }}>
        <div style={{
          background: tone, color: '#fff',
          borderRadius: 22, padding: '20px 22px',
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -30, opacity: 0.18 }}>
            <Icon name="link" size={160} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.85, textTransform: 'uppercase' }}>{negative ? 'Sisa Hutang' : 'Belum Kembali'}</div>
            <SW_Amount c={c} value={outstanding} size={32} weight={800} color="#fff" />
            <div style={{ marginTop: 14 }}>
              <SW_Bar c={c} used={paid} plan={item.principal} color="#fff" height={6} animate={false} />
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, marginTop: 6, fontSize: 11, opacity: 0.85, fontVariantNumeric: 'tabular-nums' }}>
                <span style={{ whiteSpace: 'nowrap' }}>Dibayar {SW_FORMAT.rpShort(paid)} ({Math.round(pct)}%)</span>
                <span style={{ whiteSpace: 'nowrap' }}>dari {SW_FORMAT.rpShort(item.principal)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Outstanding trend chart — only for I-owe with payments */}
      {negative && item.payments.length > 0 && (
        <DebtOutstandingChart c={c} item={item} />
      )}

      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c}>Detail</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          <DetailRow c={c} label="Arah" value={negative ? 'Saya berhutang' : 'Mereka berhutang ke saya'} />
          <DetailRow c={c} label="Pokok" value={SW_FORMAT.rp(item.principal)} />
          <DetailRow c={c} label="Mulai" value={SW_FORMAT.date(item.startDate)} />
          <DetailRow c={c} label="Jatuh tempo" value={item.dueDate ? SW_FORMAT.date(item.dueDate) : '—'} />
          <DetailRow c={c} label="Status" value={item.status === 'open' ? 'Terbuka' : 'Lunas'} last />
        </SW_Card>
      </div>

      {/* Payments */}
      <div style={{ padding: '0 20px 14px' }}>
        <SW_SectionLabel c={c} right={
          <button onClick={() => setShowPaymentSheet(true)} className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 700, display: 'inline-flex', alignItems: 'center', gap: 2, cursor: 'pointer', whiteSpace: 'nowrap' }}>
            <Icon name="plus" size={14} strokeWidth={2.4} />
            Tambah pembayaran
          </button>
        }>{item.payments.length} Pembayaran</SW_SectionLabel>
        <SW_Card c={c} padding={0}>
          {item.payments.map((p, i, arr) => {
            const acct = SW_ACCOUNTS.find(a => a.id === p.account);
            return (
              <div key={p.id} style={{
                display: 'flex', alignItems: 'center', gap: 12,
                padding: '12px 16px',
                borderBottom: i < arr.length - 1 ? `1px solid ${c.border}` : 'none',
              }}>
                <div style={{ width: 36, height: 36, borderRadius: 10, background: c.successSoft, color: c.success, display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
                  <Icon name="check" size={16} strokeWidth={2.5} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>{SW_FORMAT.date(p.date)}</div>
                  <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 1 }}>{acct?.name || 'Akun tertaut'} · txn #{p.txId || '—'}</div>
                </div>
                <SW_Amount c={c} value={p.amount} size={14} weight={700} />
              </div>
            );
          })}
        </SW_Card>
      </div>

      <div style={{ padding: '0 20px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <SW_Button c={c} icon="plus" onClick={() => setShowPaymentSheet(true)} size="lg">Tambah Pembayaran</SW_Button>
        <SW_Button c={c} variant="outline" icon="check">Tandai Lunas</SW_Button>
      </div>

      <SW_Sheet c={c} open={showPaymentSheet} onClose={() => setShowPaymentSheet(false)} title="Tambah Pembayaran" maxHeight="70%">
        <SW_Field c={c} label="Nominal" value="" prefix="Rp" placeholder="0" />
        <SW_Field c={c} label="Tanggal" value="15 Mei 2026" readOnly />
        <FieldButton c={c} label="Akun pembayar" required>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
            <SW_AccountIcon c={c} account={SW_ACCOUNTS[0]} size={32} />
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, fontWeight: 600, color: c.ink }}>{SW_ACCOUNTS[0].name}</div>
              <div style={{ fontSize: 11, color: c.inkMuted }}>Akan jadi transaksi tertaut</div>
            </div>
          </div>
        </FieldButton>
        <FieldButton c={c} label="Plan item (opsional)">
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
            <SW_CategoryDot c={c} name="Cicilan" size={32} color={c.primary} />
            <span style={{ fontSize: 14, fontWeight: 600, color: c.ink }}>Cicilan KPR</span>
          </div>
        </FieldButton>
        <SW_Button c={c} onClick={() => setShowPaymentSheet(false)} size="lg" icon="check">Simpan Pembayaran</SW_Button>
      </SW_Sheet>
    </div>
  );
};

Object.assign(window, {
  LandListScreen, LandDetailScreen,
  DepositoListScreen, DepositoDetailScreen, SnapshotChart,
  DebtListScreen, DebtDetailScreen, DebtListSection, DebtOutstandingChart,
});

// Outstanding-over-time chart for hutang.
// Series = principal at startDate, then drop by each payment to current.
function DebtOutstandingChart({ c, item }) {
  // Build [{date, value: outstanding}]
  const series = [{ date: item.startDate, value: item.principal }];
  let running = item.principal;
  const sortedPayments = [...item.payments].sort((a, b) => a.date - b.date);
  sortedPayments.forEach(p => {
    running -= p.amount;
    series.push({ date: p.date, value: running });
  });

  const months_id = ['Jan','Feb','Mar','Apr','Mei','Jun','Jul','Agu','Sep','Okt','Nov','Des'];
  const first = series[0], last = series[series.length - 1];

  return (
    <div style={{ padding: '0 20px 14px' }}>
      <div style={{
        background: c.surface, border: `1px solid ${c.border}`,
        borderRadius: 18, padding: '16px 18px 14px',
      }}>
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 8, marginBottom: 4 }}>
          <span style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', whiteSpace: 'nowrap' }}>Sisa Hutang</span>
          <span style={{ fontSize: 11, color: c.inkMuted, whiteSpace: 'nowrap' }}>{sortedPayments.length} pembayaran</span>
        </div>
        <SnapshotChart c={c} snapshots={series} lineColor={c.danger} height={100} />
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4, paddingLeft: 4, paddingRight: 4, fontSize: 10, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>
          <span>{months_id[first.date.getMonth()]} '{String(first.date.getFullYear()).slice(2)}</span>
          <span>Sekarang</span>
        </div>
      </div>
    </div>
  );
}
