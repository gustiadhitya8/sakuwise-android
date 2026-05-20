// Sakuwise — Add Transaction picker + Pengeluaran form

const AddTxnPicker = ({ c, open, onClose, onPick }) => (
  <SW_Sheet c={c} open={open} onClose={onClose} title="Tambah Transaksi" maxHeight="68%">
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10, paddingTop: 4 }}>
      <PickerRow c={c} icon="expense" iconBg={c.dangerSoft} iconColor={c.danger}
        label="Pengeluaran" sub="Catat uang yang keluar — wajib terhubung ke plan item"
        onClick={() => { onClose(); onPick('expense'); }} />
      <PickerRow c={c} icon="income" iconBg={c.successSoft} iconColor={c.success}
        label="Pemasukan" sub="Gaji, bonus, THR, penghasilan sampingan"
        onClick={() => { onClose(); onPick('income'); }} />
      <PickerRow c={c} icon="transfer" iconBg={c.infoSoft} iconColor={c.info}
        label="Transfer" sub="Pindah uang antar akun sendiri"
        onClick={() => { onClose(); onPick('transfer'); }} />
      <div style={{ height: 1, background: c.border, margin: '8px 0' }} />
      <PickerRow c={c} icon="camera" iconBg={c.primaryContainer} iconColor={c.onPrimaryContainer}
        label="Scan Struk (OCR)" sub="Foto struk → otomatis isi draft pengeluaran"
        onClick={() => { onClose(); onPick('expense'); }} />
    </div>
  </SW_Sheet>
);

const PickerRow = ({ c, icon, iconBg, iconColor, label, sub, onClick }) => (
  <button onClick={onClick} className="sw-press" style={{
    display: 'flex', alignItems: 'center', gap: 14,
    padding: '14px 14px',
    background: c.bg, border: `1px solid ${c.border}`,
    borderRadius: 14, cursor: 'pointer', textAlign: 'left',
    fontFamily: SW_TYPE.family,
  }}>
    <div style={{
      width: 44, height: 44, borderRadius: 14,
      background: iconBg, color: iconColor,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      flex: '0 0 auto',
    }}>
      <Icon name={icon} size={22} strokeWidth={1.8} />
    </div>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 15, fontWeight: 700, color: c.ink, marginBottom: 1 }}>{label}</div>
      <div style={{ fontSize: 11, color: c.inkMuted, lineHeight: 1.4 }}>{sub}</div>
    </div>
    <Icon name="chevron_right" size={20} color={c.inkSubtle} />
  </button>
);

// ─── Pengeluaran (expense) form ─────────────────────────────

