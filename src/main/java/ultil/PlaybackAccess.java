package ultil;

import dao.MovieRentalDAO;
import jakarta.servlet.http.HttpSession;
import java.sql.Timestamp;
import model.Movie;
import model.MovieRental;
import model.User;

/**
 * Owner: Tuấn Anh.
 * Role: Shared utility for the assigned feature workflow.
 * Source: src/main/java/ultil/PlaybackAccess.java.
 */
public final class PlaybackAccess {

    public enum Result {
        ALLOWED,
        LOGIN_REQUIRED,
        VIP_REQUIRED,
        RENT_REQUIRED,
        RENTAL_EXPIRED,
        RENTAL_REVOKED,
    }

    private PlaybackAccess() {}

    /** Owner: Tuấn Anh. Purpose: retrieve the signed-in user from the session. */
    public static User currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object u = session.getAttribute("loggedInUser");
        if (u instanceof User) {
            return (User) u;
        }
        Object fallback = session.getAttribute("user");
        return fallback instanceof User ? (User) fallback : null;
    }

    /** Owner: Tuấn Anh. Purpose: check whether the signed-in user is an administrator. */
    public static boolean isAdmin(HttpSession session) {
        User u = currentUser(session);
        if (u == null) {
            return false;
        }
        return u.getRoleId() == 1 || "ADMIN".equalsIgnoreCase(u.getRoleName());
    }

    /** Owner: Tuấn Anh. Purpose: check staff or administrator access. */
    public static boolean isStaffOrAdmin(HttpSession session) {
        User u = currentUser(session);
        if (u == null) {
            return false;
        }
        return (
            u.getRoleId() == 1 ||
            u.getRoleId() == 2 ||
            "ADMIN".equalsIgnoreCase(u.getRoleName()) ||
            "STAFF".equalsIgnoreCase(u.getRoleName())
        );
    }

    /** Owner: Tuấn Anh. Purpose: check whether the user VIP entitlement is active. */
    public static boolean isVipActive(User user) {
        if (user == null || !user.isVip()) {
            return false;
        }
        Timestamp exp = user.getVipExpireDate();
        return (
            exp == null || exp.after(new Timestamp(System.currentTimeMillis()))
        );
    }

    /** Owner: Tuấn Anh. Purpose: perform the check operation. */
    public static Result check(
        HttpSession session,
        Movie movie,
        MovieRentalDAO rentalDAO
    ) {
        if (movie == null) {
            return Result.RENT_REQUIRED;
        }
        if (isStaffOrAdmin(session)) {
            return Result.ALLOWED;
        }

        // Phim thường miễn phí: cho phép xem trực tiếp ngay từ trang Home
        if (!movie.isVipOnly() && !movie.isRentOnly()) {
            return Result.ALLOWED;
        }

        User user = currentUser(session);

        if (movie.isVipOnly() && !movie.isRentOnly()) {
            if (user == null) {
                return Result.VIP_REQUIRED;
            }
            return isVipActive(user) ? Result.ALLOWED : Result.VIP_REQUIRED;
        }

        if (movie.isRentOnly()) {
            if (user == null) {
                return Result.LOGIN_REQUIRED;
            }
            MovieRental active = rentalDAO.getActiveRental(
                user.getUserId(),
                movie.getMovieId()
            );
            if (active != null) {
                return Result.ALLOWED;
            }
            MovieRental latest = rentalDAO.getLatestRental(
                user.getUserId(),
                movie.getMovieId()
            );
            if (latest != null) {
                if ("REVOKED".equalsIgnoreCase(latest.getStatus())) {
                    return Result.RENTAL_REVOKED;
                }
                return Result.RENTAL_EXPIRED;
            }
            return Result.RENT_REQUIRED;
        }

        return Result.ALLOWED;
    }
}
