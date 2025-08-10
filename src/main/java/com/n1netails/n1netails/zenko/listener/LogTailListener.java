package com.n1netails.n1netails.zenko.listener;

import com.n1netails.n1netails.zenko.model.request.AlertTailRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.n1netails.n1netails.zenko.constant.TraceConstant.*;

@Slf4j
public class LogTailListener extends TailerListenerAdapter {

    private boolean capturing = false;
    private final StringBuilder buffer = new StringBuilder();

    // Retry & deduplication config
    private final int maxRetries = 3;
    private final long baseBackoffMillis = 1000;
    private final long alertCooldownSeconds = 300;
    private final Map<String, Instant> recentAlerts = new ConcurrentHashMap<>();

    private final String fileName;
    private final List<String> keywords;
    private final String alertEndpoint;
    private final String alertToken;
    private final RestTemplate restTemplate = new RestTemplate();

    public LogTailListener(String fileName, List<String> keywords, String alertEndpoint, String alertToken) {
        this.fileName = fileName;
        this.keywords = keywords;
        this.alertEndpoint = alertEndpoint;
        this.alertToken = alertToken;
    }

    @Override
    public void handle(String line) {
        if (capturing) {
            if (isStackTraceLine(line)) {
                buffer.append(line).append("\n");
                return;
            } else {
                sendAlert(buffer.toString());
                buffer.setLength(0);
                capturing = false;
            }
        }

        if (containsKeyword(line)) {
            buffer.setLength(0);
            buffer.append(line).append("\n");
            capturing = true;
        }
    }

    private boolean containsKeyword(String line) {
        String lower = line.toLowerCase();
        return keywords.stream().anyMatch(k -> lower.contains(k.toLowerCase()));
    }

    private boolean isStackTraceLine(String line) {
        return JAVA_STACK_TRACE.matcher(line).matches()
                || PYTHON_STACK_TRACE.matcher(line).matches()
                || CSHARP_STACK_TRACE.matcher(line).matches()
                || PHP_STACK_TRACE.matcher(line).matches()
                || JAVASCRIPT_STACK_TRACE.matcher(line).matches()
                || RUBY_STACK_TRACE.matcher(line).matches()
                || GO_STACK_TRACE.matcher(line).matches();
    }

    private void sendAlert(String alert) {
        if (isDuplicate(alert)) {
            log.warn("Skipping duplicate alert:\n {}", alert);
            return;
        }
        recordAlert(alert);

        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("N1ne-Token", alertToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                AlertTailRequest alertTailRequest = new AlertTailRequest();
                alertTailRequest.setTitle(getHostName() + " | " +  extractTitle(alert));
                if (alert.length() > 252) alertTailRequest.setDescription(alert.substring(0, 252) + "...");
                else alertTailRequest.setDescription(alert);
                alertTailRequest.setDetails(alert);
                alertTailRequest.setTimestamp(Instant.now());
                alertTailRequest.setLevel("ERROR");

                Map<String, String> zenkoTags = new HashMap<>();
                zenkoTags.put("log-file-name", fileName);
                zenkoTags.put("thread-name", Thread.currentThread().getName());
                zenkoTags.put("thread-id", String.valueOf(Thread.currentThread().getId()));
                zenkoTags.put("thread-state", Thread.currentThread().getState().toString());
                zenkoTags.put("host", getHostName());
                zenkoTags.put("ip-local", getLocalIp());
                zenkoTags.put("os", System.getProperty("os.name"));
                zenkoTags.put("os-version", System.getProperty("os.version"));
                zenkoTags.put("arch", System.getProperty("os.arch"));
                zenkoTags.put("timestamp", Instant.now().toString());
                alertTailRequest.setMetadata(zenkoTags);

                HttpEntity<AlertTailRequest> request = new HttpEntity<>(alertTailRequest, headers);

                restTemplate.postForObject(alertEndpoint, request, Void.class);
                log.info("Alert sent successfully.");
                return;
            } catch (Exception e) {
                attempt++;
                log.error("Failed to send alert (attempt {}): {}", attempt, e.getMessage());
                try {
                    Thread.sleep(baseBackoffMillis * (1L << (attempt - 1)));
                } catch (InterruptedException ignored) {}
            }
        }
        log.error("Giving up sending alert after " + maxRetries + " attempts.");
    }

    private boolean isDuplicate(String alert) {
        Instant lastSent = recentAlerts.get(alert);
        if (lastSent == null) return false;
        return Instant.now().isBefore(lastSent.plusSeconds(alertCooldownSeconds));
    }

    private void recordAlert(String alert) {
        recentAlerts.put(alert, Instant.now());
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }

    private String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            return "unknown-local-ip";
        }
        return "unknown-local-ip";
    }

    private String extractTitle(String alert) {
        String[] lines = alert.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // If line is NOT a stack trace line, consider it a candidate for title
            if (!isStackTraceLine(line)) {
                // Optional: truncate long titles for brevity
                return line.length() > 100 ? line.substring(0, 100) + "..." : line;
            }
        }
        // fallback: use first line or generic title
        return lines.length > 0 ? (lines[0].length() > 100 ? lines[0].substring(0, 100) + "..." : lines[0]) : "Zenko Alert";
    }
}
