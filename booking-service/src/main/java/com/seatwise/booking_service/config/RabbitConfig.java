package com.seatwise.booking_service.config;

import constants.RabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    public TopicExchange emailExchange() {
        return new TopicExchange(RabbitConstants.EMAIL_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(RabbitConstants.PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue bookingConfirmationQueue() {
        return new Queue(RabbitConstants.BOOKING_CONFIRMATION_QUEUE);
    }

    @Bean
    public Binding bookingConfirmationBinding() {
        return BindingBuilder.bind(bookingConfirmationQueue())
                .to(paymentExchange())
                .with(RabbitConstants.PAYMENT_SUCCESS_ROUTING_KEY);
    }
}
