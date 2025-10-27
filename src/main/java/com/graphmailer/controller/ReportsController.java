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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Email Reports", description = "Email volume and statistical reporting endpoints")
@SecurityRequirement(name = "ApiKeyAuth")
@SecurityRequirement(name = "BearerAuth")
@Validated
public class ReportsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public ReportsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/email-volumes")
    @Operation(summary = "Get email volume reports", description = "Retrieve email volume statistics by domain, user, and time period", responses = {
            @ApiResponse(responseCode = "200", description = "Volume report retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<EmailVolumeReport> getEmailVolumes(
            @Parameter(description = "Start date (inclusive)", example = "2025-10-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (inclusive)", example = "2025-10-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Group by (DOMAIN, USER, HOUR, DAY)", example = "DOMAIN") @RequestParam(defaultValue = "DOMAIN") String groupBy,

            @Parameter(description = "Filter by domain", example = "company.com") @RequestParam(required = false) String domain,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId,

            @Parameter(description = "Maximum number of results", example = "50") @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int limit) {

        // For now, use delivery rates as basis for volume reporting
        DeliveryRateReport deliveryReport = analyticsService.getDeliveryRates(from, to, domain, tenantId);

        EmailVolumeReport volumeReport = new EmailVolumeReport();
        volumeReport.setReportDate(LocalDate.now());
        volumeReport.setStartDate(from);
        volumeReport.setEndDate(to);
        volumeReport.setGroupBy(groupBy);
        volumeReport.setTotalVolume(deliveryReport.getTotalEmailsAttempted());
        volumeReport.setSuccessfulVolume(deliveryReport.getTotalEmailsSuccessful());
        volumeReport.setFailedVolume(deliveryReport.getTotalEmailsFailed());

        // Convert domain stats to volume breakdown
        if (deliveryReport.getDomainStats() != null) {
            List<EmailVolumeReport.VolumeBreakdown> breakdown = deliveryReport.getDomainStats().stream()
                    .map(domainStat -> {
                        EmailVolumeReport.VolumeBreakdown vb = new EmailVolumeReport.VolumeBreakdown();
                        vb.setCategory(domainStat.getDomain());
                        vb.setVolume(domainStat.getAttempted());
                        vb.setSuccessful(domainStat.getSuccessful());
                        vb.setFailed(domainStat.getFailed());
                        vb.setPercentage(
                                (double) domainStat.getAttempted() / deliveryReport.getTotalEmailsAttempted() * 100);
                        return vb;
                    })
                    .toList();
            volumeReport.setVolumeBreakdown(breakdown);
        }

        return ResponseEntity.ok(volumeReport);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get executive summary report", description = "Retrieve high-level summary of email system performance and usage", responses = {
            @ApiResponse(responseCode = "200", description = "Summary report retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ExecutiveSummaryReport> getExecutiveSummary(
            @Parameter(description = "Number of days to include in summary", example = "30") @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days,

            @Parameter(description = "Filter by tenant ID") @RequestParam(required = false) String tenantId) {

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);

        // Gather data from multiple analytics endpoints
        DeliveryRateReport deliveryReport = analyticsService.getDeliveryRates(from, to, null, tenantId);
        UsageReport usageReport = analyticsService.getUsageStats(from, to, tenantId, "DAILY");
        List<SenderStats> topSenders = analyticsService.getTopSenders(5, days, tenantId);
        ErrorTrendReport errorReport = analyticsService.getErrorTrends(from, to, tenantId);

        ExecutiveSummaryReport summary = new ExecutiveSummaryReport();
        summary.setReportDate(LocalDate.now());
        summary.setPeriodDays(days);
        summary.setStartDate(from);
        summary.setEndDate(to);

        // Email metrics
        summary.setTotalEmailsProcessed(deliveryReport.getTotalEmailsAttempted());
        summary.setEmailSuccessRate(deliveryReport.getSuccessRate());
        summary.setEmailFailureRate(deliveryReport.getFailureRate());

        // API metrics
        summary.setTotalApiRequests(usageReport.getTotalRequests());
        summary.setApiSuccessRate(usageReport.getSuccessRate());
        summary.setAverageResponseTime(usageReport.getAverageResponseTime());

        // System health
        summary.setSystemHealthScore(calculateHealthScore(deliveryReport, usageReport, errorReport));
        summary.setTotalErrors(errorReport.getTotalErrors());
        summary.setCriticalErrors(errorReport.getCriticalErrors());

        // Top senders summary
        summary.setTopSenderCount(topSenders.size());
        summary.setTopSenders(topSenders.stream().limit(3).toList());

        // Recommendations
        summary.setRecommendations(generateRecommendations(deliveryReport, usageReport, errorReport));

        return ResponseEntity.ok(summary);
    }

    private double calculateHealthScore(DeliveryRateReport delivery, UsageReport usage, ErrorTrendReport errors) {
        // Simple health score calculation
        double deliveryScore = delivery.getSuccessRate();
        double usageScore = usage.getSuccessRate();
        double errorScore = Math.max(0, 100 - (errors.getErrorRate() * 10));

        return (deliveryScore * 0.4 + usageScore * 0.4 + errorScore * 0.2);
    }

    private List<String> generateRecommendations(DeliveryRateReport delivery, UsageReport usage,
            ErrorTrendReport errors) {
        List<String> recommendations = new ArrayList<>();

        if (delivery.getSuccessRate() < 95.0) {
            recommendations.add("Email delivery rate is below 95%. Review sender reputation and recipient validation.");
        }

        if (usage.getSuccessRate() < 98.0) {
            recommendations.add("API success rate is below 98%. Investigate authentication and rate limiting issues.");
        }

        if (errors.getErrorRate() > 5.0) {
            recommendations.add("Error rate is above 5%. Review error patterns and implement additional monitoring.");
        }

        if (usage.getAverageResponseTime() > 1000) {
            recommendations.add("Average response time is above 1 second. Consider performance optimization.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("System performance is within acceptable parameters. Continue monitoring.");
        }

        return recommendations;
    }
}