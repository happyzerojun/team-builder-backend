import React, { useState, useEffect } from 'react';
import './MyPageSetting.css';
import { useNavigate } from 'react-router-dom';
import { getUserProfile, updateUserProfile } from '../../services/userService';

const MyPageSetting = () => {
    const navigate = useNavigate();

    const recommendedTags = [
        'React', 'Vue.js', 'Next.js', 'TypeScript', 'Node.js',
        'Spring Boot', 'Django', 'Express', 'React Native', 'Flutter',
        'MySQL', 'MongoDB', 'PostgreSQL', 'Firebase', 'Tailwind', 'Socket.io'
    ];

    const [profileImg, setProfileImg] = useState(null);
    const [loading, setLoading] = useState(true);

    const [formData, setFormData] = useState({
        nickname: '',
        job_role: '',
        organization: '',
        introduction: '',
        tags: []
    });

    const [customTag, setCustomTag] = useState('');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                setLoading(true);

                const profile = await getUserProfile();
                const savedUser = JSON.parse(localStorage.getItem("user") || "{}");

                const baseUser = profile || savedUser;

                setFormData({
                    nickname: baseUser.nickname || baseUser.name || '',
                    job_role: baseUser.job_role || '',
                    organization: baseUser.organization || '',
                    introduction: baseUser.introduction || '',
                    tags: baseUser.tags || []
                });

                if (baseUser.profileImg) {
                    setProfileImg(baseUser.profileImg);
                }
            } catch (error) {
                console.error("프로필 불러오기 실패:", error);
                alert("프로필 정보를 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const handleImgChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onloadend = () => {
            setProfileImg(reader.result);
        };
        reader.readAsDataURL(file);
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value
        }));
    };

    const handleTagClick = (tag) => {
        setFormData((prev) => ({
            ...prev,
            tags: prev.tags.includes(tag)
                ? prev.tags.filter((t) => t !== tag)
                : [...prev.tags, tag]
        }));
    };

    const handleAddCustomTag = (e) => {
        if (e.key === 'Enter' || e.type === 'click') {
            e.preventDefault();

            const trimmedTag = customTag.trim();
            if (!trimmedTag) return;
            if (formData.tags.includes(trimmedTag)) return;

            setFormData((prev) => ({
                ...prev,
                tags: [...prev.tags, trimmedTag]
            }));
            setCustomTag('');
        }
    };

    const handleSave = async () => {
        if (!formData.nickname.trim()) {
            alert("닉네임을 입력해주세요!");
            return;
        }

        try {
            const payload = {
                name: formData.nickname,
                nickname: formData.nickname,
                job_role: formData.job_role,
                organization: formData.organization,
                introduction: formData.introduction,
                tags: formData.tags,
                profileImg: profileImg
            };

            await updateUserProfile(payload);

            const currentUser = JSON.parse(localStorage.getItem("user") || "{}");
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

            alert("프로필이 성공적으로 저장되었습니다!");
            navigate('/mypage');
        } catch (error) {
            console.error("프로필 저장 실패:", error);
            alert("프로필 저장에 실패했습니다.");
        }
    };

    if (loading) {
        return <div className="ms-container">불러오는 중...</div>;
    }

    return (
        <div className="ms-container">
            <div className="ms-card">
                <h2 className="ms-title">프로필 수정</h2>

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
                    <input
                        className="ms-input"
                        name="nickname"
                        placeholder="사용하실 닉네임을 입력하세요"
                        value={formData.nickname}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>소속</label>
                    <input
                        className="ms-input"
                        name="organization"
                        placeholder="학교 또는 직장"
                        value={formData.organization}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>희망 직무</label>
                    <input
                        className="ms-input"
                        name="job_role"
                        placeholder="예: 프론트엔드 개발자"
                        value={formData.job_role}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>자기소개</label>
                    <textarea
                        className="ms-textarea"
                        name="introduction"
                        placeholder="자신을 자유롭게 소개해주세요"
                        value={formData.introduction}
                        onChange={handleChange}
                    />
                </div>

                <div className="ms-input-group">
                    <label>기술 스택</label>

                    <div className="ms-selected-tags">
                        {formData.tags.map((tag) => (
                            <span
                                key={tag}
                                className="ms-tag-active"
                                onClick={() => handleTagClick(tag)}
                            >
                                #{tag} <span className="ms-tag-remove">✕</span>
                            </span>
                        ))}
                    </div>

                    <div className="ms-tag-pool">
                        {recommendedTags.map((tag) => (
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
                            <button
                                type="button"
                                className="ms-tag-add-btn"
                                onClick={handleAddCustomTag}
                            >
                                +
                            </button>
                        </div>
                    </div>
                </div>

                <div className="ms-button-group">
                    <button className="ms-cancel-btn" onClick={() => navigate('/mypage')}>
                        취소
                    </button>
                    <button className="ms-save-btn" onClick={handleSave}>
                        저장하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default MyPageSetting;