// Sakuwise — Settings (Saya tab) lengkap dengan sub-pages

// ─── Settings Hub ───────────────────────────────────────────

const SettingsHub = ({ c, onNav, setSub, sub, onStartOnboarding }) => {
  if (sub === 'profile')         return <ProfileSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'allocation')      return <AllocationEditor c={c} onBack={() => setSub(null)} />;
  if (sub === 'gold-price')      return <GoldPriceSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'auto-lock')       return <AutoLockSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'period-start')    return <PeriodStartSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'backup')          return <BackupSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'pin')             return <PinSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'language')        return <LanguageSettings c={c} onBack={() => setSub(null)} />;
  if (sub === 'about')           return <AboutScreen c={c} onBack={() => setSub(null)} />;
  if (sub === 'donate')          return <DonateScreen c={c} onBack={() => setSub(null)} />;
  if (sub === 'export-reset')    return <ExportResetSettings c={c} onBack={() => setSub(null)} />;

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Saya" />

      {/* Profile card */}
      <div style={{ padding: '0 20px 20px' }}>
        <button onClick={() => setSub('profile')} className="sw-press" style={{
          width: '100%', textAlign: 'left',
          display: 'flex', alignItems: 'center', gap: 14,
          padding: '18px',
          background: c.primary, color: c.onPrimary, border: 'none',
          borderRadius: 18, cursor: 'pointer', fontFamily: SW_TYPE.family,
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', right: -30, bottom: -30, opacity: 0.12 }}>
            <LogoA_Daun theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={120} />
          </div>
          <div style={{
            width: 56, height: 56, borderRadius: '50%',
            background: 'rgba(255,255,255,0.18)', color: c.onPrimary,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 22, fontWeight: 700, flex: '0 0 auto',
          }}>G</div>
          <div style={{ flex: 1, minWidth: 0, position: 'relative' }}>
            <div style={{ fontSize: 18, fontWeight: 700, letterSpacing: '-0.01em' }}>Gusti</div>
            <div style={{ fontSize: 12, opacity: 0.78, marginTop: 2 }}>Bahasa Indonesia · Biometrik aktif</div>
          </div>
          <Icon name="chevron_right" size={20} color={c.onPrimary} />
        </button>
      </div>

      {/* Sections */}
      <SettingsGroup c={c} label="Plan">
        <SettingsRow c={c} icon="plan"    label="Default Alokasi" value="50 · 30 · 20"  sub="Template untuk plan baru" onClick={() => setSub('allocation')} />
        <SettingsRow c={c} icon="calendar" label="Tanggal Mulai Periode" value="Tanggal 1" onClick={() => setSub('period-start')} />
      </SettingsGroup>

      <SettingsGroup c={c} label="Keamanan">
        <SettingsRow c={c} icon="shield" label="PIN & Biometrik" value="Aktif" onClick={() => setSub('pin')} />
        <SettingsRow c={c} icon="eye_off" label="Auto-lock" value="5 menit" onClick={() => setSub('auto-lock')} />
      </SettingsGroup>

      <SettingsGroup c={c} label="Backup & Data">
        <SettingsRow c={c} icon="copy" label="Backup & Pemulihan" value="34 hari lalu" warning onClick={() => setSub('backup')} />
        <SettingsRow c={c} icon="trash" label="Export & Reset" sub="Hapus semua data, mulai dari nol" danger onClick={() => setSub('export-reset')} />
      </SettingsGroup>

      <SettingsGroup c={c} label="Aplikasi">
        <SettingsRow c={c} icon="me"      label="Bahasa" value="Bahasa Indonesia" onClick={() => setSub('language')} />
        <SettingsRow c={c} icon="leaf"    label="Lihat onboarding lagi" onClick={onStartOnboarding} />
        <SettingsRow c={c} icon="sparkle" label="Donasi" sub="Dukung pengembangan Sakuwise" onClick={() => setSub('donate')} />
        <SettingsRow c={c} icon="info"    label="Tentang Sakuwise" value="v1.0" onClick={() => setSub('about')} />
      </SettingsGroup>

      <div style={{ padding: '8px 24px 20px', fontSize: 11, color: c.inkSubtle, textAlign: 'center', lineHeight: 1.5 }}>
        Sakuwise · v1.0 · Local-first · Tanpa telemetry<br/>
        Made in Indonesia 🌱
      </div>
    </div>
  );
};

