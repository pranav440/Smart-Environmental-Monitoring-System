package com.environmental.models;

public enum RiskLevel {
    LOW("🟢", "Low", 0, 33),
    MEDIUM("🟠", "Medium", 34, 66),
    HIGH("🔴", "High", 67, 100);
    
    public final String icon;
    public final String text;
    public final int minScore;
    public final int maxScore;
    
    RiskLevel(String icon, String text, int minScore, int maxScore) {
        this.icon = icon;
        this.text = text;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }
    
    public static RiskLevel fromScore(double score) {
        if (score <= 33) return LOW;
        if (score <= 66) return MEDIUM;
        return HIGH;
    }
}