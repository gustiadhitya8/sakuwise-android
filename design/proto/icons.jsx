// Sakuwise — line icon set. 24x24 viewBox, 1.75 stroke, round caps.
// Pure SVG so they scale to any size and theme via currentColor.

const Icon = ({ name, size = 24, color = 'currentColor', strokeWidth = 1.75, ...rest }) => {
  const Comp = ICONS[name];
  if (!Comp) return null;
  return (
    <svg
      width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round"
      aria-hidden="true" {...rest}
    >
      <Comp />
    </svg>
  );
};

const IconFilled = ({ name, size = 24, color = 'currentColor', ...rest }) => {
  const Comp = ICONS_FILLED[name] || ICONS[name];
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill={color}
         stroke="none" aria-hidden="true" {...rest}>
      <Comp />
    </svg>
  );
};

// — Tab bar icons (outline + filled variants used for active state)
const ICONS = {
  // home
  home: () => <><path d="M4 11 L12 4 L20 11 V20 a1 1 0 0 1 -1 1 H5 a1 1 0 0 1 -1 -1 Z" /><path d="M10 21 V14 H14 V21" /></>,
  // plan / list with checks
  plan: () => <><path d="M4 6 H14" /><path d="M4 12 H14" /><path d="M4 18 H10" /><path d="M17 5 L19 7 L22 4" /><path d="M17 11 L19 13 L22 10" /></>,
  // plus (FAB-style)
  plus: () => <><path d="M12 5 V19" /><path d="M5 12 H19" /></>,
  // assets / coins stack
  assets: () => <><ellipse cx="12" cy="6" rx="7" ry="2.5" /><path d="M5 6 V11 c0 1.4 3.1 2.5 7 2.5 s7-1.1 7-2.5 V6" /><path d="M5 11 V16 c0 1.4 3.1 2.5 7 2.5 s7-1.1 7-2.5 V11" /><path d="M5 16 V19 c0 1.4 3.1 2.5 7 2.5 s7-1.1 7-2.5 V16" /></>,
  // me / profile circle
  me: () => <><circle cx="12" cy="8.5" r="3.5" /><path d="M5 20 c1.5-3.5 4.2-5 7-5 s5.5 1.5 7 5" /></>,

  // — Navigation / chrome
  back: () => <><path d="M15 5 L8 12 L15 19" /></>,
  close: () => <><path d="M6 6 L18 18" /><path d="M18 6 L6 18" /></>,
  more: () => <><circle cx="5" cy="12" r="1" /><circle cx="12" cy="12" r="1" /><circle cx="19" cy="12" r="1" /></>,
  search: () => <><circle cx="11" cy="11" r="6" /><path d="M16 16 L21 21" /></>,
  chevron_right: () => <><path d="M9 6 L15 12 L9 18" /></>,
  chevron_down: () => <><path d="M6 9 L12 15 L18 9" /></>,
  chevron_up: () => <><path d="M6 15 L12 9 L18 15" /></>,

  // — Actions
  edit: () => <><path d="M4 20 L8 19 L19 8 L16 5 L5 16 Z" /><path d="M14 7 L17 10" /></>,
  trash: () => <><path d="M5 7 H19" /><path d="M9 7 V5 a1 1 0 0 1 1 -1 H14 a1 1 0 0 1 1 1 V7" /><path d="M7 7 L8 20 a1 1 0 0 0 1 1 H15 a1 1 0 0 0 1 -1 L17 7" /></>,
  copy: () => <><rect x="8" y="8" width="12" height="12" rx="2" /><path d="M16 8 V6 a2 2 0 0 0 -2 -2 H6 a2 2 0 0 0 -2 2 V14 a2 2 0 0 0 2 2 H8" /></>,
  check: () => <><path d="M5 12 L10 17 L19 7" /></>,
  camera: () => <><path d="M4 8 H7 L9 5 H15 L17 8 H20 a1 1 0 0 1 1 1 V18 a1 1 0 0 1 -1 1 H4 a1 1 0 0 1 -1 -1 V9 a1 1 0 0 1 1 -1 Z" /><circle cx="12" cy="13" r="3.5" /></>,
  calendar: () => <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M3 10 H21" /><path d="M8 3 V7" /><path d="M16 3 V7" /></>,
  filter: () => <><path d="M4 5 H20" /><path d="M7 12 H17" /><path d="M10 19 H14" /></>,
  bell: () => <><path d="M6 16 V11 a6 6 0 0 1 12 0 V16 L20 18 H4 Z" /><path d="M10 21 a2 2 0 0 0 4 0" /></>,
  shield: () => <><path d="M12 3 L20 6 V12 c0 4 -3 7.5 -8 9 c-5 -1.5 -8 -5 -8 -9 V6 Z" /></>,
  eye: () => <><path d="M2 12 C5 6 9 4 12 4 s7 2 10 8 c-3 6 -7 8 -10 8 s-7 -2 -10 -8 Z" /><circle cx="12" cy="12" r="3" /></>,
  eye_off: () => <><path d="M3 12 c1.6 -3.2 3.7 -5.3 6 -6.5" /><path d="M9.7 5 a8 8 0 0 1 2.3 -0.3 c4 0 7 2.5 9 7 -0.7 1.6 -1.6 2.9 -2.6 4" /><path d="M14.5 14 a3 3 0 0 1 -4.2 -4.2" /><path d="M4 4 L20 20" /></>,

  // — Transaction types
  expense: () => <><circle cx="12" cy="12" r="9" /><path d="M12 7 V17" /><path d="M8 13 L12 17 L16 13" /></>,
  income: () => <><circle cx="12" cy="12" r="9" /><path d="M12 17 V7" /><path d="M8 11 L12 7 L16 11" /></>,
  transfer: () => <><circle cx="12" cy="12" r="9" /><path d="M7 10 H17 L14 7" /><path d="M17 14 H7 L10 17" /></>,

  // — Account types
  cash: () => <><rect x="3" y="7" width="18" height="11" rx="2" /><circle cx="12" cy="12.5" r="2.5" /></>,
  bank: () => <><path d="M3 21 H21" /><path d="M5 10 H19 V18 H5 Z" /><path d="M5 10 L12 4 L19 10" /><path d="M9 13 V16 M12 13 V16 M15 13 V16" /></>,
  wallet: () => <><path d="M4 7 a2 2 0 0 1 2 -2 H17 a1 1 0 0 1 1 1 V8" /><path d="M4 7 V18 a2 2 0 0 0 2 2 H18 a2 2 0 0 0 2 -2 V11 a1 1 0 0 0 -1 -1 H5 a1 1 0 0 1 -1 -1 V7" /><circle cx="16" cy="14.5" r="1.3" fill="currentColor" stroke="none" /></>,

  // — Asset types (for investasi)
  gold: () => <><path d="M6 6 H18 L20 11 L12 21 L4 11 Z" /><path d="M6 6 L8 11 H16 L18 6" /><path d="M4 11 H20" /><path d="M12 11 V21" /></>,
  land: () => <><path d="M3 19 H21" /><path d="M5 19 V13 L9 10 L13 13 V19" /><path d="M13 19 V11 L18 8 L21 11 V19" /><path d="M9 19 V15 M16 19 V13" /></>,
  deposit: () => <><circle cx="12" cy="12" r="9" /><path d="M9 9 H13.5 a2 2 0 1 1 0 4 H10 a2 2 0 1 0 0 4 H15" /></>,

  // — Misc
  link: () => <><path d="M10 14 L14 10" /><path d="M7 13 L4 16 a3 3 0 0 0 4.2 4.2 L11 17.5" /><path d="M13 6.5 L15.8 3.8 a3 3 0 0 1 4.2 4.2 L17 11" /></>,
  receipt: () => <><path d="M6 3 L7 4 L8 3 L9 4 L10 3 L11 4 L12 3 L13 4 L14 3 L15 4 L16 3 L17 4 L18 3 V21 L17 20 L16 21 L15 20 L14 21 L13 20 L12 21 L11 20 L10 21 L9 20 L8 21 L7 20 L6 21 Z" /><path d="M9 9 H15" /><path d="M9 13 H15" /></>,
  // Quick add types
  arrow_up_right: () => <><path d="M7 17 L17 7" /><path d="M8 7 H17 V16" /></>,
  arrow_down_left: () => <><path d="M17 7 L7 17" /><path d="M16 17 H7 V8" /></>,
  swap: () => <><path d="M5 8 H17 L14 5" /><path d="M19 16 H7 L10 19" /></>,

  // — Brand quick mark (Daun simplified for inline)
  leaf: () => <><path d="M12 4 C19 7 19 17 12 20 C5 17 5 7 12 4 Z" /><path d="M12 6 V18" /></>,
  warning: () => <><path d="M12 3 L22 20 H2 Z" /><path d="M12 10 V14" /><circle cx="12" cy="17" r="0.6" fill="currentColor" /></>,
  info: () => <><circle cx="12" cy="12" r="9" /><path d="M12 11 V16" /><circle cx="12" cy="8" r="0.6" fill="currentColor" /></>,
  sparkle: () => <><path d="M12 4 L13.5 10 L19 11.5 L13.5 13 L12 19 L10.5 13 L5 11.5 L10.5 10 Z" /></>,
};

