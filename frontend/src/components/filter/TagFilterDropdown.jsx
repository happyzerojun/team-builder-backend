import { useRef } from "react";

const TAG_TABS = {
    프론트엔드: ["React", "Vue", "Angular", "Next.js", "Svelte", "TypeScript", "JavaScript", "HTML/CSS", "Tailwind"],
    백엔드: ["Spring Boot", "Node.js", "Django", "FastAPI", "Flask", "Express", "NestJS", "Java", "Python", "Go", "Kotlin"],
    데이터베이스: ["MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite", "Firebase"],
    데브옵스: ["AWS", "Docker", "Kubernetes", "CI/CD", "GCP", "Azure", "Nginx", "Linux"],
    기타: ["Git", "Figma", "Unity", "Flutter", "React Native", "Swift", "Kotlin(Android)", "Kotlin(iOS)"],
};

const TAB_KEYS = Object.keys(TAG_TABS);

function TagFilterDropdown({
    selectedTags = [],
    onToggleTag,
    onClear,
    activeTab,
    onTabChange,
    isOpen,
    onToggleOpen
}) {
    const tagDropdownRef = useRef(null);
    const safeActiveTab = TAG_TABS[activeTab] ? activeTab : TAB_KEYS[0];
    const currentTags = TAG_TABS[safeActiveTab];

    return (
        <div className="tag-dropdown-wrapper" ref={tagDropdownRef}>
            <button
                type="button"
                className={`filter-btn ${isOpen || selectedTags.length > 0 ? "active" : ""}`}
                onClick={onToggleOpen}
            >
                🛠 기술 스택
                {selectedTags.length > 0 && <span className="filter-badge">{selectedTags.length}</span>}
                <span className={`filter-arrow ${isOpen ? "open" : ""}`}>▾</span>
            </button>

            {isOpen && (
                <div className="tag-dropdown-panel">
                    <div className="tag-tab-nav">
                        {TAB_KEYS.map((tab) => (
                            <button
                                key={tab}
                                type="button"
                                className={`tag-tab-btn ${safeActiveTab === tab ? "active" : ""}`}
                                onClick={() => onTabChange(tab)}
                            >
                                {tab}
                            </button>
                        ))}
                    </div>

                    <div className="tag-grid">
                        {currentTags.map((tag) => (
                            <button
                                key={tag}
                                type="button"
                                className={`tag-chip ${selectedTags.includes(tag) ? "selected" : ""}`}
                                onClick={() => onToggleTag(tag)}
                            >
                                {tag}
                            </button>
                        ))}
                    </div>

                    {selectedTags.length > 0 && (
                        <div className="tag-dropdown-footer">
                            <span className="tag-selected-count">{selectedTags.length}개 선택됨</span>
                            <button type="button" className="btn-clear" onClick={onClear}>
                                초기화 ✕
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default TagFilterDropdown;