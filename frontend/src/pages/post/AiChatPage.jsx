import { useState } from "react";
import { useNavigate } from "react-router-dom";
import PostCard from "../../components/post/PostCard";

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
    const [messages, setMessages] = useState([
        { role: "ai", text: "안녕하세요! 어떤 프로젝트를 찾고 있나요?" }
    ]);
    const [loading, setLoading] = useState(false);
    const [recommendPosts, setRecommendPosts] = useState([]);

    // 👉 사용자 이름
    const username = localStorage.getItem("username") || "사용자";

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMessage = { role: "user", text: input };
        setMessages((prev) => [...prev, userMessage]);

        const currentInput = input;
        setInput("");

        setRecommendPosts([]);
        setLoading(true);

        try {
            const response = await fetch("http://localhost:8080/api/ai/recommend", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ prompt: currentInput })
            });

            if (!response.ok) throw new Error("서버 응답 에러");

            const data = await response.json();

            // 👉 채팅 메시지
            setMessages((prev) => [
                ...prev,
                {
                    role: "ai",
                    text: `${username}님을 위한 맞춤 프로젝트를 찾았어요!`
                }
            ]);

            // 👉 카드 데이터
            const mappedProject = {
                id: Date.now(),
                title: data.title,
                description: data.description,
                tags: data.techStack ? data.techStack.split(", ") : [],
                level: data.experienceLevel,
                createdAt: formatDate(null),
                author: "AI 추천"
            };

            setRecommendPosts([mappedProject]);

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

            <div style={styles.wrapper}>

                {/* 🔹 왼쪽: 채팅 */}
                <div style={styles.chatSection}>
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

                        {loading && (
                            <div style={styles.loadingText}>
                                🤖 AI가 열심히 생각중입니다...
                            </div>
                        )}
                    </div>

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
                </div>

                {/* 🔹 오른쪽: 카드 */}
                <div style={styles.cardSection}>
                    <h3>📌 추천 프로젝트</h3>

                    {recommendPosts.length === 0 ? (
                        <div style={styles.emptyText}>
                            아직 추천 결과가 없습니다
                        </div>
                    ) : (
                        <div style={styles.cardGrid}>
                            {recommendPosts.map((post) => (
                                <PostCard
                                    key={post.id}
                                    post={post}
                                    onClick={() => navigate(`/post/${post.id}`)}
                                />
                            ))}
                        </div>
                    )}
                </div>

            </div>
        </div>
    );
}

const styles = {
    container: {
        maxWidth: "1000px",
        margin: "0 auto",
        padding: "20px"
    },
    title: {
        textAlign: "center",
        marginBottom: "20px"
    },
    wrapper: {
        display: "flex",
        gap: "20px",
        height: "80vh"
    },
    chatSection: {
        flex: 1,
        display: "flex",
        flexDirection: "column"
    },
    chatBox: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        padding: "10px",
        border: "1px solid #ddd",
        borderRadius: "10px",
        overflowY: "auto",
        marginBottom: "10px"
    },
    message: {
        padding: "10px 14px",
        borderRadius: "12px",
        maxWidth: "70%"
    },
    loadingText: {
        color: "#888",
        fontSize: "14px",
        padding: "4px 10px"
    },
    inputBox: {
        display: "flex",
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
        border: "none",
        cursor: "pointer"
    },
    cardSection: {
        width: "320px",
        borderLeft: "1px solid #ddd",
        paddingLeft: "15px",
        overflowY: "auto"
    },
    cardGrid: {
        display: "flex",
        flexDirection: "column",
        gap: "10px"
    },
    emptyText: {
        color: "#888",
        marginTop: "20px"
    }
};