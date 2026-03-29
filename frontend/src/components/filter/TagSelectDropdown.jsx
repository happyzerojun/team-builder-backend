const AVAILABLE_TAGS = [
    "React", "Vue.js", "Next.js", "TypeScript", "JavaScript",
    "Node.js", "Spring Boot", "Django", "Express", "FastAPI",
    "React Native", "Flutter", "Swift", "Kotlin",
    "MySQL", "MongoDB", "PostgreSQL", "Firebase", "Redis",
    "Docker", "AWS", "Git", "Tailwind", "Python",
];

function TagSelectDropdown({ selectedTags, onToggleTag, isOpen, onToggleOpen }) {
    return (
        <div style={{ position: "relative" }}>
            <button
                type="button"
                className="tag-dropdown-btn"
                onClick={onToggleOpen}
                style={{ width: "100%", textAlign: "left", cursor: "pointer" }}
            >
                {selectedTags.length > 0 ? `${selectedTags.length}개 선택됨` : "기술 스택 선택"} ▾
            </button>

            {isOpen && (
                <div className="tag-dropdown-list">
                    {AVAILABLE_TAGS.map((tag) => (
                        <label key={tag} className="tag-checkbox-label">
                            <input
                                type="checkbox"
                                checked={selectedTags.includes(tag)}
                                onChange={() => onToggleTag(tag)}
                            />
                            {tag}
                        </label>
                    ))}
                </div>
            )}

            {selectedTags.length > 0 && (
                <div className="tag-selector" style={{ marginTop: "10px" }}>
                    {selectedTags.map((tag) => (
                        <button
                            key={tag}
                            type="button"
                            className="tag-option selected"
                            onClick={() => onToggleTag(tag)}
                        >
                            {tag} ✕
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}

export default TagSelectDropdown;