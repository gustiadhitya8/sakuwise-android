// Sakuwise — Splash + Onboarding 4 layar

// ─── Splash ─────────────────────────────────────────────────
// Held for ~1.4s after mount, fading mark → wordmark → tagline.

const SplashScreen = ({ c, onDone }) => {
  React.useEffect(() => {
    const t = setTimeout(onDone, 1600);
    return () => clearTimeout(t);
  }, [onDone]);
  return (
    <div style={{
      position: 'absolute', inset: 0, background: c.bg,
      display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
      gap: 22,
    }}>
      <div style={{ animation: 'sw-fade-up 600ms cubic-bezier(.2,.7,.3,1) both' }}>
        <LogoA_Daun theme={c} size={120} />
      </div>
      <div style={{ animation: 'sw-fade-up 600ms 220ms cubic-bezier(.2,.7,.3,1) both' }}>
        <Wordmark theme={c} size={36} weight={800} />
      </div>
      <div style={{ animation: 'sw-fade-up 600ms 440ms cubic-bezier(.2,.7,.3,1) both', fontSize: 13, color: c.inkMuted, letterSpacing: '0.02em' }}>
        Rencanakan. Catat. Tenang.
      </div>
    </div>
  );
};

// ─── Onboarding shell ───────────────────────────────────────

const OnboardingShell = ({ c, step, total, title, sub, hero, children, primaryLabel, onPrimary, secondaryLabel, onSecondary, disabled }) => (
  <div style={{
    position: 'absolute', inset: 0, background: c.bg,
    display: 'flex', flexDirection: 'column',
    animation: 'sw-slide-in 280ms cubic-bezier(.2,.7,.3,1)',
    fontFamily: SW_TYPE.family,
  }}>
    {/* Progress dots */}
    <div style={{ padding: '52px 20px 0', display: 'flex', justifyContent: 'center', gap: 6 }}>
      {Array.from({ length: total }).map((_, i) => (
        <div key={i} style={{
          width: i === step - 1 ? 22 : 6, height: 6, borderRadius: 3,
          background: i < step ? c.primary : c.border,
          transition: 'all 200ms ease',
        }} />
      ))}
    </div>

    {/* Hero artwork */}
    <div style={{ flex: '0 0 auto', padding: '32px 24px 16px', display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 200 }}>
      {hero}
    </div>

    {/* Copy */}
    <div className="sw-scroll" style={{ flex: 1, overflowY: 'auto', padding: '0 28px 8px' }}>
      <div style={{ fontSize: 28, fontWeight: 800, color: c.ink, letterSpacing: '-0.025em', lineHeight: 1.15, marginBottom: 10 }}>{title}</div>
      <div style={{ fontSize: 15, color: c.inkMuted, lineHeight: 1.5, marginBottom: 22 }}>{sub}</div>
      {children}
    </div>

    {/* Actions */}
    <div style={{ padding: '16px 20px 40px', display: 'flex', flexDirection: 'column', gap: 8 }}>
      <SW_Button c={c} onClick={onPrimary} size="lg" disabled={disabled}>{primaryLabel}</SW_Button>
      {secondaryLabel && (
        <SW_Button c={c} variant="ghost" size="md" onClick={onSecondary}>{secondaryLabel}</SW_Button>
      )}
    </div>
  </div>
);

// ─── Step 1: Sambutan + Bahasa ─────────────────────────────

