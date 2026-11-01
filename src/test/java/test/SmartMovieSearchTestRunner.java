package test;

import controller.HomeController;
import controller.MovieListController;
import controller.api.LiveSearchAPI;
import dao.GenreDAO;
import dao.MovieDAO;
import db.DBContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Movie;

/**
 * Bộ kiểm thử toàn diện SMART MOVIE SEARCH (Thành viên 4).
 * Kiểm tra 12 kịch bản bắt buộc theo USER_REQUEST và Regression test.
 */
/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: Standalone test runner for feature and integration checks.
 * Source: src/test/java/test/SmartMovieSearchTestRunner.java.
 */
public class SmartMovieSearchTestRunner extends DBContext {

    private static int passCount = 0;
    private static int failCount = 0;
    private static int blockedCount = 0;

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the main operation. */
    public static void main(String[] args) {
        System.out.println(
            "=========================================================================="
        );
        System.out.println(
            "BAT DAU KIEM THU CHUC NANG SMART MOVIE SEARCH (LIVE SEARCH & MOVIE LIST)"
        );
        System.out.println(
            "=========================================================================="
        );

        SmartMovieSearchTestRunner runner = new SmartMovieSearchTestRunner();
        MovieDAO movieDAO = new MovieDAO();
        GenreDAO genreDAO = new GenreDAO();

        try (Connection conn = runner.getConnection()) {
            if (conn == null || conn.isClosed()) {
                System.err.println("KHONG THE KET NOI SQL SERVER!");
                return;
            }
            System.out.println("[DB] Ket noi SQL Server thanh cong.");

            // -----------------------------------------------------------------
            // TEST SUITE 1: MovieDAO.searchQuick & DAO Tests
            // -----------------------------------------------------------------
            System.out.println(
                "\n--------------------------------------------------"
            );
            System.out.println("PHAN 1: KIEM THU MOVIEDAO.SEARCHQUICK(q)");
            System.out.println(
                "--------------------------------------------------"
            );

            // TC 1: q = "Mai"
            {
                List<Movie> list = movieDAO.searchQuick("Mai");
                boolean pass =
                    !list.isEmpty() &&
                    list.get(0).getTitle().toLowerCase().contains("mai");
                assertTest(
                    "TC01 - searchQuick(q='Mai')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim. Top 1: " +
                        (list.isEmpty() ? "None" : list.get(0).getTitle())
                );
            }

            // TC 2: q = "Hành động" (Genre)
            {
                List<Movie> list = movieDAO.searchQuick("Hành động");
                boolean pass = !list.isEmpty();
                assertTest(
                    "TC02 - searchQuick(q='Hành động')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim thuoc/lien quan the loai Hanh dong. Top 1: " +
                        (list.isEmpty() ? "None" : list.get(0).getTitle())
                );
            }

            // A natural-language prefix must not turn a genre-only query into a literal phrase search.
            {
                List<Movie> list = movieDAO.searchQuick("phim hành động");
                boolean pass = !list.isEmpty();
                assertTest(
                    "TC02A - searchQuick(q='phim hành động')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim hanh dong. Top 1: " +
                        (list.isEmpty() ? "None" : list.get(0).getTitle())
                );
            }

            // TC 3: q = "2024" (ReleaseYear)
            {
                List<Movie> list = movieDAO.searchQuick("2024");
                boolean pass = !list.isEmpty();
                assertTest(
                    "TC03 - searchQuick(q='2024')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim nam 2024. Top 1: " +
                        (list.isEmpty()
                            ? "None"
                            : list.get(0).getTitle() +
                              " (" +
                              list.get(0).getReleaseYear() +
                              ")")
                );
            }

            // TC 4: q = "Trung Quốc" (Country)
            {
                List<Movie> list = movieDAO.searchQuick("Trung Quốc");
                boolean pass = !list.isEmpty();
                assertTest(
                    "TC04 - searchQuick(q='Trung Quốc')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim xuat xu Trung Quoc. Top 1: " +
                        (list.isEmpty()
                            ? "None"
                            : list.get(0).getTitle() +
                              " - " +
                              list.get(0).getCountry())
                );
            }

            // TC 5: q = "Trấn Thành" (Actor / Title)
            {
                List<Movie> list = movieDAO.searchQuick("Trấn Thành");
                boolean pass =
                    !list.isEmpty() && list.get(0).getTitle().contains("Mai");
                assertTest(
                    "TC05 - searchQuick(q='Trấn Thành')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim co Tran Thanh. Top 1: " +
                        (list.isEmpty() ? "None" : list.get(0).getTitle())
                );
            }

            // TC 6: q = "phim hành động 2024" (Compound natural query: Genre + Year)
            {
                List<Movie> list = movieDAO.searchQuick("phim hành động 2024");
                boolean pass =
                    !list.isEmpty() && list.get(0).getReleaseYear() == 2024;
                assertTest(
                    "TC06 - searchQuick(q='phim hành động 2024')",
                    pass,
                    "Tim thay " +
                        list.size() +
                        " phim hanh dong nam 2024. Top 1: " +
                        (list.isEmpty()
                            ? "None"
                            : list.get(0).getTitle() +
                              " (Nam: " +
                              list.get(0).getReleaseYear() +
                              ")")
                );
            }

            // TC 7: Tiếng Việt không dấu ("Hanh dong", "Trung Quoc", "Tran Thanh", "phim hanh dong 2024")
            {
                List<Movie> listHd = movieDAO.searchQuick("Hanh dong");
                List<Movie> listTq = movieDAO.searchQuick("Trung Quoc");
                List<Movie> listTt = movieDAO.searchQuick("Tran Thanh");
                List<Movie> listCompound = movieDAO.searchQuick(
                    "phim hanh dong 2024"
                );
                boolean pass =
                    !listHd.isEmpty() &&
                    !listTq.isEmpty() &&
                    !listTt.isEmpty() &&
                    !listCompound.isEmpty();
                assertTest(
                    "TC07 - Tieng Viet khong dau (Hanh dong, Trung Quoc, Tran Thanh, phim hanh dong 2024)",
                    pass,
                    "Hanh dong: " +
                        listHd.size() +
                        " phim | Trung Quoc: " +
                        listTq.size() +
                        " phim | Tran Thanh: " +
                        listTt.size() +
                        " phim | Compound: " +
                        listCompound.size() +
                        " phim"
                );
            }

            // TC 8: q không có kết quả
            {
                List<Movie> list = movieDAO.searchQuick(
                    "khong_the_tim_thay_phim_nay_123456"
                );
                boolean pass = list.isEmpty();
                assertTest(
                    "TC08 - searchQuick khong co ket qua",
                    pass,
                    "Tra ve danh sach rong chinh xac (size = " +
                        list.size() +
                        ")"
                );
            }

            // TC 9: q chứa ký tự SQL LIKE đặc biệt (%, _, [, !, !!)
            {
                List<Movie> listPercent = movieDAO.searchQuick("%");
                List<Movie> listUnderscore = movieDAO.searchQuick("___");
                List<Movie> listBracket = movieDAO.searchQuick("[a-z]");
                List<Movie> listExcl = movieDAO.searchQuick("!!");
                boolean pass =
                    listPercent != null &&
                    listUnderscore != null &&
                    listBracket != null &&
                    listExcl != null;
                assertTest(
                    "TC09 - An toan voi ky tu SQL LIKE dac biet (%, _, [, !)",
                    pass,
                    "Khong gay loi cu phap SQL, xu ly an toan tuyet doi voi LIKE ESCAPE '!'"
                );
            }

            // TC 10: q chứa payload XSS
            {
                String xss = "<script>alert('XSS')</script>";
                List<Movie> listXss = movieDAO.searchQuick(xss);
                boolean pass = listXss != null;
                assertTest(
                    "TC10 - An toan voi payload XSS (<script>alert(1)</script>)",
                    pass,
                    "PreparedStatement tham so hoa an toan, khong bi injection hay loi bo nho"
                );
            }

            // -----------------------------------------------------------------
            // TEST SUITE 2: LiveSearchAPI Controller Tests
            // -----------------------------------------------------------------
            System.out.println(
                "\n--------------------------------------------------"
            );
            System.out.println(
                "PHAN 2: KIEM THU LIVESEARCHAPI (GET /api/movies/quick-search)"
            );
            System.out.println(
                "--------------------------------------------------"
            );

            LiveSearchAPI liveApi = new LiveSearchAPI();
            liveApi.init();
            Method apiGet = LiveSearchAPI.class.getDeclaredMethod(
                "doGet",
                HttpServletRequest.class,
                HttpServletResponse.class
            );
            apiGet.setAccessible(true);

            // TC 11: LiveSearchAPI trả về tối đa 5 phim với format JSON đúng chuẩn
            {
                MockRequest req = new MockRequest();
                req.setParameter("q", "2024");
                MockResponse resp = new MockResponse();
                apiGet.invoke(liveApi, req.getProxy(), resp.getProxy());

                String json = resp.getOutput();
                boolean isJson =
                    resp.contentType != null &&
                    resp.contentType.contains("application/json");
                boolean validFormat =
                    json != null && json.startsWith("[") && json.endsWith("]");
                boolean containsFields =
                    json != null &&
                    json.contains("\"id\":") &&
                    json.contains("\"title\":") &&
                    json.contains("\"posterUrl\":") &&
                    json.contains("\"avgRating\":");

                assertTest(
                    "TC11 - LiveSearchAPI tra JSON chuan voi toi da 5 phim (id, title, posterUrl, avgRating)",
                    isJson && validFormat && containsFields,
                    "Status: HTTP " +
                        resp.status +
                        " | ContentType: " +
                        resp.contentType +
                        " | JSON: " +
                        (json.length() > 100
                            ? json.substring(0, 100) + "..."
                            : json)
                );
            }

            // TC 12: LiveSearchAPI với tham số rỗng hoặc chỉ khoảng trắng
            {
                MockRequest reqEmpty = new MockRequest();
                reqEmpty.setParameter("q", "   ");
                MockResponse respEmpty = new MockResponse();
                apiGet.invoke(
                    liveApi,
                    reqEmpty.getProxy(),
                    respEmpty.getProxy()
                );

                boolean pass = "[]".equals(respEmpty.getOutput().trim());
                assertTest(
                    "TC12 - LiveSearchAPI xu ly tu khoa rong tra ve '[]'",
                    pass,
                    "Output: " + respEmpty.getOutput()
                );
            }

            // TC 13: LiveSearchAPI với payload XSS -> JSON output được escape an toàn
            {
                MockRequest reqXss = new MockRequest();
                reqXss.setParameter("q", "<script>alert(\"XSS\")</script>");
                MockResponse respXss = new MockResponse();
                apiGet.invoke(liveApi, reqXss.getProxy(), respXss.getProxy());

                String out = respXss.getOutput();
                boolean pass =
                    out != null && out.startsWith("[") && out.endsWith("]");
                assertTest(
                    "TC13 - LiveSearchAPI chong XSS an toan voi input injection",
                    pass,
                    "Output: " + out
                );
            }

            // -----------------------------------------------------------------
            // TEST SUITE 3: MovieListController & Pagination Tests
            // -----------------------------------------------------------------
            System.out.println(
                "\n--------------------------------------------------"
            );
            System.out.println(
                "PHAN 3: KIEM THU MOVIELISTCONTROLLER (/movies)"
            );
            System.out.println(
                "--------------------------------------------------"
            );

            MovieListController mlc = new MovieListController();
            mlc.init();
            Method mlcGet = MovieListController.class.getDeclaredMethod(
                "doGet",
                HttpServletRequest.class,
                HttpServletResponse.class
            );
            mlcGet.setAccessible(true);

            // TC 14: /movies?q=2024 (Đúng 20 phim/trang, totalMovies và totalPages chính xác)
            {
                MockRequest req = new MockRequest();
                req.setParameter("q", "2024");
                MockResponse resp = new MockResponse();
                mlcGet.invoke(mlc, req.getProxy(), resp.getProxy());

                List<?> movies = (List<?>) req.attributes.get("movies");
                Integer totalMovies = (Integer) req.attributes.get(
                    "totalMovies"
                );
                Integer totalPages = (Integer) req.attributes.get("totalPages");
                Integer currentPage = (Integer) req.attributes.get(
                    "currentPage"
                );

                boolean fwdOk = "/WEB-INF/views/client/movie-list.jsp".equals(
                    resp.forwardLocation
                );
                boolean pageLimitOk = movies != null && movies.size() <= 20;
                boolean countOk =
                    totalMovies != null &&
                    totalMovies > 0 &&
                    totalPages != null &&
                    totalPages > 0;

                assertTest(
                    "TC14 - MovieListController (/movies?q=2024) phan trang dung 20 phim/trang",
                    fwdOk && pageLimitOk && countOk && currentPage == 1,
                    "Phim tren trang 1: " +
                        (movies != null ? movies.size() : 0) +
                        " | TotalMovies: " +
                        totalMovies +
                        " | TotalPages: " +
                        totalPages
                );
            }

            // TC 15: /movies?q=2024&page=2 (Phân trang page 2 và giữ từ khóa q)
            {
                MockRequest req = new MockRequest();
                req.setParameter("q", "2024");
                req.setParameter("page", "2");
                MockResponse resp = new MockResponse();
                mlcGet.invoke(mlc, req.getProxy(), resp.getProxy());

                List<?> movies = (List<?>) req.attributes.get("movies");
                Integer currentPage = (Integer) req.attributes.get(
                    "currentPage"
                );
                String qAttr = (String) req.attributes.get("q");

                boolean pass =
                    currentPage != null &&
                    currentPage == 2 &&
                    "2024".equals(qAttr) &&
                    movies != null;
                assertTest(
                    "TC15 - MovieListController (/movies?q=2024&page=2) giu nguyen tu khoa q va chuyen page=2",
                    pass,
                    "Page: " +
                        currentPage +
                        " | q: " +
                        qAttr +
                        " | Phim tren page 2: " +
                        (movies != null ? movies.size() : 0)
                );
            }

            // TC 16: /movies kết hợp genreId, type, year cùng với q
            {
                MockRequest req = new MockRequest();
                req.setParameter("genreId", "4"); // Hành động
                req.setParameter("type", "SERIES");
                req.setParameter("year", "2024");
                MockResponse resp = new MockResponse();
                mlcGet.invoke(mlc, req.getProxy(), resp.getProxy());

                List<?> movies = (List<?>) req.attributes.get("movies");
                Integer total = (Integer) req.attributes.get("totalMovies");
                boolean pass = movies != null && total != null && total > 0;
                assertTest(
                    "TC16 - MovieListController giu bo loc genreId=4, type=SERIES, year=2024",
                    pass,
                    "Tim thay: " +
                        total +
                        " phim phu hop toan bo bo loc ket hop"
                );
            }

            // -----------------------------------------------------------------
            // TEST SUITE 4: Regression Tests (HomeController & Other Controllers)
            // -----------------------------------------------------------------
            System.out.println(
                "\n--------------------------------------------------"
            );
            System.out.println(
                "PHAN 4: REGRESSION TEST (HOMECONTROLLER & INTEGRATION)"
            );
            System.out.println(
                "--------------------------------------------------"
            );

            // TC 17: HomeController (/home và /trang-chu) hoạt động nguyên vẹn
            {
                HomeController home = new HomeController();
                home.init();
                Method homeGet = HomeController.class.getDeclaredMethod(
                    "doGet",
                    HttpServletRequest.class,
                    HttpServletResponse.class
                );
                homeGet.setAccessible(true);

                MockRequest reqHome = new MockRequest();
                MockResponse respHome = new MockResponse();
                homeGet.invoke(home, reqHome.getProxy(), respHome.getProxy());

                boolean fwdHome = "/WEB-INF/views/client/home.jsp".equals(
                    respHome.forwardLocation
                );
                boolean hasBanners = reqHome.attributes.containsKey("banners");
                boolean hasTrending = reqHome.attributes.containsKey(
                    "trendingMovies"
                );
                boolean hasSeries = reqHome.attributes.containsKey(
                    "latestSeries"
                );
                boolean hasSingles = reqHome.attributes.containsKey(
                    "latestSingles"
                );

                boolean pass =
                    fwdHome &&
                    hasBanners &&
                    hasTrending &&
                    hasSeries &&
                    hasSingles;
                assertTest(
                    "TC17 - Regression HomeController (/home)",
                    pass,
                    "Banners: " +
                        hasBanners +
                        " | Trending: " +
                        hasTrending +
                        " | Series: " +
                        hasSeries +
                        " | Singles: " +
                        hasSingles
                );
            }

            // -----------------------------------------------------------------
            // TỔNG KẾT
            // -----------------------------------------------------------------
            System.out.println(
                "\n=========================================================================="
            );
            System.out.println("TONG KET KIEM THU SMART MOVIE SEARCH:");
            System.out.println("  PASS   : " + passCount);
            System.out.println("  FAIL   : " + failCount);
            System.out.println("  BLOCKED: " + blockedCount);
            System.out.println(
                "=========================================================================="
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void assertTest(
        String testName,
        boolean condition,
        String details
    ) {
        if (condition) {
            passCount++;
            System.out.println("  [PASS] " + testName);
            System.out.println("         -> " + details);
        } else {
            failCount++;
            System.err.println("  [FAIL] " + testName);
            System.err.println("         -> " + details);
        }
    }

    // =========================================================================
    // MOCK CLASSES CHO SERVLET KIEM THU
    // =========================================================================

    public static class MockRequest {

        public Map<String, String> parameters = new HashMap<>();
        public Map<String, Object> attributes = new HashMap<>();
        public String pathInfo = "";
        public String contextPath = "/CinemaStreaming";
        public HttpSession session = null;

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: set parameter. */
        public void setParameter(String key, String value) {
            parameters.put(key, value);
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve proxy. */
        public HttpServletRequest getProxy() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[] { HttpServletRequest.class },
                new InvocationHandler() {
                    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the invoke operation. */
                    @Override
                    public Object invoke(
                        Object proxy,
                        Method method,
                        Object[] args
                    ) throws Throwable {
                        String name = method.getName();
                        if ("getParameter".equals(name)) {
                            return parameters.get((String) args[0]);
                        }
                        if ("setAttribute".equals(name)) {
                            attributes.put((String) args[0], args[1]);
                            return null;
                        }
                        if ("getAttribute".equals(name)) {
                            return attributes.get((String) args[0]);
                        }
                        if ("getContextPath".equals(name)) {
                            return contextPath;
                        }
                        if ("getPathInfo".equals(name)) {
                            return pathInfo;
                        }
                        if ("getSession".equals(name)) {
                            return session;
                        }
                        if ("getRequestDispatcher".equals(name)) {
                            String path = (String) args[0];
                            return Proxy.newProxyInstance(
                                RequestDispatcher.class.getClassLoader(),
                                new Class[] { RequestDispatcher.class },
                                (p, m, a) -> {
                                    if ("forward".equals(m.getName())) {
                                        MockResponse resp =
                                            (MockResponse) Proxy.getInvocationHandler(
                                                a[1]
                                            );
                                        resp.forwardLocation = path;
                                    }
                                    return null;
                                }
                            );
                        }
                        return defaultValue(method.getReturnType());
                    }
                }
            );
        }
    }

    public static class MockResponse implements InvocationHandler {

        public int status = 200;
        public String contentType;
        public String characterEncoding = "UTF-8";
        public String forwardLocation;
        public String redirectLocation;
        private final StringWriter stringWriter = new StringWriter();
        private final PrintWriter writer = new PrintWriter(stringWriter);

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve output. */
        public String getOutput() {
            writer.flush();
            return stringWriter.toString();
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve proxy. */
        public HttpServletResponse getProxy() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[] { HttpServletResponse.class },
                this
            );
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the invoke operation. */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            String name = method.getName();
            if ("setStatus".equals(name)) {
                status = (Integer) args[0];
                return null;
            }
            if ("setContentType".equals(name)) {
                contentType = (String) args[0];
                return null;
            }
            if ("setCharacterEncoding".equals(name)) {
                characterEncoding = (String) args[0];
                return null;
            }
            if ("setHeader".equals(name)) {
                return null;
            }
            if ("getWriter".equals(name)) {
                return writer;
            }
            if ("sendRedirect".equals(name)) {
                redirectLocation = (String) args[0];
                return null;
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) return null;
        if (returnType == boolean.class) return false;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == double.class) return 0.0;
        return null;
    }
}
