import { useCallback, useEffect, useMemo, useState } from "react";
import bffApi from "../api/bffApi";

const proyectoInicial = {
  nombre: "",
  descripcion: "",
  estado: "PLANNED",
  fechaInicio: "",
  fechaFinEstimada: "",
};

const miembroInicial = {
  nombres: "",
  apellidoPaterno: "",
  apellidoMaterno: "",
  email: "",
  rol: "DEVELOPER",
  password: "",
};

const tareaInicial = {
  descripcion: "",
  estado: "PENDING",
  responsable: "",
  fechaInicio: "",
  fechaFinEstimada: "",
};

const filtrosEstado = [
  { value: "TODOS", label: "Todos" },
  { value: "PLANNED", label: "Planificados" },
  { value: "IN_PROGRESS", label: "En progreso" },
  { value: "COMPLETED", label: "Completados" },
  { value: "CANCELLED", label: "Cancelados" },
];

const filtrosRolUsuario = [
  { value: "TODOS", label: "Todos" },
  { value: "ADMIN", label: "Administradores" },
  { value: "PROJECT_MANAGER", label: "Project Managers" },
  { value: "SCRUM_MASTER", label: "Scrum Masters" },
  { value: "DEVELOPER", label: "Developers" },
  { value: "QA", label: "QA" },
  { value: "DEVOPS", label: "DevOps" },
  { value: "UI_UX", label: "UI/UX" },
];

const filtrosEstadoUsuario = [
  { value: "TODOS", label: "Todos" },
  { value: "ACTIVE", label: "Activos" },
  { value: "INACTIVE", label: "Inactivos" },
  { value: "PENDING", label: "Pendientes" },
  { value: "BLOCKED", label: "Bloqueados" },
];

function AdminDashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [proyectos, setProyectos] = useState([]);
  const [miembros, setMiembros] = useState([]);
  const [formProyecto, setFormProyecto] = useState(proyectoInicial);
  const [formMiembro, setFormMiembro] = useState(miembroInicial);
  const [formTarea, setFormTarea] = useState(tareaInicial);
  const [proyectoSeleccionadoId, setProyectoSeleccionadoId] = useState(null);
  const [detalleProyecto, setDetalleProyecto] = useState(null);
  const [cargandoDetalle, setCargandoDetalle] = useState(false);
  const [idMiembroAsignar, setIdMiembroAsignar] = useState("");
  const [vistaDetalle, setVistaDetalle] = useState("resumen");
  const [busqueda, setBusqueda] = useState("");
  const [filtroEstado, setFiltroEstado] = useState("TODOS");
  const [busquedaUsuarios, setBusquedaUsuarios] = useState("");
  const [filtroRolUsuario, setFiltroRolUsuario] = useState("TODOS");
  const [filtroEstadoUsuario, setFiltroEstadoUsuario] = useState("TODOS");
  const [tabActiva, setTabActiva] = useState("resumen");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);

  const cargarVistaAdmin = useCallback(async () => {
    setCargando(true);
    setError("");

    try {
      const [dashboardResponse, proyectosResponse, miembrosResponse] =
        await Promise.all([
          bffApi.get("/dashboard/resumen"),
          bffApi.get("/proyectos"),
          bffApi.get("/miembros"),
        ]);

      setDashboard(dashboardResponse.data);
      setProyectos(proyectosResponse.data);
      setMiembros(miembrosResponse.data);
    } catch (error) {
      setError(
        obtenerMensajeError(
          error,
          "No se pudo cargar la vista de administración. Verifica que gateway, BFF y microservicios estén ejecutándose."
        )
      );
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    Promise.resolve().then(cargarVistaAdmin);
  }, [cargarVistaAdmin]);

  const resumenEstados = useMemo(() => {
    const total = dashboard?.totalProyectos ?? proyectos.length;

    return [
      {
        estado: "PLANNED",
        label: "Planificados",
        total: dashboard?.proyectosPlanificados ?? contarPorEstado(proyectos, "PLANNED"),
      },
      {
        estado: "IN_PROGRESS",
        label: "En progreso",
        total: dashboard?.proyectosEnProgreso ?? contarPorEstado(proyectos, "IN_PROGRESS"),
      },
      {
        estado: "COMPLETED",
        label: "Completados",
        total: dashboard?.proyectosCompletados ?? contarPorEstado(proyectos, "COMPLETED"),
      },
      {
        estado: "CANCELLED",
        label: "Cancelados",
        total: dashboard?.proyectosCancelados ?? contarPorEstado(proyectos, "CANCELLED"),
      },
    ].map((item) => ({
      ...item,
      porcentaje: total === 0 ? 0 : Math.round((item.total * 100) / total),
    }));
  }, [dashboard, proyectos]);

  const proyectosFiltrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();

    return proyectos.filter((proyecto) => {
      const coincideEstado =
        filtroEstado === "TODOS" || proyecto.estado === filtroEstado;

      const coincideTexto =
        !texto ||
        [proyecto.nombre, proyecto.descripcion, proyecto.estado]
          .filter(Boolean)
          .some((valor) => valor.toLowerCase().includes(texto));

      return coincideEstado && coincideTexto;
    });
  }, [busqueda, filtroEstado, proyectos]);

  const proyectosPorRevisar = useMemo(() => {
    return proyectos
      .filter((proyecto) => proyecto.estado === "CANCELLED" || proyecto.estado === "PLANNED")
      .slice(0, 5);
  }, [proyectos]);

  const usuariosFiltrados = useMemo(() => {
    const texto = busquedaUsuarios.trim().toLowerCase();

    return miembros.filter((miembro) => {
      const estadoNormalizado = normalizarEstadoMiembro(miembro.estado);
      const coincideRol =
        filtroRolUsuario === "TODOS" || miembro.rol === filtroRolUsuario;
      const coincideEstado =
        filtroEstadoUsuario === "TODOS" || estadoNormalizado === filtroEstadoUsuario;
      const coincideTexto =
        !texto ||
        [
          obtenerNombreCompletoMiembro(miembro),
          miembro.email,
          formatearRol(miembro.rol),
          formatearEstadoMiembro(miembro.estado),
        ]
          .filter(Boolean)
          .some((valor) => valor.toLowerCase().includes(texto));

      return coincideRol && coincideEstado && coincideTexto;
    });
  }, [busquedaUsuarios, filtroEstadoUsuario, filtroRolUsuario, miembros]);

  const resumenUsuarios = useMemo(() => {
    return {
      total: miembros.length,
      activos: miembros.filter((miembro) => normalizarEstadoMiembro(miembro.estado) === "ACTIVE").length,
      administradores: miembros.filter((miembro) => miembro.rol === "ADMIN").length,
      roles: new Set(miembros.map((miembro) => miembro.rol).filter(Boolean)).size,
    };
  }, [miembros]);

  const proyectoValido = useMemo(() => {
    return (
      formProyecto.nombre.trim().length >= 3 &&
      formProyecto.descripcion.trim().length >= 10 &&
      fechasProyectoValidas(formProyecto)
    );
  }, [formProyecto]);

  const miembroValido = useMemo(() => {
    return (
      formMiembro.nombres.trim().length >= 2 &&
      formMiembro.apellidoPaterno.trim().length >= 2 &&
      formMiembro.apellidoMaterno.trim().length >= 2 &&
      /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formMiembro.email) &&
      formMiembro.password.length >= 8
    );
  }, [formMiembro]);

  const actualizarCampoProyecto = (event) => {
    const { name, value } = event.target;

    setFormProyecto((actual) => ({
      ...actual,
      [name]: value,
    }));
  };

  const actualizarCampoMiembro = (event) => {
    const { name, value } = event.target;

    setFormMiembro((actual) => ({
      ...actual,
      [name]: value,
    }));
  };

  const actualizarCampoTarea = (event) => {
    const { name, value } = event.target;

    setFormTarea((actual) => ({
      ...actual,
      [name]: value,
    }));
  };

  const abrirDetalleProyecto = async (idProyecto) => {
    setProyectoSeleccionadoId(idProyecto);
    setDetalleProyecto(null);
    setVistaDetalle("resumen");
    setTabActiva("detalle-proyecto");
    await cargarDetalleProyecto(idProyecto);
  };

  const volverAProyectos = () => {
    setTabActiva("proyectos");
    setProyectoSeleccionadoId(null);
    setDetalleProyecto(null);
    setIdMiembroAsignar("");
    setFormTarea(tareaInicial);
    setVistaDetalle("resumen");
  };

  const cargarDetalleProyecto = async (idProyecto = proyectoSeleccionadoId) => {
    if (!idProyecto) {
      return;
    }

    setCargandoDetalle(true);
    setError("");

    try {
      const [detalleResponse, miembrosResponse] = await Promise.all([
        bffApi.get(`/proyectos/${idProyecto}/detalle`),
        bffApi.get("/miembros"),
      ]);

      setDetalleProyecto(detalleResponse.data);
      setMiembros(miembrosResponse.data);
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo cargar el detalle del proyecto."));
    } finally {
      setCargandoDetalle(false);
    }
  };

  const crearProyecto = async (event) => {
    event.preventDefault();
    setMensaje("");
    setError("");

    if (!proyectoValido) {
      setError("Completa los datos obligatorios del proyecto antes de guardar.");
      return;
    }

    setGuardando(true);

    try {
      await bffApi.post("/proyectos", normalizarFechas(formProyecto));

      setFormProyecto(proyectoInicial);
      setMensaje("Proyecto creado correctamente.");
      await cargarVistaAdmin();
      setTabActiva("proyectos");
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo crear el proyecto."));
    } finally {
      setGuardando(false);
    }
  };

  const crearMiembro = async (event) => {
    event.preventDefault();
    setMensaje("");
    setError("");

    if (!miembroValido) {
      setError("Completa los datos obligatorios del miembro antes de guardar.");
      return;
    }

    setGuardando(true);

    try {
      await bffApi.post("/miembros", formMiembro);

      setFormMiembro(miembroInicial);
      setMensaje("Miembro registrado correctamente.");
      await cargarVistaAdmin();
      setTabActiva("equipo");
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo registrar el miembro."));
    } finally {
      setGuardando(false);
    }
  };

  const crearTarea = async (event) => {
    event.preventDefault();
    setMensaje("");
    setError("");

    if (!proyectoSeleccionadoId) {
      setError("Selecciona un proyecto antes de crear tareas.");
      return;
    }

    if (formTarea.descripcion.trim().length < 5 || formTarea.responsable.trim().length < 2) {
      setError("Completa la descripción y el responsable de la tarea.");
      return;
    }

    if (!fechasProyectoValidas(formTarea)) {
      setError("La fecha fin estimada de la tarea debe ser igual o posterior a la fecha de inicio.");
      return;
    }

    setGuardando(true);

    try {
      await bffApi.post("/tareas", {
        ...normalizarFechas(formTarea),
        idProyecto: Number(proyectoSeleccionadoId),
      });

      setFormTarea(tareaInicial);
      setMensaje("Tarea creada correctamente.");
      await cargarDetalleProyecto(proyectoSeleccionadoId);
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo crear la tarea."));
    } finally {
      setGuardando(false);
    }
  };

  const asignarMiembro = async (event) => {
    event.preventDefault();
    setMensaje("");
    setError("");

    if (!proyectoSeleccionadoId || !idMiembroAsignar) {
      setError("Selecciona un miembro para asignarlo al proyecto.");
      return;
    }

    setGuardando(true);

    try {
      await bffApi.post("/asignaciones", {
        idProyecto: Number(proyectoSeleccionadoId),
        idMiembro: Number(idMiembroAsignar),
      });

      setIdMiembroAsignar("");
      setMensaje("Miembro asignado correctamente.");
      await cargarDetalleProyecto(proyectoSeleccionadoId);
      await cargarVistaAdmin();
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo asignar el miembro."));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarEstadoTarea = async (idTarea, estado) => {
    setMensaje("");
    setError("");

    try {
      await bffApi.patch(`/tareas/${idTarea}/estado`, { estado });
      setMensaje("Estado de tarea actualizado.");
      await cargarDetalleProyecto(proyectoSeleccionadoId);
      await cargarVistaAdmin();
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo cambiar el estado de la tarea."));
    }
  };

  return (
    <main className="admin-layout admin-layout-pro">
      <aside className="admin-sidebar">
        <div className="brand-block">
          <span className="brand-mark">IT</span>

          <div>
            <strong>Innovatech</strong>
            <small>Panel administrador</small>
          </div>
        </div>

        <nav className="admin-nav" aria-label="Navegación de administración">
          <SidebarButton active={tabActiva === "resumen"} onClick={() => setTabActiva("resumen")} label="Resumen" icon="📊" />
          <SidebarButton active={tabActiva === "proyectos"} onClick={() => setTabActiva("proyectos")} label="Proyectos" icon="📁" />
          <SidebarButton active={tabActiva === "crear-proyecto"} onClick={() => setTabActiva("crear-proyecto")} label="Crear proyecto" icon="➕" />
          <SidebarButton active={tabActiva === "equipo"} onClick={() => setTabActiva("equipo")} label="Usuarios" icon="👥" />
          <SidebarButton active={tabActiva === "crear-miembro"} onClick={() => setTabActiva("crear-miembro")} label="Registrar miembro" icon="🧩" />
          {detalleProyecto && (
            <SidebarButton active={tabActiva === "detalle-proyecto"} onClick={() => setTabActiva("detalle-proyecto")} label="Proyecto activo" icon="🛠️" />
          )}
        </nav>
      </aside>

      <section className="admin-content admin-content-pro">
        <header className="admin-header hero-admin">
          <div>
            <p className="eyebrow">Administración general</p>
            <h1>Panel de control</h1>
            <p>Monitoreo de proyectos, avance operativo, tareas y equipo de Innovatech.</p>
          </div>

          <div className="header-actions">
            <button className="button ghost" type="button" onClick={() => setTabActiva("crear-proyecto")}>
              Nuevo proyecto
            </button>
            <button className="button secondary" type="button" onClick={cargarVistaAdmin} disabled={cargando}>
              {cargando ? "Actualizando..." : "Actualizar"}
            </button>
          </div>
        </header>

        {mensaje && <p className="success alert-message">{mensaje}</p>}
        {error && <p className="error alert-message">{error}</p>}

        {cargando ? (
          <section className="panel admin-panel loading-card">
            <p>Cargando información del administrador...</p>
          </section>
        ) : (
          <>
            {tabActiva === "resumen" && (
              <section className="admin-section">
                <div className="metrics-grid admin-metrics metrics-pro">
                  <Metric title="Total proyectos" value={dashboard?.totalProyectos ?? proyectos.length} helper="Proyectos registrados" />
                  <Metric title="En progreso" value={dashboard?.proyectosEnProgreso ?? contarPorEstado(proyectos, "IN_PROGRESS")} helper="Activos actualmente" variant="warning" />
                  <Metric title="Completados" value={dashboard?.proyectosCompletados ?? contarPorEstado(proyectos, "COMPLETED")} helper="Finalizados" variant="success" />
                  <Metric title="Equipo asignado" value={dashboard?.totalMiembrosAsignados ?? miembros.length} helper="Miembros vinculados" variant="info" />
                  <Metric title="Total tareas" value={dashboard?.totalTareas ?? 0} helper="Tareas registradas" />
                  <Metric title="Terminadas" value={dashboard?.tareasTerminadas ?? 0} helper="Tareas cerradas" variant="success" />
                </div>

                <div className="admin-dashboard-grid">
                  <section className="panel admin-panel progress-panel">
                    <div className="panel-heading">
                      <div>
                        <h2>Avance general</h2>
                        <p>Progreso consolidado calculado desde el BFF.</p>
                      </div>
                      <strong className="progress-number">{formatearPorcentaje(dashboard?.porcentajeAvanceGeneral ?? 0)}</strong>
                    </div>

                    <div className="progress progress-xl" aria-label="Avance general">
                      <div className="progress-bar" style={{ width: `${limitarPorcentaje(dashboard?.porcentajeAvanceGeneral ?? 0)}%` }} />
                    </div>

                    <div className="task-summary-row">
                      <TaskResume label="Pendientes" value={dashboard?.tareasPendientes ?? 0} />
                      <TaskResume label="En progreso" value={dashboard?.tareasEnProgreso ?? 0} />
                      <TaskResume label="Terminadas" value={dashboard?.tareasTerminadas ?? 0} />
                    </div>
                  </section>

                  <section className="panel admin-panel status-panel">
                    <div className="panel-heading">
                      <div>
                        <h2>Proyectos por estado</h2>
                        <p>Distribución del portafolio actual.</p>
                      </div>
                    </div>

                    <div className="status-breakdown">
                      {resumenEstados.map((item) => (
                        <div className="status-row" key={item.estado}>
                          <div>
                            <span className={`status-dot dot-${item.estado}`} />
                            <strong>{item.label}</strong>
                          </div>
                          <small>{item.total} proyectos</small>
                          <div className="status-meter">
                            <span style={{ width: `${item.porcentaje}%` }} />
                          </div>
                        </div>
                      ))}
                    </div>
                  </section>

                  <section className="panel admin-panel actions-panel">
                    <h2>Acciones rápidas</h2>
                    <div className="quick-actions">
                      <button type="button" onClick={() => setTabActiva("crear-proyecto")}>Crear nuevo proyecto</button>
                      <button type="button" onClick={() => setTabActiva("crear-miembro")}>Registrar miembro</button>
                      <button type="button" onClick={() => setTabActiva("proyectos")}>Revisar proyectos</button>
                      <button type="button" onClick={() => setTabActiva("equipo")}>Ver usuarios</button>
                    </div>
                  </section>

                  <section className="panel admin-panel attention-panel">
                    <h2>Atención rápida</h2>
                    {proyectosPorRevisar.length === 0 ? (
                      <p className="muted">No hay proyectos planificados o cancelados para revisar.</p>
                    ) : (
                      <div className="mini-list mini-list-pro">
                        {proyectosPorRevisar.map((proyecto) => (
                          <button key={proyecto.id} type="button" onClick={() => abrirDetalleProyecto(proyecto.id)}>
                            <span>{proyecto.nombre}</span>
                            <small className={`status status-${proyecto.estado}`}>{formatearEstadoProyecto(proyecto.estado)}</small>
                          </button>
                        ))}
                      </div>
                    )}
                  </section>
                </div>
              </section>
            )}

            {tabActiva === "proyectos" && (
              <section className="admin-section">
                <div className="section-toolbar section-toolbar-pro">
                  <div>
                    <p className="eyebrow">Gestión del portafolio</p>
                    <h2>Proyectos</h2>
                    <p>Filtra, busca y administra los proyectos registrados.</p>
                  </div>

                  <button className="button" type="button" onClick={() => setTabActiva("crear-proyecto")}>Nuevo proyecto</button>
                </div>

                <div className="filters-panel">
                  <label className="search-field">
                    Buscar proyecto
                    <input placeholder="Nombre, descripción o estado..." value={busqueda} onChange={(event) => setBusqueda(event.target.value)} />
                  </label>

                  <div className="filter-pills" aria-label="Filtro por estado">
                    {filtrosEstado.map((filtro) => (
                      <button
                        className={filtroEstado === filtro.value ? "filter-pill active" : "filter-pill"}
                        key={filtro.value}
                        type="button"
                        onClick={() => setFiltroEstado(filtro.value)}
                      >
                        {filtro.label}
                      </button>
                    ))}
                  </div>
                </div>

                {proyectosFiltrados.length === 0 ? (
                  <section className="panel admin-panel empty-state">
                    <h3>No se encontraron proyectos</h3>
                    <p>Prueba con otra búsqueda o cambia el filtro de estado.</p>
                  </section>
                ) : (
                  <div className="admin-project-grid project-grid-pro">
                    {proyectosFiltrados.map((proyecto) => (
                      <article className="project-admin-card project-card-pro" key={proyecto.id}>
                        <div className="card-topline card-topline-pro">
                          <span className={`status status-${proyecto.estado}`}>{formatearEstadoProyecto(proyecto.estado)}</span>
                          <small>{obtenerIndicadorFecha(proyecto)}</small>
                        </div>

                        <h3>{proyecto.nombre}</h3>
                        <p>{proyecto.descripcion || "Proyecto sin descripción registrada."}</p>

                        <div className="date-row date-row-pro">
                          <div>
                            <small>Inicio</small>
                            <strong>{formatearFecha(proyecto.fechaInicio)}</strong>
                          </div>
                          <div>
                            <small>Fin estimado</small>
                            <strong>{formatearFecha(proyecto.fechaFinEstimada)}</strong>
                          </div>
                        </div>

                        <button className="button full-button" type="button" onClick={() => abrirDetalleProyecto(proyecto.id)}>Administrar proyecto</button>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            )}


            {tabActiva === "detalle-proyecto" && (
              <section className="admin-section project-workspace">
                <div className="workspace-breadcrumb">
                  <button type="button" onClick={volverAProyectos}>← Volver al listado</button>
                  <span>Dashboard admin / Administración de proyecto</span>
                </div>

                {cargandoDetalle ? (
                  <section className="panel admin-panel loading-card">
                    <p>Cargando administración del proyecto...</p>
                  </section>
                ) : !detalleProyecto ? (
                  <section className="panel admin-panel empty-state">
                    <h3>No hay proyecto seleccionado</h3>
                    <p>Vuelve al listado y selecciona un proyecto para administrarlo.</p>
                    <button className="button" type="button" onClick={() => setTabActiva("proyectos")}>Ver proyectos</button>
                  </section>
                ) : (
                  <>
                    <header className="project-workspace-hero">
                      <div>
                        <p className="eyebrow">Proyecto activo</p>
                        <h2>{detalleProyecto.proyecto.nombre}</h2>
                        <p>{detalleProyecto.proyecto.descripcion || "Proyecto sin descripción registrada."}</p>
                      </div>

                      <div className="workspace-hero-side">
                        <span className={`status status-${detalleProyecto.proyecto.estado}`}>
                          {formatearEstadoProyecto(detalleProyecto.proyecto.estado)}
                        </span>
                        <button className="button secondary" type="button" onClick={() => cargarDetalleProyecto()} disabled={cargandoDetalle}>
                          Actualizar proyecto
                        </button>
                      </div>
                    </header>

                    <div className="workspace-tabs" aria-label="Secciones del proyecto">
                      <button className={vistaDetalle === "resumen" ? "active" : ""} type="button" onClick={() => setVistaDetalle("resumen")}>Resumen</button>
                      <button className={vistaDetalle === "tareas" ? "active" : ""} type="button" onClick={() => setVistaDetalle("tareas")}>Tareas</button>
                      <button className={vistaDetalle === "equipo" ? "active" : ""} type="button" onClick={() => setVistaDetalle("equipo")}>Equipo asignado</button>
                    </div>

                    {vistaDetalle === "resumen" && (
                      <div className="workspace-grid">
                        <section className="panel admin-panel progress-panel workspace-main-card">
                          <div className="panel-heading">
                            <div>
                              <h2>Avance del proyecto</h2>
                              <p>Seguimiento específico sin salir del dashboard administrador.</p>
                            </div>
                            <strong className="progress-number">{formatearPorcentaje(detalleProyecto.avance?.porcentajeAvance ?? 0)}</strong>
                          </div>

                          <div className="progress progress-xl" aria-label="Avance del proyecto">
                            <div className="progress-bar" style={{ width: `${limitarPorcentaje(detalleProyecto.avance?.porcentajeAvance ?? 0)}%` }} />
                          </div>

                          <div className="metrics-grid compact workspace-metrics">
                            <Metric title="Total tareas" value={detalleProyecto.avance?.totalTareas ?? 0} helper="Registradas" />
                            <Metric title="Pendientes" value={detalleProyecto.avance?.tareasPendientes ?? 0} helper="Por iniciar" variant="warning" />
                            <Metric title="En progreso" value={detalleProyecto.avance?.tareasEnProgreso ?? 0} helper="Activas" variant="info" />
                            <Metric title="Completadas" value={detalleProyecto.avance?.tareasTerminadas ?? calcularTareasTerminadas(detalleProyecto.tareas)} helper="Cerradas" variant="success" />
                          </div>
                        </section>

                        <section className="panel admin-panel workspace-side-card">
                          <h2>Información del proyecto</h2>
                          <dl className="detail-list">
                            <div>
                              <dt>Inicio</dt>
                              <dd>{formatearFecha(detalleProyecto.proyecto.fechaInicio)}</dd>
                            </div>
                            <div>
                              <dt>Fin estimado</dt>
                              <dd>{formatearFecha(detalleProyecto.proyecto.fechaFinEstimada)}</dd>
                            </div>
                            <div>
                              <dt>Estado de plazo</dt>
                              <dd>{obtenerIndicadorFecha(detalleProyecto.proyecto)}</dd>
                            </div>
                            <div>
                              <dt>Miembros asignados</dt>
                              <dd>{detalleProyecto.miembrosAsignados?.length ?? 0}</dd>
                            </div>
                          </dl>
                        </section>

                        <section className="panel admin-panel workspace-side-card">
                          <h2>Acciones del proyecto</h2>
                          <div className="quick-actions compact-actions">
                            <button type="button" onClick={() => setVistaDetalle("tareas")}>Crear o revisar tareas</button>
                            <button type="button" onClick={() => setVistaDetalle("equipo")}>Asignar equipo</button>
                            <button type="button" onClick={() => setTabActiva("crear-miembro")}>Registrar nuevo miembro</button>
                          </div>
                        </section>
                      </div>
                    )}

                    {vistaDetalle === "tareas" && (
                      <div className="workspace-grid workspace-grid-wide">
                        <section className="panel admin-panel form-panel-pro">
                          <div className="form-header-pro">
                            <div>
                              <p className="eyebrow">Nueva tarea</p>
                              <h2>Crear tarea para este proyecto</h2>
                              <p>La tarea quedará asociada automáticamente a {detalleProyecto.proyecto.nombre}.</p>
                            </div>
                          </div>

                          <form className="form-grid form-grid-pro" onSubmit={crearTarea} noValidate>
                            <FormField label="Descripción" help="Describe el trabajo a realizar." className="span-2">
                              <textarea name="descripcion" value={formTarea.descripcion} onChange={actualizarCampoTarea} maxLength="500" rows="4" required />
                            </FormField>

                            <FormField label="Responsable" help="Nombre del responsable visible para el equipo.">
                              <input name="responsable" value={formTarea.responsable} onChange={actualizarCampoTarea} maxLength="120" required />
                            </FormField>

                            <FormField label="Estado" help="Estado inicial de la tarea.">
                              <select name="estado" value={formTarea.estado} onChange={actualizarCampoTarea}>
                                <option value="PENDING">Pendiente</option>
                                <option value="IN_PROGRESS">En progreso</option>
                                <option value="DONE">Terminada</option>
                              </select>
                            </FormField>

                            <FormField label="Fecha de inicio" help="Opcional.">
                              <input type="date" name="fechaInicio" value={formTarea.fechaInicio} onChange={actualizarCampoTarea} />
                            </FormField>

                            <FormField label="Fin estimado" help="Opcional, pero debe ser posterior al inicio.">
                              <input type="date" name="fechaFinEstimada" value={formTarea.fechaFinEstimada} onChange={actualizarCampoTarea} />
                            </FormField>

                            <div className="form-actions span-2 form-actions-pro">
                              <button className="button secondary" type="button" onClick={() => setFormTarea(tareaInicial)} disabled={guardando}>Limpiar</button>
                              <button className="button" type="submit" disabled={guardando}>{guardando ? "Guardando..." : "Crear tarea"}</button>
                            </div>
                          </form>
                        </section>

                        <section className="admin-section no-margin">
                          <div className="section-toolbar section-toolbar-pro compact-toolbar">
                            <div>
                              <p className="eyebrow">Backlog operativo</p>
                              <h2>Tareas del proyecto</h2>
                            </div>
                          </div>

                          {detalleProyecto.tareas?.length === 0 ? (
                            <section className="panel admin-panel empty-state">
                              <h3>No hay tareas registradas</h3>
                              <p>Crea la primera tarea para comenzar a medir avance.</p>
                            </section>
                          ) : (
                            <div className="task-board">
                              {detalleProyecto.tareas?.map((tarea) => (
                                <article className="task-card" key={tarea.id}>
                                  <div className="task-card-head">
                                    <span className={`status status-${tarea.estado}`}>{formatearEstadoTarea(tarea.estado)}</span>
                                    <small>{obtenerIndicadorFecha(tarea)}</small>
                                  </div>
                                  <h3>{tarea.descripcion}</h3>
                                  <p><strong>Responsable:</strong> {tarea.responsable || "Sin responsable"}</p>
                                  <div className="date-row date-row-pro">
                                    <div>
                                      <small>Inicio</small>
                                      <strong>{formatearFecha(tarea.fechaInicio)}</strong>
                                    </div>
                                    <div>
                                      <small>Fin estimado</small>
                                      <strong>{formatearFecha(tarea.fechaFinEstimada)}</strong>
                                    </div>
                                  </div>
                                  <div className="inline-actions task-actions">
                                    <button type="button" onClick={() => cambiarEstadoTarea(tarea.id, "IN_PROGRESS")} disabled={tarea.estado !== "PENDING"}>Iniciar</button>
                                    <button type="button" onClick={() => cambiarEstadoTarea(tarea.id, "DONE")} disabled={tarea.estado === "DONE"}>Finalizar</button>
                                  </div>
                                </article>
                              ))}
                            </div>
                          )}
                        </section>
                      </div>
                    )}

                    {vistaDetalle === "equipo" && (
                      <div className="workspace-grid workspace-grid-wide">
                        <section className="panel admin-panel form-panel-pro">
                          <div className="form-header-pro">
                            <div>
                              <p className="eyebrow">Asignación</p>
                              <h2>Asignar miembro al proyecto</h2>
                              <p>Selecciona un miembro existente para incorporarlo al proyecto activo.</p>
                            </div>
                          </div>

                          <form className="form-grid form-grid-pro" onSubmit={asignarMiembro} noValidate>
                            <FormField label="Miembro" help="Debe estar registrado previamente en el equipo.">
                              <select value={idMiembroAsignar} onChange={(event) => setIdMiembroAsignar(event.target.value)} required>
                                <option value="">Seleccionar miembro</option>
                                {miembros.map((miembro) => (
                                  <option key={miembro.id} value={miembro.id}>
                                    {obtenerNombreCompletoMiembro(miembro)} - {formatearRol(miembro.rol)}
                                  </option>
                                ))}
                              </select>
                            </FormField>

                            <div className="form-actions form-actions-pro">
                              <button className="button" type="submit" disabled={guardando || !idMiembroAsignar}>{guardando ? "Asignando..." : "Asignar miembro"}</button>
                            </div>
                          </form>
                        </section>

                        <section className="admin-section no-margin">
                          <div className="section-toolbar section-toolbar-pro compact-toolbar">
                            <div>
                              <p className="eyebrow">Equipo del proyecto</p>
                              <h2>Miembros asignados</h2>
                            </div>
                            <button className="button secondary" type="button" onClick={() => setTabActiva("crear-miembro")}>Registrar miembro</button>
                          </div>

                          {detalleProyecto.miembrosAsignados?.length === 0 ? (
                            <section className="panel admin-panel empty-state">
                              <h3>No hay miembros asignados</h3>
                              <p>Asigna un integrante para comenzar a distribuir responsabilidades.</p>
                            </section>
                          ) : (
                            <div className="assigned-team-grid">
                              {detalleProyecto.miembrosAsignados?.map((miembro) => (
                                <article className="team-card team-card-pro compact-team-card" key={miembro.id}>
                                  <div className="avatar avatar-pro">{obtenerInicialesAsignacion(miembro)}</div>
                                  <div className="team-card-body">
                                    <div className="team-card-heading">
                                      <h3>{miembro.nombreMiembro}</h3>
                                    </div>
                                    <span className="role-pill role-pill-pro">{formatearRol(miembro.rolMiembro)}</span>
                                  </div>
                                </article>
                              ))}
                            </div>
                          )}
                        </section>
                      </div>
                    )}
                  </>
                )}
              </section>
            )}

            {tabActiva === "crear-proyecto" && (
              <section className="panel admin-panel form-panel-pro">
                <div className="form-header-pro">
                  <div>
                    <p className="eyebrow">Nuevo registro</p>
                    <h2>Crear proyecto</h2>
                    <p>Ingresa la información base para dejar el proyecto disponible en la plataforma.</p>
                  </div>
                </div>

                <form className="form-grid form-grid-pro" onSubmit={crearProyecto} noValidate>
                  <FormField label="Nombre" help="Mínimo 3 caracteres. Ejemplo: Dashboard Ejecutivo BI.">
                    <input name="nombre" value={formProyecto.nombre} onChange={actualizarCampoProyecto} maxLength="120" required />
                  </FormField>

                  <FormField label="Estado" help="Define el estado inicial del proyecto.">
                    <select name="estado" value={formProyecto.estado} onChange={actualizarCampoProyecto} required>
                      <option value="PLANNED">Planificado</option>
                      <option value="IN_PROGRESS">En progreso</option>
                      <option value="COMPLETED">Completado</option>
                      <option value="CANCELLED">Cancelado</option>
                    </select>
                  </FormField>

                  <FormField label="Fecha de inicio" help="Puede quedar vacía si aún no está definida.">
                    <input type="date" name="fechaInicio" value={formProyecto.fechaInicio} onChange={actualizarCampoProyecto} />
                  </FormField>

                  <FormField label="Fin estimado" help="Debe ser igual o posterior a la fecha de inicio.">
                    <input type="date" name="fechaFinEstimada" value={formProyecto.fechaFinEstimada} onChange={actualizarCampoProyecto} />
                  </FormField>

                  <FormField label="Descripción" help="Mínimo 10 caracteres. Resume objetivo, alcance o necesidad del proyecto." className="span-2">
                    <textarea name="descripcion" value={formProyecto.descripcion} onChange={actualizarCampoProyecto} maxLength="500" rows="5" required />
                  </FormField>

                  <div className="form-actions span-2 form-actions-pro">
                    <button className="button secondary" type="button" onClick={() => setFormProyecto(proyectoInicial)} disabled={guardando}>Limpiar</button>
                    <button className="button" type="submit" disabled={guardando || !proyectoValido}>{guardando ? "Guardando..." : "Crear proyecto"}</button>
                  </div>
                </form>
              </section>
            )}

            {tabActiva === "equipo" && (
              <section className="admin-section">
                <div className="section-toolbar section-toolbar-pro">
                  <div>
                    <p className="eyebrow">Administración de acceso</p>
                    <h2>Usuarios</h2>
                    <p>Consulta perfiles, roles y estado operativo de los miembros registrados.</p>
                  </div>

                  <button className="button" type="button" onClick={() => setTabActiva("crear-miembro")}>Nuevo usuario</button>
                </div>

                <div className="metrics-grid admin-metrics metrics-pro users-metrics">
                  <Metric title="Usuarios" value={resumenUsuarios.total} helper="Cuentas registradas" />
                  <Metric title="Activos" value={resumenUsuarios.activos} helper="Disponibles para proyectos" variant="success" />
                  <Metric title="Administradores" value={resumenUsuarios.administradores} helper="Con permisos de gestión" variant="warning" />
                  <Metric title="Roles cubiertos" value={resumenUsuarios.roles} helper="Especialidades distintas" variant="info" />
                </div>

                <div className="filters-panel users-filters-panel">
                  <label className="search-field">
                    Buscar usuario
                    <input placeholder="Nombre, correo, rol o estado..." value={busquedaUsuarios} onChange={(event) => setBusquedaUsuarios(event.target.value)} />
                  </label>

                  <label className="compact-select-field">
                    Rol
                    <select value={filtroRolUsuario} onChange={(event) => setFiltroRolUsuario(event.target.value)}>
                      {filtrosRolUsuario.map((filtro) => (
                        <option key={filtro.value} value={filtro.value}>{filtro.label}</option>
                      ))}
                    </select>
                  </label>

                  <div className="filter-pills user-state-pills" aria-label="Filtro por estado de usuario">
                    {filtrosEstadoUsuario.map((filtro) => (
                      <button
                        className={filtroEstadoUsuario === filtro.value ? "filter-pill active" : "filter-pill"}
                        key={filtro.value}
                        type="button"
                        onClick={() => setFiltroEstadoUsuario(filtro.value)}
                      >
                        {filtro.label}
                      </button>
                    ))}
                  </div>
                </div>

                {miembros.length === 0 ? (
                  <section className="panel admin-panel empty-state">
                    <h3>No hay usuarios registrados</h3>
                    <p>Registra el primer usuario para comenzar a asignarlo a proyectos.</p>
                  </section>
                ) : usuariosFiltrados.length === 0 ? (
                  <section className="panel admin-panel empty-state">
                    <h3>No se encontraron usuarios</h3>
                    <p>Prueba con otra búsqueda o ajusta los filtros aplicados.</p>
                  </section>
                ) : (
                  <div className="users-grid">
                    {usuariosFiltrados.map((miembro) => (
                      <article className="user-card" key={miembro.id}>
                        <div className="user-card-main">
                          <div className="avatar avatar-pro user-avatar">{obtenerIniciales(miembro)}</div>

                          <div className="team-card-body">
                            <div className="team-card-heading">
                              <h3>{obtenerNombreCompletoMiembro(miembro)}</h3>
                              {miembro.estado && <span className={`member-status member-status-${miembro.estado}`}>{formatearEstadoMiembro(miembro.estado)}</span>}
                            </div>

                            <p className="user-email">{miembro.email}</p>
                          </div>
                        </div>

                        <div className="user-card-meta">
                          <div>
                            <small>Rol</small>
                            <strong>{formatearRol(miembro.rol)}</strong>
                          </div>

                          <div>
                            <small>ID usuario</small>
                            <strong>#{miembro.id}</strong>
                          </div>
                        </div>

                        <div className="user-card-footer">
                          <span className="role-pill role-pill-pro">{formatearRol(miembro.rol)}</span>
                          <button className="button ghost user-card-action" type="button" onClick={() => setTabActiva("crear-miembro")}>
                            Crear similar
                          </button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            )}

            {tabActiva === "crear-miembro" && (
              <section className="panel admin-panel form-panel-pro">
                <div className="form-header-pro">
                  <div>
                    <p className="eyebrow">Nuevo integrante</p>
                    <h2>Registrar miembro</h2>
                    <p>Crea una cuenta para incorporar al miembro al flujo de proyectos.</p>
                  </div>
                </div>

                <form className="form-grid form-grid-pro" onSubmit={crearMiembro} noValidate>
                  <FormField label="Nombres" help="Ingresa el o los nombres del miembro.">
                    <input name="nombres" value={formMiembro.nombres} onChange={actualizarCampoMiembro} required />
                  </FormField>

                  <FormField label="Apellido paterno" help="Dato obligatorio para identificar al miembro.">
                    <input name="apellidoPaterno" value={formMiembro.apellidoPaterno} onChange={actualizarCampoMiembro} required />
                  </FormField>

                  <FormField label="Apellido materno" help="Dato obligatorio para completar el registro.">
                    <input name="apellidoMaterno" value={formMiembro.apellidoMaterno} onChange={actualizarCampoMiembro} required />
                  </FormField>

                  <FormField label="Email" help="Debe tener formato válido. Ejemplo: nombre@innovatech.cl.">
                    <input type="email" name="email" value={formMiembro.email} onChange={actualizarCampoMiembro} required />
                  </FormField>

                  <FormField label="Contraseña" help="Mínimo 8 caracteres.">
                    <input type="password" name="password" value={formMiembro.password} onChange={actualizarCampoMiembro} minLength="8" required />
                  </FormField>

                  <FormField label="Rol" help="Define los permisos y responsabilidades del miembro.">
                    <select name="rol" value={formMiembro.rol} onChange={actualizarCampoMiembro}>
                      <option value="ADMIN">Administrador</option>
                      <option value="PROJECT_MANAGER">Project Manager</option>
                      <option value="SCRUM_MASTER">Scrum Master</option>
                      <option value="DEVELOPER">Developer</option>
                      <option value="QA">QA</option>
                      <option value="DEVOPS">DevOps</option>
                      <option value="UI_UX">UI/UX</option>
                    </select>
                  </FormField>

                  <div className="form-actions span-2 form-actions-pro">
                    <button className="button secondary" type="button" onClick={() => setFormMiembro(miembroInicial)} disabled={guardando}>Limpiar</button>
                    <button className="button" type="submit" disabled={guardando || !miembroValido}>{guardando ? "Guardando..." : "Registrar miembro"}</button>
                  </div>
                </form>
              </section>
            )}
          </>
        )}
      </section>
    </main>
  );
}

function SidebarButton({ active, onClick, label, icon }) {
  return (
    <button className={active ? "side-link active" : "side-link"} onClick={onClick} type="button">
      <span>{icon}</span>
      {label}
    </button>
  );
}

function Metric({ title, value, helper, variant = "default" }) {
  return (
    <div className={`metric admin-metric metric-${variant}`}>
      <span>{title}</span>
      <strong>{value}</strong>
      {helper && <small>{helper}</small>}
    </div>
  );
}

function TaskResume({ label, value }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function FormField({ label, help, className = "", children }) {
  return (
    <label className={`form-field-pro ${className}`}>
      <span>{label}</span>
      {children}
      {help && <small>{help}</small>}
    </label>
  );
}

function contarPorEstado(proyectos, estado) {
  return proyectos.filter((proyecto) => proyecto.estado === estado).length;
}

function normalizarFechas(payload) {
  return {
    ...payload,
    fechaInicio: payload.fechaInicio || null,
    fechaFinEstimada: payload.fechaFinEstimada || null,
  };
}

function obtenerMensajeError(error, fallback) {
  return error.response?.data?.mensaje || error.response?.data?.error || fallback;
}

function formatearEstadoProyecto(estado) {
  const estados = {
    PLANNED: "Planificado",
    IN_PROGRESS: "En progreso",
    COMPLETED: "Completado",
    CANCELLED: "Cancelado",
  };

  return estados[estado] || estado || "Sin estado";
}

function formatearEstadoTarea(estado) {
  const estados = {
    PENDING: "Pendiente",
    IN_PROGRESS: "En progreso",
    DONE: "Terminada",
  };

  return estados[estado] || estado || "Sin estado";
}

function calcularTareasTerminadas(tareas = []) {
  return tareas.filter((tarea) => tarea.estado === "DONE").length;
}

function formatearEstadoMiembro(estado) {
  const estados = {
    ACTIVE: "Activo",
    ACTIVO: "Activo",
    INACTIVE: "Inactivo",
    INACTIVO: "Inactivo",
    PENDING: "Pendiente",
    BLOQUEADO: "Bloqueado",
    BLOCKED: "Bloqueado",
  };

  return estados[estado] || estado;
}

function normalizarEstadoMiembro(estado) {
  const estados = {
    ACTIVO: "ACTIVE",
    ACTIVE: "ACTIVE",
    INACTIVO: "INACTIVE",
    INACTIVE: "INACTIVE",
    PENDIENTE: "PENDING",
    PENDING: "PENDING",
    BLOQUEADO: "BLOCKED",
    BLOCKED: "BLOCKED",
  };

  return estados[estado] || estado || "";
}

function formatearRol(rol) {
  const roles = {
    ADMIN: "Administrador",
    PROJECT_MANAGER: "Project Manager",
    SCRUM_MASTER: "Scrum Master",
    DEVELOPER: "Developer",
    QA: "QA",
    DEVOPS: "DevOps",
    UI_UX: "UI/UX",
  };

  return roles[rol] || rol || "Sin rol";
}

function formatearFecha(fecha) {
  if (!fecha) {
    return "Sin fecha";
  }

  return new Date(`${fecha}T00:00:00`).toLocaleDateString("es-CL");
}

function formatearPorcentaje(valor) {
  return `${Number(valor || 0).toFixed(0)}%`;
}

function limitarPorcentaje(valor) {
  return Math.min(100, Math.max(0, Number(valor || 0)));
}

function fechasProyectoValidas(formProyecto) {
  if (!formProyecto.fechaInicio || !formProyecto.fechaFinEstimada) {
    return true;
  }

  return formProyecto.fechaFinEstimada >= formProyecto.fechaInicio;
}

function obtenerIndicadorFecha(proyecto) {
  if (!proyecto.fechaFinEstimada) {
    return "Sin fecha límite";
  }

  if (proyecto.estado === "COMPLETED") {
    return "Finalizado";
  }

  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const fechaFin = new Date(`${proyecto.fechaFinEstimada}T00:00:00`);

  if (fechaFin < hoy && proyecto.estado !== "COMPLETED") {
    return "Vencido";
  }

  const diferenciaDias = Math.ceil((fechaFin - hoy) / (1000 * 60 * 60 * 24));

  if (diferenciaDias === 0) {
    return "Vence hoy";
  }

  if (diferenciaDias <= 7) {
    return `Vence en ${diferenciaDias} días`;
  }

  return "En plazo";
}

function obtenerIniciales(miembro) {
  const nombres = `${miembro.nombres || miembro.nombre || ""} ${
    miembro.apellidoPaterno || miembro.apellido || ""
  }`.trim();

  return (
    nombres
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0]?.toUpperCase())
      .join("") || "IT"
  );
}

function obtenerInicialesAsignacion(miembro) {
  const nombre = miembro.nombreMiembro || "";

  return (
    nombre
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0]?.toUpperCase())
      .join("") || "IT"
  );
}

function obtenerNombreCompletoMiembro(miembro) {
  return [
    miembro.nombres || miembro.nombre,
    miembro.apellidoPaterno || miembro.apellido,
    miembro.apellidoMaterno || miembro.apellido_materno,
  ]
    .filter(Boolean)
    .join(" ");
}

export default AdminDashboardPage;
