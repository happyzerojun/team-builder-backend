import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css'; 

const Navbar = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    // 모집글 상세페이지에서 로그인유지되게 수정
    const savedUser = JSON.parse(localStorage.getItem("user"));
    if (savedUser) {
      setUser(savedUser);
    } else {
      setUser(null);
    }
  }, [navigate]);

  const handleLogout = () => {
    if (window.confirm("로그아웃 하시겠습니까?")) {
      localStorage.removeItem("user");
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
              <span className="user-greet" onClick={() => navigate('/MyPage')}>
                <span className="user-name">{user.id}</span>님
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