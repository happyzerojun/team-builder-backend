
import React, { useState, useEffect } from 'react';
import './LoginPage.css';
import { Link, useNavigate } from 'react-router-dom';

const LoginPage = ({ onLoginSuccess }) => {
    const navigate = useNavigate();
    const [id, setId] = useState('');
    const [password, setPassword] = useState('');

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');

        if (code) {
            alert("소셜 로그인 성공!");
            localStorage.setItem("isLoggedIn", "true");
            if (onLoginSuccess) onLoginSuccess();
            navigate('/', { replace: true });
        }
    }, [navigate, onLoginSuccess]);

    const handleLogin = () => {
        const savedUser = JSON.parse(localStorage.getItem("user"));

        if (!savedUser) {
            alert("회원 정보 없음");
            return;
        }

        if (id === savedUser.id && password === savedUser.password) {
            alert("로그인 성공!");
            localStorage.setItem("isLoggedIn", "true");
            if (onLoginSuccess) onLoginSuccess();
            navigate('/');
        } else {
            alert("아이디 또는 비밀번호 틀림");
        }
    };

    const handleKakaoLogin = () => {
        const REST_API_KEY = "20a0b697b5c6300c4a61d4a14313c77e";
        const REDIRECT_URI = "http://localhost:5173/login";
        window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=${REST_API_KEY}&redirect_uri=${REDIRECT_URI}&response_type=code`;
    };

    const handleGoogleLogin = () => {
        const CLIENT_ID = "825299339140-qss5ti06pv76gc0bb2s8rkqbv8o00g4i.apps.googleusercontent.com";
        const REDIRECT_URI = "http://localhost:5173/login";
        window.location.href = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=email profile&prompt=select_account`;
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h2 className="login-title">프로젝트 팀원 매칭 플랫폼</h2>
                <div className="input-group">
                    <input
                        type="text"
                        placeholder="아이디"
                        className="login-input"
                        value={id}
                        onChange={(e) => setId(e.target.value)}
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
                    <button className="kakao-btn" onClick={handleKakaoLogin}>카카오 로그인</button>
                    <button className="google-btn" onClick={handleGoogleLogin}>구글 로그인</button>
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