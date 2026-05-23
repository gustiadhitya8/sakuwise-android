// Sakuwise — Backup setup + Reconciliation + OCR receipt capture

// ─── Backup Settings (hub) ──────────────────────────────────

const BackupSettings = ({ c, onBack }) => {
  const [step, setStep] = React.useState('hub'); // hub | set-pin | backup-now | restore | change-pin
  if (step === 'set-pin')    return <BackupSetPinFlow c={c} onBack={() => setStep('hub')} onDone={() => setStep('backup-now')} />;
  if (step === 'backup-now') return <BackupNowFlow c={c} onBack={() => setStep('hub')} />;
  if (step === 'restore')    return <RestoreFlow c={c} onBack={() => setStep('hub')} />;
  if (step === 'change-pin') return <BackupSetPinFlow c={c} mode="change" onBack={() => setStep('hub')} onDone={() => setStep('hub')} />;

  return (
    <SimpleSettingsScreen c={c} title="Backup & Pemulihan" onBack={onBack}>
      {/* Status hero */}
      <div style={{
        background: c.warningSoft, border: `1px solid ${c.warning}33`,
        borderRadius: 18, padding: '18px 18px 16px',
        marginBottom: 16, display: 'flex', gap: 14, alignItems: 'flex-start',
      }}>
        <div style={{
          width: 48, height: 48, borderRadius: 14,
          background: c.warning, color: '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
        }}>
          <Icon name="shield" size={24} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 15, fontWeight: 700, color: c.ink, marginBottom: 2 }}>Backup tertunda 34 hari</div>
          <div style={{ fontSize: 12, color: c.inkMuted, lineHeight: 1.5 }}>
            Backup berkala melindungi datamu kalau HP hilang. Kalau lebih dari 60 hari,
            modal blocking akan muncul saat aplikasi dibuka.
          </div>
        </div>
      </div>

      <SW_Button c={c} size="lg" icon="copy" onClick={() => setStep('set-pin')}>Backup Sekarang</SW_Button>

      <div style={{ height: 14 }} />

      <SW_Card c={c} padding={0} style={{ marginBottom: 14 }}>
        <SettingsRow c={c} icon="shield"  label="Ubah PIN backup"        sub="6 digit · terpisah dari PIN device" onClick={() => setStep('change-pin')} />
        <SettingsRow c={c} icon="me"      label="Beralih ke passphrase"  sub="Advanced — Argon2id KDF" />
        <SettingsRow c={c} icon="receipt" label="Restore dari file"      sub=".sakuwise dari device lama" onClick={() => setStep('restore')} last />
      </SW_Card>

      {/* How it works */}
      <SW_SectionLabel c={c}>Cara Kerja Backup</SW_SectionLabel>
      <SW_Card c={c} padding={16}>
        <BackupStep c={c} num={1} title="Set PIN backup" sub="6 digit, terpisah dari PIN device. Dipakai untuk meng-enkripsi file backup." />
        <BackupStep c={c} num={2} title="Aplikasi enkripsi data" sub="Semua data → file .sakuwise terenkripsi AES-256-GCM. PIN-mu jadi kunci." />
        <BackupStep c={c} num={3} title="Kamu pilih lokasi simpan" sub="Storage lokal, USB, atau upload manual ke Google Drive / iCloud. Sakuwise nggak auto-upload." />
        <BackupStep c={c} num={4} title="Restore di device lama / baru" sub="Pilih file → masukkan PIN backup → semua data balik." last />
      </SW_Card>
    </SimpleSettingsScreen>
  );
};

const BackupStep = ({ c, num, title, sub, last }) => (
  <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start', paddingBottom: last ? 0 : 12, marginBottom: last ? 0 : 12, borderBottom: last ? 'none' : `1px solid ${c.border}` }}>
    <div style={{
      width: 28, height: 28, borderRadius: '50%',
      background: c.primaryContainer, color: c.onPrimaryContainer,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      flex: '0 0 auto', fontSize: 13, fontWeight: 800,
    }}>{num}</div>
    <div style={{ flex: 1 }}>
      <div style={{ fontSize: 13, fontWeight: 700, color: c.ink, marginBottom: 2 }}>{title}</div>
      <div style={{ fontSize: 11, color: c.inkMuted, lineHeight: 1.5 }}>{sub}</div>
    </div>
  </div>
);

