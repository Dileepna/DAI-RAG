package com.dai.microservice.rag.application.port.out;

public interface ChatGateway {
    String answer(String question, String context);
}
