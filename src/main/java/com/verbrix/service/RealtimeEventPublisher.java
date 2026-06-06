package com.verbrix.service;

public interface RealtimeEventPublisher {

    void sendToUser(String email, String eventType, Object payload);
    void sendToGroup(String topic, Object payload);
}