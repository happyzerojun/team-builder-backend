import React from 'react';
import LoginPage from './LoginPage';
import SignupPage from './SignupPage';
import MyPage from './MyPage';
import MyPageSetting from './MyPageSetting';

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          {/* 기본 주소('/')로 들어오면 로그인 페이지로 보내짐 (메인 페이지로 수정필요) */}
          <Route path="/" element={<Navigate to="/LoginPage" />} />
          
          <Route path="/LoginPage" element={<LoginPage />} />
          <Route path="/SignupPage" element={<SignupPage />} />
          <Route path="/MyPage" element={<MyPage />} />
          <Route path="/MyPageSetting" element={<MyPageSetting />} />
          
          {/* 만약 정의되지 않은 주소로 들어오면 로그인으로 리다이렉트 (메인 페이지로 수정필요)*/}
          <Route path="*" element={<Navigate to="/LoginPage" />} />
        </Routes>

      </div>
    </BrowserRouter>
  );
}

export default App;