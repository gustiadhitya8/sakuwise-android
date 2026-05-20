// Sakuwise brand tokens — exposed globally for all brand modules.

const SW_LIGHT = {
  bg:         '#F5F1E8',  // cream warm — page bg
  surface:    '#FAF7F0',  // surface (cards)
  surfaceElev:'#FFFFFF',  // elevated surface
  ink:        '#1A2520',  // primary text — deep forest ink
  inkMuted:   '#5C6963',  // secondary text
  inkSubtle:  '#8B948F',  // tertiary / caption
  border:     '#E8E0CC',  // hairlines
  borderStrong:'#D6CDB4',
  primary:    '#0F4C3A',  // deep forest — brand primary
  primaryHover:'#0A3A2C',
  onPrimary:  '#F5F1E8',
  primaryContainer: '#D4E8DC', // soft mint container
  onPrimaryContainer: '#0A2E22',
  accent:     '#7BC4A4',  // mint sage — secondary
  accentSoft: '#D4E8DC',
  success:    '#2D7A4F',
  successSoft:'#D6EDDC',
  warning:    '#C68A2E',
  warningSoft:'#F4E4C8',
  danger:     '#B84545',
  dangerSoft: '#F1D6D6',
  info:       '#4A6FA5',
  infoSoft:   '#D6E0EE',
};

const SW_DARK = {
  bg:         '#0F1411',
  surface:    '#1A211D',
  surfaceElev:'#232B26',
  ink:        '#F0EDE3',
  inkMuted:   '#A8B0AB',
  inkSubtle:  '#6B7570',
  border:     '#2D3631',
  borderStrong:'#3D4742',
  primary:    '#7BC4A4',  // mint becomes primary on dark
  primaryHover:'#9DD4BA',
  onPrimary:  '#0A1F18',
  primaryContainer: '#1F3329',
  onPrimaryContainer: '#C4E8D4',
  accent:     '#C4E8D4',
  accentSoft: '#1F3329',
  success:    '#6DC48F',
  successSoft:'#1E3526',
  warning:    '#E0A954',
  warningSoft:'#3B2E18',
  danger:     '#D67373',
  dangerSoft: '#3D1F1F',
  info:       '#7FA0C7',
  infoSoft:   '#1E2A3A',
};

// Type scale — Figtree humanist sans, optimized for mobile-first reading
const SW_TYPE = {
  family: '"Figtree", system-ui, -apple-system, sans-serif',
  mono:   '"JetBrains Mono", ui-monospace, monospace',
  scale: [
    { name: 'Display L', size: 40, lh: 48, weight: 700, letterSpacing: '-0.02em', use: 'Hero, splash' },
    { name: 'Display M', size: 32, lh: 40, weight: 700, letterSpacing: '-0.02em', use: 'Section headers' },
    { name: 'H1',        size: 26, lh: 34, weight: 700, letterSpacing: '-0.01em', use: 'Screen titles' },
    { name: 'H2',        size: 20, lh: 28, weight: 600, letterSpacing: '-0.005em', use: 'Card titles' },
    { name: 'H3',        size: 17, lh: 24, weight: 600, letterSpacing: '0', use: 'List headers' },
    { name: 'Body L',    size: 16, lh: 24, weight: 400, letterSpacing: '0', use: 'Default body' },
    { name: 'Body',      size: 14, lh: 20, weight: 400, letterSpacing: '0', use: 'Secondary text' },
    { name: 'Caption',   size: 12, lh: 16, weight: 500, letterSpacing: '0.01em', use: 'Meta, labels' },
    { name: 'Amount XL', size: 36, lh: 40, weight: 700, letterSpacing: '-0.02em', use: 'Dashboard amount', tabular: true },
    { name: 'Amount L',  size: 22, lh: 28, weight: 600, letterSpacing: '-0.01em', use: 'Card amount', tabular: true },
    { name: 'Amount',    size: 16, lh: 22, weight: 600, letterSpacing: '0', use: 'List amount', tabular: true },
  ],
};

const SW_RADII = { xs: 4, sm: 8, md: 12, lg: 16, xl: 20, '2xl': 28, full: 9999 };
const SW_SPACE = [0, 4, 8, 12, 16, 20, 24, 32, 40, 48, 64];

// Brand voice
const SW_TAGLINE = 'Rencanakan. Catat. Tenang.';
const SW_NAME = 'Sakuwise';
const SW_NAME_MEAN = 'saku (kantong) + wise (bijak)';

Object.assign(window, { SW_LIGHT, SW_DARK, SW_TYPE, SW_RADII, SW_SPACE, SW_TAGLINE, SW_NAME, SW_NAME_MEAN });
