// 📁 src/pages/DetailPage.jsx
// 모집 글 상세 페이지입니다.
//
// 사용하는 React 개념:
//   - useParams: URL의 :id 값을 읽어서 어떤 글인지 파악
//     예) URL이 /post/3 이면 → params.id === "3"
//   - useNavigate: 버튼 클릭으로 이전 페이지로 이동

import { useParams, useNavigate } from "react-router-dom";
import { MOCK_POSTS } from "../../data/mockData";
import Navbar from "@/components/common/Navbar";
import "./DetailPage.css";

function DetailPage() {
  // URL에서 id 값 추출 (예: /post/2 → id = "2")
  const { id } = useParams();
  const navigate = useNavigate();

  // id로 해당 게시글 찾기 (문자열 "2"를 숫자 2로 변환해서 비교)
  const post = MOCK_POSTS.find((p) => p.id === Number(id));

  // 해당 id의 글이 없을 경우 처리
  if (!post) {
    return (
      <div className="detail-page">
        <Navbar />
        <div className="not-found">
          <span>🔍</span>
          <h2>글을 찾을 수 없습니다.</h2>
          <button onClick={() => navigate("/")}>메인으로 돌아가기</button>
        </div>
      </div>
    );
  }

  // 지원하기 버튼 클릭 핸들러
  function handleApply() {
    alert(`"${post.title}" 프로젝트에 지원했습니다!\n(현재는 Mock으로 동작)`);
  }

  return (
    <div className="detail-page">
      <Navbar />

      <main className="detail-content">

        {/* 뒤로가기 */}
        <button className="btn-back" onClick={() => navigate(-1)}>
          ← 목록으로
        </button>

        <article className="detail-card">

          {/* 상단 메타 정보 */}
          <div className="detail-meta">
            <span className="badge-category">{post.category}</span>
            <span className="meta-divider">·</span>
            <span className="meta-text">👥 {post.headcount}명 모집 중</span>
            <span className="meta-divider">·</span>
            <span className="meta-text">{post.createdAt}</span>
          </div>

          {/* 제목 */}
          <h1 className="detail-title">{post.title}</h1>

          {/* 작성자 */}
          <div className="detail-author">
            <div className="author-avatar">{post.author[0]}</div>
            <span>{post.author}</span>
          </div>

          {/* 구분선 */}
          <hr className="divider" />

          {/* 프로젝트 설명 */}
          <section className="detail-section">
            <h2 className="section-title">📋 프로젝트 소개</h2>
            <p className="detail-description">{post.description}</p>
          </section>

          {/* 모집 분야 */}
          <section className="detail-section">
            <h2 className="section-title">🙋 모집 분야</h2>
            <div className="roles-list">
              {post.roles.map((role) => (
                <span key={role} className="badge-role">{role}</span>
              ))}
            </div>
          </section>

          {/* 기술 스택 */}
          <section className="detail-section">
            <h2 className="section-title">🛠 기술 스택</h2>
            <div className="tags-list">
              {post.tags.map((tag) => (
                <span key={tag} className="tag">{tag}</span>
              ))}
            </div>
          </section>

          {/* 지원하기 버튼 */}
          <div className="detail-actions">
            <button className="btn-apply" onClick={handleApply}>
              🚀 이 프로젝트에 지원하기
            </button>
          </div>

        </article>
      </main>
    </div>
  );
}

export default DetailPage;
