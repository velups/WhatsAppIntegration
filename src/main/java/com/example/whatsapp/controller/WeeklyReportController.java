package com.example.whatsapp.controller;

import com.example.whatsapp.entity.RecipientEntity;
import com.example.whatsapp.repository.RecipientRepository;
import com.example.whatsapp.service.WeeklyReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class WeeklyReportController {

    private final WeeklyReportService reportService;
    private final RecipientRepository recipientRepository;

    public WeeklyReportController(WeeklyReportService reportService,
                                  RecipientRepository recipientRepository) {
        this.reportService = reportService;
        this.recipientRepository = recipientRepository;
    }

    /**
     * Generate and send weekly report for a recipient
     * Sends via email or WhatsApp based on recipient's preference
     */
    @PostMapping("/weekly/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> sendWeeklyReport(@PathVariable String phoneNumber) {
        Map<String, Object> result = reportService.generateAndSendReport(phoneNumber);

        boolean success = (boolean) result.get("success");
        if (success) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Preview weekly report data without sending
     */
    @GetMapping("/weekly/{phoneNumber}/preview")
    public ResponseEntity<Map<String, Object>> previewWeeklyReport(@PathVariable String phoneNumber) {
        Optional<RecipientEntity> recipientOpt = recipientRepository.findByPhoneNumber(phoneNumber);
        if (recipientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Recipient not found: " + phoneNumber
            ));
        }

        try {
            Map<String, Object> reportData = reportService.generateReportData(phoneNumber, recipientOpt.get());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "report_data", reportData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
