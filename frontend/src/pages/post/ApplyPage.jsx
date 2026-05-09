import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { applicationService } from "../../services/applicationService";
import { projectService } from "../../services/projectService";
import "./ApplyPage.css";

const ApplyPage = () => {
  const navigate = useNavigate();
  const { projectId } = useParams();

  const [project, setProject] = useState(null);

  const [formData, setFormData] = useState({
    supportRole: "",
    message: "",
    experience: "",
    contactType: "email",
    contactValue: ""
  });

  useEffect(() => {
    const fetchProject = async () => {
      try {
        const data = await projectService.getProjectById(projectId);
        setProject(data);
      } catch (error) {
        console.error("프로젝트 정보 조회 실패:", error);
      }
    };

    fetchProject();
  }, [projectId]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleTechSelect = (techName) => {
    setFormData((prev) => ({
      ...prev,
      supportRole: techName
    }));
  };

  const handleSubmit = async () => {
    if (!formData.supportRole) {
      alert("지원할 기술스택을 선택해주세요.");
      return;
    }

    if (!formData.message.trim()) {
      alert("지원 메시지를 입력해주세요.");
      return;
    }

    if (!formData.contactValue.trim()) {
      alert("연락 정보를 입력해주세요.");
      return;
    }

    try {
      await applicationService.apply(projectId, formData);

      alert("지원 신청이 완료되었습니다.");
      navigate("/mypage");
    } catch (error) {
      console.error("지원 신청 실패:", error);
      alert("지원 신청에 실패했습니다.");
    }
  };

  const contactPlaceholder = {
    email: "이메일을 입력해주세요.",
    kakao: "카카오톡 ID 또는 오픈채팅 링크를 입력해주세요.",
    phone: "연락처를 입력해주세요."
  };

  return (
    <div className="apply-container">
      <div className="apply-card">
        <button className="apply-back-btn" onClick={() => navigate(-1)}>
          ← 뒤로가기
        </button>

        <h2 className="apply-title">프로젝트 지원서</h2>
        <p className="apply-subtitle">
          프로젝트에 지원하기 위한 정보를 입력해주세요.
        </p>

        <div className="apply-form">
          <div className="apply-input-group">
            <label>지원 역할 / 기술스택</label>

            {project?.techStacks?.length > 0 ? (
              <div className="apply-tech-list">
                {project.techStacks.map((tech) => (
                  <button
                    key={tech.tech_stack_id}
                    type="button"
                    className={`apply-tech-tag ${
                      formData.supportRole === tech.name ? "selected" : ""
                    }`}
                    onClick={() => handleTechSelect(tech.name)}
                  >
                    {tech.name}
                  </button>
                ))}
              </div>
            ) : (
              <p className="apply-empty-text">
                등록된 기술스택이 없습니다.
              </p>
            )}
          </div>

          <div className="apply-input-group">
            <label>지원 메시지</label>
            <textarea
              name="message"
              value={formData.message}
              onChange={handleChange}
              placeholder="프로젝트에 지원하는 이유를 작성해주세요."
            />
          </div>

          <div className="apply-input-group">
            <label>경험 / 기술 스택</label>
            <textarea
              name="experience"
              value={formData.experience}
              onChange={handleChange}
              placeholder="사용 가능한 기술이나 관련 경험을 작성해주세요."
            />
          </div>

          <div className="apply-input-group">
            <label>연락 방법</label>

            <div className="apply-contact-row">
              <select
                name="contactType"
                value={formData.contactType}
                onChange={handleChange}
                className="apply-select"
              >
                <option value="email">이메일</option>
                <option value="kakao">카카오톡</option>
                <option value="phone">연락처</option>
              </select>

              <input
                name="contactValue"
                value={formData.contactValue}
                onChange={handleChange}
                placeholder={contactPlaceholder[formData.contactType]}
              />
            </div>
          </div>
        </div>

        <div className="apply-button-group">
          <button className="apply-cancel-btn" onClick={() => navigate(-1)}>
            취소
          </button>
          <button className="apply-submit-btn" onClick={handleSubmit}>
            지원서 제출
          </button>
        </div>
      </div>
    </div>
  );
};

export default ApplyPage;