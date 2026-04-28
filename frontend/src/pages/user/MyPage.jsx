import React, { useState, useEffect } from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom'; // 페이지 강제 이동(리다이렉트)을 도와주는 도구
import { projectService } from "../../services/projectService"; // 백엔드의 Project 컨트롤러와 통신
import { applicationService } from "../../services/applicationService"; // 백엔드의 Application 컨트롤러와 통신

const MyPage = () => {
    // 스프링의 'return "redirect:/경로"' 와 같은 역할을 합니다.
    const navigate = useNavigate();

    // --- [상태(State) 저장소 영역] ---
    // 백엔드의 DTO 객체들이라고 생각하시면 됩니다. 값이 바뀌면 화면이 즉시 새로고침됩니다.

    // 1. 현재 로그인한 유저 정보를 담는 DTO
    const [user, setUser] = useState({
        name: "", email: "", organization: "",
        introduction: "", tags: [], job_role: "", profileImg: null
    });

    // 2. 프로젝트 리스트를 담아둘 List<ProjectDto> 역할
    const [allProjects, setAllProjects] = useState([]); // 전체 프로젝트
    const [myLead, setMyLead] = useState([]); // 내가 팀장인 프로젝트
    const [myPart, setMyPart] = useState([]); // 내가 팀원(참여 확정)인 프로젝트
    const [appliedPosts, setAppliedPosts] = useState([]); // 내가 지원한(대기/거절 포함) 프로젝트

    // 3. 로딩 상태 스위치 (데이터가 백엔드에서 오기 전까지 빙글빙글 로딩을 보여주기 위함)
    const [isLoading, setIsLoading] = useState(true);

    // --- [useEffect: 컴포넌트 초기화 영역] ---
    // 스프링의 @PostConstruct 처럼, 화면이 처음 열릴 때 딱 한 번 실행되는 세팅 함수입니다.
    useEffect(() => {
        // 브라우저의 로컬 저장소(세션/쿠키 같은 역할)에서 내 정보를 꺼내옵니다.
        const savedUser = JSON.parse(localStorage.getItem("user") || "{}");
        const token = localStorage.getItem("token");

        // 🚨 [보안 검증] 토큰이나 유저 정보가 없으면? -> 비로그인 상태이므로 로그인 페이지로 튕겨냅니다.
        if (!token || !savedUser || (!savedUser.email && !savedUser.name)) {
            navigate("/login");
            return;
        }

        // [예외 처리] 만약 로컬 저장소에 user_id(PK)가 없다면?
        // API 통신은 포기하고, 일단 로컬에 있는 이름/이메일만 껍데기로 보여줍니다.
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
            setMyLead([]); setMyPart([]); setAppliedPosts([]);
            setIsLoading(false); // 로딩 끝!
            return;
        }

        // 🚀 [본격적인 백엔드 통신 함수]
        const fetchData = async () => {
            setIsLoading(true); // 통신 시작 전 로딩 켬

            try {
                // 1. 유저 정보 세팅
                setUser({
                    name: savedUser.name || "",
                    email: savedUser.email || "",
                    organization: savedUser.organization || "",
                    introduction: savedUser.introduction || "",
                    tags: savedUser.tags || [],
                    job_role: savedUser.job_role || "",
                    profileImg: savedUser.profileImg || null
                });

                // 2. 전체 프로젝트 목록을 백엔드에서 가져옵니다. (SELECT * FROM project)
                const data = await projectService.getAllProjects();
                const projects = Array.isArray(data) ? data : [];
                setAllProjects(projects);

                // 3. [분류 작업 1: 내가 방장인 프로젝트]
                // 전체 프로젝트 중, 방장 ID(leader_id)가 내 ID(user_id)와 같은 것만 필터링합니다.
                const myCreatedProjects = projects.filter(
                    (project) => String(project.leader_id) === String(savedUser.user_id)
                );
                setMyLead(myCreatedProjects);

                // 4. 내가 찔러본(지원한) 내역들을 백엔드에서 가져옵니다.
                const myApplications = await applicationService.getMyApplications();

                // 5. [분류 작업 2: 지원 현황 (대기중/수락됨/거절됨)]
                const applied = myApplications
                    .filter((app) =>
                        app.status === "pending" ||
                        app.status === "accepted" ||
                        app.status === "rejected"
                    )
                    .map((app) => {
                        // 백엔드에서 조인(Join)해서 프로젝트 정보를 같이 줬다면 그대로 씁니다.
                        if (app.project) {
                            return {
                                ...app.project,
                                application_id: app.application_id,
                                application_status: app.status
                            };
                        }

                        // 조인해서 주지 않았다면, 아까 받아둔 전체 프로젝트 리스트에서 ID로 직접 찾아 끼워 맞춥니다.
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
                    .filter(Boolean); // null 값 찌꺼기들 제거

                setAppliedPosts(applied); // 지원 내역 저장소에 쏙!

                // 6. [분류 작업 3: 참여 확정된 프로젝트 (팀원)]
                const participating = myApplications
                    .filter((app) => app.status === "accepted") // 수락된 것만 고름
                    .map((app) => {
                        if (app.project) return app.project;
                        return projects.find((project) => String(project.project_id) === String(app.project_id));
                    })
                    .filter(Boolean)
                    // 🚨 중요: 수락된 것들 중에서도 '내가 방장인 프로젝트'는 제외합니다!
                    // (그래야 '참여 중'과 '내가 만든' 리스트가 중복되지 않음)
                    .filter(
                        (project) => String(project.leader_id) !== String(savedUser.user_id)
                    );

                setMyPart(participating); // 참여 확정 저장소에 쏙!

            } catch (error) {
                console.error("로딩 실패:", error);
                alert("마이페이지 정보를 불러오는데 실패했습니다.");
            } finally {
                // 통신이 성공하든 실패하든, 마지막엔 무조건 로딩 화면을 꺼줍니다.
                setIsLoading(false);
            }
        };

        fetchData(); // 위에서 정의한 통신 함수를 여기서 실제로 실행!
    }, [navigate]);

    // --- [UI 렌더링 영역 (HTML/JSX)] ---

    // 백엔드 통신 중일 때는 이 화면만 보여줍니다. (NullPointerException 방어용)
    if (isLoading) {
        return <div className="mp-loading">데이터를 불러오는 중...</div>;
    }

    // 데이터가 다 모이면 아래 화면을 그립니다.
    return (
        <div className="mp-container">
            <div className="mp-card">

                {/* --- [상단: 유저 프로필 영역] --- */}
                <div className="mp-header-section">
                    <button className="mp-back-btn" onClick={() => navigate('/')}>
                        ← 메인으로
                    </button>

                    <div className="mp-header">
                        <div className="mp-avatar-wrapper">
                            {/* 이미지가 있으면 뿌려주고, 없으면 이름 첫 글자로 프사 대체 */}
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
                                    {/* 톱니바퀴 누르면 프로필 수정 페이지(세팅)로 이동 */}
                                    <button className="mp-icon-btn" onClick={() => navigate('/MyPageSetting')}>⚙️</button>
                                </div>
                                <button className="mp-review-btn" onClick={() => navigate('/reviews')}>
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
                                {/* 자바의 forEach문처럼, 태그 배열을 돌면서 하나씩 배지로 만들어줍니다. */}
                                {(user.tags || []).map((tag, idx) => (
                                    <span key={idx} className="mp-tag-text">#{tag}</span>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                {/* --- [하단: 프로젝트 내역 영역] --- */}
                <div className="mp-content-section">

                    {/* 1. 내가 만든 프로젝트 리스트 */}
                    <div className="mp-section">
                        <h4>📂 내가 만든 프로젝트</h4>
                        {/* myLead 배열에 데이터가 있으면 반복문(map)으로 그리고, 없으면 '없습니다' 출력 */}
                        {myLead.length > 0 ? (
                            myLead.map((proj) => (
                                <div
                                    key={proj.project_id}
                                    className="mp-activity-card"
                                    onClick={() => navigate(`/manage/${proj.project_id}`)} // 클릭 시 해당 프로젝트 관리 페이지로 이동
                                >
                                    <div className="proj-info">
                                        <span className="title">{proj.title}</span>
                                        <span className="sub-info">{proj.region || "지역 미정"}</span>
                                    </div>
                                    <span className={`mp-badge ${
                                        proj.status === '모집중' ? 'status-recruiting' : proj.status === '종료됨' ? 'status-complete' : 'status-ongoing'
                                    }`}>
                                        {proj.status || "상태 미정"}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <div className="no-data">생성한 프로젝트가 없습니다.</div>
                        )}
                    </div>

                    {/* 2. 참여 중인 프로젝트 리스트 (팀원) */}
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
                                    <span className={`mp-badge ${proj.status === '종료됨' ? 'status-complete' : 'status-ongoing'}`}>
                                        {proj.status === '종료됨' ? '종료됨' : '진행중'}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <div className="no-data">참여 중인 프로젝트가 없습니다.</div>
                        )}
                    </div>

                    <hr className="mp-divider-subtle" />

                    {/* 3. 지원 중인 모집글 리스트 (합격/불합격/대기 상태 모두 표시) */}
                    <div className="mp-section">
                        <h4>📩 지원 중인 모집글</h4>
                        {appliedPosts.length > 0 ? (
                            appliedPosts.map((proj) => (
                                <div
                                    key={proj.application_id || proj.project_id}
                                    className="mp-activity-card"
                                    onClick={() => navigate(`/post/${proj.project_id}`)} // 지원한 글은 관리 페이지가 아니라 공고 글로 이동
                                >
                                    <div className="proj-info">
                                        <span className="title">{proj.title}</span>
                                        <span className="sub-info">{proj.region || "지역 미정"}</span>
                                    </div>
                                    {/* 상태값(accepted, rejected, pending)에 따라 배지 색상과 글씨를 다르게 보여줍니다. */}
                                    <span className={`mp-badge ${
                                        proj.application_status === 'accepted' ? 'status-ongoing' : proj.application_status === 'rejected' ? 'status-complete' : 'status-pending'
                                    }`}>
                                        {proj.application_status === 'accepted' ? '승인됨' : proj.application_status === 'rejected' ? '거절됨' : '지원완료'}
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