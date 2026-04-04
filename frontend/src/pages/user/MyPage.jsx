import React, { useState, useEffect } from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom';
import { projectService } from "../../services/projectService";
import { getAppliedIds } from "../../services/applyService";

const MyPage = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState({
        name: "", organization: "", introduction: "",
        tags: [], job_role: "", profileImg: null
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
                    navigate("/login");
                    return;
                }
                const currentName = savedUser.nickname || savedUser.name || "사용자";
                setUser({ ...savedUser, name: currentName });

                const data = await projectService.getAllProjects();
                const projects = Array.isArray(data) ? data : [];
                setAllProjects(projects);

                const appliedIds = getAppliedIds().map(id => String(id));
                const filteredApplied = projects.filter(post => {
                    const myName = String(currentName).trim();
                    const isMine = (post.author || post.leader) === myName;
                    const isInside = (post.applicants || []).some(app => 
                        (typeof app === 'object' ? app.name : String(app)).trim() === myName
                    );
                    return !isMine && (isInside || appliedIds.includes(String(post.id)));
                });
                setAppliedPosts(filteredApplied);
            } catch (error) {
                console.error("로딩 실패:", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchData();
    }, [navigate]);

    const myLead = allProjects.filter(p => String(p.author) === String(user.name));
    const myPart = allProjects.filter(p => 
        (p.members || []).some(m => (typeof m === 'string' ? m === user.name : m.name === user.name)) 
        && String(p.author) !== String(user.name)
    );

    if (isLoading) return <div className="mp-loading">데이터를 불러오는 중...</div>;

    return (
        <div className="mp-container">
            <div className="mp-card">
                
                <div className="mp-header-section">
                    <button className="mp-back-btn" onClick={() => navigate('/')}>← 메인으로</button>
                    <div className="mp-header">
                        <div className="mp-avatar-wrapper">
                            {user.profileImg ? (
                                <img src={user.profileImg} alt="프로필" className="mp-avatar-img" />
                            ) : (
                                <div className="mp-avatar">{user.name[0]}</div>
                            )}
                        </div>

                        <div className="mp-info">
                            <div className="mp-info-top">
                                {/* 이름과 설정을 하나의 그룹으로 묶음 */}
                                <div className="mp-name-group">
                                    <h3>{user.name} 님</h3>
                                    <button className="mp-icon-btn" onClick={() => navigate('/MyPageSetting')}>
                                        ⚙️
                                    </button>
                                </div>
                                {/* 리뷰 버튼은 독립적으로 배치 */}
                                <button className="mp-review-btn" onClick={() => navigate('/reviews')}>
                                    ⭐ 내 리뷰 보기
                                </button>
                            </div>

                            <div className="mp-inline-details">
                                <span className="mp-detail-item">
                                    <span className="mp-label">소속</span>
                                    <span className="mp-value">{user.organization || "소속 없음"}</span>
                                </span>
                                <span className="mp-inline-divider">|</span>
                                <span className="mp-detail-item">
                                    <span className="mp-label">희망 직무</span>
                                    <span className="mp-value">{user.job_role || "개발자"}</span>
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
                        {myLead.length > 0 ? myLead.map(proj => (
                            <div key={proj.id} className="mp-activity-card" onClick={() => navigate(`/manage/${proj.id}`)}>
                                <div className="proj-info">
                                    <span className="title">{proj.title}</span>
                                    <span className="sub-info">{proj.category}</span>
                                </div>
                                <span className={`mp-badge ${
                                    proj.status === 'recruiting' ? 'status-recruiting' : 
                                    proj.status === 'complete' ? 'status-complete' : 'status-ongoing'
                                }`}>
                                    {proj.status === 'recruiting' ? '모집중' : 
                                     proj.status === 'complete' ? '종료됨' : '진행중'}
                                </span>
                            </div>
                        )) : <div className="no-data">생성한 프로젝트가 없습니다.</div>}
                    </div>

                    <div className="mp-section">
                        <h4>🤝 참여 중인 프로젝트</h4>
                        {myPart.length > 0 ? myPart.map(proj => (
                            <div key={proj.id} className="mp-activity-card" onClick={() => navigate(`/manage/${proj.id}`)}>
                                <div className="proj-info">
                                    <span className="title">{proj.title}</span>
                                    <span className="sub-info">{proj.category}</span>
                                </div>
                                <span className={`mp-badge ${proj.status === 'complete' ? 'status-complete' : 'status-ongoing'}`}>
                                    {proj.status === 'complete' ? '종료됨' : '진행중'}
                                </span>
                            </div>
                        )) : <div className="no-data">참여 중인 프로젝트가 없습니다.</div>}
                    </div>

                    <hr className="mp-divider-subtle" />

                    <div className="mp-section">
                        <h4>📩 지원 중인 모집글</h4>
                        {appliedPosts.length > 0 ? appliedPosts.map(proj => (
                            <div key={proj.id} className="mp-activity-card" onClick={() => navigate(`/post/${proj.id}`)}>
                                <div className="proj-info">
                                    <span className="title">{proj.title}</span>
                                    <span className="sub-info">{proj.category}</span>
                                </div>
                                <span className="mp-badge status-pending">지원완료</span>
                            </div>
                        )) : <div className="no-data">지원 내역이 없습니다.</div>}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MyPage;