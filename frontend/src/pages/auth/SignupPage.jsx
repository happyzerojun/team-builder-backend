import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import './SignupPage.css';

const SignupPage = () => {
    const [id, setId] = useState(''); 
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [level, setLevel] = useState('');
    const [hasTeamExp, setHasTeamExp] = useState('');

    const navigate = useNavigate();

    const handleSignup = async () => {
        const user = {
            name: id, 
            email: email,
            password: password
        };

        try {
            await authService.signup(user);
            alert("회원가입 완료! 로그인 페이지로 이동합니다.");
            navigate('/login');
        } catch (error) {
            console.error("회원가입 에러:", error);
            alert("회원가입 실패: " + (error.response?.data?.message || "서버 연결 확인 필요"));
        }
    };

    const isMatch = password === confirmPassword;
    const isFormValid = id.length > 0 && email.includes('@') && password.length >= 4 && isMatch && level !== '' && hasTeamExp !== '';

    return (
        <div className="signup-container">
            <div className="signup-card">
                <h2 className="signup-title">회원가입</h2>
                <input type="text" placeholder="이름(아이디)" className="signup-input" value={id} onChange={(e) => setId(e.target.value)} />
                <input type="email" placeholder="이메일" className="signup-input" value={email} onChange={(e) => setEmail(e.target.value)} />
                <input type="password" placeholder="비밀번호" className="signup-input" value={password} onChange={(e) => setPassword(e.target.value)} />
                
                <input type="password" placeholder="비밀번호 확인" className="signup-input" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
                
                {confirmPassword.length > 0 && (
                    <div style={{ textAlign: 'left', width: '100%', paddingLeft: '5px', marginBottom: '10px' }}>
                        {isMatch ? (
                            <span style={{ color: '#4dabf7', fontSize: '13px' }}>✓ 비밀번호가 일치합니다.</span>
                        ) : (
                            <span style={{ color: '#ff6b6b', fontSize: '13px' }}>✕ 비밀번호가 일치하지 않습니다.</span>
                        )}
                    </div>
                )}
                
                <select className="signup-input" value={level} onChange={(e) => setLevel(e.target.value)}>
                    <option value="">실력 수준 선택</option>
                    <option value="초보">초보</option>
                    <option value="중급">중급</option>
                    <option value="고수">고수</option>
                </select>

                <select className="signup-input" value={hasTeamExp} onChange={(e) => setHasTeamExp(e.target.value)}>
                    <option value="">협업 경험 선택</option>
                    <option value="없음">없음</option>
                    <option value="있음">있음</option>
                </select>

                <button className="signup-button" disabled={!isFormValid} onClick={handleSignup}
                    style={{ backgroundColor: isFormValid ? '#748ffc' : '#ccc', cursor: isFormValid ? 'pointer' : 'not-allowed' }}>
                    가입하기
                </button>
                <div className="login-link-container">
                    이미 계정이 있으신가요? <Link to="/Login" className="login-link">로그인</Link>
                </div>
            </div>
        </div>
    );
};

export default SignupPage;