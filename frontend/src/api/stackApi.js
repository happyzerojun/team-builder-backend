// 📁 src/api/stackApi.js
import { MOCK_STACKS } from "../data/mockData";

// ──────────────────────────────────────────────
// 현재: mock 데이터 반환
// 나중에 Swagger 나오면 아래 주석 해제하고
// mock 부분 지우면 됩니다.
// ──────────────────────────────────────────────

export async function fetchStacks() {
  // ✅ 현재: mock 데이터 사용
  // Promise로 감싸는 이유: 나중에 axios로 교체해도
  // 사용하는 쪽 코드를 안 바꿔도 되게 하려고
  return Promise.resolve(MOCK_STACKS);

  // 🔄 나중에 API 연결 시 이걸로 교체:
  // const response = await axios.get("/api/stacks");
  // return response.data;
}