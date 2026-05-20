// Sakuwise — Brand Identity canvas. Lays out all artboards by section.

// ─────────────────────────────────────────────────────────────
// Section artboards
// ─────────────────────────────────────────────────────────────

// Hero — wordmark + tagline + the brand statement
const HeroArtboard = ({ theme, variant = 'A', width = 880, height = 520 }) => {
  const c = theme;
  return (
    <div style={{
      width, height, padding: '60px 64px',
      background: c.bg, color: c.ink,
      fontFamily: SW_TYPE.family,
      position: 'relative', overflow: 'hidden',
      display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
    }}>
      {/* corner glyph */}
      <div style={{ position: 'absolute', right: -40, bottom: -40, opacity: 0.07 }}>
        {React.createElement(LOGO_MAP[variant], { theme: c, size: 380 })}
      </div>

      <div>
        <div style={{ fontSize: 11, fontWeight: 600, color: c.inkMuted, letterSpacing: '0.16em', textTransform: 'uppercase', marginBottom: 16 }}>
          Brand Identity · v1.0
        </div>
        <Lockup theme={c} variant={variant} size={72} layout="v" showTagline={false} />
      </div>

      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: 24, position: 'relative', zIndex: 1 }}>
        <div style={{ maxWidth: 460 }}>
          <div style={{ fontSize: 36, fontWeight: 700, letterSpacing: '-0.025em', lineHeight: 1.1, marginBottom: 10 }}>
            {SW_TAGLINE}
          </div>
          <div style={{ fontSize: 15, color: c.inkMuted, lineHeight: 1.5 }}>
            Aplikasi anggaran Indonesia yang lokal-first, terenkripsi, dan hangat.
            Bukan corporate banking, bukan game playful — di tengah, tempat data uang
            kamu tinggal dengan tenang.
          </div>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: 11, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>NAMA</div>
          <div style={{ fontSize: 14, color: c.inkMuted, marginTop: 2 }}>{SW_NAME_MEAN}</div>
        </div>
      </div>
    </div>
  );
};

