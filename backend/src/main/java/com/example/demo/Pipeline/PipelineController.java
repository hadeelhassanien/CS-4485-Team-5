package com.example.demo.Pipeline;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pipeline")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000", "https://cs-4485-team-5.pages.dev" })
public class PipelineController {

    private final PipelineDataService pipelineDataService;

    public PipelineController(PipelineDataService pipelineDataService) {
        this.pipelineDataService = pipelineDataService;
    }

    @PostMapping("/genres/classifications")
    public ResponseEntity<Map<String, Object>> ingestGenreClassifications(
            @RequestBody GenreBatchReq request
    ) {
        int count = pipelineDataService.upsertGenreClassifications(
                request != null ? request.getClassifications() : null
        );

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "updatedCount", count
        ));
    }

    @PostMapping("/revenue/profile")
    public ResponseEntity<RevenueModel> saveRevenueProfile(
            @RequestBody RevenueModelReq request
    ) {
        return ResponseEntity.ok(pipelineDataService.saveRevenueProfile(request));
    }

    @PostMapping("/claims/extracted")
    public ResponseEntity<Map<String, Object>> ingestExtractedClaims(
            @RequestBody ExtractedVideoClaimBatchReq request
    ) {
        int count = pipelineDataService.replaceExtractedClaims(
                request != null ? request.getClaims() : null
        );

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "savedCount", count
        ));
    }

    @PostMapping("/claims/raw-ds")
    public ResponseEntity<Map<String, Object>> ingestRawDsClaims(
            @RequestBody List<RawDsVideoClaimsReq> request
    ) {
        int count = pipelineDataService.importRawDsClaims(request);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "savedCount", count
        ));
    }
}