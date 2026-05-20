// Sakuwise — Plan screen
// Alokasi → Kategori → Plan Item with collapsible category cards.

const PlanScreen = ({ c, onNav, onAddItem }) => {
  const [filter, setFilter] = React.useState('all');
  const [expanded, setExpanded] = React.useState(() => new Set(['home', 'food', 'leisure', 'tabungan']));
  const [showMonthPicker, setShowMonthPicker] = React.useState(false);
  const [showActionSheet, setShowActionSheet] = React.useState(false);

  const allocations = filter === 'all'
    ? SW_ALLOCATIONS
    : SW_ALLOCATIONS.filter(a => a.id === filter);

  const totalPlan = SW_ALLOCATIONS.reduce((s, a) => s + a.plan, 0);
  const totalUsed = SW_ALLOCATIONS.reduce((s, a) => s + a.used, 0);

  const toggleCat = (id) => {
    setExpanded(s => {
      const n = new Set(s);
      n.has(id) ? n.delete(id) : n.add(id);
      return n;
    });
  };

  return (
    <div className="sw-scroll" style={{ height: '100%', overflowY: 'auto', paddingBottom: 100, animation: 'sw-slide-in 280ms ease' }}>
      <SW_TopBar
        c={c}
        title="Plan"
        subtitle={null}
        right={
          <button onClick={() => setShowActionSheet(true)} className="sw-press" style={{ width: 40, height: 40, borderRadius: 12, border: 'none', background: 'transparent', color: c.ink, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Icon name="more" size={22} />
          </button>
        }
      />

      {/* Month switcher */}
      <div style={{ padding: '0 20px 14px' }}>
        <button onClick={() => setShowMonthPicker(true)} className="sw-press" style={{
          display: 'inline-flex', alignItems: 'center', gap: 8,
          padding: '8px 14px', background: c.surface, border: `1px solid ${c.border}`,
          borderRadius: 99, color: c.ink, cursor: 'pointer',
          fontFamily: SW_TYPE.family, fontSize: 14, fontWeight: 600, whiteSpace: 'nowrap',
        }}>
          <Icon name="calendar" size={16} />
          <span>{SW_PERIOD.label}</span>
          <Icon name="chevron_down" size={16} strokeWidth={2} />
        </button>
      </div>

      {/* Summary card */}
      <div style={{ padding: '0 20px 16px' }}>
        <SW_Card c={c} padding={18}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, marginBottom: 14 }}>
            <div style={{ minWidth: 0, flex: 1 }}>
              <div style={{ fontSize: 11, fontWeight: 700, color: c.inkSubtle, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 6 }}>Pemasukan diharapkan</div>
              <div><SW_Amount c={c} value={SW_INCOME_MONTH} size={26} weight={700} /></div>
            </div>
            <button className="sw-press" style={{ width: 32, height: 32, borderRadius: 10, border: 'none', background: c.primaryContainer, color: c.onPrimaryContainer, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Icon name="edit" size={16} />
            </button>
          </div>
          <SW_Bar c={c} used={totalUsed} plan={totalPlan} />
          <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8, marginTop: 8, fontSize: 12 }}>
            <span style={{ color: c.inkMuted, whiteSpace: 'nowrap' }}>Terpakai <strong style={{ color: c.ink, fontVariantNumeric: 'tabular-nums' }}>{SW_FORMAT.rpShort(totalUsed)}</strong></span>
            <span style={{ color: c.inkMuted, whiteSpace: 'nowrap' }}>dari <strong style={{ color: c.ink, fontVariantNumeric: 'tabular-nums' }}>{SW_FORMAT.rpShort(totalPlan)}</strong></span>
          </div>
        </SW_Card>
      </div>

      {/* Allocation filter chips */}
      <div className="sw-scroll" style={{ overflowX: 'auto', padding: '0 20px 14px', display: 'flex', gap: 8 }}>
        <SW_Chip c={c} label="Semua" active={filter === 'all'} onClick={() => setFilter('all')} />
        {SW_ALLOCATIONS.map(a => (
          <SW_Chip key={a.id} c={c} label={a.name} active={filter === a.id} onClick={() => setFilter(a.id)} />
        ))}
      </div>

      {/* Allocation sections */}
      {allocations.map(a => {
        const allocColor = { needs: c.primary, wants: c.accent, invest: c.info }[a.id];
        return (
          <div key={a.id} style={{ padding: '0 20px', marginBottom: 18 }}>
            {filter === 'all' && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 10 }}>
                <span style={{ width: 8, height: 8, borderRadius: '50%', background: allocColor }} />
                <span style={{ fontSize: 15, fontWeight: 700, color: c.ink, letterSpacing: '-0.005em' }}>{a.name}</span>
                <span style={{ fontSize: 11, color: c.inkSubtle, fontFamily: SW_TYPE.mono, letterSpacing: '0.04em' }}>{a.target}%</span>
                <div style={{ flex: 1 }} />
                <span style={{ fontSize: 12, color: c.inkMuted, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap' }}>
                  <strong style={{ color: c.ink, fontWeight: 700 }}>{SW_FORMAT.rpShort(a.used)}</strong>
                  <span> / {SW_FORMAT.rpShort(a.plan)}</span>
                </span>
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {a.categories.map(cat => (
                <CategoryCard
                  key={cat.id}
                  c={c}
                  category={cat}
                  allocColor={allocColor}
                  expanded={expanded.has(cat.id)}
                  onToggle={() => toggleCat(cat.id)}
                  onAddItem={() => onAddItem && onAddItem(cat)}
                />
              ))}
              <button onClick={() => onAddItem && onAddItem({ name: 'Kategori baru', alloc: a.id })} className="sw-press" style={{
                display: 'flex', alignItems: 'center', gap: 8, justifyContent: 'center',
                padding: '10px', background: 'transparent', border: `1.5px dashed ${c.borderStrong}`,
                borderRadius: 12, color: c.inkMuted, cursor: 'pointer',
                fontFamily: SW_TYPE.family, fontSize: 13, fontWeight: 600, whiteSpace: 'nowrap',
              }}>
                <Icon name="plus" size={16} />
                Tambah kategori
              </button>
            </div>
          </div>
        );
      })}

      {/* Sheets */}
      <SW_Sheet c={c} open={showMonthPicker} onClose={() => setShowMonthPicker(false)} title="Pilih Periode">
        {[
          { label: 'Mei 2026', range: '1 Mei – 31 Mei 2026', active: true },
          { label: 'Apr 2026', range: '1 Apr – 30 Apr 2026' },
          { label: 'Mar 2026', range: '1 Mar – 31 Mar 2026' },
          { label: 'Feb 2026', range: '1 Feb – 28 Feb 2026' },
        ].map(m => (
          <button key={m.label} onClick={() => setShowMonthPicker(false)} className="sw-press" style={{
            width: '100%', textAlign: 'left',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            padding: '14px 16px', marginBottom: 6,
            background: m.active ? c.primaryContainer : c.bg,
            border: 'none', borderRadius: 12, cursor: 'pointer',
            color: m.active ? c.onPrimaryContainer : c.ink,
            fontFamily: SW_TYPE.family,
          }}>
            <div>
              <div style={{ fontSize: 15, fontWeight: 700 }}>{m.label}</div>
              <div style={{ fontSize: 12, opacity: 0.7, marginTop: 1 }}>{m.range}</div>
            </div>
            {m.active && <Icon name="check" size={20} />}
          </button>
        ))}
      </SW_Sheet>

      <SW_Sheet c={c} open={showActionSheet} onClose={() => setShowActionSheet(false)} title="Aksi Plan">
        <ActionRow c={c} icon="copy" label="Salin dari bulan lalu" sub="Ambil item recurring dari Plan April" onClick={() => setShowActionSheet(false)} />
        <ActionRow c={c} icon="sparkle" label="Terapkan template starter" sub="Struktur kategori standard Indonesia" onClick={() => setShowActionSheet(false)} />
        <ActionRow c={c} icon="filter" label="Atur persentase untuk plan ini" sub="Override alokasi 50/30/20 cuma untuk periode ini" onClick={() => setShowActionSheet(false)} />
        <ActionRow c={c} icon="trash" label="Reset plan bulan ini" sub="Hapus semua item, mulai dari nol" danger onClick={() => setShowActionSheet(false)} />
      </SW_Sheet>
    </div>
  );
};

const ActionRow = ({ c, icon, label, sub, danger, onClick }) => (
  <button onClick={onClick} className="sw-press" style={{
    width: '100%', textAlign: 'left',
    display: 'flex', alignItems: 'center', gap: 14,
    padding: '12px 14px', marginBottom: 6,
    background: c.bg, border: 'none', borderRadius: 12, cursor: 'pointer',
    fontFamily: SW_TYPE.family,
  }}>
    <div style={{
      width: 40, height: 40, borderRadius: 12,
      background: danger ? c.dangerSoft : c.primaryContainer,
      color: danger ? c.danger : c.onPrimaryContainer,
      display: 'flex', alignItems: 'center', justifyContent: 'center', flex: '0 0 auto',
    }}>
      <Icon name={icon} size={18} />
    </div>
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={{ fontSize: 14, fontWeight: 600, color: danger ? c.danger : c.ink }}>{label}</div>
      <div style={{ fontSize: 11, color: c.inkMuted, marginTop: 1 }}>{sub}</div>
    </div>
    <Icon name="chevron_right" size={18} color={c.inkSubtle} />
  </button>
);

const CategoryCard = ({ c, category, allocColor, expanded, onToggle, onAddItem }) => {
  const pct = (category.used / category.plan) * 100;
  const over = category.used > category.plan;
  return (
    <SW_Card c={c} padding={0}>
      <button onClick={onToggle} className="sw-press" style={{
        width: '100%', textAlign: 'left',
        display: 'flex', alignItems: 'center', gap: 12,
        padding: '14px 16px',
        background: 'transparent', border: 'none', cursor: 'pointer',
        fontFamily: SW_TYPE.family,
      }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 8, marginBottom: 8 }}>
            <span style={{ fontSize: 14, fontWeight: 700, color: c.ink, letterSpacing: '-0.005em', minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', flex: 1 }}>{category.name}</span>
            <span style={{ fontSize: 12, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap', flex: '0 0 auto' }}>
              <span style={{ color: over ? c.danger : c.ink, fontWeight: 700 }}>{SW_FORMAT.rpShort(category.used)}</span>
              <span style={{ color: c.inkSubtle }}> / {SW_FORMAT.rpShort(category.plan)}</span>
            </span>
          </div>
          <SW_Bar c={c} used={category.used} plan={category.plan} color={allocColor} height={6} animate={false} />
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
            <span style={{ fontSize: 10, color: c.inkSubtle, fontVariantNumeric: 'tabular-nums' }}>{category.items.length} item · {Math.round(pct)}%</span>
            {over && <span style={{ fontSize: 10, color: c.danger, fontWeight: 700 }}>Over {SW_FORMAT.rpShort(category.used - category.plan)}</span>}
          </div>
        </div>
        <div style={{ color: c.inkSubtle, transform: expanded ? 'rotate(180deg)' : 'none', transition: 'transform 180ms ease' }}>
          <Icon name="chevron_down" size={20} strokeWidth={2} />
        </div>
      </button>

      {expanded && (
        <div style={{ borderTop: `1px solid ${c.border}`, padding: '4px 16px 12px' }}>
          {category.items.map(item => (
            <PlanItemRow key={item.id} c={c} item={item} allocColor={allocColor} />
          ))}
          <button onClick={onAddItem} className="sw-press" style={{
            width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
            padding: '8px 0 4px', background: 'transparent', border: 'none', cursor: 'pointer',
            color: c.primary, fontFamily: SW_TYPE.family, fontSize: 12, fontWeight: 600, whiteSpace: 'nowrap',
          }}>
            <Icon name="plus" size={14} strokeWidth={2.2} />
            Tambah item
          </button>
        </div>
      )}
    </SW_Card>
  );
};

const PlanItemRow = ({ c, item, allocColor }) => {
  const pct = (item.used / item.plan) * 100;
  const over = item.over || item.used > item.plan;
  const recurringLabel = {
    'monthly': '⟳ bulanan',
    'quarterly': '⟳ kwartalan',
    'yearly': '⟳ tahunan',
    'one-off': null,
  }[item.recurring];

  return (
    <div style={{ padding: '10px 0', borderBottom: `1px solid ${c.border}55` }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 8, marginBottom: 4 }}>
        <div style={{ minWidth: 0, flex: 1, paddingRight: 8, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          <span style={{ fontSize: 13, color: c.ink, fontWeight: 500 }}>{item.name}</span>
          {recurringLabel && (
            <span style={{ fontSize: 10, color: c.inkSubtle, marginLeft: 6 }}>{recurringLabel}</span>
          )}
        </div>
        <div style={{ fontSize: 12, fontVariantNumeric: 'tabular-nums', whiteSpace: 'nowrap', flex: '0 0 auto' }}>
          <span style={{ color: over ? c.danger : c.ink, fontWeight: 600 }}>{SW_FORMAT.rpShort(item.used)}</span>
          <span style={{ color: c.inkSubtle }}> / {SW_FORMAT.rpShort(item.plan)}</span>
        </div>
      </div>
      <SW_Bar c={c} used={item.used} plan={item.plan} color={allocColor} height={4} animate={false} />
    </div>
  );
};

Object.assign(window, { PlanScreen, CategoryCard, PlanItemRow });