// Logo detail card — shows one concept across sizes + on light & dark
const LogoConceptArtboard = ({ variant, width = 520, height = 600 }) => {
  const Mark = LOGO_MAP[variant];
  return (
    <div style={{
      width, height, padding: 28,
      background: '#FAF7F0',
      fontFamily: SW_TYPE.family,
      display: 'flex', flexDirection: 'column',
    }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
        <div style={{ fontSize: 13, color: SW_LIGHT.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>CONCEPT {variant}</div>
        <div style={{ fontSize: 11, color: SW_LIGHT.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>v1</div>
      </div>
      <div style={{ fontSize: 28, fontWeight: 700, letterSpacing: '-0.02em', color: SW_LIGHT.ink, marginBottom: 4 }}>
        {LOGO_NAMES[variant]}
      </div>
      <div style={{ fontSize: 13, color: SW_LIGHT.inkMuted, marginBottom: 22, lineHeight: 1.45 }}>
        {LOGO_MEAN[variant]}
      </div>

      {/* Main mark — large */}
      <div style={{
        background: SW_LIGHT.bg, borderRadius: 18, padding: 30,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        marginBottom: 14, flex: 1, minHeight: 0,
      }}>
        <Mark theme={SW_LIGHT} size={180} />
      </div>

      {/* Variants row */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 10 }}>
        <div style={{
          background: SW_LIGHT.bg, borderRadius: 12, padding: 14,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 16,
        }}>
          <Mark theme={SW_LIGHT} size={56} />
          <Mark theme={SW_LIGHT} size={32} />
          <Mark theme={SW_LIGHT} size={20} />
        </div>
        <div style={{
          background: SW_DARK.bg, borderRadius: 12, padding: 14,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 16,
        }}>
          <Mark theme={SW_DARK} size={56} />
          <Mark theme={SW_DARK} size={32} />
          <Mark theme={SW_DARK} size={20} />
        </div>
      </div>

      {/* Lockup */}
      <div style={{
        background: SW_LIGHT.surface, border: `1px solid ${SW_LIGHT.border}`,
        borderRadius: 12, padding: '14px 16px',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <Lockup theme={SW_LIGHT} variant={variant} size={28} />
        <div style={{ fontSize: 10, color: SW_LIGHT.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>HORIZ. LOCKUP</div>
      </div>
    </div>
  );
};

// Tagline lockup artboard — large display of "Rencanakan. Catat. Tenang."
const TaglineArtboard = ({ theme, variant = 'A', width = 760, height = 380 }) => {
  const c = theme;
  return (
    <div style={{
      width, height, padding: '48px 56px',
      background: c.primary, color: c.onPrimary,
      fontFamily: SW_TYPE.family,
      display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
      position: 'relative', overflow: 'hidden',
    }}>
      <div style={{ position: 'absolute', right: -50, top: -50, opacity: 0.08 }}>
        {React.createElement(LOGO_MAP[variant], { theme: { ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }, size: 280 })}
      </div>

      <div style={{ position: 'relative', zIndex: 1 }}>
        <div style={{ fontSize: 11, fontWeight: 600, letterSpacing: '0.16em', opacity: 0.7, textTransform: 'uppercase', marginBottom: 12 }}>
          Tagline
        </div>
        <div style={{ fontSize: 56, fontWeight: 800, letterSpacing: '-0.035em', lineHeight: 1.05 }}>
          Rencanakan.<br/>
          Catat.<br/>
          <span style={{ color: c.accent }}>Tenang.</span>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', position: 'relative', zIndex: 1 }}>
        <div style={{ fontSize: 13, opacity: 0.7, maxWidth: 320, lineHeight: 1.4 }}>
          Tiga kata, satu ritual bulanan. Verb pendek, imperatif yang ramah —
          bukan perintah, lebih kayak ngajak.
        </div>
        <Lockup theme={{ ...c, ink: c.onPrimary, primary: c.accent }} variant={variant} size={22} />
      </div>
    </div>
  );
};

// Voice & tone artboard
const VoiceArtboard = ({ theme, width = 760, height = 520 }) => {
  const c = theme;
  const examples = [
    {
      label: 'Sapaan Dashboard',
      yes: 'Selamat pagi, Gusti. Plan Mei 2026, sisa 16 hari.',
      no: 'Halo User! Anda memiliki 16 hari tersisa dalam siklus anggaran.',
    },
    {
      label: 'Empty state',
      yes: 'Belum ada plan bulan ini. Mulai dari template starter?',
      no: 'Anda tidak memiliki rencana keuangan untuk bulan ini.',
    },
    {
      label: 'Overspending',
      yes: 'Kategori Makanan sudah lewat anggaran Rp 120.000.',
      no: 'PERINGATAN: Anda telah melebihi batas pengeluaran kategori Makanan.',
    },
    {
      label: 'Backup berhasil',
      yes: 'Backup tersimpan. File ada di Download.',
      no: 'Backup data Anda telah berhasil dilakukan dan disimpan.',
    },
    {
      label: 'Konfirmasi hapus',
      yes: 'Hapus transaksi ini? Tidak bisa dibatalkan.',
      no: 'Apakah Anda yakin ingin menghapus transaksi tersebut?',
    },
  ];
  return (
    <div style={{
      width, height, padding: 32, background: c.bg, color: c.ink,
      fontFamily: SW_TYPE.family, overflow: 'hidden',
    }}>
      <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.02em', marginBottom: 4 }}>Voice &amp; Tone</div>
      <div style={{ fontSize: 14, color: c.inkMuted, marginBottom: 20 }}>
        Ramah · jelas · sedikit hangat · tanpa basa-basi corporate. Pakai sapaan, bukan "Anda" formal.
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14, marginBottom: 16 }}>
        <div style={{ background: c.successSoft, borderRadius: 12, padding: '12px 14px', border: `1px solid ${c.success}33` }}>
          <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.08em', color: c.success, textTransform: 'uppercase' }}>Begini ✓</div>
        </div>
        <div style={{ background: c.dangerSoft, borderRadius: 12, padding: '12px 14px', border: `1px solid ${c.danger}33` }}>
          <div style={{ fontSize: 11, fontWeight: 700, letterSpacing: '0.08em', color: c.danger, textTransform: 'uppercase' }}>Hindari ✗</div>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {examples.map((ex, i) => (
          <div key={i} style={{ display: 'grid', gridTemplateColumns: '120px 1fr 1fr', gap: 14, alignItems: 'start' }}>
            <div style={{ fontSize: 11, color: c.inkSubtle, textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600, paddingTop: 10 }}>
              {ex.label}
            </div>
            <div style={{ background: c.surface, border: `1px solid ${c.success}55`, borderLeft: `3px solid ${c.success}`, borderRadius: 8, padding: '8px 12px', fontSize: 13, color: c.ink }}>
              {ex.yes}
            </div>
            <div style={{ background: c.surface, border: `1px solid ${c.danger}33`, borderLeft: `3px solid ${c.danger}`, borderRadius: 8, padding: '8px 12px', fontSize: 13, color: c.inkMuted, textDecoration: 'line-through', textDecorationColor: c.inkSubtle }}>
              {ex.no}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};


// ─────────────────────────────────────────────────────────────
// App
// ─────────────────────────────────────────────────────────────

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "primaryLogo": "A"
}/*EDITMODE-END*/;

function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const v = t.primaryLogo || 'A';

  return (
    <React.Fragment>
      <DesignCanvas>
        <DCSection id="intro" title="Sakuwise · Brand Identity v1" subtitle="Milestone 1 — fondasi visual. Drag artboard untuk reorder, klik untuk fokus.">
          <DCArtboard id="hero-light" label="Hero · Light" width={880} height={520}>
            <HeroArtboard theme={SW_LIGHT} variant={v} />
          </DCArtboard>
          <DCArtboard id="hero-dark" label="Hero · Dark" width={880} height={520}>
            <HeroArtboard theme={SW_DARK} variant={v} />
          </DCArtboard>
          <DCPostIt width={260}>
            <strong>Cara baca:</strong><br/>
            3 konsep logo di section bawah → pilih satu pakai Tweaks (kanan-bawah).
            Pilihan kamu otomatis terpakai di hero, tagline, app icon, dan dashboard preview.
          </DCPostIt>
        </DCSection>

        <DCSection id="logos" title="Konsep Logo" subtitle="3 arah symbol-led. Semua scalable ke 16px favicon.">
          <DCArtboard id="logo-a" label="A · Daun" width={520} height={600}>
            <LogoConceptArtboard variant="A" />
          </DCArtboard>
          <DCArtboard id="logo-b" label="B · Lingkar S" width={520} height={600}>
            <LogoConceptArtboard variant="B" />
          </DCArtboard>
          <DCArtboard id="logo-c" label="C · Lipat" width={520} height={600}>
            <LogoConceptArtboard variant="C" />
          </DCArtboard>
        </DCSection>

        <DCSection id="tagline" title="Tagline" subtitle="Lockup bermerek untuk splash & marketing.">
          <DCArtboard id="tagline-light" label="Tagline · Light" width={760} height={380}>
            <TaglineArtboard theme={SW_LIGHT} variant={v} />
          </DCArtboard>
          <DCArtboard id="tagline-dark" label="Tagline · Dark" width={760} height={380}>
            <TaglineArtboard theme={SW_DARK} variant={v} />
          </DCArtboard>
        </DCSection>

        <DCSection id="color" title="Color System" subtitle="Forest green + cream. Light dan dark didesain setara.">
          <DCArtboard id="color-light" label="Color · Light" width={640} height={720}>
            <ColorArtboard theme={SW_LIGHT} themeName="Light" width={640} height={720} />
          </DCArtboard>
          <DCArtboard id="color-dark" label="Color · Dark" width={640} height={720}>
            <ColorArtboard theme={SW_DARK} themeName="Dark" width={640} height={720} />
          </DCArtboard>
        </DCSection>

        <DCSection id="type" title="Typography" subtitle="Figtree — humanist sans, Google Fonts, full Latin Extended.">
          <DCArtboard id="type-light" label="Type · Light" width={760} height={720}>
            <TypographyArtboard theme={SW_LIGHT} width={760} height={720} />
          </DCArtboard>
          <DCArtboard id="type-dark" label="Type · Dark" width={760} height={720}>
            <TypographyArtboard theme={SW_DARK} width={760} height={720} />
          </DCArtboard>
        </DCSection>

        <DCSection id="icon" title="App Icon" subtitle="Adaptive Android 108dp · foreground + background layer.">
          <DCArtboard id="icon-light" label="App Icon · Light tone" width={760} height={480}>
            <AppIconArtboard theme={SW_LIGHT} variant={v} width={760} height={480} />
          </DCArtboard>
          <DCArtboard id="icon-dark" label="App Icon · Dark tone" width={760} height={480}>
            <AppIconArtboard theme={SW_DARK} variant={v} width={760} height={480} />
          </DCArtboard>
        </DCSection>

        <DCSection id="voice" title="Voice &amp; Tone" subtitle="Microcopy starter. Tone: ramah profesional Indonesia.">
          <DCArtboard id="voice-light" label="Voice · Light" width={760} height={520}>
            <VoiceArtboard theme={SW_LIGHT} width={760} height={520} />
          </DCArtboard>
        </DCSection>

        <DCSection id="incontext" title="Brand di Konteks" subtitle="Cara brand terasa di layar Dashboard. Preview, bukan final UI.">
          <DCArtboard id="ctx-light" label="Dashboard · Light" width={440} height={800}>
            <InContextArtboard theme={SW_LIGHT} variant={v} width={440} height={800} />
          </DCArtboard>
          <DCArtboard id="ctx-dark" label="Dashboard · Dark" width={440} height={800}>
            <InContextArtboard theme={SW_DARK} variant={v} width={440} height={800} />
          </DCArtboard>
          <DCPostIt width={240}>
            Catatan: ini cuma preview seberapa visual brand "terasa" — Dashboard
            full dengan semua tile (9 section di PRD) akan dikerjakan di
            Milestone 3.
          </DCPostIt>
        </DCSection>
      </DesignCanvas>

      <TweaksPanel title="Tweaks">
        <TweakSection label="Logo Primer">
          <TweakRadio
            label="Konsep"
            value={v}
            onChange={(val) => setTweak('primaryLogo', val)}
            options={[
              { value: 'A', label: 'Daun' },
              { value: 'B', label: 'Lingkar' },
              { value: 'C', label: 'Lipat' },
            ]}
          />
        </TweakSection>
      </TweaksPanel>
    </React.Fragment>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