// Filled variants for active tabs (just key tab icons)
const ICONS_FILLED = {
  home: () => <path d="M4 11 L12 4 L20 11 V20 a1 1 0 0 1 -1 1 H14 V14 H10 V21 H5 a1 1 0 0 1 -1 -1 Z" />,
  plan: () => <><path d="M3 5 H15 V7 H3 Z" /><path d="M3 11 H15 V13 H3 Z" /><path d="M3 17 H11 V19 H3 Z" /><path d="M17 5 L19 7 L22 4 L23.4 5.4 L19 9.8 L15.6 6.4 Z" /></>,
  assets: () => <><ellipse cx="12" cy="6" rx="7" ry="2.5" /><path d="M5 6 V11 c0 1.4 3.1 2.5 7 2.5 s7-1.1 7-2.5 V6 a1 1 0 0 0 0 0 z" /><path d="M5 11 V16 c0 1.4 3.1 2.5 7 2.5 s7-1.1 7-2.5 V11" /><path d="M5 16 V19 c0 1.4 3.1 2.5 7 2.5 s7-1.1 7-2.5 V16" /></>,
  me: () => <><circle cx="12" cy="8" r="4" /><path d="M4 21 c1.5-4.5 4.5-6.5 8-6.5 s6.5 2 8 6.5 Z" /></>,
};

Object.assign(window, { Icon, IconFilled });
