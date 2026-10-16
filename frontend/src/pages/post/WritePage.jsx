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
    Frontend: [
        { id: 1, name: "React" },
        { id: 2, name: "Vue" },
        { id: 3, name: "Angular" },
        { id: 4, name: "Next.js" },
        { id: 5, name: "Svelte" },
        { id: 6, name: "TypeScript" },
        { id: 7, name: "JavaScript" },
        { id: 8, name: "HTML/CSS" },
        { id: 9, name: "Tailwind" }
    ],
    Backend: [
        { id: 10, name: "Spring Boot" },
        { id: 11, name: "Node.js" },
        { id: 12, name: "Django" },
        { id: 13, name: "FastAPI" },
        { id: 14, name: "Flask" },
        { id: 15, name: "Express" },
        { id: 16, name: "NestJS" },
        { id: 17, name: "Java" },
        { id: 18, name: "Python" },
        { id: 19, name: "Go" },
        { id: 20, name: "Kotlin" }
    ],
    Database: [
        { id: 21, name: "MySQL" },
        { id: 22, name: "PostgreSQL" },
        { id: 23, name: "MongoDB" },
        { id: 24, name: "Redis" },
        { id: 25, name: "Oracle" },
        { id: 26, name: "SQLite" },
        { id: 27, name: "Firebase" }
    ],
    DevOps: [
        { id: 28, name: "AWS" },
        { id: 29, name: "Docker" },
        { id: 30, name: "Kubernetes" },
        { id: 31, name: "CI/CD" },
        { id: 32, name: "GCP" },
        { id: 33, name: "Azure" },
        { id: 34, name: "Nginx" },
        { id: 35, name: "Linux" }
    ],
    기타: [
        { id: 36, name: "Git" },
        { id: 37, name: "Figma" },
        { id: 38, name: "Unity" },
        { id: 39, name: "Flutter" },
        { id: 40, name: "React Native" },
        { id: 41, name: "Swift" },
        { id: 42, name: "Kotlin(Android)" },
        { id: 43, name: "Kotlin(iOS)" }
    ],
};

// 모집 포지션 (TS-19): 지원서의 지원 역할(support_role)이 포지션 이름과 같아야 그 포지션의 정원을 쓴다
const MAX_POSITIONS = 10;
const MAX_POSITION_COUNT = 100;
const POSITION_PRESETS = ["프론트엔드", "백엔드", "디자이너", "기획/PM", "모바일", "AI/데이터"];
const LEADER_ROLE = "팀장"; // 서버가 작성자 자리로 쓰는 이름이라 포지션 이름으로 쓸 수 없다

const MAX_DETAIL_IMAGES = 5;
const MAX_FILE_SIZE_MB = 5;

