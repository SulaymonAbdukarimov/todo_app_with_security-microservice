package uz.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import uz.common.event.UserRegisteredEvent;
import uz.common.messaging.RabbitConstants;
import uz.notification.entity.Notification;
import uz.notification.enums.NotificationChannel;
import uz.notification.enums.NotificationType;
import uz.notification.repository.NotificationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitConstants.USER_REGISTERED_QUEUE)
    public void onUserRegistered(UserRegisteredEvent event) {
        String message = "Welcome %s! Your account (%s) was created successfully."
                .formatted(event.getFullName(), event.getEmail());

        notificationRepository.save(Notification.builder()
                .userId(event.getUserId())
                .type(NotificationType.USER_REGISTERED)
                .channel(NotificationChannel.LOG)
                .message(message)
                .build());

        log.info("[simulated email] to {} -> {}", event.getEmail(), message);
    }
}