package uz.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.common.event.TodoCompletedEvent;
import uz.common.event.TodoCreatedEvent;
import uz.common.event.TodoDueSoonEvent;
import uz.common.messaging.RabbitConstants;
import uz.notification.entity.Notification;
import uz.notification.enums.NotificationChannel;
import uz.notification.enums.NotificationType;
import uz.notification.repository.NotificationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodoEventListener {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitConstants.TODO_CREATED_QUEUE)
    public void onTodoCreated(TodoCreatedEvent event) {
        save(event.getUserId(), NotificationType.TODO_CREATED,
                "Todo created: \"%s\"".formatted(event.getTitle()));
    }

    @RabbitListener(queues = RabbitConstants.TODO_COMPLETED_QUEUE)
    public void onTodoCompleted(TodoCompletedEvent event) {
        save(event.getUserId(), NotificationType.TODO_COMPLETED,
                "Todo completed: \"%s\"".formatted(event.getTitle()));
    }

    @RabbitListener(queues = RabbitConstants.TODO_DUE_SOON_QUEUE)
    public void onTodoDueSoon(TodoDueSoonEvent event) {
        save(event.getUserId(), NotificationType.TODO_DUE_SOON,
                "Todo \"%s\" is due on %s".formatted(event.getTitle(), event.getDueDate()));
    }

    private void save(Long userId, NotificationType type, String message) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .channel(NotificationChannel.LOG)
                .message(message)
                .build());
        log.info("[simulated notification] user {} -> {}", userId, message);
    }
}