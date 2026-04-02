import React, { useState, useEffect } from 'react';
import './LoginPage.css';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService'; 

const LoginPage = ({ onLoginSuccess }) => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    useEffect(() => {

        const urlParams = new URLSearchParams(window.location.search);

        const code = urlParams.get("code");
        const state = urlParams.get("state");

        if (!code) return;

        if (state === "google") {
            getGoogleToken(code);
        }

        if (state === "kakao") {
            getKakaoToken(code);
        }

    }, []);

    const handleLogin = async () => {
        try {
            const data = await authService.login({ email, password });

            console.log("응답 전체:", data);

            const { accessToken } = data;
            localStorage.setItem("token", accessToken);
            localStorage.setItem("isLoggedIn", "true");
            localStorage.setItem("user", JSON.stringify({ id: email, name: data.name }));

            alert("로그인 성공!");
            if (onLoginSuccess) onLoginSuccess();
            navigate('/');
        } catch (error) {
            if (error.response) {
                alert(`로그인 실패: ${error.response.data.message || '이메일 또는 비밀번호를 확인해주세요.'}`);
            } else {
                alert("서버에 연결할 수 없습니다.");
            }
        }
    };

    const handleKakaoLogin = () => {

        const REST_API_KEY = "20a0b697b5c6300c4a61d4a14313c77e";
        const REDIRECT_URI = "http://localhost:5173/login";

        window.location.href =
            `https://kauth.kakao.com/oauth/authorize` +
            `?client_id=${REST_API_KEY}` +
            `&redirect_uri=${REDIRECT_URI}` +
            `&response_type=code` +
            `&state=kakao`; 
    };

    const handleGoogleLogin = () => {
        const CLIENT_ID = "825299339140-qss5ti06pv76gc0bb2s8rkqbv8o00g4i.apps.googleusercontent.com";
        const REDIRECT_URI = "http://localhost:5173/login";

        const url =
            `https://accounts.google.com/o/oauth2/v2/auth` +
            `?client_id=${CLIENT_ID}` +
            `&redirect_uri=${REDIRECT_URI}` +
            `&response_type=code` +
            `&scope=email profile` +
            `&prompt=select_account` +
            `&state=google`;

        window.location.href = url;
    };

    const getGoogleToken = async (code) => {

        const CLIENT_ID = "825299339140-qss5ti06pv76gc0bb2s8rkqbv8o00g4i.apps.googleusercontent.com";
        const CLIENT_SECRET = "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";// 프론트에서 돌릴려고 넣었어요, 나중에 백엔드연결할때 빼야함(보안키)
        const REDIRECT_URI = "http://localhost:5173/login";

        const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                code: code,
                client_id: CLIENT_ID,
                client_secret: CLIENT_SECRET,
                redirect_uri: REDIRECT_URI,
                grant_type: "authorization_code"
            })
        });

        const tokenData = await tokenRes.json();

        const accessToken = tokenData.access_token;

        getGoogleUser(accessToken);
    };

    const getGoogleUser = async (accessToken) => {

        const userRes = await fetch(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            }
        );

        const user = await userRes.json();

        const socialUser = {
            id: user.email,
            name: user.name,
            picture: user.picture,
            isSocial: true
        };

        localStorage.setItem("user", JSON.stringify(socialUser));
        localStorage.setItem("isLoggedIn", "true");

        if (onLoginSuccess) onLoginSuccess();
        navigate("/", { replace: true });
    };

    const getKakaoToken = async (code) => {

        const REST_API_KEY = "20a0b697b5c6300c4a61d4a14313c77e";
        const REDIRECT_URI = "http://localhost:5173/login";

        const res = await fetch("https://kauth.kakao.com/oauth/token", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                grant_type: "authorization_code",
                client_id: REST_API_KEY,
                redirect_uri: REDIRECT_URI,
                code: code
            })
        });

        const data = await res.json();

        const accessToken = data.access_token;

        getKakaoUser(accessToken);
    };

    const getKakaoUser = async (accessToken) => {

        const res = await fetch("https://kapi.kakao.com/v2/user/me", {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        const data = await res.json();

        const user = {
            id: data.id,
            name: data.kakao_account.profile.nickname,
            isSocial: true
        };

        localStorage.setItem("user", JSON.stringify(user));
        localStorage.setItem("isLoggedIn", "true");

        if (onLoginSuccess) onLoginSuccess();
        navigate("/", { replace: true });
    };

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