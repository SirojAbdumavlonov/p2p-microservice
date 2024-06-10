package com.example.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaUserTopicConfig {

    @Bean
    public NewTopic userTopic(){
        return TopicBuilder
                .name("user-topic")
                .build();
    }
    //done
    @Bean
    public NewTopic transactionTopic(){
        return TopicBuilder
                .name("transaction-to-card-topic")
                .build();
    }

}
