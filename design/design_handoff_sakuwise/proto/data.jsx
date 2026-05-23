// Sakuwise — sample data for the prototype.

const SW_FORMAT = {
  rp: (n) => 'Rp ' + new Intl.NumberFormat('id-ID').format(Math.round(n)),
  rpShort: (n) => {
    const a = Math.abs(n);
    if (a >= 1e9) return 'Rp ' + (n / 1e9).toFixed(1).replace(/\.0$/, '') + ' M';
    if (a >= 1e6) return 'Rp ' + (n / 1e6).toFixed(1).replace(/\.0$/, '') + ' jt';
    if (a >= 1e3) return 'Rp ' + (n / 1e3).toFixed(0) + 'rb';
    return 'Rp ' + n;
  },
  date: (d) => {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];
    return `${d.getDate()} ${months[d.getMonth()]} ${d.getFullYear()}`;
  },
  dateRel: (d) => {
    const now = new Date();
    const diff = Math.floor((now - d) / 86400000);
    if (diff === 0) return 'Hari ini';
    if (diff === 1) return 'Kemarin';
    if (diff < 7) return `${diff} hari lalu`;
    return SW_FORMAT.date(d);
  },
};

// Accounts
const SW_ACCOUNTS = [
  { id: 'mandiri', name: 'Mandiri', type: 'Bank', icon: 'bank', balance: 12450000 },
  { id: 'bca',     name: 'BCA',     type: 'Bank', icon: 'bank', balance: 8200000 },
  { id: 'gopay',   name: 'GoPay',   type: 'E-Wallet', icon: 'wallet', balance: 280000 },
  { id: 'tunai',   name: 'Tunai',   type: 'Tunai', icon: 'cash', balance: 540000 },
];

// AccountSnapshot history — one per reconciliation event
// Fields: tanggal, saldo_aplikasi (terkomputasi saat itu), saldo_asli (input user), selisih
const SW_ACCOUNT_SNAPSHOTS = {
  mandiri: [
    { id: 'sn1',  date: new Date(2026, 4, 1),  appBalance: 14250000, realBalance: 14225000, diff: -25000,  note: 'PBB online lupa catat' },
    { id: 'sn2',  date: new Date(2026, 3, 1),  appBalance: 11800000, realBalance: 11800000, diff: 0,       note: null },
    { id: 'sn3',  date: new Date(2026, 2, 1),  appBalance: 9450000,  realBalance: 9492500,  diff: 42500,   note: 'Bunga tabungan' },
    { id: 'sn4',  date: new Date(2026, 1, 1),  appBalance: 7820000,  realBalance: 7820000,  diff: 0,       note: null },
    { id: 'sn5',  date: new Date(2026, 0, 1),  appBalance: 5640000,  realBalance: 5605000,  diff: -35000,  note: 'Biaya admin' },
    { id: 'sn6',  date: new Date(2025, 11, 1), appBalance: 3120000,  realBalance: 3120000,  diff: 0,       note: null },
  ],
  bca: [
    { id: 'sn7',  date: new Date(2026, 4, 1),  appBalance: 8200000, realBalance: 8200000, diff: 0,      note: null },
    { id: 'sn8',  date: new Date(2026, 3, 1),  appBalance: 6500000, realBalance: 6485000, diff: -15000, note: 'Biaya transfer' },
    { id: 'sn9',  date: new Date(2026, 2, 1),  appBalance: 4200000, realBalance: 4200000, diff: 0,      note: null },
  ],
  gopay: [
    { id: 'sn10', date: new Date(2026, 4, 10), appBalance: 280000, realBalance: 280000, diff: 0,      note: null },
    { id: 'sn11', date: new Date(2026, 3, 8),  appBalance: 450000, realBalance: 444000, diff: -6000,  note: 'Cashback masuk berbeda' },
  ],
  tunai: [
    { id: 'sn12', date: new Date(2026, 4, 1),  appBalance: 540000, realBalance: 520000, diff: -20000, note: 'Jajan lupa input' },
    { id: 'sn13', date: new Date(2026, 3, 1),  appBalance: 320000, realBalance: 320000, diff: 0,      note: null },
  ],
};

// Income for the month (Mei 2026)
const SW_INCOME_MONTH = 15500000;

