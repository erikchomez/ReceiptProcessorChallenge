package com.example.controller;


import com.example.model.Points;
import com.example.model.Receipt;
import com.example.model.Process;
import com.example.service.ReceiptProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/receipts")
@Slf4j
public class ReceiptProcessorController {

    private final ReceiptProcessorService receiptProcessorService;

    public ReceiptProcessorController(ReceiptProcessorService receiptProcessorService) {
        this.receiptProcessorService = receiptProcessorService;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPoints(@RequestBody Receipt receipt) {
        if (receipt == null) {
            log.error("Receipt is null");
            return createErrorResponse(HttpStatus.BAD_REQUEST, "The receipt is invalid.");
        }

        log.info("Going to process receipt for retailer {}", receipt.getRetailer());
        try {
            String id = receiptProcessorService.processPoints(receipt);
            return ResponseEntity.ok(new Process(id));
        } catch (Exception e) {
            log.error("Encountered an error while processing points", e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, "The receipt is invalid.");
        }
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<?> getReceiptPoints(@PathVariable("id") String id) {
        if (id == null || id.isEmpty()) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "No receipt found for that ID.");
        }

        log.info("Fetching points for process id {}", id);
        try {
            int points = receiptProcessorService.getPoints(id);
            return ResponseEntity.ok(new Points(points));
        } catch (Exception e) {
            log.error("Error while fetching points for process id {}", id, e);
            return createErrorResponse(HttpStatus.NOT_FOUND, "No receipt found for that ID.");
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("timestamp", System.currentTimeMillis());
        errorMap.put("status", status.value());  // Use "status" instead of "error" for clarity
        errorMap.put("error", status.getReasonPhrase());
        errorMap.put("message", message);

        return ResponseEntity.status(status).body(errorMap);
    }
}
