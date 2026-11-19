package edu.cit.dingding.tabletap.dashboard.dto;

import java.util.List;

public record DashboardResponse(String role, String welcomeMessage, String fullName, List<String> features) {}