const ExpenseForm = ({ c, onClose, onSave }) => {
  const [amount, setAmount] = React.useState(28000);
  const [planItem, setPlanItem] = React.useState({ id: 'kopi', name: 'Kopi/Kafe', cat: 'Makan di Luar', alloc: 'Wants' });
  const [account, setAccount] = React.useState(SW_ACCOUNTS[2]); // GoPay
  const [merchant, setMerchant] = React.useState('Kopi Kenangan');
  const [date, setDate] = React.useState(new Date(2026, 4, 15));
  const [note, setNote] = React.useState('');
  const [linkDebt, setLinkDebt] = React.useState(false);
  const [showPlanPicker, setShowPlanPicker] = React.useState(false);
  const [showAcctPicker, setShowAcctPicker] = React.useState(false);
  const [showDatePicker, setShowDatePicker] = React.useState(false);

  const allocOfItem = planItem ? planItem.alloc : null;
  const allocColor = allocOfItem === 'Wants' ? c.accent : allocOfItem === 'Investment' ? c.info : c.primary;
  const heroText = allocOfItem === 'Wants' ? '#0A2820' : '#FFFFFF';

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', animation: 'sw-slide-in 280ms cubic-bezier(.2,.7,.3,1)', background: c.bg }}>
      <SW_TopBar c={c} title="Pengeluaran" onBack={onClose} right={
        <button className="sw-press" style={{
          height: 36, padding: '0 14px',
          background: c.primary, color: c.onPrimary, border: 'none', borderRadius: 10,
          fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 700, cursor: 'pointer',
        }} onClick={onSave}>Simpan</button>
      } />

      <div className="sw-scroll" style={{ flex: 1, overflowY: 'auto', padding: '0 20px 28px' }}>
        {/* Hero amount */}
        <div style={{
          background: allocColor, color: heroText,
          borderRadius: 22, padding: '24px 22px',
          marginBottom: 18, position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -30, bottom: -30, opacity: 0.14 }}>
            <LogoA_Daun theme={{ ...c, primary: heroText, onPrimary: allocColor, accent: c.accent }} size={140} />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase', marginBottom: 6 }}>Jumlah Pengeluaran</div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 4, fontFamily: SW_TYPE.family }}>
              <span style={{ fontSize: 22, fontWeight: 600, opacity: 0.85 }}>Rp</span>
              <input
                inputMode="numeric"
                value={new Intl.NumberFormat('id-ID').format(amount)}
                onChange={(e) => {
                  const n = parseInt(e.target.value.replace(/\D/g, '')) || 0;
                  setAmount(n);
                }}
                style={{
                  flex: 1, minWidth: 0,
                  background: 'transparent', border: 'none', outline: 'none',
                  fontFamily: SW_TYPE.family, fontSize: 44, fontWeight: 800,
                  color: 'inherit', fontVariantNumeric: 'tabular-nums',
                  letterSpacing: '-0.025em', padding: 0, lineHeight: 1.05,
                }}
              />
            </div>
            {allocOfItem && (
              <div style={{ fontSize: 12, opacity: 0.75, marginTop: 6, fontWeight: 500 }}>
                Alokasi <strong style={{ fontWeight: 700 }}>{allocOfItem}</strong>
              </div>
            )}
          </div>
        </div>

        {/* Plan item — required */}
        <FieldButton c={c} label="Plan Item" required onClick={() => setShowPlanPicker(true)}>
          {planItem ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1, minWidth: 0 }}>
              <SW_CategoryDot c={c} name={planItem.name} color={allocColor} size={32} />
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontSize: 15, fontWeight: 600, color: c.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{planItem.name}</div>
                <div style={{ fontSize: 11, color: c.inkMuted }}>{planItem.cat} · {planItem.alloc}</div>
              </div>
            </div>
          ) : (
            <span style={{ color: c.inkMuted, fontSize: 15 }}>Pilih plan item</span>
          )}
        </FieldButton>

        {/* Merchant */}
        <SW_Field c={c} label="Toko / Keterangan" value={merchant} onChange={setMerchant} placeholder="Misal: Kopi Kenangan" />

        {/* Akun */}
        <FieldButton c={c} label="Akun" required onClick={() => setShowAcctPicker(true)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1, minWidth: 0 }}>
            <SW_AccountIcon c={c} account={account} size={32} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 15, fontWeight: 600, color: c.ink }}>{account.name}</div>
              <div style={{ fontSize: 11, color: c.inkMuted, fontVariantNumeric: 'tabular-nums' }}>Saldo: {SW_FORMAT.rp(account.balance)}</div>
            </div>
          </div>
        </FieldButton>

        {/* Date */}
        <FieldButton c={c} label="Tanggal" onClick={() => setShowDatePicker(true)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
            <div style={{ width: 32, height: 32, borderRadius: 10, background: c.primaryContainer, color: c.onPrimaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Icon name="calendar" size={16} />
            </div>
            <div>
              <div style={{ fontSize: 15, fontWeight: 600, color: c.ink, whiteSpace: 'nowrap' }}>{SW_FORMAT.date(date)}</div>
              <div style={{ fontSize: 11, color: c.inkMuted, whiteSpace: 'nowrap' }}>{SW_FORMAT.dateRel(date)}</div>
            </div>
          </div>
        </FieldButton>

        {/* Catatan */}
        <SW_Field c={c} label="Catatan (opsional)" value={note} onChange={setNote} placeholder="Tambah catatan..." />

        {/* Photo struk */}
        <div style={{ marginBottom: 14 }}>
          <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 6 }}>Foto struk (opsional)</div>
          <button className="sw-press" style={{
            width: '100%', height: 88,
            background: c.surface, border: `1.5px dashed ${c.borderStrong}`,
            borderRadius: 12, cursor: 'pointer',
            display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            gap: 6, color: c.inkMuted, fontFamily: SW_TYPE.family,
          }}>
            <Icon name="camera" size={22} />
            <span style={{ fontSize: 12, fontWeight: 600 }}>Ambil foto · OCR otomatis</span>
          </button>
        </div>

        {/* Link debt toggle */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 12,
          padding: '12px 14px',
          background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 12, marginBottom: 14,
        }}>
          <div style={{ width: 36, height: 36, borderRadius: 10, background: c.warningSoft, color: c.warning, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="link" size={18} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>Tautkan ke hutang</div>
            <div style={{ fontSize: 11, color: c.inkMuted }}>Cicilan / pembayaran utang yang sudah ada</div>
          </div>
          <SW_Toggle c={c} value={linkDebt} onChange={setLinkDebt} />
        </div>

        {/* Save (mobile-style) */}
        <SW_Button c={c} onClick={onSave} size="lg" icon="check">Simpan Pengeluaran</SW_Button>
      </div>

      {/* Plan item picker sheet */}
      <SW_Sheet c={c} open={showPlanPicker} onClose={() => setShowPlanPicker(false)} title="Pilih Plan Item">
        {SW_ALLOCATIONS.map(alloc => (
          <div key={alloc.id} style={{ marginBottom: 14 }}>
            <SW_SectionLabel c={c}>{alloc.name}</SW_SectionLabel>
            {alloc.categories.flatMap(cat =>
              cat.items.map(item => (
                <button
                  key={item.id}
                  onClick={() => {
                    setPlanItem({ id: item.id, name: item.name, cat: cat.name, alloc: alloc.name });
                    setShowPlanPicker(false);
                  }}
                  className="sw-press"
                  style={{
                    width: '100%', textAlign: 'left',
                    display: 'flex', alignItems: 'center', gap: 12,
                    padding: '10px 12px', marginBottom: 4,
                    background: planItem?.id === item.id ? c.primaryContainer : c.bg,
                    border: 'none', borderRadius: 10, cursor: 'pointer',
                    fontFamily: SW_TYPE.family,
                  }}
                >
                  <SW_CategoryDot c={c} name={item.name} size={28} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>{item.name}</div>
                    <div style={{ fontSize: 11, color: c.inkMuted }}>{cat.name}</div>
                  </div>
                  <span style={{ fontSize: 11, color: c.inkSubtle, fontVariantNumeric: 'tabular-nums' }}>
                    {SW_FORMAT.rpShort(item.used)}/{SW_FORMAT.rpShort(item.plan)}
                  </span>
                </button>
              ))
            )}
          </div>
        ))}
      </SW_Sheet>

      {/* Account picker sheet */}
      <SW_Sheet c={c} open={showAcctPicker} onClose={() => setShowAcctPicker(false)} title="Pilih Akun" maxHeight="60%">
        {SW_ACCOUNTS.map(a => (
          <button key={a.id} onClick={() => { setAccount(a); setShowAcctPicker(false); }} className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 14px', marginBottom: 6,
            background: account.id === a.id ? c.primaryContainer : c.bg,
            border: 'none', borderRadius: 12, cursor: 'pointer', fontFamily: SW_TYPE.family,
          }}>
            <SW_AccountIcon c={c} account={a} size={40} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>{a.name}</div>
              <div style={{ fontSize: 11, color: c.inkMuted }}>{a.type}</div>
            </div>
            <div style={{ fontSize: 14, fontWeight: 700, color: c.primary, fontVariantNumeric: 'tabular-nums' }}>
              {SW_FORMAT.rpShort(a.balance)}
            </div>
          </button>
        ))}
      </SW_Sheet>

      {/* Date picker — simplified */}
      <SW_Sheet c={c} open={showDatePicker} onClose={() => setShowDatePicker(false)} title="Pilih Tanggal" maxHeight="50%">
        <DatePickerCompact c={c} value={date} onChange={(d) => { setDate(d); setShowDatePicker(false); }} />
      </SW_Sheet>
    </div>
  );
};

