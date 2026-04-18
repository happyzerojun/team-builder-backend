import { useState, useMemo, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { projectService } from "../../services/projectService";
import PostCard from "@/components/post/PostCard";
import Navbar from "@/components/common/Navbar";
import "./MainPage.css";
import Pagination from "@/components/common/Pagination";
import SearchBar from "@/components/common/SearchBar";
import TagFilterDropdown from "@/components/filter/TagFilterDropdown";
import DurationFilterDropdown from "@/components/filter/DurationFilterDropdown";

const POSTS_PER_PAGE = 9;

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
    const [isDurationOpen, setIsDurationOpen] = useState(false);
    const [durationPreset, setDurationPreset] = useState("전체");
    const [customMin, setCustomMin] = useState("");
    const [customMax, setCustomMax] = useState("");
    const [useCustom, setUseCustom] = useState(false);

    useEffect(() => {
        const fetchPosts = async () => {
            try {
                setLoading(true);
                const data = await projectService.getAllProjects();
                setPosts(Array.isArray(data) ? data : []);
            } catch (error) {
                console.error("데이터 로딩 실패:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPosts();
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
        return posts
            .filter((post) => {
                const matchesSearch =
                    searchText === "" ||
                    (post.title || "").includes(searchText) ||
                    (post.content || "").includes(searchText);

                // 현재 DB 구조상 tags 없을 가능성 높아서 안전 처리
                const matchesTags =
                    selectedTags.length === 0 ||
                    selectedTags.every((tag) => (post.tags || []).includes(tag));

                // term이 숫자(개월)라고 가정
                const months = Number(post.term) || 0;
                const { min, max } = activeDurationRange;
                const matchesDuration = months >= min && months <= max;

                return matchesSearch && matchesTags && matchesDuration;
            })
            .sort((a, b) => {
                const dateA = new Date(a.created_at || a.createdAt || 0);
                const dateB = new Date(b.created_at || b.createdAt || 0);
                return dateB - dateA;
            });
    }, [posts, searchText, selectedTags, activeDurationRange]);

    const totalPages = Math.max(1, Math.ceil(filteredPosts.length / POSTS_PER_PAGE));
    const pagedPosts = filteredPosts.slice(
        (currentPage - 1) * POSTS_PER_PAGE,
        currentPage * POSTS_PER_PAGE
    );

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
                        함께할 팀원을
                        <br />
                        <span className="hero-highlight">찾고 있나요?</span>
                    </h1>
                    <p className="hero-subtitle">
                        개발자, 디자이너, 기획자들이 모여 아이디어를 현실로 만드는 공간
                    </p>
                </section>

                <div className="ai-btn-wrapper">
                    <button
                        className="btn-ai"
                        onClick={() => navigate("/ai")}
                    >
                                🤖 AI 추천 받기
                    </button>
                </div>

                <SearchBar
                    searchText={searchText}
                    onSearchChange={(value) => {
                        setSearchText(value);
                        setCurrentPage(1);
                    }}
                />

                <section className="filter-bar">
                    <TagFilterDropdown
                        selectedTags={selectedTags}
                        onToggleTag={toggleTag}
                        onClear={() => {
                            setSelectedTags([]);
                            setCurrentPage(1);
                        }}
                        activeTab={activeTab}
                        onTabChange={setActiveTab}
                        isOpen={isTagOpen}
                        onToggleOpen={() => setIsTagOpen((prev) => !prev)}
                    />

                    <DurationFilterDropdown
                        isOpen={isDurationOpen}
                        onToggleOpen={() => setIsDurationOpen((prev) => !prev)}
                        durationPreset={durationPreset}
                        onPresetChange={(label) => {
                            setDurationPreset(label);
                            setUseCustom(false);
                            setCurrentPage(1);
                        }}
                        customMin={customMin}
                        customMax={customMax}
                        onCustomMinChange={(v) => {
                            setCustomMin(v);
                            setUseCustom(true);
                            setCurrentPage(1);
                        }}
                        onCustomMaxChange={(v) => {
                            setCustomMax(v);
                            setUseCustom(true);
                            setCurrentPage(1);
                        }}
                        useCustom={useCustom}
                        durationLabel={durationLabel}
                        isDurationFiltered={isDurationFiltered}
                        onReset={resetDuration}
                    />
                </section>

                {selectedTags.length > 0 && (
                    <div className="selected-tag-preview">
                        {selectedTags.map((tag) => (
                            <span
                                key={tag}
                                className="selected-tag-chip"
                                onClick={() => toggleTag(tag)}
                            >
                                {tag} ✕
                            </span>
                        ))}
                    </div>
                )}

                <div className="result-count">
                    총 <strong>{filteredPosts.length}</strong>개의 모집 글
                </div>

                {loading ? (
                    <div className="empty-state">데이터를 불러오는 중입니다...</div>
                ) : pagedPosts.length > 0 ? (
                    <div className="post-grid">
                        {pagedPosts.map((post) => (
                            <PostCard
                                key={post.project_id}
                                post={post}
                                onClick={() => navigate(`/post/${post.project_id}`)}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">
                        <span className="empty-icon">🔍</span>
                        <p>검색 결과가 없습니다.</p>
                    </div>
                )}

                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                />
            </main>
        </div>
    );
}

export default MainPage;