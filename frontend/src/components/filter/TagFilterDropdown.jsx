import { useRef } from "react";

const TAG_TABS = {
    인기: ["React", "TypeScript", "Next.js", "Node.js", "Python", "Flutter", "Spring Boot", "Vue.js"],
    프론트엔드: ["React", "Vue.js", "Next.js", "TypeScript", "JavaScript", "Tailwind"],
    백엔드: ["Node.js", "Spring Boot", "Django", "Express", "FastAPI", "Redis", "MySQL", "PostgreSQL", "MongoDB"],
    모바일: ["React Native", "Flutter", "Swift", "Kotlin"],
    기타: ["Docker", "AWS", "Firebase", "Git", "Python"],
    모두보기: ["React", "Vue.js", "Next.js", "TypeScript", "JavaScript",
        "Node.js", "Spring Boot", "Django", "Express", "FastAPI",
        "React Native", "Flutter", "Swift", "Kotlin",
        "MySQL", "MongoDB", "PostgreSQL", "Firebase", "Redis",
        "Docker", "AWS", "Git", "Tailwind", "Python"],
};
const TAB_KEYS = Object.keys(TAG_TABS);

function TagFilterDropdown({ selectedTags, onToggleTag, onClear, activeTab, onTabChange, isOpen, onToggleOpen }) {
    const tagDropdownRef = useRef(null);

    return (
        <div className="tag-dropdown-wrapper" ref={tagDropdownRef}>
            <button
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
                            <button key={tab} className={`tag-tab-btn ${activeTab === tab ? "active" : ""}`} onClick={() => onTabChange(tab)}>{tab}</button>
                        ))}
                    </div>
                    <div className="tag-grid">
                        {TAG_TABS[activeTab].map((tag) => (
                            <button key={tag} className={`tag-chip ${selectedTags.includes(tag) ? "selected" : ""}`} onClick={() => onToggleTag(tag)}>{tag}</button>
                        ))}
                    </div>
                    {selectedTags.length > 0 && (
                        <div className="tag-dropdown-footer">
                            <span className="tag-selected-count">{selectedTags.length}개 선택됨</span>
                            <button className="btn-clear" onClick={onClear}>초기화 ✕</button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default TagFilterDropdown;