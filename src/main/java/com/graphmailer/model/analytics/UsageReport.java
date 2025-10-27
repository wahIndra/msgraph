package com.graphmailer.model.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "API usage statistics report")
public class UsageReport {

    @Schema(description = "Report generation timestamp", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @Schema(description = "Start date of the report period", example = "2025-10-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the report period", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Total API requests made", example = "15420")
    private long totalRequests;

    @Schema(description = "Successful API requests", example = "14891")
    private long successfulRequests;

    @Schema(description = "Failed API requests", example = "529")
    private long failedRequests;

    @Schema(description = "Rate limited requests", example = "45")
    private long rateLimitedRequests;

    @Schema(description = "Average response time in milliseconds", example = "285.5")
    private double averageResponseTime;

    @Schema(description = "Peak requests per hour", example = "156")
    private long peakRequestsPerHour;

    @Schema(description = "Success rate as percentage", example = "96.57")
    private double successRate;

    @Schema(description = "Request breakdown by time period")
    private List<PeriodUsageStats> periodStats;

    @Schema(description = "Endpoint usage breakdown")
    private List<EndpointUsageStats> endpointStats;

    @Schema(description = "Tenant usage breakdown")
    private List<TenantUsageStats> tenantStats;

    @Schema(description = "Error breakdown by type")
    private List<ErrorTypeStats> errorStats;

    // Constructors
    public UsageReport() {
    }

    public UsageReport(LocalDate reportDate, LocalDate startDate, LocalDate endDate,
            long totalRequests, long successfulRequests, long failedRequests) {
        this.reportDate = reportDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;
    }

    // Getters and Setters
    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
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

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
        updateSuccessRate();
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public void setSuccessfulRequests(long successfulRequests) {
        this.successfulRequests = successfulRequests;
        updateSuccessRate();
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(long failedRequests) {
        this.failedRequests = failedRequests;
    }

    public long getRateLimitedRequests() {
        return rateLimitedRequests;
    }

    public void setRateLimitedRequests(long rateLimitedRequests) {
        this.rateLimitedRequests = rateLimitedRequests;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public long getPeakRequestsPerHour() {
        return peakRequestsPerHour;
    }

    public void setPeakRequestsPerHour(long peakRequestsPerHour) {
        this.peakRequestsPerHour = peakRequestsPerHour;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public List<PeriodUsageStats> getPeriodStats() {
        return periodStats;
    }

    public void setPeriodStats(List<PeriodUsageStats> periodStats) {
        this.periodStats = periodStats;
    }

    public List<EndpointUsageStats> getEndpointStats() {
        return endpointStats;
    }

    public void setEndpointStats(List<EndpointUsageStats> endpointStats) {
        this.endpointStats = endpointStats;
    }

    public List<TenantUsageStats> getTenantStats() {
        return tenantStats;
    }

    public void setTenantStats(List<TenantUsageStats> tenantStats) {
        this.tenantStats = tenantStats;
    }

    public List<ErrorTypeStats> getErrorStats() {
        return errorStats;
    }

    public void setErrorStats(List<ErrorTypeStats> errorStats) {
        this.errorStats = errorStats;
    }

    private void updateSuccessRate() {
        this.successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;
    }

    // Nested classes
    @Schema(description = "Usage statistics for a specific time period")
    public static class PeriodUsageStats {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private String period; // HOURLY, DAILY, WEEKLY
        private long requests;
        private long successful;
        private long failed;
        private double averageResponseTime;

        public PeriodUsageStats() {
        }

        public PeriodUsageStats(LocalDate date, String period, long requests, long successful, long failed) {
            this.date = date;
            this.period = period;
            this.requests = requests;
            this.successful = successful;
            this.failed = failed;
        }

        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public long getRequests() {
            return requests;
        }

        public void setRequests(long requests) {
            this.requests = requests;
        }

        public long getSuccessful() {
            return successful;
        }

        public void setSuccessful(long successful) {
            this.successful = successful;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }
    }

    @Schema(description = "Usage statistics by API endpoint")
    public static class EndpointUsageStats {
        private String endpoint;
        private String method;
        private long requests;
        private long successful;
        private long failed;
        private double averageResponseTime;
        private double successRate;

        public EndpointUsageStats() {
        }

        public EndpointUsageStats(String endpoint, String method, long requests, long successful, long failed) {
            this.endpoint = endpoint;
            this.method = method;
            this.requests = requests;
            this.successful = successful;
            this.failed = failed;
            this.successRate = requests > 0 ? (double) successful / requests * 100 : 0;
        }

        // Getters and Setters
        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public long getRequests() {
            return requests;
        }

        public void setRequests(long requests) {
            this.requests = requests;
        }

        public long getSuccessful() {
            return successful;
        }

        public void setSuccessful(long successful) {
            this.successful = successful;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }
    }

    @Schema(description = "Usage statistics by tenant")
    public static class TenantUsageStats {
        private String tenantId;
        private String tenantName;
        private long requests;
        private long successful;
        private long failed;
        private double averageResponseTime;

        public TenantUsageStats() {
        }

        public TenantUsageStats(String tenantId, String tenantName, long requests, long successful, long failed) {
            this.tenantId = tenantId;
            this.tenantName = tenantName;
            this.requests = requests;
            this.successful = successful;
            this.failed = failed;
        }

        // Getters and Setters
        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }

        public long getRequests() {
            return requests;
        }

        public void setRequests(long requests) {
            this.requests = requests;
        }

        public long getSuccessful() {
            return successful;
        }

        public void setSuccessful(long successful) {
            this.successful = successful;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }
    }

    @Schema(description = "Error statistics by error type")
    public static class ErrorTypeStats {
        private String errorType;
        private String errorCode;
        private long count;
        private double percentage;
        private String description;

        public ErrorTypeStats() {
        }

        public ErrorTypeStats(String errorType, String errorCode, long count, double percentage) {
            this.errorType = errorType;
            this.errorCode = errorCode;
            this.count = count;
            this.percentage = percentage;
        }

        // Getters and Setters
        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}