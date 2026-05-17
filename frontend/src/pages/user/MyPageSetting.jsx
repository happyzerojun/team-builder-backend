import React, { useState, useEffect } from 'react';
import './MyPageSetting.css';
import { useNavigate } from 'react-router-dom';
import { getUserProfile, updateUserProfile } from '../../services/userService';

const REGIONS = [
    "서울", "경기", "인천", "부산", "대구", "광주", "대전", "울산", "세종",
    "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
];

const MyPageSetting = () => {
    const navigate = useNavigate();

    const recommendedTags = [
        'React', 'Vue', 'Angular', 'Next.js', 'Svelte', 'TypeScript', 'JavaScript', 'HTML/CSS', 'Tailwind',
        'Spring Boot', 'Node.js', 'Django', 'FastAPI', 'Flask', 'Express', 'NestJS', 'Java', 'Python', 'Go', 'Kotlin',
        'MySQL', 'PostgreSQL', 'MongoDB', 'Redis', 'Oracle', 'SQLite', 'Firebase',
        'AWS', 'Docker', 'Kubernetes', 'CI/CD', 'GCP', 'Azure', 'Nginx', 'Linux',
        'Git', 'Figma', 'Unity', 'Flutter', 'React Native', 'Swift', 'Kotlin(Android)', 'Kotlin(iOS)'
    ];

    const [profileImg, setProfileImg] = useState(null);
    const [loading, setLoading] = useState(true);

    const [formData, setFormData] = useState({
        nickname: '',
        jobRole: '',
        organization: '',
        region: '',
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

                const baseUser = { ...savedUser, ...profile };

                setFormData({
                    nickname: baseUser.nickname || baseUser.name || '',
                    jobRole: baseUser.jobRole || baseUser.job_role || '',
                    organization: baseUser.organization || '',
                    region: baseUser.region || '',
                    introduction: baseUser.introduction || '',
                    tags: baseUser.tags || baseUser.techStacks || []
                });

                if (baseUser.profileImg || baseUser.profile_img) {
                    setProfileImg(baseUser.profileImg || baseUser.profile_img);
                }
            } catch (error) {
                console.error("프로필 불러오기 실패:", error);
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

        if (!formData.region) {
            alert("지역을 선택해주세요!");
            return;
        }

        try {
            const payload = {
                name: formData.nickname,
                nickname: formData.nickname,
                jobRole: formData.jobRole,
                organization: formData.organization,
                region: formData.region,
                introduction: formData.introduction,
                tags: formData.tags,
                profileImg
            };

            await updateUserProfile(payload);

            const currentUser = JSON.parse(localStorage.getItem("user") || "{}");
            localStorage.setItem("user", JSON.stringify({
                ...currentUser,
                ...payload
            }));

            alert("프로필이 성공적으로 저장되었습니다!");
            navigate('/mypage');
        } catch (error) {
            console.error("프로필 저장 실패:", error);
            alert("프로필 저장에 실패했습니다.");
        }
    };

    if (loading) {
        return <div className="ms-container">데이터를 불러오는 중...</div>;
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
                    <label>지역</label>
                    <select
                        className="ms-input"
                        name="region"
                        value={formData.region}
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

                <div className="ms-input-group">
                    <label>희망 직무</label>
                    <input
                        className="ms-input"
                        name="jobRole"
                        placeholder="예: 프론트엔드 개발자"
                        value={formData.jobRole}
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
                            <span key={tag} className="ms-tag-active" onClick={() => handleTagClick(tag)}>
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
                            <button type="button" className="ms-tag-add-btn" onClick={handleAddCustomTag}>
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