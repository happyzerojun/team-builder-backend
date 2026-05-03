import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { projectService } from "../../services/projectService";
import { applicationService } from "../../services/applicationService";
import Navbar from "@/components/common/Navbar";
import "./DetailPage.css";


function DetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isApplied, setIsApplied] = useState(false);
    const [myApplicationId, setMyApplicationId] = useState(null);

    useEffect(() => {
        const fetchPostDetail = async () => {
            try {
                setLoading(true);

                const data = await projectService.getProjectById(id);
                setPost(data);

                // 내가 이 프로젝트에 이미 지원했는지 확인
                try {
                    const myApplications = await applicationService.getMyApplications();

                    const matchedApplication = myApplications.find((app) => {
                        // 1순위: project 객체 포함 응답
                        if (app.project && String(app.project.project_id) === String(id)) {
                            return true;
                        }

                        // 2순위: project_id 직접 포함 응답
                        if (String(app.project_id) === String(id)) {
                            return true;
                        }

                        return false;
                    });

                    if (matchedApplication) {
                        setIsApplied(true);
                        setMyApplicationId(matchedApplication.application_id);
                    } else {
                        setIsApplied(false);
                        setMyApplicationId(null);
                    }
                } catch (applicationError) {
                    console.error("내 지원 여부 확인 실패:", applicationError);
                    setIsApplied(false);
                    setMyApplicationId(null);
                }

            } catch (error) {
                console.error("상세 정보 로딩 실패:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPostDetail();
    }, [id]);

    const handleCancel = async () => {
        try {
            if (!window.confirm("정말 이 프로젝트 지원을 취소하시겠습니까?")) return;

            if (!myApplicationId) {
                alert("지원 취소에 필요한 신청 정보가 없습니다.");
                return;
            }

            await applicationService.cancel(myApplicationId);
            setIsApplied(false);
            setMyApplicationId(null);

            alert("지원이 취소되었습니다.");
            navigate("/MyPage");
        } catch (error) {
            console.error("지원 취소 실패:", error);
            alert("지원 취소에 실패했습니다.");
        }
    };

    const handleApply = () => {
        navigate(`/apply/${id}`);
    };

    if (loading) {
        return (
            <div className="detail-page">
                <Navbar />
                <div className="loading-state" style={{ textAlign: "center", marginTop: "100px" }}>
                    <p>데이터를 불러오는 중입니다...</p>
                </div>
            </div>
        );
    }

    if (!post) {
        return (
            <div className="detail-page">
                <Navbar />
                <div className="not-found">
                    <span>🔍</span>
                    <h2>글을 찾을 수 없습니다.</h2>
                    <p>삭제된 게시글이거나 경로가 잘못되었습니다.</p>
                    <button onClick={() => navigate("/")}>메인으로 돌아가기</button>
                </div>
            </div>
        );
    }

    return (
        <div className="detail-page">
            <Navbar />
            <main className="detail-content">
                <button className="btn-back" onClick={() => navigate(-1)}>
                    ← 목록으로
                </button>

                <article className="detail-card">
                    <div className="detail-meta">
                        <span className="badge-category">{post.category || "프로젝트"}</span>
                        <span className="meta-divider">·</span>
                        <span className="meta-text">📍 {post.region || "지역 미정"}</span>
                        <span className="meta-divider">·</span>
                        <span className="meta-text">{post.status || "모집중"}</span>
                    </div>

                    <h1 className="detail-title">{post.title}</h1>

                    <div className="detail-author">
                        <div className="author-avatar">{(post.author_name || "익")[0]}</div>
                        <span>
                            {post.author_name?.name || post.author_name || "익명 사용자"}
                        </span>
                    </div>

                    <hr className="divider" />

                    <section className="detail-section">
                        <h2 className="section-title">📋 프로젝트 소개</h2>
                        <p className="detail-description">{post.content || "내용이 없습니다."}</p>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">⏳ 진행 기간</h2>
                        <p className="detail-description">
                            {post.term ? `${post.term}개월` : "미정"}
                        </p>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">🛠 기술 스택</h2>

                        {post.techStacks && post.techStacks.length > 0 ? (
                            <div className="detail-tech-list">
                            {post.techStacks.map((tech) => (
                                <span key={tech.tech_stack_id} className="detail-tech-tag">
                                {tech.name}
                                </span>
                            ))}
                            </div>
                        ) : (
                            <p className="detail-description">등록된 기술 스택이 없습니다.</p>
                        )}
                    </section>

                    <div
                        className="detail-footer"
                        style={{ marginTop: "40px", display: "flex", justifyContent: "center" }}
                    >
                        {isApplied ? (
                            <button className="btn-cancle" onClick={handleCancel}>
                                지원 취소하기
                            </button>
                        ) : (
                            <button className="btn-apply" onClick={handleApply}>
                                프로젝트 지원하기
                            </button>
                        )}
                    </div>
                </article>
            </main>

        </div>
    );
}

export default DetailPage;