const SettingsGroup = ({ c, label, children }) => (
  <div style={{ padding: '0 20px 14px' }}>
    <div style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', margin: '4px 4px 8px' }}>{label}</div>
    <SW_Card c={c} padding={0}>
      {React.Children.toArray(children).map((child, i, arr) =>
        React.cloneElement(child, { last: i === arr.length - 1 })
      )}
    </SW_Card>
  </div>
);

const SettingsRow = ({ c, icon, label, value, sub, onClick, danger, warning, last }) => (
  <button onClick={onClick} className="sw-press" style={{
    width: '100%', textAlign: 'left',
    display: 'flex', alignItems: 'center', gap: 12,
    padding: '14px 16px',
    background: 'transparent', border: 'none', cursor: 'pointer',
    borderBottom: last ? 'none' : `1px solid ${c.border}`,
    fontFamily: SW_TYPE.family,
  }}>
    <div style={{
      width: 36, height: 36, borderRadius: 11,
      background: danger ? c.dangerSoft : warning ? c.warningSoft : c.primaryContainer,
      color: danger ? c.danger : warning ? c.warning : c.onPrimaryContainer,
      display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
    }}>
      <Icon name={icon} size={18} />
    </div>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 14, fontWeight: 600, color: danger ? c.danger : c.ink }}>{label}</div>
      {sub && <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>{sub}</div>}
    </div>
    {value && <span style={{ fontSize: 12, color: warning ? c.warning : c.inkMuted, fontWeight: 600, whiteSpace: 'nowrap' }}>{value}</span>}
    <Icon name="chevron_right" size={18} color={c.inkSubtle} />
  </button>
);

// ─── Settings sub-screens ────────────────────────────────────

const SimpleSettingsScreen = ({ c, title, onBack, intro, children }) => (
  <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
    <SW_TopBar c={c} title={title} onBack={onBack} />
    {intro && (
      <div style={{ padding: '0 20px 14px', fontSize: 13, color: c.inkMuted, lineHeight: 1.5 }}>{intro}</div>
    )}
    <div style={{ padding: '0 20px' }}>{children}</div>
  </div>
);

const ProfileSettings = ({ c, onBack }) => {
  const [nickname, setNickname] = React.useState('Gusti');
  return (
    <SimpleSettingsScreen c={c} title="Profil" onBack={onBack}
      intro="Nama panggilan ini cuma untuk sapaan di Beranda. Sakuwise nggak menyimpan email atau identitas lain.">
      <SW_Field c={c} label="Nama panggilan" value={nickname} onChange={setNickname} />
      <SW_Button c={c} onClick={onBack} size="lg" icon="check">Simpan</SW_Button>
    </SimpleSettingsScreen>
  );
};

const AllocationEditor = ({ c, onBack }) => {
  const [pcts, setPcts] = React.useState({ needs: 50, wants: 30, invest: 20 });
  const total = pcts.needs + pcts.wants + pcts.invest;
  const valid = total === 100;
  return (
    <SimpleSettingsScreen c={c} title="Persentase Alokasi" onBack={onBack}
      intro="Default 50 · 30 · 20 (Needs · Wants · Investment). Total harus 100%. Berlaku untuk plan baru, tidak mengubah plan yang sudah ada.">
      <SW_Card c={c} padding={20} style={{ marginBottom: 14 }}>
        <AllocSlider c={c} label="Needs" tint={c.primary} value={pcts.needs} onChange={(v) => setPcts({ ...pcts, needs: v })} />
        <AllocSlider c={c} label="Wants" tint={c.accent} value={pcts.wants} onChange={(v) => setPcts({ ...pcts, wants: v })} />
        <AllocSlider c={c} label="Investment" tint={c.info} value={pcts.invest} onChange={(v) => setPcts({ ...pcts, invest: v })} />
        <div style={{ marginTop: 14, padding: '10px 12px', background: valid ? c.successSoft : c.dangerSoft, color: valid ? c.success : c.danger, borderRadius: 10, fontSize: 13, fontWeight: 700, textAlign: 'center' }}>
          Total: {total}% {valid ? '✓' : `(${total > 100 ? 'lebih ' : 'kurang '}${Math.abs(100 - total)}%)`}
        </div>
      </SW_Card>
      <SW_Button c={c} onClick={onBack} size="lg" icon="check" disabled={!valid}>Simpan</SW_Button>
    </SimpleSettingsScreen>
  );
};

