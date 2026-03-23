import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { MOCK_POSTS } from "../../data/mockData";
import Navbar from "@/components/common/Navbar";
import "./DetailPage.css";
import { applyToProject } from "../../services/applyService";

function DetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [isApplied, setIsApplied] = useState(false);

    // ✅ 추가: 지원 모달 상태
    const [showApplyModal, setShowApplyModal] = useState(false);

    const post = MOCK_POSTS.find((p) => p.id === Number(id));

    useEffect(() => {
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

    // ✅ 수정: 지원하기 버튼 → 모달 열기
    const handleApply = () => {
        setShowApplyModal(true);
    };

    // ✅ 추가: 모달에서 링크 이동 후 지원 처리
    const handleConfirmApply = () => {
        const applyLink = post.applyType === "kakao" ? post.kakaoLink : post.googleFormLink;

        // 링크가 있으면 새 탭으로 열기
        if (applyLink) {
            window.open(applyLink, "_blank", "noopener,noreferrer");
        }

        // 지원 기록 저장
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

    if (!post) {
        return (
            <div className="detail-page">
                <Navbar />
                <div className="not-found">
                    <span>🔍</span>
                    <h2>글을 찾을 수 없습니다.</h2>
                    <button onClick={() => navigate("/")}>메인으로 돌아가기</button>
                </div>
            </div>
        );
    }

    // ✅ 지원 방식에 따라 표시할 정보
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
                        <span className="meta-text">{post.createdAt}</span>
                    </div>

                    <h1 className="detail-title">{post.title}</h1>

                    <div className="detail-author">
                        <div className="author-avatar">{post.author[0]}</div>
                        <span>{post.author}</span>
                    </div>

                    <hr className="divider" />

                    <section className="detail-section">
                        <h2 className="section-title">📋 프로젝트 소개</h2>
                        <p className="detail-description">{post.description}</p>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">🙋 모집 분야</h2>
                        <div className="roles-list">
                            {post.roles.map((role) => (
                                <span key={role} className="badge-role">{role}</span>
                            ))}
                        </div>
                    </section>

                    <section className="detail-section">
                        <h2 className="section-title">🛠 기술 스택</h2>
                        <div className="tags-list">
                            {post.tags.map((tag) => (
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
                                {post.hasTeamExp === "있음" ? "🤝 협업 경험 있음" : "🙋 협업 경험 없음"}
                            </span>
                        </div>
                    </section>

                    {/* ✅ 지원 방식 안내 배너 */}
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

            {/* ✅ 지원하기 모달 */}
            {showApplyModal && (
                <div className="modal-overlay" onClick={() => setShowApplyModal(false)}>
                    <div className="modal-box" onClick={(e) => e.stopPropagation()}>
                        <button className="modal-close" onClick={() => setShowApplyModal(false)}>✕</button>

                        <div className="modal-icon">{isKakao ? "💬" : "📋"}</div>
                        <h2 className="modal-title">
                            {isKakao ? "카카오 오픈채팅으로 지원" : "구글폼으로 지원"}
                        </h2>
                        <p className="modal-desc">
                            {isKakao
                                ? "카카오 오픈채팅방 링크로 이동합니다.\n입장 후 지원 의사를 전달해주세요."
                                : "구글폼 신청서 링크로 이동합니다.\n양식을 작성하여 지원해주세요."
                            }
                        </p>

                        {applyLink && (
                            <div className="modal-link-preview">
                                <span className="modal-link-label">
                                    {isKakao ? "오픈채팅 링크" : "구글폼 링크"}
                                </span>
                                <span className="modal-link-url">{applyLink}</span>
                            </div>
                        )}

                        <div className="modal-actions">
                            <button
                                className="modal-btn-cancel"
                                onClick={() => setShowApplyModal(false)}
                            >
                                취소
                            </button>
                            <button
                                className="modal-btn-confirm"
                                onClick={handleConfirmApply}
                            >
                                {isKakao ? "오픈채팅 입장하기 →" : "신청서 작성하기 →"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default DetailPage;
