package com.civicpulse.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class IssueIdGenerator {
    
    private static final AtomicInteger sequence = new AtomicInteger(0);
    private static String lastDate = "";

    /**
     * Generate unique issue ID in format: CIVIC-{CATEGORY_CODE}-{YYYYMMDD}-{SEQUENCE}
     * Example: CIVIC-RD-20260210-0001
     */
    public static synchronized String generateIssueId(String category) {
        String categoryCode = getCategoryCode(category);
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        
        // Reset sequence if date changed
        if (!dateStr.equals(lastDate)) {
            sequence.set(0);
            lastDate = dateStr;
        }
        
        int seq = sequence.incrementAndGet();
        return String.format("CIVIC-%s-%s-%04d", categoryCode, dateStr, seq);
    }

    /**
     * Get category code from category name
     */
    private static String getCategoryCode(String category) {
        switch (category.toUpperCase()) {
            case "ROAD":
                return "RD";
            case "WATER":
                return "WA";
            case "SANITATION":
                return "SN";
            case "ELECTRICITY":
                return "EL";
            default:
                return "XX";
        }
    }
}
