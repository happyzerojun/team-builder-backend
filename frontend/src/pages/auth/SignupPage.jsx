import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './SignupPage.css';
import { authService } from '../../services/authService'; 

const SignupPage = () => {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [level, setLevel] = useState('');
    const [hasTeamExp, setHasTeamExp] = useState('');

    const navigate = useNavigate();

    const handleSignup = async () => {
        try {

            await authService.signup({
                name,
                email,
                password
            });

            alert("회원가입 완료!");
            navigate('/login');
        } catch (error) {

            if (error.response) {
                alert(`회원가입 실패: ${error.response.data.message || '다시 시도해주세요.'}`);
            } else {
                alert("서버에 연결할 수 없습니다.");
            }
        }
    };

    const isMatch = password === confirmPassword;
    const showMessage = confirmPassword.length > 0;
    const isFormValid =
        name.length > 0 &&
        email.length > 0 &&
        password.length >= 4 &&
        isMatch &&
        confirmPassword.length > 0 &&
        level !== '' &&
        hasTeamExp !== '';

    return (
        <div className="signup-container">
            <div className="signup-card">
                <h2 className="signup-title">회원가입</h2>

                <input
                    type="text"
                    placeholder="이름"
                    className="signup-input"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
                <input
                    type="email"
                    placeholder="이메일"
                    className="signup-input"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="비밀번호"
                    className="signup-input"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="비밀번호 확인"
                    className="signup-input"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />

                {showMessage && (
                    <p className={`message ${isMatch ? 'match' : 'no-match'}`}>
                        {isMatch ? '✓ 비밀번호가 일치합니다' : '✗ 비밀번호가 일치하지 않습니다'}
                    </p>
                )}

                <select
                    className="signup-input"
                    value={level}
                    onChange={(e) => setLevel(e.target.value)}
                >
                    <option value="">실력 수준 선택</option>
                    <option value="초보">초보 — 공부 중, 작은 프로젝트 경험</option>
                    <option value="중급">중급 — 혼자 개발 가능, 일부 경험 있음</option>
                    <option value="고수">고수 — 실무/대형 프로젝트 경험 있음</option>
                </select>

                <select
                    className="signup-input"
                    value={hasTeamExp}
                    onChange={(e) => setHasTeamExp(e.target.value)}
                >
                    <option value="">협업 경험 선택</option>
                    <option value="없음">협업 경험 없음</option>
                    <option value="있음">협업 경험 있음</option>
                </select>

                <button
                    className="signup-button"
                    disabled={!isFormValid}
                    onClick={handleSignup}
                    style={{
                        backgroundColor: isFormValid ? '#748ffc' : '#ccc',
                        cursor: isFormValid ? 'pointer' : 'not-allowed'
                    }}
                >
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