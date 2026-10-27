package com.example.pbj2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/mock")
public class MockApiController {
  @PostMapping("/credit-assessment")
  public ResponseEntity<?> credit(@RequestBody Map<String,Object> body) {
    String id = String.valueOf(body.getOrDefault("applicantId", ""));
    if (id.equalsIgnoreCase("API_ERROR")) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "simulated credit service outage"));
    double income = num(body.get("monthlyIncome"));
    double amount = num(body.get("requestedAmount"));
    int score = id.toUpperCase(Locale.ROOT).contains("LOW") ? 520 : (amount > income * 12 ? 630 : 740);
    return ResponseEntity.ok(Map.of("creditScore", score, "rating", score >= 700 ? "GOOD" : score >= 600 ? "FAIR" : "POOR", "assessmentId", "CR-" + UUID.randomUUID()));
  }

  @PostMapping("/payments")
  public ResponseEntity<?> payment(@RequestBody Map<String,Object> body) {
    String token = String.valueOf(body.getOrDefault("paymentToken", ""));
    if (token.equalsIgnoreCase("FAIL_SERVICE")) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "simulated payment service outage"));
    if (token.equalsIgnoreCase("DECLINE")) return ResponseEntity.ok(Map.of("status", "DECLINED", "reference", "PAY-DECLINED"));
    return ResponseEntity.ok(Map.of("status", "APPROVED", "reference", "PAY-" + UUID.randomUUID()));
  }

  @GetMapping("/slots")
  public ResponseEntity<?> slots(@RequestParam(defaultValue="MORNING") String preference, @RequestParam(defaultValue="Routine") String category) {
    if (preference.equalsIgnoreCase("API_ERROR")) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "simulated scheduling service outage"));
    List<Map<String,String>> slots = preference.equalsIgnoreCase("NONE") ? List.of() : List.of(
      Map.of("slotId", "SLOT-101", "date", "2026-11-02", "time", "09:30", "preference", preference),
      Map.of("slotId", "SLOT-102", "date", "2026-11-02", "time", "11:00", "preference", preference));
    return ResponseEntity.ok(Map.of("slots", slots, "category", category));
  }
  private static double num(Object o) { try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0; } }
}