const AllocSlider = ({ c, label, tint, value, onChange }) => (
  <div style={{ marginBottom: 14 }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 6 }}>
      <span style={{ fontSize: 13, fontWeight: 700, color: c.ink, display: 'inline-flex', alignItems: 'center', gap: 8 }}>
        <span style={{ width: 8, height: 8, borderRadius: '50%', background: tint }} /> {label}
      </span>
      <span style={{ fontSize: 16, fontWeight: 700, color: c.ink, fontVariantNumeric: 'tabular-nums' }}>{value}%</span>
    </div>
    <input type="range" min={0} max={100} step={5} value={value} onChange={(e) => onChange(parseInt(e.target.value))}
      style={{ width: '100%', accentColor: tint }} />
  </div>
);

const GoldPriceSettings = ({ c, onBack }) => {
  const [price, setPrice] = React.useState(1050000);
  return (
    <SimpleSettingsScreen c={c} title="Harga Emas Global" onBack={onBack}
      intro="Update manual sesuai harga jual emas hari ini (Antam, Pegadaian, dll). Auto-fetch akan tersedia di V2.">
      <SW_Field c={c} label="Harga per gram"
        value={new Intl.NumberFormat('id-ID').format(price)}
        onChange={(v) => setPrice(parseInt(v.replace(/\D/g, '')) || 0)}
        prefix="Rp" suffix="/ gram" />
      <div style={{ padding: '12px 14px', background: c.warningSoft, border: `1px solid ${c.warning}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5, marginBottom: 14 }}>
        <strong style={{ color: c.ink }}>Tip:</strong> Update bulanan saja sudah cukup. Harga emas tidak fluktuasi liar harian, dan Sakuwise menyimpan harga ini sebagai snapshot — bisa di-recall kapan saja.
      </div>
      <SW_Button c={c} onClick={onBack} size="lg" icon="check">Simpan</SW_Button>
    </SimpleSettingsScreen>
  );
};

const AutoLockSettings = ({ c, onBack }) => {
  const [duration, setDuration] = React.useState('5m');
  const opts = [
    { id: 'immediate', label: 'Langsung', sub: 'Lock saat aplikasi pindah ke background' },
    { id: '1m',  label: '1 menit',  sub: 'Cocok kalau sering kerja sambil cek HP' },
    { id: '5m',  label: '5 menit',  sub: 'Default — saldo keseimbangan privacy & kenyamanan' },
    { id: '15m', label: '15 menit', sub: 'Lebih jarang prompt PIN/biometrik' },
    { id: '30m', label: '30 menit', sub: 'Untuk pemakaian intensif sesi panjang' },
  ];
  return (
    <SimpleSettingsScreen c={c} title="Auto-lock" onBack={onBack}
      intro="Setelah berapa lama aplikasi mengunci diri sendiri. Buka pakai PIN atau biometrik.">
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {opts.map(o => (
          <button key={o.id} onClick={() => setDuration(o.id)} className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '14px 16px',
            background: duration === o.id ? c.primaryContainer : c.surface,
            border: `1.5px solid ${duration === o.id ? c.primary : c.border}`,
            borderRadius: 14, cursor: 'pointer', fontFamily: SW_TYPE.family,
          }}>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>{o.label}</div>
              <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>{o.sub}</div>
            </div>
            {duration === o.id && (
              <div style={{ width: 22, height: 22, borderRadius: '50%', background: c.primary, color: c.onPrimary, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Icon name="check" size={12} strokeWidth={3} />
              </div>
            )}
          </button>
        ))}
      </div>
    </SimpleSettingsScreen>
  );
};

const PeriodStartSettings = ({ c, onBack }) => {
  const [day, setDay] = React.useState(1);
  return (
    <SimpleSettingsScreen c={c} title="Tanggal Mulai Periode" onBack={onBack}
      intro="Tanggal berapa tiap bulan sebuah plan-bulanan dimulai. Default tanggal 1 (mengikuti kalender). Atur ke 25 kalau gajian tanggal 25 dan mau Plan Mei berarti 25 April–24 Mei.">
      <SW_Card c={c} padding={16} style={{ marginBottom: 14 }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 6 }}>
          {Array.from({ length: 28 }).map((_, i) => {
            const d = i + 1;
            const active = day === d;
            return (
              <button key={d} onClick={() => setDay(d)} className="sw-press" style={{
                aspectRatio: '1 / 1',
                background: active ? c.primary : c.bg,
                color: active ? c.onPrimary : c.ink,
                border: `1px solid ${active ? c.primary : c.border}`,
                borderRadius: 10, cursor: 'pointer',
                fontFamily: SW_TYPE.family, fontSize: 14, fontWeight: 700,
                fontVariantNumeric: 'tabular-nums',
              }}>{d}</button>
            );
          })}
        </div>
      </SW_Card>
      <div style={{ padding: '12px 14px', background: c.infoSoft, border: `1px solid ${c.info}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5, marginBottom: 14 }}>
        Plan dengan tanggal mulai = {day} akan dilabel sesuai bulan akhir. Contoh: kalau set 25, "Plan Mei 2026" = 25 April – 24 Mei 2026. Bulan Februari fallback ke hari terakhir kalau {day} &gt; 28.
      </div>
      <SW_Button c={c} onClick={onBack} size="lg" icon="check">Simpan</SW_Button>
    </SimpleSettingsScreen>
  );
};

