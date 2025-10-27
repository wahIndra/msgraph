package com.graphmailer.model.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Error trend analysis report")
public class ErrorTrendReport {

    @Schema(description = "Report generation timestamp", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @Schema(description = "Start date of the report period", example = "2025-10-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the report period", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Total errors recorded", example = "245")
    private long totalErrors;

    @Schema(description = "Critical errors", example = "12")
    private long criticalErrors;

    @Schema(description = "Warning level errors", example = "89")
    private long warningErrors;

    @Schema(description = "Info level errors", example = "144")
    private long infoErrors;

    @Schema(description = "Error rate as percentage", example = "3.2")
    private double errorRate;

    @Schema(description = "Trend direction (INCREASING, DECREASING, STABLE)", example = "DECREASING")
    private String trendDirection;

    @Schema(description = "Percentage change from previous period", example = "-15.3")
    private double trendPercentage;

    @Schema(description = "Daily error breakdown")
    private List<DailyErrorStats> dailyErrorStats;

    @Schema(description = "Error breakdown by category")
    private List<ErrorCategoryStats> errorCategories;

    @Schema(description = "Most frequent error patterns")
    private List<ErrorPatternStats> errorPatterns;

    @Schema(description = "Resolution recommendations")
    private List<ErrorRecommendation> recommendations;

    // Constructors
    public ErrorTrendReport() {
    }

    public ErrorTrendReport(LocalDate reportDate, LocalDate startDate, LocalDate endDate,
            long totalErrors, long criticalErrors, long warningErrors, long infoErrors) {
        this.reportDate = reportDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalErrors = totalErrors;
        this.criticalErrors = criticalErrors;
        this.warningErrors = warningErrors;
        this.infoErrors = infoErrors;
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

    public long getWarningErrors() {
        return warningErrors;
    }

    public void setWarningErrors(long warningErrors) {
        this.warningErrors = warningErrors;
    }

    public long getInfoErrors() {
        return infoErrors;
    }

    public void setInfoErrors(long infoErrors) {
        this.infoErrors = infoErrors;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public String getTrendDirection() {
        return trendDirection;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }

    public double getTrendPercentage() {
        return trendPercentage;
    }

    public void setTrendPercentage(double trendPercentage) {
        this.trendPercentage = trendPercentage;
    }

    public List<DailyErrorStats> getDailyErrorStats() {
        return dailyErrorStats;
    }

    public void setDailyErrorStats(List<DailyErrorStats> dailyErrorStats) {
        this.dailyErrorStats = dailyErrorStats;
    }

    public List<ErrorCategoryStats> getErrorCategories() {
        return errorCategories;
    }

    public void setErrorCategories(List<ErrorCategoryStats> errorCategories) {
        this.errorCategories = errorCategories;
    }

    public List<ErrorPatternStats> getErrorPatterns() {
        return errorPatterns;
    }

    public void setErrorPatterns(List<ErrorPatternStats> errorPatterns) {
        this.errorPatterns = errorPatterns;
    }

    public List<ErrorRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<ErrorRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    // Nested classes
    @Schema(description = "Daily error statistics")
    public static class DailyErrorStats {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private long totalErrors;
        private long criticalErrors;
        private long warningErrors;
        private long infoErrors;
        private double errorRate;

        public DailyErrorStats() {
        }

        public DailyErrorStats(LocalDate date, long totalErrors, long criticalErrors, long warningErrors,
                long infoErrors) {
            this.date = date;
            this.totalErrors = totalErrors;
            this.criticalErrors = criticalErrors;
            this.warningErrors = warningErrors;
            this.infoErrors = infoErrors;
        }

        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
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

        public long getWarningErrors() {
            return warningErrors;
        }

        public void setWarningErrors(long warningErrors) {
            this.warningErrors = warningErrors;
        }

        public long getInfoErrors() {
            return infoErrors;
        }

        public void setInfoErrors(long infoErrors) {
            this.infoErrors = infoErrors;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }
    }

    @Schema(description = "Error statistics by category")
    public static class ErrorCategoryStats {
        private String category;
        private String description;
        private long count;
        private double percentage;
        private String severity;

        public ErrorCategoryStats() {
        }

        public ErrorCategoryStats(String category, String description, long count, double percentage, String severity) {
            this.category = category;
            this.description = description;
            this.count = count;
            this.percentage = percentage;
            this.severity = severity;
        }

        // Getters and Setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
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

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }
    }

    @Schema(description = "Frequent error pattern statistics")
    public static class ErrorPatternStats {
        private String pattern;
        private String errorCode;
        private long occurrences;
        private String firstSeen;
        private String lastSeen;
        private String affectedEndpoints;

        public ErrorPatternStats() {
        }

        public ErrorPatternStats(String pattern, String errorCode, long occurrences) {
            this.pattern = pattern;
            this.errorCode = errorCode;
            this.occurrences = occurrences;
        }

        // Getters and Setters
        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public long getOccurrences() {
            return occurrences;
        }

        public void setOccurrences(long occurrences) {
            this.occurrences = occurrences;
        }

        public String getFirstSeen() {
            return firstSeen;
        }

        public void setFirstSeen(String firstSeen) {
            this.firstSeen = firstSeen;
        }

        public String getLastSeen() {
            return lastSeen;
        }

        public void setLastSeen(String lastSeen) {
            this.lastSeen = lastSeen;
        }

        public String getAffectedEndpoints() {
            return affectedEndpoints;
        }

        public void setAffectedEndpoints(String affectedEndpoints) {
            this.affectedEndpoints = affectedEndpoints;
        }
    }

    @Schema(description = "Error resolution recommendation")
    public static class ErrorRecommendation {
        private String errorType;
        private String priority;
        private String recommendation;
        private String actionRequired;
        private String estimatedImpact;

        public ErrorRecommendation() {
        }

        public ErrorRecommendation(String errorType, String priority, String recommendation, String actionRequired) {
            this.errorType = errorType;
            this.priority = priority;
            this.recommendation = recommendation;
            this.actionRequired = actionRequired;
        }

        // Getters and Setters
        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        public String getActionRequired() {
            return actionRequired;
        }

        public void setActionRequired(String actionRequired) {
            this.actionRequired = actionRequired;
        }

        public String getEstimatedImpact() {
            return estimatedImpact;
        }

        public void setEstimatedImpact(String estimatedImpact) {
            this.estimatedImpact = estimatedImpact;
        }
    }
}