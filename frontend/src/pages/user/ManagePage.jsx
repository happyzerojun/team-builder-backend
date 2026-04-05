import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectService } from "../../services/projectService";
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
    const [memberReviews, setMemberReviews] = useState({});
    const [memberRatings, setMemberRatings] = useState({});

    useEffect(() => {
        const loadData = async () => {
            try {
                setLoading(true);
                const currentProject = await projectService.getProjectById(id);

                if (!currentProject) {
                    alert("프로젝트를 찾을 수 없습니다.");
                    navigate("/mypage");
                    return;
                }

                const savedUser = JSON.parse(localStorage.getItem("user"));
                const myName = savedUser?.nickname || savedUser?.name || "";
                setCurrentMyName(myName);
                setProject(currentProject);
                setApplicants(currentProject.applicants || []);

                const syncMembers = (currentProject.members || []).map(m =>
                    (m.role.includes("팀장") && String(currentProject.author) === String(myName)) ? { ...m, name: myName } : m
                );
                setMembers(syncMembers);

                const reviewData = currentProject.myReviewData;
                const hasReviews = reviewData &&
                    reviewData.reviews &&
                    Object.keys(reviewData.reviews).length > 0;

                if (hasReviews) {
                    setMemberReviews(reviewData.reviews);
                    setMemberRatings(reviewData.ratings || {});
                    setIsReviewed(true);
                } else {
                    setIsReviewed(false);
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

                if (String(currentProject.author) === String(myName)) setIsLeader(true);
            } catch (error) {
                console.error("데이터 로드 실패:", error);
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, [id, navigate, currentMyName]); 

    const updateProjectOnServer = async (updatedFields) => {
        try {
            const updatedProject = { ...project, ...updatedFields };
            await projectService.updateProject(id, updatedProject);
            setProject(updatedProject);
            return true;
        } catch (error) {
            console.error("서버 업데이트 실패:", error);
            alert("변경사항 저장에 실패했습니다.");
            return false;
        }
    };

    const handleAccept = async (app) => {
        if (window.confirm(`${app.name} 님을 팀원으로 승인하시겠습니까?`)) {
            const updatedMembers = [...members, { ...app, status: "fixed" }];
            const updatedApplicants = applicants.filter(a => a.id !== app.id);

            const success = await updateProjectOnServer({
                members: updatedMembers,
                applicants: updatedApplicants
            });

            if (success) {
                setMembers(updatedMembers);
                setApplicants(updatedApplicants);
            }
        }
    };

    const handleReject = async (app) => {
        if (window.confirm(`${app.name} 님의 신청을 거절하시겠습니까?`)) {
            const updatedApplicants = applicants.filter(a => a.id !== app.id);
            const success = await updateProjectOnServer({ applicants: updatedApplicants });
            if (success) setApplicants(updatedApplicants);
        }
    };

    const handleStatusChange = async () => {
        let nextStatus = project.status === 'recruiting' ? 'ing' : 'complete';
        const msg = nextStatus === 'ing' ? "모집을 마감하고 시작하시겠습니까?" : "프로젝트 종료 시 팀원 리뷰가 가능합니다. 종료하시겠습니까?";

        if (window.confirm(msg)) {
            const success = await updateProjectOnServer({ status: nextStatus });
            if (success) {
                alert(nextStatus === 'ing' ? "프로젝트가 시작되었습니다!" : "종료되었습니다. 리뷰를 남겨주세요.");
            }
        }
    };

    const saveReviews = async () => {
        const reviewValues = Object.values(memberReviews);
        if (reviewValues.every(v => v.trim() === "")) {
            if (!window.confirm("내용을 한 글자도 적지 않으셨습니다. 그래도 저장할까요?")) 
                return;
        }

        const reviewData = { reviews: memberReviews, ratings: memberRatings };
        const success = await updateProjectOnServer({ myReviewData: reviewData });

        if (success) {
            setIsReviewed(true);
            setShowReviewModal(false);
            alert("소중한 피드백이 저장되었습니다!");
        }
    };

    const handleRemoveMember = async (memberId) => {
        if (window.confirm("해당 팀원을 프로젝트에서 제외하시겠습니까?")) {
            const nextMembers = members.filter(mem => mem.id !== memberId);
            const success = await updateProjectOnServer({ members: nextMembers });
            if (success) setMembers(nextMembers);
        }
    };

    if (loading) return <div className="manage-container">로딩 중...</div>;
    if (!project) return null;

    return (
        <div className="manage-container">
            <button className="back-btn" onClick={() => navigate('/mypage')}>← 마이페이지로</button>

            <div className="manage-header">
                <div className="title-row">
                    <div className="title-left"> 
                        <h2>{project.title}</h2>
                        <span className={`status-badge ${project.status}`}>
                            {project.status === 'ing' ? '진행 중' : project.status === 'complete' ? '종료됨' : '모집 중'}
                        </span>
                    </div>

                    {isLeader && project.status === 'recruiting' && (
                        <button 
                            className="edit-project-btn" 
                            onClick={() => navigate(`/write/${project.id}`)}
                        >
                            ✏️ 프로젝트 수정
                        </button>
                    )}
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
                                <button className="btn-remove" onClick={() => handleRemoveMember(m.id)}>제외</button>
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
                    <button className={isReviewed ? "review-view-btn" : "review-open-btn"} onClick={() => setShowReviewModal(true)}>
                        {isReviewed ? "📋 내가 남긴 리뷰 보기" : "🎉 팀원 리뷰 남기기"}
                    </button>
                )}
            </div>

            {showReviewModal && (
                <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowReviewModal(false); }}>
                    <div className="review-modal multi-review">
                        <h3>{isReviewed ? "📋 작성된 피드백" : "🎉 팀원 피드백"}</h3>
                        <p className="modal-desc">{isReviewed ? "팀원들에게 남긴 소중한 평가입니다." : "함께한 팀원들에게 점수와 후기를 남겨주세요!"}</p>
                        <div className="review-scroll-list">
                            {members.filter(m => m.name !== currentMyName).map(m => (
                                <div key={m.id} className="member-review-item">
                                    <div className="member-label-row">
                                        <strong>{m.name}</strong>
                                        <div className="star-rating">
                                            {[1, 2, 3, 4, 5].map(star => (
                                                <span key={star} className={`star ${memberRatings[m.id] >= star ? 'active' : ''}`}
                                                    onClick={() => !isReviewed && setMemberRatings({ ...memberRatings, [m.id]: star })}>★</span>
                                            ))}
                                        </div>
                                    </div>
                                    <textarea
                                        placeholder={isReviewed ? "" : "팀원에게 남길 말을 적어주세요..."}
                                        value={memberReviews[m.id] || ""}
                                        readOnly={isReviewed}
                                        onChange={(e) => setMemberReviews({ ...memberReviews, [m.id]: e.target.value })}
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