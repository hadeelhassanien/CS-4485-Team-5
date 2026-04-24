package com.example.demo.Pipeline;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExtractedVideoClaimRepo extends JpaRepository<ExtractedVideoClaim, Long> {

    List<ExtractedVideoClaim> findByVideoIdInOrderByConfidenceDescCreatedAtDesc(Collection<String> videoIds);

    void deleteByVideoId(String videoId);
}