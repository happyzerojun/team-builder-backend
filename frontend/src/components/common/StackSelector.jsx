// 📁 src/components/common/StackSelector.jsx
//
// props:
//   selectedStacks  - 현재 선택된 스택 id 배열  예) [1, 3, 7]
//   onChange        - 선택 변경 시 호출할 함수   예) (newList) => setStacks(newList)

import { useState, useEffect } from "react";
import { fetchStacks } from "../../api/stackApi";
import "./StackSelector.css";

function StackSelector({ selectedStacks = [], onChange }) {
  // 스택 목록 상태 (API에서 불러온 데이터)
  const [stacks, setStacks] = useState([]);

  // 컴포넌트가 처음 마운트될 때 스택 목록 불러오기
  useEffect(() => {
    fetchStacks().then((data) => setStacks(data));
  }, []); // 빈 배열: 처음 한 번만 실행

  // 태그 클릭 시 선택/해제 토글
  function toggleStack(id) {
    const isSelected = selectedStacks.includes(id);
    const updated = isSelected
      ? selectedStacks.filter((s) => s !== id)  // 이미 선택됨 → 제거
      : [...selectedStacks, id];                 // 선택 안 됨 → 추가
    onChange(updated); // 부모 컴포넌트에 변경 알림
  }

  return (
    <div className="stack-selector">
      <div className="stack-list">
        {stacks.map((stack) => (
          <button
            key={stack.id}
            type="button"
            className={`stack-tag ${selectedStacks.includes(stack.id) ? "selected" : ""}`}
            onClick={() => toggleStack(stack.id)}
          >
            {stack.name}
          </button>
        ))}
      </div>

      {/* 선택된 스택이 있을 때 선택 개수 표시 */}
      {selectedStacks.length > 0 && (
        <div className="stack-selected-info">
          {selectedStacks.length}개 선택됨
          <button
            className="stack-clear-btn"
            onClick={() => onChange([])}
          >
            초기화
          </button>
        </div>
      )}
    </div>
  );
}

export default StackSelector;