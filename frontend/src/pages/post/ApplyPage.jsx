import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { applicationService } from "../../services/applicationService";
import "./ApplyPage.css";

const ApplyPage = () => {
  const navigate = useNavigate();
  const { projectId } = useParams();

  const [formData, setFormData] = useState({
    supportRole: "",
    message: "",
    experience: "",
    contact: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async () => {
    if (!formData.supportRole.trim()) {
      alert("지원 역할을 입력해주세요.");
      return;
    }

    if (!formData.message.trim()) {
      alert("지원 메시지를 입력해주세요.");
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
            <label>지원 역할</label>
            <input
              name="supportRole"
              value={formData.supportRole}
              onChange={handleChange}
              placeholder="예: 프론트엔드, 백엔드, 디자이너"
            />
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
            <input
              name="contact"
              value={formData.contact}
              onChange={handleChange}
              placeholder="예: 이메일, 카카오톡 오픈채팅 링크 등"
            />
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