// Allocations 50/30/20 with current actuals
const SW_ALLOCATIONS = [
  {
    id: 'needs', name: 'Needs', target: 50, plan: 7750000, used: 5180000,
    categories: [
      { id: 'home', name: 'Tempat Tinggal', plan: 3500000, used: 2950000, items: [
        { id: 'sewa', name: 'Sewa/Cicilan Rumah', plan: 2500000, used: 2500000, recurring: 'monthly' },
        { id: 'listrik', name: 'Listrik', plan: 350000, used: 285000, recurring: 'monthly' },
        { id: 'air', name: 'Air PAM', plan: 150000, used: 165000, recurring: 'monthly', over: true },
        { id: 'inet', name: 'Internet', plan: 350000, used: 0, recurring: 'monthly' },
        { id: 'galon', name: 'Air Galon', plan: 150000, used: 0, recurring: 'monthly' },
      ]},
      { id: 'food', name: 'Makanan', plan: 2200000, used: 1480000, items: [
        { id: 'harian', name: 'Makan Harian', plan: 1500000, used: 980000, recurring: 'monthly' },
        { id: 'belanja', name: 'Belanja Bulanan', plan: 700000, used: 500000, recurring: 'monthly' },
      ]},
      { id: 'transport', name: 'Transportasi', plan: 1200000, used: 470000, items: [
        { id: 'bbm', name: 'BBM', plan: 700000, used: 320000, recurring: 'monthly' },
        { id: 'gojek', name: 'Transportasi Online', plan: 500000, used: 150000, recurring: 'monthly' },
      ]},
      { id: 'comm', name: 'Komunikasi', plan: 250000, used: 200000, items: [
        { id: 'pulsa', name: 'Pulsa & Paket Data', plan: 250000, used: 200000, recurring: 'monthly' },
      ]},
      { id: 'health', name: 'Kesehatan', plan: 600000, used: 80000, items: [
        { id: 'bpjs', name: 'BPJS Kesehatan', plan: 250000, used: 0, recurring: 'monthly' },
        { id: 'obat', name: 'Obat-obatan', plan: 350000, used: 80000, recurring: 'one-off' },
      ]},
    ],
  },
  {
    id: 'wants', name: 'Wants', target: 30, plan: 4650000, used: 3120000,
    categories: [
      { id: 'leisure', name: 'Hiburan', plan: 800000, used: 535000, items: [
        { id: 'netflix', name: 'Streaming', plan: 200000, used: 200000, recurring: 'monthly' },
        { id: 'bioskop', name: 'Bioskop', plan: 250000, used: 175000, recurring: 'one-off' },
        { id: 'game', name: 'Gaming', plan: 350000, used: 160000, recurring: 'one-off' },
      ]},
      { id: 'eatout', name: 'Makan di Luar', plan: 1500000, used: 1240000, items: [
        { id: 'kopi', name: 'Kopi/Kafe', plan: 600000, used: 580000, recurring: 'monthly' },
        { id: 'resto', name: 'Restoran', plan: 700000, used: 510000, recurring: 'monthly' },
        { id: 'jajan', name: 'Jajan', plan: 200000, used: 150000, recurring: 'monthly' },
      ]},
      { id: 'shop', name: 'Belanja', plan: 1500000, used: 950000, items: [
        { id: 'baju', name: 'Pakaian', plan: 800000, used: 650000, recurring: 'one-off' },
        { id: 'elek', name: 'Elektronik', plan: 700000, used: 300000, recurring: 'one-off' },
      ]},
      { id: 'self', name: 'Self Care', plan: 850000, used: 395000, items: [
        { id: 'skin', name: 'Skincare', plan: 600000, used: 350000, recurring: 'one-off' },
        { id: 'salon', name: 'Salon / Barbershop', plan: 250000, used: 45000, recurring: 'monthly' },
      ]},
    ],
  },
  {
    id: 'invest', name: 'Investment', target: 20, plan: 3100000, used: 2500000,
    categories: [
      { id: 'tabungan', name: 'Tabungan', plan: 1500000, used: 1500000, items: [
        { id: 'darurat', name: 'Dana Darurat', plan: 500000, used: 500000, recurring: 'monthly' },
        { id: 'tabreg', name: 'Tabungan Reguler', plan: 1000000, used: 1000000, recurring: 'monthly' },
      ]},
      { id: 'inv', name: 'Investasi', plan: 1300000, used: 1000000, items: [
        { id: 'emas', name: 'Emas', plan: 500000, used: 500000, recurring: 'monthly' },
        { id: 'reksa', name: 'Reksa Dana / Saham', plan: 500000, used: 500000, recurring: 'monthly' },
        { id: 'dplk', name: 'DPLK Tambahan', plan: 300000, used: 0, recurring: 'monthly' },
      ]},
      { id: 'edu', name: 'Pendidikan', plan: 300000, used: 0, items: [
        { id: 'kursus', name: 'Kursus Online', plan: 300000, used: 0, recurring: 'one-off' },
      ]},
    ],
  },
];

