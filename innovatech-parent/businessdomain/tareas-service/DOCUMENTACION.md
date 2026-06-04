# API Tareas Service

## Descripción
Microservicio responsable de la gestión integral de tareas en la plataforma InnovatechPlataform.

## Características

### Entidades
- **Tarea**: Representa una tarea en el sistema con estados, prioridades y asignaciones.
- **EstadoTarea**: Estados posibles (PENDIENTE, EN_PROGRESO, EN_REVISION, COMPLETADA, CANCELADA)
- **PrioridadTarea**: Niveles de prioridad (BAJA, MEDIA, ALTA, URGENTE)

### Funcionalidades

#### Operaciones CRUD
- ✅ Crear tarea
- ✅ Obtener tarea por ID
- ✅ Obtener todas las tareas (con paginación)
- ✅ Actualizar tarea
- ✅ Eliminar tarea

#### Filtrado y Búsqueda
- ✅ Filtrar por proyecto
- ✅ Filtrar por equipo
- ✅ Filtrar por usuario asignado
- ✅ Filtrar por estado
- ✅ Filtrar por prioridad
- ✅ Filtrar por estado y prioridad combinados
- ✅ Buscar tareas próximas a vencer

#### Gestión de Estado
- ✅ Cambiar estado de tarea
- ✅ Asignar tarea a un usuario
- ✅ Obtener estadísticas de tareas

## Endpoints

### Base URL
```
http://localhost:8084/tareas-api/api/v1/tareas
```

### POST - Crear Tarea
```
POST /api/v1/tareas
Content-Type: application/json

{
  "titulo": "Implementar login",
  "descripcion": "Implementar autenticación OAuth",
  "estado": "PENDIENTE",
  "prioridad": "ALTA",
  "proyectoId": 1,
  "equipoId": 1,
  "asignadoA": 1,
  "fechaVencimiento": "2024-12-31T23:59:59"
}
```

### GET - Obtener Tarea por ID
```
GET /api/v1/tareas/{id}
```

### GET - Obtener Todas las Tareas
```
GET /api/v1/tareas?page=0&size=20
```

### GET - Tareas por Proyecto
```
GET /api/v1/tareas/proyecto/{proyectoId}?page=0&size=20
```

### GET - Tareas por Equipo
```
GET /api/v1/tareas/equipo/{equipoId}?page=0&size=20
```

### GET - Tareas Asignadas a Usuario
```
GET /api/v1/tareas/usuario/{usuarioId}?page=0&size=20
```

### GET - Tareas por Estado
```
GET /api/v1/tareas/estado/{estado}?page=0&size=20
```

### GET - Tareas por Prioridad
```
GET /api/v1/tareas/prioridad/{prioridad}?page=0&size=20
```

### GET - Tareas Filtradas
```
GET /api/v1/tareas/filtro?estado=PENDIENTE&prioridad=ALTA&page=0&size=20
```

### PUT - Actualizar Tarea
```
PUT /api/v1/tareas/{id}
Content-Type: application/json

{
  "titulo": "Implementar login",
  "descripcion": "Actualizar descripción",
  "estado": "EN_PROGRESO",
  "prioridad": "MEDIA"
}
```

### PATCH - Actualizar Estado
```
PATCH /api/v1/tareas/{id}/estado?nuevoEstado=EN_PROGRESO
```

### PATCH - Asignar Tarea
```
PATCH /api/v1/tareas/{id}/asignar/{usuarioId}
```

### DELETE - Eliminar Tarea
```
DELETE /api/v1/tareas/{id}
```

### GET - Tareas Próximas a Vencer
```
GET /api/v1/tareas/proximas-a-vencer?inicio=2024-01-01T00:00:00&fin=2024-12-31T23:59:59
```

### GET - Estadísticas
```
GET /api/v1/tareas/estadisticas
```

## Configuración

### Base de Datos
- Motor: MySQL
- Base de datos: innovatech_tareas
- Usuario: root
- Contraseña: root

### Puertos
- Tareas Service: 8084

### Eureka
- Registro automático en Eureka
- Service ID: tareas-service

## Dependencias Principales

- Spring Boot 3.x
- Spring Data JPA
- Spring Cloud (Eureka, OpenFeign)
- MySQL Connector
- Lombok
- Validation API

## Manejo de Errores

El servicio implementa un manejador global de excepciones que devuelve:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Tarea No Encontrada",
  "mensaje": "Tarea no encontrada con ID: 999",
  "ruta": "/api/v1/tareas/999"
}
```

## Validaciones

- El título es requerido
- El estado es requerido
- La prioridad es requerida
- Validación de campos en DTOs

## Logging

- Nivel de LOG para com.innovatech: DEBUG
- Nivel de LOG root: INFO
- Formato: timestamp - mensaje
