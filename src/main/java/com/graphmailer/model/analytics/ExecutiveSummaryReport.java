package com.graphmailer.model.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Executive summary report for email system performance")
public class ExecutiveSummaryReport {

    @Schema(description = "Report generation timestamp", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @Schema(description = "Number of days covered in this report", example = "30")
    private int periodDays;

    @Schema(description = "Start date of the report period", example = "2025-09-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the report period", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // Email Performance Metrics
    @Schema(description = "Total emails processed", example = "125430")
    private long totalEmailsProcessed;

    @Schema(description = "Email delivery success rate", example = "96.5")
    private double emailSuccessRate;

    @Schema(description = "Email delivery failure rate", example = "3.5")
    private double emailFailureRate;

    // API Performance Metrics
    @Schema(description = "Total API requests", example = "45620")
    private long totalApiRequests;

    @Schema(description = "API success rate", example = "98.2")
    private double apiSuccessRate;

    @Schema(description = "Average API response time in milliseconds", example = "285.5")
    private double averageResponseTime;

    // System Health
    @Schema(description = "Overall system health score (0-100)", example = "94.5")
    private double systemHealthScore;

    @Schema(description = "Total errors recorded", example = "156")
    private long totalErrors;

    @Schema(description = "Critical errors requiring immediate attention", example = "12")
    private long criticalErrors;

    // Top Performers
    @Schema(description = "Number of active senders", example = "15")
    private int topSenderCount;

    @Schema(description = "Top email senders by volume")
    private List<SenderStats> topSenders;

    // Insights and Recommendations
    @Schema(description = "System performance recommendations")
    private List<String> recommendations;

    @Schema(description = "Performance trend summary")
    private String performanceTrend;

    @Schema(description = "Key performance indicators summary")
    private PerformanceKPIs kpis;

    // Constructors
    public ExecutiveSummaryReport() {
        // Default constructor for JSON deserialization
    }

    // Getters and Setters
    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public int getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(int periodDays) {
        this.periodDays = periodDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getTotalEmailsProcessed() {
        return totalEmailsProcessed;
    }

    public void setTotalEmailsProcessed(long totalEmailsProcessed) {
        this.totalEmailsProcessed = totalEmailsProcessed;
    }

    public double getEmailSuccessRate() {
        return emailSuccessRate;
    }

    public void setEmailSuccessRate(double emailSuccessRate) {
        this.emailSuccessRate = emailSuccessRate;
    }

    public double getEmailFailureRate() {
        return emailFailureRate;
    }

    public void setEmailFailureRate(double emailFailureRate) {
        this.emailFailureRate = emailFailureRate;
    }

    public long getTotalApiRequests() {
        return totalApiRequests;
    }

    public void setTotalApiRequests(long totalApiRequests) {
        this.totalApiRequests = totalApiRequests;
    }

    public double getApiSuccessRate() {
        return apiSuccessRate;
    }

    public void setApiSuccessRate(double apiSuccessRate) {
        this.apiSuccessRate = apiSuccessRate;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public double getSystemHealthScore() {
        return systemHealthScore;
    }

    public void setSystemHealthScore(double systemHealthScore) {
        this.systemHealthScore = systemHealthScore;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public void setTotalErrors(long totalErrors) {
        this.totalErrors = totalErrors;
    }

    public long getCriticalErrors() {
        return criticalErrors;
    }

    public void setCriticalErrors(long criticalErrors) {
        this.criticalErrors = criticalErrors;
    }

    public int getTopSenderCount() {
        return topSenderCount;
    }

    public void setTopSenderCount(int topSenderCount) {
        this.topSenderCount = topSenderCount;
    }

    public List<SenderStats> getTopSenders() {
        return topSenders;
    }

    public void setTopSenders(List<SenderStats> topSenders) {
        this.topSenders = topSenders;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getPerformanceTrend() {
        return performanceTrend;
    }

    public void setPerformanceTrend(String performanceTrend) {
        this.performanceTrend = performanceTrend;
    }

    public PerformanceKPIs getKpis() {
        return kpis;
    }

    public void setKpis(PerformanceKPIs kpis) {
        this.kpis = kpis;
    }

    // Nested class for KPIs
    @Schema(description = "Key performance indicators")
    public static class PerformanceKPIs {
        @Schema(description = "Daily average email volume", example = "4181")
        private long dailyAverageVolume;

        @Schema(description = "Peak hour email volume", example = "520")
        private long peakHourVolume;

        @Schema(description = "System uptime percentage", example = "99.9")
        private double uptimePercentage;

        @Schema(description = "User satisfaction score", example = "4.5")
        private double userSatisfactionScore;

        public PerformanceKPIs() {
            // Default constructor for JSON deserialization
        }

        // Getters and Setters
        public long getDailyAverageVolume() {
            return dailyAverageVolume;
        }

        public void setDailyAverageVolume(long dailyAverageVolume) {
            this.dailyAverageVolume = dailyAverageVolume;
        }

        public long getPeakHourVolume() {
            return peakHourVolume;
        }

        public void setPeakHourVolume(long peakHourVolume) {
            this.peakHourVolume = peakHourVolume;
        }

        public double getUptimePercentage() {
            return uptimePercentage;
        }

        public void setUptimePercentage(double uptimePercentage) {
            this.uptimePercentage = uptimePercentage;
        }

        public double getUserSatisfactionScore() {
            return userSatisfactionScore;
        }

        public void setUserSatisfactionScore(double userSatisfactionScore) {
            this.userSatisfactionScore = userSatisfactionScore;
        }
    }
}