import React from 'react';
import './SignupPage.css';

const SignupPage = () => {
  return (
    <div className="signup-container">
      <div className="signup-card">
        <h2 className="signup-title">회원가입</h2>
        <input type="text" placeholder="아이디" className="signup-input" />
        <input type="email" placeholder="이메일" className="signup-input" />
        <input type="password" placeholder="비밀번호" className="signup-input" />
        <button className="signup-button">가입하기</button>
      </div>
    </div>
  );
};

export default SignupPage;