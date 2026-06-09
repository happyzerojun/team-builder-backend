import { useState } from "react";
import api from "../services/api";

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
      const response = await api.post("/api/ai/recommend", { userPrompt });
      const data = response.data;
      setResult(data);

      // 🔥 변경점: 데이터를 성공적으로 받아오면 sessionStorage에 저장합니다.
      sessionStorage.setItem("aiResult", JSON.stringify(data));

    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return { result, isLoading, error, getRecommendation };
};

export default useAiRecommend;
