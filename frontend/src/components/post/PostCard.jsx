// 📁 src/components/post/PostCard.jsx
// 메인 페이지에서 모집 글 하나를 카드 형태로 보여주는 컴포넌트입니다.
// props: post - 모집 글 데이터 객체 (mockData의 항목 하나)
//        onClick - 카드 클릭 시 호출될 함수

import "./PostCard.css";

function PostCard({ post, onClick }) {
  return (
    <article className="post-card" onClick={onClick}>

      {/* 카드 상단: 카테고리 배지 + 모집 인원 */}
      <div className="card-header">
        <span className="badge-category">{post.category}</span>
        <span className="card-headcount">👥 {post.headcount}명 모집</span>
      </div>

      {/* 제목 */}
      <h2 className="card-title">{post.title}</h2>

      {/* 설명 (길면 2줄로 자름) */}
      <p className="card-description">{post.description}</p>

      {/* 모집 분야 */}
      <div className="card-roles">
        {post.roles.map((role) => (
          <span key={role} className="badge-role">{role}</span>
        ))}
      </div>

      {/* 기술 스택 태그 */}
      <div className="card-tags">
        {post.tags.map((tag) => (
          <span key={tag} className="tag">{tag}</span>
        ))}
      </div>

      {/* 카드 하단: 작성자 + 날짜 */}
      <div className="card-footer">
        <span className="card-author">✍️ {post.author}</span>
        <span className="card-date">{post.createdAt}</span>
      </div>

    </article>
  );
}

export default PostCard;
