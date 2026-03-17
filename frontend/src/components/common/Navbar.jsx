// 📁 src/components/common/Navbar.jsx
import { useNavigate } from "react-router-dom";
import "./Navbar.css";

function Navbar({ isLoggedIn, onLogout }) {
    const navigate = useNavigate();

    return (
        <nav className="navbar">
            <div className="navbar-inner">

                {/* 로고 */}
                <div className="navbar-logo" onClick={() => navigate("/")}>
                    <span className="logo-bracket">&lt;</span>
                    TeamBuilder
                    <span className="logo-bracket">/&gt;</span>
                </div>

                {/* 오른쪽 버튼 영역 */}
                <div className="navbar-actions">
                    {isLoggedIn ? (
                        // ✅ 로그인 상태일 때
                        <>
                            <button className="btn-ghost" onClick={() => navigate("/mypage")}>
                                마이페이지
                            </button>
                            <button className="btn-primary" onClick={onLogout}>
                                로그아웃
                            </button>
                        </>
                    ) : (
                        // ✅ 비로그인 상태일 때
                        <>
                            <button className="btn-ghost" onClick={() => navigate("/login")}>
                                로그인
                            </button>
                            <button className="btn-primary" onClick={() => navigate("/Signup")}>
                                회원가입
                            </button>
                        </>
                    )}
                </div>

            </div>
        </nav>
    );
}

export default Navbar;