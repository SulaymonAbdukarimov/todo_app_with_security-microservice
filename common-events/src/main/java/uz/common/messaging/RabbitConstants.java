package uz.common.messaging;

public final class RabbitConstants {

    private RabbitConstants() {
    }

    public static final String AUTH_EXCHANGE = "auth.exchange";
    public static final String TODO_EXCHANGE = "todo.exchange";

    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";
    public static final String TODO_CREATED_ROUTING_KEY = "todo.created";
    public static final String TODO_COMPLETED_ROUTING_KEY = "todo.completed";
    public static final String TODO_DUE_SOON_ROUTING_KEY = "todo.due-soon";

    public static final String USER_REGISTERED_QUEUE = "user.registered.queue";
    public static final String TODO_CREATED_QUEUE = "todo.created.queue";
    public static final String TODO_COMPLETED_QUEUE = "todo.completed.queue";
    public static final String TODO_DUE_SOON_QUEUE = "todo.due-soon.queue";
}