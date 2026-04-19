import React, { useState, useEffect } from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom';
import { projectService } from "../../services/projectService";
import { applicationService } from "../../services/applicationService";

const MyPage = () => {
    const navigate = useNavigate();

    const [user, setUser] = useState({
        name: "",
        email: "",
        organization: "",
        introduction: "",
        tags: [],
        job_role: "",
        profileImg: null
    });

    const [allProjects, setAllProjects] = useState([]);
    const [myLead, setMyLead] = useState([]);
    const [myPart, setMyPart] = useState([]);
    const [appliedPosts, setAppliedPosts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const savedUser = JSON.parse(localStorage.getItem("user") || "{}");
        const token = localStorage.getItem("token");

        if (!token || !savedUser || (!savedUser.email && !savedUser.name)) {
            navigate("/login");
            return;
        }

        // user_id가 없으면 마이페이지 기본 정보만 보여주고 API 호출은 막음
        if (!savedUser.user_id) {
            setUser({
                name: savedUser.name || "",
                email: savedUser.email || "",
                organization: savedUser.organization || "",
                introduction: savedUser.introduction || "",
                tags: savedUser.tags || [],
                job_role: savedUser.job_role || "",
                profileImg: savedUser.profileImg || null
            });
            setMyLead([]);
            setMyPart([]);
            setAppliedPosts([]);
            setIsLoading(false);
            return;
        }

        const fetchData = async () => {
            setIsLoading(true);

            try {
                setUser({
                    name: savedUser.name || "",
                    email: savedUser.email || "",
                    organization: savedUser.organization || "",
                    introduction: savedUser.introduction || "",
                    tags: savedUser.tags || [],
                    job_role: savedUser.job_role || "",
                    profileImg: savedUser.profileImg || null
                });

                const data = await projectService.getAllProjects();
                const projects = Array.isArray(data) ? data : [];
                setAllProjects(projects);

                const myCreatedProjects = projects.filter(
                    (project) => String(project.leader_id) === String(savedUser.user_id)
                );
                setMyLead(myCreatedProjects);

                const myApplications = await applicationService.getMyApplications();

                const applied = myApplications
                    .filter((app) =>
                        app.status === "pending" ||
                        app.status === "accepted" ||
                        app.status === "rejected"
                    )
                    .map((app) => {
                        if (app.project) {
                            return {
                                ...app.project,
                                application_id: app.application_id,
                                application_status: app.status
                            };
                        }

                        const matchedProject = projects.find(
                            (project) => String(project.project_id) === String(app.project_id)
                        );

                        if (!matchedProject) return null;

                        return {
                            ...matchedProject,
                            application_id: app.application_id,
                            application_status: app.status
                        };
                    })
                    .filter(Boolean);

                setAppliedPosts(applied);

                const participating = myApplications
                    .filter((app) => app.status === "accepted")
                    .map((app) => {
                        if (app.project) {
                            return app.project;
                        }

                        return projects.find(
                            (project) => String(project.project_id) === String(app.project_id)
                        );
                    })
                    .filter(Boolean)
                    .filter(
                        (project) => String(project.leader_id) !== String(savedUser.user_id)
                    );

                setMyPart(participating);

            } catch (error) {
                console.error("로딩 실패:", error);
                alert("마이페이지 정보를 불러오는데 실패했습니다.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [navigate]);

    if (isLoading) {
        return <div className="mp-loading">데이터를 불러오는 중...</div>;
    }

    return (
        <div className="mp-container">
            <div className="mp-card">

                <div className="mp-header-section">
                    <button className="mp-back-btn" onClick={() => navigate('/')}>
                        ← 메인으로
                    </button>

                    <div className="mp-header">
                        <div className="mp-avatar-wrapper">
                            {user.profileImg ? (
                                <img src={user.profileImg} alt="프로필" className="mp-avatar-img" />
                            ) : (
                                <div className="mp-avatar">{(user.name || "유")[0]}</div>
                            )}
                        </div>

                        <div className="mp-info">
                            <div className="mp-info-top">
                                <div className="mp-name-group">
                                    <h3>{user.name} 님</h3>
                                    <button
                                        className="mp-icon-btn"
                                        onClick={() => navigate('/MyPageSetting')}
                                    >
                                        ⚙️
                                    </button>
                                </div>

                                <button
                                    className="mp-review-btn"
                                    onClick={() => navigate('/reviews')}
                                >
                                    ⭐ 내 리뷰 보기
                                </button>
                            </div>

                            <div className="mp-inline-details">
                                <span className="mp-detail-item">
                                    <span className="mp-label">이메일</span>
                                    <span className="mp-value">{user.email || "정보 없음"}</span>
                                </span>
                                <span className="mp-inline-divider">|</span>
                                <span className="mp-detail-item">
                                    <span className="mp-label">희망 직무</span>
                                    <span className="mp-value">{user.job_role || "미입력"}</span>
                                </span>
                            </div>

                            <p className="mp-bio">{user.introduction || "자기소개가 없습니다."}</p>

                            <div className="mp-tags-minimal">
                                {(user.tags || []).map((tag, idx) => (
                                    <span key={idx} className="mp-tag-text">#{tag}</span>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="mp-content-section">
                    <div className="mp-section">
                        <h4>📂 내가 만든 프로젝트</h4>
                        {myLead.length > 0 ? (
                            myLead.map((proj) => (
                                <div
                                    key={proj.project_id}
                                    className="mp-activity-card"
                                    onClick={() => navigate(`/manage/${proj.project_id}`)}
                                >
                                    <div className="proj-info">
                                        <span className="title">{proj.title}</span>
                                        <span className="sub-info">{proj.region || "지역 미정"}</span>
                                    </div>
                                    <span className={`mp-badge ${
                                        proj.status === '모집중'
                                            ? 'status-recruiting'
                                            : proj.status === '종료됨'
                                            ? 'status-complete'
                                            : 'status-ongoing'
                                    }`}>
                                        {proj.status || "상태 미정"}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <div className="no-data">생성한 프로젝트가 없습니다.</div>
                        )}
                    </div>

                    <div className="mp-section">
                        <h4>🤝 참여 중인 프로젝트</h4>
                        {myPart.length > 0 ? (
                            myPart.map((proj) => (
                                <div
                                    key={proj.project_id}
                                    className="mp-activity-card"
                                    onClick={() => navigate(`/manage/${proj.project_id}`)}
                                >
                                    <div className="proj-info">
                                        <span className="title">{proj.title}</span>
                                        <span className="sub-info">{proj.region || "지역 미정"}</span>
                                    </div>
                                    <span className={`mp-badge ${
                                        proj.status === '종료됨'
                                            ? 'status-complete'
                                            : 'status-ongoing'
                                    }`}>
                                        {proj.status === '종료됨' ? '종료됨' : '진행중'}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <div className="no-data">참여 중인 프로젝트가 없습니다.</div>
                        )}
                    </div>

                    <hr className="mp-divider-subtle" />

                    <div className="mp-section">
                        <h4>📩 지원 중인 모집글</h4>
                        {appliedPosts.length > 0 ? (
                            appliedPosts.map((proj) => (
                                <div
                                    key={proj.application_id || proj.project_id}
                                    className="mp-activity-card"
                                    onClick={() => navigate(`/post/${proj.project_id}`)}
                                >
                                    <div className="proj-info">
                                        <span className="title">{proj.title}</span>
                                        <span className="sub-info">{proj.region || "지역 미정"}</span>
                                    </div>
                                    <span className={`mp-badge ${
                                        proj.application_status === 'accepted'
                                            ? 'status-ongoing'
                                            : proj.application_status === 'rejected'
                                            ? 'status-complete'
                                            : 'status-pending'
                                    }`}>
                                        {proj.application_status === 'accepted'
                                            ? '승인됨'
                                            : proj.application_status === 'rejected'
                                            ? '거절됨'
                                            : '지원완료'}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <div className="no-data">지원 내역이 없습니다.</div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MyPage;