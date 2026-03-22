import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MOCK_POSTS } from "../../data/mockData";
import './ManagePage.css';

const ManagePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [project, setProject] = useState(null);
  const [isLeader, setIsLeader] = useState(false);
  const [loading, setLoading] = useState(true);
  const [currentMyName, setCurrentMyName] = useState("");

  const [members, setMembers] = useState([]);
  const [applicants, setApplicants] = useState([]);
  const [isReviewed, setIsReviewed] = useState(false); 
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [memberReviews, setMemberReviews] = useState({}); // { memberId: "내용" }
  const [memberRatings, setMemberRatings] = useState({}); // { memberId: 점수 }

  useEffect(() => {
    const loadData = () => {
      try {
        const projectId = parseInt(id);
        const currentProject = MOCK_POSTS.find(p => p.id === projectId);

        if (!currentProject) {
          navigate("/mypage");
          return;
        }
        const savedUser = JSON.parse(localStorage.getItem("user"));
        const myName = savedUser?.nickname || savedUser?.name || "강무원";
        setCurrentMyName(myName);
        setProject(currentProject);

        if (currentProject.members) {
          const syncMembers = currentProject.members.map(m => 
            (m.role.includes("팀장") && currentProject.author === myName) ? { ...m, name: myName } : m
          );
          setMembers(syncMembers);

          const initialReviews = {};
          const initialRatings = {};
          syncMembers.forEach(m => {
            if (m.name !== myName) {
              initialReviews[m.id] = "";
              initialRatings[m.id] = 5; 
            }
          });
          setMemberReviews(initialReviews);
          setMemberRatings(initialRatings);
        }

        setApplicants(currentProject.applicants || []);

        if (currentProject.author === myName) setIsLeader(true);
        
        setLoading(false);
      } catch (error) {
        console.error("데이터 로드 실패:", error);
        setLoading(false);
      }
    };
    loadData();
  }, [id, navigate, currentMyName]);

  const handleAccept = (app) => {
    if (window.confirm(`${app.name} 님을 팀원으로 승인하시겠습니까?`)) {
      setMembers([...members, { ...app, status: "fixed" }]);
      setApplicants(applicants.filter(a => a.id !== app.id));
    }
  };

  const handleReject = (app) => {
    if (window.confirm(`${app.name} 님의 신청을 거절하시겠습니까?`)) {
      setApplicants(applicants.filter(a => a.id !== app.id));
    }
  };

  const handleStatusChange = () => {
    if (project.status === 'recruiting') {
      if (window.confirm("모집을 마감하고 프로젝트를 시작하시겠습니까?")) {
        setProject({ ...project, status: 'ing' });
        alert("프로젝트가 시작되었습니다! 이제 팀원 제외가 불가합니다.");
      }
    } else if (project.status === 'ing') {
      if (window.confirm("프로젝트를 종료하시겠습니까? 종료 후 리뷰를 남길 수 있습니다.")) {
        setProject({ ...project, status: 'complete' });
      }
    }
  };

  const saveReviews = () => {
    alert("소중한 피드백이 저장되었습니다!");
    setIsReviewed(true);
    setShowReviewModal(false);
  };

  if (loading) return <div className="manage-container">로딩 중...</div>;
  if (!project) return null;

  return (
    <div className="manage-container">
      <button className="back-btn" onClick={() => navigate('/mypage')}>← 마이페이지로</button>

      <div className="manage-header">
        <div className="title-row">
          <h2>{project.title}</h2>
          <span className={`status-badge ${project.status}`}>
            {project.status === 'ing' ? '진행 중' : project.status === 'complete' ? '종료됨' : '모집 중'}
          </span>
        </div>
        <p className="manage-info">작성자: {project.author} | 카테고리: {project.category}</p>
      </div>

      {project.status === 'recruiting' && isLeader && (
        <div className="manage-section">
          <h4>📩 새로운 신청자 ({applicants.length})</h4>
          {applicants.length > 0 ? (
            <div className="applicant-list">
              {applicants.map(app => (
                <div key={app.id} className="applicant-card">
                  <div className="app-info">
                    <span className="app-name">{app.name}</span>
                    <span className="app-role">{app.role}</span>
                  </div>
                  <div className="app-actions">
                    <button className="btn-accept" onClick={() => handleAccept(app)}>승인</button>
                    <button className="btn-reject" onClick={() => handleReject(app)}>거절</button>
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="empty-msg">현재 대기 중인 신청자가 없습니다.</p>}
        </div>
      )}

      <div className="manage-section">
        <h4>👥 현재 팀원 ({members.length}명)</h4>
        <div className="member-list">
          {members.map(m => (
            <div key={m.id} className="member-item">
              <div className="member-info">
                <span className="m-name">{m.name} {m.name === currentMyName && "(나)"}</span>
                <span className="m-role">{m.role}</span>
              </div>
              {isLeader && m.name !== currentMyName && project.status === 'recruiting' && (
                <button 
                  className="btn-remove" 
                  onClick={() => setMembers(members.filter(mem => mem.id !== m.id))}
                >제외</button>
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="leader-footer">
        {isLeader && project.status !== 'complete' && (
          <button className="complete-project-btn" onClick={handleStatusChange}>
            {project.status === 'recruiting' ? "모집 마감 및 시작하기" : "프로젝트 종료하기"}
          </button>
        )}

        {project.status === 'complete' && (
          <button 
            className={isReviewed ? "review-view-btn" : "review-open-btn"} 
            onClick={() => setShowReviewModal(true)}
          >
            {isReviewed ? "📋 내가 남긴 리뷰 보기" : "🎉 팀원 리뷰 남기기"}
          </button>
        )}
      </div>

      {showReviewModal && (
        <div className="modal-overlay">
          <div className="review-modal multi-review">
            <h3>{isReviewed ? "📋 작성된 피드백" : "🎉 팀원 피드백"}</h3>
            <p className="modal-desc">함께한 팀원들에게 점수와 한 줄 평을 남겨주세요.</p>
            
            <div className="review-scroll-list">
              {members.filter(m => m.name !== currentMyName).map(m => (
                <div key={m.id} className="member-review-item">
                  <div className="member-label-row">
                    <strong>{m.name}</strong>
                    <div className="star-rating">
                      {[1, 2, 3, 4, 5].map(star => (
                        <span 
                          key={star} 
                          className={`star ${memberRatings[m.id] >= star ? 'active' : ''}`}
                          onClick={() => !isReviewed && setMemberRatings({...memberRatings, [m.id]: star})}
                        >★</span>
                      ))}
                    </div>
                  </div>
                  <textarea 
                    placeholder="팀원과의 협업 경험을 적어주세요."
                    value={memberReviews[m.id]}
                    onChange={(e) => setMemberReviews({...memberReviews, [m.id]: e.target.value})}
                    readOnly={isReviewed}
                    className={isReviewed ? "read-only-text" : ""}
                  />
                </div>
              ))}
            </div>

            <div className="modal-actions">
              {!isReviewed && <button className="save-btn" onClick={saveReviews}>리뷰 저장</button>}
              <button className="close-btn" onClick={() => setShowReviewModal(false)}>닫기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManagePage;