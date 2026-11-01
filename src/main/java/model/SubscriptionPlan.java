package model;

import java.io.Serializable;

/**
 * Owner: Thông.
 * Role: Domain model shared by controllers, DAOs, and JSP views.
 * Source: src/main/java/model/SubscriptionPlan.java.
 */
public class SubscriptionPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    private int planId;
    private String planCode;
    private String planName;
    private double price;
    private int durationDays;
    private int maxScreens;
    private String maxQuality;
    private String description;
    private boolean isActive;

    public SubscriptionPlan() {}

    public SubscriptionPlan(
        int planId,
        String planCode,
        String planName,
        double price,
        int durationDays,
        int maxScreens,
        String maxQuality,
        String description,
        boolean isActive
    ) {
        this.planId = planId;
        this.planCode = planCode;
        this.planName = planName;
        this.price = price;
        this.durationDays = durationDays;
        this.maxScreens = maxScreens;
        this.maxQuality = maxQuality;
        this.description = description;
        this.isActive = isActive;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getId() {
        return planId;
    }

    public void setId(int id) {
        this.planId = id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getName() {
        return planName;
    }

    public void setName(String name) {
        this.planName = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getMaxScreens() {
        return maxScreens;
    }

    public void setMaxScreens(int maxScreens) {
        this.maxScreens = maxScreens;
    }

    public int getScreenLimit() {
        return maxScreens;
    }

    public void setScreenLimit(int screenLimit) {
        this.maxScreens = screenLimit;
    }

    public String getMaxQuality() {
        return maxQuality;
    }

    public void setMaxQuality(String maxQuality) {
        this.maxQuality = maxQuality;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
