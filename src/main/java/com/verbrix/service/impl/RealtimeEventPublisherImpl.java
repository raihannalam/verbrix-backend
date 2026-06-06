package com.verbrix.service.impl;

import com.verbrix.service.RealtimeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeEventPublisherImpl implements RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String email, String eventType, Object payload) {
        log.info("Sending event '{}' to user: {}", eventType, email);
        messagingTemplate.convertAndSendToUser(
                email,
                "queue/notifications",
                Map.of("type", eventType, "data" , payload)
        );


    }



    @Override
    public void sendToGroup(String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }
}
