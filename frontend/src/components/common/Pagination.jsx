function Pagination({ currentPage, totalPages, onPageChange }) {
    const groupSize = 10;
    const groupStart = Math.floor((currentPage - 1) / groupSize) * groupSize + 1;
    const groupEnd = Math.min(groupStart + groupSize - 1, totalPages);
    const pages = [];
    for (let i = groupStart; i <= groupEnd; i++) pages.push(i);

    if (totalPages <= 1) return null;

    return (
        <div className="pagination">
            <button className="page-btn page-nav" onClick={() => onPageChange(groupStart - 1)} disabled={groupStart === 1}>‹</button>
            {pages.map((page) => (
                <button key={page} className={`page-btn ${currentPage === page ? "active" : ""}`} onClick={() => onPageChange(page)}>{page}</button>
            ))}
            <button className="page-btn page-nav" onClick={() => onPageChange(groupEnd + 1)} disabled={groupEnd === totalPages}>›</button>
        </div>
    );
}

export default Pagination;