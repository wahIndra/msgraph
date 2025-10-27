package com.graphmailer.controller;

import com.graphmailer.model.analytics.*;
import com.graphmailer.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Email Analytics", description = "Email analytics and reporting endpoints")
@SecurityRequirement(name = "ApiKeyAuth")
@SecurityRequirement(name = "BearerAuth")
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/delivery-rates")
    @Operation(summary = "Get email delivery statistics", description = "Retrieve delivery rates, success/failure statistics for emails sent within the specified date range", responses = {
            @ApiResponse(responseCode = "200", description = "Delivery statistics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<DeliveryRateReport> getDeliveryRates(
            @Parameter(description = "Start date (inclusive)", example = "2025-10-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (inclusive)", example = "2025-10-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Filter by sender domain", example = "company.com") @RequestParam(required = false) String domain,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId) {

        DeliveryRateReport report = analyticsService.getDeliveryRates(from, to, domain, tenantId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/engagement")
    @Operation(summary = "Get email engagement statistics", description = "Retrieve open rates, click rates, and other engagement metrics (requires tracking to be enabled)", responses = {
            @ApiResponse(responseCode = "200", description = "Engagement statistics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "501", description = "Engagement tracking not implemented")
    })
    public ResponseEntity<EngagementReport> getEngagementStats(
            @Parameter(description = "Start date (inclusive)", example = "2025-10-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (inclusive)", example = "2025-10-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Filter by campaign ID") @RequestParam(required = false) String campaignId,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId) {

        EngagementReport report = analyticsService.getEngagementStats(from, to, campaignId, tenantId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/usage")
    @Operation(summary = "Get API usage statistics", description = "Retrieve API usage metrics including request counts, rate limiting hits, and performance data", responses = {
            @ApiResponse(responseCode = "200", description = "Usage statistics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<UsageReport> getUsageStats(
            @Parameter(description = "Start date (inclusive)", example = "2025-10-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (inclusive)", example = "2025-10-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId,

            @Parameter(description = "Group by interval", example = "DAILY") @RequestParam(defaultValue = "DAILY") String groupBy) {

        UsageReport report = analyticsService.getUsageStats(from, to, tenantId, groupBy);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/top-senders")
    @Operation(summary = "Get top email senders", description = "Retrieve the most active email senders by volume", responses = {
            @ApiResponse(responseCode = "200", description = "Top senders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<SenderStats>> getTopSenders(
            @Parameter(description = "Number of top senders to return", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,

            @Parameter(description = "Number of days to look back", example = "7") @RequestParam(defaultValue = "7") @Min(1) @Max(365) int days,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId) {

        List<SenderStats> stats = analyticsService.getTopSenders(limit, days, tenantId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/error-trends")
    @Operation(summary = "Get error trend analysis", description = "Retrieve error patterns and trending issues in email delivery", responses = {
            @ApiResponse(responseCode = "200", description = "Error trends retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ErrorTrendReport> getErrorTrends(
            @Parameter(description = "Start date (inclusive)", example = "2025-10-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (inclusive)", example = "2025-10-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId) {

        ErrorTrendReport report = analyticsService.getErrorTrends(from, to, tenantId);
        return ResponseEntity.ok(report);
    }
}