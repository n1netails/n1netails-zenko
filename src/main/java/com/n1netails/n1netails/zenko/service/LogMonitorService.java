package com.n1netails.n1netails.zenko.service;

import com.n1netails.n1netails.zenko.config.LogTailConfig;
import com.n1netails.n1netails.zenko.listener.LogTailListener;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

@Slf4j
@Service
public class LogMonitorService {

    private final LogTailConfig properties;

    public LogMonitorService(LogTailConfig properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void startTailing() {
        log.info("Zenko tailing files");
        for (String file : properties.getFiles()) {
            log.info("file name: {}", file);
            LogTailListener listener = new LogTailListener(
                    file,
                    properties.getKeywords(),
                    properties.getAlertEndpoint(),
                    properties.getAlertToken()
            );

            Tailer tailer = Tailer.builder()
                    .setFile(new File(file))
                    .setTailerListener(listener)
                    .setDelayDuration(Duration.ofMillis(1000))
                    .setTailFromEnd(true)
                    .get();
            new Thread(tailer).start();
        }
    }
}
