package ultil;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bộ phân tích truy vấn tìm kiếm thông minh (Smart Search Query Parser).
 * Hỗ trợ nhận diện tự nhiên: Thể loại (Genre), Năm (Year), Loại phim (MovieType),
 * Quốc gia (Country) và từ khóa chính mà không phụ thuộc thư viện nặng hay AI API.
 */
/**
 * Owner: Chuẩn.
 * Role: Shared utility for the assigned feature workflow.
 * Source: src/main/java/ultil/SearchQueryParser.java.
 */
public class SearchQueryParser {

    private static final Pattern YEAR_PATTERN = Pattern.compile(
        "\\b(19\\d\\d|20[0-3]\\d)\\b"
    );

    public static class GenreDef {

        private final int id;
        private final String name;
        private final String[] aliases;

        public GenreDef(int id, String name, String... aliases) {
            this.id = id;
            this.name = name;
            this.aliases = aliases;
        }

        /** Owner: Chuẩn. Purpose: retrieve id. */
        public int getId() {
            return id;
        }

        /** Owner: Chuẩn. Purpose: retrieve name. */
        public String getName() {
            return name;
        }

        /** Owner: Chuẩn. Purpose: retrieve aliases. */
        public String[] getAliases() {
            return aliases;
        }
    }

    private static final List<GenreDef> GENRES = new ArrayList<>();

    static {
        GENRES.add(new GenreDef(1, "Tiên hiệp", "tien hiep", "tiên hiệp"));
        GENRES.add(
            new GenreDef(2, "Huyền huyễn", "huyen huyen", "huyền huyễn")
        );
        GENRES.add(
            new GenreDef(
                3,
                "Hoạt hình 3D",
                "hoat hinh 3d",
                "hoạt hình 3d",
                "hoat hinh",
                "hoạt hình",
                "anime"
            )
        );
        GENRES.add(
            new GenreDef(4, "Hành động", "hanh dong", "hành động", "action")
        );
        GENRES.add(new GenreDef(5, "Kinh dị", "kinh di", "kinh dị", "horror"));
        GENRES.add(
            new GenreDef(
                6,
                "Hài hước",
                "hai huoc",
                "hài hước",
                "phim hai",
                "phim hài",
                "comedy"
            )
        );
        GENRES.add(
            new GenreDef(
                7,
                "Tình cảm",
                "tinh cam",
                "tình cảm",
                "lang man",
                "lãng mạn",
                "romance"
            )
        );
        GENRES.add(
            new GenreDef(
                8,
                "Khoa học viễn tưởng",
                "khoa hoc vien tuong",
                "khoa học viễn tưởng",
                "vien tuong",
                "viễn tưởng",
                "sci-fi"
            )
        );
        GENRES.add(
            new GenreDef(
                9,
                "Tâm lý xã hội",
                "tam ly xa hoi",
                "tâm lý xã hội",
                "tam ly",
                "tâm lý",
                "drama"
            )
        );
        GENRES.add(
            new GenreDef(10, "Phiêu lưu", "phieu luu", "phiêu lưu", "adventure")
        );
        GENRES.add(
            new GenreDef(11, "Gia đình", "gia dinh", "gia đình", "family")
        );
        GENRES.add(
            new GenreDef(
                12,
                "Hình sự & Tội phạm",
                "hinh su toi pham",
                "hình sự tội phạm",
                "hinh su",
                "hình sự",
                "toi pham",
                "tội phạm",
                "crime"
            )
        );
    }

    public static class ParsedQuery {

        private String rawQuery;
        private String keyword;
        private Integer detectedYear;
        private Integer detectedGenreId;
        private String detectedGenreName;
        private String detectedMovieType;
        private String detectedCountry;
        private boolean compound;

        /** Owner: Chuẩn. Purpose: retrieve raw query. */
        public String getRawQuery() {
            return rawQuery;
        }

        /** Owner: Chuẩn. Purpose: set raw query. */
        public void setRawQuery(String rawQuery) {
            this.rawQuery = rawQuery;
        }

