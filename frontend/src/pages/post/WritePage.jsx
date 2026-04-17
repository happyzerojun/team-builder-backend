import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Navbar from "@/components/common/Navbar";
import { projectService } from "../../services/projectService";
import "./WritePage.css";

const REGIONS = [
    "서울",
    "경기",
    "인천",
    "부산",
    "대구",
    "광주",
    "대전",
    "울산",
    "세종",
    "강원",
    "충북",
    "충남",
    "전북",
    "전남",
    "경북",
    "경남",
    "제주",
    "온라인"
];

const MONTH_OPTIONS = Array.from({ length: 24 }, (_, i) => i + 1);

function WritePage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const isEditMode = Boolean(id);

    const [loading, setLoading] = useState(false);

    const [form, setForm] = useState({
        title: "",
        content: "",
        region: "",
        term: "",
        status: "모집중",
    });

    useEffect(() => {
        const savedUser = JSON.parse(localStorage.getItem("user") || "{}");

        if (!savedUser?.user_id) {
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
                        status: data.status || "모집중",
                    });
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

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
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

        const submitData = {
            title: form.title.trim(),
            content: form.content.trim(),
            region: form.region,
            term: Number(form.term),
            status: form.status || "모집중",
        };

        try {
            setLoading(true);

            if (isEditMode) {
                await projectService.updateProject(id, submitData);
                alert("프로젝트 정보가 성공적으로 수정되었습니다.");
                navigate(`/post/${id}`);
            } else {
                const created = await projectService.createProject(submitData);
                alert("모집 글이 성공적으로 등록되었습니다.");

                const newProjectId = created?.project_id || created?.id;
                if (newProjectId) {
                    navigate(`/post/${newProjectId}`);
                } else {
                    navigate("/");
                }
            }
        } catch (error) {
            console.error("저장 실패:", error);
            alert("저장 중 오류가 발생했습니다.");
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
                            <select
                                name="region"
                                value={form.region}
                                onChange={handleChange}
                            >
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
                                모집 상태
                            </label>
                            <select
                                name="status"
                                value={form.status}
                                onChange={handleChange}
                            >
                                <option value="모집중">모집중</option>
                                <option value="모집완료">모집완료</option>
                            </select>
                        </div>
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
                                {MONTH_OPTIONS.map((m) => (
                                    <option key={m} value={m}>
                                        {m}개월
                                    </option>
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

                    <div className="form-actions">
                        <button
                            type="button"
                            className="btn-cancel"
                            onClick={() => navigate(-1)}
                            disabled={loading}
                        >
                            취소
                        </button>

                        <button
                            type="submit"
                            className="btn-submit"
                            disabled={loading}
                        >
                            {loading
                                ? "처리 중..."
                                : isEditMode
                                ? "수정 완료하기"
                                : "모집 글 등록하기"}
                        </button>
                    </div>
                </form>
            </main>
        </div>
    );
}
/*author
members
applicants
createdAt
roles
tags
applyType
kakaoLink
googleFormLink
headcount
db에 없어서 제거
*/
export default WritePage;