import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { MOCK_POSTS, ALL_TAGS } from "../../data/mockData";
import PostCard from "@/components/post/PostCard";
import Navbar from "@/components/common/Navbar";
import "./MainPage.css";

// ✅ 1번째 변경: { isLoggedIn, onLogout } 추가
function MainPage({ isLoggedIn, onLogout }) {
    const navigate = useNavigate();

    const [searchText, setSearchText] = useState("");
    const [selectedTags, setSelectedTags] = useState([]);

    const [durationFilter, setDurationFilter] = useState("전체");

    function toggleTag(tag) {
        setSelectedTags((prev) =>
            prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
        );
    }

    const filteredPosts = useMemo(() => {
        return MOCK_POSTS.filter((post) => {
            const matchesSearch =
                searchText === "" ||
                post.title.includes(searchText) ||
                post.description.includes(searchText);
            const matchesTags =
                selectedTags.length === 0 ||
                selectedTags.every((tag) => post.tags.includes(tag));
            const matchesDuration =
                durationFilter === "전체" || post.duration === durationFilter;

            return matchesSearch && matchesTags && matchesDuration;
        });
    }, [searchText, selectedTags, durationFilter]);

    return (
        <div className="main-page">
            {/* ✅ 2번째 변경: isLoggedIn, onLogout 전달 */}
            <Navbar isLoggedIn={isLoggedIn} onLogout={onLogout} />

            <main className="main-content">
                <section className="hero">
                    <div className="hero-badge">🚀 사이드 프로젝트 팀 빌딩 플랫폼</div>
                    <h1 className="hero-title">
                        함께할 팀원을<br />
                        <span className="hero-highlight">찾고 있나요?</span>
                    </h1>
                    <p className="hero-subtitle">
                        개발자, 디자이너, 기획자들이 모여 아이디어를 현실로 만드는 공간
                    </p>
                </section>

                <section className="search-section">
                    <div className="search-bar-wrapper">
                        <span className="search-icon">🔍</span>
                        <input
                            type="text"
                            className="search-input"
                            placeholder="프로젝트 제목이나 설명으로 검색..."
                            value={searchText}
                            onChange={(e) => setSearchText(e.target.value)}
                        />
                    </div>
                    <button className="btn-write" onClick={() => navigate("/write")}>
                        + 글 작성
                    </button>
                </section>

                <section className="tag-filter-section">
                    <span className="filter-label">기술 스택 필터</span>
                    <div className="tag-filter-list">
                        {ALL_TAGS.map((tag) => (
                            <button
                                key={tag}
                                className={`tag-filter-btn ${selectedTags.includes(tag) ? "active" : ""}`}
                                onClick={() => toggleTag(tag)}
                            >
                                {tag}
                            </button>
                        ))}
                    </div>
                    {selectedTags.length > 0 && (
                        <button className="btn-clear" onClick={() => setSelectedTags([])}>
                            필터 초기화 ✕
                        </button>
                    )}
                </section>

                <div className="duration-filter">
                    {["전체", "단기", "장기"].map((d) => (
                        <button
                            key={d}
                            className={`tag-filter-btn ${durationFilter === d ? "active" : ""}`}
                            onClick={() => setDurationFilter(d)}
                        >
                            {d === "단기" ? "⚡ 단기" : d === "장기" ? "📅 장기" : d}
                        </button>
                    ))}
                </div>

                <div className="result-count">
                    총 <strong>{filteredPosts.length}</strong>개의 모집 글
                </div>

                {filteredPosts.length > 0 ? (
                    <div className="post-grid">
                        {filteredPosts.map((post) => (
                            <PostCard
                                key={post.id}
                                post={post}
                                onClick={() => navigate(`/post/${post.id}`)}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">
                        <span className="empty-icon">🔍</span>
                        <p>검색 결과가 없습니다.</p>
                        <small>다른 키워드나 태그로 검색해보세요.</small>
                    </div>
                )}
            </main>
        </div>
    );
}

export default MainPage;