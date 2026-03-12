// 📁 src/components/common/Navbar.jsx
// 모든 페이지 상단에 표시되는 네비게이션 바 컴포넌트입니다.
// useNavigate: 버튼 클릭 시 페이지 이동에 사용
// useLocation: 현재 경로를 확인해 활성화된 링크 스타일 처리에 사용

import { useNavigate, useLocation } from "react-router-dom";
import "./Navbar.css";

function Navbar() {
  const navigate = useNavigate();   // 페이지 이동 함수
  const location = useLocation();   // 현재 URL 정보

  return (
    <nav className="navbar">
      <div className="navbar-inner">

        {/* 로고 - 클릭하면 메인 페이지로 이동 */}
        <div className="navbar-logo" onClick={() => navigate("/")}>
          <span className="logo-bracket">&lt;</span>
          TeamBuilder
          <span className="logo-bracket">/&gt;</span>
        </div>

        {/* 오른쪽 버튼 영역 */}
        <div className="navbar-actions">
          <button
            className="btn-ghost"
            onClick={() => navigate("/login")}
          >
            로그인
          </button>
          <button
            className="btn-primary"
            onClick={() => navigate("/signup")}
          >
            회원가입
          </button>
        </div>

      </div>
    </nav>
  );
}

export default Navbar;
