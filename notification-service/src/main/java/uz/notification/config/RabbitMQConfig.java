package uz.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.common.messaging.RabbitConstants;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange authExchange() {
        return ExchangeBuilder.topicExchange(RabbitConstants.AUTH_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange todoExchange() {
        return ExchangeBuilder.topicExchange(RabbitConstants.TODO_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(RabbitConstants.USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Queue todoCreatedQueue() {
        return QueueBuilder.durable(RabbitConstants.TODO_CREATED_QUEUE).build();
    }

    @Bean
    public Queue todoCompletedQueue() {
        return QueueBuilder.durable(RabbitConstants.TODO_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue todoDueSoonQueue() {
        return QueueBuilder.durable(RabbitConstants.TODO_DUE_SOON_QUEUE).build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(authExchange)
                .with(RabbitConstants.USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public Binding todoCreatedBinding(Queue todoCreatedQueue, TopicExchange todoExchange) {
        return BindingBuilder.bind(todoCreatedQueue).to(todoExchange)
                .with(RabbitConstants.TODO_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding todoCompletedBinding(Queue todoCompletedQueue, TopicExchange todoExchange) {
        return BindingBuilder.bind(todoCompletedQueue).to(todoExchange)
                .with(RabbitConstants.TODO_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding todoDueSoonBinding(Queue todoDueSoonQueue, TopicExchange todoExchange) {
        return BindingBuilder.bind(todoDueSoonQueue).to(todoExchange)
                .with(RabbitConstants.TODO_DUE_SOON_ROUTING_KEY);
    }
}