import React from 'react';
import './MyPage.css';
import { useNavigate } from 'react-router-dom';

const MyPage = () => {
  
  const myLeadProjects = [
    { id: 201, title: "포트폴리오 공유 플랫폼", members: "3/4명", status: "모집중" }
  ];

  const myParticipation = [
    { id: 101, title: "캠퍼스 팀 매칭 서비스", role: "프론트엔드", status: "진행중" }
  ];

  const Navigate = useNavigate();

  return (
    <div className="mp-container"> 
      <div className="mp-card"> 
        
        <div className="mp-header">
          <div className="mp-avatar"></div> 
          <div className="mp-info">
            <h3>
              강무원 님 
              <button className="mp-setting-btn" onClick={() => Navigate('/MyPageSetting')}>프로필 수정</button>
            </h3>
            <div className="mp-tags">
              <span className="mp-tag">#React</span>
              <span className="mp-tag">#Java</span>
              <span className="mp-tag">#OracleSQL</span>
            </div>
          </div>
        </div>

        <hr className="mp-divider" />

        <div className="mp-section">
          <h4>자기소개</h4>
          <p1>
            안녕하세요! 대학생 강무원입니다. 
            ...
          </p1>
          
          <div>
            <p>📍 <strong>희망 직무:</strong> ...</p>
            <p>🏫 <strong>소속:</strong> 조선대학교 (컴퓨터공학과)</p>
          </div>
        </div>

        <hr className="mp-divider" />

        {/* 3. 내가 리더인 프로젝트  */}
        <div className="mp-section">
          <h4>내가 만든 프로젝트</h4>
          {myLeadProjects.map(proj => (
            <div key={proj.id} className="mp-activity-card">
              <span className="title">{proj.title}</span>
              <span className="role" style={{ fontSize: '13px' }}>{proj.members}</span>
              <span className="mp-badge status-recruiting">{proj.status}</span>
            </div>
          ))}
        </div>

        {/* 4. 참여 중인 프로젝트 (project_position 테이블 연동) */}
        <div className="mp-section" style={{ marginTop: '20px' }}>
          <h4>참여 중인 프로젝트</h4>
          {myParticipation.length > 0 ? (
            myParticipation.map(proj => (
              <div key={proj.id} className="mp-activity-card">
                <span className="title">{proj.title}</span>
                <span className="role">{proj.role}</span>
                <span className="mp-badge status-ongoing">{proj.status}</span>
              </div>
            ))
          ) : (
            <div className="mp-activity-card" style={{ color: '#999' }}>참여 중인 프로젝트가 없습니다.</div>
          )}
        </div>

        {/* 5. 나의 지원 현황 (application 테이블 연동 - 비공개 영역) */}
        <div className="mp-section" style={{ marginTop: '20px' }}>
          <h4>나의 지원 현황</h4>
          <div className="mp-activity-card">
            <span className="title">AI 스터디 그룹</span>
            <span className="mp-badge status-pending">대기중</span>
          </div>
        </div>

      </div>
    </div>
  );
};

export default MyPage;