        /** Owner: Chuẩn. Purpose: retrieve keyword. */
        public String getKeyword() {
            return keyword;
        }

        /** Owner: Chuẩn. Purpose: set keyword. */
        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        /** Owner: Chuẩn. Purpose: retrieve detected year. */
        public Integer getDetectedYear() {
            return detectedYear;
        }

        /** Owner: Chuẩn. Purpose: set detected year. */
        public void setDetectedYear(Integer detectedYear) {
            this.detectedYear = detectedYear;
        }

        /** Owner: Chuẩn. Purpose: retrieve detected genre id. */
        public Integer getDetectedGenreId() {
            return detectedGenreId;
        }

        /** Owner: Chuẩn. Purpose: set detected genre id. */
        public void setDetectedGenreId(Integer detectedGenreId) {
            this.detectedGenreId = detectedGenreId;
        }

        /** Owner: Chuẩn. Purpose: retrieve detected genre name. */
        public String getDetectedGenreName() {
            return detectedGenreName;
        }

        /** Owner: Chuẩn. Purpose: set detected genre name. */
        public void setDetectedGenreName(String detectedGenreName) {
            this.detectedGenreName = detectedGenreName;
        }

        /** Owner: Chuẩn. Purpose: retrieve detected movie type. */
        public String getDetectedMovieType() {
            return detectedMovieType;
        }

        /** Owner: Chuẩn. Purpose: set detected movie type. */
        public void setDetectedMovieType(String detectedMovieType) {
            this.detectedMovieType = detectedMovieType;
        }

        /** Owner: Chuẩn. Purpose: retrieve detected country. */
        public String getDetectedCountry() {
            return detectedCountry;
        }

        /** Owner: Chuẩn. Purpose: set detected country. */
        public void setDetectedCountry(String detectedCountry) {
            this.detectedCountry = detectedCountry;
        }

        /** Owner: Chuẩn. Purpose: check whether compound. */
        public boolean isCompound() {
            return compound;
        }

        /** Owner: Chuẩn. Purpose: set compound. */
        public void setCompound(boolean compound) {
            this.compound = compound;
        }

        /** Owner: Chuẩn. Purpose: format the object as readable text. */
        @Override
        public String toString() {
            return (
                "ParsedQuery{" +
                "raw='" +
                rawQuery +
                '\'' +
                ", kw='" +
                keyword +
                '\'' +
                ", year=" +
                detectedYear +
                ", genreId=" +
                detectedGenreId +
                " (" +
                detectedGenreName +
                ")" +
                ", type='" +
                detectedMovieType +
                '\'' +
                ", country='" +
                detectedCountry +
                '\'' +
                ", compound=" +
                compound +
                '}'
            );
        }
    }

    /**
     * Phân tích chuỗi tìm kiếm tự nhiên thành các tiêu chí có cấu trúc.
     * Owner: Chuẩn.
     */
    public static ParsedQuery parse(String raw) {
        if (raw == null) {
            return null;
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.length() > 100) {
            trimmed = trimmed.substring(0, 100).trim();
        }

        ParsedQuery pq = new ParsedQuery();
        pq.setRawQuery(trimmed);

        String working = trimmed;
        String workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);

        // 1. Nhận diện năm phát hành (4 chữ số: 1900 - 2039)
        Matcher ym = YEAR_PATTERN.matcher(working);
        if (ym.find()) {
            try {
                int y = Integer.parseInt(ym.group(1));
                if (y >= 1900 && y <= 2030) {
                    pq.setDetectedYear(y);
                    working =
                        working.substring(0, ym.start()) +
                        " " +
                        working.substring(ym.end());
                    workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
                }
            } catch (NumberFormatException ignored) {}
        }

