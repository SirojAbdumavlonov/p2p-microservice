package com.example.card.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaCardTopicConfig {

    @Bean
    public NewTopic cardUpdateBalanceTopic() {
        return TopicBuilder
                .name("card-balance-update-topic")
                .build();
    }
    @Bean
    public NewTopic cardActionTopic() {
        return TopicBuilder
                .name("card-action-topic")
                .build();
    }

}
