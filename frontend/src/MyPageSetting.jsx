import React, { useState } from 'react';
import './MyPageSetting.css';
import { Link, useNavigate } from 'react-router-dom';

const MyPageSetting = () => {

  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    nickname: '강무원',
    job_role: '개발자',
    introduction: '안녕하세요....',
    tags: '#React #Java #OracleSQL'
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSave = () => {
  // 1. 서버에 데이터 보내기
  // 2. 저장이 잘 됐는지 확인
  // 3. 성공했다면 페이지 이동
    alert("프로필 정보가 성공적으로 수정되었습니다!");
    navigate('/MyPage');
  };

  return (
    <div className="ms-container">
      <div className="ms-card">
        <h2 className="ms-title">프로필 수정</h2>

        <div className="ms-profile-img-wrap">
          <label htmlFor="profile-upload" className="ms-img-label-wrapper">
            <div className="ms-img-box">
              <div className="ms-default-img">👤</div>
              <div className="ms-img-overlay">편집</div>
            </div>
            <input type="file" id="profile-upload" className="ms-hidden-input" />
          </label>
          <span className="ms-img-instruction">이미지를 클릭하여 변경</span>
        </div>

        <div className="ms-input-group">
          <label>닉네임</label>
          <input 
            className="ms-input"
            name="nickname"
            value={formData.nickname}
            onChange={handleChange}
          />
        </div>

        <div className="ms-input-group">
          <label>희망 직무</label>
          <input 
            className="ms-input"
            name="job_role"
            value={formData.job_role}
            onChange={handleChange}
          />
        </div>

        <div className="ms-input-group">
          <label>자기소개</label>
          <textarea 
            className="ms-textarea"
            name="introduction"
            value={formData.introduction}
            onChange={handleChange}
          />
        </div>

        <div className="ms-input-group">
          <label>기술 스택 (태그)</label>
          <input 
            className="ms-input"
            name="tags"
            placeholder="#React #Java"
            value={formData.tags}
            onChange={handleChange}
          />
        </div>

        <div className="ms-button-group">
          <button className="ms-cancel-btn" onClick={() => navigate('/mypage')}>취소</button>
          <button className="ms-save-btn" onClick={handleSave}>저장하기</button>
        </div>
      </div>
    </div>
  );
};

export default MyPageSetting;