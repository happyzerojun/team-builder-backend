const DURATION_PRESETS = [
    { label: "전체", min: 0, max: Infinity },
    { label: "1~3개월", min: 1, max: 3 },
    { label: "1~6개월", min: 1, max: 6 },
    { label: "6~12개월", min: 6, max: 12 },
    { label: "12개월 이상", min: 12, max: Infinity },
];

function DurationFilterDropdown({
    isOpen, onToggleOpen,
    durationPreset, onPresetChange,
    customMin, customMax, onCustomMinChange, onCustomMaxChange,
    useCustom, durationLabel, isDurationFiltered,
    onReset
}) {
    return (
        <div className="tag-dropdown-wrapper">
            <button
                className={`filter-btn ${isOpen || isDurationFiltered ? "active" : ""}`}
                onClick={onToggleOpen}
            >
                📅 {durationLabel}
                {isDurationFiltered && (
                    <span className="filter-clear-x" onClick={(e) => { e.stopPropagation(); onReset(); }}>✕</span>
                )}
                <span className={`filter-arrow ${isOpen ? "open" : ""}`}>▾</span>
            </button>
            {isOpen && (
                <div className="duration-dropdown-panel">
                    <p className="duration-panel-title">프로젝트 기간</p>
                    <div className="duration-presets">
                        {DURATION_PRESETS.map((preset) => (
                            <button
                                key={preset.label}
                                className={`duration-preset-btn ${!useCustom && durationPreset === preset.label ? "active" : ""}`}
                                onClick={() => onPresetChange(preset.label)}
                            >
                                {preset.label}
                            </button>
                        ))}
                    </div>
                    <div className="duration-divider">직접 입력</div>
                    <div className="duration-custom-row">
                        <input type="number" className="duration-custom-input" placeholder="최소" value={customMin} onChange={(e) => onCustomMinChange(e.target.value)} />
                        <span className="duration-range-sep">개월 ~</span>
                        <input type="number" className="duration-custom-input" placeholder="최대" value={customMax} onChange={(e) => onCustomMaxChange(e.target.value)} />
                        <span className="duration-range-sep">개월</span>
                    </div>
                    {isDurationFiltered && (
                        <button className="btn-clear duration-reset-btn" onClick={onReset}>필터 초기화 ✕</button>
                    )}
                </div>
            )}
        </div>
    );
}

export default DurationFilterDropdown;