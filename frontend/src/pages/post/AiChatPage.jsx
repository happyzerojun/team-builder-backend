import { useState } from "react";
import { useNavigate } from "react-router-dom";
// import PostCard from "@/components/post/PostCard";
import PostCard from "../../components/post/PostCard";

/**
 * ✅ 날짜 포맷 함수
 * API 응답에 날짜가 없을 경우를 대비해 오늘 날짜를 기본값으로 설정합니다.
 */
const formatDate = (dateString) => {
    const date = dateString ? new Date(dateString) : new Date();
    return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
    }).format(date);
};

export default function AiChatPage() {
    const navigate = useNavigate();
    const [input, setInput] = useState("");
    const [messages, setMessages] = useState([{ role: "ai", text: "안녕하세요! 어떤 프로젝트를 찾고 있나요?" }]);
    const [loading, setLoading] = useState(false);

    // 추천 카드용 상태 (백엔드 명세에 맞춰 단일 객체 혹은 배열로 관리)
    const [recommendPosts, setRecommendPosts] = useState([]);

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMessage = { role: "user", text: input };
        setMessages((prev) => [...prev, userMessage]);
        const currentInput = input;
        setInput("");

        setRecommendPosts([]);
        setLoading(true);

        try {
            // 🔗 백엔드 API 호출 (명세서 반영)
            const response = await fetch("http://localhost:8080/api/ai/recommend", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // "Authorization": "Bearer {JWT토큰}" // 시큐리티 해제 상태이므로 일단 주석 처리
                },
                body: JSON.stringify({ prompt: currentInput })
            });

            if (!response.ok) throw new Error("서버 응답 에러");

            const data = await response.json();

            // 1. AI 채팅 메시지 추가
            setMessages((prev) => [
                ...prev,
                { role: "ai", text: "수연 님을 위한 맞춤 프로젝트를 찾았어요! 아래 카드를 확인해 보세요." }
            ]);

            /**
             * ✅ 2. 데이터 매핑 (가장 중요한 부분!)
             * 백엔드 명세서의 필드명을 PostCard 컴포넌트가 사용하는 필드명으로 변환합니다.
             */
            const mappedProject = {
                id: Date.now(), // 상세 페이지 이동을 위한 임시 ID
                title: data.title,
                description: data.description,
                tags: data.techStack ? data.techStack.split(", ") : [], // "Spring Boot, PostgreSQL" -> ["Spring Boot", "PostgreSQL"]
                level: data.experienceLevel,
                createdAt: formatDate(null), // 명세서에 날짜가 없으므로 생성 시점 날짜 적용
                author: "AI 추천"
            };

            setRecommendPosts([mappedProject]); // 카드가 배열 기반이므로 배열에 담아줌

        } catch (error) {
            console.error("연동 실패:", error);
            setMessages((prev) => [
                ...prev,
                { role: "ai", text: "죄송해요, 서버와 통신 중 문제가 발생했습니다." }
            ]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>🤖 AI 프로젝트 추천</h1>
            <div style={styles.chatBox}>
                {messages.map((msg, i) => (
                    <div key={i} style={{ ...styles.message, alignSelf: msg.role === "user" ? "flex-end" : "flex-start", background: msg.role === "user" ? "#5a52d6" : "#eee", color: msg.role === "user" ? "white" : "black" }}>
                        {msg.text}
                    </div>
                ))}
            </div>

            <div style={styles.inputBox}>
                <input value={input} onChange={(e) => setInput(e.target.value)} placeholder="예: 백엔드 공부용 프로젝트 추천해줘" style={styles.input} onKeyDown={(e) => e.key === "Enter" && handleSend()} />
                <button onClick={handleSend} style={styles.button}>전송</button>
            </div>

            {(loading || recommendPosts.length > 0) && (
                <div style={{ marginTop: "20px" }}>
                    <h3 style={{ marginBottom: "10px" }}>📌 추천 프로젝트</h3>
                    <div style={styles.cardGrid}>
                        {loading ? (
                            <><div style={styles.skeletonCard}></div><div style={styles.skeletonCard}></div></>
                        ) : (
                            recommendPosts.map((post) => (
                                <PostCard key={post.id} post={post} onClick={() => navigate(`/post/${post.id}`)} />
                            ))
                        )}
                    </div>
                </div>
            )}
            <style>{`@keyframes skeleton-gradient { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }`}</style>
        </div>
    );
}

const styles = {
    container: { maxWidth: "600px", margin: "0 auto", padding: "20px", display: "flex", flexDirection: "column", height: "100vh" },
    title: { textAlign: "center" },
    chatBox: { flex: 1, display: "flex", flexDirection: "column", gap: "10px", padding: "10px", border: "1px solid #ddd", borderRadius: "10px", overflowY: "auto", marginBottom: "10px" },
    message: { padding: "10px 14px", borderRadius: "12px", maxWidth: "70%" },
    inputBox: { display: "flex", gap: "10px" },
    input: { flex: 1, padding: "10px", borderRadius: "8px", border: "1px solid #ccc" },
    button: { padding: "10px 16px", borderRadius: "8px", background: "#5a52d6", color: "white", border: "none", cursor: "pointer" },
    cardGrid: { display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" },
    skeletonCard: { height: "150px", borderRadius: "12px", background: "linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%)", backgroundSize: "200% 100%", animation: "skeleton-gradient 1.5s infinite linear" }
};