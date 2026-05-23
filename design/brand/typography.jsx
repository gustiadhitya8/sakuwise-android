// Sakuwise — typography scale visualization

const TypeRow = ({ entry, theme }) => {
  const c = theme;
  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: '110px 1fr 140px',
      gap: 16,
      alignItems: 'center',
      padding: '12px 0',
      borderBottom: `1px solid ${c.border}`,
    }}>
      <div>
        <div style={{ fontFamily: SW_TYPE.mono, fontSize: 11, color: c.inkMuted, letterSpacing: '0.02em' }}>
          {entry.size}/{entry.lh} · {entry.weight}
        </div>
        <div style={{ fontSize: 12, color: c.inkSubtle, marginTop: 2 }}>{entry.name}</div>
      </div>
      <div style={{
        fontFamily: SW_TYPE.family,
        fontSize: Math.min(entry.size, 32),
        lineHeight: `${Math.min(entry.lh, 40)}px`,
        fontWeight: entry.weight,
        letterSpacing: entry.letterSpacing,
        color: c.ink,
        fontVariantNumeric: entry.tabular ? 'tabular-nums' : 'normal',
      }}>
        {entry.tabular ? 'Rp 1.500.000' : 'Rencanakan keuanganmu'}
      </div>
      <div style={{ fontSize: 11, color: c.inkSubtle, textAlign: 'right' }}>{entry.use}</div>
    </div>
  );
};

const TypographyArtboard = ({ theme, width = 760, height = 720 }) => {
  const c = theme;
  return (
    <div style={{
      width, height, padding: 32, background: c.bg, color: c.ink,
      fontFamily: SW_TYPE.family, overflow: 'hidden',
    }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.02em' }}>Typography</div>
        <div style={{ fontFamily: SW_TYPE.mono, fontSize: 11, color: c.inkMuted, letterSpacing: '0.04em' }}>FIGTREE · GOOGLE FONTS</div>
      </div>
      <div style={{ fontSize: 14, color: c.inkMuted, marginBottom: 20 }}>
        Humanist sans — friendly, jelas, hemat ruang vertikal. Tabular nums untuk angka.
      </div>

      {/* Font specimen */}
      <div style={{
        padding: '20px 22px',
        background: c.surface,
        border: `1px solid ${c.border}`,
        borderRadius: 14,
        marginBottom: 18,
      }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 16, marginBottom: 6 }}>
          <span style={{ fontSize: 72, fontWeight: 800, letterSpacing: '-0.04em', lineHeight: 1, color: c.primary }}>Aa</span>
          <span style={{ fontFamily: SW_TYPE.mono, fontSize: 12, color: c.inkMuted }}>300 · 400 · 500 · 600 · 700 · 800</span>
        </div>
        <div style={{ fontSize: 14, color: c.inkMuted, marginTop: 8, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>
          ABCDEFGHIJKLMNOPQRSTUVWXYZ · 0123456789
        </div>
        <div style={{ fontSize: 13, color: c.inkSubtle, marginTop: 4, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>
          abcdefghijklmnopqrstuvwxyz · áéíóú ñ ç · Rp €
        </div>
      </div>

      {/* Scale */}
      <div style={{ fontSize: 11, fontWeight: 600, color: c.inkSubtle, letterSpacing: '0.08em', marginBottom: 4, textTransform: 'uppercase' }}>Scale</div>
      <div style={{ background: c.surface, border: `1px solid ${c.border}`, borderRadius: 14, padding: '4px 18px' }}>
        {SW_TYPE.scale.map((e, i) => <TypeRow key={i} entry={e} theme={c} />)}
      </div>
    </div>
  );
};

Object.assign(window, { TypographyArtboard });
