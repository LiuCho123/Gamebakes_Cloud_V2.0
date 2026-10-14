# Configuración de AWS SQS para GameBakes

## Configuración con Rol IAM de EC2

Los servicios `servicio-pagos` y `servicio-productos` usan automáticamente el rol IAM asignado a la instancia EC2. **No se requieren credenciales explícitas (access key, secret key).**

El cliente SQS del SDK (SqsAsyncClient) toma las credenciales automáticamente del rol de la EC2.

## Variables de Entorno Opcionales

Puedes configurar las URLs de las colas SQS como variables de entorno en docker-compose.yml:

```yaml
servicio-pagos:
  environment:
    - SQS_QUEUE_PAGO_EXITOSO=https://sqs.us-east-1.amazonaws.com/675168827747/gamebakes-pago-exitoso
    - SQS_QUEUE_DLQ=https://sqs.us-east-1.amazonaws.com/675168827747/gamebakes-pago-DLQ

servicio-productos:
  environment:
    - SQS_QUEUE_PAGO_EXITOSO=https://sqs.us-east-1.amazonaws.com/675168827747/gamebakes-pago-exitoso
    - SQS_QUEUE_DLQ=https://sqs.us-east-1.amazonaws.com/675168827747/gamebakes-pago-DLQ
```

Si no se definen, se usan los valores por defecto en application.properties.

## Configuración del Rol IAM

La instancia EC2 debe tener un rol IAM con los siguientes permisos SQS:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sqs:SendMessage",
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:GetQueueAttributes"
            ],
            "Resource": [
                "arn:aws:sqs:us-east-1:675168827747:gamebakes-pago-exitoso",
                "arn:aws:sqs:us-east-1:675168827747:gamebakes-pago-DLQ"
            ]
        }
    ]
}
```

## Verificación

Para verificar que el rol tiene permisos SQS, ejecuta en la EC2:

```bash
aws sqs get-queue-attributes \
  --queue-url https://sqs.us-east-1.amazonaws.com/675168827747/gamebakes-pago-exitoso \
  --attribute-names QueueArn
```

Si falla con "AccessDenied", el rol no tiene permisos SQS.

## Tecnologías Utilizadas

- **Spring Cloud AWS**: Integración de Spring Boot con servicios de AWS
- **@SqsListener**: Anotación para consumir mensajes de SQS automáticamente
- **SqsTemplate**: Template simplificado para enviar mensajes a SQS
- **Rol IAM de EC2**: Credenciales automáticas sin configuración explícita

## Solución de Problemas

### Error: Unable to load credentials

Si el servicio falla con "Unable to load credentials", significa que el contenedor no está alcanzando el rol de la EC2. Verifica:

1. Que el rol IAM esté asignado a la instancia EC2 correcta (Backend-Spring-Rabbit)
2. Que el rol tenga los permisos SQS necesarios
3. Que la instancia EC2 tenga permisos para asumir el rol

### Error: AccessDenied

Si obtienes errores de acceso, verifica que:
- El rol IAM tenga permisos para las colas SQS específicas
- El ARN de las colas coincida con la política del rol
- La región esté configurada correctamente (us-east-1)
