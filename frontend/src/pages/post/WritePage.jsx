import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Navbar from "@/components/common/Navbar";
import { projectService } from "../../services/projectService";
import "./WritePage.css";
import TagSelectDropdown from "@/components/filter/TagSelectDropdown";

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
    const { id } = useParams(); // URL에 id가 있으면 수정 모드
    const navigate = useNavigate();
    const isEditMode = Boolean(id);

    const [currentUser, setCurrentUser] = useState(null);
    const [tagDropdownOpen, setTagDropdownOpen] = useState(false);

    // 폼 상태 초기화
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

    // 데이터 로드 로직
    useEffect(() => {
        const savedUser = JSON.parse(localStorage.getItem("user"));
        if (!savedUser) {
            alert("로그인이 필요한 페이지입니다.");
            navigate("/login");
            return;
        }
        setCurrentUser(savedUser);

        // 수정 모드일 경우 기존 데이터 불러오기
        if (isEditMode) {
            const fetchProjectData = async () => {
                try {
                    const data = await projectService.getProjectById(id);
                    if (data) {
                        setForm({
                            ...data,
                            // 서버의 배열 데이터를 인풋창용 문자열로 변환 (예: ["기획", "디자인"] -> "기획, 디자인")
                            roles: Array.isArray(data.roles) ? data.roles.join(", ") : data.roles,
                        });
                    }
                } catch (error) {
                    console.error("데이터 로드 실패:", error);
                    alert("프로젝트 정보를 불러오는데 실패했습니다.");
                    navigate(-1);
                }
            };
            fetchProjectData();
        }
    }, [id, isEditMode, navigate]);

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

    async function handleSubmit(e) {
        e.preventDefault();

        // 필수 입력값 검증
        if (!form.title.trim()) { alert("제목을 입력해주세요."); return; }
        if (!form.description.trim()) { alert("프로젝트 설명을 입력해주세요."); return; }
        if (!form.category) { alert("카테고리를 선택해주세요."); return; }
        if (!form.roles.trim()) { alert("모집 분야를 입력해주세요."); return; }
        if (!form.durationMonths) { alert("프로젝트 기간을 선택해주세요."); return; }

        const months = Number(form.durationMonths);
        const authorName = currentUser?.name || currentUser?.nickname || "익명";

        // 서버 전송용 데이터 가공 (roles를 다시 배열로 변환)
        const commonData = {
            ...form,
            roles: typeof form.roles === 'string' 
                ? form.roles.split(',').map(r => r.trim()).filter(r => r !== "") 
                : form.roles,
            duration: months <= 3 ? "단기" : months <= 6 ? "중기" : "장기",
            author: authorName,
        };

        try {
            if (isEditMode) {
                // 수정 처리 (PUT)
                await projectService.updateProject(id, commonData);
                alert("✨ 프로젝트 정보가 성공적으로 수정되었습니다!");
                navigate(`/project/manage/${id}`); // 관리 페이지로 복귀
            } else {
                // 신규 등록 처리 (POST)
                const newData = {
                    ...commonData,
                    members: [
                        { 
                            id: Date.now(), 
                            name: authorName, 
                            role: "리더",
                            joinedAt: new Date().toLocaleDateString()
                        }
                    ],
                    applicants: [],
                    status: "recruiting",
                    createdAt: new Date().toLocaleDateString(),
                };
                await projectService.createProject(newData);
                alert("🚀 모집 글이 성공적으로 등록되었습니다!");
                navigate("/");
            }
        } catch (error) {
            console.error("저장 실패:", error);
            alert("저장 중 오류가 발생했습니다.");
        }
    }

    const months = Number(form.durationMonths);

    return (
        <div className="write-page">
            <Navbar />
            <main className="write-content">
                <button className="btn-back" onClick={() => navigate(-1)}>← 뒤로가기</button>

                <div className="write-header">
                    <h1 className="write-title">
                        {isEditMode ? "프로젝트 정보 수정" : "팀원 모집 글 작성"}
                    </h1>
                    <p className="write-subtitle">
                        {isEditMode ? "프로젝트의 변경된 내용을 업데이트하세요." : "멋진 팀원을 찾고 있나요? 프로젝트를 소개해주세요."}
                    </p>
                </div>

                <div className="write-form">
                    <div className="form-group">
                        <label className="form-label">프로젝트 제목 <span className="required">*</span></label>
                        <input
                            type="text" name="title" value={form.title} onChange={handleChange}
                            placeholder="예: AI 기반 일정 관리 앱 팀원 모집" maxLength={60}
                        />
                        <span className="char-count">{form.title.length} / 60</span>
                    </div>

                    <div className="form-group">
                        <label className="form-label">프로젝트 설명 <span className="required">*</span></label>
                        <textarea
                            name="description" value={form.description} onChange={handleChange}
                            placeholder="프로젝트 목적, 주요 기능, 일정 등을 자세히 설명해주세요." rows={5}
                        />
                    </div>

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
                    </div>

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

                    <div className="form-group">
                        <label className="form-label">모집 분야 <span className="required">*</span></label>
                        <input
                            type="text" name="roles" value={form.roles} onChange={handleChange}
                            placeholder="예: 프론트엔드 개발자, UI/UX 디자이너 (쉼표로 구분)"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">기술 스택 태그</label>
                        <TagSelectDropdown
                            selectedTags={form.tags}
                            onToggleTag={toggleTag}
                            isOpen={tagDropdownOpen}
                            onToggleOpen={() => setTagDropdownOpen((prev) => !prev)}
                        />
                    </div>

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
                            </div>
                        )}
                        {form.applyType === "google" && (
                            <div className="apply-link-input">
                                <input type="url" name="googleFormLink" value={form.googleFormLink} onChange={handleChange} placeholder="https://docs.google.com/forms/..." />
                            </div>
                        )}
                    </div>

                    <div className="form-actions">
                        <button type="button" className="btn-cancel" onClick={() => navigate(-1)}>취소</button>
                        <button type="button" className="btn-submit" onClick={handleSubmit}>
                            {isEditMode ? "수정 완료하기" : "모집 글 등록하기"}
                        </button>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default WritePage;