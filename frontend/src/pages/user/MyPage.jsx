import React, { useState, useEffect } from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom';
import { MOCK_POSTS } from "../../data/mockData"; 
import { getAppliedIds } from "../../services/applyService"; 

const MyPage = () => {
  const navigate = useNavigate();
  
  const [user, setUser] = useState({
    name: "강무원",
    organization: "조선대",
    introduction: "안녕하세요!",
    tags: ["React", "JavaScript", "CS"],
    job_role: "개발자",
    profileImg: null
  });
  
  const [appliedPosts, setAppliedPosts] = useState([]);

  useEffect(() => {
    const savedUser = JSON.parse(localStorage.getItem("user"));
    if (!savedUser) {
      alert("로그인이 필요한 페이지입니다.");
      navigate("/login");
      return;
    }

    setUser({
      ...savedUser,
      name: savedUser.nickname || savedUser.name || "강무원",
      profileImg: savedUser.profileImg || null
    });

    const ids = getAppliedIds();
    const filteredApplied = MOCK_POSTS.filter(post => 
      ids.includes(post.id) && 
      post.author !== (savedUser.nickname || savedUser.name) && 
      ![1, 3].includes(post.id)
    );
    
    setAppliedPosts(filteredApplied);
  }, [navigate]);

  const myLeadProjects = MOCK_POSTS.filter(post => post.author === user.name);
  const myParticipation = MOCK_POSTS.filter(post => [1, 3].includes(post.id));

  // 채팅방 이동 함수
  const handleChatMove = (e, projectId) => {
    e.stopPropagation(); // 카드 전체 클릭 이벤트(상세보기)가 실행되지 않도록 방지
    navigate(`/chat/${projectId}`);
    };

    // 팀원 리뷰 평가
    const [rating, setRating] = useState(0);
    const [hovered, setHovered] = useState(0);
    const [comment, setComment] = useState("");
    const [reviews, setReviews] = useState([
        { id: 1, author: "김지훈", rating: 5, comment: "소통이 잘 되고 책임감 있어요!" },
        { id: 2, author: "박소연", rating: 4, comment: "일정 관리를 잘 해줬어요." },
    ]);

    function handleSubmitReview() {
        if (rating === 0) { alert("별점을 선택해주세요!"); return; }
        if (comment.trim() === "") { alert("한줄평을 입력해주세요!"); return; }
        setReviews([...reviews, {
            id: reviews.length + 1,
            author: user.name,
            rating: rating,
            comment: comment,
        }]);
        setRating(0);
        setComment("");
        alert("리뷰가 등록되었습니다!");
    }

  return (
    <div className="mp-container"> 
      <div className="mp-card"> 
        <button className="mp-back-btn" onClick={() => navigate('/')}>
          ← 메인으로
        </button>

        <div className="mp-header">
          {user.profileImg ? (
            <img src={user.profileImg} alt="프로필" className="mp-avatar-img" />
          ) : (
            <div className="mp-avatar">{user.name ? user.name[0] : "무"}</div>
          )}

          <div className="mp-info">
            <div className="mp-info-top">
              <h3>{user.name} 님</h3>
              <button className="mp-setting-btn" onClick={() => navigate('/MyPageSetting')}>프로필 수정</button>
            </div>
            <div className="mp-org-row">
              <span className="mp-org-label">소속</span>
              <span className="mp-org-value">{user.organization || "소속 없음"}</span>
            </div>
            <div className="mp-tags">
              {user.tags && user.tags.map(tag => (
                <span key={tag} className="mp-tag">#{tag}</span>
              ))}
            </div>
            <p className="mp-bio">{user.introduction}</p>
          </div>
        </div>

        <hr className="mp-divider" />

        {/* 📋 내가 만든 프로젝트 */}
        <div className="mp-section">
          <h4>내가 만든 프로젝트</h4>
          {myLeadProjects.length > 0 ? (
            myLeadProjects.map(proj => (
              <div key={proj.id} className="mp-activity-card clickable" onClick={() => navigate(`/post/${proj.id}`)}>
                <div className="proj-info">
                  <span className="title">{proj.title}</span>
                  <span className="sub-info">👥 {proj.headcount}명 모집</span>
                </div>
                <span className="mp-badge status-recruiting">모집중</span>
              </div>
            ))
          ) : (
            <div className="mp-activity-card no-data">작성한 게시글이 없습니다.</div>
          )}
        </div>

        {/* 🤝 참여 중인 프로젝트 (채팅 버튼 추가됨) */}
        <div className="mp-section" style={{ marginTop: '25px' }}>
          <h4>참여 중인 프로젝트</h4>
          {myParticipation.length > 0 ? (
            myParticipation.map(proj => (
              <div key={proj.id} className="mp-activity-card clickable" onClick={() => navigate(`/post/${proj.id}`)}>
                <div className="proj-info">
                  <span className="title">{proj.title}</span>
                  <span className="sub-info">🛠 {proj.roles ? proj.roles[0] : "팀원"}</span>
                </div>
                <div className="mp-card-actions">
                  {/* ✅ 채팅방 이동 버튼 추가 */}
                  <button className="mp-action-chat-btn" onClick={(e) => handleChatMove(e, proj.id)}>
                    💬 채팅
                  </button>
                  <span className="mp-badge status-ongoing">진행중</span>
                </div>
              </div>
            ))
          ) : (
            <div className="mp-activity-card no-data">참여 중인 프로젝트가 없습니다.</div>
          )}
        </div>

              {/* ⭐ 팀원 리뷰 — 추가 */}
              <div className="mp-section" style={{ marginTop: '25px' }}>
                  <h4>⭐ 팀원 리뷰</h4>

                  {/* 리뷰 작성 폼 */}
                  <div className="mp-review-form">
                      {/* 별점 선택 */}
                      <div className="mp-star-row">
                          {[1, 2, 3, 4, 5].map((star) => (
                              <span
                                  key={star}
                                  className={`mp-star ${star <= (hovered || rating) ? "filled" : ""}`}
                                  onClick={() => setRating(star)}
                                  onMouseEnter={() => setHovered(star)}
                                  onMouseLeave={() => setHovered(0)}
                              >
                                  ★
                              </span>
                          ))}
                          <span className="mp-rating-text">
                              {rating > 0 ? `${rating}점` : "별점 선택"}
                          </span>
                      </div>

                      {/* 한줄평 입력 */}
                      <textarea
                          className="mp-review-input"
                          placeholder="이 팀원과의 협업은 어땠나요?"
                          value={comment}
                          onChange={(e) => setComment(e.target.value)}
                          rows={2}
                      />

                      <button className="mp-review-submit-btn" onClick={handleSubmitReview}>
                          리뷰 등록
                      </button>
                  </div>

                  {/* 등록된 리뷰 목록 */}
                  <div className="mp-review-list">
                      {reviews.map((review) => (
                          <div key={review.id} className="mp-review-item">
                              <div className="mp-review-header">
                                  <span className="mp-review-author">{review.author}</span>
                                  <span className="mp-review-stars">
                                      {"★".repeat(review.rating)}{"☆".repeat(5 - review.rating)}
                                  </span>
                              </div>
                              <p className="mp-review-comment">{review.comment}</p>
                          </div>
                      ))}
                  </div>
              </div>

              <hr className="mp-divider" />


        {/* 📩 지원 중인 모집글 */}
        <div className="mp-section" style={{ marginTop: '25px' }}>
          <h4>지원 중인 모집글</h4>
          {appliedPosts.length > 0 ? (
            appliedPosts.map(proj => (
              <div key={proj.id} className="mp-activity-card clickable" onClick={() => navigate(`/post/${proj.id}`)}>
                <div className="proj-info">
                  <span className="title">{proj.title}</span>
                  <span className="sub-info">📍 {proj.category}</span>
                </div>
                <span className="mp-badge status-pending">지원완료</span>
              </div>
            ))
          ) : (
            <div className="mp-activity-card no-data">아직 지원한 프로젝트가 없습니다.</div>
          )}
        </div>

        <hr className="mp-divider" />

        <div className="mp-footer-tags">
            <span className="mp-tag-badge blue">⚡ {user.job_role || "개발자"}</span>
            <span className="mp-tag-badge green">🤝 협업 가능</span>
        </div>
      </div>
      </div>


  );
};

export default MyPage;