const Onb_Language = ({ c, lang, setLang, onNext }) => (
  <OnboardingShell
    c={c} step={1} total={4}
    hero={
      <div style={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{
          width: 180, height: 180, borderRadius: 60,
          background: c.primaryContainer,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <LogoA_Daun theme={c} size={108} />
        </div>
      </div>
    }
    title="Halo, kenalan dulu yuk"
    sub="Sakuwise adalah aplikasi anggaran lokal — semua data tinggal di HP kamu, tanpa cloud, tanpa pelacakan."
    primaryLabel="Mulai"
    onPrimary={onNext}
  >
    <div style={{ fontSize: 12, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 10 }}>Pilih Bahasa</div>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {[
        { id: 'id', label: 'Bahasa Indonesia', sub: 'Default' },
        { id: 'en', label: 'English', sub: 'Available too' },
      ].map(l => (
        <button key={l.id} onClick={() => setLang(l.id)} className="sw-press" style={{
          display: 'flex', alignItems: 'center', gap: 14,
          padding: '14px 16px',
          background: lang === l.id ? c.primaryContainer : c.surface,
          border: `1.5px solid ${lang === l.id ? c.primary : c.border}`,
          borderRadius: 14, cursor: 'pointer', textAlign: 'left',
          fontFamily: SW_TYPE.family,
        }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 15, fontWeight: 700, color: c.ink }}>{l.label}</div>
            <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>{l.sub}</div>
          </div>
          <div style={{
            width: 22, height: 22, borderRadius: '50%',
            border: `2px solid ${lang === l.id ? c.primary : c.borderStrong}`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            background: lang === l.id ? c.primary : 'transparent',
          }}>
            {lang === l.id && <Icon name="check" size={12} strokeWidth={3} color={c.onPrimary} />}
          </div>
        </button>
      ))}
    </div>
  </OnboardingShell>
);

// ─── Step 2: Nickname + Biometric + PIN ────────────────────

const Onb_Identity = ({ c, nickname, setNickname, biometric, setBiometric, pin, setPin, onNext }) => (
  <OnboardingShell
    c={c} step={2} total={4}
    hero={
      <div style={{ position: 'relative', width: 180, height: 180 }}>
        <div style={{
          width: 180, height: 180, borderRadius: 60, background: c.primaryContainer,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <Icon name="shield" size={90} color={c.primary} strokeWidth={1.5} />
        </div>
        <div style={{
          position: 'absolute', right: -8, bottom: -8,
          width: 56, height: 56, borderRadius: 18,
          background: c.surface, border: `2px solid ${c.bg}`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 4px 16px rgba(0,0,0,0.10)',
        }}>
          <Icon name="me" size={28} color={c.primary} />
        </div>
      </div>
    }
    title="Atur identitas singkat"
    sub="Cuma untuk sapaan dan keamanan. Tidak ada data ke server, ini semua tetap di HP kamu."
    primaryLabel="Lanjut"
    onPrimary={onNext}
    disabled={!nickname || pin.length !== 6}
  >
    <SW_Field c={c} label="Nama panggilan" value={nickname} onChange={setNickname} placeholder="Mis. Gusti" hint="Dipakai untuk sapaan di Beranda." />

    <div style={{ marginBottom: 14 }}>
      <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 6 }}>PIN 6 digit (cadangan biometrik)</div>
      <PinInput c={c} value={pin} onChange={setPin} />
      <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 6, marginLeft: 4 }}>
        Dipakai kalau biometrik gagal. Bisa diganti kapan saja di Pengaturan.
      </div>
    </div>

    <div style={{
      display: 'flex', alignItems: 'center', gap: 12,
      padding: '12px 14px',
      background: c.surface, border: `1px solid ${c.border}`,
      borderRadius: 12,
    }}>
      <div style={{ width: 40, height: 40, borderRadius: 12, background: c.primaryContainer, color: c.onPrimaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Icon name="shield" size={20} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 13, fontWeight: 700, color: c.ink }}>Buka pakai biometrik</div>
        <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>Sidik jari / wajah — lebih cepat dari ketik PIN.</div>
      </div>
      <SW_Toggle c={c} value={biometric} onChange={setBiometric} />
    </div>
  </OnboardingShell>
);

const PinInput = ({ c, value, onChange }) => (
  <div style={{ display: 'flex', gap: 8 }}>
    {Array.from({ length: 6 }).map((_, i) => {
      const filled = i < value.length;
      const active = i === value.length;
      return (
        <div key={i} onClick={() => {
          // simulated: clicking auto-fills (prototype-only)
          if (i >= value.length) onChange(value + '·');
        }} className="sw-press" style={{
          flex: 1, height: 56, maxWidth: 50,
          background: c.surface,
          border: `1.5px solid ${active ? c.primary : c.border}`,
          borderRadius: 12,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer',
        }}>
          {filled && (
            <span style={{ width: 12, height: 12, borderRadius: '50%', background: c.ink }} />
          )}
        </div>
      );
    })}
  </div>
);

// ─── Step 3: Privacy notice ────────────────────────────────

const Onb_Privacy = ({ c, onNext }) => (
  <OnboardingShell
    c={c} step={3} total={4}
    hero={
      <div style={{
        width: 180, height: 180, borderRadius: 60, background: c.primaryContainer,
        display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative',
      }}>
        <Icon name="shield" size={100} color={c.primary} strokeWidth={1.4} />
        <Icon name="check" size={40} color={c.primary} strokeWidth={2.5} style={{ position: 'absolute' }} />
      </div>
    }
    title="Data kamu tinggal di sini."
    sub="Tidak ada server. Tidak ada telemetry. Tidak ada permintaan akses internet."
    primaryLabel="Saya mengerti"
    onPrimary={onNext}
  >
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <PrivacyPoint c={c} icon="shield" title="Terenkripsi di disk" sub="Database & foto struk semua dienkripsi pakai AES-256." />
      <PrivacyPoint c={c} icon="me" title="Bukan akun, bukan email" sub="Tidak ada login. Sakuwise tidak tahu siapa kamu." />
      <PrivacyPoint c={c} icon="copy" title="Backup dipegang kamu" sub="File backup terenkripsi — kamu yang simpan, kamu yang pegang PIN-nya." />
    </div>
  </OnboardingShell>
);

