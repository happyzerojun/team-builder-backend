import React from 'react';

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
    return (
        <BrowserRouter>
            <div className="App">
                <Routes>

                    {/* 기본 페이지 → 메인페이지 */}
                    <Route path="/" element={<MainPage />} />

                    {/* 로그인 / 회원가입 */}
                    <Route path="/login" element={<LoginPage />} />
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