const FieldButton = ({ c, label, required, onClick, children }) => (
  <div style={{ marginBottom: 14 }}>
    <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 6 }}>
      {label}{required && <span style={{ color: c.danger, marginLeft: 4 }}>*</span>}
    </div>
    <button onClick={onClick} className="sw-press" style={{
      width: '100%', textAlign: 'left',
      display: 'flex', alignItems: 'center', gap: 8,
      background: c.surface, border: `1.5px solid ${c.border}`,
      borderRadius: 12, padding: '10px 14px', minHeight: 56, cursor: 'pointer',
      fontFamily: SW_TYPE.family,
    }}>
      <div style={{ flex: 1, display: 'flex', alignItems: 'center' }}>{children}</div>
      <Icon name="chevron_right" size={18} color={c.inkSubtle} />
    </button>
  </div>
);

const SW_Toggle = ({ c, value, onChange }) => (
  <button onClick={() => onChange(!value)} className="sw-press" style={{
    width: 44, height: 26, borderRadius: 13,
    background: value ? c.primary : c.borderStrong,
    border: 'none', cursor: 'pointer', position: 'relative',
    transition: 'background 200ms ease',
  }}>
    <span style={{
      position: 'absolute', top: 3, left: value ? 21 : 3,
      width: 20, height: 20, borderRadius: '50%',
      background: '#fff',
      transition: 'left 180ms cubic-bezier(.2,.7,.3,1)',
      boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
    }} />
  </button>
);

// Compact date picker — last 14 days as chips + custom
const DatePickerCompact = ({ c, value, onChange }) => {
  const days = [];
  const today = new Date(2026, 4, 15);
  for (let i = 0; i < 14; i++) {
    const d = new Date(today); d.setDate(d.getDate() - i);
    days.push(d);
  }
  const sameDay = (a, b) => a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  return (
    <div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 6, marginBottom: 12 }}>
        {days.map((d, i) => {
          const active = sameDay(d, value);
          return (
            <button key={i} onClick={() => onChange(d)} className="sw-press" style={{
              padding: '8px 4px',
              background: active ? c.primary : c.bg,
              color: active ? c.onPrimary : c.ink,
              border: 'none', borderRadius: 10, cursor: 'pointer',
              fontFamily: SW_TYPE.family,
            }}>
              <div style={{ fontSize: 10, opacity: 0.7, fontWeight: 500 }}>
                {['M','S','S','R','K','J','S'][d.getDay()]}
              </div>
              <div style={{ fontSize: 16, fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>{d.getDate()}</div>
            </button>
          );
        })}
      </div>
      <SW_Button c={c} variant="outline" icon="calendar">Pilih tanggal lain…</SW_Button>
    </div>
  );
};

Object.assign(window, { AddTxnPicker, ExpenseForm, FieldButton, SW_Toggle, DatePickerCompact });