// Recent transactions
const today = new Date(2026, 4, 15); // Mei 15
const addDays = (d, n) => { const x = new Date(d); x.setDate(x.getDate() + n); return x; };

const SW_TRANSACTIONS = [
  { id: 't1',  date: today,             type: 'expense', cat: 'Kopi/Kafe',     merchant: 'Kopi Kenangan',     account: 'gopay',   amount: 28000,    alloc: 'wants' },
  { id: 't2',  date: today,             type: 'expense', cat: 'Makan Harian',  merchant: 'Warteg Bahari',     account: 'tunai',   amount: 35000,    alloc: 'needs' },
  { id: 't3',  date: addDays(today,-1), type: 'expense', cat: 'Listrik',       merchant: 'PLN',               account: 'mandiri', amount: 285000,   alloc: 'needs' },
  { id: 't4',  date: addDays(today,-1), type: 'expense', cat: 'BBM',           merchant: 'Pertamina',         account: 'mandiri', amount: 150000,   alloc: 'needs' },
  { id: 't5',  date: addDays(today,-2), type: 'income',  cat: 'Gaji Pokok',    merchant: 'PT. Sumber Karya',  account: 'mandiri', amount: 15000000 },
  { id: 't6',  date: addDays(today,-3), type: 'expense', cat: 'Restoran',      merchant: 'Sushi Tei',         account: 'bca',     amount: 220000,   alloc: 'wants' },
  { id: 't7',  date: addDays(today,-3), type: 'transfer',cat: 'Transfer',      merchant: 'BCA → Mandiri',     account: 'bca',     amount: 2000000 },
  { id: 't8',  date: addDays(today,-4), type: 'expense', cat: 'Streaming',     merchant: 'Netflix',           account: 'bca',     amount: 200000,   alloc: 'wants' },
  { id: 't9',  date: addDays(today,-5), type: 'expense', cat: 'Skincare',      merchant: 'Watsons',           account: 'gopay',   amount: 350000,   alloc: 'wants' },
  { id: 't10', date: addDays(today,-6), type: 'expense', cat: 'Air PAM',       merchant: 'PDAM',              account: 'mandiri', amount: 165000,   alloc: 'needs' },
];

// Top 5 spending categories this period
const SW_TOP_CATEGORIES = [
  { name: 'Sewa/Cicilan',      amount: 2500000, color: 'primary'  },
  { name: 'Makan Harian',      amount: 980000,  color: 'accent'   },
  { name: 'Tabungan Reguler',  amount: 1000000, color: 'info'     },
  { name: 'Kopi/Kafe',         amount: 580000,  color: 'warning'  },
  { name: 'Restoran',          amount: 510000,  color: 'danger'   },
];

// Investasi snapshot
const SW_INVEST = {
  gold:   { value: 18500000, profit: 2300000, profitPct: 14.2, weight: 25 },
  land:   { value: 450000000, profit: 50000000, profitPct: 12.5, count: 1 },
  deposit:{ value: 87500000, growth: 6.8, count: 3 },
};

const SW_DEBT = { iOwe: 220000000, owedToMe: 5000000 };

const SW_NETWORTH = 
  SW_ACCOUNTS.reduce((s, a) => s + a.balance, 0) +
  SW_INVEST.gold.value + SW_INVEST.land.value + SW_INVEST.deposit.value -
  SW_DEBT.iOwe;

// Period info
const SW_PERIOD = {
  label: 'Plan Mei 2026',
  start: new Date(2026, 4, 1),
  end:   new Date(2026, 4, 31),
  daysLeft: 16,
  daysTotal: 31,
};

// Detailed asset records
const SW_GOLD = [
  { id: 'g1', date: new Date(2024, 5, 12),  weight: 10, buyPrice: 9800000,  serial: 'ANTAM-12345', status: 'held' },
  { id: 'g2', date: new Date(2024, 10, 3),  weight: 5,  buyPrice: 5100000,  serial: 'UBS-67890',    status: 'held' },
  { id: 'g3', date: new Date(2025, 2, 18),  weight: 10, buyPrice: 10500000, serial: 'ANTAM-22441',  status: 'held' },
];

const SW_GOLD_PRICE = 1_050_000; // per gram, manual

