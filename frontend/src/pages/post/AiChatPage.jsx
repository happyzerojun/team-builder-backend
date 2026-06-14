import React, { useState , useEffect} from "react";
import { useNavigate } from "react-router-dom";
import useAiRecommend from "../../hooks/useAiRecommend";
import PostCard from "../../components/post/PostCard";
import styles from "./AiChatPage.module.css";

const TECH_MAP = {
  "프론트엔드": ["React", "Vue", "Angular", "Next.js", "Svelte", "TypeScript", "JavaScript", "HTML/CSS", "Tailwind"],
  "백엔드": ["Spring Boot", "Node.js", "Django", "FastAPI", "Flask", "Express", "NestJS", "Java", "Python", "Go", "Kotlin", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite"],
  "인프라/기타": ["Firebase", "AWS", "Docker", "Kubernetes", "CI/CD", "GCP", "Azure", "Nginx", "Linux", "Git", "Figma", "Unity", "Flutter", "React Native", "Swift", "Kotlin(Android)", "Kotlin(iOS)"]
};

const DURATION_OPTIONS = [
  { label: "1개월 내", value: "1개월 이내" },
  { label: "1~2개월", value: "1~2개월" },
  { label: "2~3개월", value: "2~3개월" },
  { label: "3~6개월", value: "3~6개월" },
  { label: "6개월 이상", value: "6개월 이상" },
];

// 🔥 변경점 1: 로딩 중 순차적으로 보여줄 메시지 배열 정의
const LOADING_MESSAGES = [
  "🔍 DB에서 프로젝트 목록 검색 중...",
  "📝 맞춤형 AI 프롬프트 구성 중...",
  "🧠 구글 제미나이 AI 추천 분석 중...",
  "🎨 추천 카드 결과 생성 중..."
];

export default function AiChatPage() {
  const navigate = useNavigate();
  
  const [selectedTechs, setSelectedTechs] = useState(() => JSON.parse(sessionStorage.getItem("aiTechs")) || []);
  const [duration, setDuration] = useState(() => sessionStorage.getItem("aiDuration") || "");
  const [freeText, setFreeText] = useState(() => sessionStorage.getItem("aiFreeText") || "");
  const [activeTab, setActiveTab] = useState(() => sessionStorage.getItem("aiActiveTab") || "프론트엔드");

  // 🔥 변경점 2: 현재 표시할 로딩 메시지의 인덱스 상태 추가
  const [loadingStep, setLoadingStep] = useState(0);

  const { result, isLoading, error, getRecommendation } = useAiRecommend();

  useEffect(() => {
    sessionStorage.setItem("aiTechs", JSON.stringify(selectedTechs));
    sessionStorage.setItem("aiDuration", duration);
    sessionStorage.setItem("aiFreeText", freeText);
    sessionStorage.setItem("aiActiveTab", activeTab);
  }, [selectedTechs, duration, freeText, activeTab]);

  // 🔥 변경점 3: isLoading이 true가 되면 2.5초마다 메시지를 순차적으로 변경하는 타이머 가동
  useEffect(() => {
    let timer;
    if (isLoading) {
      setLoadingStep(0); // 로딩 시작 시 첫 번째 메시지로 초기화
      timer = setInterval(() => {
        setLoadingStep((prev) => (prev < LOADING_MESSAGES.length - 1 ? prev + 1 : prev));
      }, 2500); // 2.5초마다 다음 단계 메시지로 전환
    } else {
      setLoadingStep(0);
    }

    return () => clearInterval(timer); // 언마운트 시 타이머 클리어
  }, [isLoading]);

  const toggleTech = (tech) => {
    setSelectedTechs((prev) =>
      prev.includes(tech) ? prev.filter((t) => t !== tech) : [...prev, tech]
    );
  };

  const handleSubmit = () => {
    if (selectedTechs.length === 0 && !duration && !freeText.trim()) {
      alert("기술스택, 기간 중 하나 이상 선택하거나 내용을 입력해주세요.");
      return;
    }

    let serializedPrompt = "다음 조건과 상황에 맞는 프로젝트를 추천해줘. ";

    if (selectedTechs.length > 0) {
      serializedPrompt += `내가 사용할 기술 스택은 [${selectedTechs.join(", ")}] 이야. `;
    }
    if (duration) {
      serializedPrompt += `프로젝트 기간은 [${duration}] 정도를 선호해. `;
    }
    if (freeText.trim()) {
      serializedPrompt += `그리고 나의 특별한 목적이나 상황은 다음과 같아: "${freeText.trim()}"`;
    }

    getRecommendation(serializedPrompt);
  };

  const recommendedPosts = result?.recommendations?.map((item) => ({
    id: item.project_id,
    title: item.title,
    description: item.reason,
    tags: [`매칭 ${item.matching_score}%`],
    level: "AI 추천",
    createdAt: new Date().toLocaleDateString(),
    author: "AI 멘토"
  })) || [];

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>🤖 맞춤형 프로젝트 AI 추천</h1>
      <p className={styles.subtitle}>스택, 기간, 그리고 나의 목표(예: 대기업 취업, 쇼핑몰 개발)를 알려주세요.</p>

      <section className={styles.section}>
        <span className={styles.label}>관심 기술스택</span>
        <div className={styles.tabs}>
          {Object.keys(TECH_MAP).map((cat) => (
            <button key={cat} className={`${styles.tab} ${activeTab === cat ? styles.tabOn : ""}`} onClick={() => setActiveTab(cat)}>
              {cat}
            </button>
          ))}
        </div>
        <div className={styles.chips}>
          {TECH_MAP[activeTab].map((tech) => (
            <button key={tech} className={`${styles.chip} ${selectedTechs.includes(tech) ? styles.chipOn : ""}`} onClick={() => toggleTech(tech)}>
              {tech}
            </button>
          ))}
        </div>
      </section>

      <section className={styles.section}>
        <span className={styles.label}>선호 프로젝트 기간</span>
        <div className={styles.chips}>
          {DURATION_OPTIONS.map((opt) => (
            <button key={opt.value} className={`${styles.chip} ${duration === opt.value ? styles.chipOn : ""}`} onClick={() => setDuration(duration === opt.value ? "" : opt.value)}>
              {opt.label}
            </button>
          ))}
        </div>
      </section>

      <section className={styles.section}>
        <span className={styles.label}>나의 상황 및 목적 (중요도 ⭐️⭐️⭐️)</span>
        <textarea
          className={styles.textarea}
          value={freeText}
          onChange={(e) => setFreeText(e.target.value)}
          rows={3}
          placeholder="예: 나는 쇼핑몰 쪽 개발을 하고 싶어. 혹은 대기업에 지원하기 위해 트래픽을 다루는 경험이 필요해."
        />
      </section>

      {/* 🔥 변경점 4: 로딩 상태에 따라 실시간으로 변하는 LOADING_MESSAGES[loadingStep] 반영 */}
      <button className={styles.submitBtn} onClick={handleSubmit} disabled={isLoading}>
        {isLoading ? LOADING_MESSAGES[loadingStep] : "AI에게 추천받기"}
      </button>

      <div className={styles.resultArea}>
        {error && <p className={styles.errorText}>{error}</p>}
        {result?.recommendations?.length === 0 && !isLoading && !error && (
            <p className={styles.errorText}>현재 조건에 맞는 프로젝트가 없습니다. 조건을 조금 완화해 보세요!</p>
        )}
        <div className={styles.cardGrid}>
          {recommendedPosts.map((post) => (
            <PostCard key={post.id} post={post} onClick={() => navigate(`/post/${post.id}`)} isAiResult={true} />
          ))}
        </div>
      </div>
    </div>
  );
}