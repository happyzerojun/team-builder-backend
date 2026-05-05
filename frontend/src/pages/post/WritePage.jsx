import { useState, useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Navbar from "@/components/common/Navbar";
import { projectService } from "../../services/projectService";
import "./WritePage.css";

const REGIONS = [
    "서울", "경기", "인천", "부산", "대구", "광주", "대전", "울산",
    "세종", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주", "온라인"
];

const MONTH_OPTIONS = Array.from({ length: 24 }, (_, i) => i + 1);

const TECH_STACK_TABS = {
    "Frontend": ["React", "Vue", "Angular", "Next.js", "Svelte", "TypeScript", "JavaScript", "HTML/CSS", "Tailwind"],
    "Backend": ["Spring Boot", "Node.js", "Django", "FastAPI", "Flask", "Express", "NestJS", "Java", "Python", "Go", "Kotlin"],
    "Database": ["MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQLite", "Firebase"],
    "DevOps": ["AWS", "Docker", "Kubernetes", "CI/CD", "GCP", "Azure", "Nginx", "Linux"],
    "기타": ["Git", "Figma", "Unity", "Flutter", "React Native", "Swift", "Kotlin(Android)", "Kotlin(iOS)"],
};

const MAX_DETAIL_IMAGES = 5;
const MAX_FILE_SIZE_MB = 5;

function WritePage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = Boolean(id);

    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState("Frontend");

    // 이미지 상태: { file: File | null, preview: string }
    const [thumbnail, setThumbnail] = useState(null);
    const [thumbnailDrag, setThumbnailDrag] = useState(false);
    const [detailImages, setDetailImages] = useState([]);
    const thumbnailInputRef = useRef(null);
    const detailInputRef = useRef(null);

    const [form, setForm] = useState({
        title: "",
        content: "",
        region: "",
        term: "",
        tags: [],
    });

    useEffect(() => {
        const savedUser = JSON.parse(localStorage.getItem("user") || "{}");
        const token = localStorage.getItem("token");

        if (!token || !savedUser || (!savedUser.email && !savedUser.name)) {
            alert("로그인이 필요한 페이지입니다.");
            navigate("/login");
            return;
        }

        if (isEditMode) {
            const fetchProjectData = async () => {
                try {
                    setLoading(true);
                    const data = await projectService.getProjectById(id);
                    if (!data) {
                        alert("프로젝트 정보를 찾을 수 없습니다.");
                        navigate(-1);
                        return;
                    }
                    setForm({
                        title: data.title || "",
                        content: data.content || "",
                        region: data.region || "",
                        term: data.term || "",
                        tags: data.tags || [],
                    });
                    // 기존 이미지 URL이 내려올 경우 미리보기 세팅 (file은 null → 재전송 안 함)
                    if (data.thumbnailUrl) {
                        setThumbnail({ file: null, preview: data.thumbnailUrl });
                    }
                    if (data.detailImageUrls?.length) {
                        setDetailImages(data.detailImageUrls.map((url) => ({ file: null, preview: url })));
                    }
                } catch (error) {
                    console.error("프로젝트 정보 불러오기 실패:", error);
                    alert("프로젝트 정보를 불러오는데 실패했습니다.");
                    navigate(-1);
                } finally {
                    setLoading(false);
                }
            };
            fetchProjectData();
        }
    }, [id, isEditMode, navigate]);

    // ── 이미지 유효성 검사 ──────────────────────────────
    const validateImageFile = (file) => {
        if (!file.type.startsWith("image/")) {
            alert("이미지 파일만 업로드할 수 있습니다.");
            return false;
        }
        if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
            alert(`파일 크기는 ${MAX_FILE_SIZE_MB}MB 이하여야 합니다.`);
            return false;
        }
        return true;
    };

    // ── 대표 이미지 ──────────────────────────────────────
    const handleThumbnailFile = (file) => {
        if (!file || !validateImageFile(file)) return;
        setThumbnail({ file, preview: URL.createObjectURL(file) });
    };

    const handleThumbnailDrop = (e) => {
        e.preventDefault();
        setThumbnailDrag(false);
        handleThumbnailFile(e.dataTransfer.files[0]);
    };

    const handleThumbnailChange = (e) => {
        handleThumbnailFile(e.target.files[0]);
        e.target.value = "";
    };

    const removeThumbnail = () => {
        if (thumbnail?.file) URL.revokeObjectURL(thumbnail.preview);
        setThumbnail(null);
    };

    // ── 상세 이미지 ──────────────────────────────────────
    const handleDetailFiles = (files) => {
        const fileArr = Array.from(files);
        const available = MAX_DETAIL_IMAGES - detailImages.length;

        if (available <= 0) {
            alert(`상세 이미지는 최대 ${MAX_DETAIL_IMAGES}장까지 업로드할 수 있습니다.`);
            return;
        }

        const valid = fileArr.filter(validateImageFile);
        const toAdd = valid.slice(0, available).map((file) => ({
            file,
            preview: URL.createObjectURL(file),
        }));
        setDetailImages((prev) => [...prev, ...toAdd]);

        if (valid.length > available) {
            alert(`최대 ${MAX_DETAIL_IMAGES}장까지만 업로드됩니다. 일부 이미지가 제외되었습니다.`);
        }
    };

    const handleDetailChange = (e) => {
        handleDetailFiles(e.target.files);
        e.target.value = "";
    };

    const removeDetailImage = (index) => {
        setDetailImages((prev) => {
            const next = [...prev];
            if (next[index].file) URL.revokeObjectURL(next[index].preview);
            next.splice(index, 1);
            return next;
        });
    };

    // ── 폼 필드 ──────────────────────────────────────────
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleTagToggle = (tag) => {
        setForm((prev) => {
            if (prev.tags.includes(tag)) {
                return { ...prev, tags: prev.tags.filter((t) => t !== tag) };
            }
            if (prev.tags.length >= 10) {
                alert("기술 스택은 최대 10개까지 선택 가능합니다.");
                return prev;
            }
            return { ...prev, tags: [...prev.tags, tag] };
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        alert("현재 백엔드 미구현으로 실제 저장은 되지 않습니다.");
    };

    /*
    백엔드 구현 시 다시 살림
    // ── 제출 ─────────────────────────────────────────────
    const handleSubmit = async (e) => {

        e.preventDefault();

        if (!form.title.trim()) { alert("제목을 입력해주세요."); return; }
        if (!form.content.trim()) { alert("프로젝트 내용을 입력해주세요."); return; }
        if (!form.region) { alert("지역을 선택해주세요."); return; }
        if (!form.term) { alert("프로젝트 기간을 선택해주세요."); return; }

        // FormData로 구성 (이미지 포함)
        const formData = new FormData();
        formData.append("title", form.title.trim());
        formData.append("content", form.content.trim());
        formData.append("region", form.region);
        formData.append("term", Number(form.term));
        formData.append("status", "모집중");
        form.tags.forEach((tag) => formData.append("tags", tag));

        if (thumbnail?.file) formData.append("thumbnail", thumbnail.file);
        detailImages.forEach((img) => {
            if (img.file) formData.append("detailImages", img.file);
        });

        try {
            setLoading(true);
            if (isEditMode) {
                await projectService.updateProject(id, formData);
                alert("프로젝트 정보가 성공적으로 수정되었습니다.");
                navigate(`/post/${id}`);
            } else {
                const created = await projectService.createProject(formData);
                alert("모집 글이 성공적으로 등록되었습니다.");
                const newProjectId = created?.project_id || created?.id;
                navigate(newProjectId ? `/post/${newProjectId}` : "/");
            }
        } catch (error) {
            console.error("저장 실패:", error);
            alert("저장 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };
    */

    const months = Number(form.term);

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
                        {isEditMode
                            ? "프로젝트의 변경된 내용을 업데이트하세요."
                            : "프로젝트를 소개하고 함께할 팀원을 모집해보세요."}
                    </p>
                </div>

                <form className="write-form" onSubmit={handleSubmit}>

                    {/* ── 제목 ── */}
                    <div className="form-group">
                        <label className="form-label">프로젝트 제목 <span className="required">*</span></label>
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

                    {/* ── 소개 ── */}
                    <div className="form-group">
                        <label className="form-label">프로젝트 소개 <span className="required">*</span></label>
                        <textarea
                            name="content"
                            value={form.content}
                            onChange={handleChange}
                            placeholder="프로젝트 목적, 주요 기능, 일정 등을 자세히 설명해주세요."
                            rows={6}
                        />
                    </div>

                    {/* ── 지역 + 기간 ── */}
                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">진행 지역 <span className="required">*</span></label>
                            <select name="region" value={form.region} onChange={handleChange}>
                                <option value="">지역 선택</option>
                                {REGIONS.map((r) => <option key={r} value={r}>{r}</option>)}
                            </select>
                        </div>

                        <div className="form-group">
                            <label className="form-label">프로젝트 기간 <span className="required">*</span></label>
                            <div className="duration-month-wrapper">
                                <select name="term" value={form.term} onChange={handleChange} className="duration-select">
                                    <option value="">기간 선택</option>
                                    {MONTH_OPTIONS.map((m) => <option key={m} value={m}>{m}개월</option>)}
                                </select>
                                {months > 0 && (
                                    <span className="duration-badge">
                                        {months <= 3 ? "🟢 단기" : months <= 6 ? "🟡 중기" : "🔴 장기"}&nbsp;프로젝트
                                    </span>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* ── 기술 스택 ── */}
                    <div className="form-group">
                        <label className="form-label">
                            기술 스택
                            <span className="tag-count-hint"> (최대 10개 / 현재 {form.tags.length}개 선택됨)</span>
                        </label>
                        <div className="tech-tabs">
                            {Object.keys(TECH_STACK_TABS).map((tab) => (
                                <button
                                    key={tab}
                                    type="button"
                                    className={`tech-tab ${activeTab === tab ? "active" : ""}`}
                                    onClick={() => setActiveTab(tab)}
                                >
                                    {tab}
                                </button>
                            ))}
                        </div>
                        <div className="tech-options">
                            {TECH_STACK_TABS[activeTab].map((tech) => (
                                <button
                                    key={tech}
                                    type="button"
                                    className={`tech-option-btn ${form.tags.includes(tech) ? "selected" : ""}`}
                                    onClick={() => handleTagToggle(tech)}
                                >
                                    {tech}
                                </button>
                            ))}
                        </div>
                        {form.tags.length > 0 && (
                            <div className="selected-tags">
                                <span className="selected-tags-label">선택된 기술:</span>
                                {form.tags.map((tag) => (
                                    <span
                                        key={tag}
                                        className="selected-tag-badge"
                                        onClick={() => handleTagToggle(tag)}
                                        title="클릭하여 제거"
                                    >
                                        {tag} ×
                                    </span>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* ── 대표 이미지 ── */}
                    <div className="form-group">
                        <label className="form-label">
                            대표 이미지
                            <span className="tag-count-hint"> (썸네일 1장 · 최대 {MAX_FILE_SIZE_MB}MB)</span>
                        </label>

                        {thumbnail ? (
                            <div className="thumbnail-preview-wrapper">
                                <img src={thumbnail.preview} alt="대표 이미지 미리보기" className="thumbnail-preview" />
                                <button
                                    type="button"
                                    className="img-remove-btn thumbnail-remove-btn"
                                    onClick={removeThumbnail}
                                    title="이미지 제거"
                                >×</button>
                                <button
                                    type="button"
                                    className="thumbnail-change-btn"
                                    onClick={() => thumbnailInputRef.current?.click()}
                                >
                                    이미지 변경
                                </button>
                            </div>
                        ) : (
                            <div
                                className={`img-dropzone ${thumbnailDrag ? "drag-over" : ""}`}
                                onClick={() => thumbnailInputRef.current?.click()}
                                onDragOver={(e) => { e.preventDefault(); setThumbnailDrag(true); }}
                                onDragLeave={() => setThumbnailDrag(false)}
                                onDrop={handleThumbnailDrop}
                            >
                                <span className="dropzone-icon">🖼️</span>
                                <span className="dropzone-text">클릭하거나 이미지를 드래그하세요</span>
                                <span className="dropzone-subtext">PNG · JPG · WEBP · 최대 {MAX_FILE_SIZE_MB}MB</span>
                            </div>
                        )}

                        <input
                            ref={thumbnailInputRef}
                            type="file"
                            accept="image/*"
                            className="img-input-hidden"
                            onChange={handleThumbnailChange}
                        />
                    </div>

                    {/* ── 상세 이미지 ── */}
                    <div className="form-group">
                        <label className="form-label">
                            상세 이미지
                            <span className="tag-count-hint"> (최대 {MAX_DETAIL_IMAGES}장 · 현재 {detailImages.length}장)</span>
                        </label>

                        <div className="detail-images-grid">
                            {detailImages.map((img, index) => (
                                <div key={index} className="detail-img-item">
                                    <img src={img.preview} alt={`상세 이미지 ${index + 1}`} className="detail-img-preview" />
                                    <button
                                        type="button"
                                        className="img-remove-btn detail-remove-btn"
                                        onClick={() => removeDetailImage(index)}
                                        title="이미지 제거"
                                    >×</button>
                                </div>
                            ))}

                            {detailImages.length < MAX_DETAIL_IMAGES && (
                                <div
                                    className="detail-img-add"
                                    onClick={() => detailInputRef.current?.click()}
                                    title="이미지 추가"
                                >
                                    <span className="detail-add-icon">+</span>
                                    <span className="detail-add-text">이미지 추가</span>
                                </div>
                            )}
                        </div>

                        <input
                            ref={detailInputRef}
                            type="file"
                            accept="image/*"
                            multiple
                            className="img-input-hidden"
                            onChange={handleDetailChange}
                        />
                    </div>

                    {/* ── 제출 버튼 ── */}
                    <div className="form-actions">
                        <button type="button" className="btn-cancel" onClick={() => navigate(-1)} disabled={loading}>
                            취소
                        </button>
                        <button type="submit" className="btn-submit" disabled={loading}>
                            {loading ? "처리 중..." : isEditMode ? "수정 완료하기" : "모집 글 등록하기"}
                        </button>
                    </div>
                </form>
            </main>
        </div>
    );
}

export default WritePage;
