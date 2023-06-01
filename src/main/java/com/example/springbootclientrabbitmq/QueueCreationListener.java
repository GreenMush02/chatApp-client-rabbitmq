package com.example.springbootclientrabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class QueueCreationListener {
    @Autowired
    private ConnectionFactory connectionFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    RestTemplate restTemplate;

    public void createListener(String queueId) {

            boolean queueExists = rabbitAdmin.getQueueInfo(queueId) != null;
            if (queueExists) {
                // Tworzenie instancji SimpleMessageListenerContainer
                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

                // Konfiguracja połączenia i ustawienie queueId
                container.setConnectionFactory(connectionFactory);
                container.setQueueNames(queueId);

                // Ustawienie MessageListenerAdapter i metody obsługującej wiadomości
                MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(this, "handleMessage");
                container.setMessageListener(messageListenerAdapter);

                container.setExclusive(false);
                container.setShutdownTimeout(0);

                // Start nasłuchiwania
                container.start();
            } else {
                System.out.println("Listener dla podanej kolejki już istnieje!");
            }


    }

    // Metoda obsługująca odbiór wiadomości
    public void handleMessage(byte[] message) {
        // Konwersja tablicy bajtów na ciąg znaków lub inny format, jeśli to konieczne
        String messageString = new String(message, StandardCharsets.UTF_8);

        // Deserializacja ciągu znaków do obiektu MessageDto
        ObjectMapper objectMapper = new ObjectMapper();
        MessageDto messageDto;
        try {
            messageDto = objectMapper.readValue(messageString, MessageDto.class);
        } catch (IOException e) {
            System.err.println("Błąd podczas deserializacji wiadomości: " + e.getMessage());
            return;
        }

        // Przetwarzanie otrzymanej wiadomości
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MessageDto> requestEntity = new HttpEntity<>(messageDto, headers);

        restTemplate.postForObject("http://localhost:8082/messages", requestEntity, MessageDto.class);

        if (messageDto != null) {
            System.out.println(messageDto);
        } else {
            System.out.println("BŁĄD");
        }
     }
    }


