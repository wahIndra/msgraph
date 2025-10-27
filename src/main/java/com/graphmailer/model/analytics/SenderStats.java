package com.graphmailer.model.analytics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email sender statistics")
public class SenderStats {

    @Schema(description = "Sender email address or UPN", example = "noreply@company.com")
    private String senderUpn;

    @Schema(description = "Sender domain", example = "company.com")
    private String domain;

    @Schema(description = "Total emails sent", example = "1250")
    private long emailsSent;

    @Schema(description = "Successful emails", example = "1198")
    private long emailsSuccessful;

    @Schema(description = "Failed emails", example = "52")
    private long emailsFailed;

    @Schema(description = "Success rate as percentage", example = "95.84")
    private double successRate;

    @Schema(description = "Average emails per day", example = "178.6")
    private double averagePerDay;

    @Schema(description = "Rank among all senders", example = "3")
    private int rank;

    @Schema(description = "Tenant ID associated with sender")
    private String tenantId;

    // Constructors
    public SenderStats() {
    }

    public SenderStats(String senderUpn, long emailsSent, long emailsSuccessful, long emailsFailed) {
        this.senderUpn = senderUpn;
        this.emailsSent = emailsSent;
        this.emailsSuccessful = emailsSuccessful;
        this.emailsFailed = emailsFailed;
        this.successRate = emailsSent > 0 ? (double) emailsSuccessful / emailsSent * 100 : 0;

        // Extract domain from UPN
        if (senderUpn != null && senderUpn.contains("@")) {
            this.domain = senderUpn.substring(senderUpn.indexOf("@") + 1);
        }
    }

    // Getters and Setters
    public String getSenderUpn() {
        return senderUpn;
    }

    public void setSenderUpn(String senderUpn) {
        this.senderUpn = senderUpn;
        // Extract domain when UPN is set
        if (senderUpn != null && senderUpn.contains("@")) {
            this.domain = senderUpn.substring(senderUpn.indexOf("@") + 1);
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getEmailsSent() {
        return emailsSent;
    }

    public void setEmailsSent(long emailsSent) {
        this.emailsSent = emailsSent;
        updateSuccessRate();
    }

    public long getEmailsSuccessful() {
        return emailsSuccessful;
    }

    public void setEmailsSuccessful(long emailsSuccessful) {
        this.emailsSuccessful = emailsSuccessful;
        updateSuccessRate();
    }

    public long getEmailsFailed() {
        return emailsFailed;
    }

    public void setEmailsFailed(long emailsFailed) {
        this.emailsFailed = emailsFailed;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getAveragePerDay() {
        return averagePerDay;
    }

    public void setAveragePerDay(double averagePerDay) {
        this.averagePerDay = averagePerDay;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    private void updateSuccessRate() {
        this.successRate = emailsSent > 0 ? (double) emailsSuccessful / emailsSent * 100 : 0;
    }
}