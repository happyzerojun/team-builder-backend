import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectService } from "../../services/projectService";
import { applicationService } from "../../services/applicationService";
import { reviewService } from "../../services/reviewService";
import './ManagePage.css';

const ManagePage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [project, setProject] = useState(null);
    const [isLeader, setIsLeader] = useState(false);
    const [loading, setLoading] = useState(true);
    const [currentMyName, setCurrentMyName] = useState("");
    const [currentUserId, setCurrentUserId] = useState(null);

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

                const savedUser = JSON.parse(localStorage.getItem("user") || "{}");
                const myName = savedUser?.nickname || savedUser?.name || "";
                const myUserId = savedUser?.user_id;

                setCurrentMyName(myName);
                setCurrentUserId(myUserId);

                const currentProject = await projectService.getProjectById(id);

                if (!currentProject) {
                    alert("프로젝트를 찾을 수 없습니다.");
                    navigate("/mypage");
                    return;
                }

                setProject(currentProject);

                const leaderCheck =
                    String(currentProject.leader_id) === String(myUserId);
                setIsLeader(leaderCheck);

                const memberData = await projectService.getProjectMembers(id);
                const normalizedMembers = Array.isArray(memberData) ? memberData : [];
                setMembers(normalizedMembers);

                const applicationData = await applicationService.getProjectApplications(id);
                const pendingApplicants = applicationData.filter(
                    (app) => app.status === "pending"
                );
                setApplicants(pendingApplicants);

                const reviewData = await reviewService.getProjectMyReviews(id);

                if (reviewData.length > 0) {
                    const reviewsObj = {};
                    const ratingsObj = {};

                    reviewData.forEach((review) => {
                        const key = review.reviewee_id;
                        reviewsObj[key] = review.comment || "";
                        ratingsObj[key] = review.rating || 5;
                    });

                    setMemberReviews(reviewsObj);
                    setMemberRatings(ratingsObj);
                    setIsReviewed(true);
                } else {
                    const initialReviews = {};
                    const initialRatings = {};

                    normalizedMembers.forEach((member) => {
                        if (String(member.user_id) !== String(myUserId)) {
                            initialReviews[member.user_id] = "";
                            initialRatings[member.user_id] = 5;
                        }
                    });

                    setMemberReviews(initialReviews);
                    setMemberRatings(initialRatings);
                    setIsReviewed(false);
                }

            } catch (error) {
                console.error("데이터 로드 실패:", error);
                alert("관리 페이지 데이터를 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, [id, navigate]);

    const refreshApplicants = async () => {
        try {
            const applicationData = await applicationService.getProjectApplications(id);
            const pendingApplicants = applicationData.filter(
                (app) => app.status === "pending"
            );
            setApplicants(pendingApplicants);
        } catch (error) {
            console.error("신청자 목록 새로고침 실패:", error);
        }
    };

    const refreshMembers = async () => {
        try {
            const memberData = await projectService.getProjectMembers(id);
            setMembers(Array.isArray(memberData) ? memberData : []);
        } catch (error) {
            console.error("멤버 목록 새로고침 실패:", error);
        }
    };

    const handleAccept = async (app) => {
        const applicantName =
            app.applicant_name || app.name || "지원자";

        if (!window.confirm(`${applicantName} 님을 팀원으로 승인하시겠습니까?`)) {
            return;
        }

        try {
            await applicationService.acceptApplication(app.application_id);
            await refreshApplicants();
            await refreshMembers();
            alert("신청자를 승인했습니다.");
        } catch (error) {
            console.error("승인 실패:", error);
            alert("승인 처리에 실패했습니다.");
        }
    };

    const handleReject = async (app) => {
        const applicantName =
            app.applicant_name || app.name || "지원자";

        if (!window.confirm(`${applicantName} 님의 신청을 거절하시겠습니까?`)) {
            return;
        }

        try {
            await applicationService.rejectApplication(app.application_id);
            await refreshApplicants();
            alert("신청을 거절했습니다.");
        } catch (error) {
            console.error("거절 실패:", error);
            alert("거절 처리에 실패했습니다.");
        }
    };

    const handleStatusChange = async () => {
        if (!project) return;

        let nextStatus = "";
        let msg = "";

        if (project.status === "모집중") {
            nextStatus = "진행중";
            msg = "모집을 마감하고 시작하시겠습니까?";
        } else if (project.status === "진행중") {
            nextStatus = "종료됨";
            msg = "프로젝트 종료 시 팀원 리뷰가 가능합니다. 종료하시겠습니까?";
        } else {
            return;
        }

        if (!window.confirm(msg)) return;

        try {
            const updated = await projectService.updateProjectStatus(id, nextStatus);

            setProject((prev) => ({
                ...prev,
                ...(updated || {}),
                status: updated?.status || nextStatus
            }));

            alert(nextStatus === "진행중" ? "프로젝트가 시작되었습니다!" : "종료되었습니다. 리뷰를 남겨주세요.");
        } catch (error) {
            console.error("상태 변경 실패:", error);
            alert("상태 변경에 실패했습니다.");
        }
    };

    const saveReviews = async () => {
        const reviewValues = Object.values(memberReviews);
        if (reviewValues.every((v) => String(v).trim() === "")) {
            if (!window.confirm("내용을 한 글자도 적지 않으셨습니다. 그래도 저장할까요?")) {
                return;
            }
        }

        try {
            const payload = members
                .filter((m) => String(m.user_id) !== String(currentUserId))
                .map((m) => ({
                    reviewee_id: m.user_id,
                    rating: memberRatings[m.user_id] || 5,
                    comment: memberReviews[m.user_id] || ""
                }));

            await reviewService.saveProjectReviews(id, payload);
            setIsReviewed(true);
            setShowReviewModal(false);
            alert("소중한 피드백이 저장되었습니다!");
        } catch (error) {
            console.error("리뷰 저장 실패:", error);
            alert("리뷰 저장에 실패했습니다.");
        }
    };

    const handleRemoveMember = async (memberId) => {
        if (!window.confirm("해당 팀원을 프로젝트에서 제외하시겠습니까?")) {
            return;
        }

        try {
            await projectService.removeProjectMember(id, memberId);
            await refreshMembers();
            alert("팀원이 제외되었습니다.");
        } catch (error) {
            console.error("팀원 제외 실패:", error);
            alert("팀원 제외에 실패했습니다.");
        }
    };

    if (loading) return <div className="manage-container">로딩 중...</div>;
    if (!project) return null;

    return (
        <div className="manage-container">
            <button className="back-btn" onClick={() => navigate('/mypage')}>
                ← 마이페이지로
            </button>

            <div className="manage-header">
                <div className="title-row">
                    <div className="title-left">
                        <h2>{project.title}</h2>
                        <span className={`status-badge ${project.status}`}>
                            {project.status === '진행중'
                                ? '진행 중'
                                : project.status === '종료됨'
                                ? '종료됨'
                                : '모집 중'}
                        </span>
                    </div>

                    {isLeader && project.status === '모집중' && (
                        <button
                            className="edit-project-btn"
                            onClick={() => navigate(`/write/${project.project_id || id}`)}
                        >
                            ✏️ 프로젝트 수정
                        </button>
                    )}
                </div>

                <p className="manage-info">
                    작성자 ID: {project.leader_id} | 지역: {project.region || "미정"}
                </p>
            </div>

            {project.status === '모집중' && isLeader && (
                <div className="manage-section">
                    <h4>📩 새로운 신청자 ({applicants.length})</h4>
                    {applicants.length > 0 ? (
                        <div className="applicant-list">
                            {applicants.map((app) => (
                                <div key={app.application_id} className="applicant-card">
                                    <div className="app-info">
                                        <span className="app-name">
                                            {app.applicant_name || app.name || "이름 없음"}
                                        </span>
                                        <span className="app-role">
                                            {app.support_role || app.role || "지원자"}
                                        </span>
                                    </div>
                                    <div className="app-actions">
                                        <button className="btn-accept" onClick={() => handleAccept(app)}>
                                            승인
                                        </button>
                                        <button className="btn-reject" onClick={() => handleReject(app)}>
                                            거절
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="empty-msg">현재 대기 중인 신청자가 없습니다.</p>
                    )}
                </div>
            )}

            <div className="manage-section">
                <h4>👥 현재 팀원 ({members.length}명)</h4>
                <div className="member-list">
                    {members.map((m) => (
                        <div key={m.user_id || m.member_id} className="member-item">
                            <div className="member-info">
                                <span className="m-name">
                                    {m.name || m.member_name || "이름 없음"}{" "}
                                    {String(m.user_id) === String(currentUserId) && "(나)"}
                                </span>
                                <span className="m-role">
                                    {m.role || m.member_role || "팀원"}
                                </span>
                            </div>

                            {isLeader &&
                                String(m.user_id) !== String(currentUserId) &&
                                project.status === '모집중' && (
                                    <button
                                        className="btn-remove"
                                        onClick={() => handleRemoveMember(m.user_id || m.member_id)}
                                    >
                                        제외
                                    </button>
                                )}
                        </div>
                    ))}
                </div>
            </div>

            <div className="leader-footer">
                {isLeader && project.status !== '종료됨' && (
                    <button className="complete-project-btn" onClick={handleStatusChange}>
                        {project.status === '모집중'
                            ? "모집 마감 및 시작하기"
                            : "프로젝트 종료하기"}
                    </button>
                )}

                {project.status === '종료됨' && (
                    <button
                        className={isReviewed ? "review-view-btn" : "review-open-btn"}
                        onClick={() => setShowReviewModal(true)}
                    >
                        {isReviewed ? "📋 내가 남긴 리뷰 보기" : "🎉 팀원 리뷰 남기기"}
                    </button>
                )}
            </div>

            {showReviewModal && (
                <div
                    className="modal-overlay"
                    onClick={(e) => {
                        if (e.target === e.currentTarget) setShowReviewModal(false);
                    }}
                >
                    <div className="review-modal multi-review">
                        <h3>{isReviewed ? "📋 작성된 피드백" : "🎉 팀원 피드백"}</h3>
                        <p className="modal-desc">
                            {isReviewed
                                ? "팀원들에게 남긴 소중한 평가입니다."
                                : "함께한 팀원들에게 점수와 후기를 남겨주세요!"}
                        </p>

                        <div className="review-scroll-list">
                            {members
                                .filter((m) => String(m.user_id) !== String(currentUserId))
                                .map((m) => {
                                    const reviewKey = m.user_id;
                                    return (
                                        <div key={reviewKey} className="member-review-item">
                                            <div className="member-label-row">
                                                <strong>{m.name || m.member_name || "이름 없음"}</strong>
                                                <div className="star-rating">
                                                    {[1, 2, 3, 4, 5].map((star) => (
                                                        <span
                                                            key={star}
                                                            className={`star ${(memberRatings[reviewKey] || 0) >= star ? 'active' : ''}`}
                                                            onClick={() =>
                                                                !isReviewed &&
                                                                setMemberRatings({
                                                                    ...memberRatings,
                                                                    [reviewKey]: star
                                                                })
                                                            }
                                                        >
                                                            ★
                                                        </span>
                                                    ))}
                                                </div>
                                            </div>

                                            <textarea
                                                placeholder={isReviewed ? "" : "팀원에게 남길 말을 적어주세요..."}
                                                value={memberReviews[reviewKey] || ""}
                                                readOnly={isReviewed}
                                                onChange={(e) =>
                                                    setMemberReviews({
                                                        ...memberReviews,
                                                        [reviewKey]: e.target.value
                                                    })
                                                }
                                            />
                                        </div>
                                    );
                                })}
                        </div>

                        <div className="modal-actions">
                            {!isReviewed && (
                                <button className="save-btn" onClick={saveReviews}>
                                    리뷰 저장
                                </button>
                            )}
                            <button className="close-btn" onClick={() => setShowReviewModal(false)}>
                                닫기
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ManagePage;