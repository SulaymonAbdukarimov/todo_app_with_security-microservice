package uz.todo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.common.event.TodoDueSoonEvent;
import uz.common.messaging.RabbitConstants;
import uz.todo.entity.Todo;
import uz.todo.repository.TodoRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DueSoonNotifier {

    private final TodoRepository todoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional
    public void notifyDueSoonTodos() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Todo> dueSoon = todoRepository
                .findByCompletedFalseAndDueSoonNotifiedFalseAndDueDateLessThanEqual(tomorrow);

        for (Todo todo : dueSoon) {
            rabbitTemplate.convertAndSend(RabbitConstants.TODO_EXCHANGE, RabbitConstants.TODO_DUE_SOON_ROUTING_KEY,
                    TodoDueSoonEvent.builder()
                            .todoId(todo.getId())
                            .userId(todo.getUserId())
                            .title(todo.getTitle())
                            .dueDate(todo.getDueDate())
                            .build());
            todo.setDueSoonNotified(true);
        }

        if (!dueSoon.isEmpty()) {
            todoRepository.saveAll(dueSoon);
            log.info("Published {} due-soon notifications", dueSoon.size());
        }
    }
}