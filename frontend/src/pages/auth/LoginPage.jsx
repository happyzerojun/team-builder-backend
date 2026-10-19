import React, { useState } from 'react';
import './LoginPage.css';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const LoginPage = ({ onLoginSuccess }) => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async () => {
        try {
            await authService.login({ email, password });

            alert("로그인 성공!");
            if (onLoginSuccess) onLoginSuccess();
            navigate('/');
        } catch (error) {
            console.error("로그인 실패:", error);
            alert("로그인 실패: 이메일 또는 비밀번호를 확인해주세요.");
        }
    };

    // 소셜 로그인은 백엔드(Spring Security OAuth2)가 처리하고, 성공하면 /oauth/callback 으로 JWT를 넘겨준다.
    // 클라이언트 시크릿을 프론트 코드에 두지 않는다 (TS-15). 소셜 로그인은 Google만 제공한다 (TS-21)
    const startSocialLogin = (provider) => {
        window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`;
    };

    const handleGoogleLogin = () => startSocialLogin("google");

    return (
        <div className="login-container">
            <div className="login-card">
                <h2 className="login-title">프로젝트 팀원 매칭 플랫폼</h2>

                <div className="input-group">
                    <input
                        type="email"
                        placeholder="이메일"
                        className="login-input"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <input
                        type="password"
                        placeholder="비밀번호"
                        className="login-input"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>

                <button className="login-button" onClick={handleLogin}>
                    로그인
                </button>

                <div className="social-login-group">
                    <button className="google-btn" onClick={handleGoogleLogin}>
                        구글 로그인
                    </button>
                </div>

                <div className="login-footer">
                    계정이 없으신가요?
                    <Link className="a" to="/Signup">회원가입</Link>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;