const PinSettings = ({ c, onBack }) => (
  <SimpleSettingsScreen c={c} title="PIN & Biometrik" onBack={onBack}>
    <SW_Card c={c} padding={0} style={{ marginBottom: 14 }}>
      <SettingsRow c={c} icon="shield" label="Buka pakai biometrik" value="Aktif" />
      <SettingsRow c={c} icon="me"     label="Ubah PIN device"     value="6 digit" last />
    </SW_Card>
    <div style={{ padding: '12px 14px', background: c.infoSoft, border: `1px solid ${c.info}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5 }}>
      PIN device dipakai untuk buka aplikasi sehari-hari. PIN backup terpisah dipakai cuma saat export file backup — bisa diubah di Backup & Pemulihan.
    </div>
  </SimpleSettingsScreen>
);

const LanguageSettings = ({ c, onBack }) => {
  const [lang, setLang] = React.useState('id');
  return (
    <SimpleSettingsScreen c={c} title="Bahasa" onBack={onBack}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {[
          { id: 'id', label: 'Bahasa Indonesia', sub: 'Default' },
          { id: 'en', label: 'English', sub: 'Available too' },
        ].map(l => (
          <button key={l.id} onClick={() => setLang(l.id)} className="sw-press" style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '14px 16px',
            background: lang === l.id ? c.primaryContainer : c.surface,
            border: `1.5px solid ${lang === l.id ? c.primary : c.border}`,
            borderRadius: 14, cursor: 'pointer', textAlign: 'left',
            fontFamily: SW_TYPE.family,
          }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>{l.label}</div>
              <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>{l.sub}</div>
            </div>
            {lang === l.id && (
              <div style={{ width: 22, height: 22, borderRadius: '50%', background: c.primary, color: c.onPrimary, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Icon name="check" size={12} strokeWidth={3} />
              </div>
            )}
          </button>
        ))}
      </div>
    </SimpleSettingsScreen>
  );
};

const AboutScreen = ({ c, onBack }) => (
  <SimpleSettingsScreen c={c} title="Tentang Sakuwise" onBack={onBack}>
    <div style={{ textAlign: 'center', padding: '20px 0 30px' }}>
      <LogoA_Daun theme={c} size={80} />
      <div style={{ fontSize: 22, fontWeight: 800, color: c.ink, marginTop: 14, letterSpacing: '-0.02em' }}>Sakuwise</div>
      <div style={{ fontSize: 13, color: c.inkMuted, marginTop: 4 }}>Versi 1.0 · Build 1</div>
    </div>
    <SW_Card c={c} padding={16} style={{ marginBottom: 12 }}>
      <div style={{ fontSize: 14, color: c.ink, lineHeight: 1.6 }}>
        Aplikasi anggaran Indonesia yang lokal-first, terenkripsi, dan hangat.
        Bukan corporate banking, bukan game playful — di tengah, tempat data uang
        kamu tinggal dengan tenang.
      </div>
    </SW_Card>
    <SW_Card c={c} padding={0}>
      <SettingsRow c={c} icon="shield"  label="Kebijakan Privasi" />
      <SettingsRow c={c} icon="info"    label="Lisensi Open Source" />
      <SettingsRow c={c} icon="me"      label="Kontak Pengembang" last />
    </SW_Card>
  </SimpleSettingsScreen>
);

const ExportResetSettings = ({ c, onBack }) => {
  const [showConfirm, setShowConfirm] = React.useState(false);
  return (
    <SimpleSettingsScreen c={c} title="Export & Reset" onBack={onBack}>
      <SW_Card c={c} padding={0} style={{ marginBottom: 14 }}>
        <SettingsRow c={c} icon="copy" label="Export semua data" sub="File .sakuwise terenkripsi untuk dipindah ke device lain" />
        <SettingsRow c={c} icon="receipt" label="Export laporan PDF" sub="Ringkasan transaksi & aset per bulan (V2)" last />
      </SW_Card>
      <SW_Button c={c} variant="danger" icon="trash" onClick={() => setShowConfirm(true)} size="lg">Reset Aplikasi</SW_Button>

      <SW_Sheet c={c} open={showConfirm} onClose={() => setShowConfirm(false)} title="Reset Aplikasi?" maxHeight="62%">
        <div style={{ padding: '8px 0 16px' }}>
          <div style={{
            width: 64, height: 64, borderRadius: 20, margin: '8px auto 18px',
            background: c.dangerSoft, color: c.danger,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="warning" size={32} strokeWidth={1.8} />
          </div>
          <div style={{ fontSize: 18, fontWeight: 800, color: c.ink, textAlign: 'center', letterSpacing: '-0.01em', marginBottom: 8 }}>
            Hapus semua data?
          </div>
          <div style={{ fontSize: 13, color: c.inkMuted, textAlign: 'center', lineHeight: 1.5, marginBottom: 16 }}>
            Semua akun, plan, transaksi, aset, hutang, dan foto struk akan dihapus permanen.
            Tindakan ini <strong style={{ color: c.danger }}>tidak bisa dibatalkan</strong>. Pastikan kamu punya backup terbaru sebelum lanjut.
          </div>
          <SW_Button c={c} variant="danger" onClick={() => setShowConfirm(false)} size="lg" icon="trash">Ya, hapus semua</SW_Button>
          <div style={{ height: 8 }} />
          <SW_Button c={c} variant="ghost" onClick={() => setShowConfirm(false)} size="md">Batal</SW_Button>
        </div>
      </SW_Sheet>
    </SimpleSettingsScreen>
  );
};

// ─── Donasi ─────────────────────────────────────────────────

const DonateScreen = ({ c, onBack }) => (
  <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
    <SW_TopBar c={c} title="Donasi" onBack={onBack} />

    <div style={{ padding: '0 20px' }}>
      {/* Hero */}
      <div style={{
        background: c.primary, color: c.onPrimary,
        borderRadius: 22, padding: '24px 22px',
        position: 'relative', overflow: 'hidden', marginBottom: 18,
      }}>
        <div style={{ position: 'absolute', right: -30, bottom: -30, opacity: 0.14 }}>
          <LogoA_Daun theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={160} />
        </div>
        <div style={{ position: 'relative' }}>
          <Icon name="sparkle" size={28} color={c.accent} strokeWidth={1.6} />
          <div style={{ fontSize: 22, fontWeight: 800, marginTop: 10, letterSpacing: '-0.02em', lineHeight: 1.2 }}>
            Sakuwise gratis untuk semua.
          </div>
          <div style={{ fontSize: 13, opacity: 0.85, marginTop: 6, lineHeight: 1.5 }}>
            Dikembangkan sendiri, sebagai sapaan untuk komunitas. Kalau kamu merasa terbantu,
            traktir kopi sebagai apresiasi.
          </div>
        </div>
      </div>

      <SW_SectionLabel c={c}>Pilih Platform</SW_SectionLabel>
      <SW_Card c={c} padding={0} style={{ marginBottom: 14 }}>
        <button className="sw-press" style={{
          width: '100%', textAlign: 'left',
          display: 'flex', alignItems: 'center', gap: 14,
          padding: '14px 16px',
          background: 'transparent', border: 'none', cursor: 'pointer',
          borderBottom: `1px solid ${c.border}`, fontFamily: SW_TYPE.family,
        }}>
          <div style={{ width: 40, height: 40, borderRadius: 12, background: '#FFC107', color: '#000', display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto', fontSize: 18, fontWeight: 800 }}>S</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>Saweria</div>
            <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>Donasi cepat lewat QRIS / e-wallet</div>
          </div>
          <Icon name="arrow_up_right" size={18} color={c.inkSubtle} />
        </button>
        <button className="sw-press" style={{
          width: '100%', textAlign: 'left',
          display: 'flex', alignItems: 'center', gap: 14,
          padding: '14px 16px',
          background: 'transparent', border: 'none', cursor: 'pointer',
          fontFamily: SW_TYPE.family,
        }}>
          <div style={{ width: 40, height: 40, borderRadius: 12, background: '#9333EA', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto', fontSize: 18, fontWeight: 800 }}>T</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>Trakteer</div>
            <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>Beli kopi atau langganan bulanan</div>
          </div>
          <Icon name="arrow_up_right" size={18} color={c.inkSubtle} />
        </button>
      </SW_Card>

      <SW_SectionLabel c={c}>Atau Scan QRIS</SW_SectionLabel>
      <SW_Card c={c} padding={20} style={{ marginBottom: 18 }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{
            width: 200, height: 200, margin: '0 auto 14px',
            background: '#fff', borderRadius: 16,
            padding: 12, border: `1px solid ${c.border}`,
            position: 'relative',
          }}>
            <QrisPlaceholder c={c} />
          </div>
          <div style={{ fontSize: 12, color: c.inkMuted, marginBottom: 4 }}>QRIS · semua bank & e-wallet Indonesia</div>
          <div style={{ fontSize: 11, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>NMID 9320 0203 88</div>
        </div>
      </SW_Card>

      <div style={{ fontSize: 11, color: c.inkSubtle, textAlign: 'center', padding: '0 12px 12px', lineHeight: 1.5 }}>
        Sakuwise tidak memproses pembayaran di dalam aplikasi.<br/>
        Semua donasi mengalir lewat platform eksternal.
      </div>
    </div>
  </div>
);

// QRIS visual placeholder — geometric noise pattern that reads as QR
const QrisPlaceholder = ({ c }) => (
  <svg viewBox="0 0 100 100" style={{ width: '100%', height: '100%' }}>
    {/* corner markers */}
    {[[10, 10], [80, 10], [10, 80]].map(([x, y], i) => (
      <g key={i}>
        <rect x={x} y={y} width="10" height="10" fill="#1A2520" />
        <rect x={x + 2} y={y + 2} width="6" height="6" fill="#fff" />
        <rect x={x + 4} y={y + 4} width="2" height="2" fill="#1A2520" />
      </g>
    ))}
    {/* QRIS center small logo placeholder */}
    <rect x="42" y="42" width="16" height="16" fill="#fff" />
    <rect x="44" y="44" width="12" height="12" fill="#E60000" />
    <text x="50" y="53" textAnchor="middle" fontFamily="Figtree" fontSize="6" fontWeight="700" fill="#fff">QR</text>
    {/* random dots forming QR-ish noise */}
    {Array.from({ length: 100 }).map((_, i) => {
      const x = (i * 17) % 90 + 5;
      const y = ((i * 31) % 90) + 5;
      const skip = (x > 8 && x < 22 && y > 8 && y < 22) || (x > 78 && x < 92 && y > 8 && y < 22) || (x > 8 && x < 22 && y > 78 && y < 92) || (x > 40 && x < 60 && y > 40 && y < 60);
      if (skip) return null;
      return <rect key={i} x={x} y={y} width={2.5} height={2.5} fill="#1A2520" />;
    })}
  </svg>
);

Object.assign(window, {
  SettingsHub, SettingsGroup, SettingsRow, SimpleSettingsScreen,
  ProfileSettings, AllocationEditor, AllocSlider, GoldPriceSettings,
  AutoLockSettings, PeriodStartSettings, PinSettings, LanguageSettings,
  AboutScreen, ExportResetSettings, DonateScreen, QrisPlaceholder,
});
