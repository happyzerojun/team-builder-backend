import React, { useState } from 'react'; 
import { Link } from 'react-router-dom'; 
import './SignupPage.css';

const SignupPage = () => {

    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const isMatch = password === confirmPassword;
    const showMessage = confirmPassword.length > 0;

    const isFormValid = password.length >= 4 && isMatch && confirmPassword.length > 0; //비번 4글자이상

    return (
        <div className="signup-container">
            <div className="signup-card">
                <h2 className="signup-title">회원가입</h2>

                <input type="text" placeholder="아이디" className="signup-input" />
                <input type="email" placeholder="이메일" className="signup-input" />

                {/* 비밀번호 입력 */}
                <input
                    type="password"
                    placeholder="비밀번호"
                    className="signup-input"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                {/* 3. 비밀번호 확인 칸 추가 */}
                <input
                    type="password"
                    placeholder="비밀번호 확인"
                    className="signup-input"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                />

                {/* 4. 실시간 일치 메시지 */}
                {showMessage && (
                    <p className={`message ${isMatch ? 'match' : 'no-match'}`}>
                        {isMatch ? '✓ 비밀번호가 일치합니다' : '✗ 비밀번호가 일치하지 않습니다'}
                    </p>
                )}

                <button
                    className="signup-button"
                    disabled={!isFormValid} 
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