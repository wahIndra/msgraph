package com.graphmailer.service;

import com.graphmailer.model.analytics.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for email analytics and reporting functionality.
 * Provides methods to retrieve various analytics reports and statistics.
 */
public interface AnalyticsService {

    /**
     * Get delivery rate statistics for emails sent within the specified date range.
     *
     * @param from     Start date (inclusive)
     * @param to       End date (inclusive)
     * @param domain   Optional domain filter
     * @param tenantId Optional tenant filter
     * @return Delivery rate report with success/failure statistics
     */
    DeliveryRateReport getDeliveryRates(LocalDate from, LocalDate to, String domain, String tenantId);

    /**
     * Get email engagement statistics including open and click rates.
     * Note: Requires engagement tracking to be implemented.
     *
     * @param from       Start date (inclusive)
     * @param to         End date (inclusive)
     * @param campaignId Optional campaign filter
     * @param tenantId   Optional tenant filter
     * @return Engagement report with open/click statistics
     */
    EngagementReport getEngagementStats(LocalDate from, LocalDate to, String campaignId, String tenantId);

    /**
     * Get API usage statistics including request counts and performance metrics.
     *
     * @param from     Start date (inclusive)
     * @param to       End date (inclusive)
     * @param tenantId Optional tenant filter
     * @param groupBy  Grouping interval (DAILY, WEEKLY, MONTHLY)
     * @return Usage report with API statistics
     */
    UsageReport getUsageStats(LocalDate from, LocalDate to, String tenantId, String groupBy);

    /**
     * Get top email senders by volume.
     *
     * @param limit    Number of top senders to return
     * @param days     Number of days to look back
     * @param tenantId Optional tenant filter
     * @return List of sender statistics ordered by volume
     */
    List<SenderStats> getTopSenders(int limit, int days, String tenantId);

    /**
     * Get error trend analysis for the specified period.
     *
     * @param from     Start date (inclusive)
     * @param to       End date (inclusive)
     * @param tenantId Optional tenant filter
     * @return Error trend report with analysis and recommendations
     */
    ErrorTrendReport getErrorTrends(LocalDate from, LocalDate to, String tenantId);
}