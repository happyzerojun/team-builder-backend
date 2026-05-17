import { useState } from "react";
import useAiRecommend from "../hooks/useAiRecommend";
import styles from "./AiRecommendPage.module.css";

const GRADE_OPTIONS = [
  { label: "1학년", value: "대학교 1학년" },
  { label: "2학년", value: "대학교 2학년" },
  { label: "3학년", value: "대학교 3학년" },
  { label: "4학년", value: "대학교 4학년" },
  { label: "취준생", value: "취업준비생" },
  { label: "주니어", value: "주니어 개발자 (1~3년)" },
  { label: "미들+", value: "미들 개발자 (3년 이상)" },
];

const TECH_OPTIONS = {
  프론트엔드: ["React", "Vue", "Next.js", "TypeScript", "JavaScript", "HTML/CSS"],
  백엔드: ["Spring Boot", "Node.js", "Django", "FastAPI", "MySQL", "MongoDB"],
  기타: ["iOS/Swift", "Android/Kotlin", "Flutter", "AI/ML", "Docker", "AWS"],
};

const DURATION_OPTIONS = [
  { label: "1개월", value: "1개월 이내" },
  { label: "1~2개월", value: "1~2개월" },
  { label: "2~3개월", value: "2~3개월" },
  { label: "3~6개월", value: "3~6개월" },
  { label: "6개월+", value: "6개월 이상" },
];

export default function AiRecommendPage() {
  const [grade, setGrade] = useState("");
  const [selectedTechs, setSelectedTechs] = useState([]);
  const [duration, setDuration] = useState("");
  const [freeText, setFreeText] = useState("");
  const [activeTab, setActiveTab] = useState("프론트엔드");

  const { result, isLoading, error, getRecommendation } = useAiRecommend();

  const toggleTech = (tech) => {
    setSelectedTechs((prev) =>
      prev.includes(tech) ? prev.filter((t) => t !== tech) : [...prev, tech]
    );
  };

  const handleSubmit = () => {
    if (!grade && selectedTechs.length === 0 && !duration && !freeText.trim()) {
      alert("학년, 기술스택, 기간 중 하나 이상 선택해주세요.");
      return;
    }
    getRecommendation({ grade, selectedTechs, duration, freeText });
  };

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>AI 프로젝트 추천</h1>
        <p className={styles.subtitle}>
          내 상황을 알려주면 딱 맞는 사이드 프로젝트를 찾아드려요.
        </p>
      </div>

      {/* 학년/경력 */}
      <section className={styles.section}>
        <span className={styles.label}>학년 / 경력</span>
        <div className={styles.chips}>
          {GRADE_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              className={`${styles.chip} ${grade === opt.value ? styles.chipOn : ""}`}
              onClick={() => setGrade(grade === opt.value ? "" : opt.value)}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </section>

      {/* 기술스택 */}
      <section className={styles.section}>
        <span className={styles.label}>보유 기술스택</span>
        <div className={styles.tabs}>
          {Object.keys(TECH_OPTIONS).map((tab) => (
            <button
              key={tab}
              className={`${styles.tab} ${activeTab === tab ? styles.tabOn : ""}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab}
            </button>
          ))}
        </div>
        <div className={styles.chips}>
          {TECH_OPTIONS[activeTab].map((tech) => (
            <button
              key={tech}
              className={`${styles.chip} ${selectedTechs.includes(tech) ? styles.chipOn : ""}`}
              onClick={() => toggleTech(tech)}
            >
              {tech}
            </button>
          ))}
        </div>
      </section>

      {/* 참여 가능 기간 */}
      <section className={styles.section}>
        <span className={styles.label}>참여 가능 기간</span>
        <div className={styles.chips}>
          {DURATION_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              className={`${styles.chip} ${duration === opt.value ? styles.chipOn : ""}`}
              onClick={() => setDuration(duration === opt.value ? "" : opt.value)}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </section>

      {/* 자유 텍스트 */}
      <section className={styles.section}>
        <span className={styles.label}>추가로 하고 싶은 말 (선택)</span>
        <textarea
          className={styles.textarea}
          placeholder="예) 백엔드도 배워보고 싶어요. 포트폴리오에 쓸 수 있는 프로젝트 위주로 추천해주세요."
          value={freeText}
          onChange={(e) => setFreeText(e.target.value)}
          rows={3}
        />
      </section>

      {/* 선택 미리보기 */}
      {(grade || selectedTechs.length > 0 || duration) && (
        <div className={styles.summaryBar}>
          {[grade, ...selectedTechs, duration]
            .filter(Boolean)
            .map((v) => (
              <span key={v} className={styles.tag}>{v}</span>
            ))}
        </div>
      )}

      <button
        className={styles.submitBtn}
        onClick={handleSubmit}
        disabled={isLoading}
      >
        {isLoading ? "추천 찾는 중..." : "AI에게 추천받기"}
      </button>

      {/* 결과 */}
      {(isLoading || result || error) && (
        <div className={styles.resultArea}>
          {isLoading && (
            <p className={styles.loadingText}>
              <span className={styles.dot} />
              <span className={styles.dot} />
              <span className={styles.dot} />
              추천 프로젝트 찾는 중...
            </p>
          )}
          {error && <p className={styles.errorText}>{error}</p>}
          {result && <p className={styles.resultText}>{result}</p>}
        </div>
      )}
    </div>
  );
}
