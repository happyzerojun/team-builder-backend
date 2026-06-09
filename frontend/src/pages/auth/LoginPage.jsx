import React, { useEffect, useState } from 'react';
import './LoginPage.css';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';

const LoginPage = ({ onLoginSuccess }) => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    useEffect(() => {
        const hashParams = new URLSearchParams(window.location.hash.slice(1));
        const accessToken = hashParams.get('accessToken');
        if (!accessToken) return;

        window.history.replaceState({}, document.title, '/login');
        authService.completeSocialLogin(accessToken)
            .then(() => {
                onLoginSuccess?.();
                navigate('/', { replace: true });
            })
            .catch(() => alert('소셜 로그인에 실패했습니다.'));
    }, [navigate, onLoginSuccess]);

    const handleLogin = async () => {
        try {
            await authService.login({ email, password });
            onLoginSuccess?.();
            navigate('/');
        } catch (error) {
            console.error('로그인 실패:', error);
            alert('로그인 실패: 이메일 또는 비밀번호를 확인해주세요.');
        }
    };

    const handleSocialLogin = async (provider) => {
        try {
            await authService.startSocialLogin(provider);
        } catch (error) {
            console.error('소셜 로그인 시작 실패:', error);
            alert('소셜 로그인을 시작할 수 없습니다.');
        }
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
                        onChange={(event) => setEmail(event.target.value)}
                        autoComplete="email"
                    />
                    <input
                        type="password"
                        placeholder="비밀번호"
                        className="login-input"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        autoComplete="current-password"
                    />
                </div>

                <button className="login-button" onClick={handleLogin}>
                    로그인
                </button>

                <div className="social-login-group">
                    <button className="kakao-btn" onClick={() => handleSocialLogin('kakao')}>
                        카카오 로그인
                    </button>
                    <button className="google-btn" onClick={() => handleSocialLogin('google')}>
                        구글 로그인
                    </button>
                </div>

                <div className="login-footer">
                    계정이 없으신가요?
                    <Link className="a" to="/signup">회원가입</Link>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
