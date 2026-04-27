package com.example.demo.Pipeline;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ExtractedVideoClaimRepo extends JpaRepository<ExtractedVideoClaim, Long> {

    List<ExtractedVideoClaim> findByVideoIdInOrderByConfidenceDescCreatedAtDesc(Collection<String> videoIds);

    List<ExtractedVideoClaim> findByGenreNameOrderByCreatedAtDesc(String genreName);

    void deleteByVideoId(String videoId);

    @Query("select distinct c.genreName from ExtractedVideoClaim c where c.genreName is not null order by c.genreName")
    List<String> findDistinctGenreNames();
}