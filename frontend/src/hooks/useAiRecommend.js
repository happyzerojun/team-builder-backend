import { useState } from "react";
import api from "../services/api";

// Gemini 응답(서버 read timeout 15초)을 기다릴 수 있도록 공용 axios 기본값(10초)보다 길게 잡는다.
const AI_TIMEOUT_MS = 30000;

const useAiRecommend = () => {
  // 🔥 변경점: 초기값을 null 대신 sessionStorage에서 가져옵니다.
  const [result, setResult] = useState(() => {
    const saved = sessionStorage.getItem("aiResult");
    return saved ? JSON.parse(saved) : null;
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const getRecommendation = async (userPrompt) => {
    setIsLoading(true);
    setError(null);
    try {
      // 요청자는 서버가 JWT로 식별한다. 예전처럼 userId(하드코딩 2)를 보내지 않는다 (TS-05)
      // 공용 api 인스턴스가 Authorization 헤더를 붙여 준다.
      const { data } = await api.post(
        "/api/ai/recommend",
        { userPrompt },
        { timeout: AI_TIMEOUT_MS }
      );

      setResult(data);

      // 🔥 변경점: 데이터를 성공적으로 받아오면 sessionStorage에 저장합니다.
      sessionStorage.setItem("aiResult", JSON.stringify(data));

    } catch (err) {
      const status = err?.response?.status;
      if (status === 401) {
        setError("로그인 후 AI 추천을 이용할 수 있습니다.");
      } else {
        setError(err?.response?.data?.message || "서버와의 통신에 실패했습니다.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return { result, isLoading, error, getRecommendation };
};

export default useAiRecommend;
