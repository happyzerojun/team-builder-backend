// 📁 src/pages/MainPage.jsx
// 메인 페이지: 모집 글 목록을 보여주는 핵심 페이지입니다.
//
// 사용하는 React 개념:
//   - useState: 검색어, 선택된 태그, 목록 데이터를 상태로 관리
//   - useMemo: 검색/필터 결과를 계산 (불필요한 재계산 방지)
//   - useNavigate: 글 클릭 시 상세 페이지로, 글쓰기 버튼 클릭 시 작성 페이지로 이동

import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { MOCK_POSTS, ALL_TAGS } from "../../data/mockData";
import PostCard from "@/components/post/PostCard";
import Navbar from "@/components/common/Navbar";
import "./MainPage.css";

function MainPage() {
  const navigate = useNavigate();

  // ── 상태(state) 정의 ──────────────────────────────────────
  // 검색창에 입력한 텍스트
  const [searchText, setSearchText] = useState("");
  // 선택된 태그들 (배열: 여러 개 선택 가능)
  const [selectedTags, setSelectedTags] = useState([]);

  // ── 태그 토글 함수 ─────────────────────────────────────────
  // 이미 선택된 태그를 클릭하면 제거, 아니면 추가
  function toggleTag(tag) {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  }

  // ── 필터링된 게시글 목록 ────────────────────────────────────
  // useMemo: searchText나 selectedTags가 바뀔 때만 다시 계산
  const filteredPosts = useMemo(() => {
    return MOCK_POSTS.filter((post) => {
      // 1) 검색어 필터: 제목 또는 설명에 검색어가 포함된 경우
      const matchesSearch =
        searchText === "" ||
        post.title.includes(searchText) ||
        post.description.includes(searchText);

      // 2) 태그 필터: 선택된 태그가 모두 포함된 경우
      const matchesTags =
        selectedTags.length === 0 ||
        selectedTags.every((tag) => post.tags.includes(tag));

      return matchesSearch && matchesTags;
    });
  }, [searchText, selectedTags]);

  return (
    <div className="main-page">
      <Navbar />

      <main className="main-content">

        {/* ── 히어로 섹션 ── */}
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

        {/* ── 검색 + 글쓰기 버튼 ── */}
        <section className="search-section">
          <div className="search-bar-wrapper">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              className="search-input"
              placeholder="프로젝트 제목이나 설명으로 검색..."
              value={searchText}
              // 입력할 때마다 searchText 상태 업데이트
              onChange={(e) => setSearchText(e.target.value)}
            />
          </div>
          {/* 글쓰기 버튼: 클릭하면 /write 페이지로 이동 */}
          <button
            className="btn-write"
            onClick={() => navigate("/write")}
          >
            + 글 작성
          </button>
        </section>

        {/* ── 태그 필터 ── */}
        <section className="tag-filter-section">
          <span className="filter-label">기술 스택 필터</span>
          <div className="tag-filter-list">
            {ALL_TAGS.map((tag) => (
              <button
                key={tag}
                // 선택된 태그면 active 클래스 추가 (CSS에서 다른 스타일 적용)
                className={`tag-filter-btn ${selectedTags.includes(tag) ? "active" : ""}`}
                onClick={() => toggleTag(tag)}
              >
                {tag}
              </button>
            ))}
          </div>
          {/* 선택된 태그가 있을 때만 초기화 버튼 표시 */}
          {selectedTags.length > 0 && (
            <button
              className="btn-clear"
              onClick={() => setSelectedTags([])}
            >
              필터 초기화 ✕
            </button>
          )}
        </section>

        {/* ── 결과 카운트 ── */}
        <div className="result-count">
          총 <strong>{filteredPosts.length}</strong>개의 모집 글
        </div>

        {/* ── 게시글 카드 목록 ── */}
        {filteredPosts.length > 0 ? (
          <div className="post-grid">
            {filteredPosts.map((post) => (
              // 각 글 카드 클릭 시 /post/:id 경로로 이동
              <PostCard
                key={post.id}
                post={post}
                onClick={() => navigate(`/post/${post.id}`)}
              />
            ))}
          </div>
        ) : (
          // 검색 결과가 없을 때 표시
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
