// Sakuwise — 3 logo concepts (symbol-led per user direction).
// Each accepts { theme, size, variant } where theme is a token object,
// variant ∈ 'mark' | 'lockup-h' | 'lockup-v' | 'mono'.

const LogoA_Daun = ({ theme, size = 64 }) => {
  // Daun — pouch/saku with a leaf inscribed. The vein creates a soft visual axis.
  const c = theme || SW_LIGHT;
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-label="Sakuwise — Daun">
      <rect x="4" y="4" width="56" height="56" rx="18" fill={c.primary} />
      {/* leaf */}
      <path d="M 32 13 C 50 20 50 44 32 51 C 14 44 14 20 32 13 Z" fill={c.onPrimary} />
      {/* vein */}
      <path d="M 32 16 L 32 49" stroke={c.primary} strokeWidth="2.5" strokeLinecap="round" />
      {/* side veins, subtle */}
      <path d="M 32 26 Q 38 28 41 32" stroke={c.primary} strokeWidth="2" strokeLinecap="round" fill="none" opacity="0.45" />
      <path d="M 32 36 Q 38 38 41 42" stroke={c.primary} strokeWidth="2" strokeLinecap="round" fill="none" opacity="0.45" />
      <path d="M 32 26 Q 26 28 23 32" stroke={c.primary} strokeWidth="2" strokeLinecap="round" fill="none" opacity="0.45" />
      <path d="M 32 36 Q 26 38 23 42" stroke={c.primary} strokeWidth="2" strokeLinecap="round" fill="none" opacity="0.45" />
    </svg>
  );
};

const LogoB_Lingkar = ({ theme, size = 64 }) => {
  // Lingkar S — coin with S-flow negative space. Reads as balance + S monogram.
  const c = theme || SW_LIGHT;
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-label="Sakuwise — Lingkar S">
      <circle cx="32" cy="32" r="27" fill={c.primary} />
      {/* S-curve sweep: filled wedge formed by two opposing semicircles */}
      <path
        d="M 32 5 A 27 27 0 0 1 32 59 A 13.5 13.5 0 0 0 32 32 A 13.5 13.5 0 0 1 32 5 Z"
        fill={c.onPrimary}
      />
      {/* Tiny counter-coins for balance */}
      <circle cx="32" cy="18.5" r="3.2" fill={c.primary} />
      <circle cx="32" cy="45.5" r="3.2" fill={c.onPrimary} />
    </svg>
  );
};

const LogoC_Lipat = ({ theme, size = 64 }) => {
  // Lipat — folded saku/wallet corner revealing inner accent.
  const c = theme || SW_LIGHT;
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-label="Sakuwise — Lipat">
      {/* main saku */}
      <path d="M 18 6 H 50 a8 8 0 0 1 8 8 V 50 a8 8 0 0 1 -8 8 H 14 a8 8 0 0 1 -8 -8 V 22 Z" fill={c.primary} />
      {/* folded flap triangle revealing accent */}
      <path d="M 18 6 L 6 22 H 18 Z" fill={c.accent} />
      {/* fold crease */}
      <path d="M 18 6 L 6 22" stroke={c.primary} strokeWidth="1.5" strokeLinecap="round" opacity="0.5" />
      {/* tiny coin inside */}
      <circle cx="32" cy="36" r="9" fill="none" stroke={c.onPrimary} strokeWidth="2.5" />
      <path d="M 32 31 V 41 M 28.5 33 H 35.5 a 2 2 0 1 1 0 4 H 28.5 a 2 2 0 1 0 0 4 H 35.5" stroke={c.onPrimary} strokeWidth="1.6" strokeLinecap="round" fill="none" />
    </svg>
  );
};

const LOGO_MAP = { A: LogoA_Daun, B: LogoB_Lingkar, C: LogoC_Lipat };
const LOGO_NAMES = { A: 'Daun', B: 'Lingkar S', C: 'Lipat' };
const LOGO_MEAN = {
  A: 'Saku tempat tumbuh. Daun = pertumbuhan + ketenangan.',
  B: 'Koin yang mengalir. S = Sakuwise, putaran = keseimbangan.',
  C: 'Dompet terlipat. Sederhana, modern, fungsional.',
};

// Wordmark — uses Figtree with custom letter tracking
const Wordmark = ({ theme, size = 32, weight = 700 }) => {
  const c = theme || SW_LIGHT;
  return (
    <span style={{
      fontFamily: SW_TYPE.family,
      fontWeight: weight,
      fontSize: size,
      letterSpacing: '-0.025em',
      color: c.ink,
      lineHeight: 1,
      display: 'inline-block',
    }}>
      Saku<span style={{ color: c.primary }}>wise</span>
    </span>
  );
};

// Horizontal lockup: mark + wordmark
const Lockup = ({ theme, variant = 'A', size = 40, layout = 'h', showTagline = false }) => {
  const c = theme || SW_LIGHT;
  const Mark = LOGO_MAP[variant];
  if (layout === 'v') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: size * 0.4 }}>
        <Mark theme={c} size={size * 1.6} />
        <Wordmark theme={c} size={size * 0.75} />
        {showTagline && (
          <span style={{ fontFamily: SW_TYPE.family, fontSize: size * 0.32, color: c.inkMuted, letterSpacing: '0.02em', fontWeight: 500 }}>
            {SW_TAGLINE}
          </span>
        )}
      </div>
    );
  }
  return (
    <div style={{ display: 'inline-flex', alignItems: 'center', gap: size * 0.35 }}>
      <Mark theme={c} size={size} />
      <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <Wordmark theme={c} size={size * 0.72} />
        {showTagline && (
          <span style={{ fontFamily: SW_TYPE.family, fontSize: size * 0.28, color: c.inkMuted, letterSpacing: '0.02em', fontWeight: 500 }}>
            {SW_TAGLINE}
          </span>
        )}
      </div>
    </div>
  );
};

Object.assign(window, { LogoA_Daun, LogoB_Lingkar, LogoC_Lipat, LOGO_MAP, LOGO_NAMES, LOGO_MEAN, Wordmark, Lockup });