        // 2. Nhận diện loại phim (MovieType: SERIES / SINGLE / ANIMATION_3D)
        if (workingNorm.contains("phim bo") || workingNorm.contains("series")) {
            pq.setDetectedMovieType("SERIES");
            working = working.replaceAll(
                "(?i)\\b(phim\\s+bộ|phim\\s+bo|series)\\b",
                " "
            );
            workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
        } else if (
            workingNorm.contains("phim le") || workingNorm.contains("single")
        ) {
            pq.setDetectedMovieType("SINGLE");
            working = working.replaceAll(
                "(?i)\\b(phim\\s+lẻ|phim\\s+le|single)\\b",
                " "
            );
            workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
        }

        // 3. Nhận diện thể loại phim (Genres)
        for (GenreDef g : GENRES) {
            boolean matched = false;
            for (String alias : g.getAliases()) {
                String aliasNorm = stripAccents(alias).toLowerCase(Locale.ROOT);
                Pattern p = Pattern.compile(
                    "(?i)\\b" + Pattern.quote(aliasNorm) + "\\b"
                );
                Matcher m = p.matcher(workingNorm);
                if (m.find()) {
                    pq.setDetectedGenreId(g.getId());
                    pq.setDetectedGenreName(g.getName());
                    // Xóa token thể loại khỏi chuỗi
                    working =
                        working.substring(0, m.start()) +
                        " " +
                        working.substring(m.end());
                    workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
                    matched = true;
                    break;
                }
            }
            if (matched) {
                break;
            }
        }

        // 4. Nhận diện quốc gia phổ biến
        if (
            workingNorm.contains("trung quoc") ||
            workingNorm.contains("trung quốc")
        ) {
            pq.setDetectedCountry("Trung Quốc");
            working = working.replaceAll(
                "(?i)\\b(trung\\s+quốc|trung\\s+quoc)\\b",
                " "
            );
            workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
        } else if (
            workingNorm.contains("viet nam") || workingNorm.contains("việt nam")
        ) {
            pq.setDetectedCountry("Việt Nam");
            working = working.replaceAll(
                "(?i)\\b(việt\\s+nam|viet\\s+nam)\\b",
                " "
            );
            workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
        } else if (
            workingNorm.contains("au my") || workingNorm.contains("âu mỹ")
        ) {
            pq.setDetectedCountry("Âu Mỹ");
            working = working.replaceAll("(?i)\\b(âu\\s+mỹ|au\\s+my)\\b", " ");
            workingNorm = stripAccents(working).toLowerCase(Locale.ROOT);
        }

        // 5. Loại bỏ từ đệm phổ biến ở đầu/cuối: "phim", "xem", "tìm"
        working = working.replaceAll(
            "(?i)^\\s*(phim|xem\\s+phim|tim\\s+phim|tìm\\s+phim)\\s+",
            " "
        );
        working = working.replaceAll("(?i)\\s+(phim)$", " ");
        working = working.replaceAll("\\s+", " ").trim();

        if (!working.isEmpty()) {
            pq.setKeyword(working);
        } else {
            pq.setKeyword(null);
        }

        // Xác định cờ compound: khi có ít nhất 1 tiêu chí đặc tả (thể loại, năm, loại phim) kết hợp
        boolean hasFilter =
            pq.getDetectedGenreId() != null ||
            pq.getDetectedYear() != null ||
            pq.getDetectedMovieType() != null ||
            pq.getDetectedCountry() != null;

        // Nếu chuỗi hoàn toàn chỉ là từ khóa tự do (ví dụ "Mai", "Trấn Thành") -> không coi là compound
        // Nhưng nếu là "phim hành động 2024" hoặc "hành động 2024" -> compound = true
        pq.setCompound(hasFilter);

        return pq;
    }

    /**
     * Bỏ dấu tiếng Việt phục vụ so khớp mờ.
     * Owner: Chuẩn.
     */
    public static String stripAccents(String s) {
        if (s == null) {
            return "";
        }
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll(
            "\\p{InCombiningDiacriticalMarks}+",
            ""
        );
        return withoutAccents.replace("đ", "d").replace("Đ", "D");
    }

    /**
     * Thoát các ký tự đại diện đặc biệt của SQL Server LIKE: !, %, _, [
     * Owner: Chuẩn.
     */
    public static String escapeLike(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")
            .replace("[", "![");
    }
}
