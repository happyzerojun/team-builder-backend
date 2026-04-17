import { useNavigate } from "react-router-dom";

function SearchBar({ searchText, onSearchChange }) {
    const navigate = useNavigate();

    return (
        <section className="search-section">
            <div className="search-bar-wrapper">
                <span className="search-icon">🔍</span>
                <input
                    type="text"
                    className="search-input"
                    placeholder="프로젝트 제목이나 설명으로 검색..."
                    value={searchText}
                    onChange={(e) => onSearchChange(e.target.value)}
                />
            </div>
            <button className="btn-write" onClick={() => navigate("/write")}>+ 글 작성</button>
        </section>
    );
}

export default SearchBar;