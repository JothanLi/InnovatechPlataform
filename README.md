# Innovatech Platform

Plataforma basada en microservicios para la gestión integral de proyectos tecnológicos de Innovatech Solutions.

Este proyecto corresponde al caso semestral de la asignatura Desarrollo Fullstack III. La solución permite administrar proyectos, tareas, equipos de trabajo y centralizar la comunicación entre frontend y backend mediante una arquitectura de microservicios.

## Integrantes

- Jonathan Jean Pierre Lillo Marín
- Sebastián Alberto Mariqueo Pérez
- Jorge Luis Salazar Parra

## Arquitectura del proyecto

El sistema está organizado como un proyecto Maven multi-módulo. Todos los componentes backend se encuentran dentro de un mismo repositorio GitHub bajo el proyecto padre `innovatech-parent`.

```txt
innovatech-parent/
│
├── pom.xml
│
├── businessdomain/
│   ├── pom.xml
│   ├── proyectos-service/
│   ├── tareas-service/
│   └── equipos-service/
│
└── infraestructuredomain/
    ├── pom.xml
    ├── eureka-server/
    ├── api-gateway/
    └── bff-service/