const SW_LAND = [
  { id: 'l1', name: 'Sawah Tegal', location: 'Tegal, Jawa Tengah', shm: 'SHM 1234', size: 1500, buyPrice: 250000000, currentValue: 350000000, status: 'held', taxes: [
    { id: 'tx1', date: new Date(2026, 0, 20), amount: 850000, note: 'PBB 2026' },
    { id: 'tx2', date: new Date(2025, 0, 18), amount: 800000, note: 'PBB 2025' },
    { id: 'tx3', date: new Date(2024, 0, 22), amount: 750000, note: 'PBB 2024' },
  ]},
  { id: 'l2', name: 'Rumah Jakarta', location: 'Jakarta Selatan', shm: 'SHM 5678', size: 120, buyPrice: 1200000000, currentValue: 1450000000, status: 'held', taxes: [
    { id: 'tx4', date: new Date(2026, 1, 12), amount: 3200000, note: 'PBB 2026' },
  ]},
];

const SW_DEPOSIT = [
  { id: 'd1', name: 'BPJSTK JHT',     type: 'BPJSTK', institution: 'BPJS Ketenagakerjaan', snapshots: [
    { date: new Date(2024, 0, 1),  balance: 28000000 },
    { date: new Date(2024, 3, 1),  balance: 31500000 },
    { date: new Date(2024, 6, 1),  balance: 35200000 },
    { date: new Date(2024, 9, 1),  balance: 39400000 },
    { date: new Date(2025, 0, 1),  balance: 43800000 },
    { date: new Date(2025, 3, 1),  balance: 48500000 },
    { date: new Date(2025, 6, 1),  balance: 53600000 },
    { date: new Date(2025, 9, 1),  balance: 58900000 },
    { date: new Date(2026, 0, 1),  balance: 64500000 },
    { date: new Date(2026, 3, 1),  balance: 71000000 },
  ]},
  { id: 'd2', name: 'DPLK Mandiri',   type: 'DPLK',   institution: 'Bank Mandiri', snapshots: [
    { date: new Date(2025, 0, 1), balance: 8000000 },
    { date: new Date(2025, 6, 1), balance: 11500000 },
    { date: new Date(2026, 0, 1), balance: 15200000 },
  ]},
  { id: 'd3', name: 'Deposito BCA',   type: 'Deposito', institution: 'Bank BCA', snapshots: [
    { date: new Date(2025, 6, 1), balance: 15000000 },
    { date: new Date(2026, 0, 1), balance: 15500000 },
  ]},
];

const SW_DEBTS = [
  { id: 'db1', counterparty: 'KPR Bank Mandiri', direction: 'i_owe', principal: 240000000, dueDate: new Date(2034, 5, 15), startDate: new Date(2024, 5, 15), status: 'open', payments: [
    { id: 'pm1', date: new Date(2026, 4, 10), amount: 2500000, account: 'mandiri', txId: 'tx-101' },
    { id: 'pm2', date: new Date(2026, 3, 10), amount: 2500000, account: 'mandiri', txId: 'tx-100' },
    { id: 'pm3', date: new Date(2026, 2, 10), amount: 2500000, account: 'mandiri', txId: 'tx-099' },
  ]},
  { id: 'db2', counterparty: 'Adik (Pinjaman)', direction: 'owed_to_me', principal: 5000000, dueDate: null, startDate: new Date(2025, 11, 20), status: 'open', payments: [
    { id: 'pm4', date: new Date(2026, 2, 5), amount: 500000, account: 'bca' },
  ]},
];

// Net worth history (monthly snapshots — would be derived from txn history in app)
const SW_NETWORTH_HISTORY = [
  { date: new Date(2025, 5, 1),  value: 1450000000 },
  { date: new Date(2025, 6, 1),  value: 1478000000 },
  { date: new Date(2025, 7, 1),  value: 1495000000 },
  { date: new Date(2025, 8, 1),  value: 1510000000 },
  { date: new Date(2025, 9, 1),  value: 1538000000 },
  { date: new Date(2025, 10, 1), value: 1562000000 },
  { date: new Date(2025, 11, 1), value: 1590000000 },
  { date: new Date(2026, 0, 1),  value: 1612000000 },
  { date: new Date(2026, 1, 1),  value: 1638000000 },
  { date: new Date(2026, 2, 1),  value: 1665000000 },
  { date: new Date(2026, 3, 1),  value: 1688000000 },
  { date: new Date(2026, 4, 15), value: 1716920000 },
];

Object.assign(window, {
  SW_GOLD, SW_GOLD_PRICE, SW_LAND, SW_DEPOSIT, SW_DEBTS,
  SW_FORMAT, SW_ACCOUNTS, SW_ACCOUNT_SNAPSHOTS, SW_INCOME_MONTH, SW_ALLOCATIONS, SW_TRANSACTIONS,
  SW_TOP_CATEGORIES, SW_INVEST, SW_DEBT, SW_NETWORTH, SW_PERIOD,
  SW_NETWORTH_HISTORY,
});
