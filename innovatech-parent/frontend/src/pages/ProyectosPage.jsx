import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import bffApi from "../api/bffApi";

const proyectoInicial = {
  nombre: "",
  descripcion: "",
  estado: "PLANNED",
  fechaInicio: "",
  fechaFinEstimada: "",
};

function ProyectosPage() {
  const [proyectos, setProyectos] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [formProyecto, setFormProyecto] = useState(proyectoInicial);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    cargarVista();
  }, []);

  const cargarVista = async () => {
    try {
      const [proyectosResponse, dashboardResponse] = await Promise.all([
        bffApi.get("/proyectos"),
        bffApi.get("/dashboard/resumen"),
      ]);

      setProyectos(proyectosResponse.data);
      setDashboard(dashboardResponse.data);
      setError("");
    } catch (error) {
      setError("No se pudieron cargar los proyectos. Verifica que el BFF esté encendido.");
    }
  };

  const actualizarCampoProyecto = (event) => {
    const { name, value } = event.target;
    setFormProyecto((formActual) => ({ ...formActual, [name]: value }));
  };

  const crearProyecto = async (event) => {
    event.preventDefault();
    setGuardando(true);
    setMensaje("");
    setError("");

    try {
      await bffApi.post("/proyectos", normalizarFechas(formProyecto));
      setFormProyecto(proyectoInicial);
      setMensaje("Proyecto creado correctamente.");
      await cargarVista();
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo crear el proyecto."));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <main className="container">
      <header className="page-header">
        <div>
          <h1>Proyectos Innovatech</h1>
          <p>Gestión centralizada de proyectos, tareas y equipos.</p>
        </div>
      </header>

      {error && <p className="error">{error}</p>}
      {mensaje && <p className="success">{mensaje}</p>}

      {dashboard && (
        <section className="metrics-grid" aria-label="Resumen de avance">
          <div className="metric">
            <span>Total proyectos</span>
            <strong>{dashboard.totalProyectos}</strong>
          </div>
          <div className="metric">
            <span>En progreso</span>
            <strong>{dashboard.proyectosEnProgreso}</strong>
          </div>
          <div className="metric">
            <span>Tareas terminadas</span>
            <strong>{dashboard.tareasTerminadas}</strong>
          </div>
          <div className="metric">
            <span>Avance general</span>
            <strong>{dashboard.porcentajeAvanceGeneral}%</strong>
          </div>
        </section>
      )}

      <section className="panel">
        <h2>Crear proyecto</h2>

        <form className="form-grid" onSubmit={crearProyecto}>
          <label>
            Nombre
            <input
              name="nombre"
              value={formProyecto.nombre}
              onChange={actualizarCampoProyecto}
              maxLength="120"
              required
            />
          </label>

          <label>
            Estado
            <select
              name="estado"
              value={formProyecto.estado}
              onChange={actualizarCampoProyecto}
              required
            >
              <option value="PLANNED">Planificado</option>
              <option value="IN_PROGRESS">En progreso</option>
              <option value="COMPLETED">Completado</option>
              <option value="CANCELLED">Cancelado</option>
            </select>
          </label>

          <label>
            Inicio
            <input
              type="date"
              name="fechaInicio"
              value={formProyecto.fechaInicio}
              onChange={actualizarCampoProyecto}
            />
          </label>

          <label>
            Fin estimado
            <input
              type="date"
              name="fechaFinEstimada"
              value={formProyecto.fechaFinEstimada}
              onChange={actualizarCampoProyecto}
            />
          </label>

          <label className="span-2">
            Descripción
            <textarea
              name="descripcion"
              value={formProyecto.descripcion}
              onChange={actualizarCampoProyecto}
              maxLength="500"
              rows="3"
              required
            />
          </label>

          <div className="form-actions span-2">
            <button className="button" type="submit" disabled={guardando}>
              {guardando ? "Guardando..." : "Crear proyecto"}
            </button>
          </div>
        </form>
      </section>

      {proyectos.length === 0 && !error && (
        <p>No hay proyectos para mostrar.</p>
      )}

      <div className="grid">
        {proyectos.map((proyecto) => (
          <div className="card" key={proyecto.id}>
            <h2>{proyecto.nombre}</h2>
            <p>{proyecto.descripcion}</p>

            <span className={`status status-${proyecto.estado}`}>
              {formatearEstado(proyecto.estado)}
            </span>

            <Link className="button" to={`/proyectos/${proyecto.id}`}>
              Ver detalle
            </Link>
          </div>
        ))}
      </div>
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

function formatearEstado(estado) {
  const estados = {
    PLANNED: "Planificado",
    IN_PROGRESS: "En progreso",
    COMPLETED: "Completado",
    CANCELLED: "Cancelado",
  };

  return estados[estado] || estado;
}

export default ProyectosPage;
