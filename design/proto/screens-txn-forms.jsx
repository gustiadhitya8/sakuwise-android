// Sakuwise — Pemasukan (income) + Transfer forms

// ─── Pemasukan form ─────────────────────────────────────────

const IncomeForm = ({ c, onClose, onSave }) => {
  const [amount, setAmount] = React.useState(15000000);
  const [category, setCategory] = React.useState({ id: 'salary', name: 'Gaji Pokok' });
  const [account, setAccount] = React.useState(SW_ACCOUNTS[0]); // Mandiri
  const [source, setSource] = React.useState('PT Sumber Karya');
  const [date, setDate] = React.useState(new Date(2026, 4, 25));
  const [note, setNote] = React.useState('');
  const [recurring, setRecurring] = React.useState(true);
  const [showCatPicker, setShowCatPicker] = React.useState(false);
  const [showAcctPicker, setShowAcctPicker] = React.useState(false);
  const [showDatePicker, setShowDatePicker] = React.useState(false);

  const categories = [
    { id: 'salary',  name: 'Gaji Pokok',          icon: 'income' },
    { id: 'bonus',   name: 'Bonus',                icon: 'sparkle' },
    { id: 'thr',     name: 'THR',                  icon: 'sparkle' },
    { id: 'side',    name: 'Penghasilan Sampingan',icon: 'arrow_up_right' },
    { id: 'other',   name: 'Lainnya',              icon: 'income' },
  ];

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', animation: 'sw-slide-in 280ms cubic-bezier(.2,.7,.3,1)', background: c.bg }}>
      <SW_TopBar c={c} title="Pemasukan" onBack={onClose} right={
        <button className="sw-press" style={{
          height: 36, padding: '0 14px',
          background: c.primary, color: c.onPrimary, border: 'none', borderRadius: 10,
          fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 700, cursor: 'pointer',
        }} onClick={onSave}>Simpan</button>
      } />

      <div className="sw-scroll" style={{ flex: 1, overflowY: 'auto', padding: '0 20px 28px' }}>
        {/* Hero amount — success green */}
        <div style={{
          background: c.success, color: '#fff',
          borderRadius: 22, padding: '24px 22px',
          marginBottom: 18, position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -30, bottom: -30, opacity: 0.14 }}>
            <Icon name="income" size={160} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase', marginBottom: 6 }}>Jumlah Pemasukan</div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 4, fontFamily: SW_TYPE.family }}>
              <span style={{ fontSize: 22, fontWeight: 600, opacity: 0.85 }}>+ Rp</span>
              <input
                inputMode="numeric"
                value={new Intl.NumberFormat('id-ID').format(amount)}
                onChange={(e) => setAmount(parseInt(e.target.value.replace(/\D/g, '')) || 0)}
                style={{
                  flex: 1, minWidth: 0,
                  background: 'transparent', border: 'none', outline: 'none',
                  fontFamily: SW_TYPE.family, fontSize: 40, fontWeight: 800,
                  color: 'inherit', fontVariantNumeric: 'tabular-nums',
                  letterSpacing: '-0.025em', padding: 0, lineHeight: 1.05,
                }}
              />
            </div>
            <div style={{ fontSize: 12, opacity: 0.78, marginTop: 6 }}>
              ke akun <strong style={{ fontWeight: 700 }}>{account.name}</strong>
            </div>
          </div>
        </div>

        <FieldButton c={c} label="Kategori sumber" required onClick={() => setShowCatPicker(true)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
            <div style={{ width: 32, height: 32, borderRadius: 10, background: c.successSoft, color: c.success, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Icon name={category.icon || 'income'} size={16} />
            </div>
            <span style={{ fontSize: 15, fontWeight: 600, color: c.ink }}>{category.name}</span>
          </div>
        </FieldButton>

        <SW_Field c={c} label="Dari (sumber)" value={source} onChange={setSource} placeholder="Mis. PT Sumber Karya" />

        <FieldButton c={c} label="Akun tujuan" required onClick={() => setShowAcctPicker(true)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
            <SW_AccountIcon c={c} account={account} size={32} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 15, fontWeight: 600, color: c.ink }}>{account.name}</div>
              <div style={{ fontSize: 11, color: c.inkMuted, fontVariantNumeric: 'tabular-nums' }}>Saldo: {SW_FORMAT.rp(account.balance)}</div>
            </div>
          </div>
        </FieldButton>

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

        <SW_Field c={c} label="Catatan (opsional)" value={note} onChange={setNote} placeholder="Tambah catatan..." />

        {/* Recurring toggle */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 12,
          padding: '12px 14px',
          background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 12, marginBottom: 14,
        }}>
          <div style={{ width: 36, height: 36, borderRadius: 10, background: c.primaryContainer, color: c.onPrimaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="sparkle" size={18} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>Pemasukan berulang</div>
            <div style={{ fontSize: 11, color: c.inkMuted }}>Auto-suggest di plan bulan depan</div>
          </div>
          <SW_Toggle c={c} value={recurring} onChange={setRecurring} />
        </div>

        <SW_Button c={c} onClick={onSave} size="lg" icon="check">Simpan Pemasukan</SW_Button>
      </div>

      {/* Pickers */}
      <SW_Sheet c={c} open={showCatPicker} onClose={() => setShowCatPicker(false)} title="Kategori Sumber" maxHeight="60%">
        {categories.map(cat => (
          <button key={cat.id} onClick={() => { setCategory(cat); setShowCatPicker(false); }} className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 14px', marginBottom: 6,
            background: category.id === cat.id ? c.primaryContainer : c.bg,
            border: 'none', borderRadius: 12, cursor: 'pointer', fontFamily: SW_TYPE.family,
          }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: c.successSoft, color: c.success, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Icon name={cat.icon} size={18} />
            </div>
            <span style={{ fontSize: 14, fontWeight: 600, color: c.ink, flex: 1 }}>{cat.name}</span>
            {category.id === cat.id && <Icon name="check" size={18} color={c.primary} strokeWidth={2.5} />}
          </button>
        ))}
      </SW_Sheet>

      <SW_Sheet c={c} open={showAcctPicker} onClose={() => setShowAcctPicker(false)} title="Pilih Akun Tujuan" maxHeight="60%">
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

      <SW_Sheet c={c} open={showDatePicker} onClose={() => setShowDatePicker(false)} title="Pilih Tanggal" maxHeight="50%">
        <DatePickerCompact c={c} value={date} onChange={(d) => { setDate(d); setShowDatePicker(false); }} />
      </SW_Sheet>
    </div>
  );
};

// ─── Transfer form ──────────────────────────────────────────

const TransferForm = ({ c, onClose, onSave }) => {
  const [amount, setAmount] = React.useState(2000000);
  const [fee, setFee] = React.useState(0);
  const [from, setFrom] = React.useState(SW_ACCOUNTS[1]); // BCA
  const [to, setTo] = React.useState(SW_ACCOUNTS[0]);     // Mandiri
  const [date, setDate] = React.useState(new Date(2026, 4, 15));
  const [note, setNote] = React.useState('');
  const [showFromPicker, setShowFromPicker] = React.useState(false);
  const [showToPicker, setShowToPicker] = React.useState(false);
  const [showDatePicker, setShowDatePicker] = React.useState(false);

  const swap = () => { setFrom(to); setTo(from); };

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', animation: 'sw-slide-in 280ms cubic-bezier(.2,.7,.3,1)', background: c.bg }}>
      <SW_TopBar c={c} title="Transfer" onBack={onClose} right={
        <button className="sw-press" style={{
          height: 36, padding: '0 14px',
          background: c.primary, color: c.onPrimary, border: 'none', borderRadius: 10,
          fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 700, cursor: 'pointer',
        }} onClick={onSave}>Simpan</button>
      } />

      <div className="sw-scroll" style={{ flex: 1, overflowY: 'auto', padding: '0 20px 28px' }}>
        {/* Hero amount — info blue */}
        <div style={{
          background: c.info, color: '#fff',
          borderRadius: 22, padding: '24px 22px',
          marginBottom: 18, position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -30, bottom: -30, opacity: 0.16 }}>
            <Icon name="transfer" size={160} color="#fff" />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.78, textTransform: 'uppercase', marginBottom: 6 }}>Jumlah Transfer</div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 4, fontFamily: SW_TYPE.family }}>
              <span style={{ fontSize: 22, fontWeight: 600, opacity: 0.85 }}>Rp</span>
              <input
                inputMode="numeric"
                value={new Intl.NumberFormat('id-ID').format(amount)}
                onChange={(e) => setAmount(parseInt(e.target.value.replace(/\D/g, '')) || 0)}
                style={{
                  flex: 1, minWidth: 0,
                  background: 'transparent', border: 'none', outline: 'none',
                  fontFamily: SW_TYPE.family, fontSize: 40, fontWeight: 800,
                  color: 'inherit', fontVariantNumeric: 'tabular-nums',
                  letterSpacing: '-0.025em', padding: 0, lineHeight: 1.05,
                }}
              />
            </div>
            <div style={{ fontSize: 12, opacity: 0.78, marginTop: 6 }}>
              {from.name} <span style={{ opacity: 0.6 }}>→</span> {to.name}
              {fee > 0 && <span> · biaya {SW_FORMAT.rpShort(fee)}</span>}
            </div>
          </div>
        </div>

        {/* From → To swappable */}
        <div style={{ position: 'relative', marginBottom: 14 }}>
          <FieldButton c={c} label="Dari akun" required onClick={() => setShowFromPicker(true)}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
              <SW_AccountIcon c={c} account={from} size={32} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 15, fontWeight: 600, color: c.ink }}>{from.name}</div>
                <div style={{ fontSize: 11, color: c.inkMuted, fontVariantNumeric: 'tabular-nums' }}>{SW_FORMAT.rp(from.balance)}</div>
              </div>
            </div>
          </FieldButton>

          <FieldButton c={c} label="Ke akun" required onClick={() => setShowToPicker(true)}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
              <SW_AccountIcon c={c} account={to} size={32} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 15, fontWeight: 600, color: c.ink }}>{to.name}</div>
                <div style={{ fontSize: 11, color: c.inkMuted, fontVariantNumeric: 'tabular-nums' }}>{SW_FORMAT.rp(to.balance)}</div>
              </div>
            </div>
          </FieldButton>

          <button onClick={swap} className="sw-press" aria-label="Tukar arah" style={{
            position: 'absolute', right: 18, top: '50%', marginTop: -18,
            width: 36, height: 36, borderRadius: '50%',
            background: c.surface, border: `1.5px solid ${c.border}`,
            color: c.ink, cursor: 'pointer', boxShadow: '0 2px 6px rgba(0,0,0,0.05)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="swap" size={18} />
          </button>
        </div>

        <SW_Field c={c} label="Biaya transfer (opsional)" value={fee === 0 ? '' : new Intl.NumberFormat('id-ID').format(fee)}
          onChange={(v) => setFee(parseInt(v.replace(/\D/g, '')) || 0)}
          prefix="Rp" placeholder="0"
          hint={fee > 0 ? 'Biaya dianggap pengeluaran (default: "Biaya Transfer")' : null} />

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

        <SW_Field c={c} label="Catatan (opsional)" value={note} onChange={setNote} placeholder="Tambah catatan..." />

        {/* Summary */}
        <div style={{
          padding: '12px 14px',
          background: c.infoSoft, border: `1px solid ${c.info}33`,
          borderRadius: 12, marginBottom: 14, fontSize: 12, color: c.inkMuted, lineHeight: 1.5,
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 2 }}>
            <span>{from.name} <strong style={{ color: c.danger }}>−</strong></span>
            <strong style={{ color: c.ink, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>{SW_FORMAT.rp(amount + fee)}</strong>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>{to.name} <strong style={{ color: c.success }}>+</strong></span>
            <strong style={{ color: c.ink, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>{SW_FORMAT.rp(amount)}</strong>
          </div>
        </div>

        <SW_Button c={c} onClick={onSave} size="lg" icon="check">Simpan Transfer</SW_Button>
      </div>

      {/* Pickers */}
      <SW_Sheet c={c} open={showFromPicker} onClose={() => setShowFromPicker(false)} title="Pilih Akun Sumber" maxHeight="60%">
        {SW_ACCOUNTS.map(a => (
          <button key={a.id} onClick={() => { setFrom(a); setShowFromPicker(false); }} className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 14px', marginBottom: 6,
            background: from.id === a.id ? c.primaryContainer : c.bg,
            border: 'none', borderRadius: 12, cursor: a.id === to.id ? 'not-allowed' : 'pointer', fontFamily: SW_TYPE.family,
            opacity: a.id === to.id ? 0.4 : 1,
          }} disabled={a.id === to.id}>
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

      <SW_Sheet c={c} open={showToPicker} onClose={() => setShowToPicker(false)} title="Pilih Akun Tujuan" maxHeight="60%">
        {SW_ACCOUNTS.map(a => (
          <button key={a.id} onClick={() => { setTo(a); setShowToPicker(false); }} className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 14px', marginBottom: 6,
            background: to.id === a.id ? c.primaryContainer : c.bg,
            border: 'none', borderRadius: 12, cursor: a.id === from.id ? 'not-allowed' : 'pointer', fontFamily: SW_TYPE.family,
            opacity: a.id === from.id ? 0.4 : 1,
          }} disabled={a.id === from.id}>
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

      <SW_Sheet c={c} open={showDatePicker} onClose={() => setShowDatePicker(false)} title="Pilih Tanggal" maxHeight="50%">
        <DatePickerCompact c={c} value={date} onChange={(d) => { setDate(d); setShowDatePicker(false); }} />
      </SW_Sheet>
    </div>
  );
};

Object.assign(window, { IncomeForm, TransferForm });
