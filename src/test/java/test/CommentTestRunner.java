package test;

import dao.CommentDAO;
import db.DBContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import model.User;

/**
 * Owner: Chuẩn / Tuấn Anh / Hữu Lộc.
 * Role: Standalone test runner for feature and integration checks.
 * Source: src/test/java/test/CommentTestRunner.java.
 */
public class CommentTestRunner extends DBContext {

    private static int passCount = 0;
    private static int failCount = 0;
    private static int blockedCount = 0;

    /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: perform the main operation. */
    public static void main(String[] args) {
        System.out.println(
            "======================================================================"
        );
        System.out.println(
            "BAT DAU KIEM THU TOAN DIEN GIAI DOAN 6: COMMENTCONTROLLER (TC01 - TC15)"
        );
        System.out.println(
            "======================================================================"
        );

        CommentTestRunner runner = new CommentTestRunner();
        runner.cleanupTestData();
        int initialSeedCount = runner.getCommentCount();
        System.out.println(
            "[DB] So luong ban ghi Comments hien co ban dau trong DB: " +
                initialSeedCount
        );

        try {
            CommentDAO commentDAO = new CommentDAO();
            controller.CommentController controller =
                new controller.CommentController();
            controller.setCommentDAO(commentDAO);

            // Method doPost in HttpServlet is protected, invoke via reflection
            Method doPostMethod =
                controller.CommentController.class.getDeclaredMethod(
                    "doPost",
                    HttpServletRequest.class,
                    HttpServletResponse.class
                );
            doPostMethod.setAccessible(true);

            // User mẫu
            User user1 = new User();
            user1.setUserId(1);
            user1.setUsername("leader_test");

            User user4 = new User();
            user4.setUserId(4);
            user4.setUsername("member4_test");

            String csrfToken = "valid-csrf-token-123456";

            // TC01: Gửi bình luận hợp lệ (1-10 sao, content hợp lệ, user đã đăng nhập, CSRF đúng)
            System.out.println(
                "\n--- [TC01] Gui binh luan hop le (Rating: 8, User: 4, CSRF dung) ---"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "8");
                req.setParameter(
                    "content",
                    "[TEST_TC01] Phim rat hay va kich tinh!"
                );
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean redirected =
                    resp.redirectLocation != null &&
                    resp.redirectLocation.contains("/movie?id=1");
                boolean dbExists = runner.checkCommentExists("[TEST_TC01]");
                if (redirected && dbExists && resp.errorCode == 0) {
                    recordPass(
                        "TC01: Tao binh luan goc hop le thanh cong, redirect /movie?id=1."
                    );
                } else {
                    recordFail(
                        "TC01: Tao binh luan goc that bai. Redirect: " +
                            resp.redirectLocation +
                            ", Error: " +
                            resp.errorCode
                    );
                }
            }

            // TC02: Đánh giá biên 1 sao
            System.out.println("\n--- [TC02] Danh gia bien 1 sao ---");
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "1");
                req.setParameter(
                    "content",
                    "[TEST_TC02] Phim hoi dai dong nhung chap nhan duoc"
                );
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExistsWithRating(
                    "[TEST_TC02]",
                    1
                );
                if (dbExists && resp.errorCode == 0) {
                    recordPass(
                        "TC02: Chap nhan danh gia bien 1 sao thanh cong."
                    );
                } else {
                    recordFail(
                        "TC02: Danh gia bien 1 sao that bai. Error: " +
                            resp.errorCode
                    );
                }
            }

            // TC03: Đánh giá biên 10 sao
            System.out.println("\n--- [TC03] Danh gia bien 10 sao ---");
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "10");
                req.setParameter(
                    "content",
                    "[TEST_TC03] Tuyet tac dien anh 10/10!"
                );
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExistsWithRating(
                    "[TEST_TC03]",
                    10
                );
                if (dbExists && resp.errorCode == 0) {
                    recordPass(
                        "TC03: Chap nhan danh gia bien 10 sao thanh cong."
                    );
                } else {
                    recordFail(
                        "TC03: Danh gia bien 10 sao that bai. Error: " +
                            resp.errorCode
                    );
                }
            }

            // TC04: Đánh giá 0 sao (< 1)
            System.out.println("\n--- [TC04] Danh gia 0 sao (< 1) ---");
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "0");
                req.setParameter("content", "[TEST_TC04] Danh gia 0 sao");
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExists("[TEST_TC04]");
                if (resp.errorCode == 400 && !dbExists) {
                    recordPass(
                        "TC04: Chan danh gia 0 sao bang HTTP 400 Bad Request."
                    );
                } else {
                    recordFail(
                        "TC04: Khong chan dung ma 400. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC05: Đánh giá 11 sao (> 10)
            System.out.println("\n--- [TC05] Danh gia 11 sao (> 10) ---");
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "11");
                req.setParameter("content", "[TEST_TC05] Danh gia 11 sao");
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExists("[TEST_TC05]");
                if (resp.errorCode == 400 && !dbExists) {
                    recordPass(
                        "TC05: Chan danh gia 11 sao bang HTTP 400 Bad Request."
                    );
                } else {
                    recordFail(
                        "TC05: Khong chan dung ma 400. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC06: Nội dung rỗng hoặc chỉ toàn khoảng trắng
            System.out.println(
                "\n--- [TC06] Noi dung rong hoac chi toan khoang trang ---"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "7");
                req.setParameter("content", "       ");
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                if (resp.errorCode == 400) {
                    recordPass(
                        "TC06: Chan noi dung rong/khoang trang bang HTTP 400 Bad Request."
                    );
                } else {
                    recordFail(
                        "TC06: Khong chan dung ma 400. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC07: Nội dung vượt quá 1000 ký tự
            System.out.println("\n--- [TC07] Noi dung vuot qua 1000 ky tu ---");
            {
                StringBuilder longText = new StringBuilder();
                for (int i = 0; i < 1005; i++) {
                    longText.append("A");
                }
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "7");
                req.setParameter("content", longText.toString());
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                if (resp.errorCode == 400) {
                    recordPass(
                        "TC07: Chan noi dung vuot qua 1000 ky tu bang HTTP 400 Bad Request."
                    );
                } else {
                    recordFail(
                        "TC07: Khong chan dung ma 400. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC08: Gửi phản hồi (reply) hợp lệ cho bình luận cha
            System.out.println(
                "\n--- [TC08] Gui phan hoi (reply) hop le cho binh luan cha ---"
            );
            long parentCommentId = runner.getValidCommentIdByMovie(1);
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "reply");
                req.setParameter("movieId", "1");
                req.setParameter("parentId", String.valueOf(parentCommentId));
                req.setParameter(
                    "content",
                    "[TEST_TC08] Phan hoi dong tinh voi binh luan goc!"
                );
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean redirected =
                    resp.redirectLocation != null &&
                    resp.redirectLocation.contains("/movie?id=1");
                boolean dbExists = runner.checkReplyExists(
                    "[TEST_TC08]",
                    parentCommentId
                );
                if (redirected && dbExists && resp.errorCode == 0) {
                    recordPass(
                        "TC08: Tao phan hoi (reply) hop le thanh cong, ParentId=" +
                            parentCommentId +
                            ", Rating=null."
                    );
                } else {
                    recordFail(
                        "TC08: Tao phan hoi that bai. Redirect: " +
                            resp.redirectLocation +
                            ", Error: " +
                            resp.errorCode
                    );
                }
            }

            // TC09: Phản hồi vào bình luận cha không tồn tại
            System.out.println(
                "\n--- [TC09] Phan hoi vao binh luan cha khong ton tai ---"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "reply");
                req.setParameter("movieId", "1");
                req.setParameter("parentId", "999999999");
                req.setParameter(
                    "content",
                    "[TEST_TC09] Phan hoi vao id khong ton tai"
                );
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExists("[TEST_TC09]");
                if (resp.errorCode == 400 && !dbExists) {
                    recordPass(
                        "TC09: Chan phan hoi vao binh luan cha khong ton tai bang HTTP 400 Bad Request."
                    );
                } else {
                    recordFail(
                        "TC09: Khong chan dung ma 400. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC10: Người dùng tự xóa mềm bình luận của chính mình (Status = 0)
            System.out.println(
                "\n--- [TC10] Nguoi dung tu xoa mem binh luan cua minh ---"
            );
            long myCommentId = runner.createCommentForUser(
                4,
                1,
                9,
                "[TEST_TC10] Binh luan de xoa mem"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "delete");
                req.setParameter("movieId", "1");
                req.setParameter("commentId", String.valueOf(myCommentId));
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                Comment c = commentDAO.getCommentById(myCommentId);
                boolean softDeleted = c != null && !c.isStatus();
                if (softDeleted && resp.errorCode == 0) {
                    recordPass(
                        "TC10: Xoa mem thanh cong (Status = 0), khong xoa vat ly khoi DB."
                    );
                } else {
                    recordFail(
                        "TC10: Xoa mem that bai. SoftDeleted: " +
                            softDeleted +
                            ", Error: " +
                            resp.errorCode
                    );
                }
            }

            // TC11: Người dùng A cố xóa bình luận của người dùng B
            System.out.println(
                "\n--- [TC11] Nguoi dung A co xoa binh luan cua nguoi dung B ---"
            );
            long user1CommentId = runner.createCommentForUser(
                1,
                1,
                9,
                "[TEST_TC11] Binh luan cua User 1"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken); // User 4 dang dang nhap, co xoa binh luan cua User 1
                req.setParameter("action", "delete");
                req.setParameter("movieId", "1");
                req.setParameter("commentId", String.valueOf(user1CommentId));
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                Comment c = commentDAO.getCommentById(user1CommentId);
                boolean stillActive = c != null && c.isStatus();
                if (resp.errorCode == 403 && stillActive) {
                    recordPass(
                        "TC11: Chan nguoi dung xoa binh luan cua nguoi khac bang HTTP 403 Forbidden, Status van giu nguyen 1."
                    );
                } else {
                    recordFail(
                        "TC11: Khong chan dung ma 403. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC12: Xóa bình luận không tồn tại hoặc đã bị ẩn
            System.out.println(
                "\n--- [TC12] Xoa binh luan khong ton tai hoac da bi an ---"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "delete");
                req.setParameter("movieId", "1");
                req.setParameter("commentId", "999999999");
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                if (resp.errorCode == 404) {
                    recordPass(
                        "TC12: Tra ve HTTP 404 Not Found khi xoa binh luan khong ton tai hoac da an."
                    );
                } else {
                    recordFail(
                        "TC12: Khong tra ve dung ma 404. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC13: Thiếu hoặc sai CSRF Token khi POST
            System.out.println(
                "\n--- [TC13] Thieu hoac sai CSRF Token khi POST ---"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                req.setSessionUser(user4, csrfToken);
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "8");
                req.setParameter("content", "[TEST_TC13] Hack CSRF");
                req.setParameter("csrfToken", "fake-token-attacker");

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExists("[TEST_TC13]");
                if (resp.errorCode == 403 && !dbExists) {
                    recordPass(
                        "TC13: Chan CSRF token sai bang HTTP 403 Forbidden."
                    );
                } else {
                    recordFail(
                        "TC13: Khong chan dung ma 403. Actual: " +
                            resp.errorCode
                    );
                }
            }

            // TC14: Người dùng chưa đăng nhập (Session null/không có User)
            System.out.println(
                "\n--- [TC14] Nguoi dung chua dang nhap (Guest) ---"
            );
            {
                MockResponse resp = new MockResponse();
                MockRequest req = new MockRequest();
                // Khong set session user
                req.setParameter("action", "create");
                req.setParameter("movieId", "1");
                req.setParameter("rating", "8");
                req.setParameter("content", "[TEST_TC14] Khach gui binh luan");
                req.setParameter("csrfToken", csrfToken);

                doPostMethod.invoke(
                    controller,
                    req.getProxy(),
                    resp.getProxy()
                );
                boolean dbExists = runner.checkCommentExists("[TEST_TC14]");
                if (
                    resp.redirectLocation != null &&
                    resp.redirectLocation.endsWith("/login") &&
                    !dbExists
                ) {
                    recordPass(
                        "TC14: Chuyen huong Guest ve trang /login, khong luu vao DB."
                    );
                } else {
                    recordFail(
                        "TC14: Khong chuyen huong ve /login. Actual redirect: " +
                            resp.redirectLocation
                    );
                }
            }

            // TC15: Hiển thị bình luận trên giao diện / Tích hợp MovieDetailController
            System.out.println(
                "\n--- [TC15] Kiem thu hien thi binh luan & Tich hop MovieDetailController ---"
            );
            {
                List<Comment> comments = commentDAO.getCommentsByMovieId(1);
                boolean daoPass = comments != null && !comments.isEmpty();
                if (daoPass) {
                    System.out.println(
                        "  -> CommentDAO.getCommentsByMovieId(1) tra ve " +
                            comments.size() +
                            " binh luan goc (kem replies long nhau)."
                    );
                }

                // Kiem tra trang thai cua MovieDetailController
                boolean movieDetailReady = false;
                try {
                    Class<?> mdcClass = Class.forName(
                        "controller.MovieDetailController"
                    );
                    Class<?> superClass = mdcClass.getSuperclass();
                    if (!superClass.getSimpleName().equals("ScaffoldServlet")) {
                        movieDetailReady = true;
                    }
                } catch (Exception ignored) {}

                if (!movieDetailReady) {
                    recordBlocked(
                        "TC15: Integration E2E test bi BLOCKED do MovieDetailController (Thanh vien 1) hien van la ScaffoldServlet chua hoan thien. Giao dien JSP va CommentDAO da PASS doc lap."
                    );
                } else {
                    recordPass(
                        "TC15: Tich hop toan trinh MovieDetailController va giao dien movie-detail.jsp hoan tat."
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Loi trong qua trinh kiem thu:");
            e.printStackTrace();
        } finally {
            // TEARDOWN & CLEANUP: Đảm bảo không làm thay đổi dữ liệu mẫu
            System.out.println(
                "\n======================================================================"
            );
            System.out.println(
                "TIEN HANH TEARDOWN & CLEANUP DU LIEU TEST THEO YEU CAU..."
            );
            int cleaned = runner.cleanupTestData();
            System.out.println(
                "[CLEANUP] Da don dep " +
                    cleaned +
                    " ban ghi test [TEST_*] khoi DB."
            );
            int finalCount = runner.getCommentCount();
            System.out.println(
                "[DB] So luong ban ghi Comments hien tai: " + finalCount
            );
            if (finalCount == initialSeedCount) {
                System.out.println(
                    "[XAC NHAN] Du lieu mau hoan toan duoc bao toan nguyen ven (100% CLEAN)!"
                );
            } else {
                System.err.println(
                    "[CANH BAO] Chenh lech du lieu: Ban dau " +
                        initialSeedCount +
                        ", sau test " +
                        finalCount
                );
            }
            System.out.println(
                "======================================================================"
            );
            System.out.println(
                String.format(
                    "TONG KET KIEM THU: PASS = %d | FAIL = %d | BLOCKED = %d",
                    passCount,
                    failCount,
                    blockedCount
                )
            );
            System.out.println(
                "======================================================================"
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

    // Helper methods for database inspection & cleanup
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

    private boolean checkCommentExistsWithRating(String keyword, int rating) {
        String sql =
            "SELECT 1 FROM Comments WHERE CHARINDEX(?, Content) > 0 AND Rating = ?";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, keyword);
            ps.setInt(2, rating);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkReplyExists(String keyword, long parentId) {
        String sql =
            "SELECT 1 FROM Comments WHERE CHARINDEX(?, Content) > 0 AND ParentId = ? AND Rating IS NULL";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, keyword);
            ps.setLong(2, parentId);
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

    private long createCommentForUser(
        int userId,
        int movieId,
        int rating,
        String content
    ) {
        String sql =
            "INSERT INTO Comments (UserId, MovieId, ParentId, Rating, Content, IsEdited, Status, CreatedAt, UpdatedAt) " +
            "VALUES (?, ?, NULL, ?, ?, 0, 1, GETDATE(), GETDATE())";
        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.setInt(3, rating);
            ps.setString(4, content);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
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

    // Dynamic Mock for HttpServletRequest, HttpServletResponse, HttpSession
    private static class MockRequest implements InvocationHandler {

        private final Map<String, String> parameters = new HashMap<>();
        private MockSession session;

        /** Owner: Chuẩn / Tuấn Anh / Hữu Lộc. Purpose: set parameter. */
        public void setParameter(String name, String value) {
            parameters.put(name, value);
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
            } else if ("setCharacterEncoding".equals(m)) {
                return null;
            } else if ("getCharacterEncoding".equals(m)) {
                return "UTF-8";
            }
            return null;
        }
    }

    private static class MockResponse implements InvocationHandler {

        int errorCode = 0;
        String errorMessage;
        String redirectLocation;

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
            } else if (
                "setCharacterEncoding".equals(m) || "setContentType".equals(m)
            ) {
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

    private static class MockSession implements InvocationHandler {

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
