package com.example.demo.Pipeline;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ClaimsFetchScheduler {

    private final DsClaimsClient dsClaimsClient;
    private final PipelineDataService pipelineDataService;

    public ClaimsFetchScheduler(DsClaimsClient dsClaimsClient, PipelineDataService pipelineDataService) {
        this.dsClaimsClient = dsClaimsClient;
        this.pipelineDataService = pipelineDataService;
    }

    //will run 5 seconds after startup then every hour
    @Scheduled(fixedRate = 3600000, initialDelay = 5000)
    public void fetchAndIngestClaims() {
        List<RawDsVideoClaimsReq> claims = dsClaimsClient.fetchClaims();

        if (!claims.isEmpty()) {
            int saved = pipelineDataService.importRawDsClaims(claims);
            System.out.println("DS claims ingested into DB: " + saved);
        } else {
            System.out.println("No DS claims to ingest, skipping.");
        }
    }
}