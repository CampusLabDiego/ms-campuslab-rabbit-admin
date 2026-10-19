package cl.campuslab.rabbitadmin.service;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ShutdownSignalException;

import cl.campuslab.rabbitadmin.dto.RabbitAdminRequests.BindingRequest;
import cl.campuslab.rabbitadmin.dto.RabbitAdminRequests.ExchangeRequest;
import cl.campuslab.rabbitadmin.dto.RabbitAdminRequests.QueueRequest;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RabbitAdminService {

    private final AmqpAdmin admin;

    public RabbitAdminService(AmqpAdmin admin) {
        this.admin = admin;
    }

    public void createQueue(QueueRequest request) {
        validateName(request.name());

        Queue queue = QueueBuilder
                .durable(request.name())
                .withArgument("x-queue-type", "classic")
                .withArgument("x-message-ttl", request.ttlMs())
                .withArgument("x-max-length", request.maxLength())
                .withArgument("x-max-length-bytes", 10485760)
                .withArgument("x-overflow", "reject-publish")
                .build();

        execute(() -> {
            admin.declareQueue(queue);
            return null;
        });
    }

    public QueueStatus getQueue(String name) {
        validateName(name);

        return execute(() -> {
            QueueInformation information = admin.getQueueInfo(name);

            if (information == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cola no existe."
                );
            }

            return new QueueStatus(
                    name,
                    information.getMessageCount(),
                    information.getConsumerCount()
            );
        });
    }

    public void deleteQueue(String name) {
        validateName(name);

        execute(() -> {
            // El broker comprueba ambas condiciones de forma atómica:
            // la cola debe estar vacía y sin consumidores.
            admin.deleteQueue(name, true, true);
            return null;
        });
    }

    public void createExchange(ExchangeRequest request) {
        validateName(request.name());

        Exchange exchange = switch (request.type()) {
            case DIRECT -> new DirectExchange(
                    request.name(),
                    true,
                    false
            );

            case TOPIC -> new TopicExchange(
                    request.name(),
                    true,
                    false
            );
        };

        execute(() -> {
            admin.declareExchange(exchange);
            return null;
        });
    }

    public void deleteExchange(String name) {
        validateName(name);

        execute(() -> {
            boolean deleted = admin.deleteExchange(name);

            if (!deleted) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se pudo eliminar el exchange o ya no existe."
                );
            }

            return null;
        });
    }

    public void createBinding(BindingRequest request) {
        Binding binding = buildBinding(request);

        execute(() -> {
            admin.declareBinding(binding);
            return null;
        });
    }

    public void deleteBinding(BindingRequest request) {
        Binding binding = buildBinding(request);

        execute(() -> {
            admin.removeBinding(binding);
            return null;
        });
    }

    private Binding buildBinding(BindingRequest request) {
        validateName(request.queue());
        validateName(request.exchange());

        String routingKey = request.routingKey();

        if (
                routingKey == null
                || routingKey.isBlank()
                || routingKey.getBytes(StandardCharsets.UTF_8).length > 255
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La routing key es obligatoria y no debe superar 255 bytes."
            );
        }

        return new Binding(
                request.queue(),
                Binding.DestinationType.QUEUE,
                request.exchange(),
                routingKey,
                null
        );
    }

    private void validateName(String name) {
        if (
                name == null
                || name.isBlank()
                || !name.equals(name.trim())
                || name.length() > 200
                || name.getBytes(StandardCharsets.UTF_8).length > 255
                || name.startsWith("amq.")
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre inválido: debe tener entre 1 y 200 caracteres, "
                    + "sin espacios al inicio o al final, "
                    + "y no comenzar con amq."
            );
        }
    }

    private <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();

        } catch (AmqpException error) {
            throw translate(error);
        }
    }

    private ResponseStatusException translate(AmqpException error) {
        Throwable current = error;

        for (
                int depth = 0;
                current != null && depth < 15;
                depth++, current = current.getCause()
        ) {
            if (
                    current instanceof ShutdownSignalException signal
                    && signal.getReason() instanceof AMQP.Channel.Close close
            ) {
                int code = close.getReplyCode();

                if (code == 404) {
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "La cola o el exchange indicado no existe.",
                            error
                    );
                }

                if (code == 403) {
                    return new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "RabbitMQ rechazó la operación por permisos.",
                            error
                    );
                }

                if (code == 405 || code == 406) {
                    return new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "El recurso tiene otra configuración, "
                            + "está en uso o contiene mensajes.",
                            error
                    );
                }
            }
        }

        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo completar la operación con RabbitMQ.",
                error
        );
    }

    public record QueueStatus(
            String name,
            long messages,
            int consumers
    ) {
    }
}