package com.rafaellima.hojeafestaenossa.upload.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

    Page<Upload> findByEventIdAndVisibleTrueOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    Page<Upload> findAllByEventIdOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    void deleteById(UUID uploadId);

    long countByEventId(UUID eventId);

    long countByEventIdAndMediaType(UUID eventId, MediaType mediaType);

    long countByMediaType(MediaType mediaType);

    @Query("SELECT COALESCE(SUM(u.fileSize), 0L) FROM Upload u")
    long sumTotalFileSize();

    @Query("SELECT COALESCE(SUM(u.fileSize), 0L) FROM Upload u WHERE u.eventId = :eventId")
    long sumFileSizeByEventId(@Param("eventId") UUID eventId);

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, " +
           "COUNT(CASE WHEN media_type = 'PHOTO' THEN 1 END) as photos, " +
           "COUNT(CASE WHEN media_type = 'VIDEO' THEN 1 END) as videos, " +
           "COUNT(*) as total " +
           "FROM uploads " +
           "WHERE created_at >= :startDate " +
           "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> countUploadsByMonth(@Param("startDate") Instant startDate);

    @Query("SELECT e.name, COUNT(u.id) as total, " +
           "SUM(CASE WHEN u.mediaType = 'PHOTO' THEN 1 ELSE 0 END) as photos, " +
           "SUM(CASE WHEN u.mediaType = 'VIDEO' THEN 1 ELSE 0 END) as videos " +
           "FROM Event e LEFT JOIN Upload u ON u.eventId = e.id " +
           "GROUP BY e.id, e.name " +
           "HAVING COUNT(u.id) > 0 " +
           "ORDER BY total DESC " +
           "LIMIT 10")
    List<Object[]> findTop10EventsByUploads();

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') as month, " +
           "COALESCE(SUM(file_size), 0) / 1048576.0 as sizeMB " +
           "FROM uploads " +
           "WHERE created_at >= :startDate " +
           "GROUP BY TO_CHAR(created_at, 'YYYY-MM') " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> sumStorageByMonth(@Param("startDate") Instant startDate);

    List<Upload> findByEventIdOrderByMediaTypeAscCreatedAtAsc(UUID eventId);
}
