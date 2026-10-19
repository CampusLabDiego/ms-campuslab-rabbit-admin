package cl.campuslab.rabbitadmin.controller;

import jakarta.validation.Valid;

import cl.campuslab.rabbitadmin.dto.RabbitAdminRequests.BindingRequest;
import cl.campuslab.rabbitadmin.dto.RabbitAdminRequests.ExchangeRequest;
import cl.campuslab.rabbitadmin.dto.RabbitAdminRequests.QueueRequest;

import cl.campuslab.rabbitadmin.service.RabbitAdminService;
import cl.campuslab.rabbitadmin.service.RabbitAdminService.QueueStatus;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class RabbitAdminController {

    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponse createQueue(
            @Valid @RequestBody QueueRequest request
    ) {
        service.createQueue(request);

        return new OperationResponse(
                "Cola declarada correctamente.",
                request.name()
        );
    }

    @GetMapping("/queues/{name}")
    public QueueStatus getQueue(
            @PathVariable("name") String name
    ) {
        return service.getQueue(name);
    }

    @DeleteMapping("/queues/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQueue(
            @PathVariable("name") String name
    ) {
        service.deleteQueue(name);
    }

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponse createExchange(
            @Valid @RequestBody ExchangeRequest request
    ) {
        service.createExchange(request);

        return new OperationResponse(
                "Exchange declarado correctamente.",
                request.name()
        );
    }

    @DeleteMapping("/exchanges/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExchange(
            @PathVariable("name") String name
    ) {
        service.deleteExchange(name);
    }

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponse createBinding(
            @Valid @RequestBody BindingRequest request
    ) {
        service.createBinding(request);

        return new OperationResponse(
                "Binding declarado correctamente.",
                request.routingKey()
        );
    }

    @DeleteMapping("/bindings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBinding(
            @Valid @RequestBody BindingRequest request
    ) {
        service.deleteBinding(request);
    }

    public record OperationResponse(
            String message,
            String resource
    ) {
    }
}