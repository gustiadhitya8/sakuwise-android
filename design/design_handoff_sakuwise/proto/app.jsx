// Sakuwise — main prototype app

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "theme": "light",
  "showOnboarding": false
}/*EDITMODE-END*/;

// ─── Success toast ──────────────────────────────────────────

const Toast = ({ c, message, onClose }) => {
  React.useEffect(() => {
    if (!message) return;
    const t = setTimeout(onClose, 2200);
    return () => clearTimeout(t);
  }, [message, onClose]);
  if (!message) return null;
  return (
    <div style={{
      position: 'absolute', left: 16, right: 16, bottom: 100, zIndex: 20,
      background: c.ink, color: c.bg,
      borderRadius: 14, padding: '12px 16px',
      display: 'flex', alignItems: 'center', gap: 10,
      boxShadow: '0 12px 30px rgba(0,0,0,0.25)',
      animation: 'sw-fade-up 220ms ease',
      fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 600,
    }}>
      <div style={{
        width: 24, height: 24, borderRadius: '50%',
        background: c.success, color: '#fff',
        display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
      }}>
        <Icon name="check" size={14} strokeWidth={2.5} />
      </div>
      {message}
    </div>
  );
};

// ─── App shell ──────────────────────────────────────────────

const App = () => {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const c = t.theme === 'dark' ? SW_DARK : SW_LIGHT;

  const [tab, setTab] = React.useState('home');       // home | plan | assets | me
  const [overlay, setOverlay] = React.useState(null); // 'picker' | 'expense' | 'income' | 'transfer' | 'ocr' | 'reconcile' | null
  const [toast, setToast] = React.useState(null);
  const [showOnboarding, setShowOnboarding] = React.useState(t.showOnboarding === true);
  const [assetsSub, setAssetsSub] = React.useState(null);  // sub-nav within Aset
  const [meSub, setMeSub] = React.useState(null);          // sub-nav within Saya/Settings
  const [reconcileAcct, setReconcileAcct] = React.useState(null);

  // Sync body class so page bg follows theme
  React.useEffect(() => {
    document.body.classList.toggle('theme-dark', t.theme === 'dark');
  }, [t.theme]);

  // When switching tabs, reset their internal sub-nav
  const navTab = (next) => {
    setTab(next);
    if (next !== 'assets') setAssetsSub(null);
    if (next !== 'me') setMeSub(null);
  };

  const openAdd = () => setOverlay('picker');
  const onPickType = (type) => {
    if (type === 'expense')  setOverlay('expense');
    if (type === 'income')   setOverlay('income');
    if (type === 'transfer') setOverlay('transfer');
    if (type === 'ocr')      setOverlay('ocr');
  };
  const closeOverlay = () => setOverlay(null);
  const saveTxn = (label) => () => {
    setOverlay(null);
    setToast(`${label} tersimpan`);
  };

  const tabContent = {
    home:   <DashboardScreen c={c} onNav={navTab} />,
    plan:   <PlanScreen c={c} onNav={navTab} />,
    assets: <AssetsHubScreen c={c} onNav={navTab} sub={assetsSub} setSub={setAssetsSub} />,
    me:     <SettingsHub c={c} onNav={navTab} sub={meSub} setSub={setMeSub} onStartOnboarding={() => setShowOnboarding(true)} />,
  }[tab];

  // Onboarding takes over the whole phone
  if (showOnboarding) {
    return (
      <React.Fragment>
        <div style={{
          minHeight: '100dvh', width: '100%',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          padding: '40px 20px',
          background: t.theme === 'dark' ? '#0a0c0b' : '#ECE6D5',
          transition: 'background 200ms ease',
        }}>
          <SW_PhoneFrame c={c}>
            <SW_StatusBar c={c} />
            <OnboardingFlow c={c} onDone={() => {
              setShowOnboarding(false);
              setTab('home');
              setToast('Selamat datang di Sakuwise');
            }} />
            <Toast c={c} message={toast} onClose={() => setToast(null)} />
            <SW_NavHandle c={c} />
          </SW_PhoneFrame>
        </div>
        {renderTweaks()}
      </React.Fragment>
    );
  }

  function renderTweaks() {
    return (
      <TweaksPanel title="Tweaks">
        <TweakSection label="Tampilan">
          <TweakRadio
            label="Tema"
            value={t.theme}
            onChange={(v) => setTweak('theme', v)}
            options={[
              { value: 'light', label: 'Terang' },
              { value: 'dark', label: 'Gelap' },
            ]}
          />
        </TweakSection>
        <TweakSection label="Jelajah">
          <TweakButton label="Beranda" onClick={() => { setShowOnboarding(false); navTab('home'); }} />
          <TweakButton label="Plan" onClick={() => { setShowOnboarding(false); navTab('plan'); }} />
          <TweakButton label="Aset" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub(null); }} />
          <TweakButton label="Saya / Settings" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub(null); }} />
        </TweakSection>
        <TweakSection label="M4b — Forms & Flows">
          <TweakButton label="+ Pemasukan" onClick={() => { setShowOnboarding(false); navTab('home'); setOverlay('income'); }} />
          <TweakButton label="+ Transfer" onClick={() => { setShowOnboarding(false); navTab('home'); setOverlay('transfer'); }} />
          <TweakButton label="📷 OCR Capture" onClick={() => { setShowOnboarding(false); navTab('home'); setOverlay('ocr'); }} />
          <TweakButton label="Rekonsiliasi" onClick={() => { setShowOnboarding(false); navTab('home'); setReconcileAcct(SW_ACCOUNTS[0]); setOverlay('reconcile'); }} />
        </TweakSection>
        <TweakSection label="M4b — Settings">
          <TweakButton label="Backup & Pemulihan" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('backup'); }} />
          <TweakButton label="Persentase Alokasi" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('allocation'); }} />
          <TweakButton label="Auto-lock" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('auto-lock'); }} />
          <TweakButton label="Tanggal Mulai Periode" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('period-start'); }} />
          <TweakButton label="Donasi" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('donate'); }} />
          <TweakButton label="Tentang Sakuwise" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('about'); }} />
          <TweakButton label="Export & Reset" onClick={() => { setShowOnboarding(false); navTab('me'); setMeSub('export-reset'); }} />
        </TweakSection>
        <TweakSection label="M4c — Account Detail">
          <TweakButton label="Mandiri (detail)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'account', id: 'mandiri' }); }} />
          <TweakButton label="BCA (detail)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'account', id: 'bca' }); }} />
        </TweakSection>
        <TweakSection label="M4a — Aset Detail">
          <TweakButton label="🎬 Onboarding (mulai)" onClick={() => setShowOnboarding(true)} />
          <TweakButton label="Akun (list)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'accounts' }); }} />
          <TweakButton label="Emas (list)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'emas-list' }); }} />
          <TweakButton label="Tanah (detail + pajak)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'land', id: 'l1' }); }} />
          <TweakButton label="Deposito (chart)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'dep', id: 'd1' }); }} />
          <TweakButton label="Hutang (detail + chart)" onClick={() => { setShowOnboarding(false); navTab('assets'); setAssetsSub({ kind: 'debt', id: 'db1' }); }} />
        </TweakSection>
        <TweakSection label="M3 Layar Inti">
          <TweakButton label="+ Tambah Transaksi" onClick={() => { setShowOnboarding(false); navTab('home'); setOverlay('picker'); }} />
          <TweakButton label="Form Pengeluaran" onClick={() => { setShowOnboarding(false); navTab('home'); setOverlay('expense'); }} />
        </TweakSection>
      </TweaksPanel>
    );
  }

  // Full-screen overlay forms (replaces phone content while open)
  const fullScreenOverlays = {
    expense:  <ExpenseForm  c={c} onClose={closeOverlay} onSave={saveTxn('Pengeluaran')} />,
    income:   <IncomeForm   c={c} onClose={closeOverlay} onSave={saveTxn('Pemasukan')} />,
    transfer: <TransferForm c={c} onClose={closeOverlay} onSave={saveTxn('Transfer')} />,
    reconcile: reconcileAcct
      ? <ReconciliationFlow c={c} account={reconcileAcct} onClose={closeOverlay} onDone={() => { closeOverlay(); setToast('Saldo akun disesuaikan'); }} />
      : null,
  };
  const fullScreen = fullScreenOverlays[overlay];

  return (
    <React.Fragment>
      <div style={{
        minHeight: '100dvh', width: '100%',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: '40px 20px',
        background: t.theme === 'dark' ? '#0a0c0b' : '#ECE6D5',
        transition: 'background 200ms ease',
      }}>
        <SW_PhoneFrame c={c}>
          <SW_StatusBar c={c} />

          {/* Main tabs */}
          <div style={{ position: 'absolute', top: 40, left: 0, right: 0, bottom: 0 }}>
            <div key={tab + (assetsSub ? '/' + assetsSub.kind + (assetsSub.id || '') : '') + (meSub ? '/me-' + meSub : '')} style={{ height: '100%' }}>{tabContent}</div>
          </div>

          {/* Tab bar — hidden when full-screen overlay is showing */}
          {!fullScreen && overlay !== 'ocr' && (
            <SW_TabBar c={c} active={tab} onChange={navTab} onAdd={openAdd} />
          )}

          {/* Full-screen overlays (expense/income/transfer/reconcile forms) */}
          {fullScreen && (
            <div style={{ position: 'absolute', inset: 0, zIndex: 8, background: c.bg }}>
              <SW_StatusBar c={c} />
              <div style={{ position: 'absolute', top: 40, left: 0, right: 0, bottom: 0 }}>
                {fullScreen}
              </div>
            </div>
          )}

          {/* OCR is its own black-bg overlay (camera) */}
          {overlay === 'ocr' && (
            <OcrFlow c={c} onClose={closeOverlay} onProceed={() => { setOverlay('expense'); }} />
          )}

          {/* Picker sheet */}
          <AddTxnPicker c={c} open={overlay === 'picker'} onClose={closeOverlay} onPick={onPickType} />

          {/* Toast */}
          <Toast c={c} message={toast} onClose={() => setToast(null)} />

          <SW_NavHandle c={c} />
        </SW_PhoneFrame>
      </div>

      {renderTweaks()}
    </React.Fragment>
  );
};

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
