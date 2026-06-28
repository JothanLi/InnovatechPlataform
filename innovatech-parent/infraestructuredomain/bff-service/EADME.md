# BFF Service - Innovatech

## Descripción

Este servicio corresponde al Backend For Frontend de la plataforma Innovatech.

Su responsabilidad es centralizar las llamadas del frontend y orquestar la información proveniente de los microservicios:

- proyectos-service
- tareas-service
- equipos-service

El BFF no contiene lógica de negocio compleja. Actúa como capa de integración y entrega respuestas agregadas al frontend.

## Patrón aplicado

Se aplica el patrón Facade mediante la clase:

```text
InnovatechBffFacade