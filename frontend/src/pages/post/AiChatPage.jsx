import { useState } from "react";
import { useNavigate } from "react-router-dom";
// import PostCard from "@/components/post/PostCard";
import PostCard from "../../components/post/PostCard";

export default function AiChatPage() {
    const navigate = useNavigate();

    const [input, setInput] = useState("");
    const [messages, setMessages] = useState([
        { role: "ai", text: "안녕하세요! 어떤 프로젝트를 찾고 있나요?" }
    ]);
    const [loading, setLoading] = useState(false);

    // 🔥 추가된 상태 (추천 카드용)
    const [recommendPosts, setRecommendPosts] = useState([]);

    // 🔥 가짜 데이터 (나중에 백엔드로 교체)
    const fakeProjects = [
        {
            id: 1,
            title: "백엔드 스터디 프로젝트",
            description: "Spring Boot로 API 서버 만들기",
            tags: ["Spring Boot", "MySQL"],
            roles: ["백엔드"], 
            category: "개발",
            duration: "단기",
            headcount: 3,
            author: "AI 추천",
            createdAt: "방금 전",
            level: "초보",
            hasTeamExp: "없음"
        },
        {
            id: 2,
            title: "프론트 협업 프로젝트",
            description: "React로 웹 서비스 개발",
            tags: ["React"],
            roles: ["프론트엔드"],
            category: "개발",
            duration: "장기",
            headcount: 4,
            author: "AI 추천",
            createdAt: "방금 전",
            level: "중급",
            hasTeamExp: "있음"
        }
    ];

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMessage = { role: "user", text: input };
        setMessages((prev) => [...prev, userMessage]);
        setInput("");
        setLoading(true);

        // 👉 나중에 여기 API 연결
        setTimeout(() => {
            setMessages((prev) => [
                ...prev,
                { role: "ai", text: "추천 프로젝트를 찾았어요 👇" }
            ]);

            // 🔥 여기서 카드 데이터 넣기
            setRecommendPosts(fakeProjects);

            setLoading(false);
        }, 1000);
    };

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>🤖 AI 프로젝트 추천</h1>

            {/* 채팅 영역 */}
            <div style={styles.chatBox}>
                {messages.map((msg, i) => (
                    <div
                        key={i}
                        style={{
                            ...styles.message,
                            alignSelf: msg.role === "user" ? "flex-end" : "flex-start",
                            background: msg.role === "user" ? "#5a52d6" : "#eee",
                            color: msg.role === "user" ? "white" : "black"
                        }}
                    >
                        {msg.text}
                    </div>
                ))}

                {loading && <div style={styles.loading}>AI가 생각중...</div>}
            </div>

            {/* 입력창 */}
            <div style={styles.inputBox}>
                <input
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="예: 백엔드 공부용 프로젝트 추천해줘"
                    style={styles.input}
                    onKeyDown={(e) => e.key === "Enter" && handleSend()}
                />
                <button onClick={handleSend} style={styles.button}>
                    전송
                </button>
            </div>

            {/* 🔥 추천 카드 영역 */}
            {recommendPosts.length > 0 && (
                <div style={{ marginTop: "20px" }}>
                    <h3>📌 추천 프로젝트</h3>

                    <div style={styles.cardGrid}>
                        {recommendPosts.map((post) => (
                            <PostCard
                                key={post.id}
                                post={post}
                                onClick={() => navigate(`/post/${post.id}`)}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

const styles = {
    container: {
        maxWidth: "600px",
        margin: "0 auto",
        padding: "20px",
        display: "flex",
        flexDirection: "column",
        height: "100vh"
    },
    title: {
        textAlign: "center"
    },
    chatBox: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        padding: "10px",
        border: "1px solid #ddd",
        borderRadius: "10px",
        overflowY: "auto"
    },
    message: {
        padding: "10px 14px",
        borderRadius: "12px",
        maxWidth: "70%"
    },
    inputBox: {
        display: "flex",
        marginTop: "10px",
        gap: "10px"
    },
    input: {
        flex: 1,
        padding: "10px",
        borderRadius: "8px",
        border: "1px solid #ccc"
    },
    button: {
        padding: "10px 16px",
        borderRadius: "8px",
        background: "#5a52d6",
        color: "white",
        border: "none"
    },
    loading: {
        fontSize: "12px",
        color: "gray"
    },
    cardGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "10px"
    }
};