// ─── Backup: Set PIN flow ───────────────────────────────────

const BackupSetPinFlow = ({ c, onBack, onDone, mode = 'first' }) => {
  const [stage, setStage] = React.useState(1); // 1: enter, 2: confirm
  const [pin1, setPin1] = React.useState('······');
  const [pin2, setPin2] = React.useState('······');

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={mode === 'change' ? 'Ubah PIN Backup' : 'Set PIN Backup'} onBack={onBack} />

      <div style={{ padding: '0 24px' }}>
        <div style={{ textAlign: 'center', padding: '20px 0 24px' }}>
          <div style={{
            width: 100, height: 100, borderRadius: 32, margin: '0 auto 16px',
            background: c.primaryContainer, color: c.primary,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="shield" size={48} strokeWidth={1.5} />
          </div>
          <div style={{ fontSize: 22, fontWeight: 800, color: c.ink, letterSpacing: '-0.02em' }}>
            {stage === 1 ? 'Buat PIN backup' : 'Konfirmasi PIN'}
          </div>
          <div style={{ fontSize: 13, color: c.inkMuted, marginTop: 8, lineHeight: 1.5, maxWidth: 320, margin: '8px auto 0' }}>
            {stage === 1
              ? 'PIN 6-digit untuk meng-enkripsi file backup. Catat & simpan di tempat aman — Sakuwise tidak menyimpan PIN ini.'
              : 'Masukkan PIN sekali lagi untuk konfirmasi.'}
          </div>
        </div>

        <PinInput c={c} value={stage === 1 ? pin1 : pin2} onChange={stage === 1 ? setPin1 : setPin2} />

        <div style={{ height: 24 }} />

        <SW_Button c={c} size="lg" icon={stage === 2 ? 'check' : null}
          onClick={() => {
            if (stage === 1) setStage(2);
            else onDone();
          }}>
          {stage === 1 ? 'Lanjut' : 'Konfirmasi & Mulai Backup'}
        </SW_Button>

        {stage === 1 && (
          <div style={{ marginTop: 16, padding: '12px 14px', background: c.warningSoft, border: `1px solid ${c.warning}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5 }}>
            <strong style={{ color: c.ink }}>⚠ Penting:</strong> Kalau PIN ini hilang, file backup
            tidak bisa di-restore. Sakuwise zero-knowledge — kami tidak punya backdoor.
          </div>
        )}
      </div>
    </div>
  );
};

// ─── Backup Now (pilih lokasi) ──────────────────────────────

const BackupNowFlow = ({ c, onBack }) => {
  const [stage, setStage] = React.useState('encrypt'); // encrypt | pick | done
  React.useEffect(() => {
    if (stage === 'encrypt') {
      const t = setTimeout(() => setStage('pick'), 1400);
      return () => clearTimeout(t);
    }
  }, [stage]);

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Backup" onBack={onBack} />
      <div style={{ padding: '20px 24px' }}>
        {stage === 'encrypt' && <BackupEncrypting c={c} />}
        {stage === 'pick' && <BackupPickLocation c={c} onDone={() => setStage('done')} />}
        {stage === 'done' && <BackupDone c={c} onBack={onBack} />}
      </div>
    </div>
  );
};

const BackupEncrypting = ({ c }) => (
  <div style={{ textAlign: 'center', padding: '40px 0' }}>
    <div style={{
      width: 100, height: 100, borderRadius: 32, margin: '0 auto 18px',
      background: c.primaryContainer, color: c.primary,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      position: 'relative',
    }}>
      <Icon name="shield" size={48} strokeWidth={1.5} />
      <div style={{ position: 'absolute', inset: -4, borderRadius: 36, border: `3px solid ${c.primary}33`, borderTopColor: c.primary, animation: 'spin 1s linear infinite' }} />
      <style>{`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}</style>
    </div>
    <div style={{ fontSize: 18, fontWeight: 800, color: c.ink, marginBottom: 6, letterSpacing: '-0.01em' }}>
      Mengenkripsi data...
    </div>
    <div style={{ fontSize: 13, color: c.inkMuted, lineHeight: 1.5 }}>
      AES-256-GCM. Argon2id KDF.<br/>Tidak ada data keluar dari HP ini.
    </div>
  </div>
);

const BackupPickLocation = ({ c, onDone }) => (
  <div>
    <div style={{ textAlign: 'center', padding: '0 0 24px' }}>
      <div style={{
        width: 80, height: 80, borderRadius: 26, margin: '0 auto 14px',
        background: c.successSoft, color: c.success,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <Icon name="check" size={40} strokeWidth={2.5} />
      </div>
      <div style={{ fontSize: 18, fontWeight: 800, color: c.ink, letterSpacing: '-0.01em' }}>File backup siap</div>
      <div style={{ fontSize: 12, color: c.inkMuted, marginTop: 4, fontFamily: SW_TYPE.mono, letterSpacing: '0.02em' }}>
        sakuwise-backup-2026-05-15-0941.sakuwise · 4.2 MB
      </div>
    </div>

    <div style={{ fontSize: 12, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 8, padding: '0 4px' }}>Simpan ke</div>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <BackupLocationOption c={c} icon="copy" label="Storage HP" sub="/Documents/Sakuwise/" onClick={onDone} />
      <BackupLocationOption c={c} icon="link" label="Google Drive" sub="Manual upload via share intent" onClick={onDone} />
      <BackupLocationOption c={c} icon="link" label="Dropbox / iCloud" sub="Via aplikasi penyimpanan lain" onClick={onDone} />
      <BackupLocationOption c={c} icon="arrow_up_right" label="Bagikan..." sub="Share sheet sistem (WhatsApp, email, dll.)" onClick={onDone} />
    </div>
  </div>
);

const BackupLocationOption = ({ c, icon, label, sub, onClick }) => (
  <button onClick={onClick} className="sw-press" style={{
    width: '100%', textAlign: 'left',
    display: 'flex', alignItems: 'center', gap: 12,
    padding: '14px 16px',
    background: c.surface, border: `1px solid ${c.border}`,
    borderRadius: 14, cursor: 'pointer', fontFamily: SW_TYPE.family,
  }}>
    <div style={{ width: 40, height: 40, borderRadius: 12, background: c.primaryContainer, color: c.onPrimaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
      <Icon name={icon} size={18} />
    </div>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 14, fontWeight: 700, color: c.ink }}>{label}</div>
      <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{sub}</div>
    </div>
    <Icon name="chevron_right" size={18} color={c.inkSubtle} />
  </button>
);

const BackupDone = ({ c, onBack }) => (
  <div style={{ textAlign: 'center', padding: '40px 0' }}>
    <div style={{
      width: 100, height: 100, borderRadius: 32, margin: '0 auto 18px',
      background: c.successSoft, color: c.success,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <Icon name="check" size={56} strokeWidth={2.5} />
    </div>
    <div style={{ fontSize: 22, fontWeight: 800, color: c.ink, marginBottom: 8, letterSpacing: '-0.02em' }}>Backup tersimpan</div>
    <div style={{ fontSize: 13, color: c.inkMuted, lineHeight: 1.5, marginBottom: 24 }}>
      File ada di Download. Ingat — jaga baik-baik file & PIN-nya.
    </div>
    <SW_Button c={c} onClick={onBack} size="lg">Selesai</SW_Button>
  </div>
);

// ─── Restore flow ───────────────────────────────────────────

const RestoreFlow = ({ c, onBack }) => {
  const [pin, setPin] = React.useState('······');
  const [loading, setLoading] = React.useState(false);
  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Restore dari File" onBack={onBack} />
      <div style={{ padding: '20px 24px' }}>
        <div style={{
          padding: 14, background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 14, display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16,
        }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: c.primaryContainer, color: c.onPrimaryContainer, display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto' }}>
            <Icon name="copy" size={20} />
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: c.ink, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>sakuwise-backup-2026-04-12.sakuwise</div>
            <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>4.1 MB · dipilih dari Downloads</div>
          </div>
          <button className="sw-press" style={{ background: 'transparent', border: 'none', color: c.primary, fontSize: 12, fontWeight: 700, cursor: 'pointer' }}>Ganti</button>
        </div>

        <div style={{ fontSize: 12, fontWeight: 600, color: c.inkMuted, marginBottom: 8 }}>Masukkan PIN backup</div>
        <PinInput c={c} value={pin} onChange={setPin} />

        <div style={{ height: 18 }} />

        <SW_Button c={c} size="lg" icon={loading ? null : 'check'} onClick={() => setLoading(true)} disabled={loading}>
          {loading ? 'Mendekripsi...' : 'Pulihkan Data'}
        </SW_Button>

        <div style={{ marginTop: 16, padding: '12px 14px', background: c.infoSoft, border: `1px solid ${c.info}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5 }}>
          Restore akan mengganti SEMUA data di aplikasi ini dengan data dari file backup.
          Data yang sekarang ada akan hilang. Pastikan ini yang kamu inginkan.
        </div>
      </div>
    </div>
  );
};

// ─── Reconciliation flow ────────────────────────────────────

const ReconciliationFlow = ({ c, account, onClose, onDone }) => {
  const [stage, setStage] = React.useState('input'); // input | confirm | done
  const [realBalance, setRealBalance] = React.useState(account.balance);
  const diff = realBalance - account.balance;

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: c.bg, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title={`Rekonsiliasi · ${account.name}`} onBack={onClose} />

      <div className="sw-scroll" style={{ flex: 1, overflowY: 'auto', padding: '0 20px 28px' }}>
        {stage === 'input' && (
          <>
            <div style={{ padding: '8px 4px 16px' }}>
              <div style={{ fontSize: 13, color: c.inkMuted, lineHeight: 1.5 }}>
                Buka aplikasi bank / e-wallet, lihat saldo aslinya. Tulis di sini — Sakuwise akan
                otomatis catat selisih sebagai transaksi penyesuaian.
              </div>
            </div>

            <SW_Card c={c} padding={16} style={{ marginBottom: 14 }}>
              <div style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 6 }}>Saldo Sakuwise</div>
              <SW_Amount c={c} value={account.balance} size={26} weight={700} />
              <div style={{ fontSize: 11, color: c.inkSubtle, marginTop: 4 }}>Terkomputasi dari transaksi tercatat</div>
            </SW_Card>

            <SW_Field c={c} label="Saldo Asli di Bank / E-Wallet"
              value={new Intl.NumberFormat('id-ID').format(realBalance)}
              onChange={(v) => setRealBalance(parseInt(v.replace(/\D/g, '')) || 0)}
              prefix="Rp" />

            {diff !== 0 && (
              <div style={{
                padding: '14px 16px',
                background: diff > 0 ? c.successSoft : c.dangerSoft,
                border: `1px solid ${(diff > 0 ? c.success : c.danger)}33`,
                borderRadius: 12, marginBottom: 16,
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                  <span style={{ fontSize: 12, fontWeight: 700, color: c.ink }}>Selisih</span>
                  <span style={{ fontSize: 18, fontWeight: 800, color: diff > 0 ? c.success : c.danger, fontVariantNumeric: 'tabular-nums' }}>
                    {diff > 0 ? '+' : '−'} {SW_FORMAT.rp(Math.abs(diff))}
                  </span>
                </div>
                <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 4, lineHeight: 1.5 }}>
                  {diff > 0
                    ? 'Saldo aslinya lebih besar — mungkin ada transaksi masuk yang belum tercatat.'
                    : 'Saldo aslinya lebih kecil — mungkin ada transaksi keluar yang belum tercatat.'}
                </div>
              </div>
            )}

            <SW_Button c={c} size="lg" icon="check" onClick={() => diff === 0 ? onDone() : setStage('confirm')}>
              {diff === 0 ? 'Sudah sesuai · Selesai' : 'Lanjut · Buat Penyesuaian'}
            </SW_Button>
          </>
        )}

        {stage === 'confirm' && (
          <ReconcileConfirm c={c} account={account} diff={diff}
            onBack={() => setStage('input')} onConfirm={() => setStage('done')} />
        )}

        {stage === 'done' && (
          <ReconcileDone c={c} account={account} diff={diff} realBalance={realBalance} onDone={onDone} />
        )}
      </div>
    </div>
  );
};

const ReconcileConfirm = ({ c, account, diff, onBack, onConfirm }) => (
  <div>
    <div style={{ textAlign: 'center', padding: '20px 0 24px' }}>
      <div style={{
        width: 80, height: 80, borderRadius: 26, margin: '0 auto 14px',
        background: c.infoSoft, color: c.info,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <Icon name="info" size={40} />
      </div>
      <div style={{ fontSize: 20, fontWeight: 800, color: c.ink, letterSpacing: '-0.02em' }}>Konfirmasi Penyesuaian</div>
    </div>

    <SW_Card c={c} padding={16} style={{ marginBottom: 14 }}>
      <DetailRow c={c} label="Akun" value={account.name} />
      <DetailRow c={c} label="Tipe transaksi" value="Penyesuaian (rekonsiliasi)" />
      <DetailRow c={c} label="Tag" value="Rekonsiliasi" />
      <DetailRow c={c} label="Tanggal" value={SW_FORMAT.date(new Date(2026, 4, 15))} />
      <DetailRow c={c} label="Arah" value={diff > 0 ? 'Pemasukan tidak tercatat' : 'Pengeluaran tidak tercatat'} />
      <DetailRow c={c} label="Nominal" value={`${diff > 0 ? '+' : '−'} ${SW_FORMAT.rp(Math.abs(diff))}`} last />
    </SW_Card>

    <div style={{ padding: '12px 14px', background: c.infoSoft, border: `1px solid ${c.info}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5, marginBottom: 16 }}>
      Transaksi ini <strong style={{ color: c.ink }}>tidak terhitung</strong> ke statistik plan bulanan
      (Pemasukan / Pengeluaran). Cuma untuk meluruskan saldo akun.
    </div>

    <SW_Button c={c} size="lg" icon="check" onClick={onConfirm}>Simpan Penyesuaian</SW_Button>
    <div style={{ height: 8 }} />
    <SW_Button c={c} variant="ghost" size="md" onClick={onBack}>Kembali</SW_Button>
  </div>
);

const ReconcileDone = ({ c, account, diff, realBalance, onDone }) => (
  <div style={{ textAlign: 'center', padding: '40px 0' }}>
    <div style={{
      width: 100, height: 100, borderRadius: 32, margin: '0 auto 18px',
      background: c.successSoft, color: c.success,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <Icon name="check" size={56} strokeWidth={2.5} />
    </div>
    <div style={{ fontSize: 22, fontWeight: 800, color: c.ink, marginBottom: 8, letterSpacing: '-0.02em' }}>Saldo sudah sesuai</div>
    <div style={{ fontSize: 13, color: c.inkMuted, lineHeight: 1.5, marginBottom: 8 }}>
      {account.name} sekarang menampilkan
    </div>
    <SW_Amount c={c} value={realBalance} size={28} weight={700} />
    <div style={{ height: 24 }} />
    <SW_Button c={c} onClick={onDone} size="lg">Selesai</SW_Button>
  </div>
);

// ─── OCR receipt capture preview ────────────────────────────

const OcrFlow = ({ c, onClose, onProceed }) => {
  const [stage, setStage] = React.useState('camera'); // camera | processing | review

  if (stage === 'camera') {
    return (
      <div style={{ position: 'absolute', inset: 0, background: '#000', display: 'flex', flexDirection: 'column', animation: 'sw-fade-up 200ms ease' }}>
        {/* Camera viewfinder */}
        <div style={{ flex: 1, position: 'relative', background: '#1a1a1a', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
          {/* simulated receipt */}
          <div style={{
            background: '#FAF7F0', width: '60%', height: '70%',
            borderRadius: 4, transform: 'rotate(-2deg)',
            padding: 20, color: '#1A2520',
            fontFamily: SW_TYPE.mono, fontSize: 10, lineHeight: 1.6,
            boxShadow: '0 4px 20px rgba(255,255,255,0.08)',
            backgroundImage: 'repeating-linear-gradient(90deg, transparent 0, transparent 19px, rgba(0,0,0,0.02) 19px, rgba(0,0,0,0.02) 20px)',
          }}>
            <div style={{ fontFamily: SW_TYPE.family, fontSize: 14, fontWeight: 700, textAlign: 'center', marginBottom: 8 }}>KOPI KENANGAN</div>
            <div style={{ textAlign: 'center', fontSize: 9, marginBottom: 14 }}>Jl. Sudirman No. 1 · Jakarta</div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}><span>Es Kopi Susu</span><span>28.000</span></div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 14 }}><span>Service</span><span>0</span></div>
            <div style={{ borderTop: '1px dashed #000', paddingTop: 8, display: 'flex', justifyContent: 'space-between', fontFamily: SW_TYPE.family, fontWeight: 700, fontSize: 13 }}><span>TOTAL</span><span>Rp 28.000</span></div>
            <div style={{ fontSize: 9, marginTop: 14, textAlign: 'center' }}>15/05/2026 · 14:32</div>
            <div style={{ fontSize: 9, marginTop: 2, textAlign: 'center' }}>Terima kasih</div>
          </div>

          {/* viewfinder corners */}
          {[[20, 20, 'tl'], [20, 20, 'tr'], [20, 20, 'bl'], [20, 20, 'br']].map(([_, __, pos], i) => {
            const styles = {
              tl: { top: 40, left: 30, borderRight: 'none', borderBottom: 'none' },
              tr: { top: 40, right: 30, borderLeft: 'none', borderBottom: 'none' },
              bl: { bottom: 30, left: 30, borderRight: 'none', borderTop: 'none' },
              br: { bottom: 30, right: 30, borderLeft: 'none', borderTop: 'none' },
            }[pos];
            return <div key={i} style={{ position: 'absolute', width: 28, height: 28, border: '3px solid #fff', borderRadius: 2, ...styles }} />;
          })}

          <div style={{ position: 'absolute', top: 60, left: 30, right: 30, textAlign: 'center', color: '#fff', fontSize: 13, fontWeight: 600, padding: '8px 14px', background: 'rgba(0,0,0,0.6)', borderRadius: 99 }}>
            Sejajarkan struk di bingkai
          </div>

          <button onClick={onClose} className="sw-press" aria-label="Tutup" style={{
            position: 'absolute', top: 50, right: 24,
            width: 40, height: 40, borderRadius: '50%',
            background: 'rgba(0,0,0,0.5)', border: 'none',
            color: '#fff', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="close" size={22} />
          </button>
        </div>

        {/* Camera controls */}
        <div style={{ background: '#000', padding: '20px 30px 30px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <button className="sw-press" style={{
            background: 'rgba(255,255,255,0.1)', border: 'none', color: '#fff',
            width: 50, height: 50, borderRadius: 14, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="receipt" size={22} />
          </button>
          <button onClick={() => setStage('processing')} className="sw-press" aria-label="Ambil foto" style={{
            width: 72, height: 72, borderRadius: '50%',
            background: '#fff', border: '4px solid rgba(255,255,255,0.4)',
            cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <div style={{ width: 56, height: 56, borderRadius: '50%', background: '#fff' }} />
          </button>
          <button className="sw-press" style={{
            background: 'rgba(255,255,255,0.1)', border: 'none', color: '#fff',
            width: 50, height: 50, borderRadius: 14, cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon name="sparkle" size={22} />
          </button>
        </div>
      </div>
    );
  }

  if (stage === 'processing') {
    React.useEffect(() => {
      const t = setTimeout(() => setStage('review'), 1500);
      return () => clearTimeout(t);
    }, []);
    return (
      <div style={{ position: 'absolute', inset: 0, background: c.bg, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18, animation: 'sw-fade-up 200ms ease' }}>
        <div style={{
          width: 100, height: 100, borderRadius: 32,
          background: c.primaryContainer, color: c.primary,
          display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative',
        }}>
          <Icon name="receipt" size={48} strokeWidth={1.5} />
          <div style={{ position: 'absolute', inset: -4, borderRadius: 36, border: `3px solid ${c.primary}33`, borderTopColor: c.primary, animation: 'spin 1s linear infinite' }} />
        </div>
        <div style={{ fontSize: 18, fontWeight: 800, color: c.ink, letterSpacing: '-0.01em' }}>Membaca struk...</div>
        <div style={{ fontSize: 13, color: c.inkMuted, textAlign: 'center', maxWidth: 280, lineHeight: 1.5 }}>
          OCR berjalan on-device pakai ML Kit. Tidak ada gambar yang keluar dari HP.
        </div>
      </div>
    );
  }

  // review stage
  return (
    <div style={{ position: 'absolute', inset: 0, background: c.bg, display: 'flex', flexDirection: 'column', animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar c={c} title="Review Struk" onBack={onClose} right={
        <button onClick={onProceed} className="sw-press" style={{
          height: 36, padding: '0 14px',
          background: c.primary, color: c.onPrimary, border: 'none', borderRadius: 10,
          fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 700, cursor: 'pointer',
        }}>Lanjut</button>
      } />
      <div className="sw-scroll" style={{ flex: 1, overflowY: 'auto', padding: '0 20px 28px' }}>
        {/* Receipt preview */}
        <SW_Card c={c} padding={16} style={{ marginBottom: 14 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
            <div style={{ width: 40, height: 40, borderRadius: 12, background: c.successSoft, color: c.success, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Icon name="check" size={20} strokeWidth={2.5} />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: 700, color: c.ink }}>Berhasil dibaca</div>
              <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>3 field terdeteksi · cek sebelum simpan</div>
            </div>
          </div>
        </SW_Card>

        <div style={{ display: 'flex', gap: 12, marginBottom: 14 }}>
          {/* Mini receipt thumbnail */}
          <div style={{
            width: 90, height: 110, background: '#FAF7F0', borderRadius: 10,
            padding: 8, color: '#1A2520', fontFamily: SW_TYPE.mono,
            fontSize: 6, lineHeight: 1.4, flex: '0 0 auto',
            border: `1px solid ${c.border}`,
          }}>
            <div style={{ fontFamily: SW_TYPE.family, fontSize: 8, fontWeight: 700, textAlign: 'center', marginBottom: 4 }}>KOPI KENANGAN</div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Kopi</span><span>28k</span></div>
            <div style={{ borderTop: '1px dashed #000', marginTop: 6, paddingTop: 4, display: 'flex', justifyContent: 'space-between', fontFamily: SW_TYPE.family, fontWeight: 700 }}><span>Total</span><span>28.000</span></div>
          </div>
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 8 }}>
            <ExtractedField c={c} label="Merchant" value="KOPI KENANGAN" confidence="high" />
            <ExtractedField c={c} label="Nominal" value="Rp 28.000" confidence="high" />
            <ExtractedField c={c} label="Tanggal" value="15 Mei 2026" confidence="med" />
          </div>
        </div>

        <div style={{ padding: '12px 14px', background: c.infoSoft, border: `1px solid ${c.info}33`, borderRadius: 12, fontSize: 12, color: c.inkMuted, lineHeight: 1.5, marginBottom: 14 }}>
          Tap <strong>Lanjut</strong> untuk buka form pengeluaran. Field yang terdeteksi sudah di-prefill.
          Foto struk otomatis di-attach (terkompresi ~200 KB).
        </div>
      </div>
    </div>
  );
};

const ExtractedField = ({ c, label, value, confidence }) => {
  const tone = confidence === 'high' ? c.success : confidence === 'med' ? c.warning : c.danger;
  return (
    <div style={{ padding: '10px 12px', background: c.surface, border: `1px solid ${c.border}`, borderRadius: 10 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 2 }}>
        <span style={{ fontSize: 10, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.06em', textTransform: 'uppercase' }}>{label}</span>
        <span style={{ fontSize: 9, fontWeight: 700, color: tone, padding: '1px 6px', background: tone + '20', borderRadius: 4 }}>
          {confidence === 'high' ? 'tinggi' : confidence === 'med' ? 'sedang' : 'rendah'}
        </span>
      </div>
      <div style={{ fontSize: 13, fontWeight: 700, color: c.ink, fontVariantNumeric: 'tabular-nums' }}>{value}</div>
    </div>
  );
};

Object.assign(window, {
  BackupSettings, BackupSetPinFlow, BackupNowFlow, RestoreFlow,
  BackupStep, BackupEncrypting, BackupPickLocation, BackupDone, BackupLocationOption,
  ReconciliationFlow, ReconcileConfirm, ReconcileDone,
  OcrFlow, ExtractedField,
});
