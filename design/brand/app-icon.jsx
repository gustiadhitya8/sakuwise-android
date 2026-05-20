// Sakuwise — adaptive Android app icon
// Spec: 108dp canvas, 72dp safe zone (66.7% center). Foreground sits on transparent layer.
// Background is a separate fill. Play Store mask = circle, but launcher may apply
// squircle / rounded square / circle mask depending on OEM.

const ICON_CANVAS = 108;
const ICON_SAFE  = 66;  // safe zone diameter

// Background = brand primary; foreground = mark in cream onPrimary at safe-zone size
const AppIconForeground = ({ theme, variant = 'A', size = 192 }) => {
  const c = theme;
  const Mark = LOGO_MAP[variant];
  // Scale: mark renders inside its own 64 viewBox, we render at safe-zone size
  const markPx = (size * ICON_SAFE) / ICON_CANVAS;
  return (
    <div style={{
      width: size, height: size,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      {/* Render mark with onPrimary-only fill so it sits crisp on bg */}
      <div style={{ width: markPx, height: markPx, display: 'flex' }}>
        <Mark theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={markPx} />
      </div>
    </div>
  );
};

const AppIconBackground = ({ theme, size = 192 }) => {
  const c = theme;
  return (
    <div style={{
      width: size, height: size,
      background: c.primary,
    }} />
  );
};

// Composed icon at the OEM mask of choice
const AppIconComposed = ({ theme, variant = 'A', size = 192, mask = 'squircle', shadow = true }) => {
  const c = theme;
  const Mark = LOGO_MAP[variant];
  const markPx = (size * ICON_SAFE) / ICON_CANVAS;
  const radius =
    mask === 'circle' ? size / 2 :
    mask === 'squircle' ? size * 0.235 :  // Android adaptive squircle ≈ 25.4dp on 108dp
    mask === 'rounded' ? size * 0.15 :
    0;
  return (
    <div style={{
      width: size, height: size,
      background: c.primary,
      borderRadius: radius,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      boxShadow: shadow ? '0 6px 20px rgba(15, 76, 58, 0.18), 0 1px 2px rgba(0,0,0,0.1)' : 'none',
      overflow: 'hidden',
      position: 'relative',
    }}>
      {/* subtle radial highlight */}
      <div style={{
        position: 'absolute', inset: 0,
        background: 'radial-gradient(circle at 30% 25%, rgba(255,255,255,0.10), transparent 55%)',
        pointerEvents: 'none',
      }} />
      <div style={{ width: markPx, height: markPx, display: 'flex', position: 'relative' }}>
        <Mark theme={{ ...c, primary: c.onPrimary, onPrimary: c.primary, accent: c.accent }} size={markPx} />
      </div>
    </div>
  );
};

const AppIconArtboard = ({ theme, variant = 'A', width = 760, height = 480 }) => {
  const c = theme;
  return (
    <div style={{
      width, height, padding: 32, background: c.bg, color: c.ink,
      fontFamily: SW_TYPE.family, overflow: 'hidden',
    }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
        <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.02em' }}>App Icon · {LOGO_NAMES[variant]}</div>
        <div style={{ fontFamily: SW_TYPE.mono, fontSize: 11, color: c.inkMuted, letterSpacing: '0.04em' }}>ADAPTIVE 108DP</div>
      </div>
      <div style={{ fontSize: 14, color: c.inkMuted, marginBottom: 24 }}>
        Foreground + background layer. Tiap OEM bisa apply mask berbeda — kita test 3.
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 18, marginBottom: 22 }}>
        {[
          { mask: 'squircle', name: 'Squircle', sub: 'Pixel default' },
          { mask: 'circle', name: 'Circle', sub: 'Play Store, Samsung' },
          { mask: 'rounded', name: 'Rounded', sub: 'OnePlus, MIUI' },
        ].map(m => (
          <div key={m.mask} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
            <AppIconComposed theme={c} variant={variant} size={140} mask={m.mask} />
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>{m.name}</div>
              <div style={{ fontSize: 11, color: c.inkSubtle }}>{m.sub}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Layer breakdown */}
      <div style={{ fontSize: 11, fontWeight: 600, color: c.inkSubtle, letterSpacing: '0.08em', marginBottom: 8, textTransform: 'uppercase' }}>Adaptive Layers</div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 14 }}>
        <LayerCard theme={c} title="Foreground" hint="ic_launcher_foreground.xml">
          <div style={{ position: 'relative', width: 96, height: 96 }}>
            <div style={{ position: 'absolute', inset: 0, border: `1.5px dashed ${c.borderStrong}`, borderRadius: '50%', width: 96 * (66/108), height: 96 * (66/108), left: 96 * (21/108), top: 96 * (21/108) }} />
            <div style={{ position: 'absolute', inset: 0 }}>
              <AppIconForeground theme={c} variant={variant} size={96} />
            </div>
          </div>
        </LayerCard>
        <LayerCard theme={c} title="Background" hint="ic_launcher_background.xml">
          <div style={{ width: 96, height: 96, background: c.primary, borderRadius: 8 }} />
        </LayerCard>
        <LayerCard theme={c} title="At 48dp" hint="Launcher size">
          <AppIconComposed theme={c} variant={variant} size={48} mask="squircle" shadow={false} />
        </LayerCard>
        <LayerCard theme={c} title="At 24dp" hint="Notification">
          <AppIconComposed theme={c} variant={variant} size={28} mask="squircle" shadow={false} />
        </LayerCard>
      </div>
    </div>
  );
};

const LayerCard = ({ theme, title, hint, children }) => (
  <div style={{
    background: theme.surface,
    border: `1px solid ${theme.border}`,
    borderRadius: 12,
    padding: 14,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: 8,
  }}>
    <div style={{ height: 96, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{children}</div>
    <div style={{ textAlign: 'center' }}>
      <div style={{ fontSize: 12, fontWeight: 600, color: theme.ink }}>{title}</div>
      <div style={{ fontSize: 10, color: theme.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.02em' }}>{hint}</div>
    </div>
  </div>
);

Object.assign(window, { AppIconForeground, AppIconBackground, AppIconComposed, AppIconArtboard });
