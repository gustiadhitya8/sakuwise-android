// Sakuwise — color system visualization

const SwatchTile = ({ name, hex, role, fg, large = false }) => (
  <div style={{
    background: hex,
    color: fg,
    padding: large ? '20px 18px' : '14px 14px',
    borderRadius: 12,
    minHeight: large ? 96 : 72,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    fontFamily: SW_TYPE.family,
    boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.04)',
  }}>
    <div style={{ fontSize: large ? 15 : 13, fontWeight: 600, letterSpacing: '-0.005em' }}>{name}</div>
    <div>
      <div style={{ fontSize: large ? 13 : 11, fontWeight: 500, opacity: 0.75, marginBottom: 2 }}>{role}</div>
      <div style={{ fontSize: large ? 12 : 11, fontFamily: SW_TYPE.mono, opacity: 0.85, letterSpacing: '0.02em' }}>{hex.toUpperCase()}</div>
    </div>
  </div>
);

const ColorArtboard = ({ theme, themeName, width = 640, height = 720 }) => {
  const c = theme;
  const onDark = c === SW_DARK;
  return (
    <div style={{
      width, height, padding: 32, background: c.bg, color: c.ink,
      fontFamily: SW_TYPE.family, overflow: 'hidden',
    }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.02em' }}>Color · {themeName}</div>
        <div style={{ fontSize: 12, color: c.inkMuted, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>
          {onDark ? 'DARK' : 'LIGHT'}
        </div>
      </div>
      <div style={{ fontSize: 14, color: c.inkMuted, marginBottom: 20 }}>
        Hangat, tenang, dapat dipercaya — forest green sebagai jangkar.
      </div>

      {/* Primary row — big */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr 1fr', gap: 10, marginBottom: 16 }}>
        <SwatchTile name="Primary" role="brand · CTA · jangkar" hex={c.primary} fg={c.onPrimary} large />
        <SwatchTile name="Accent" role="secondary · highlight" hex={c.accent} fg={onDark ? c.onPrimary : '#0A2E22'} large />
        <SwatchTile name="Primary Container" role="soft state · chip" hex={c.primaryContainer} fg={c.onPrimaryContainer} large />
      </div>

      {/* Surface row */}
      <div style={{ fontSize: 11, fontWeight: 600, color: c.inkSubtle, letterSpacing: '0.08em', marginBottom: 8, textTransform: 'uppercase' }}>Surface</div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10, marginBottom: 16 }}>
        <SwatchTile name="BG" role="page background" hex={c.bg} fg={c.ink} />
        <SwatchTile name="Surface" role="cards, sheets" hex={c.surface} fg={c.ink} />
        <SwatchTile name="Surface Elev" role="modals, FAB" hex={c.surfaceElev} fg={c.ink} />
      </div>

      {/* Text + border */}
      <div style={{ fontSize: 11, fontWeight: 600, color: c.inkSubtle, letterSpacing: '0.08em', marginBottom: 8, textTransform: 'uppercase' }}>Ink &amp; Hairline</div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10, marginBottom: 16 }}>
        <SwatchTile name="Ink" role="primary text" hex={c.ink} fg={c.bg} />
        <SwatchTile name="Ink Muted" role="secondary text" hex={c.inkMuted} fg={c.bg} />
        <SwatchTile name="Ink Subtle" role="caption, hint" hex={c.inkSubtle} fg={c.bg} />
        <SwatchTile name="Border" role="hairline" hex={c.border} fg={c.ink} />
      </div>

      {/* Semantic */}
      <div style={{ fontSize: 11, fontWeight: 600, color: c.inkSubtle, letterSpacing: '0.08em', marginBottom: 8, textTransform: 'uppercase' }}>Semantic</div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10 }}>
        <SwatchTile name="Success" role="lunas, saved" hex={c.success} fg="#fff" />
        <SwatchTile name="Warning" role="hampir over · backup" hex={c.warning} fg="#fff" />
        <SwatchTile name="Danger" role="overspending · error" hex={c.danger} fg="#fff" />
        <SwatchTile name="Info" role="rekonsiliasi · tips" hex={c.info} fg="#fff" />
      </div>

      <div style={{ marginTop: 18, padding: '12px 14px', background: c.surface, borderRadius: 10, fontSize: 12, color: c.inkMuted, border: `1px solid ${c.border}` }}>
        <strong style={{ color: c.ink, fontWeight: 600 }}>Catatan kontras.</strong> Semua kombinasi text-on-surface
        lulus WCAG AA (4.5:1) — text utama pada BG &amp; Surface, plus white-on-primary/danger/warning untuk badge.
      </div>
    </div>
  );
};

Object.assign(window, { SwatchTile, ColorArtboard });
