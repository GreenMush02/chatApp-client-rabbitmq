package com.example.springbootclientrabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String messageId;
    private String userId;
    private String time;
    private String content;
    private boolean isDeleted;
    private String chatGroupId;
    @JsonProperty("queueId")
    private String queueId;


}