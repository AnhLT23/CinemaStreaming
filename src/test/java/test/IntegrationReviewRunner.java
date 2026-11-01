package test;

import controller.CommentController;
import controller.HomeController;
import controller.MovieListController;
import controller.api.LiveSearchAPI;
import dao.BannerDAO;
import dao.CommentDAO;
import dao.GenreDAO;
import dao.MovieDAO;
import dao.WatchHistoryDAO;
import db.DBContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Comment;
import model.Genre;
import model.Movie;
import model.User;

/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: Standalone test runner for feature and integration checks.
 * Source: src/test/java/test/IntegrationReviewRunner.java.
 */
public class IntegrationReviewRunner extends DBContext {

    private static int passCount = 0;
    private static int failCount = 0;
    private static int blockedCount = 0;

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the main operation. */
    public static void main(String[] args) {
        System.out.println(
            "=========================================================================="
        );
        System.out.println(
            "GIAI DOAN 7: FINAL INTEGRATION REVIEW & HANDOVER - TEST SUITE"
        );
        System.out.println(
            "=========================================================================="
        );

        IntegrationReviewRunner runner = new IntegrationReviewRunner();
        runner.cleanupTestData();
        int initialCommentCount = runner.getCommentCount();
        System.out.println(
            "[DB] So luong Comments hien co ban dau trong DB: " +
                initialCommentCount
        );

        try {
            // =================================================================
            // 1. REGRESSION: HomeController (/home, /trang-chu)
            // =================================================================
            System.out.println(
                "\n[REGRESSION 1] Kiem tra HomeController (/home & /trang-chu)"
            );
            {
                HomeController homeController = new HomeController();
                homeController.init();

                Method doGet = HomeController.class.getDeclaredMethod(
                    "doGet",
                    HttpServletRequest.class,
                    HttpServletResponse.class
                );
                doGet.setAccessible(true);

                // Test /home
                MockRequest reqHome = new MockRequest();
                reqHome.setPathInfo("/home");
                MockResponse respHome = new MockResponse();
                doGet.invoke(
                    homeController,
                    reqHome.getProxy(),
                    respHome.getProxy()
                );

                boolean homeFwd = "/WEB-INF/views/client/home.jsp".equals(
                    respHome.forwardLocation
                );
                boolean hasBanners = reqHome.attributes.containsKey("banners");
                boolean hasTrending = reqHome.attributes.containsKey(
                    "trendingMovies"
                );

                // Test /trang-chu
                MockRequest reqTc = new MockRequest();
                reqTc.setPathInfo("/trang-chu");
                MockResponse respTc = new MockResponse();
                doGet.invoke(
                    homeController,
                    reqTc.getProxy(),
                    respTc.getProxy()
                );
                boolean tcFwd = "/WEB-INF/views/client/home.jsp".equals(
                    respTc.forwardLocation
                );

                if (homeFwd && hasBanners && hasTrending && tcFwd) {
                    recordPass(
                        "HomeController hoat dong on dinh tren ca /home va /trang-chu, load du lieu Banner & Movies chinh xac."
                    );
                } else {
                    recordFail(
                        "HomeController that bai. homeFwd=" +
                            homeFwd +
                            ", tcFwd=" +
                            tcFwd
                    );
                }
            }

            // =================================================================
            // 2. REGRESSION: MovieListController (/movies, ?page=2, ?genreId=1)
            // =================================================================
            System.out.println(
                "\n[REGRESSION 2] Kiem tra MovieListController (/movies, ?page=2, ?genreId=1)"
            );
            {
                MovieListController mlc = new MovieListController();
                mlc.init();

                Method doGet = MovieListController.class.getDeclaredMethod(
                    "doGet",
                    HttpServletRequest.class,
                    HttpServletResponse.class
                );
                doGet.setAccessible(true);

                // /movies (mac dinh page=1)
                MockRequest req1 = new MockRequest();
                MockResponse resp1 = new MockResponse();
                doGet.invoke(mlc, req1.getProxy(), resp1.getProxy());
                boolean fwd1 = "/WEB-INF/views/client/movie-list.jsp".equals(
                    resp1.forwardLocation
                );
                List<?> movies1 = (List<?>) req1.attributes.get("movies");

                // /movies?page=2
                MockRequest req2 = new MockRequest();
                req2.setParameter("page", "2");
                MockResponse resp2 = new MockResponse();
                doGet.invoke(mlc, req2.getProxy(), resp2.getProxy());
                Integer page2 = (Integer) req2.attributes.get("currentPage");

                // /movies?genreId=1
                MockRequest req3 = new MockRequest();
                req3.setParameter("genreId", "1");
                MockResponse resp3 = new MockResponse();
                doGet.invoke(mlc, req3.getProxy(), resp3.getProxy());
                Integer selectedGenre = (Integer) req3.attributes.get(
                    "selectedGenreId"
                );

                if (
                    fwd1 &&
                    movies1 != null &&
                    page2 != null &&
                    page2 == 2 &&
                    selectedGenre != null &&
                    selectedGenre == 1
                ) {
                    recordPass(
                        "MovieListController xu ly phan trang va loc theo genreId=1 chinh xac."
                    );
                } else {
                    recordFail("MovieListController that bai.");
                }
            }

            // =================================================================
            // 3. REGRESSION: LiveSearchAPI (/api/movies/quick-search?q=Mai)
            // =================================================================
            System.out.println(
                "\n[REGRESSION 3] Kiem tra LiveSearchAPI (/api/movies/quick-search?q=Mai)"
            );
            {
                LiveSearchAPI api = new LiveSearchAPI();
                api.init();

                Method doGet = LiveSearchAPI.class.getDeclaredMethod(
                    "doGet",
                    HttpServletRequest.class,
                    HttpServletResponse.class
                );
                doGet.setAccessible(true);

                MockRequest req = new MockRequest();
                req.setParameter("q", "Mai");
                MockResponse resp = new MockResponse();

                doGet.invoke(api, req.getProxy(), resp.getProxy());
                String json = resp.getOutput();

                boolean isJson =
                    resp.contentType != null &&
                    resp.contentType.contains("application/json");
                boolean containsResults =
                    json != null && json.startsWith("[") && json.endsWith("]");

                if (isJson && containsResults) {
                    recordPass(
                        "LiveSearchAPI tra ve application/json hop le cho tu khoa 'Mai'. JSON: " +
                            json
                    );
                } else {
                    recordFail(
                        "LiveSearchAPI that bai. ContentType: " +
                            resp.contentType +
                            ", Body: " +
                            json
                    );
                }
            }

            // =================================================================
            // 4. KIEM TRA BO SUNG YEU CAU 6: ParentId thuoc MovieId khac
            // =================================================================
            System.out.println(
                "\n[YEU CAU 6] Kiem tra ParentId thuoc MovieId khac & bao toan du lieu khi bi tu choi"
            );
            {
                CommentController commentController = new CommentController();
                CommentDAO commentDAO = new CommentDAO();
                commentController.setCommentDAO(commentDAO);

                Method doPost = CommentController.class.getDeclaredMethod(
                    "doPost",
                    HttpServletRequest.class,
                    HttpServletResponse.class
                );
                doPost.setAccessible(true);

                User user4 = new User();
                user4.setUserId(4);
                user4.setUsername("member4_test");

                String csrfToken = "valid-csrf-token";

                // Lay 1 comment hop le trong DB
                Comment parentComment = commentDAO.getCommentById(1L);
                long validParentId = parentComment.getCommentId();
                int actualParentMovieId = parentComment.getMovieId();
                int differentMovieId = actualParentMovieId == 1 ? 2 : 1;

                // Gui reply chi dinh movieId khac voi MovieId thuc te cua parent comment
                MockRequest reqCross = new MockRequest();
                reqCross.setSessionUser(user4, csrfToken);
                reqCross.setParameter("action", "reply");
                reqCross.setParameter(
                    "movieId",
                    String.valueOf(differentMovieId)
                );
                reqCross.setParameter(
                    "parentId",
                    String.valueOf(validParentId)
                );
                reqCross.setParameter(
                    "content",
                    "[TEST_CROSS_MOVIE] Co tinh reply cheo movie"
                );
                reqCross.setParameter("csrfToken", csrfToken);

                MockResponse respCross = new MockResponse();
                int countBefore = runner.getCommentCount();

                doPost.invoke(
                    commentController,
                    reqCross.getProxy(),
                    respCross.getProxy()
                );

                int countAfter = runner.getCommentCount();
                boolean rejected400 = respCross.errorCode == 400;
                boolean dataUnchanged =
                    countBefore == countAfter &&
                    !runner.checkCommentExists("TEST_CROSS_MOVIE");

                if (rejected400 && dataUnchanged) {
                    recordPass(
                        "Tu choi reply vao comment thuoc MovieId khac bang HTTP 400 Bad Request; Du lieu DB hoan toan khong bi thay doi."
                    );
                } else {
                    recordFail(
                        "Kiem tra cheo Movie that bai. Code: " +
                            respCross.errorCode +
                            ", Unchanged: " +
                            dataUnchanged
                    );
                }
            }

            // =================================================================
            // 5. PHU THUOC THANH VIEN 1 & 2 / 3
            // =================================================================
            System.out.println(
                "\n[PHU THUOC HE THONG] Kiem tra trang thai tich hop voi cac thanh vien khac"
            );
            {
                // Kiem tra MovieDetailController (Thanh vien 1)
                boolean mdcComplete = false;
                try {
                    Class<?> clazz = Class.forName(
                        "controller.MovieDetailController"
                    );
                    if (
                        !clazz
                            .getSuperclass()
                            .getSimpleName()
                            .equals("ScaffoldServlet")
                    ) {
                        mdcComplete = true;
                    }
                } catch (Exception ignored) {}

                if (!mdcComplete) {
                    recordBlocked(
                        "MovieDetailController (Thanh vien 1) van la ScaffoldServlet -> Integration test E2E xem chi tiet phim bi BLOCKED."
                    );
                } else {
                    recordPass("MovieDetailController da san sang tich hop.");
                }

                // Kiem tra AuthController (Thanh vien 3 / 2)
                boolean authComplete = false;
                try {
                    Class<?> clazz = Class.forName("controller.AuthController");
                    if (
                        !clazz
                            .getSuperclass()
                            .getSimpleName()
                            .equals("ScaffoldServlet")
                    ) {
                        authComplete = true;
                    }
                } catch (Exception ignored) {}

                if (!authComplete) {
                    recordBlocked(
                        "AuthController (Thanh vien 3) van la ScaffoldServlet -> Integration test E2E dang nhap form thuc te bi BLOCKED."
                    );
                } else {
                    recordPass("AuthController da san sang tich hop.");
                }
            }
        } catch (Exception e) {
            System.err.println("Loi trong qua trinh kiem thu tich hop:");
            e.printStackTrace();
        } finally {
            // TEARDOWN & CLEANUP
            int cleaned = runner.cleanupTestData();
            int finalCommentCount = runner.getCommentCount();
            System.out.println(
                "\n=========================================================================="
            );
            System.out.println("TEARDOWN & CLEANUP:");
            System.out.println(
                " - So luong ban ghi test da don dep: " + cleaned
            );
            System.out.println(
                " - So luong ban ghi Comments hien tai: " + finalCommentCount
            );
            System.out.println(
                " - Trang thai bao toan SQL: " +
                    (finalCommentCount == initialCommentCount
                        ? "100% NGUYEN VEN"
                        : "LECH DU LIEU")
            );
            System.out.println(
                "=========================================================================="
            );
            System.out.println(
                String.format(
                    "KET QUA: PASS = %d | FAIL = %d | BLOCKED = %d",
                    passCount,
                    failCount,
                    blockedCount
                )
            );
            System.out.println(
                "=========================================================================="
            );
        }
    }

