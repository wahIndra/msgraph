package com.graphmailer.model.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Email delivery rate statistics report")
public class DeliveryRateReport {

    @Schema(description = "Report generation timestamp", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @Schema(description = "Start date of the report period", example = "2025-10-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the report period", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Total number of emails attempted", example = "1500")
    private long totalEmailsAttempted;

    @Schema(description = "Number of successfully sent emails", example = "1425")
    private long totalEmailsSuccessful;

    @Schema(description = "Number of failed email attempts", example = "75")
    private long totalEmailsFailed;

    @Schema(description = "Overall success rate as percentage", example = "95.0")
    private double successRate;

    @Schema(description = "Overall failure rate as percentage", example = "5.0")
    private double failureRate;

    @Schema(description = "Daily breakdown of delivery statistics")
    private List<DailyDeliveryStats> dailyStats;

    @Schema(description = "Breakdown by failure reasons")
    private List<FailureReasonStats> failureReasons;

    @Schema(description = "Domain-specific delivery statistics")
    private List<DomainDeliveryStats> domainStats;

    // Constructors
    public DeliveryRateReport() {
    }

    public DeliveryRateReport(LocalDate reportDate, LocalDate startDate, LocalDate endDate,
            long totalEmailsAttempted, long totalEmailsSuccessful, long totalEmailsFailed) {
        this.reportDate = reportDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalEmailsAttempted = totalEmailsAttempted;
        this.totalEmailsSuccessful = totalEmailsSuccessful;
        this.totalEmailsFailed = totalEmailsFailed;
        this.successRate = totalEmailsAttempted > 0 ? (double) totalEmailsSuccessful / totalEmailsAttempted * 100 : 0;
        this.failureRate = totalEmailsAttempted > 0 ? (double) totalEmailsFailed / totalEmailsAttempted * 100 : 0;
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

    public long getTotalEmailsAttempted() {
        return totalEmailsAttempted;
    }

    public void setTotalEmailsAttempted(long totalEmailsAttempted) {
        this.totalEmailsAttempted = totalEmailsAttempted;
        updateRates();
    }

    public long getTotalEmailsSuccessful() {
        return totalEmailsSuccessful;
    }

    public void setTotalEmailsSuccessful(long totalEmailsSuccessful) {
        this.totalEmailsSuccessful = totalEmailsSuccessful;
        updateRates();
    }

    public long getTotalEmailsFailed() {
        return totalEmailsFailed;
    }

    public void setTotalEmailsFailed(long totalEmailsFailed) {
        this.totalEmailsFailed = totalEmailsFailed;
        updateRates();
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public List<DailyDeliveryStats> getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(List<DailyDeliveryStats> dailyStats) {
        this.dailyStats = dailyStats;
    }

    public List<FailureReasonStats> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(List<FailureReasonStats> failureReasons) {
        this.failureReasons = failureReasons;
    }

    public List<DomainDeliveryStats> getDomainStats() {
        return domainStats;
    }

    public void setDomainStats(List<DomainDeliveryStats> domainStats) {
        this.domainStats = domainStats;
    }

    private void updateRates() {
        if (totalEmailsAttempted > 0) {
            this.successRate = (double) totalEmailsSuccessful / totalEmailsAttempted * 100;
            this.failureRate = (double) totalEmailsFailed / totalEmailsAttempted * 100;
        }
    }

    // Nested classes for breakdown statistics
    @Schema(description = "Daily delivery statistics")
    public static class DailyDeliveryStats {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private long attempted;
        private long successful;
        private long failed;
        private double successRate;

        public DailyDeliveryStats() {
        }

        public DailyDeliveryStats(LocalDate date, long attempted, long successful, long failed) {
            this.date = date;
            this.attempted = attempted;
            this.successful = successful;
            this.failed = failed;
            this.successRate = attempted > 0 ? (double) successful / attempted * 100 : 0;
        }

        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public long getAttempted() {
            return attempted;
        }

        public void setAttempted(long attempted) {
            this.attempted = attempted;
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

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }
    }

    @Schema(description = "Failure reason statistics")
    public static class FailureReasonStats {
        private String reason;
        private long count;
        private double percentage;

        public FailureReasonStats() {
        }

        public FailureReasonStats(String reason, long count, double percentage) {
            this.reason = reason;
            this.count = count;
            this.percentage = percentage;
        }

        // Getters and Setters
        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
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
    }

    @Schema(description = "Domain-specific delivery statistics")
    public static class DomainDeliveryStats {
        private String domain;
        private long attempted;
        private long successful;
        private long failed;
        private double successRate;

        public DomainDeliveryStats() {
        }

        public DomainDeliveryStats(String domain, long attempted, long successful, long failed) {
            this.domain = domain;
            this.attempted = attempted;
            this.successful = successful;
            this.failed = failed;
            this.successRate = attempted > 0 ? (double) successful / attempted * 100 : 0;
        }

        // Getters and Setters
        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public long getAttempted() {
            return attempted;
        }

        public void setAttempted(long attempted) {
            this.attempted = attempted;
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

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }
    }
}