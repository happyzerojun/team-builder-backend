// 📁 src/pages/WritePage.jsx
// 팀원 모집 글을 작성하는 페이지입니다.
//
// 사용하는 React 개념:
//   - useState: 폼 입력값들을 상태로 관리
//   - useNavigate: 제출 후 메인 페이지로 이동

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "@/components/common/Navbar";
import "./WritePage.css";

// 선택 가능한 카테고리와 기술 스택 목록
const CATEGORIES = ["웹 개발", "앱 개발", "AI/ML", "게임 개발", "기타"];
const AVAILABLE_TAGS = [
  "React", "Vue.js", "Next.js", "TypeScript", "JavaScript",
  "Node.js", "Spring Boot", "Django", "Express", "FastAPI",
  "React Native", "Flutter", "Swift", "Kotlin",
  "MySQL", "MongoDB", "PostgreSQL", "Firebase", "Redis",
  "Docker", "AWS", "Git", "Tailwind", "Python",
];

function WritePage() {
  const navigate = useNavigate();

  // ── 폼 상태 정의 ──────────────────────────────────────────
  // 여러 입력 필드를 객체 하나로 관리합니다.
  const [form, setForm] = useState({
    title: "",
    description: "",
    category: "",
    roles: "",        // 쉼표로 구분한 텍스트 입력
    headcount: 1,
    tags: [],         // 선택된 태그 배열
  });

  // ── 일반 입력 필드 변경 핸들러 ──────────────────────────────
  // input/select/textarea의 name 속성과 상태 key를 맞춰서
  // 하나의 함수로 모든 필드를 처리합니다.
  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  // ── 태그 토글 ──────────────────────────────────────────────
  function toggleTag(tag) {
    setForm((prev) => ({
      ...prev,
      tags: prev.tags.includes(tag)
        ? prev.tags.filter((t) => t !== tag)
        : [...prev.tags, tag],
    }));
  }

  // ── 폼 제출 핸들러 ─────────────────────────────────────────
  function handleSubmit(e) {
    e.preventDefault(); // 기본 form submit 동작(페이지 새로고침) 방지

    // 유효성 검사
    if (!form.title.trim()) { alert("제목을 입력해주세요."); return; }
    if (!form.description.trim()) { alert("프로젝트 설명을 입력해주세요."); return; }
    if (!form.category) { alert("카테고리를 선택해주세요."); return; }
    if (!form.roles.trim()) { alert("모집 분야를 입력해주세요."); return; }

    // 현재는 콘솔 출력 (나중에 API 호출로 교체)
    console.log("제출된 데이터:", form);
    alert("모집 글이 등록되었습니다! (현재는 Mock 데이터로 동작)");
    navigate("/"); // 메인 페이지로 이동
  }

  return (
    <div className="write-page">
      <Navbar />

      <main className="write-content">

        {/* 뒤로가기 버튼 */}
        <button className="btn-back" onClick={() => navigate(-1)}>
          ← 뒤로가기
        </button>

        <div className="write-header">
          <h1 className="write-title">팀원 모집 글 작성</h1>
          <p className="write-subtitle">멋진 팀원을 찾고 있나요? 프로젝트를 소개해주세요.</p>
        </div>

        {/* form 태그 대신 div + 직접 submit 버튼 처리 */}
        <div className="write-form">

          {/* 프로젝트 제목 */}
          <div className="form-group">
            <label className="form-label">
              프로젝트 제목 <span className="required">*</span>
            </label>
            <input
              type="text"
              name="title"
              value={form.title}
              onChange={handleChange}
              placeholder="예: AI 기반 일정 관리 앱 팀원 모집"
              maxLength={60}
            />
            <span className="char-count">{form.title.length} / 60</span>
          </div>

          {/* 프로젝트 설명 */}
          <div className="form-group">
            <label className="form-label">
              프로젝트 설명 <span className="required">*</span>
            </label>
            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              placeholder="프로젝트 목적, 주요 기능, 일정 등을 자세히 설명해주세요."
              rows={5}
            />
          </div>

          <div className="form-group">
            <label className="form-label">프로젝트 기간 <span className="required">*</span></label>
            <div className="period-selector">
              <label className="radio-label">
                <input
                  type="radio"
                  name="period"
                  value="단기"
                  checked={form.period === "단기"}
                  onChange={handleChange}
                />
                단기
              </label>
              <label className="radio-label">
                <input
                  type="radio"
                  name="period"
                  value="장기"
                  checked={form.period === "장기"}
                  onChange={handleChange}
                />
                장기
              </label>
            </div>
          </div>

          {/* 두 칸 나란히: 카테고리 + 모집 인원 */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">
                카테고리 <span className="required">*</span>
              </label>
              <select name="category" value={form.category} onChange={handleChange}>
                <option value="">카테고리 선택</option>
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">모집 인원</label>
              <input
                type="number"
                name="headcount"
                value={form.headcount}
                onChange={handleChange}
                min={1}
                max={20}
              />
            </div>
          </div>

          {/* 모집 분야 */}
          <div className="form-group">
            <label className="form-label">
              모집 분야 <span className="required">*</span>
            </label>
            <input
              type="text"
              name="roles"
              value={form.roles}
              onChange={handleChange}
              placeholder="예: 프론트엔드 개발자, UI/UX 디자이너 (쉼표로 구분)"
            />
            <span className="form-hint">여러 분야는 쉼표(,)로 구분해주세요.</span>
          </div>

          {/* 기술 스택 태그 선택 */}
          <div className="form-group">
            <label className="form-label">기술 스택 태그</label>
            <div className="tag-selector">
              {AVAILABLE_TAGS.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  className={`tag-option ${form.tags.includes(tag) ? "selected" : ""}`}
                  onClick={() => toggleTag(tag)}
                >
                  {tag}
                </button>
              ))}
            </div>
            {form.tags.length > 0 && (
              <div className="selected-tags">
                <span className="form-hint">선택됨: </span>
                {form.tags.map((tag) => (
                  <span key={tag} className="selected-tag-badge">{tag}</span>
                ))}
              </div>
            )}
          </div>

          {/* 제출 버튼 */}
          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={() => navigate("/")}
            >
              취소
            </button>
            <button
              type="button"
              className="btn-submit"
              onClick={handleSubmit}
            >
              모집 글 등록하기
            </button>
          </div>

        </div>
      </main>
    </div>
  );
}

export default WritePage;
