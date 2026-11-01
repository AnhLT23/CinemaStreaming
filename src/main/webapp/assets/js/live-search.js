(() => {
    const input = document.querySelector("#liveSearchInput");
    const results = document.querySelector("#liveSearchResults");
    if (!input || !results) return;

    let timer;
    let activeRequest;
    let currentRequestId = 0;

    const hideResults = () => {
        results.replaceChildren();
        results.hidden = true;
    };

    input.addEventListener("input", () => {
        window.clearTimeout(timer);
        const requestId = ++currentRequestId;
        if (activeRequest) {
            activeRequest.abort();
        }

        const query = input.value.trim();
        if (query.length === 0) {
            hideResults();
            return;
        }

        timer = window.setTimeout(async () => {
            if (requestId !== currentRequestId) {
                return;
            }
            activeRequest = new AbortController();

            results.replaceChildren();
            results.hidden = false;

            const loading = document.createElement("div");
            loading.className = "search-empty";
            loading.textContent = "Đang tìm...";
            results.append(loading);

            try {
                const searchEndpoint = input.dataset.searchUrl || "/api/movies/quick-search";
                const url = new URL(searchEndpoint, window.location.href);
                url.searchParams.set("q", query);

                const response = await fetch(url, {
                    headers: { Accept: "application/json" },
                    signal: activeRequest.signal
                });

                if (!response.ok) {
                    throw new Error(`Search request failed: HTTP ${response.status}`);
                }

                // Kiểm tra loại bỏ kết quả từ request cũ nếu có request mới hơn
                if (requestId !== currentRequestId) {
                    return;
                }

                const payload = await response.json();
                if (requestId !== currentRequestId) {
                    return;
                }

                const movies = Array.isArray(payload) ? payload : (payload && payload.results);
                if (!Array.isArray(movies)) {
                    throw new Error("Search response must be a movie array.");
                }

                results.replaceChildren();

                const detailBase = input.dataset.detailUrl || "/movie";
                const contextPath = detailBase.replace(/\/movie$/, "");
                const moviesBase = input.dataset.moviesUrl || (contextPath ? `${contextPath}/movies` : "/movies");

                if (movies.length === 0) {
                    // Xử lý không có kết quả
                    const empty = document.createElement("div");
                    empty.className = "search-empty";
                    empty.textContent = "Không tìm thấy phim phù hợp.";
                    results.append(empty);

                    // Vẫn cho phép người dùng mở trang danh sách phim để lọc chi tiết
                    const viewAllLink = document.createElement("a");
                    viewAllLink.className = "search-all-link search-result d-block text-center py-2 px-3 text-secondary text-decoration-none border-top border-secondary small";
                    viewAllLink.href = `${moviesBase}?q=${encodeURIComponent(query)}`;
                    viewAllLink.textContent = `Tìm kiếm nâng cao cho "${query}" ›`;
                    results.append(viewAllLink);
                } else {
                    // Hiển thị tối đa 5 kết quả gồm tên phim, poster, điểm đánh giá
                    movies.slice(0, 5).forEach((movie) => {
                        const link = document.createElement("a");
                        link.className = "search-result d-flex align-items-center gap-2";
                        link.setAttribute("role", "option");
                        // Click kết quả mở trang chi tiết phim đúng MovieId
                        link.href = `${detailBase}?id=${encodeURIComponent(movie.id)}`;

                        // 1. Poster thumbnail
                        if (movie.posterUrl) {
                            const img = document.createElement("img");
                            img.alt = "";
                            img.style.width = "38px";
                            img.style.height = "52px";
                            img.style.objectFit = "cover";
                            img.style.borderRadius = "4px";
                            img.style.flexShrink = "0";

                            let posterSrc = movie.posterUrl;
                            if (!posterSrc.startsWith("http://") && !posterSrc.startsWith("https://")) {
                                if (!posterSrc.startsWith("/")) {
                                    posterSrc = "/" + posterSrc;
                                }
                                if (contextPath && !posterSrc.startsWith(contextPath + "/")) {
                                    posterSrc = contextPath + posterSrc;
                                }
                            }
                            img.src = posterSrc;
                            img.onerror = () => { img.style.display = "none"; };
                            link.append(img);
                        }

                        // 2. Thông tin phim (sử dụng textContent an toàn để chống XSS)
                        const info = document.createElement("div");
                        info.className = "flex-grow-1 overflow-hidden";

                        const titleEl = document.createElement("div");
                        titleEl.className = "fw-semibold text-truncate";
                        titleEl.textContent = movie.title || "Phim chưa có tên";
                        info.append(titleEl);

                        const ratingEl = document.createElement("div");
                        ratingEl.className = "small text-warning";
                        const ratingVal = typeof movie.avgRating === "number"
                            ? movie.avgRating.toFixed(1)
                            : (movie.avgRating || "10.0");
                        ratingEl.textContent = "★ " + ratingVal;
                        info.append(ratingEl);

                        link.append(info);
                        results.append(link);
                    });

                    // Thêm tùy chọn "Xem tất cả kết quả" để mở /movies?q=...
                    const viewAllLink = document.createElement("a");
                    viewAllLink.className = "search-all-link search-result d-block text-center py-2 px-3 fw-bold text-accent border-top border-secondary text-decoration-none";
                    viewAllLink.href = `${moviesBase}?q=${encodeURIComponent(query)}`;
                    viewAllLink.textContent = `Xem tất cả kết quả cho "${query}" ›`;
                    results.append(viewAllLink);
                }
            } catch (error) {
                if (error.name === "AbortError") {
                    return;
                }
                console.error("Live search error:", error);
                if (requestId === currentRequestId) {
                    results.replaceChildren();
                    const message = document.createElement("div");
                    message.className = "search-empty";
                    message.textContent = "Tìm kiếm tạm thời không khả dụng.";
                    results.append(message);
                }
            }
        }, 300);
    });

    document.addEventListener("click", (event) => {
        if (!input.parentElement.contains(event.target)) {
            hideResults();
        }
    });

    input.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            hideResults();
        } else if (event.key === "Enter") {
            // Khi nhấn Enter, hủy timer debounce đang chờ và đóng gợi ý để form submit bình thường
            window.clearTimeout(timer);
            if (activeRequest) {
                activeRequest.abort();
            }
            hideResults();
        }
    });
})();