const PrivacyPoint = ({ c, icon, title, sub }) => (
  <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
    <div style={{
      width: 36, height: 36, borderRadius: 11,
      background: c.primaryContainer, color: c.onPrimaryContainer,
      display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
    }}>
      <Icon name={icon} size={18} />
    </div>
    <div style={{ flex: 1 }}>
      <div style={{ fontSize: 14, fontWeight: 700, color: c.ink, marginBottom: 2 }}>{title}</div>
      <div style={{ fontSize: 12, color: c.inkMuted, lineHeight: 1.45 }}>{sub}</div>
    </div>
  </div>
);

// ─── Step 4: Akun pertama ──────────────────────────────────

const Onb_FirstAccount = ({ c, account, setAccount, onDone }) => {
  const types = [
    { id: 'cash', label: 'Tunai', icon: 'cash' },
    { id: 'bank', label: 'Bank', icon: 'bank' },
    { id: 'ewallet', label: 'Dompet', icon: 'wallet' },
  ];
  return (
    <OnboardingShell
      c={c} step={4} total={4}
      hero={
        <div style={{
          width: 220, height: 130, borderRadius: 18, background: c.primary, color: c.onPrimary,
          padding: 18, position: 'relative', overflow: 'hidden', boxShadow: '0 10px 30px rgba(15,76,58,0.15)',
        }}>
          <div style={{ position: 'absolute', right: -20, bottom: -20, opacity: 0.18 }}>
            <LogoA_Daun theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={140} />
          </div>
          <div style={{ position: 'relative' }}>
            <div style={{ fontSize: 10, fontWeight: 700, letterSpacing: '0.1em', opacity: 0.8, textTransform: 'uppercase' }}>Akun · {account.type}</div>
            <div style={{ fontSize: 22, fontWeight: 700, marginTop: 6, letterSpacing: '-0.01em' }}>{account.name || 'Tunai'}</div>
            <div style={{ fontSize: 18, fontWeight: 700, marginTop: 10, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em' }}>
              {SW_FORMAT.rp(account.balance || 0)}
            </div>
          </div>
        </div>
      }
      title="Akun pertamamu"
      sub="Kita siapkan akun Tunai dulu. Bisa di-rename, ubah saldonya, atau langsung lanjut."
      primaryLabel="Selesai · Masuk Beranda"
      onPrimary={onDone}
      secondaryLabel="Tambah akun lain nanti"
    >
      <SW_Field c={c} label="Nama akun" value={account.name} onChange={(v) => setAccount({ ...account, name: v })} placeholder="Mis. Tunai, Mandiri, GoPay" />

      <div style={{ marginBottom: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 6 }}>Tipe</div>
        <div style={{ display: 'flex', gap: 8 }}>
          {types.map(tp => (
            <button key={tp.id} onClick={() => setAccount({ ...account, type: tp.label, icon: tp.icon })} className="sw-press" style={{
              flex: 1, padding: '10px 6px',
              background: account.icon === tp.icon ? c.primaryContainer : c.surface,
              border: `1.5px solid ${account.icon === tp.icon ? c.primary : c.border}`,
              borderRadius: 12, cursor: 'pointer',
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
              color: c.ink, fontFamily: SW_TYPE.family,
            }}>
              <Icon name={tp.icon} size={22} />
              <span style={{ fontSize: 12, fontWeight: 600, whiteSpace: 'nowrap' }}>{tp.label}</span>
            </button>
          ))}
        </div>
      </div>

      <SW_Field c={c} label="Saldo awal" value={new Intl.NumberFormat('id-ID').format(account.balance || 0)}
        onChange={(v) => setAccount({ ...account, balance: parseInt(v.replace(/\D/g, '')) || 0 })}
        prefix="Rp" type="text" hint="Boleh 0 — kamu bisa update nanti." />
    </OnboardingShell>
  );
};

// ─── Onboarding flow ────────────────────────────────────────

const OnboardingFlow = ({ c, onDone }) => {
  const [step, setStep] = React.useState(0);
  const [lang, setLang] = React.useState('id');
  const [nickname, setNickname] = React.useState('Gusti');
  const [biometric, setBiometric] = React.useState(true);
  const [pin, setPin] = React.useState('······');
  const [account, setAccount] = React.useState({ name: 'Tunai', type: 'Tunai', icon: 'cash', balance: 0 });

  if (step === 0) return <SplashScreen c={c} onDone={() => setStep(1)} />;
  if (step === 1) return <Onb_Language c={c} lang={lang} setLang={setLang} onNext={() => setStep(2)} />;
  if (step === 2) return <Onb_Identity c={c} nickname={nickname} setNickname={setNickname} biometric={biometric} setBiometric={setBiometric} pin={pin} setPin={setPin} onNext={() => setStep(3)} />;
  if (step === 3) return <Onb_Privacy c={c} onNext={() => setStep(4)} />;
  if (step === 4) return <Onb_FirstAccount c={c} account={account} setAccount={setAccount} onDone={onDone} />;
  return null;
};

Object.assign(window, { SplashScreen, OnboardingFlow });
