const AVAILABLE_TAGS = [
    "React", "Vue", "Angular", "Next.js", "Svelte", "TypeScript", "JavaScript", "HTML/CSS", "Tailwind",
    "Spring Boot", "Node.js", "Django", "FastAPI", "Flask", "Express", "NestJS", "Java", "Python", "Go", "Kotlin",
    "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite", "Firebase",
    "AWS", "Docker", "Kubernetes", "CI/CD", "GCP", "Azure", "Nginx", "Linux",
    "Git", "Figma", "Unity", "Flutter", "React Native", "Swift", "Kotlin(Android)", "Kotlin(iOS)"
];

function TagSelectDropdown({
    selectedTags = [],
    onToggleTag,
    isOpen,
    onToggleOpen
}) {
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