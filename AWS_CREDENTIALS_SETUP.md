# Configuración de Credenciales AWS para SQS

## Variables de Entorno Requeridas

Los servicios `servicio-pagos` y `servicio-productos` requieren las siguientes variables de entorno para conectarse a AWS SQS usando Spring Cloud AWS:

```bash
AWS_ACCESS_KEY_ID=tu_access_key_aqui
AWS_SECRET_ACCESS_KEY=tu_secret_key_aqui
AWS_SESSION_TOKEN=tu_session_token_aqui
```

## Cómo Obtener Nuevas Credenciales

Las credenciales de AWS Academy expiran después de un tiempo (generalmente 1-3 horas). Para obtener nuevas credenciales:

1. Entra al portal de AWS Academy
2. Busca el laboratorio activo
3. Busca la sección "AWS Account Details" o "Credentials"
4. Copia las nuevas credenciales (Access Key ID, Secret Access Key, Session Token)

## Cómo Configurar las Variables de Entorno

### En la EC2 (docker-compose)

Edita el archivo `docker-compose.yml` y agrega las variables de entorno en la sección de cada servicio:

```yaml
servicio-pagos:
  environment:
    - AWS_ACCESS_KEY_ID=tu_access_key
    - AWS_SECRET_ACCESS_KEY=tu_secret_key
    - AWS_SESSION_TOKEN=tu_session_token

servicio-productos:
  environment:
    - AWS_ACCESS_KEY_ID=tu_access_key
    - AWS_SECRET_ACCESS_KEY=tu_secret_key
    - AWS_SESSION_TOKEN=tu_session_token
```

### O exportar en la terminal de la EC2

```bash
export AWS_ACCESS_KEY_ID=tu_access_key
export AWS_SECRET_ACCESS_KEY=tu_secret_key
export AWS_SESSION_TOKEN=tu_session_token
```

## Tecnologías Utilizadas

- **Spring Cloud AWS**: Integración de Spring Boot con servicios de AWS
- **@SqsListener**: Anotación para consumir mensajes de SQS automáticamente
- **SqsTemplate**: Template simplificado para enviar mensajes a SQS

## Importante

- **Nunca subas las credenciales al repositorio de GitHub**
- Las credenciales son temporales y deben actualizarse cuando expiren
- Solo el equipo de desarrollo debe tener acceso a estas credenciales
- Spring Cloud AWS maneja automáticamente los reintentos y envío a DLQ