function WritePage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = Boolean(id);

    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState("Frontend");

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
        techStackIds: [],
        meetingType: "비대면",
        isLocalOnly: false,
        // { roleName, requiredCount, currentCount } — currentCount는 수정 화면에서만 의미가 있다(합류 인원)
        positions: [],
    });

    const allTechStacks = Object.values(TECH_STACK_TABS).flat();

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
                        techStackIds: Array.isArray(data.techStacks)
                            ? data.techStacks.map((tech) => tech.tech_stack_id)
                            : [],
                        meetingType: data.meetingType || "비대면",
                        isLocalOnly: data.isLocalOnly ?? false,
                        positions: Array.isArray(data.positions)
                            ? data.positions.map((position) => ({
                                roleName: position.roleName || "",
                                requiredCount: position.requiredCount ?? 1,
                                currentCount: position.currentCount ?? 0,
                            }))
                            : [],
                    });

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

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleTechStackToggle = (techId) => {
        setForm((prev) => {
            if (prev.techStackIds.includes(techId)) {
                return {
                    ...prev,
                    techStackIds: prev.techStackIds.filter((id) => id !== techId),
                };
            }

            if (prev.techStackIds.length >= 10) {
                alert("기술 스택은 최대 10개까지 선택 가능합니다.");
                return prev;
            }

            return {
                ...prev,
                techStackIds: [...prev.techStackIds, techId],
            };
        });
    };

    const addPosition = (roleName = "") => {
        setForm((prev) => {
            if (prev.positions.length >= MAX_POSITIONS) {
                alert(`포지션은 최대 ${MAX_POSITIONS}개까지 등록할 수 있습니다.`);
                return prev;
            }
            if (roleName && prev.positions.some((position) => position.roleName.trim() === roleName)) {
                return prev;
            }
            return {
                ...prev,
                positions: [...prev.positions, { roleName, requiredCount: 1, currentCount: 0 }],
            };
        });
    };

    const updatePosition = (index, field, value) => {
        setForm((prev) => ({
            ...prev,
            positions: prev.positions.map((position, i) =>
                i === index ? { ...position, [field]: value } : position
            ),
        }));
    };

    const removePosition = (index) => {
        setForm((prev) => ({
            ...prev,
            positions: prev.positions.filter((_, i) => i !== index),
        }));
    };

    // 서버 규칙과 같은 검사를 먼저 해서 알림으로 안내한다 (서버도 같은 규칙으로 400/409를 돌려준다)
    const validatePositions = () => {
        const names = new Set();
        for (const position of form.positions) {
            const name = position.roleName.trim();
            const count = Number(position.requiredCount);
            if (!name) return "포지션 이름을 입력해주세요.";
            if (name.length > 30) return "포지션 이름은 30자 이하로 입력해주세요.";
            if (name === LEADER_ROLE) return `'${LEADER_ROLE}'은(는) 포지션 이름으로 쓸 수 없습니다.`;
            if (names.has(name)) return "같은 이름의 포지션을 두 번 등록할 수 없습니다.";
            names.add(name);
            if (!Number.isInteger(count) || count < 1 || count > MAX_POSITION_COUNT) {
                return `모집 인원은 1~${MAX_POSITION_COUNT}명으로 입력해주세요.`;
            }
            if (count < (position.currentCount || 0)) {
                return `'${name}' 포지션은 이미 ${position.currentCount}명이 합류해 그보다 적게 줄일 수 없습니다.`;
            }
        }
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!form.title.trim()) {
            alert("제목을 입력해주세요.");
            return;
        }

        if (!form.content.trim()) {
            alert("프로젝트 내용을 입력해주세요.");
            return;
        }

        if (!form.region) {
            alert("지역을 선택해주세요.");
            return;
        }

        if (!form.term) {
            alert("프로젝트 기간을 선택해주세요.");
            return;
        }

        const positionError = validatePositions();
        if (positionError) {
            alert(positionError);
            return;
        }

        try {
            setLoading(true);

            const projectPayload = {
                title: form.title.trim(),
                content: form.content.trim(),
                region: form.region,
                term: Number(form.term),
                techStackIds: form.techStackIds,
                meetingType: form.meetingType,
                isLocalOnly: form.meetingType === "대면" ? form.isLocalOnly : false,
                // 수정 화면은 항상 목록을 보낸다(빈 목록 = 포지션 모두 삭제, 서버가 합류 인원이 있으면 409)
                positions: form.positions.map((position) => ({
                    roleName: position.roleName.trim(),
                    requiredCount: Number(position.requiredCount),
                })),
            };

            let savedProject;

            if (isEditMode) {
                savedProject = await projectService.updateProject(id, projectPayload);
                alert("프로젝트 정보가 성공적으로 수정되었습니다.");
                navigate("/");
            } else {
                    console.log("전송 payload:", projectPayload);
                    // 작성자를 팀원(팀장)으로 등록하는 것은 서버가 글 생성과 같은 트랜잭션에서 처리한다
                    savedProject = await projectService.createProject(projectPayload);

    alert("모집 글이 성공적으로 등록되었습니다.");
    navigate("/");
}
        } catch (error) {
            console.error("저장 실패:", error);
            alert(error?.response?.data?.message || "저장 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const months = Number(form.term);

    return (
        <div className="write-page">
            <Navbar />

            <main className="write-content">
                <button className="btn-back" onClick={() => navigate(-1)}>
                    ← 뒤로가기
                </button>

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

                    <div className="form-group">
                        <label className="form-label">
                            프로젝트 소개 <span className="required">*</span>
                        </label>
                        <textarea
                            name="content"
                            value={form.content}
                            onChange={handleChange}
                            placeholder="프로젝트 목적, 주요 기능, 일정 등을 자세히 설명해주세요."
                            rows={6}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">
                                진행 지역 <span className="required">*</span>
                            </label>
                            <select name="region" value={form.region} onChange={handleChange}>
                                <option value="">지역 선택</option>
                                {REGIONS.map((region) => (
                                    <option key={region} value={region}>
                                        {region}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label className="form-label">
                                프로젝트 기간 <span className="required">*</span>
                            </label>
                            <div className="duration-month-wrapper">
                                <select
                                    name="term"
                                    value={form.term}
                                    onChange={handleChange}
                                    className="duration-select"
                                >
                                    <option value="">기간 선택</option>
                                    {MONTH_OPTIONS.map((month) => (
                                        <option key={month} value={month}>
                                            {month}개월
                                        </option>
                                    ))}
                                </select>

                                {months > 0 && (
                                    <span className="duration-badge">
                                        {months <= 3
                                            ? "🟢 단기"
                                            : months <= 6
                                            ? "🟡 중기"
                                            : "🔴 장기"}
                                        &nbsp;프로젝트
                                    </span>
                                )}
                            </div>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">
                            모임 방식 <span className="required">*</span>
                        </label>

                        <div className="meeting-type-options">
                            <button
                                type="button"
                                className={`meeting-option-btn ${form.meetingType === "비대면" ? "selected" : ""}`}
                                onClick={() =>
                                    setForm((prev) => ({
                                        ...prev,
                                        meetingType: "비대면",
                                        isLocalOnly: false,
                                    }))
                                }
                            >
                                💻 비대면
                            </button>

                            <button
                                type="button"
                                className={`meeting-option-btn ${form.meetingType === "대면" ? "selected" : ""}`}
                                onClick={() =>
                                    setForm((prev) => ({
                                        ...prev,
                                        meetingType: "대면",
                                    }))
                                }
                            >
                                🤝 대면
                            </button>
                        </div>

                        {form.meetingType === "대면" && (
                            <div className="local-only-box">
                                <span className="local-only-label">지역 제한</span>

                                <button
                                    type="button"
                                    className={`local-option-btn ${form.isLocalOnly === true ? "selected" : ""}`}
                                    onClick={() =>
                                        setForm((prev) => ({
                                            ...prev,
                                            isLocalOnly: true,
                                        }))
                                    }
                                >
                                    📍 해당 지역만
                                </button>

                                <button
                                    type="button"
                                    className={`local-option-btn ${form.isLocalOnly === false ? "selected" : ""}`}
                                    onClick={() =>
                                        setForm((prev) => ({
                                            ...prev,
                                            isLocalOnly: false,
                                        }))
                                    }
                                >
                                    🌍 타지역도 가능
                                </button>
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <label className="form-label">
                            모집 포지션
                            <span className="tag-count-hint">
                                {" "}(선택 · 최대 {MAX_POSITIONS}개 / 현재 {form.positions.length}개)
                            </span>
                        </label>
                        <p className="form-hint">
                            포지션을 정하면 지원자는 포지션을 골라 지원하고, 정원이 차면 더 수락할 수 없어요.
                            비워 두면 인원 제한 없이 받습니다.
                        </p>

                        <div className="position-presets">
                            {POSITION_PRESETS.map((preset) => (
                                <button
                                    key={preset}
                                    type="button"
                                    className={`tech-option-btn ${
                                        form.positions.some((position) => position.roleName.trim() === preset) ? "selected" : ""
                                    }`}
                                    onClick={() => addPosition(preset)}
                                >
                                    + {preset}
                                </button>
                            ))}
                            <button type="button" className="tech-option-btn" onClick={() => addPosition("")}>
                                + 직접 입력
                            </button>
                        </div>

                        {form.positions.length > 0 && (
                            <div className="position-rows">
                                {form.positions.map((position, index) => {
                                    const joined = position.currentCount || 0;
                                    return (
                                        <div key={index} className="position-row">
                                            <input
                                                type="text"
                                                className="position-name-input"
                                                value={position.roleName}
                                                onChange={(e) => updatePosition(index, "roleName", e.target.value)}
                                                placeholder="역할 이름 (예: 백엔드)"
                                                maxLength={30}
                                                aria-label={`포지션 ${index + 1} 이름`}
                                                disabled={joined > 0}
                                                title={joined > 0 ? "합류한 팀원이 있어 이름을 바꿀 수 없습니다." : undefined}
                                            />
                                            <input
                                                type="number"
                                                className="position-count-input"
                                                value={position.requiredCount}
                                                min={Math.max(1, joined)}
                                                max={MAX_POSITION_COUNT}
                                                onChange={(e) => updatePosition(index, "requiredCount", e.target.value)}
                                                aria-label={`포지션 ${index + 1} 모집 인원`}
                                            />
                                            <span className="position-count-unit">명</span>
                                            {isEditMode && joined > 0 && (
                                                <span className="position-joined">{joined}명 합류</span>
                                            )}
                                            <button
                                                type="button"
                                                className="position-remove-btn"
                                                onClick={() => removePosition(index)}
                                                disabled={joined > 0}
                                                title={joined > 0 ? "합류한 팀원이 있어 삭제할 수 없습니다." : "포지션 삭제"}
                                            >
                                                ×
                                            </button>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <label className="form-label">
                            기술 스택
                            <span className="tag-count-hint">
                                {" "}(최대 10개 / 현재 {form.techStackIds.length}개 선택됨)
                            </span>
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
                                    key={tech.id}
                                    type="button"
                                    className={`tech-option-btn ${
                                        form.techStackIds.includes(tech.id) ? "selected" : ""
                                    }`}
                                    onClick={() => handleTechStackToggle(tech.id)}
                                >
                                    {tech.name}
                                </button>
                            ))}
                        </div>

                        {form.techStackIds.length > 0 && (
                            <div className="selected-tags">
                                <span className="selected-tags-label">선택된 기술:</span>
                                {form.techStackIds.map((techId) => {
                                    const tech = allTechStacks.find((item) => item.id === techId);

                                    return (
                                        <span
                                            key={techId}
                                            className="selected-tag-badge"
                                            onClick={() => handleTechStackToggle(techId)}
                                            title="클릭하여 제거"
                                        >
                                            {tech?.name || techId} ×
                                        </span>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <label className="form-label">
                            대표 이미지
                            <span className="tag-count-hint">
                                {" "}(썸네일 1장 · 최대 {MAX_FILE_SIZE_MB}MB)
                            </span>
                        </label>

                        {thumbnail ? (
                            <div className="thumbnail-preview-wrapper">
                                <img
                                    src={thumbnail.preview}
                                    alt="대표 이미지 미리보기"
                                    className="thumbnail-preview"
                                />
                                <button
                                    type="button"
                                    className="img-remove-btn thumbnail-remove-btn"
                                    onClick={removeThumbnail}
                                    title="이미지 제거"
                                >
                                    ×
                                </button>
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
                                onDragOver={(e) => {
                                    e.preventDefault();
                                    setThumbnailDrag(true);
                                }}
                                onDragLeave={() => setThumbnailDrag(false)}
                                onDrop={handleThumbnailDrop}
                            >
                                <span className="dropzone-icon">🖼️</span>
                                <span className="dropzone-text">클릭하거나 이미지를 드래그하세요</span>
                                <span className="dropzone-subtext">
                                    PNG · JPG · WEBP · 최대 {MAX_FILE_SIZE_MB}MB
                                </span>
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

                    <div className="form-group">
                        <label className="form-label">
                            상세 이미지
                            <span className="tag-count-hint">
                                {" "}(최대 {MAX_DETAIL_IMAGES}장 · 현재 {detailImages.length}장)
                            </span>
                        </label>

                        <div className="detail-images-grid">
                            {detailImages.map((img, index) => (
                                <div key={index} className="detail-img-item">
                                    <img
                                        src={img.preview}
                                        alt={`상세 이미지 ${index + 1}`}
                                        className="detail-img-preview"
                                    />
                                    <button
                                        type="button"
                                        className="img-remove-btn detail-remove-btn"
                                        onClick={() => removeDetailImage(index)}
                                        title="이미지 제거"
                                    >
                                        ×
                                    </button>
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

                    <div className="form-actions">
                        <button
                            type="button"
                            className="btn-cancel"
                            onClick={() => navigate(-1)}
                            disabled={loading}
                        >
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