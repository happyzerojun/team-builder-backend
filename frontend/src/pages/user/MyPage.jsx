import React, { useState, useEffect } from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom'; // 페이지 강제 이동(리다이렉트)을 도와주는 도구
import { projectService } from "../../services/projectService"; // 백엔드의 Project 컨트롤러와 통신
import { applicationService } from "../../services/applicationService"; // 백엔드의 Application 컨트롤러와 통신
import { getUserProfile } from "../../services/userService";
import { PROJECT_STATUS } from "../../constants/projectStatus";

const MY_LEAD_PAGE_SIZE = 10;
// 서버가 글 작성자를 팀원으로 자동 등록할 때 쓰는 역할 이름 (ProjectService.LEADER_ROLE)
const LEADER_ROLE = "팀장";

// 내가 만든 프로젝트 한 페이지 (Spring Page 응답을 화면용 값으로)
const fetchMyLeadPage = async (page) => {
    const data = await projectService.getMyProjects(page, MY_LEAD_PAGE_SIZE);
    const content = Array.isArray(data?.content) ? data.content : [];
    return {
        content,
        page: {
            number: data?.number ?? page,
            last: data?.last ?? true,
            totalElements: data?.totalElements ?? content.length
        }
    };
};


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
    const [myLead, setMyLead] = useState([]); // 내가 팀장인 프로젝트 (GET /api/projects/me, 페이지 단위로 이어 붙임)
    const [myLeadPage, setMyLeadPage] = useState({ number: 0, last: true, totalElements: 0 });
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [myPart, setMyPart] = useState([]); // 내가 팀원(참여 확정)인 프로젝트
    const [appliedPosts, setAppliedPosts] = useState([]); // 내가 지원한(대기/거절 포함) 프로젝트

    // 3. 로딩 상태 스위치 (데이터가 백엔드에서 오기 전까지 빙글빙글 로딩을 보여주기 위함)
    const [isLoading, setIsLoading] = useState(true);

    const handleLoadMoreLead = async () => {
        setIsLoadingMore(true);
        try {
            const next = await fetchMyLeadPage(myLeadPage.number + 1);
            setMyLead((prev) => [...prev, ...next.content]);
            setMyLeadPage(next.page);
        } catch (error) {
            console.error("내 프로젝트 더 보기 실패:", error);
            alert("프로젝트를 더 불러오지 못했습니다.");
        } finally {
            setIsLoadingMore(false);
        }
    };

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
                profileImg: savedUser.profileImg || null,
                region: savedUser.region || ""
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
                // 1. 서버에서 최신 프로필 가져오기
                const profile = await getUserProfile();

                setUser({
                    name: profile.name || savedUser.name || "",
                    email: profile.email || savedUser.email || "",
                    organization: profile.organization || "",
                    region: profile.region || savedUser.region || "",
                    introduction: profile.introduction || "",
                    tags: profile.tags || profile.techStacks || [],
                    jobRole: profile.jobRole || "",
                    profileImg: profile.profileImg || null
                });

                // 2. [내가 만든 프로젝트] 서버가 토큰 사용자 기준으로 걸러서 페이지로 준다 (GET /api/projects/me)
                // 이전에는 전체 목록 첫 페이지만 받아 leader_id로 걸러, 다른 사람 글이 많으면 내 글이 빠졌다 (TS-17)
                const lead = await fetchMyLeadPage(0);
                setMyLead(lead.content);
                setMyLeadPage(lead.page);

                // 3. 내 지원 내역. 응답에 프로젝트 제목·상태·지역이 들어 있어 전체 목록과 맞춰 볼 필요가 없다
                const myApplications = await applicationService.getMyApplications();
                const toCard = (app) => ({
                    project_id: app.projectId ?? app.project_id,
                    title: app.projectTitle,
                    region: app.projectRegion,
                    status: app.projectStatus,
                    application_id: app.applicationId ?? app.application_id,
                    application_status: app.status,
                    support_role: app.supportRole
                });
                const isAccepted = (app) => String(app.status).toUpperCase() === "ACCEPTED";
                // 팀장 본인의 자동 등록 지원서(팀장)는 지원 현황·참여 중 목록에서 뺀다 ('내가 만든 프로젝트'와 중복 방지)
                const isMyOwnLeaderSeat = (app) => app.supportRole === LEADER_ROLE;

                // 4. [지원 현황: 대기중/수락됨/거절됨]
                setAppliedPosts(
                    myApplications
                        .filter((app) => !isMyOwnLeaderSeat(app))
                        .filter((app) => ["PENDING", "ACCEPTED", "REJECTED"].includes(String(app.status).toUpperCase()))
                        .map(toCard)
                );

                // 5. [참여 확정된 프로젝트 (팀원)]
                setMyPart(myApplications.filter((app) => isAccepted(app) && !isMyOwnLeaderSeat(app)).map(toCard));

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
                                <div className="mp-info-row">
                                    <span className="mp-label">소속</span>
                                    <span className="mp-value">{user.organization || "미입력"}</span>
                                </div>
                                <span className="mp-inline-divider">|</span>
                                <span className="mp-detail-item">
                                    <span className="mp-label">지역</span>
                                    <span className="mp-value">{user.region || "미입력"}</span>
                                </span>
                                <span className="mp-inline-divider">|</span>
                                <span className="mp-detail-item">
                                    <span className="mp-label">이메일</span>
                                    <span className="mp-value">{user.email || "정보 없음"}</span>
                                </span>
                                <span className="mp-inline-divider">|</span>
                                <span className="mp-detail-item">
                                    <span className="mp-label">희망 직무</span>
                                    <span className="mp-value">{user.jobRole || user.job_role || "미입력"}</span>
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
                        <h4>📂 내가 만든 프로젝트{myLeadPage.totalElements > 0 ? ` (${myLeadPage.totalElements})` : ""}</h4>
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
                                        proj.status === PROJECT_STATUS.RECRUITING ? 'status-recruiting' : proj.status === PROJECT_STATUS.COMPLETED ? 'status-complete' : 'status-ongoing'
                                    }`}>
                                        {proj.status || "상태 미정"}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <div className="no-data">생성한 프로젝트가 없습니다.</div>
                        )}
                        {!myLeadPage.last && (
                            <button
                                type="button"
                                className="mp-review-btn"
                                style={{ marginTop: "8px" }}
                                onClick={handleLoadMoreLead}
                                disabled={isLoadingMore}
                            >
                                {isLoadingMore ? "불러오는 중..." : "더 보기"}
                            </button>
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
                                    <span className={`mp-badge ${proj.status === PROJECT_STATUS.COMPLETED ? 'status-complete' : 'status-ongoing'}`}>
                                        {proj.status === PROJECT_STATUS.COMPLETED ? '종료됨' : '진행중'}
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
                                        (proj.application_status === 'accepted' || proj.application_status === 'ACCEPTED') ? 'status-ongoing' : (proj.application_status === 'rejected' || proj.application_status === 'REJECTED') ? 'status-complete' : 'status-pending'
                                    }`}>
                                        {(proj.application_status === 'accepted' || proj.application_status === 'ACCEPTED') ? '승인됨' : (proj.application_status === 'rejected' || proj.application_status === 'REJECTED') ? '거절됨' : '지원완료'}
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