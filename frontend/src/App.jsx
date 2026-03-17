import React, { useState } from 'react';  // ✅ useState 추가

import LoginPage from './pages/auth/LoginPage';
import SignupPage from './pages/auth/SignupPage';

import MyPage from './pages/user/MyPage';
import MyPageSetting from './pages/user/MyPageSetting';

import MainPage from "./pages/post/MainPage";
import WritePage from "./pages/post/WritePage";
import DetailPage from "./pages/post/DetailPage";

import "./styles/global.css";

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

function App() {
    // ✅ 로그인 상태 관리 (localStorage에서 초기값 읽기)
    const [isLoggedIn, setIsLoggedIn] = useState(
        () => localStorage.getItem("isLoggedIn") === "true"
    );

    // ✅ 로그아웃 함수
    function handleLogout() {
        localStorage.removeItem("isLoggedIn");
        setIsLoggedIn(false);
    }

    return (
        <BrowserRouter>
            <div className="App">
                <Routes>

                    {/* 기본 페이지 → 메인페이지 */}
                    {/* ✅ isLoggedIn, onLogout props 전달 */}
                    <Route path="/" element={
                        <MainPage
                            isLoggedIn={isLoggedIn}
                            onLogout={handleLogout}
                        />}
                    />

                    {/* 로그인 / 회원가입 */}
                    {/* ✅ onLoginSuccess props 전달 */}
                    <Route path="/login" element={
                        <LoginPage
                            onLoginSuccess={() => setIsLoggedIn(true)}
                        />}
                    />
                    <Route path="/signup" element={<SignupPage />} />

                    {/* 마이페이지 */}
                    <Route path="/mypage" element={<MyPage />} />
                    <Route path="/mypagesetting" element={<MyPageSetting />} />

                    {/* 게시글 관련 */}
                    <Route path="/write" element={<WritePage />} />
                    <Route path="/post/:id" element={<DetailPage />} />

                    {/* 존재하지 않는 주소 → 메인으로 */}
                    <Route path="*" element={<Navigate to="/" />} />

                </Routes>
            </div>
        </BrowserRouter>
    );
}

export default App;