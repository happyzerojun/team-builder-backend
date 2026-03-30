import React, { useState, useEffect } from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom';
import { projectService } from "../../services/projectService";
import { getAppliedIds } from "../../services/applyService";

const MyPage = () => {
    const navigate = useNavigate();

    const [user, setUser] = useState({
        name: "",
        organization: "",
        introduction: "",
        tags: [],
        job_role: "",
        profileImg: null
    });

    const [allProjects, setAllProjects] = useState([]);
    const [appliedPosts, setAppliedPosts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true);
            try {
                const savedUser = JSON.parse(localStorage.getItem("user"));
                if (!savedUser) {
                    alert("로그인이 필요한 페이지입니다.");
                    navigate("/login");
                    return;
                }
                const currentName = savedUser.nickname || savedUser.name || "강무원";
                setUser({
                    ...savedUser,
                    name: currentName,
                    profileImg: savedUser.profileImg || null
                });

                const data = await projectService.getAllProjects();
                const projects = Array.isArray(data) ? data : [];
                setAllProjects(projects);

                const ids = getAppliedIds().map(id => String(id)); 

                const filteredApplied = projects.filter(post => {

                    const myName = String(user.name || "").trim();

                    const rawAuthor = post.author || post.leader || "";
                    const authorName = (typeof rawAuthor === 'object' ? rawAuthor.name : String(rawAuthor)).trim();

                    const isMine = authorName === myName;

                    const applicants = post.applicants || [];
                    const isInsideApplicants = applicants.some(app => {
                        const appName = (typeof app === 'object' ? app.name : String(app)).trim();
                        return appName === myName;
                    });

                    const ids = getAppliedIds().map(id => String(id));
                    const isAppliedId = ids.includes(String(post.id));

                    return !isMine && (isInsideApplicants || isAppliedId);
                });

                setAppliedPosts(filteredApplied);

            } catch (error) {
                console.error("마이페이지 로딩 실패:", error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [navigate]);

    const myLeadProjects = allProjects.filter(post => String(post.author) === String(user.name));
    const recruitingProjects = myLeadProjects.filter(post => post.status === "recruiting");
    const ongoingLeadProjects = myLeadProjects.filter(post => post.status === "ing" || post.status === "complete");

    const myParticipation = allProjects.filter(post => {
        const members = post.members || [];

        const isMember = members.some(m => 
            (typeof m === 'string' ? m === user.name : m.name === user.name)
        );

        const isNotAuthor = String(post.author) !== String(user.name);
        
        return isMember && isNotAuthor;
    });

    if (isLoading) return <div className="mp-loading">데이터를 불러오는 중입니다...</div>;

    return (
        <div className="mp-container">
            <div className="mp-card">
                <button className="mp-back-btn" onClick={() => navigate('/')}>← 메인으로</button>

                {/* 프로필 헤더 */}
                <div className="mp-header">
                    {user.profileImg ? (
                        <img src={user.profileImg} alt="프로필" className="mp-avatar-img" />
                    ) : (
                        <div className="mp-avatar">{user.name ? user.name[0] : "무"}</div>
                    )}

                    <div className="mp-info">
                        <div className="mp-info-top">
                            <h3>{user.name} 님</h3>
                            <div className="mp-btn-group">
                                <button className="mp-review-view-btn" onClick={() => navigate('/reviews')}>⭐ 내 리뷰 보기</button>
                                <button className="mp-setting-btn" onClick={() => navigate('/MyPageSetting')}>프로필 수정</button>
                            </div>
                        </div>
                        <div className="mp-org-row">
                            <span className="mp-org-label">소속</span>
                            <span className="mp-org-value">{user.organization || "소속 없음"}</span>
                        </div>
                        <div className="mp-tags">
                            {(user.tags || []).map((tag, idx) => (
                                <span key={idx} className="mp-tag">#{tag}</span>
                            ))}
                        </div>
                        <p className="mp-bio">{user.introduction || "자기소개가 없습니다."}</p>
                    </div>
                </div>

                <hr className="mp-divider" />

                {/* 1. 내가 만든 프로젝트 섹션 */}
                <div className="mp-section">
                    <h4>📂 내가 만든 프로젝트</h4>

                    {/* 모집 중 (신청자 관리) */}
                    {recruitingProjects.map(proj => (
                        <div key={proj.id} className="mp-activity-card clickable recruiting" onClick={() => navigate(`/manage/${proj.id}`)}>
                            <div className="proj-info">
                                <span className="title">{proj.title}</span>
                                <span className="sub-info">📩 신규 신청자 확인 및 승인</span>
                            </div>
                            <span className="mp-badge status-recruiting">신청자 관리</span>
                        </div>
                    ))}

                    {/* 진행 중/종료 (진행 관리) */}
                    {ongoingLeadProjects.map(proj => (
                        <div key={proj.id} className="mp-activity-card clickable ongoing" onClick={() => navigate(`/manage/${proj.id}`)}>
                            <div className="proj-info">
                                <span className="title">{proj.title}</span>
                                <span className="sub-info">
                                    {proj.status === 'complete' ? '✅ 프로젝트 종료됨' : '👥 팀원 관리 및 협업 중'}
                                </span>
                            </div>
                            <span className={`mp-badge ${proj.status === 'complete' ? 'status-complete' : 'status-ongoing'}`}>
                                {proj.status === 'complete' ? '종료됨' : '진행 관리'}
                            </span>
                        </div>
                    ))}

                    {myLeadProjects.length === 0 && (
                        <div className="mp-activity-card no-data">작성한 프로젝트가 없습니다.</div>
                    )}
                </div>

                {/* 2. 참여 중인 프로젝트 섹션 */}
                <div className="mp-section" style={{ marginTop: '30px' }}>
                    <h4>🤝 참여 중인 프로젝트</h4>
                    {myParticipation.length > 0 ? (
                        myParticipation.map(proj => (
                            <div key={proj.id} className="mp-activity-card clickable" onClick={() => navigate(`/manage/${proj.id}`)}>
                                <div className="proj-info">
                                    <span className="title">{proj.title}</span>
                                    <span className="sub-info">🛠 팀원으로 협업 중</span>
                                </div>
                                <div className="mp-card-actions">
                                    <span className="mp-badge status-ongoing">진행중</span>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="mp-activity-card no-data">참여 중인 프로젝트가 없습니다.</div>
                    )}
                </div>

                <hr className="mp-divider" />

                {/* 3. 지원 중인 모집글 섹션 */}
                <div className="mp-section">
                    <h4>📩 지원 중인 모집글</h4>
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

                <div className="mp-footer-tags">
                    <span className="mp-tag-badge blue">⚡ {user.job_role || "개발자"}</span>
                    <span className="mp-tag-badge green">🤝 협업 가능</span>
                </div>
            </div>
        </div>
    );
};

export default MyPage;