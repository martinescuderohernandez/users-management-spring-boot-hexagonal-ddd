# **Contexto resumido de la conversación**

Estoy trabajando en actividades prácticas para estudiantes de **Proyecto de Grado / Ingeniería de Software**, próximos a finalizar la carrera.

La mayoría de los estudiantes ya sabe desarrollar aplicaciones web con **frontend \+ backend \+ base de datos**, por lo que quiero tratar temas más avanzados y prácticos, orientados a convertir una aplicación que “funciona” en un sistema más cercano a producción.

## **Enfoque general acordado**

La idea no es enseñar más frameworks, sino trabajar problemas reales de ingeniería de software como:

* arquitectura de software;  
* APIs REST profesionales;  
* validaciones y manejo de errores;  
* autenticación y autorización;  
* seguridad;  
* testing;  
* Docker;  
* CI/CD;  
* rendimiento;  
* concurrencia;  
* idempotencia;  
* caché;  
* resiliencia;  
* procesamiento asíncrono;  
* mensajería;  
* consistencia distribuida;  
* observabilidad;  
* pruebas de carga;  
* despliegue y rollback.

El enfoque pedagógico acordado es:

1. presentar un problema real;  
2. preguntar qué creen que ocurrirá;  
3. provocar el problema;  
4. explicar la base conceptual;  
5. hacer una solución guiada;  
6. dejar un reto práctico;  
7. exigir una prueba que demuestre que la solución funciona;  
8. aplicar el concepto al proyecto de grado de cada estudiante;  
9. pedir evidencia y sustentación.

Ejemplos de preguntas disparadoras:

* ¿Qué pasa si entran 500 usuarios al mismo tiempo?  
* ¿Qué pasa si el usuario presiona dos veces “Pagar”?  
* ¿Qué pasa si la base de datos tarda 15 segundos?  
* ¿Qué pasa si un servicio externo está caído?  
* ¿Cómo sabemos por qué falló una petición ayer?  
* ¿Qué pasa si alguien roba un JWT?  
* ¿Qué pasa si dos usuarios intentan comprar la última unidad al mismo tiempo?  
* ¿Qué pasa si una transferencia se procesa pero la respuesta se pierde?  
* ¿Qué pasa si guardamos el pedido pero falla la publicación del evento?  
* ¿Qué pasa si el broker entrega dos veces el mismo mensaje?  
* ¿Qué pasa si un usuario modifica el ID de otro recurso?  
* ¿Qué pasa si alguien llama el endpoint 20.000 veces por minuto?  
* ¿Qué pasa si una contraseña queda en GitHub?  
* ¿Qué pasa si una nueva versión tiene un error y necesitamos rollback?

## **Repositorio base**

Se decidió utilizar como repositorio maestro:

[https://github.com/arrietajohn/users-management-spring-boot-hexagonal-ddd](https://github.com/arrietajohn/users-management-spring-boot-hexagonal-ddd)

Este repositorio ya se usa para enseñar fundamentos de arquitectura hexagonal, DDD y algunos patrones.

Tiene, entre otros:

* Spring Boot;  
* Java 17;  
* arquitectura hexagonal;  
* separación domain / application / infrastructure;  
* puertos y adaptadores;  
* DDD;  
* domain events;  
* JDBC;  
* MySQL;  
* HikariCP;  
* Bean Validation;  
* BCrypt;  
* OpenAPI / Swagger;  
* JUnit;  
* Mockito;  
* JaCoCo.

Se concluyó que el repositorio sirve muy bien como base transversal de los talleres.

No se debe forzar todos los problemas sobre “usuarios”. Para temas como concurrencia, idempotencia, mensajería o consistencia distribuida, se propuso extender mínimamente el dominio con conceptos como:

* Subscription;  
* Plan;  
* Payment;  
* Notification.

Por ejemplo:

* doble activación de una suscripción → idempotencia;  
* último cupo de un plan → concurrencia;  
* proveedor de pago caído → resiliencia;  
* SubscriptionActivated → mensajería;  
* mensaje duplicado → consumidor idempotente;  
* guardar suscripción y fallar al publicar evento → Transactional Outbox.

La idea es mantener la identidad de `users-management` y agregar solo lo necesario para provocar problemas reales.

## **Posible organización de ramas**

Se propuso evolucionar el repositorio con ramas o etapas como:

* 01-rest-profesional  
* 02-validation-errors  
* 03-testing  
* 04-security-jwt  
* 05-docker  
* 06-ci-cd  
* 07-performance-cache  
* 08-concurrency  
* 09-idempotency  
* 10-resilience  
* 11-messaging  
* 12-outbox  
* 13-observability  
* 14-load-testing

Cada taller podría tener:

* una versión `starter`, con el problema;  
* una versión `solution`, para la explicación del docente.

## **Ruta de talleres pensada**

Se propusieron inicialmente estos talleres:

1. Entendiendo una arquitectura existente.  
2. Diseño profesional de API REST.  
3. Validación y manejo de errores.  
4. Autenticación y autorización.  
5. Seguridad: atacar nuestra propia API.  
6. Testing y regresión.  
7. Docker.  
8. CI/CD.  
9. Base de datos bajo presión.  
10. Concurrencia.  
11. Idempotencia.  
12. Caché y rendimiento.  
13. Resiliencia.  
14. Procesamiento asíncrono.  
15. Mensajería confiable.  
16. Consistencia distribuida / Outbox.  
17. Observabilidad.  
18. Pruebas de carga.  
19. Despliegue y rollback.  
20. Desafío final de “sobrevivir a producción”.

## **Restricción económica importante**

Los estudiantes:

* no tienen dinero para pagar infraestructura;  
* muchos no tienen tarjeta de crédito;  
* cuentan con su PC;  
* tienen Internet;  
* pueden usar IA gratuita;  
* pueden usar servicios gratuitos como Render y Supabase.

Por ello se acordó un enfoque **local-first**.

Ningún taller obligatorio debe depender de AWS, Azure, Oracle Cloud o tarjetas de crédito.

## **Estrategia de infraestructura**

Se propuso que la mayor parte del curso se pueda ejecutar en el PC usando Docker.

Equivalencias locales sugeridas:

* MySQL/PostgreSQL → Docker  
* Redis → Docker  
* RabbitMQ → Docker  
* Kafka/Redpanda → Docker  
* Keycloak → autenticación/OIDC local  
* WireMock → simulación de APIs externas  
* Mailpit → correo local  
* Prometheus → métricas  
* Grafana → dashboards  
* OpenTelemetry → observabilidad  
* Jaeger → tracing  
* k6/JMeter → carga  
* almacenamiento S3 compatible → local  
* LocalStack → simulación de algunos servicios AWS

La filosofía es enseñar los conceptos detrás de la nube, no enseñar únicamente a hacer clic en AWS.

Ejemplo:

AWS SQS → RabbitMQ / LocalStack  
AWS S3 → almacenamiento S3 compatible local  
Cognito → Keycloak  
CloudWatch → Prometheus/Grafana  
X-Ray → OpenTelemetry/Jaeger  
RDS → PostgreSQL/MySQL local

## **Proveedores cloud gratuitos considerados**

### **Render**

Se considera útil para enseñar despliegue real de Spring Boot desde GitHub.

Puede usarse como experiencia de:

GitHub → build → deploy → URL pública.

No se debe depender de él para toda la arquitectura.

### **Supabase**

Se considera útil como base de datos PostgreSQL remota gratuita.

Una arquitectura educativa posible:

GitHub → Render → Spring Boot → Supabase PostgreSQL

### **Google Cloud**

Se encontró que Google tiene un Starter Tier sin tarjeta para determinados usos, pero debe evaluarse antes de convertirlo en requisito obligatorio.

### **Oracle Cloud**

Se descartó como requisito porque normalmente exige tarjeta para verificar la cuenta.

### **AWS/Azure**

No deben ser requisito obligatorio.

El profesor puede mostrarlos como demostración, pero el alumno debe poder completar el taller localmente.

## **Uso de arquitectura hexagonal para enseñar infraestructura**

Se detectó una oportunidad pedagógica importante:

gracias a la arquitectura hexagonal del repo, se puede cambiar infraestructura sin modificar el dominio.

Ejemplo:

StoragePort  
→ LocalStorageAdapter  
→ S3StorageAdapter

o:

NotificationPort  
→ Mailpit  
→ SendGrid  
→ SES

Esto sirve para demostrar realmente por qué existen puertos y adaptadores.

## **Primera actividad creada**

Se empezó a trabajar una primera guía para instalar herramientas esenciales.

Se decidió iniciar con:

1. Git  
2. Docker

El usuario pidió expresamente que no sea una actividad genérica, sino una **guía paso a paso y sin errores**.

Se preparó una guía separando:

### **Windows**

* PowerShell;  
* instalación y verificación de Git;  
* `winget` o instalador oficial;  
* configuración `user.name` y `user.email`;  
* creación de primer repo;  
* WSL 2;  
* Docker Desktop;  
* verificación de virtualización;  
* `docker --version`;  
* `docker compose version`;  
* `docker info`.

### **Linux**

Se tomó como referencia Ubuntu 22.04 / 24.04.

Incluye:

* `apt`;  
* instalación de Git;  
* instalación oficial de Docker Engine;  
* repositorio oficial de Docker;  
* `docker-ce`;  
* `docker-ce-cli`;  
* `containerd.io`;  
* `docker-buildx-plugin`;  
* `docker-compose-plugin`;  
* configuración del grupo `docker`;  
* ejecución sin `sudo`.

## **Primera práctica con Docker**

La guía incluye:

docker run hello-world

y luego Nginx:

docker run \--name nginx-lab \-d \-p 8080:80 nginx:alpine

Los estudiantes deben:

* comprobar con `docker ps`;  
* abrir `http://localhost:8080`;  
* consultar logs;  
* detener;  
* reiniciar;  
* eliminar;  
* comprobar la diferencia entre imagen y contenedor.

Luego hacen un reto guiado con Apache:

docker run \--name apache-lab \-d \-p 8081:80 httpd:alpine

## **Evidencia esperada en las actividades**

Las actividades deben enfatizar:

* uso de Git;  
* varios commits reales, no un único commit al final;  
* repositorio GitHub;  
* README con evidencia;  
* explicación de dificultades;  
* sustentación mediante video.

En actividades anteriores del proyecto se ha establecido como criterio frecuente que la sustentación sea mediante video en YouTube o Google Drive institucional, con permisos correctos para que el docente pueda visualizarlo.

El estudiante debe explicar:

* qué aprendió;  
* qué hizo;  
* por qué tomó determinadas decisiones;  
* cómo se relaciona con su formación como ingeniero de software;  
* y demostrar que realmente ejecutó la práctica.

## **Idea central que debe conservarse**

Los talleres no deben convertirse en simples recetas de comandos.

La estructura debe ser:

**guía paso a paso del docente \+ problema real \+ práctica del estudiante \+ prueba de que funciona \+ explicación de por qué funciona.**

La meta general es que los estudiantes pasen de:

> “sé hacer un CRUD”

a:

> “sé diseñar, probar, desplegar, observar y hacer resistente una aplicación ante problemas reales”.

