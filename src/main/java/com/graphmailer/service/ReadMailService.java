package com.graphmailer.service;

/**
 * Service interface for reading emails.
 * 
 * This interface defines the contract for email reading operations,
 * maintaining compatibility with the existing ReadMailService pattern.
 */
public interface ReadMailService {
    
    /**
     * Reads emails from the specified mailbox using the provided parameters.
     * 
     * @param from Email address of the mailbox to read from
     * @param paswd Password (ignored in Graph API implementation)
     * @param subject Subject filter for emails
     * @param sender Sender filter for emails  
     * @param filename Output filename for results
     * @param filetype File type for output (e.g., CSV, JSON)
     * @param counted Number of emails to retrieve
     * @param separator Separator for CSV output
     * @param header Whether to include headers in output
     * @return JSON string containing the email data
     */
    String readEmails(String from, String paswd, String subject, String sender, 
                     String filename, String filetype, Integer counted, 
                     String separator, String header);
}