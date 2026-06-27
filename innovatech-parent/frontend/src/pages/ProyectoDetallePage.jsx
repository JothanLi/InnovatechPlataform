import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import bffApi from "../api/bffApi";

const tareaInicial = {
    descripcion: "",
    estado: "PENDING",
    responsable: "",
    fechaInicio: "",
    fechaFinEstimada: "",
};

const columnasTareas = [
    { estado: "PENDING", titulo: "Pendientes" },
    { estado: "IN_PROGRESS", titulo: "En progreso" },
    { estado: "DONE", titulo: "Terminadas" },
];

function ProyectoDetallePage() {
    const { idProyecto } = useParams();

    const rolesSesion = obtenerRolesSesion();

    const puedeGestionarProyecto = rolesSesion.some((rol) =>
        [
            "ADMIN",
            "ROLE_ADMIN",
            "PROJECT_MANAGER",
            "ROLE_PROJECT_MANAGER",
            "SCRUM_MASTER",
            "ROLE_SCRUM_MASTER",
        ].includes(rol)
    );

    const puedeActualizarTareas = rolesSesion.some((rol) =>
        [
            "ADMIN",
            "ROLE_ADMIN",
            "PROJECT_MANAGER",
            "ROLE_PROJECT_MANAGER",
            "SCRUM_MASTER",
            "ROLE_SCRUM_MASTER",
            "DEVELOPER",
            "ROLE_DEVELOPER",
            "QA",
            "ROLE_QA",
            "DEVOPS",
            "ROLE_DEVOPS",
            "UI_UX",
            "ROLE_UI_UX",
        ].includes(rol)
    );

    const [detalle, setDetalle] = useState(null);
    const [miembros, setMiembros] = useState([]);
    const [formTarea, setFormTarea] = useState(tareaInicial);
    const [idMiembroAsignar, setIdMiembroAsignar] = useState("");
    const [tabActiva, setTabActiva] = useState("resumen");
    const [error, setError] = useState("");
    const [mensaje, setMensaje] = useState("");
    const [guardando, setGuardando] = useState(false);
    const [eliminandoTareaId, setEliminandoTareaId] = useState(null);
    const [actualizandoEstadoProyecto, setActualizandoEstadoProyecto] = useState(false);
    const [mostrarOpcionesEstado, setMostrarOpcionesEstado] = useState(false);

    const cargarVista = useCallback(async () => {
        try {
            const detalleResponse = await bffApi.get(`/proyectos/${idProyecto}/detalle`);

            setDetalle(detalleResponse.data);

            if (puedeGestionarProyecto) {
                const miembrosResponse = await bffApi.get("/miembros");
                setMiembros(miembrosResponse.data);
            } else {
                setMiembros([]);
            }

            setError("");
        } catch (error) {
            setError(obtenerMensajeError(error, "No se pudo cargar el detalle del proyecto."));
        }
    }, [idProyecto, puedeGestionarProyecto]);

    useEffect(() => {
        Promise.resolve().then(cargarVista);
    }, [cargarVista]);

    const tareasPorEstado = useMemo(() => {
        return columnasTareas.map((columna) => ({
            ...columna,
            tareas: detalle?.tareas?.filter((tarea) => tarea.estado === columna.estado) || [],
        }));
    }, [detalle]);

    const miembrosAsignadosProyecto = useMemo(() => {
        return detalle?.miembrosAsignados || [];
    }, [detalle]);

    const proyectoNoPermiteNuevasTareas = useMemo(() => {
        return ["COMPLETED", "CANCELLED"].includes(detalle?.proyecto?.estado);
    }, [detalle]);

    const mensajeProyectoNoPermiteTareas = proyectoNoPermiteNuevasTareas
        ? "No se pueden asignar nuevas tareas a un proyecto finalizado o cancelado."
        : "";

    const miembrosDisponibles = useMemo(() => {
        const idsAsignados = new Set(
            detalle?.miembrosAsignados?.map((miembro) => Number(miembro.idMiembro || miembro.id)) || []
        );

        return miembros.filter((miembro) => !idsAsignados.has(Number(miembro.id)));
    }, [detalle, miembros]);

    const actualizarCampoTarea = (event) => {
        const { name, value } = event.target;
        setFormTarea((formActual) => ({ ...formActual, [name]: value }));
    };

    const crearTarea = async (event) => {
        event.preventDefault();
        setGuardando(true);
        setError("");
        setMensaje("");

        if (proyectoNoPermiteNuevasTareas) {
            setError(mensajeProyectoNoPermiteTareas);
            setGuardando(false);
            return;
        }

        if (!formTarea.responsable) {
            setError("Selecciona un responsable del equipo asignado al proyecto.");
            setGuardando(false);
            return;
        }

        try {
            await bffApi.post("/tareas", {
                ...normalizarFechas(formTarea),
                idProyecto: Number(idProyecto),
            });

            setFormTarea(tareaInicial);
            setMensaje("Tarea creada correctamente.");
            await cargarVista();
        } catch (error) {
            setError(obtenerMensajeError(error, "No se pudo crear la tarea."));
        } finally {
            setGuardando(false);
        }
    };

    const asignarMiembro = async (event) => {
        event.preventDefault();
        setGuardando(true);
        setError("");
        setMensaje("");

        try {
            await bffApi.post("/asignaciones", {
                idProyecto: Number(idProyecto),
                idMiembro: Number(idMiembroAsignar),
            });

            setIdMiembroAsignar("");
            setMensaje("Miembro asignado correctamente.");
            await cargarVista();
        } catch (error) {
            setError(obtenerMensajeError(error, "No se pudo asignar el miembro."));
        } finally {
            setGuardando(false);
        }
    };

    const cambiarEstadoTarea = async (idTarea, estado) => {
        setError("");
        setMensaje("");

        try {
            await bffApi.patch(`/tareas/${idTarea}/estado`, { estado });
            setMensaje("Estado de tarea actualizado.");
            await cargarVista();
        } catch (error) {
            setError(obtenerMensajeError(error, "No se pudo cambiar el estado de la tarea."));
        }
    };

    const eliminarTarea = async (tarea) => {
        setError("");
        setMensaje("");

        const descripcion = tarea?.descripcion || "esta tarea";
        const confirmar = window.confirm(`¿Seguro que deseas eliminar la tarea "${descripcion}"? Esta acción no se puede deshacer.`);

        if (!confirmar) {
            return;
        }

        setEliminandoTareaId(tarea.id);

        try {
            await bffApi.delete(`/tareas/${tarea.id}`);
            setMensaje("Tarea eliminada correctamente.");
            await cargarVista();
        } catch (error) {
            setError(obtenerMensajeError(error, "No se pudo eliminar la tarea."));
        } finally {
            setEliminandoTareaId(null);
        }
    };

    const cambiarEstadoProyecto = async (estado) => {
        setError("");
        setMensaje("");
        setActualizandoEstadoProyecto(true);

        try {
            await bffApi.patch(`/proyectos/${idProyecto}/estado`, { estado });
            setMensaje("Estado del proyecto actualizado correctamente.");
            setMostrarOpcionesEstado(false);
            await cargarVista();
        } catch (error) {
            setError(obtenerMensajeError(error, "No se pudo cambiar el estado del proyecto."));
        } finally {
            setActualizandoEstadoProyecto(false);
        }
    };

    if (error && !detalle) {
        return (
            <main className="work-shell">
                <Link className="back-link" to="/proyectos">Volver a proyectos</Link>
                <p className="error">{error}</p>
            </main>
        );
    }

    if (!detalle) {
        return (
            <main className="work-shell">
                <section className="panel loading-card">
                    <p>Cargando tablero del proyecto...</p>
                </section>
            </main>
        );
    }

    return (
        <main className="work-shell">
            <div className="workspace-breadcrumb">
                <Link className="back-link" to="/proyectos">Volver a proyectos</Link>
                <button type="button" onClick={cargarVista}>Actualizar</button>
            </div>

            {mensaje && <p className="success alert-message">{mensaje}</p>}
            {error && <p className="error alert-message">{error}</p>}

            <section className="project-work-hero">
                <div>
                    <p className="eyebrow">Tablero del proyecto</p>
                    <h1>{detalle.proyecto.nombre}</h1>
                    <p>{detalle.proyecto.descripcion}</p>
                </div>

                <div className="project-work-side">
          <span className={`status status-${detalle.proyecto.estado}`}>
            {formatearEstadoProyecto(detalle.proyecto.estado)}
          </span>

                    <strong>{formatearPorcentaje(detalle.avance.porcentajeAvance)}</strong>
                    <small>avance completado</small>

                    {puedeGestionarProyecto ? (
                        <div className="project-update-box">
                            <button
                                type="button"
                                className="project-update-button"
                                disabled={actualizandoEstadoProyecto}
                                onClick={() => setMostrarOpcionesEstado((valorActual) => !valorActual)}
                            >
                                {actualizandoEstadoProyecto ? "Actualizando..." : "Actualizar proyecto"}
                            </button>

                            {mostrarOpcionesEstado && (
                                <div className="project-update-menu">
                                    <button
                                        type="button"
                                        onClick={() => cambiarEstadoProyecto("PLANNED")}
                                        disabled={detalle.proyecto.estado === "PLANNED" || actualizandoEstadoProyecto}
                                    >
                                        Planificado
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => cambiarEstadoProyecto("IN_PROGRESS")}
                                        disabled={detalle.proyecto.estado === "IN_PROGRESS" || actualizandoEstadoProyecto}
                                    >
                                        En progreso
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => cambiarEstadoProyecto("COMPLETED")}
                                        disabled={detalle.proyecto.estado === "COMPLETED" || actualizandoEstadoProyecto}
                                    >
                                        Finalizado
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => cambiarEstadoProyecto("CANCELLED")}
                                        disabled={detalle.proyecto.estado === "CANCELLED" || actualizandoEstadoProyecto}
                                    >
                                        Cancelado
                                    </button>
                                </div>
                            )}
                        </div>
                    ) : (
                        <small className="muted">No tienes permisos para actualizar el estado.</small>
                    )}
                </div>
            </section>

            <nav className="workspace-tabs work-tabs" aria-label="Vistas del proyecto">
                <button
                    className={tabActiva === "resumen" ? "active" : ""}
                    type="button"
                    onClick={() => setTabActiva("resumen")}
                >
                    Resumen
                </button>

                <button
                    className={tabActiva === "tareas" ? "active" : ""}
                    type="button"
                    onClick={() => setTabActiva("tareas")}
                >
                    Tareas
                </button>

                <button
                    className={tabActiva === "equipo" ? "active" : ""}
                    type="button"
                    onClick={() => setTabActiva("equipo")}
                >
                    Equipo
                </button>
            </nav>

            {tabActiva === "resumen" && (
                <section className="work-detail-grid">
                    <article className="panel work-main-panel">
                        <div className="panel-heading">
                            <div>
                                <h2>Avance del proyecto</h2>
                                <p>Progreso calculado desde las tareas registradas.</p>
                            </div>
                            <strong className="progress-number">
                                {formatearPorcentaje(detalle.avance.porcentajeAvance)}
                            </strong>
                        </div>

                        <div className="progress progress-xl">
                            <div
                                className="progress-bar"
                                style={{ width: `${limitarPorcentaje(detalle.avance.porcentajeAvance)}%` }}
                            />
                        </div>

                        <div className="task-summary-row">
                            <TaskResume label="Total tareas" value={detalle.avance.totalTareas} />
                            <TaskResume label="Pendientes" value={detalle.avance.tareasPendientes} />
                            <TaskResume label="En progreso" value={detalle.avance.tareasEnProgreso} />
                        </div>
                    </article>

                    <article className="panel work-side-panel">
                        <h2>Fechas</h2>
                        <dl className="detail-list">
                            <div>
                                <dt>Inicio</dt>
                                <dd>{formatearFecha(detalle.proyecto.fechaInicio)}</dd>
                            </div>
                            <div>
                                <dt>Fin estimado</dt>
                                <dd>{formatearFecha(detalle.proyecto.fechaFinEstimada)}</dd>
                            </div>
                            <div>
                                <dt>Estado de agenda</dt>
                                <dd>{obtenerIndicadorFecha(detalle.proyecto)}</dd>
                            </div>
                        </dl>
                    </article>

                    <article className="panel work-side-panel">
                        <h2>Equipo asignado</h2>

                        {detalle.miembrosAsignados.length === 0 ? (
                            <p className="muted">Aún no hay miembros asignados.</p>
                        ) : (
                            <div className="mini-team-list">
                                {detalle.miembrosAsignados.slice(0, 4).map((miembro) => (
                                    <div key={miembro.id}>
                    <span className="avatar avatar-pro">
                      {obtenerInicialesAsignacion(miembro)}
                    </span>
                                        <div>
                                            <strong>{miembro.nombreMiembro}</strong>
                                            <small>{formatearRol(miembro.rolMiembro)}</small>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </article>
                </section>
            )}

            {tabActiva === "tareas" && (
                <section className={puedeGestionarProyecto ? "tasks-workspace" : "tasks-workspace tasks-workspace-full"}>
                    {puedeGestionarProyecto && (
                        <aside className="panel work-form-panel task-create-panel">
                            <h2>Nueva tarea</h2>
                            {proyectoNoPermiteNuevasTareas && (
                                <p className="form-warning">{mensajeProyectoNoPermiteTareas}</p>
                            )}

                            <form className="form-grid single" onSubmit={crearTarea}>
                                <FormField label="Descripción">
                  <textarea
                      name="descripcion"
                      value={formTarea.descripcion}
                      onChange={actualizarCampoTarea}
                      maxLength="500"
                      rows="4"
                      required
                      disabled={proyectoNoPermiteNuevasTareas}
                  />
                                </FormField>

                                <FormField label="Responsable">
                                    <select
                                        name="responsable"
                                        value={formTarea.responsable}
                                        onChange={actualizarCampoTarea}
                                        required
                                        disabled={proyectoNoPermiteNuevasTareas || miembrosAsignadosProyecto.length === 0}
                                    >
                                        <option value="">
                                            {miembrosAsignadosProyecto.length === 0
                                                ? "Asigna miembros al proyecto primero"
                                                : "Seleccionar responsable"}
                                        </option>
                                        {miembrosAsignadosProyecto.map((miembro) => (
                                            <option key={miembro.id} value={miembro.nombreMiembro}>
                                                {miembro.nombreMiembro} - {formatearRol(miembro.rolMiembro)}
                                            </option>
                                        ))}
                                    </select>
                                </FormField>

                                <FormField label="Estado">
                                    <select
                                        name="estado"
                                        value={formTarea.estado}
                                        onChange={actualizarCampoTarea}
                                        disabled={proyectoNoPermiteNuevasTareas}
                                    >
                                        <option value="PENDING">Pendiente</option>
                                        <option value="IN_PROGRESS">En progreso</option>
                                        <option value="DONE">Terminada</option>
                                    </select>
                                </FormField>

                                <FormField label="Inicio">
                                    <input
                                        type="date"
                                        name="fechaInicio"
                                        value={formTarea.fechaInicio}
                                        onChange={actualizarCampoTarea}
                                        disabled={proyectoNoPermiteNuevasTareas}
                                    />
                                </FormField>

                                <FormField label="Fin estimado">
                                    <input
                                        type="date"
                                        name="fechaFinEstimada"
                                        value={formTarea.fechaFinEstimada}
                                        onChange={actualizarCampoTarea}
                                        disabled={proyectoNoPermiteNuevasTareas}
                                    />
                                </FormField>

                                <button className="button" type="submit" disabled={guardando || proyectoNoPermiteNuevasTareas}>
                                    {guardando ? "Guardando..." : "Crear tarea"}
                                </button>
                            </form>
                        </aside>
                    )}

                    <div className="task-kanban">
                        {tareasPorEstado.map((columna) => (
                            <section className="kanban-column" key={columna.estado}>
                                <div className="kanban-column-head">
                                    <h2>{columna.titulo}</h2>
                                    <span>{columna.tareas.length}</span>
                                </div>

                                {columna.tareas.length === 0 ? (
                                    <p className="muted empty-column">Sin tareas.</p>
                                ) : (
                                    columna.tareas.map((tarea) => (
                                        <article className="task-card work-task-card" key={tarea.id}>
                                            <div className="task-card-head">
                        <span className={`status status-${tarea.estado}`}>
                          {formatearEstadoTarea(tarea.estado)}
                        </span>
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
                                                    <small>Fin</small>
                                                    <strong>{formatearFecha(tarea.fechaFinEstimada)}</strong>
                                                </div>
                                            </div>

                                            {puedeActualizarTareas && (
                                                <div className="inline-actions task-actions">
                                                    <button
                                                        type="button"
                                                        onClick={() => cambiarEstadoTarea(tarea.id, "IN_PROGRESS")}
                                                        disabled={tarea.estado !== "PENDING"}
                                                    >
                                                        Iniciar
                                                    </button>

                                                    <button
                                                        type="button"
                                                        onClick={() => cambiarEstadoTarea(tarea.id, "DONE")}
                                                        disabled={tarea.estado !== "IN_PROGRESS"}
                                                    >
                                                        Finalizar
                                                    </button>

                                                    {puedeGestionarProyecto && (
                                                        <button
                                                            className="danger"
                                                            type="button"
                                                            onClick={() => eliminarTarea(tarea)}
                                                            disabled={eliminandoTareaId === tarea.id}
                                                        >
                                                            {eliminandoTareaId === tarea.id ? "Eliminando..." : "Eliminar"}
                                                        </button>
                                                    )}
                                                </div>
                                            )}
                                        </article>
                                    ))
                                )}
                            </section>
                        ))}
                    </div>
                </section>
            )}

            {tabActiva === "equipo" && (
                <section className={puedeGestionarProyecto ? "work-detail-grid team-work-grid" : "work-detail-grid team-work-grid team-work-grid-readonly"}>
                    {puedeGestionarProyecto && (
                        <article className="panel work-form-panel">
                            <h2>Asignar miembro</h2>

                            <form className="form-grid single" onSubmit={asignarMiembro}>
                                <FormField label="Miembro existente">
                                    <select
                                        value={idMiembroAsignar}
                                        onChange={(event) => setIdMiembroAsignar(event.target.value)}
                                        required
                                    >
                                        <option value="">Seleccionar miembro</option>
                                        {miembrosDisponibles.map((miembro) => (
                                            <option key={miembro.id} value={miembro.id}>
                                                {obtenerNombreCompletoMiembro(miembro)} - {formatearRol(miembro.rol)}
                                            </option>
                                        ))}
                                    </select>
                                </FormField>

                                <button
                                    className="button"
                                    type="submit"
                                    disabled={guardando || !idMiembroAsignar}
                                >
                                    Asignar al proyecto
                                </button>
                            </form>
                        </article>
                    )}

                    <section className="assigned-team-grid">
                        {detalle.miembrosAsignados.length === 0 ? (
                            <article className="panel empty-state">
                                <h2>No hay miembros asignados</h2>
                                <p>Asigna integrantes para distribuir responsabilidades del proyecto.</p>
                            </article>
                        ) : (
                            detalle.miembrosAsignados.map((miembro) => (
                                <article className="team-card team-card-pro" key={miembro.id}>
                                    <div className="avatar avatar-pro">
                                        {obtenerInicialesAsignacion(miembro)}
                                    </div>

                                    <div className="team-card-body">
                                        <div className="team-card-heading">
                                            <h3>{miembro.nombreMiembro}</h3>
                                        </div>
                                        <span className="role-pill role-pill-pro">
                      {formatearRol(miembro.rolMiembro)}
                    </span>
                                    </div>
                                </article>
                            ))
                        )}
                    </section>
                </section>
            )}
        </main>
    );
}

function obtenerRolesSesion() {
    const posiblesKeys = [
        "innovatech_roles",
        "roles",
        "user_roles",
        "authorities",
    ];

    const roles = [];

    posiblesKeys.forEach((key) => {
        try {
            const valor = localStorage.getItem(key);

            if (!valor) {
                return;
            }

            const parsed = JSON.parse(valor);

            if (Array.isArray(parsed)) {
                parsed.forEach((rol) => roles.push(normalizarRol(rol)));
            } else if (typeof parsed === "string") {
                roles.push(normalizarRol(parsed));
            }
        } catch {
            const valorPlano = localStorage.getItem(key);
            if (valorPlano) {
                roles.push(normalizarRol(valorPlano));
            }
        }
    });

    const posiblesUsuarios = [
        "innovatech_user",
        "usuario",
        "user",
        "currentUser",
        "authUser",
    ];

    posiblesUsuarios.forEach((key) => {
        try {
            const valor = localStorage.getItem(key);

            if (!valor) {
                return;
            }

            const usuario = JSON.parse(valor);

            if (usuario?.rol) {
                roles.push(normalizarRol(usuario.rol));
            }

            if (usuario?.role) {
                roles.push(normalizarRol(usuario.role));
            }

            if (usuario?.tipoUsuario) {
                roles.push(normalizarRol(usuario.tipoUsuario));
            }

            if (Array.isArray(usuario?.roles)) {
                usuario.roles.forEach((rol) => roles.push(normalizarRol(rol)));
            }

            if (Array.isArray(usuario?.authorities)) {
                usuario.authorities.forEach((authority) => {
                    if (typeof authority === "string") {
                        roles.push(normalizarRol(authority));
                    } else if (authority?.authority) {
                        roles.push(normalizarRol(authority.authority));
                    }
                });
            }
        } catch {
            // Ignorar valores inválidos en localStorage
        }
    });

    return [...new Set(roles.filter(Boolean))];
}

function normalizarRol(rol) {
    if (!rol) {
        return "";
    }

    if (typeof rol === "object" && rol.authority) {
        return String(rol.authority).trim().toUpperCase();
    }

    return String(rol).trim().toUpperCase();
}

function TaskResume({ label, value }) {
    return (
        <div>
            <span>{label}</span>
            <strong>{value}</strong>
        </div>
    );
}

function FormField({ label, children }) {
    return (
        <label className="form-field-pro">
            <span>{label}</span>
            {children}
        </label>
    );
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
        COMPLETED: "Finalizado",
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

function formatearRol(rol) {
    const roles = {
        ADMIN: "Administrador",
        ROLE_ADMIN: "Administrador",
        PROJECT_MANAGER: "Project Manager",
        ROLE_PROJECT_MANAGER: "Project Manager",
        SCRUM_MASTER: "Scrum Master",
        ROLE_SCRUM_MASTER: "Scrum Master",
        DEVELOPER: "Developer",
        ROLE_DEVELOPER: "Developer",
        QA: "QA",
        ROLE_QA: "QA",
        DEVOPS: "DevOps",
        ROLE_DEVOPS: "DevOps",
        UI_UX: "UI/UX",
        ROLE_UI_UX: "UI/UX",
    };

    return roles[rol] || rol || "Sin rol";
}

function convertirFecha(fecha) {
    if (!fecha) {
        return null;
    }

    if (Array.isArray(fecha)) {
        const [year, month, day] = fecha;
        return new Date(year, month - 1, day);
    }

    if (typeof fecha === "object") {
        const year = fecha.year ?? fecha.anio;
        const month = fecha.month ?? fecha.mes;
        const day = fecha.day ?? fecha.dia;

        if (!year || !month || !day) {
            return null;
        }

        return new Date(year, month - 1, day);
    }

    if (typeof fecha === "string") {
        return new Date(`${fecha}T00:00:00`);
    }

    return null;
}

function formatearFecha(fecha) {
    const date = convertirFecha(fecha);

    if (!date || Number.isNaN(date.getTime())) {
        return "Sin fecha";
    }

    return date.toLocaleDateString("es-CL");
}

function formatearPorcentaje(valor) {
    return `${Number(valor || 0).toFixed(0)}%`;
}

function limitarPorcentaje(valor) {
    return Math.min(100, Math.max(0, Number(valor || 0)));
}

function obtenerIndicadorFecha(item) {
    if (!item.fechaFinEstimada) {
        return "Sin fecha límite";
    }

    if (item.estado === "COMPLETED" || item.estado === "DONE") {
        return "Finalizado";
    }

    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);

    const fechaFin = convertirFecha(item.fechaFinEstimada);

    if (!fechaFin || Number.isNaN(fechaFin.getTime())) {
        return "Sin fecha límite";
    }

    fechaFin.setHours(0, 0, 0, 0);

    if (fechaFin < hoy) {
        return "Atrasado";
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
    return [miembro.nombres, miembro.apellidoPaterno, miembro.apellidoMaterno]
        .filter(Boolean)
        .join(" ");
}

export default ProyectoDetallePage;