import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { MOCK_POSTS } from "../../data/mockData";
import Navbar from "@/components/common/Navbar";
import "./DetailPage.css";
import { applyToProject } from "../../services/applyService"; 

function DetailPage() {
  const { id } = useParams(); 
  const navigate = useNavigate();
  const [isApplied, setIsApplied] = useState(false);

  const post = MOCK_POSTS.find((p) => p.id === Number(id));

  // [수정] applyService의 'appliedProjects' 키와 숫자 배열 구조에 맞춤
  useEffect(() => {
    const appliedIds = JSON.parse(localStorage.getItem("appliedProjects") || "[]");
    
    // appliedIds가 [1, 2, 3] 형태이므로 includes로 바로 확인 가능
    const alreadyApplied = appliedIds.includes(Number(id));
    
    setIsApplied(alreadyApplied);
  }, [id]);

  // [수정] 지원 취소 로직 (단순 숫자 배열에서 해당 ID 삭제)
  const handleCancel = () => {
    if (window.confirm("정말 이 프로젝트 지원을 취소하시겠습니까?")) {
      const appliedIds = JSON.parse(localStorage.getItem("appliedProjects") || "[]");
      
      // 현재 게시글 ID만 제외하고 필터링
      const updatedIds = appliedIds.filter((appliedId) => Number(appliedId) !== Number(id));

      localStorage.setItem("appliedProjects", JSON.stringify(updatedIds));
      setIsApplied(false);
        alert("지원이 취소되었습니다.");
        navigate('/MyPage');
    }
  };

  const handleApply = () => {
    const success = applyToProject(Number(id)); 

    if (success) {
      alert(`"${post.title}" 프로젝트에 지원했습니다! 마이페이지로 이동합니다.`);
      setIsApplied(true); 
      navigate('/MyPage');
    } else {
      alert("이미 지원한 프로젝트입니다.");
    }
  };

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

  return (
    <div className="detail-page">
      <Navbar />
      <main className="detail-content">
        <button className="btn-back" onClick={() => navigate(-1)}>
          ← 목록으로
        </button>

        <article className="detail-card">
          <div className="detail-meta">
            <span className="badge-category">{post.category}</span>
            <span className="meta-divider">·</span>
            <span className="meta-text">👥 {post.headcount}명 모집 중</span>
            <span className="meta-divider">·</span>
            <span className="meta-text">{post.createdAt}</span>
          </div>

          <h1 className="detail-title">{post.title}</h1>

          <div className="detail-author">
            <div className="author-avatar">{post.author[0]}</div>
            <span>{post.author}</span>
          </div>

          <hr className="divider" />

          <section className="detail-section">
            <h2 className="section-title">📋 프로젝트 소개</h2>
            <p className="detail-description">{post.description}</p>
          </section>

          <section className="detail-section">
            <h2 className="section-title">🙋 모집 분야</h2>
            <div className="roles-list">
              {post.roles.map((role) => (
                <span key={role} className="badge-role">{role}</span>
              ))}
            </div>
          </section>

          <section className="detail-section">
            <h2 className="section-title">🛠 기술 스택</h2>
            <div className="tags-list">
              {post.tags.map((tag) => (
                <span key={tag} className="tag">{tag}</span>
              ))}
            </div>
          </section>

          <section className="detail-section">
            <h2 className="section-title">📋 모집 조건</h2>
            <div className="tags-list">
              <span className={`badge-level ${post.level}`}>
                 {post.level === "초보" ? "🌱 초보" : post.level === "중급" ? "⚡ 중급" : "🔥 고수"}
              </span>
              <span className="badge-exp">
                {post.hasTeamExp === "있음" ? "🤝 협업 경험 있음" : "🙋 협업 경험 없음"}
              </span>
            </div>
          </section>

          <div className="detail-footer" style={{ marginTop: "40px", display: "flex", justifyContent: "center" }}>
            {isApplied ? (
              <button className="btn-cancle" onClick={handleCancel}>
                지원 취소하기
              </button>
            ) : (
              <button className="btn-apply" onClick={handleApply}>
                프로젝트 지원하기
              </button>
            )}
          </div>
        </article>
      </main>
    </div>
  );
}

export default DetailPage;