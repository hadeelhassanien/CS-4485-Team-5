package com.example.demo.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ClaimsController {

    private final RevenueService revenueService;

    public ClaimsController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueEstimateDTO> getRevenueEstimate(
            @RequestParam String genre) {
        return ResponseEntity.ok(revenueService.getRevenueEstimate(genre));
    }

    @PostMapping("/claim")
    public ResponseEntity<RevenueEstimateDTO> claimEarnings(
            @RequestParam String genre) {
        return ResponseEntity.ok(revenueService.claimEarnings(genre));
    }
}