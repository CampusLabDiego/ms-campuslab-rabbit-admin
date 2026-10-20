# Administrador RabbitMQ CampusLab

API REST para administrar colas, exchanges y bindings.
Requiere Access Token válido con el rol Admin.

## Tecnologías

- Java 21 y Spring Boot.
- Spring Web MVC.
- Spring AMQP y AmqpAdmin.
- Bean Validation.
- Spring Security OAuth2 Resource Server.
- Maven y Docker.

Este servicio no necesita base de datos.

## Endpoints

| Método | Ruta | Operación |
|---|---|---|
| POST | /api/admin/queues | Declarar cola |
| GET | /api/admin/queues/{name} | Consultar mensajes y consumidores |
| DELETE | /api/admin/queues/{name} | Eliminar cola vacía y sin consumidores |
| POST | /api/admin/exchanges | Declarar exchange |
| DELETE | /api/admin/exchanges/{name} | Eliminar exchange |
| POST | /api/admin/bindings | Declarar binding |
| DELETE | /api/admin/bindings | Eliminar binding |

DELETE /bindings recibe los mismos campos que su creación.

## Ejemplos

### Cola

```json
{
  "name": "demo.campuslab.cola",
  "ttlMs": 60000,
  "maxLength": 100
}
```

Las colas creadas son durables, classic y tienen:

- TTL obligatorio.
- Límite de mensajes obligatorio.
- Límite de bytes de 10 MiB.
- Overflow reject-publish.

Estas colas administrativas no incorporan automáticamente una DLQ.

### Exchange

```json
{
  "name": "demo.campuslab.direct",
  "type": "DIRECT"
}
```

Tipos permitidos: DIRECT y TOPIC.

### Binding

```json
{
  "queue": "demo.campuslab.cola",
  "exchange": "demo.campuslab.direct",
  "routingKey": "demo.prueba"
}
```

La cola y el exchange deben existir.

## Validaciones

- Nombres obligatorios y de hasta 200 caracteres.
- Se rechazan nombres con espacios iniciales o finales.
- Se rechaza el prefijo reservado amq.
- TTL entre 1000 y 604800000 milisegundos.
- Límite entre 1 y 100000 mensajes.
- Routing key obligatoria y con longitud validada.

## Respuestas

- 201: declaración completada.
- 200: consulta completada.
- 204: eliminación completada.
- 400: parámetros inválidos.
- 401: autenticación requerida o token inválido.
- 403: autorización insuficiente.
- 404: recurso no encontrado.
- 409: configuración incompatible o recurso en uso.
- 503: operación no disponible con RabbitMQ.

ApiExceptionHandler devuelve errores de validación como JSON.

## Seguridad

Valida firma, issuer, audience, expiración, scope access_as_user
e identificador del usuario.

El rol Admin se exige en /api/admin y sus subrutas.

Para las demostraciones se recomienda usar el prefijo
demo.campuslab. y conservar las colas funcionales del sistema.

## Configuración

- RABBITMQ_ADDRESSES.
- RABBITMQ_USER.
- RABBITMQ_PASSWORD.
- RABBITMQ_VHOST.

Puerto: 8084, accesible dentro de la red Docker.
El acceso externo se realiza mediante API Gateway y BFF.

## Compilación

```powershell
.\mvnw.cmd clean package -DskipTests
```

Este comando compila y empaqueta; omite la ejecución de pruebas.

El despliegue está definido en infra-campuslab.