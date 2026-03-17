
import './LoginPage.css'; 
import {Link} from 'react-router-dom';

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LoginPage = ({ onLoginSuccess }) => {
    const [id, setId] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleLogin = () => {
        const savedUser = JSON.parse(localStorage.getItem("user"));

        if (!savedUser) {
            alert("회원 정보 없음");
            return;
        }

        if (id === savedUser.id && password === savedUser.password) {
            alert("로그인 성공!");
            localStorage.setItem("isLoggedIn", "true");
            onLoginSuccess(); // ✅ 추가
            navigate('/');
        } else {
            alert("아이디 또는 비밀번호 틀림");
        }
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
                      onChange={(e) => setId(e.target.value)}
                  />

                  <input
                      type="password"
                      placeholder="비밀번호"
                      className="login-input"
                      onChange={(e) => setPassword(e.target.value)}
                  />
        </div>
              <button className="login-button" onClick={handleLogin}>
                  로그인
              </button>
        <div className="login-footer">
                  계정이 없으신가요?
                  <Link className="a" to="/Signup">회원가입</Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;