    private static void recordPass(String msg) {
        passCount++;
        System.out.println("  [PASS] " + msg);
    }

    private static void recordFail(String msg) {
        failCount++;
        System.err.println("  [FAIL] " + msg);
    }

    private static void recordBlocked(String msg) {
        blockedCount++;
        System.out.println("  [BLOCKED] " + msg);
    }

    private int getCommentCount() {
        String sql = "SELECT COUNT(*) FROM Comments";
        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean checkCommentExists(String keyword) {
        String sql = "SELECT 1 FROM Comments WHERE CHARINDEX(?, Content) > 0";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private long getValidCommentIdByMovie(int movieId) {
        String sql =
            "SELECT TOP 1 CommentId FROM Comments WHERE MovieId = ? AND ParentId IS NULL AND Status = 1";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1L;
    }

    private int cleanupTestData() {
        String sql =
            "DELETE FROM Comments WHERE CHARINDEX('TEST_', Content) > 0";
        try (
            Connection conn = getConnection();
            Statement stmt = conn.createStatement()
        ) {
            return stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Mock dynamic proxy
    public static class MockRequest implements InvocationHandler {

        final Map<String, String> parameters = new HashMap<>();
        final Map<String, Object> attributes = new HashMap<>();
        private MockSession session;
        private String pathInfo = "";

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: set parameter. */
        public void setParameter(String name, String value) {
            parameters.put(name, value);
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: set path info. */
        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: set session user. */
        public void setSessionUser(User user, String csrfToken) {
            if (this.session == null) {
                this.session = new MockSession();
            }
            this.session.setAttribute("loggedInUser", user);
            this.session.setAttribute("csrfToken", csrfToken);
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve proxy. */
        public HttpServletRequest getProxy() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                this
            );
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the invoke operation. */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            String m = method.getName();
            if ("getParameter".equals(m)) {
                return parameters.get((String) args[0]);
            } else if ("setAttribute".equals(m)) {
                attributes.put((String) args[0], args[1]);
                return null;
            } else if ("getAttribute".equals(m)) {
                return attributes.get((String) args[0]);
            } else if ("getSession".equals(m)) {
                if (
                    args != null &&
                    args.length > 0 &&
                    args[0] instanceof Boolean
                ) {
                    boolean create = (Boolean) args[0];
                    if (session == null && create) {
                        session = new MockSession();
                    }
                    return session != null ? session.getProxy() : null;
                }
                if (session == null) {
                    session = new MockSession();
                }
                return session.getProxy();
            } else if ("getContextPath".equals(m)) {
                return "";
            } else if ("getPathInfo".equals(m)) {
                return pathInfo;
            } else if ("getServletPath".equals(m)) {
                return pathInfo;
            } else if ("getRequestURI".equals(m)) {
                return pathInfo;
            } else if ("setCharacterEncoding".equals(m)) {
                return null;
            } else if ("getCharacterEncoding".equals(m)) {
                return "UTF-8";
            } else if ("getRequestDispatcher".equals(m)) {
                final String target = (String) args[0];
                return Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[] { jakarta.servlet.RequestDispatcher.class },
                    (dProxy, dMethod, dArgs) -> {
                        if ("forward".equals(dMethod.getName())) {
                            Object respArg = dArgs[1];
                            if (Proxy.isProxyClass(respArg.getClass())) {
                                InvocationHandler handler =
                                    Proxy.getInvocationHandler(respArg);
                                if (handler instanceof MockResponse) {
                                    ((MockResponse) handler).forwardLocation =
                                        target;
                                }
                            }
                        }
                        return null;
                    }
                );
            }
            return null;
        }
    }

    public static class MockResponse implements InvocationHandler {

        int errorCode = 0;
        String errorMessage;
        String redirectLocation;
        String forwardLocation;
        String contentType;
        private final StringWriter stringWriter = new StringWriter();
        private final PrintWriter printWriter = new PrintWriter(stringWriter);

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve output. */
        public String getOutput() {
            printWriter.flush();
            return stringWriter.toString();
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve proxy. */
        public HttpServletResponse getProxy() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { HttpServletResponse.class },
                this
            );
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the invoke operation. */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            String m = method.getName();
            if ("sendError".equals(m)) {
                this.errorCode = (Integer) args[0];
                if (args.length > 1) this.errorMessage = (String) args[1];
                return null;
            } else if ("sendRedirect".equals(m)) {
                this.redirectLocation = (String) args[0];
                return null;
            } else if ("setContentType".equals(m)) {
                this.contentType = (String) args[0];
                return null;
            } else if ("getWriter".equals(m)) {
                return printWriter;
            } else if ("setCharacterEncoding".equals(m)) {
                return null;
            } else if ("getStatus".equals(m)) {
                return errorCode != 0
                    ? errorCode
                    : redirectLocation != null
                      ? 302
                      : 200;
            }
            return null;
        }
    }

    public static class MockSession implements InvocationHandler {

        private final Map<String, Object> attributes = new HashMap<>();
        private final String id = "mock-session-" + System.currentTimeMillis();

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: set attribute. */
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: retrieve proxy. */
        public HttpSession getProxy() {
            return (HttpSession) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { HttpSession.class },
                this
            );
        }

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the invoke operation. */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            String m = method.getName();
            if ("getAttribute".equals(m)) {
                return attributes.get((String) args[0]);
            } else if ("setAttribute".equals(m)) {
                attributes.put((String) args[0], args[1]);
                return null;
            } else if ("removeAttribute".equals(m)) {
                return attributes.remove((String) args[0]);
            } else if ("getId".equals(m)) {
                return id;
            }
            return null;
        }
    }
}
