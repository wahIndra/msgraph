package com.graphmailer.model.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Email volume statistics report")
public class EmailVolumeReport {

    @Schema(description = "Report generation timestamp", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @Schema(description = "Start date of the report period", example = "2025-10-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the report period", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Grouping method used", example = "DOMAIN")
    private String groupBy;

    @Schema(description = "Total email volume", example = "15420")
    private long totalVolume;

    @Schema(description = "Successful email volume", example = "14891")
    private long successfulVolume;

    @Schema(description = "Failed email volume", example = "529")
    private long failedVolume;

    @Schema(description = "Success rate as percentage", example = "96.57")
    private double successRate;

    @Schema(description = "Volume breakdown by category")
    private List<VolumeBreakdown> volumeBreakdown;

    @Schema(description = "Trend information")
    private VolumeTrend trend;

    // Constructors
    public EmailVolumeReport() {
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

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(long totalVolume) {
        this.totalVolume = totalVolume;
        updateSuccessRate();
    }

    public long getSuccessfulVolume() {
        return successfulVolume;
    }

    public void setSuccessfulVolume(long successfulVolume) {
        this.successfulVolume = successfulVolume;
        updateSuccessRate();
    }

    public long getFailedVolume() {
        return failedVolume;
    }

    public void setFailedVolume(long failedVolume) {
        this.failedVolume = failedVolume;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public List<VolumeBreakdown> getVolumeBreakdown() {
        return volumeBreakdown;
    }

    public void setVolumeBreakdown(List<VolumeBreakdown> volumeBreakdown) {
        this.volumeBreakdown = volumeBreakdown;
    }

    public VolumeTrend getTrend() {
        return trend;
    }

    public void setTrend(VolumeTrend trend) {
        this.trend = trend;
    }

    private void updateSuccessRate() {
        if (totalVolume > 0) {
            this.successRate = (double) successfulVolume / totalVolume * 100;
        }
    }

    // Nested classes
    @Schema(description = "Volume breakdown by category")
    public static class VolumeBreakdown {
        private String category;
        private long volume;
        private long successful;
        private long failed;
        private double percentage;

        public VolumeBreakdown() {
        }

        // Getters and Setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getVolume() {
            return volume;
        }

        public void setVolume(long volume) {
            this.volume = volume;
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

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    @Schema(description = "Volume trend information")
    public static class VolumeTrend {
        private String direction;
        private double changePercentage;
        private String comparison;

        public VolumeTrend() {
        }

        public VolumeTrend(String direction, double changePercentage, String comparison) {
            this.direction = direction;
            this.changePercentage = changePercentage;
            this.comparison = comparison;
        }

        // Getters and Setters
        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public double getChangePercentage() {
            return changePercentage;
        }

        public void setChangePercentage(double changePercentage) {
            this.changePercentage = changePercentage;
        }

        public String getComparison() {
            return comparison;
        }

        public void setComparison(String comparison) {
            this.comparison = comparison;
        }
    }
}