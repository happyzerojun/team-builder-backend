import React, { useState, useEffect } from 'react';
import './MyPageSetting.css';
import { useNavigate } from 'react-router-dom';

const MyPageSetting = () => {
  const navigate = useNavigate();

  const recommendedTags = ['React', 'Vue.js', 'Next.js', 'TypeScript', 'Node.js', 'Spring Boot', 'Django', 'Express', 'React Native', 'Flutter', 'MySQL', 'MongoDB', 'PostgreSQL', 'Firebase', 'Tailwind', 'Socket.io'];

  const [profileImg, setProfileImg] = useState(null);
  const [formData, setFormData] = useState({
    nickname: '',
    job_role: '',
    organization: '',
    introduction: '',
    tags: []
  });

  const [customTag, setCustomTag] = useState('');

  // 1. 초기 데이터 로드 (기존 저장 정보 불러오기)
  useEffect(() => {
    const savedUser = JSON.parse(localStorage.getItem("user"));
    if (savedUser) {
      setFormData({
        nickname: savedUser.nickname || savedUser.name || '강무원',
        job_role: savedUser.job_role || '개발자',
        organization: savedUser.organization || '조선대',
        introduction: savedUser.introduction || '안녕하세요!',
        tags: savedUser.tags || ['React']
      });
      if (savedUser.profileImg) {
        setProfileImg(savedUser.profileImg);
      }
    }
  }, []);

  const handleImgChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setProfileImg(reader.result); 
      };
      reader.readAsDataURL(file);
    }
  };

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
    if (e.key === 'Enter' || e.type === 'click') {
      e.preventDefault();
      const trimmedTag = customTag.trim();
      if (trimmedTag && !formData.tags.includes(trimmedTag)) {
        setFormData({
          ...formData,
          tags: [...formData.tags, trimmedTag]
        });
        setCustomTag('');
      }
    }
  };

  // 로컬저장
  const handleSave = () => {
    const currentUser = JSON.parse(localStorage.getItem("user")) || {};
    
    const updatedUser = {
      ...currentUser,
      name: formData.nickname,
      nickname: formData.nickname,
      job_role: formData.job_role,
      organization: formData.organization,
      introduction: formData.introduction,
      tags: formData.tags,
      profileImg: profileImg 
    };

    localStorage.setItem("user", JSON.stringify(updatedUser));
    alert("프로필 정보가 성공적으로 수정되었습니다!");
    navigate('/mypage');
  };

  return (
    <div className="ms-container">
      <div className="ms-card">
        <h2 className="ms-title">프로필 수정</h2>

        {/* 프로필 이미지 업로드 구역 */}
        <div className="ms-profile-img-wrap">
          <label htmlFor="profile-upload" className="ms-img-label-wrapper">
            <div className="ms-img-box">
              {profileImg ? (
                <img src={profileImg} alt="프로필 미리보기" className="ms-uploaded-img" />
              ) : (
                <div className="ms-default-img">👤</div>
              )}
              <div className="ms-img-overlay">편집</div>
            </div>
            <input 
              type="file" 
              id="profile-upload" 
              className="ms-hidden-input" 
              accept="image/*" 
              onChange={handleImgChange} 
            />
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