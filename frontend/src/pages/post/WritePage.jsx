// 📁 src/pages/WritePage.jsx

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "@/components/common/Navbar";
import "./WritePage.css";

const CATEGORIES = ["웹 개발", "앱 개발", "AI/ML", "게임 개발", "기타"];
const AVAILABLE_TAGS = [
  "React", "Vue.js", "Next.js", "TypeScript", "JavaScript",
  "Node.js", "Spring Boot", "Django", "Express", "FastAPI",
  "React Native", "Flutter", "Swift", "Kotlin",
  "MySQL", "MongoDB", "PostgreSQL", "Firebase", "Redis",
  "Docker", "AWS", "Git", "Tailwind", "Python",
];
const MONTH_OPTIONS = Array.from({ length: 24 }, (_, i) => i + 1);

function WritePage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    title: "",
    description: "",
    category: "",
    roles: "",
    headcount: 1,
    tags: [],
    durationMonths: "",
    applyType: "kakao",
    kakaoLink: "",
    googleFormLink: "",
  });

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function toggleTag(tag) {
    setForm((prev) => ({
      ...prev,
      tags: prev.tags.includes(tag)
        ? prev.tags.filter((t) => t !== tag)
        : [...prev.tags, tag],
    }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    if (!form.title.trim()) { alert("제목을 입력해주세요."); return; }
    if (!form.description.trim()) { alert("프로젝트 설명을 입력해주세요."); return; }
    if (!form.category) { alert("카테고리를 선택해주세요."); return; }
    if (!form.roles.trim()) { alert("모집 분야를 입력해주세요."); return; }
    if (!form.durationMonths) { alert("프로젝트 기간을 선택해주세요."); return; }
    if (form.applyType === "kakao" && !form.kakaoLink.trim()) {
      alert("카카오 오픈채팅방 링크를 입력해주세요."); return;
    }
    if (form.applyType === "google" && !form.googleFormLink.trim()) {
      alert("구글폼 신청서 링크를 입력해주세요."); return;
    }
    console.log("제출된 데이터:", form);
    alert("모집 글이 등록되었습니다! (현재는 Mock 데이터로 동작)");
    navigate("/");
  }

  const months = Number(form.durationMonths);

  return (
    <div className="write-page">
      <Navbar />
      <main className="write-content">
        <button className="btn-back" onClick={() => navigate(-1)}>← 뒤로가기</button>

        <div className="write-header">
          <h1 className="write-title">팀원 모집 글 작성</h1>
          <p className="write-subtitle">멋진 팀원을 찾고 있나요? 프로젝트를 소개해주세요.</p>
        </div>

        <div className="write-form">

          {/* 프로젝트 제목 */}
          <div className="form-group">
            <label className="form-label">프로젝트 제목 <span className="required">*</span></label>
            <input
              type="text" name="title" value={form.title} onChange={handleChange}
              placeholder="예: AI 기반 일정 관리 앱 팀원 모집" maxLength={60}
            />
            <span className="char-count">{form.title.length} / 60</span>
          </div>

          {/* 프로젝트 설명 */}
          <div className="form-group">
            <label className="form-label">프로젝트 설명 <span className="required">*</span></label>
            <textarea
              name="description" value={form.description} onChange={handleChange}
              placeholder="프로젝트 목적, 주요 기능, 일정 등을 자세히 설명해주세요." rows={5}
            />
          </div>

          {/* ✅ 프로젝트 기간 - 개월 수 드롭다운 */}
          <div className="form-group">
            <label className="form-label">프로젝트 기간 <span className="required">*</span></label>
            <div className="duration-month-wrapper">
              <select
                name="durationMonths"
                value={form.durationMonths}
                onChange={handleChange}
                className="duration-select"
              >
                <option value="">기간 선택</option>
                {MONTH_OPTIONS.map((m) => (
                  <option key={m} value={m}>{m}개월</option>
                ))}
              </select>
              {months > 0 && (
                <span className="duration-badge">
                  {months <= 3 ? "🟢 단기" : months <= 6 ? "🟡 중기" : "🔴 장기"}
                  &nbsp;프로젝트
                </span>
              )}
            </div>
            <span className="form-hint">1~3개월 단기 · 4~6개월 중기 · 7개월 이상 장기</span>
          </div>

          {/* 카테고리 + 모집 인원 */}
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">카테고리 <span className="required">*</span></label>
              <select name="category" value={form.category} onChange={handleChange}>
                <option value="">카테고리 선택</option>
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">모집 인원</label>
              <input type="number" name="headcount" value={form.headcount} onChange={handleChange} min={1} max={20} />
            </div>
          </div>

          {/* 모집 분야 */}
          <div className="form-group">
            <label className="form-label">모집 분야 <span className="required">*</span></label>
            <input
              type="text" name="roles" value={form.roles} onChange={handleChange}
              placeholder="예: 프론트엔드 개발자, UI/UX 디자이너 (쉼표로 구분)"
            />
            <span className="form-hint">여러 분야는 쉼표(,)로 구분해주세요.</span>
          </div>

          {/* 기술 스택 */}
          <div className="form-group">
            <label className="form-label">기술 스택 태그</label>
            <div className="tag-selector">
              {AVAILABLE_TAGS.map((tag) => (
                <button key={tag} type="button"
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

          {/* 지원 방식 */}
          <div className="form-group">
            <label className="form-label">지원 방식 <span className="required">*</span></label>
            <div className="apply-type-selector">
              <label className={`apply-type-card ${form.applyType === "kakao" ? "active" : ""}`}>
                <input type="radio" name="applyType" value="kakao" checked={form.applyType === "kakao"} onChange={handleChange} style={{ display: "none" }} />
                <span className="apply-type-icon">💬</span>
                <span className="apply-type-label">카카오 오픈채팅</span>
              </label>
              <label className={`apply-type-card ${form.applyType === "google" ? "active" : ""}`}>
                <input type="radio" name="applyType" value="google" checked={form.applyType === "google"} onChange={handleChange} style={{ display: "none" }} />
                <span className="apply-type-icon">📋</span>
                <span className="apply-type-label">구글폼 신청서</span>
              </label>
            </div>
            {form.applyType === "kakao" && (
              <div className="apply-link-input">
                <input type="url" name="kakaoLink" value={form.kakaoLink} onChange={handleChange} placeholder="https://open.kakao.com/o/..." />
                <span className="form-hint">카카오 오픈채팅방 링크를 입력해주세요.</span>
              </div>
            )}
            {form.applyType === "google" && (
              <div className="apply-link-input">
                <input type="url" name="googleFormLink" value={form.googleFormLink} onChange={handleChange} placeholder="https://docs.google.com/forms/..." />
                <span className="form-hint">구글폼 신청서 링크를 입력해주세요.</span>
              </div>
            )}
          </div>

          {/* 제출 */}
          <div className="form-actions">
            <button type="button" className="btn-cancel" onClick={() => navigate("/")}>취소</button>
            <button type="button" className="btn-submit" onClick={handleSubmit}>모집 글 등록하기</button>
          </div>

        </div>
      </main>
    </div>
  );
}

export default WritePage;
