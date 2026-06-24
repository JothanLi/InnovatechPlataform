import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import bffApi from "../api/bffApi";

const tareaInicial = {
  descripcion: "",
  estado: "PENDING",
  responsable: "",
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

function ProyectoDetallePage() {
  const { idProyecto } = useParams();

  const [detalle, setDetalle] = useState(null);
  const [miembros, setMiembros] = useState([]);
  const [formTarea, setFormTarea] = useState(tareaInicial);
  const [formMiembro, setFormMiembro] = useState(miembroInicial);
  const [idMiembroAsignar, setIdMiembroAsignar] = useState("");
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    cargarVista();
  }, [idProyecto]);

  const cargarVista = async () => {
    try {
      const [detalleResponse, miembrosResponse] = await Promise.all([
        bffApi.get(`/proyectos/${idProyecto}/detalle`),
        bffApi.get("/miembros"),
      ]);

      setDetalle(detalleResponse.data);
      setMiembros(miembrosResponse.data);
      setError("");
    } catch (error) {
      setError("No se pudo cargar el detalle del proyecto.");
    }
  };

  const actualizarCampoTarea = (event) => {
    const { name, value } = event.target;
    setFormTarea((formActual) => ({ ...formActual, [name]: value }));
  };

  const actualizarCampoMiembro = (event) => {
    const { name, value } = event.target;
    setFormMiembro((formActual) => ({ ...formActual, [name]: value }));
  };

  const crearTarea = async (event) => {
    event.preventDefault();
    setGuardando(true);
    setError("");
    setMensaje("");

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

  const crearMiembro = async (event) => {
    event.preventDefault();
    setGuardando(true);
    setError("");
    setMensaje("");

    try {
      const response = await bffApi.post("/miembros", formMiembro);
      setFormMiembro(miembroInicial);
      setIdMiembroAsignar(String(response.data.id));
      setMensaje("Miembro creado correctamente.");
      await cargarVista();
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo crear el miembro."));
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

  if (error && !detalle) {
    return (
      <div className="container">
        <Link to="/">← Volver</Link>
        <p className="error">{error}</p>
        {detalle && (
          <button className="button" type="button" onClick={cargarVista}>
            Reintentar
          </button>
        )}
      </div>
    );
  }

  if (!detalle) {
    return (
      <div className="container">
        <p>Cargando detalle...</p>
      </div>
    );
  }

  return (
    <main className="container">
      <Link className="back-link" to="/">← Volver a proyectos</Link>

      {mensaje && <p className="success">{mensaje}</p>}
      {error && <p className="error">{error}</p>}

      <header className="page-header">
        <div>
          <h1>{detalle.proyecto.nombre}</h1>
          <p>{detalle.proyecto.descripcion}</p>
        </div>
        <span className={`status status-${detalle.proyecto.estado}`}>
          {formatearEstadoProyecto(detalle.proyecto.estado)}
        </span>
      </header>

      <section className="panel">
        <h2>Avance del proyecto</h2>
        <div className="progress">
          <div
            className="progress-bar"
            style={{ width: `${detalle.avance.porcentajeAvance}%` }}
          />
        </div>
        <div className="metrics-grid compact">
          <div className="metric">
            <span>Total tareas</span>
            <strong>{detalle.avance.totalTareas}</strong>
          </div>
          <div className="metric">
            <span>Pendientes</span>
            <strong>{detalle.avance.tareasPendientes}</strong>
          </div>
          <div className="metric">
            <span>En progreso</span>
            <strong>{detalle.avance.tareasEnProgreso}</strong>
          </div>
          <div className="metric">
            <span>Completado</span>
            <strong>{detalle.avance.porcentajeAvance}%</strong>
          </div>
        </div>
      </section>

      <section className="panel">
        <h2>Crear tarea</h2>
        <form className="form-grid" onSubmit={crearTarea}>
          <label className="span-2">
            Descripción
            <textarea
              name="descripcion"
              value={formTarea.descripcion}
              onChange={actualizarCampoTarea}
              maxLength="500"
              rows="3"
              required
            />
          </label>

          <label>
            Responsable
            <input
              name="responsable"
              value={formTarea.responsable}
              onChange={actualizarCampoTarea}
              maxLength="120"
              required
            />
          </label>

          <label>
            Estado
            <select name="estado" value={formTarea.estado} onChange={actualizarCampoTarea}>
              <option value="PENDING">Pendiente</option>
              <option value="IN_PROGRESS">En progreso</option>
              <option value="DONE">Terminada</option>
            </select>
          </label>

          <label>
            Inicio
            <input
              type="date"
              name="fechaInicio"
              value={formTarea.fechaInicio}
              onChange={actualizarCampoTarea}
            />
          </label>

          <label>
            Fin estimado
            <input
              type="date"
              name="fechaFinEstimada"
              value={formTarea.fechaFinEstimada}
              onChange={actualizarCampoTarea}
            />
          </label>

          <div className="form-actions span-2">
            <button className="button" type="submit" disabled={guardando}>
              Crear tarea
            </button>
          </div>
        </form>
      </section>

      <section>
        <h2>Tareas</h2>

        {detalle.tareas.length === 0 ? (
          <p>No hay tareas registradas.</p>
        ) : (
          detalle.tareas.map((tarea) => (
            <div className="card" key={tarea.id}>
              <h3>{tarea.descripcion}</h3>
              <span className={`status status-${tarea.estado}`}>
                {formatearEstadoTarea(tarea.estado)}
              </span>
              <p>
                <strong>Responsable:</strong> {tarea.responsable}
              </p>
              <div className="inline-actions">
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
                  disabled={tarea.estado === "DONE"}
                >
                  Finalizar
                </button>
              </div>
            </div>
          ))
        )}
      </section>

      <section className="panel">
        <h2>Registrar miembro</h2>
        <form className="form-grid" onSubmit={crearMiembro}>
          <label>
            Nombres
            <input name="nombres" value={formMiembro.nombres} onChange={actualizarCampoMiembro} required />
          </label>
          <label>
            Apellido paterno
            <input name="apellidoPaterno" value={formMiembro.apellidoPaterno} onChange={actualizarCampoMiembro} required />
          </label>
          <label>
            Apellido materno
            <input name="apellidoMaterno" value={formMiembro.apellidoMaterno} onChange={actualizarCampoMiembro} required />
          </label>
          <label>
            Email
            <input type="email" name="email" value={formMiembro.email} onChange={actualizarCampoMiembro} required />
          </label>
          <label>
            Contraseña
            <input type="password" name="password" value={formMiembro.password} onChange={actualizarCampoMiembro} minLength="8" required />
          </label>
          <label>
            Rol
            <select name="rol" value={formMiembro.rol} onChange={actualizarCampoMiembro}>
              <option value="PROJECT_MANAGER">Project Manager</option>
              <option value="SCRUM_MASTER">Scrum Master</option>
              <option value="DEVELOPER">Developer</option>
              <option value="QA">QA</option>
              <option value="DEVOPS">DevOps</option>
              <option value="UI_UX">UI/UX</option>
            </select>
          </label>
          <div className="form-actions span-2">
            <button className="button" type="submit" disabled={guardando}>
              Registrar miembro
            </button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>Asignar miembro al proyecto</h2>
        <form className="form-row" onSubmit={asignarMiembro}>
          <label>
            Miembro
            <select
              value={idMiembroAsignar}
              onChange={(event) => setIdMiembroAsignar(event.target.value)}
              required
            >
              <option value="">Seleccionar miembro</option>
              {miembros.map((miembro) => (
                <option key={miembro.id} value={miembro.id}>
                  {miembro.nombres} {miembro.apellidoPaterno} {miembro.apellidoMaterno} - {miembro.rol}
                </option>
              ))}
            </select>
          </label>
          <button className="button" type="submit" disabled={guardando || !idMiembroAsignar}>
            Asignar
          </button>
        </form>
      </section>

      <section>
        <h2>Miembros asignados</h2>

        {detalle.miembrosAsignados.length === 0 ? (
          <p>No hay miembros asignados.</p>
        ) : (
          detalle.miembrosAsignados.map((miembro) => (
            <div className="card" key={miembro.id}>
              <h3>{miembro.nombreMiembro}</h3>
              <p>
                <strong>Rol:</strong> {miembro.rolMiembro}
              </p>
            </div>
          ))
        )}
      </section>
    </main>
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
    COMPLETED: "Completado",
    CANCELLED: "Cancelado",
  };

  return estados[estado] || estado;
}

function formatearEstadoTarea(estado) {
  const estados = {
    PENDING: "Pendiente",
    IN_PROGRESS: "En progreso",
    DONE: "Terminada",
  };

  return estados[estado] || estado;
}

export default ProyectoDetallePage;
