import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { reviewService } from "../../services/reviewService";
import './ReviewPage.css';

const ReviewPage = () => {
  const navigate = useNavigate();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadMyReviews = async () => {
      try {
        setLoading(true);
        const data = await reviewService.getMyReviews();
        setReviews(data);
      } catch (error) {
        console.error("리뷰 로드 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    loadMyReviews();
  }, []);

  if (loading) {
    return <div className="review-page-container">리뷰 로딩 중...</div>;
  }

  return (
    <div className="review-page-container">
      <button className="back-btn" onClick={() => navigate(-1)}>← 뒤로가기</button>
      <h2>⭐ 내게 도착한 리뷰</h2>

      <div className="review-list">
        {reviews.length > 0 ? (
          reviews.map((r) => (
            <div key={r.review_id} className="review-card">
              <div className="r-header">
                <div className="r-info-group">
                  <span className="r-author">{r.reviewer_name || "익명"}</span>
                  <span className="r-project-name">[{r.project_title || "프로젝트"}]</span>
                </div>
                <span className="r-stars">{"★".repeat(r.rating || 0)}</span>
              </div>
              <p className="r-comment">{r.comment || "내용 없음"}</p>
            </div>
          ))
        ) : (
          <p className="empty-msg">아직 도착한 리뷰가 없습니다.</p>
        )}
      </div>
    </div>
  );
};

export default ReviewPage;