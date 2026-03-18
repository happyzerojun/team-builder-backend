import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MOCK_POSTS } from "../../data/mockData";
import './ChatPage.css';

const ChatPage = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const scrollRef = useRef();

  //프로젝트 정보 가져오기
  const project = MOCK_POSTS.find(p => p.id === parseInt(projectId));

  const myInfo = JSON.parse(localStorage.getItem("user")) || { name: "익명" };

  const [messages, setMessages] = useState([
  ]);
  const [inputText, setInputText] = useState("");

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (!inputText.trim()) return;

    const newMessage = {
      id: Date.now(),
      sender: myInfo.nickname || myInfo.name,
      content: inputText,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isMe: true,
    };

    setMessages([...messages, newMessage]);
    setInputText("");
  };

  if (!project) return <div className="chat-error">존재하지 않는 프로젝트입니다.</div>;

  return (
    <div className="chat-container">
      {/* 상단 헤더 */}
      <div className="chat-header">
        <button className="back-btn" onClick={() => navigate(-1)}>←</button>
        <div className="header-info">
          <span className="room-name">{project.title} 팀 채팅방</span>
          <span className="member-count">👥 3</span>
        </div>
      </div>

      {/* 메시지 영역 */}
      <div className="chat-messages" ref={scrollRef}>
        {messages.map((msg) => (
          <div key={msg.id} className={`message-row ${msg.isMe ? 'me' : 'other'}`}>
            {!msg.isMe && <div className="sender-name">{msg.sender}</div>}
            <div className="bubble-container">
              <div className="bubble">{msg.content}</div>
              <span className="time">{msg.time}</span>
            </div>
          </div>
        ))}
      </div>

      {/* 입력 영역 */}
      <form className="chat-input-area" onSubmit={handleSendMessage}>
        <input 
          type="text" 
          placeholder="메시지를 입력하세요..." 
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
        />
        <button type="submit" disabled={!inputText.trim()}>전송</button>
      </form>
    </div>
  );
};

export default ChatPage;