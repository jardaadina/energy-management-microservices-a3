package com.energy.support.service;

import com.energy.support.dto.ChatResponse;
import com.energy.support.model.ChatRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class RuleBasedChatbotService {

    private List<ChatRule> rules;

    @PostConstruct
    public void initRules() {
        rules = new ArrayList<>();

        // Rule 1: Greetings
        rules.add(new ChatRule(
                "greeting",
                Arrays.asList("hello", "hi", "hey", "good morning", "good afternoon", "good evening"),
                "Hello! Welcome to Energy Management System support. How can I help you today?",
                10
        ));

        // Rule 2: Device not working
        rules.add(new ChatRule(
                "device_not_working",
                Arrays.asList("device not working", "device broken", "device stopped", "device won't start", "device error"),
                "I'm sorry to hear your device isn't working. Please check: 1) Is the device properly connected? 2) Does it have power? 3) Is it assigned to your account? If problems persist, I can connect you with an administrator.",
                9
        ));

        // Rule 3: High consumption
        rules.add(new ChatRule(
                "high_consumption",
                Arrays.asList("consumption high", "using too much energy", "power usage high", "overconsumption", "energy bill high"),
                "High energy consumption can be caused by: 1) Multiple devices running simultaneously, 2) Devices operating during peak hours, 3) Appliances nearing end of life. Check your energy chart to identify peak usage times and consider adjusting usage patterns.",
                8
        ));

        // Rule 4: How to add device
        rules.add(new ChatRule(
                "add_device",
                Arrays.asList("add device", "create device", "new device", "register device", "how to add"),
                "Only administrators can add new devices to the system. If you need a device added, please contact your system administrator or use the 'Request Admin Help' option.",
                7
        ));

        // Rule 5: View consumption
        rules.add(new ChatRule(
                "view_consumption",
                Arrays.asList("view consumption", "see energy", "check usage", "consumption data", "energy history", "consumption chart"),
                "To view your energy consumption: 1) Go to your dashboard, 2) Select a device from 'My Devices', 3) The energy chart will show hourly consumption, 4) Use the date picker to view historical data.",
                7
        ));

        // Rule 6: Password reset
        rules.add(new ChatRule(
                "password_reset",
                Arrays.asList("forgot password", "reset password", "change password", "password reset", "can't login"),
                "To reset your password, please click on 'Forgot Password' on the login page. You'll receive instructions via email. If you don't receive the email, please contact your administrator.",
                8
        ));

        // Rule 7: Device assignment
        rules.add(new ChatRule(
                "device_assignment",
                Arrays.asList("assign device", "device not showing", "missing device", "where is my device", "can't see device"),
                "If you can't see your device: 1) Verify with your administrator that it's been assigned to you, 2) Try logging out and back in, 3) Check if you're using the correct account. Administrators can assign devices from the Assignments tab.",
                7
        ));

        // Rule 8: Energy saving tips
        rules.add(new ChatRule(
                "energy_tips",
                Arrays.asList("save energy", "reduce consumption", "lower bill", "energy tips", "efficiency tips"),
                "Energy saving tips: 1) Turn off devices when not in use, 2) Use devices during off-peak hours, 3) Regular maintenance of appliances, 4) Use energy-efficient settings, 5) Monitor your consumption regularly through the dashboard.",
                6
        ));

        // Rule 9: Account issues
        rules.add(new ChatRule(
                "account_issues",
                Arrays.asList("account problem", "can't access", "account locked", "account issue", "login problem"),
                "For account-related issues: 1) Verify your username and password, 2) Clear your browser cache, 3) Try a different browser, 4) Contact your administrator if the problem persists. Your account may need to be reset.",
                8
        ));

        // Rule 10: Billing/Pricing
        rules.add(new ChatRule(
                "billing",
                Arrays.asList("price", "cost", "billing", "payment", "how much", "charge"),
                "For pricing and billing information, please contact your service provider or administrator. The system shows consumption data, but pricing is managed externally based on your service agreement.",
                6
        ));

        // Rule 11: Admin contact
        rules.add(new ChatRule(
                "contact_admin",
                Arrays.asList("contact admin", "speak to admin", "talk to administrator", "admin help", "escalate"),
                "I can connect you with an administrator. Please describe your issue in detail, and an admin will respond as soon as possible. You can also continue chatting here, and I'll notify them.",
                9
        ));

        // Rule 12: Chart not loading
        rules.add(new ChatRule(
                "chart_issue",
                Arrays.asList("chart not loading", "no data", "graph empty", "chart error", "visualization not working"),
                "If your energy chart isn't displaying data: 1) Ensure your device has been active and collecting data, 2) Try selecting a different date, 3) Refresh the page, 4) Data is collected every 10 minutes, so new devices may take time to show data.",
                7
        ));

        // Rule 13: Alerts/Notifications
        rules.add(new ChatRule(
                "alerts",
                Arrays.asList("alert", "notification", "warning", "overconsumption alert", "notify me"),
                "You'll automatically receive alerts when your device consumption exceeds its maximum limit. These appear as real-time notifications. Make sure notifications are enabled in your browser settings.",
                6
        ));

        // Rule 14: System features
        rules.add(new ChatRule(
                "features",
                Arrays.asList("what can I do", "features", "capabilities", "what does this do", "how does it work"),
                "This Energy Management System allows you to: 1) Monitor real-time device energy consumption, 2) View historical consumption data via charts, 3) Receive overconsumption alerts, 4) Manage your device assignments (admin only), 5) Track daily/hourly energy usage patterns.",
                5
        ));

        // Rule 15: Goodbye
        rules.add(new ChatRule(
                "goodbye",
                Arrays.asList("bye", "goodbye", "thanks", "thank you", "that's all"),
                "You're welcome! Feel free to reach out if you need more help. Have a great day!",
                5
        ));

        rules.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));

        log.info("Initialized {} chatbot rules", rules.size());
    }

    public ChatResponse processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new ChatResponse("Please provide a message.", "ERROR");
        }

        String lowerMessage = message.toLowerCase().trim();

        for (ChatRule rule : rules) {
            if (matchesRule(lowerMessage, rule)) {
                log.info("Matched rule: {} for message: {}", rule.getId(), message);
                ChatResponse response = new ChatResponse(
                        rule.getResponse(),
                        "RULE_BASED"
                );
                response.setMatchedRuleId(rule.getId());
                return response;
            }
        }

        log.info("No rule matched for message: {}", message);
        return null;
    }

    private boolean matchesRule(String message, ChatRule rule) {
        if (rule.getPattern() != null && !rule.getPattern().isEmpty()) {
            return message.matches(".*" + rule.getPattern() + ".*");
        }

        if (rule.getKeywords() != null) {
            for (String keyword : rule.getKeywords()) {
                String keywordToMatch = rule.isCaseSensitive() ? keyword : keyword.toLowerCase();
                if (message.contains(keywordToMatch)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<ChatRule> getAllRules() {
        return new ArrayList<>(rules);
    }

    public int getRuleCount() {
        return rules.size();
    }
}