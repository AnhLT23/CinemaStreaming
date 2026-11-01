(function () {
    document.addEventListener("DOMContentLoaded", function () {
        const videoEl = document.getElementById("videoPlayer");
        const iframeEl = document.getElementById("embedPlayer");
        const syncContainer = document.getElementById("watchSyncMeta");
        if (!syncContainer) return;

        const syncUrl = syncContainer.dataset.syncUrl;
        const episodeId = parseInt(syncContainer.dataset.episodeId || "0", 10);
        const fallbackDuration = parseInt(syncContainer.dataset.durationSeconds || "1458", 10);

        if (!syncUrl || episodeId <= 0) return;

        let virtualElapsed = parseInt(syncContainer.dataset.resumeSecond || "0", 10);

        function sendHeartbeat() {
            let currentSec = 0;
            let totalSec = fallbackDuration;

            if (videoEl && !videoEl.classList.contains("d-none")) {
                if (videoEl.paused || videoEl.ended) return;
                currentSec = Math.floor(videoEl.currentTime || 0);
                totalSec = Math.floor(videoEl.duration || fallbackDuration);
            } else if (iframeEl && !iframeEl.classList.contains("d-none")) {
                virtualElapsed += 10;
                currentSec = virtualElapsed;
            } else {
                return;
            }

            if (currentSec <= 0) return;

            const params = new URLSearchParams();
            params.set("episodeId", String(episodeId));
            params.set("lastPosition", String(currentSec));
            params.set("currentSecond", String(currentSec));
            params.set("durationSeconds", String(totalSec > 0 ? totalSec : fallbackDuration));

            fetch(syncUrl, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
                },
                body: params.toString()
            }).then(function (res) {
                if (res.status === 403) {
                    clearInterval(timerId);
                    if (videoEl) {
                        videoEl.pause();
                        videoEl.removeAttribute("src");
                        videoEl.load();
                    }
                    if (iframeEl) {
                        iframeEl.src = "about:blank";
                    }
                    alert("Thông báo: Quyền xem bộ phim này đã bị thu hồi bởi nhân viên hỗ trợ (REVOKED).");
                    window.location.href = syncContainer.dataset.movieDetailUrl || "home";
                }
            }).catch(function () {});
        }

        const timerId = setInterval(sendHeartbeat, 10000);
        window.addEventListener("beforeunload", sendHeartbeat);
    });
})();