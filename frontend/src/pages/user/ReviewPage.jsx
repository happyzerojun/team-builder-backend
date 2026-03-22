import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ReviewPage.css';

const ReviewPage = () => {
  const navigate = useNavigate();
  const reviews = [
    { id: 1, author: "김지훈", rating: 5, comment: "소통이 잘 되고 책임감 있어요!" },
    { id: 2, author: "박소연", rating: 4, comment: "일정 관리를 잘 해줬어요." },
  ];

  return (
    <div className="review-page-container">
      <button className="back-btn" onClick={() => navigate(-1)}>← 뒤로가기</button>
      <h2>⭐ 내게 도착한 리뷰</h2>
      <div className="review-list">
        {reviews.map(r => (
          <div key={r.id} className="review-card">
            <div className="r-header">
              <span className="r-author">{r.author}</span>
              <span className="r-stars">{"★".repeat(r.rating)}</span>
            </div>
            <p className="r-comment">{r.comment}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ReviewPage;