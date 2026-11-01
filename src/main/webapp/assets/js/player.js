(function () {
    document.addEventListener("DOMContentLoaded", function () {
        const videoEl = document.getElementById("videoPlayer");
        const iframeEl = document.getElementById("embedPlayer");
        const canvasEl = document.getElementById("playerCanvas");
        const wrapperEl = document.querySelector(".tvhay-watch-wrapper");
        const hudQualityEl = document.getElementById("hudQualityText");
        const serverButtons = document.querySelectorAll(".js-server-switch");
        let hlsInstance = null;

        if (!videoEl && !iframeEl) return;

        const resumeSecond = parseInt(
            (videoEl && videoEl.dataset.resumeSecond) || "0",
            10
        );
        let hasAppliedResume = false;

        function normalizeEmbedUrl(rawUrl) {
            if (!rawUrl) return "";
            let url = rawUrl.trim();
            if (url.includes("youtube.com/watch?v=")) {
                let vid = url.split("v=")[1].split("&")[0];
                return "https://www.youtube.com/embed/" + vid + "?autoplay=1&rel=0";
            }
            if (url.includes("youtu.be/")) {
                let vid = url.split("youtu.be/")[1].split("?")[0];
                return "https://www.youtube.com/embed/" + vid + "?autoplay=1&rel=0";
            }
            return url;
        }

        function loadStream(videoUrl, serverType, qualityLabel, subtitleUrl) {
            if (hudQualityEl && qualityLabel) {
                hudQualityEl.textContent = qualityLabel.replace("_", " • ");
            }

            const isEmbed =
                (serverType && serverType.toUpperCase().includes("EMBED")) ||
                (serverType && serverType.toUpperCase().includes("IFRAME")) ||
                videoUrl.includes("youtube.com") ||
                videoUrl.includes("youtu.be") ||
                videoUrl.includes("player.vimeo.com");

            // 1. Nếu là nguồn phát Iframe / YouTube Embed
            if (isEmbed) {
                if (hlsInstance) {
                    hlsInstance.destroy();
                    hlsInstance = null;
                }
                if (videoEl) {
                    videoEl.pause();
                    videoEl.classList.add("d-none");
                }
                if (iframeEl) {
                    iframeEl.src = normalizeEmbedUrl(videoUrl);
                    iframeEl.classList.remove("d-none");
                }
                return;
            }

            // 2. Nếu là nguồn phát Video trực tiếp (.m3u8 HLS hoặc .mp4)
            if (iframeEl) {
                iframeEl.src = "about:blank";
                iframeEl.classList.add("d-none");
            }
            if (!videoEl) return;
            videoEl.classList.remove("d-none");

            // Cập nhật phụ đề rời .vtt nếu có
            const trackEl = videoEl.querySelector("track");
            if (trackEl && subtitleUrl) {
                trackEl.src = subtitleUrl;
            }

            if (hlsInstance) {
                hlsInstance.destroy();
                hlsInstance = null;
            }

            const seekTarget = !hasAppliedResume && resumeSecond > 0
                ? resumeSecond
                : Math.floor(videoEl.currentTime || 0);

            if (videoUrl.includes(".m3u8") && window.Hls && window.Hls.isSupported()) {
                hlsInstance = new window.Hls({
                    enableWorker: true,
                    lowLatencyMode: true
                });
                hlsInstance.loadSource(videoUrl);
                hlsInstance.attachMedia(videoEl);
                hlsInstance.on(window.Hls.Events.MANIFEST_PARSED, function () {
                    if (seekTarget > 0) {
                        videoEl.currentTime = seekTarget;
                        hasAppliedResume = true;
                    }
                    videoEl.play().catch(function () {});
                });
            } else {
                videoEl.src = videoUrl;
                videoEl.addEventListener("loadedmetadata", function onMeta() {
                    videoEl.removeEventListener("loadedmetadata", onMeta);
                    if (seekTarget > 0 && seekTarget < videoEl.duration) {
                        videoEl.currentTime = seekTarget;
                        hasAppliedResume = true;
                    }
                    videoEl.play().catch(function () {});
                });
            }
        }

        // Khởi chạy Server đầu tiên khi vào trang
        const initialBtn = document.querySelector(".js-server-switch.active") || serverButtons[0];
        if (initialBtn) {
            loadStream(
                initialBtn.dataset.videoUrl,
                initialBtn.dataset.serverType,
                initialBtn.dataset.quality,
                initialBtn.dataset.subtitleUrl
            );
        } else if (videoEl && videoEl.dataset.src) {
            loadStream(videoEl.dataset.src, "HLS_SPEED", "1080P_60FPS", "");
        }

        // Bắt sự kiện chuyển đổi giữa các Server phát
        serverButtons.forEach(function (btn) {
            btn.addEventListener("click", function (e) {
                e.preventDefault();
                serverButtons.forEach(function (b) { b.classList.remove("active"); });
                btn.classList.add("active");

                loadStream(
                    btn.dataset.videoUrl,
                    btn.dataset.serverType,
                    btn.dataset.quality,
                    btn.dataset.subtitleUrl
                );
            });
        });

        // Các nút điều khiển nhanh dưới Player (Tua -10s, +10s, Tốc độ, Rạp, Toàn màn hình)
        const btnRewind = document.getElementById("btnRewind10");
        const btnForward = document.getElementById("btnForward10");
        const btnSpeed = document.getElementById("btnPlaybackSpeed");
        const btnTheater = document.getElementById("btnTheaterMode");
        const btnFullscreen = document.getElementById("btnFullscreen");

        if (btnRewind && videoEl) {
            btnRewind.addEventListener("click", function () {
                videoEl.currentTime = Math.max(0, (videoEl.currentTime || 0) - 10);
            });
        }
        if (btnForward && videoEl) {
            btnForward.addEventListener("click", function () {
                videoEl.currentTime = Math.min(videoEl.duration || 99999, (videoEl.currentTime || 0) + 10);
            });
        }
        if (btnSpeed && videoEl) {
            const speeds = [1.0, 1.25, 1.5, 2.0, 0.75];
            let idx = 0;
            btnSpeed.addEventListener("click", function () {
                idx = (idx + 1) % speeds.length;
                videoEl.playbackRate = speeds[idx];
                btnSpeed.textContent = speeds[idx] + "x";
            });
        }
        if (btnTheater && wrapperEl) {
            btnTheater.addEventListener("click", function () {
                wrapperEl.classList.toggle("theater-mode");
            });
        }
        if (btnFullscreen && canvasEl) {
            btnFullscreen.addEventListener("click", function () {
                if (!document.fullscreenElement) {
                    canvasEl.requestFullscreen().catch(function () {});
                } else {
                    document.exitFullscreen().catch(function () {});
                }
            });
        }
    });
})();