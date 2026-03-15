import React, { useState } from 'react';
import './MyPageSetting.css';
import { useNavigate } from 'react-router-dom';

const MyPageSetting = () => {
  const navigate = useNavigate();

  const recommendedTags = ['React', 'Vue.js', 'Next.js', 'TypeScript', 'Node.js', 'Spring Boot', 'Django', 'Express', 'React Native', 'Flutter', 'MySQL', 'MongoDB', 'PostgreSQL', 'Firebase', 'Tailwind', 'Socket.io'];
  const [formData, setFormData] = useState({
    nickname: '강무원',
    job_role: '개발자',
    organization: '조선대',
    introduction: '안녕하세요. 조선대학교 컴퓨터공학과 강무원입니다.',
    tags: ['React', 'Vue.js', 'Next.js'] 
  });

  const [customTag, setCustomTag] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleTagClick = (tag) => {
    if (formData.tags.includes(tag)) {
      setFormData({
        ...formData,
        tags: formData.tags.filter(t => t !== tag)
      });
    } else {
      setFormData({
        ...formData,
        tags: [...formData.tags, tag]
      });
    }
  };

  const handleAddCustomTag = (e) => {
    // 엔터키를 누르거나 버튼을 클릭했을 때 작동
    if (e.key === 'Enter' || e.type === 'click') {
      e.preventDefault(); // 엔터 시 폼 제출 방지
      
      const trimmedTag = customTag.trim();
      
      if (trimmedTag && !formData.tags.includes(trimmedTag)) {
        setFormData({
          ...formData,
          tags: [...formData.tags, trimmedTag]
        });
        setCustomTag(''); // 입력창 초기화
      }
    }
  };

  const handleSave = () => {
    console.log("저장 요청 데이터:", formData);
    alert("프로필 정보가 성공적으로 수정되었습니다!");
    navigate('/mypage');
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
          <input className="ms-input" name="nickname" value={formData.nickname} onChange={handleChange} />
        </div>

        <div className="ms-input-group">
          <label>소속</label>
          <input className="ms-input" name="organization" value={formData.organization} onChange={handleChange} />
        </div>

        <div className="ms-input-group">
          <label>희망 직무</label>
          <input className="ms-input" name="job_role" value={formData.job_role} onChange={handleChange} />
        </div>

        <div className="ms-input-group">
          <label>자기소개</label>
          <textarea className="ms-textarea" name="introduction" value={formData.introduction} onChange={handleChange} />
        </div>

        <div className="ms-input-group">
          <label>기술 스택 (클릭하거나 직접 입력)</label>
          
          <div className="ms-selected-tags">
            {formData.tags.map(tag => (
              <span key={tag} className="ms-tag-active" onClick={() => handleTagClick(tag)}>
                #{tag} <span className="ms-tag-remove">✕</span>
              </span>
            ))}
          </div>

          <div className="ms-tag-pool">
            {recommendedTags.map(tag => (
              <button 
                key={tag} 
                type="button"
                className={`ms-tag-btn ${formData.tags.includes(tag) ? 'selected' : ''}`}
                onClick={() => handleTagClick(tag)}
              >
                {tag}
              </button>
            ))}
            
            <div className="ms-custom-tag-wrap">
              <input 
                type="text"
                className="ms-tag-input"
                placeholder="직접 입력..."
                value={customTag}
                onChange={(e) => setCustomTag(e.target.value)}
                onKeyDown={handleAddCustomTag}
              />
              <button type="button" className="ms-tag-add-btn" onClick={handleAddCustomTag}>+</button>
            </div>
          </div>
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