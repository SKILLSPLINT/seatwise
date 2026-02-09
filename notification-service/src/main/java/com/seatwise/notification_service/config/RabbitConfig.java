package com.seatwise.notification_service.config;

import constants.RabbitConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        return template;
    }

    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(RabbitConstants.NOTIFICATION_EXCHANGE);
    }

    @Bean
    TopicExchange deadLetterExchange() {
        return new TopicExchange(RabbitConstants.DLX);
    }

    @Bean
    TopicExchange emailExchange() {
        return new TopicExchange(RabbitConstants.EMAIL_EXCHANGE);
    }

    @Bean
    TopicExchange emailDeadLetterExchange() {
        return new TopicExchange(RabbitConstants.EMAIL_DLX);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(RabbitConstants.EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitConstants.EMAIL_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitConstants.EMAIL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue emailRetryQueue() {
        return QueueBuilder.durable(RabbitConstants.EMAIL_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitConstants.EMAIL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitConstants.EMAIL_ROUTING_KEY)
                .withArgument("x-message-ttl", 5000)
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(RabbitConstants.EMAIL_DLQ).build();
    }


    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(RabbitConstants.NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitConstants.DLX)
                .withArgument("x-dead-letter-routing-key", RabbitConstants.DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitConstants.DLQ).build();
    }


    //     binding
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(RabbitConstants.NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitConstants.DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(RabbitConstants.EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding emailRetryBinding() {
        return BindingBuilder.bind(emailRetryQueue())
                .to(emailExchange())
                .with(RabbitConstants.EMAIL_RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq())
                .to(emailDeadLetterExchange())
                .with(RabbitConstants.EMAIL_DLQ_ROUTING_KEY);
    }
}

