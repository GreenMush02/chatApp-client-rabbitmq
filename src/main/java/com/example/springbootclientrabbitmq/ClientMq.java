package com.example.springbootclientrabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodIntrospector;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@RestController
public class ClientMq  {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Autowired
    RestTemplate restTemplate;
    ConnectionFactory factory = new ConnectionFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();


    public ClientMq() throws IOException, TimeoutException {
    }

    @RabbitListener(queues = "general", messageConverter = "Jackson2JsonMessageConverter")
    public void get(@RequestBody MessageDto message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MessageDto> requestEntity = new HttpEntity<>(message, headers);

        restTemplate.postForObject("http://localhost:8082/messages", requestEntity, MessageDto.class);

        if(message != null) {
            System.out.println(message);;
        } else {
            System.out.println("BŁĄD");
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/checkMessages")
    public String check() throws IOException {
        AMQP.Queue.DeclareOk result = channel.queueDeclarePassive("java");
        int messageCount = result.getMessageCount();
        if(messageCount != 0) {
            return "Masz oczekujące wiadomości!";
        } else {
            return "Nie masz oczekujących wiadomości!";
        }
    }



}
