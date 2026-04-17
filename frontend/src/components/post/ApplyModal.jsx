function ApplyModal({ post, onClose, onConfirm }) {
    const isKakao = post.applyType === "kakao";
    const applyLink = isKakao ? post.kakaoLink : post.googleFormLink;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-box" onClick={(e) => e.stopPropagation()}>
                <button className="modal-close" onClick={onClose}>✕</button>
                <div className="modal-icon">{isKakao ? "💬" : "📋"}</div>
                <h2 className="modal-title">{isKakao ? "카카오 오픈채팅으로 지원" : "구글폼으로 지원"}</h2>
                <p className="modal-desc">
                    {isKakao
                        ? "오픈채팅방 링크로 이동합니다.\n입장 후 지원 의사를 전달해주세요."
                        : "구글폼 신청서 링크로 이동합니다.\n양식을 작성하여 지원해주세요."}
                </p>
                <div className="modal-actions">
                    <button className="modal-btn-cancel" onClick={onClose}>취소</button>
                    <button className="modal-btn-confirm" onClick={onConfirm}>
                        {isKakao ? "오픈채팅 입장하기 →" : "신청서 작성하기 →"}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ApplyModal;