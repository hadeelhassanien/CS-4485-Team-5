package com.example.demo.Claims;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000",
        "https://cs-4485-team-5.pages.dev",
        "https://165.232.136.214.sslip.io"
})
public class ClaimsController {

    private final RevenueService revenueService;

    public ClaimsController(
            RevenueService revenueService
    ) {
        this.revenueService = revenueService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueEstimateDTO> getRevenueEstimate(@RequestParam(defaultValue = "Action") String genre) {
        return ResponseEntity.ok(revenueService.getRevenueEstimate(genre));
    }

    @PostMapping("/claim")
    public ResponseEntity<RevenueEstimateDTO> claimEarnings(@RequestParam String genre) {
        return ResponseEntity.ok(revenueService.claimEarnings(genre));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadGenre(IllegalArgumentException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleNoBalance(IllegalStateException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }
}