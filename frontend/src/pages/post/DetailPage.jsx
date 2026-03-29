// 📁 src/pages/DetailPage.jsx

import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { projectService } from "../../services/projectService"; 
import Navbar from "@/components/common/Navbar";
import "./DetailPage.css";
import { applyToProject } from "../../services/applyService";
import ApplyModal from "@/components/post/ApplyModal";
function DetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    // ✅ 상태 관리
    const [post, setPost] = useState(null); 
    const [loading, setLoading] = useState(true); 
    const [isApplied, setIsApplied] = useState(false); 
    const [showApplyModal, setShowApplyModal] = useState(false); 

    // ✅ 서버에서 실시간 데이터 가져오기
    useEffect(() => {
        const fetchPostDetail = async () => {
            try {
                setLoading(true);
                const data = await projectService.getProjectById(id);
                setPost(data);
            } catch (error) {
                console.error("상세 정보 로딩 실패:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPostDetail();

        const appliedIds = JSON.parse(localStorage.getItem("appliedProjects") || "[]");
        const alreadyApplied = appliedIds.includes(Number(id));
        setIsApplied(alreadyApplied);
    }, [id]);

    const handleCancel = () => {
        if (window.confirm("정말 이 프로젝트 지원을 취소하시겠습니까?")) {
            const appliedIds = JSON.parse(localStorage.getItem("appliedProjects") || "[]");
            const updatedIds = appliedIds.filter((appliedId) => Number(appliedId) !== Number(id));
            localStorage.setItem("appliedProjects", JSON.stringify(updatedIds));
            setIsApplied(false);
            alert("지원이 취소되었습니다.");
            navigate("/MyPage");
        }
    };

    const handleApply = () => {
        setShowApplyModal(true);
    };

    const handleConfirmApply = () => {
        const applyLink = post.applyType === "kakao" ? post.kakaoLink : post.googleFormLink;

        if (applyLink) {
            window.open(applyLink, "_blank", "noopener,noreferrer");
        }

        const success = applyToProject(Number(id));
        if (success) {
            setIsApplied(true);
            setShowApplyModal(false);
            alert(`"${post.title}" 프로젝트에 지원했습니다! 마이페이지로 이동합니다.`);
            navigate("/MyPage");
        } else {
            alert("이미 지원한 프로젝트입니다.");
            setShowApplyModal(false);
        }
    };

    if (loading) {
        return (
            <div className="detail-page">
                <Navbar />
                <div className="loading-state" style={{ textAlign: 'center', marginTop: '100px' }}>
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

    const isKakao = post.applyType === "kakao";
    const applyLink = isKakao ? post.kakaoLink : post.googleFormLink;

    return (
        <div className="detail-page">
            <Navbar />
            <main className="detail-content">
                <button className="btn-back" onClick={() => navigate(-1)}>
                    ← 목록으로
                </button>

                <article className="detail-card">
                    <div className="detail-meta">
                        <span className="badge-category">{post.category}</span>
                        <span className="meta-divider">·</span>
                        <span className="meta-text">👥 {post.headcount}명 모집 중</span>
                        <span className="meta-divider">·</span>
                        <span className="meta-text">{new Date(post.createdAt).toLocaleDateString()}</span>
                    </div>

                    <h1 className="detail-title">{post.title}</h1>

                    <div className="detail-author">
                        <div className="author-avatar">{(post.author || "익")[0]}</div>
                        <span>{post.author || "익명 사용자"}</span>
                    </div>

                    <hr className="divider" />

                    <section className="detail-section">
                        <h2 className="section-title">📋 프로젝트 소개</h2>
                        <p className="detail-description">{post.description}</p>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">🙋 모집 분야</h2>
                        <div className="roles-list">
                            {Array.isArray(post.roles) ? post.roles.map((role) => (
                                <span key={role} className="badge-role">{role}</span>
                            )) : <span className="badge-role">{post.roles}</span>}
                        </div>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">🛠 기술 스택</h2>
                        <div className="tags-list">
                            {Array.isArray(post.tags) && post.tags.map((tag) => (
                                <span key={tag} className="tag">{tag}</span>
                            ))}
                        </div>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">📋 모집 조건</h2>
                        <div className="tags-list">
                            <span className={`badge-level ${post.level}`}>
                                {post.level === "초보" ? "🌱 초보" : post.level === "중급" ? "⚡ 중급" : "🔥 고수"}
                            </span>
                            <span className="badge-exp">
                                {post.hasTeamExp === "있음" ? "🤝 협업 경험 있음" : "🙋 협업 경험 무관"}
                            </span>
                        </div>
                    </section>

                    {applyLink && (
                        <section className="detail-section">
                            <h2 className="section-title">
                                {isKakao ? "💬 지원 방법" : "📋 지원 방법"}
                            </h2>
                            <div className="apply-info-banner">
                                <span className="apply-info-icon">{isKakao ? "💬" : "📋"}</span>
                                <div className="apply-info-text">
                                    <strong>{isKakao ? "카카오 오픈채팅" : "구글폼 신청서"}로 지원</strong>
                                    <p>{isKakao
                                        ? "아래 버튼을 눌러 카카오 오픈채팅방에 입장 후 지원해주세요."
                                        : "아래 버튼을 눌러 구글폼 신청서를 작성해주세요."
                                    }</p>
                                </div>
                            </div>
                        </section>
                    )}

                    <div className="detail-footer" style={{ marginTop: "40px", display: "flex", justifyContent: "center" }}>
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

            {showApplyModal && (
                <ApplyModal
                    post={post}
                    onClose={() => setShowApplyModal(false)}
                    onConfirm={handleConfirmApply}
                />
            )}
        </div>
    );
}

export default DetailPage;