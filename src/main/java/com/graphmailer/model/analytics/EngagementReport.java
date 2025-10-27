package com.graphmailer.model.analytics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Email engagement statistics report")
public class EngagementReport {

    @Schema(description = "Report generation timestamp", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    @Schema(description = "Start date of the report period", example = "2025-10-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "End date of the report period", example = "2025-10-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Total number of emails sent for tracking", example = "1000")
    private long totalEmailsSent;

    @Schema(description = "Number of emails opened", example = "650")
    private long totalEmailsOpened;

    @Schema(description = "Number of unique opens", example = "580")
    private long uniqueOpens;

    @Schema(description = "Number of clicks recorded", example = "120")
    private long totalClicks;

    @Schema(description = "Number of unique clicks", example = "95")
    private long uniqueClicks;

    @Schema(description = "Open rate as percentage", example = "65.0")
    private double openRate;

    @Schema(description = "Click rate as percentage", example = "12.0")
    private double clickRate;

    @Schema(description = "Click-to-open rate as percentage", example = "18.46")
    private double clickToOpenRate;

    @Schema(description = "Daily engagement breakdown")
    private List<DailyEngagementStats> dailyStats;

    @Schema(description = "Device type breakdown")
    private List<DeviceEngagementStats> deviceStats;

    @Schema(description = "Campaign-specific engagement")
    private List<CampaignEngagementStats> campaignStats;

    @Schema(description = "Whether engagement tracking is enabled")
    private boolean trackingEnabled;

    @Schema(description = "Note about engagement tracking capabilities")
    private String trackingNote;

    // Constructors
    public EngagementReport() {
        this.trackingEnabled = false;
        this.trackingNote = "Engagement tracking requires additional implementation with tracking pixels and link wrapping";
    }

    public EngagementReport(LocalDate reportDate, LocalDate startDate, LocalDate endDate,
            long totalEmailsSent, long totalEmailsOpened, long totalClicks) {
        this.reportDate = reportDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalEmailsSent = totalEmailsSent;
        this.totalEmailsOpened = totalEmailsOpened;
        this.totalClicks = totalClicks;
        this.trackingEnabled = true;
        calculateRates();
    }

    private void calculateRates() {
        if (totalEmailsSent > 0) {
            this.openRate = (double) totalEmailsOpened / totalEmailsSent * 100;
            this.clickRate = (double) totalClicks / totalEmailsSent * 100;
        }
        if (totalEmailsOpened > 0) {
            this.clickToOpenRate = (double) totalClicks / totalEmailsOpened * 100;
        }
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

    public long getTotalEmailsSent() {
        return totalEmailsSent;
    }

    public void setTotalEmailsSent(long totalEmailsSent) {
        this.totalEmailsSent = totalEmailsSent;
        calculateRates();
    }

    public long getTotalEmailsOpened() {
        return totalEmailsOpened;
    }

    public void setTotalEmailsOpened(long totalEmailsOpened) {
        this.totalEmailsOpened = totalEmailsOpened;
        calculateRates();
    }

    public long getUniqueOpens() {
        return uniqueOpens;
    }

    public void setUniqueOpens(long uniqueOpens) {
        this.uniqueOpens = uniqueOpens;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
        calculateRates();
    }

    public long getUniqueClicks() {
        return uniqueClicks;
    }

    public void setUniqueClicks(long uniqueClicks) {
        this.uniqueClicks = uniqueClicks;
    }

    public double getOpenRate() {
        return openRate;
    }

    public void setOpenRate(double openRate) {
        this.openRate = openRate;
    }

    public double getClickRate() {
        return clickRate;
    }

    public void setClickRate(double clickRate) {
        this.clickRate = clickRate;
    }

    public double getClickToOpenRate() {
        return clickToOpenRate;
    }

    public void setClickToOpenRate(double clickToOpenRate) {
        this.clickToOpenRate = clickToOpenRate;
    }

    public List<DailyEngagementStats> getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(List<DailyEngagementStats> dailyStats) {
        this.dailyStats = dailyStats;
    }

    public List<DeviceEngagementStats> getDeviceStats() {
        return deviceStats;
    }

    public void setDeviceStats(List<DeviceEngagementStats> deviceStats) {
        this.deviceStats = deviceStats;
    }

    public List<CampaignEngagementStats> getCampaignStats() {
        return campaignStats;
    }

    public void setCampaignStats(List<CampaignEngagementStats> campaignStats) {
        this.campaignStats = campaignStats;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }

    public String getTrackingNote() {
        return trackingNote;
    }

    public void setTrackingNote(String trackingNote) {
        this.trackingNote = trackingNote;
    }

    // Nested classes
    @Schema(description = "Daily engagement statistics")
    public static class DailyEngagementStats {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private long sent;
        private long opened;
        private long clicked;
        private double openRate;
        private double clickRate;

        public DailyEngagementStats() {
        }

        public DailyEngagementStats(LocalDate date, long sent, long opened, long clicked) {
            this.date = date;
            this.sent = sent;
            this.opened = opened;
            this.clicked = clicked;
            this.openRate = sent > 0 ? (double) opened / sent * 100 : 0;
            this.clickRate = sent > 0 ? (double) clicked / sent * 100 : 0;
        }

        // Getters and Setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public long getSent() {
            return sent;
        }

        public void setSent(long sent) {
            this.sent = sent;
        }

        public long getOpened() {
            return opened;
        }

        public void setOpened(long opened) {
            this.opened = opened;
        }

        public long getClicked() {
            return clicked;
        }

        public void setClicked(long clicked) {
            this.clicked = clicked;
        }

        public double getOpenRate() {
            return openRate;
        }

        public void setOpenRate(double openRate) {
            this.openRate = openRate;
        }

        public double getClickRate() {
            return clickRate;
        }

        public void setClickRate(double clickRate) {
            this.clickRate = clickRate;
        }
    }

    @Schema(description = "Device type engagement statistics")
    public static class DeviceEngagementStats {
        private String deviceType;
        private long opens;
        private long clicks;
        private double openPercentage;
        private double clickPercentage;

        public DeviceEngagementStats() {
        }

        public DeviceEngagementStats(String deviceType, long opens, long clicks) {
            this.deviceType = deviceType;
            this.opens = opens;
            this.clicks = clicks;
        }

        // Getters and Setters
        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public long getOpens() {
            return opens;
        }

        public void setOpens(long opens) {
            this.opens = opens;
        }

        public long getClicks() {
            return clicks;
        }

        public void setClicks(long clicks) {
            this.clicks = clicks;
        }

        public double getOpenPercentage() {
            return openPercentage;
        }

        public void setOpenPercentage(double openPercentage) {
            this.openPercentage = openPercentage;
        }

        public double getClickPercentage() {
            return clickPercentage;
        }

        public void setClickPercentage(double clickPercentage) {
            this.clickPercentage = clickPercentage;
        }
    }

    @Schema(description = "Campaign-specific engagement statistics")
    public static class CampaignEngagementStats {
        private String campaignId;
        private String campaignName;
        private long sent;
        private long opened;
        private long clicked;
        private double openRate;
        private double clickRate;

        public CampaignEngagementStats() {
        }

        public CampaignEngagementStats(String campaignId, String campaignName, long sent, long opened, long clicked) {
            this.campaignId = campaignId;
            this.campaignName = campaignName;
            this.sent = sent;
            this.opened = opened;
            this.clicked = clicked;
            this.openRate = sent > 0 ? (double) opened / sent * 100 : 0;
            this.clickRate = sent > 0 ? (double) clicked / sent * 100 : 0;
        }

        // Getters and Setters
        public String getCampaignId() {
            return campaignId;
        }

        public void setCampaignId(String campaignId) {
            this.campaignId = campaignId;
        }

        public String getCampaignName() {
            return campaignName;
        }

        public void setCampaignName(String campaignName) {
            this.campaignName = campaignName;
        }

        public long getSent() {
            return sent;
        }

        public void setSent(long sent) {
            this.sent = sent;
        }

        public long getOpened() {
            return opened;
        }

        public void setOpened(long opened) {
            this.opened = opened;
        }

        public long getClicked() {
            return clicked;
        }

        public void setClicked(long clicked) {
            this.clicked = clicked;
        }

        public double getOpenRate() {
            return openRate;
        }

        public void setOpenRate(double openRate) {
            this.openRate = openRate;
        }

        public double getClickRate() {
            return clickRate;
        }

        public void setClickRate(double clickRate) {
            this.clickRate = clickRate;
        }
    }
}