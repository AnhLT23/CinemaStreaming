-- SQLCMD must read this UTF-8 file with -f 65001 to preserve Vietnamese text.
USE CinemaStreamingDB;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    UPDATE m
    SET
        Title = CASE ((m.MovieId - 1) % 12) + 1
            WHEN 1 THEN N'Nghịch Thiên Tu Luyện - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 2 THEN N'Bí Ẩn Huyền Giới - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 3 THEN N'Hành Trình Vạn Giới - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 4 THEN N'Mật Lệnh Hành Động - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 5 THEN N'Bóng Đêm Gọi Tên - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 6 THEN N'Chuyện Cười Bất Tận - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 7 THEN N'Hẹn Ước Mùa Hạ - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 8 THEN N'Du Hành Liên Sao - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 9 THEN N'Góc Khuất Cuộc Đời - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 10 THEN N'Ranh Giới Sinh Tồn - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            WHEN 11 THEN N'Gia Đình Là Nhà - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
            ELSE N'Hồ Sơ Tội Phạm - Phần ' + CONVERT(NVARCHAR(10), m.MovieId)
        END,
        Description = CASE ((m.MovieId - 1) % 12) + 1
            WHEN 1 THEN N'Một người trẻ bước vào con đường tu luyện, đối mặt thử thách và khám phá bí mật về thân thế.'
            WHEN 2 THEN N'Những thế lực đối đầu trong một thế giới huyền bí, nơi mỗi lựa chọn đều làm thay đổi vận mệnh.'
            WHEN 3 THEN N'Cuộc phiêu lưu hoạt hình qua nhiều vùng đất kỳ ảo với những người bạn đồng hành đặc biệt.'
            WHEN 4 THEN N'Đội đặc nhiệm chạy đua với thời gian để ngăn chặn một âm mưu nguy hiểm.'
            WHEN 5 THEN N'Những dấu hiệu kỳ lạ dẫn một nhóm bạn đến bí mật bị chôn giấu trong bóng tối.'
            WHEN 6 THEN N'Những tình huống dở khóc dở cười nối tiếp nhau trong một khu phố náo nhiệt.'
            WHEN 7 THEN N'Một cuộc gặp gỡ bất ngờ mở ra chuyện tình giữa hai người với những ước mơ khác biệt.'
            WHEN 8 THEN N'Phi hành đoàn khám phá hành tinh xa lạ và tìm cách đưa mọi người trở về an toàn.'
            WHEN 9 THEN N'Một người phải đối mặt với biến cố, lựa chọn khó khăn và những mối quan hệ nhiều chiều.'
            WHEN 10 THEN N'Nhóm người lạ phải hợp sức để sinh tồn trước thiên nhiên khắc nghiệt.'
            WHEN 11 THEN N'Câu chuyện ấm áp về tình thân, sự sẻ chia và những điều nhỏ bé trong cuộc sống.'
            ELSE N'Một điều tra viên lần theo manh mối để làm sáng tỏ vụ án nhiều uẩn khúc.'
        END
    FROM Movies m
    WHERE m.MovieId BETWEEN 21 AND 200
      AND m.OriginalTitle LIKE N'Cinema Noir Collection #%';

    ;WITH SampleGenrePairs AS (
        SELECT
            m.MovieId,
            CASE ((m.MovieId - 1) % 12) + 1
                WHEN 1 THEN 2
                WHEN 2 THEN 3
                WHEN 3 THEN 11
                WHEN 4 THEN 10
                WHEN 5 THEN 12
                WHEN 6 THEN 11
                WHEN 7 THEN 9
                WHEN 8 THEN 10
                WHEN 9 THEN 7
                WHEN 10 THEN 4
                WHEN 11 THEN 6
                ELSE 4
            END AS SecondaryGenreId
        FROM Movies m
        WHERE m.MovieId BETWEEN 21 AND 200
          AND m.OriginalTitle LIKE N'Cinema Noir Collection #%'
    )
    DELETE mg
    FROM MovieGenres mg
    JOIN SampleGenrePairs p ON p.MovieId = mg.MovieId
    WHERE mg.GenreId = ((p.MovieId + 4) % 12) + 1
      AND mg.GenreId <> p.SecondaryGenreId;

    ;WITH SampleGenrePairs AS (
        SELECT
            m.MovieId,
            CASE ((m.MovieId - 1) % 12) + 1
                WHEN 1 THEN 2
                WHEN 2 THEN 3
                WHEN 3 THEN 11
                WHEN 4 THEN 10
                WHEN 5 THEN 12
                WHEN 6 THEN 11
                WHEN 7 THEN 9
                WHEN 8 THEN 10
                WHEN 9 THEN 7
                WHEN 10 THEN 4
                WHEN 11 THEN 6
                ELSE 4
            END AS SecondaryGenreId
        FROM Movies m
        WHERE m.MovieId BETWEEN 21 AND 200
          AND m.OriginalTitle LIKE N'Cinema Noir Collection #%'
    )
    INSERT INTO MovieGenres (MovieId, GenreId)
    SELECT p.MovieId, p.SecondaryGenreId
    FROM SampleGenrePairs p
    WHERE NOT EXISTS (
          SELECT 1
          FROM MovieGenres mg
          WHERE mg.MovieId = p.MovieId
            AND mg.GenreId = p.SecondaryGenreId
      );

    DECLARE @maxEpisodeNumber INT = (
        SELECT ISNULL(MAX(TotalEpisodes), 1)
        FROM Movies
        WHERE MovieType IN ('SERIES', 'ANIMATION_3D')
    );

    ;WITH EpisodeNumbers AS (
        SELECT TOP (@maxEpisodeNumber)
            ROW_NUMBER() OVER (ORDER BY object_id) AS EpisodeNumber
        FROM sys.all_objects
    )
    INSERT INTO MovieEpisodes (MovieId, EpisodeNumber, EpisodeTitle, DurationSeconds)
    SELECT
        m.MovieId,
        n.EpisodeNumber,
        N'Tập ' + RIGHT(N'000' + CONVERT(NVARCHAR(10), n.EpisodeNumber), 3)
            + N' - Hành trình tiếp nối',
        CASE WHEN m.Duration > 0 THEN m.Duration * 60 ELSE 1458 END
    FROM Movies m
    CROSS JOIN EpisodeNumbers n
    WHERE m.Status = 'PUBLISHED'
      AND m.MovieType IN ('SERIES', 'ANIMATION_3D')
      AND n.EpisodeNumber <= m.TotalEpisodes
      AND NOT EXISTS (
          SELECT 1
          FROM MovieEpisodes e
          WHERE e.MovieId = m.MovieId
            AND e.EpisodeNumber = n.EpisodeNumber
      );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
