package cl.campuslab.rabbitadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class RabbitAdminRequests {

    private RabbitAdminRequests() {
    }

    public record QueueRequest(

            @NotBlank(message = "El nombre de la cola es obligatorio.")
            @Size(
                    max = 200,
                    message = "El nombre debe tener como máximo 200 caracteres."
            )
            String name,

            @NotNull(message = "El TTL es obligatorio.")
            @Min(
                    value = 1000,
                    message = "El TTL mínimo es de 1000 milisegundos."
            )
            @Max(
                    value = 604800000,
                    message = "El TTL máximo es de 7 días."
            )
            Long ttlMs,

            @NotNull(message = "El límite de mensajes es obligatorio.")
            @Min(
                    value = 1,
                    message = "La cola debe admitir al menos un mensaje."
            )
            @Max(
                    value = 100000,
                    message = "El máximo permitido es de 100000 mensajes."
            )
            Integer maxLength

    ) {
    }

    public record ExchangeRequest(

            @NotBlank(message = "El nombre del exchange es obligatorio.")
            @Size(
                    max = 200,
                    message = "El nombre debe tener como máximo 200 caracteres."
            )
            String name,

            @NotNull(message = "El tipo de exchange es obligatorio.")
            ExchangeType type

    ) {
    }

    public record BindingRequest(

            @NotBlank(message = "El nombre de la cola es obligatorio.")
            @Size(
                    max = 200,
                    message = "El nombre debe tener como máximo 200 caracteres."
            )
            String queue,

            @NotBlank(message = "El nombre del exchange es obligatorio.")
            @Size(
                    max = 200,
                    message = "El nombre debe tener como máximo 200 caracteres."
            )
            String exchange,

            @NotBlank(message = "La routing key es obligatoria.")
            @Size(
                    max = 200,
                    message = "La routing key debe tener como máximo 200 caracteres."
            )
            String routingKey

    ) {
    }

    public enum ExchangeType {
        DIRECT,
        TOPIC
    }
}