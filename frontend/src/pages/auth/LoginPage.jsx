import React from 'react';
import './LoginPage.css'; 
import {Link} from 'react-router-dom';


const LoginPage = () => {
  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">프로젝트 팀원 매칭 플랫폼</h2>
        <div className="input-group">
          <input type="text" placeholder="아이디" className="login-input" />
          <input type="password" placeholder="비밀번호" className="login-input" />
        </div>
        <button className="login-button">로그인</button>
        <div className="login-footer">
          계정이 없으신가요? <Link to = "/SignupPage">회원가입</Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;