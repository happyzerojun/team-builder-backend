import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom'; 
import './Navbar.css'; 

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation(); 
  const [user, setUser] = useState(null);

  useEffect(() => {
    const savedUser = JSON.parse(localStorage.getItem("user"));
    if (savedUser) {
      setUser(savedUser);
    } else {
      setUser(null);
    }
  }, [location]); 

  const handleLogout = () => {
    if (window.confirm("로그아웃 하시겠습니까?")) {
      localStorage.removeItem("user");
      localStorage.removeItem("isLoggedIn"); 
      setUser(null);
      navigate('/');
    }
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <div className="logo" onClick={() => navigate('/')}>
          <span className="logo-point">Team</span>Matching
        </div>
        
        <div className="nav-menu">
          {user ? (
            <div className="nav-user-area">
              <span className="user-greet" onClick={() => navigate('/MyPage')} style={{ cursor: 'pointer' }}>
                {/* 닉네임 필드명을 user.id에서 user.name으로 수정 */}
                <span className="user-name">{user.name || "사용자"}</span>님
              </span>
              <button className="nav-btn logout" onClick={handleLogout}>로그아웃</button>
            </div>
          ) : (
            <div className="nav-auth-area">
              <button className="nav-btn login" onClick={() => navigate('/login')}>로그인</button>
              <button className="nav-btn signup" onClick={() => navigate('/signup')}>회원가입</button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;