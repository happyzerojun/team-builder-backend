import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import './SignupPage.css';

const SignupPage = () => {
    // ✅ id, email 상태 추가
    const [id, setId] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [level, setLevel] = useState('');           
    const [hasTeamExp, setHasTeamExp] = useState(''); 

    const navigate = useNavigate();

    const handleSignup = () => {
        // ✅ 하드코딩 대신 실제 입력값 저장
        const user = {
            id: id,
            email: email,
            password: password,
            level: level,        
            hasTeamExp: hasTeamExp
        };

        localStorage.setItem("user", JSON.stringify(user));
        alert("회원가입 완료!");
        navigate('/login');
    };

    const isMatch = password === confirmPassword;
    const showMessage = confirmPassword.length > 0;
    const isFormValid =
        id.length > 0 &&
        password.length >= 4 &&
        isMatch &&
        confirmPassword.length > 0 &&
        level !== '' &&
        hasTeamExp !== '';

    return (
        <div className="signup-container">
            <div className="signup-card">
                <h2 className="signup-title">회원가입</h2>

                {/* ✅ value, onChange 추가 */}
                <input
                    type="text"
                    placeholder="아이디"
                    className="signup-input"
                    value={id}
                    onChange={(e) => setId(e.target.value)}
                />
                <input
                    type="email"
                    placeholder="이메일"
                    className="signup-input"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                {/* 아래는 기존 코드 그대로 */}
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

                {/* ✅ 추가: 실력 수준 선택 */}
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

                {/* ✅ 추가: 협업 경험 선택 */}
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