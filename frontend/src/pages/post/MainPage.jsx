// 📁 src/pages/MainPage.jsx

import { useState, useMemo, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { projectService } from "../../services/projectService"; 
import PostCard from "@/components/post/PostCard";
import Navbar from "@/components/common/Navbar";
import "./MainPage.css";

const POSTS_PER_PAGE = 9;

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

const DURATION_PRESETS = [
    { label: "전체", min: 0, max: Infinity },
    { label: "1~3개월", min: 1, max: 3 },
    { label: "1~6개월", min: 1, max: 6 },
    { label: "6~12개월", min: 6, max: 12 },
    { label: "12개월 이상", min: 12, max: Infinity },
];

function MainPage({ isLoggedIn, onLogout }) {
    const navigate = useNavigate();

    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);

    const [searchText, setSearchText] = useState("");
    const [selectedTags, setSelectedTags] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [activeTab, setActiveTab] = useState("인기");

    const [isTagOpen, setIsTagOpen] = useState(false);
    const tagDropdownRef = useRef(null);

    const [isDurationOpen, setIsDurationOpen] = useState(false);
    const [durationPreset, setDurationPreset] = useState("전체");
    const [customMin, setCustomMin] = useState("");
    const [customMax, setCustomMax] = useState("");
    const [useCustom, setUseCustom] = useState(false);
    const durationDropdownRef = useRef(null);

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                setLoading(true);
                const data = await projectService.getAllProjects();
                setPosts(data);
            } catch (error) {
                console.error("데이터 로딩 실패:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchPosts();
    }, []);

    useEffect(() => {
        function handleClickOutside(e) {
            if (tagDropdownRef.current && !tagDropdownRef.current.contains(e.target)) setIsTagOpen(false);
            if (durationDropdownRef.current && !durationDropdownRef.current.contains(e.target)) setIsDurationOpen(false);
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    function toggleTag(tag) {
        setSelectedTags((prev) =>
            prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
        );
        setCurrentPage(1);
    }

    const activeDurationRange = useMemo(() => {
        if (useCustom) {
            return {
                min: customMin !== "" ? Number(customMin) : 0,
                max: customMax !== "" ? Number(customMax) : Infinity,
            };
        }
        const preset = DURATION_PRESETS.find((p) => p.label === durationPreset);
        return preset ? { min: preset.min, max: preset.max } : { min: 0, max: Infinity };
    }, [useCustom, customMin, customMax, durationPreset]);

    const durationLabel = useMemo(() => {
        if (useCustom && (customMin !== "" || customMax !== "")) {
            const minStr = customMin !== "" ? `${customMin}개월` : "제한없음";
            const maxStr = customMax !== "" ? `${customMax}개월` : "제한없음";
            return `${minStr} ~ ${maxStr}`;
        }
        return durationPreset;
    }, [useCustom, customMin, customMax, durationPreset]);

    const isDurationFiltered = durationLabel !== "전체";

    const filteredPosts = useMemo(() => {
        return posts.filter((post) => {
            const matchesSearch =
                searchText === "" ||
                (post.title || "").includes(searchText) ||
                (post.description || "").includes(searchText);

            const matchesTags =
                selectedTags.length === 0 ||
                selectedTags.every((tag) => (post.tags || []).includes(tag));

            const months = Number(post.durationMonths) || 0;
            const { min, max } = activeDurationRange;
            const matchesDuration = months >= min && months <= max;

            return matchesSearch && matchesTags && matchesDuration;
        });
    }, [posts, searchText, selectedTags, activeDurationRange]);

    const totalPages = Math.max(1, Math.ceil(filteredPosts.length / POSTS_PER_PAGE));
    const pagedPosts = filteredPosts.slice(
        (currentPage - 1) * POSTS_PER_PAGE,
        currentPage * POSTS_PER_PAGE
    );

    function getPageNumbers() {
        const groupSize = 10;
        const groupStart = Math.floor((currentPage - 1) / groupSize) * groupSize + 1;
        const groupEnd = Math.min(groupStart + groupSize - 1, totalPages);
        const pages = [];
        for (let i = groupStart; i <= groupEnd; i++) pages.push(i);
        return { pages, groupStart, groupEnd };
    }
    const { pages, groupStart, groupEnd } = getPageNumbers();

    function handlePageChange(page) {
        setCurrentPage(page);
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function resetDuration() {
        setDurationPreset("전체");
        setCustomMin("");
        setCustomMax("");
        setUseCustom(false);
        setCurrentPage(1);
    }

    return (
        <div className="main-page">
            <Navbar isLoggedIn={isLoggedIn} onLogout={onLogout} />

            <main className="main-content">
                <section className="hero">
                    <div className="hero-badge">🚀 사이드 프로젝트 팀 빌딩 플랫폼</div>
                    <h1 className="hero-title">
                        함께할 팀원을<br />
                        <span className="hero-highlight">찾고 있나요?</span>
                    </h1>
                    <p className="hero-subtitle">개발자, 디자이너, 기획자들이 모여 아이디어를 현실로 만드는 공간</p>
                </section>

                <section className="search-section">
                    <div className="search-bar-wrapper">
                        <span className="search-icon">🔍</span>
                        <input
                            type="text" className="search-input"
                            placeholder="프로젝트 제목이나 설명으로 검색..."
                            value={searchText}
                            onChange={(e) => { setSearchText(e.target.value); setCurrentPage(1); }}
                        />
                    </div>
                    <button className="btn-write" onClick={() => navigate("/write")}>+ 글 작성</button>
                </section>

                <section className="filter-bar">
                    {/* 기술 스택 드롭다운 */}
                    <div className="tag-dropdown-wrapper" ref={tagDropdownRef}>
                        <button
                            className={`filter-btn ${isTagOpen || selectedTags.length > 0 ? "active" : ""}`}
                            onClick={() => setIsTagOpen((prev) => !prev)}
                        >
                            🛠 기술 스택
                            {selectedTags.length > 0 && <span className="filter-badge">{selectedTags.length}</span>}
                            <span className={`filter-arrow ${isTagOpen ? "open" : ""}`}>▾</span>
                        </button>
                        {isTagOpen && (
                            <div className="tag-dropdown-panel">
                                <div className="tag-tab-nav">
                                    {TAB_KEYS.map((tab) => (
                                        <button key={tab} className={`tag-tab-btn ${activeTab === tab ? "active" : ""}`} onClick={() => setActiveTab(tab)}>{tab}</button>
                                    ))}
                                </div>
                                <div className="tag-grid">
                                    {TAG_TABS[activeTab].map((tag) => (
                                        <button key={tag} className={`tag-chip ${selectedTags.includes(tag) ? "selected" : ""}`} onClick={() => toggleTag(tag)}>{tag}</button>
                                    ))}
                                </div>
                                {selectedTags.length > 0 && (
                                    <div className="tag-dropdown-footer">
                                        <span className="tag-selected-count">{selectedTags.length}개 선택됨</span>
                                        <button className="btn-clear" onClick={() => { setSelectedTags([]); setCurrentPage(1); }}>초기화 ✕</button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* 기간 필터 드롭다운 */}
                    <div className="tag-dropdown-wrapper" ref={durationDropdownRef}>
                        <button
                            className={`filter-btn ${isDurationOpen || isDurationFiltered ? "active" : ""}`}
                            onClick={() => setIsDurationOpen((prev) => !prev)}
                        >
                            📅 {durationLabel}
                            {isDurationFiltered && (
                                <span className="filter-clear-x" onClick={(e) => { e.stopPropagation(); resetDuration(); }}>✕</span>
                            )}
                            <span className={`filter-arrow ${isDurationOpen ? "open" : ""}`}>▾</span>
                        </button>
                        {isDurationOpen && (
                            <div className="duration-dropdown-panel">
                                <p className="duration-panel-title">프로젝트 기간</p>
                                <div className="duration-presets">
                                    {DURATION_PRESETS.map((preset) => (
                                        <button key={preset.label} className={`duration-preset-btn ${!useCustom && durationPreset === preset.label ? "active" : ""}`} onClick={() => { setDurationPreset(preset.label); setUseCustom(false); setCurrentPage(1); }}>{preset.label}</button>
                                    ))}
                                </div>
                                <div className="duration-divider">직접 입력</div>
                                <div className="duration-custom-row">
                                    <input type="number" className="duration-custom-input" placeholder="최소" value={customMin} onChange={(e) => { setCustomMin(e.target.value); setUseCustom(true); setCurrentPage(1); }} />
                                    <span className="duration-range-sep">개월 ~</span>
                                    <input type="number" className="duration-custom-input" placeholder="최대" value={customMax} onChange={(e) => { setCustomMax(e.target.value); setUseCustom(true); setCurrentPage(1); }} />
                                    <span className="duration-range-sep">개월</span>
                                </div>
                                {isDurationFiltered && (
                                    <button className="btn-clear duration-reset-btn" onClick={resetDuration}>필터 초기화 ✕</button>
                                )}
                            </div>
                        )}
                    </div>
                </section>

                {/* 선택된 태그 칩 */}
                {selectedTags.length > 0 && (
                    <div className="selected-tag-preview">
                        {selectedTags.map((tag) => (
                            <span key={tag} className="selected-tag-chip" onClick={() => toggleTag(tag)}>{tag} ✕</span>
                        ))}
                    </div>
                )}

                <div className="result-count">
                    총 <strong>{filteredPosts.length}</strong>개의 모집 글
                </div>

                {/* 로딩 상태 표시 */}
                {loading ? (
                    <div className="empty-state">데이터를 불러오는 중입니다...</div>
                ) : pagedPosts.length > 0 ? (
                    <div className="post-grid">
                        {pagedPosts.map((post) => (
                            <PostCard key={post.id} post={post} onClick={() => navigate(`/post/${post.id}`)} />
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">
                        <span className="empty-icon">🔍</span>
                        <p>검색 결과가 없습니다.</p>
                    </div>
                )}

                {totalPages > 1 && (
                    <div className="pagination">
                        <button className="page-btn page-nav" onClick={() => handlePageChange(groupStart - 1)} disabled={groupStart === 1}>‹</button>
                        {pages.map((page) => (
                            <button key={page} className={`page-btn ${currentPage === page ? "active" : ""}`} onClick={() => handlePageChange(page)}>{page}</button>
                        ))}
                        <button className="page-btn page-nav" onClick={() => handlePageChange(groupEnd + 1)} disabled={groupEnd === totalPages}>›</button>
                    </div>
                )}
            </main>
        </div>
    );
}

export default MainPage;