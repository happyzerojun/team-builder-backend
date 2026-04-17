import "./PostCard.css";

function PostCard({ post, onClick }) {
    const roles = Array.isArray(post.roles) ? post.roles : [];
    const tags = Array.isArray(post.tags) ? post.tags : [];

    const createdDate = post.created_at || post.createdAt;
    const formattedDate = createdDate
        ? new Date(createdDate).toLocaleDateString()
        : "날짜 미정";

    const termValue = post.term ?? "";
    const durationType =
        String(termValue).includes("단기") || Number(termValue) <= 3
            ? "short"
            : "long";

    const durationLabel =
        String(termValue).includes("단기") || Number(termValue) <= 3
            ? "⚡ 단기"
            : "📅 장기";

    return (
        <article className="post-card" onClick={onClick}>
            <div className="card-header">
                <span className="badge-category">{post.category || "프로젝트"}</span>

                <span className={`badge-duration ${durationType}`}>
                    {durationLabel}
                </span>

                <span className="card-headcount">
                    👥 {post.headcount || post.recruit_count || 0}명 모집
                </span>
            </div>

            <h2 className="card-title">{post.title}</h2>

            <p className="card-description">
                {post.content || post.description || "프로젝트 소개가 없습니다."}
            </p>

            {roles.length > 0 && (
                <div className="card-roles">
                    {roles.map((role) => (
                        <span key={role} className="badge-role">
                            {role}
                        </span>
                    ))}
                </div>
            )}

            {tags.length > 0 && (
                <div className="card-tags">
                    {tags.map((tag) => (
                        <span key={tag} className="tag">
                            {tag}
                        </span>
                    ))}
                </div>
            )}

            <div className="card-footer">
                <span className="card-author">
                    ✍️ {post.author_name || post.author || "익명 사용자"}
                </span>
                <span className="card-date">{formattedDate}</span>
            </div>

            <div className="card-meta">
                {post.level && (
                    <span className={`badge-level ${post.level}`}>
                        {post.level === "초보"
                            ? "🌱 초보"
                            : post.level === "중급"
                            ? "⚡ 중급"
                            : "🔥 고수"}
                    </span>
                )}

                {post.hasTeamExp && (
                    <span className="badge-exp">
                        {post.hasTeamExp === "있음"
                            ? "🤝 협업 경험 있음"
                            : "🙋 협업 경험 없음"}
                    </span>
                )}
            </div>
        </article>
    );
}

export default PostCard;