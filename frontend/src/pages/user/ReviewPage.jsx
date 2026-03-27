import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { projectService } from "../../services/projectService";
import './ReviewPage.css';

const ReviewPage = () => {
  const navigate = useNavigate();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadMyReviews = async () => {
      try {
        setLoading(true);
        const allProjects = await projectService.getAllProjects();

        const savedUser = JSON.parse(localStorage.getItem("user"));
        const myName = savedUser?.nickname || savedUser?.name || "";

        const myReceivedReviews = [];

        allProjects.forEach((proj) => {
          const reviewData = proj.myReviewData;

          if (reviewData && reviewData.reviews && reviewData.reviews[myName]) {
            myReceivedReviews.push({
              id: proj.id,
              author: proj.author, 
              rating: reviewData.ratings[myName], 
              comment: reviewData.reviews[myName], 
              projectTitle: proj.title 
            });
          }
        });

        setReviews(myReceivedReviews);
      } catch (error) {
        console.error("리뷰 로드 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    loadMyReviews();
  }, []);

  if (loading) return <div className="review-page-container">리뷰 로딩 중...</div>;

  return (
    <div className="review-page-container">
      <button className="back-btn" onClick={() => navigate(-1)}>← 뒤로가기</button>
      <h2>⭐ 내게 도착한 리뷰</h2>
      
      <div className="review-list">
        {reviews.length > 0 ? (
          reviews.map((r) => (
            <div key={r.id} className="review-card">
              <div className="r-header">
                <div className="r-info-group">
                  <span className="r-author">{r.author}</span>
                  <span className="r-project-name">[{r.projectTitle}]</span>
                </div>
                <span className="r-stars">{"★".repeat(r.rating)}</span>
              </div>
              <p className="r-comment">{r.comment}</p>
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