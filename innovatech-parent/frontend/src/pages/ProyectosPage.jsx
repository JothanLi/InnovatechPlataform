import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import bffApi from "../api/bffApi";

const proyectoInicial = {
  nombre: "",
  descripcion: "",
  estado: "PLANNED",
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

function ProyectosPage() {
  const rolesSesion = JSON.parse(localStorage.getItem("innovatech_roles") || "[]");
  const esAdmin = rolesSesion.includes("ADMIN");
  const [proyectos, setProyectos] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [formProyecto, setFormProyecto] = useState(proyectoInicial);
  const [busqueda, setBusqueda] = useState("");
  const [filtroEstado, setFiltroEstado] = useState("TODOS");
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [guardando, setGuardando] = useState(false);

  const cargarVista = useCallback(async () => {
    try {
      const [proyectosResponse, dashboardResponse] = await Promise.all([
        bffApi.get("/proyectos"),
        bffApi.get("/dashboard/resumen"),
      ]);

      setProyectos(proyectosResponse.data);
      setDashboard(dashboardResponse.data);
      setError("");
    } catch {
      setError("No se pudieron cargar los proyectos. Verifica que el BFF esté encendido.");
    }
  }, []);

  useEffect(() => {
    Promise.resolve().then(cargarVista);
  }, [cargarVista]);

  const proyectosFiltrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();

    return proyectos.filter((proyecto) => {
      const coincideEstado =
        filtroEstado === "TODOS" || proyecto.estado === filtroEstado;
      const coincideTexto =
        !texto ||
        [proyecto.nombre, proyecto.descripcion, formatearEstado(proyecto.estado)]
          .filter(Boolean)
          .some((valor) => valor.toLowerCase().includes(texto));

      return coincideEstado && coincideTexto;
    });
  }, [busqueda, filtroEstado, proyectos]);

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
      setMostrarFormulario(false);
      await cargarVista();
    } catch (error) {
      setError(obtenerMensajeError(error, "No se pudo crear el proyecto."));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <main className="work-shell">
      <section className="work-hero">
        <div>
          <p className="eyebrow">Mi espacio de trabajo</p>
          <h1>Proyectos</h1>
          <p>Organiza el portafolio, revisa avances y entra directo al tablero de cada proyecto.</p>
        </div>

        <div className="work-hero-actions">
          <button className="button ghost" type="button" onClick={cargarVista}>
            Actualizar
          </button>
          {esAdmin && (
            <button className="button" type="button" onClick={() => setMostrarFormulario((actual) => !actual)}>
              {mostrarFormulario ? "Ocultar formulario" : "Nuevo proyecto"}
            </button>
          )}
        </div>
      </section>

      {error && <p className="error alert-message">{error}</p>}
      {mensaje && <p className="success alert-message">{mensaje}</p>}

      {dashboard && (
        <section className="metrics-grid work-metrics" aria-label="Resumen de trabajo">
          <WorkMetric label="Total proyectos" value={dashboard.totalProyectos} helper="En tu portafolio" />
          <WorkMetric label="En progreso" value={dashboard.proyectosEnProgreso} helper="Trabajo activo" />
          <WorkMetric label="Tareas terminadas" value={dashboard.tareasTerminadas} helper="Cierre operativo" />
          <WorkMetric label="Avance general" value={`${dashboard.porcentajeAvanceGeneral}%`} helper="Promedio global" />
        </section>
      )}

      {esAdmin && mostrarFormulario && (
        <section className="panel work-form-panel">
          <div className="panel-heading">
            <div>
              <h2>Crear proyecto</h2>
              <p>Define el objetivo, estado inicial y fechas del nuevo trabajo.</p>
            </div>
          </div>

          <form className="form-grid form-grid-pro" onSubmit={crearProyecto}>
            <FormField label="Nombre">
              <input name="nombre" value={formProyecto.nombre} onChange={actualizarCampoProyecto} maxLength="120" required />
            </FormField>

            <FormField label="Estado">
              <select name="estado" value={formProyecto.estado} onChange={actualizarCampoProyecto} required>
                <option value="PLANNED">Planificado</option>
                <option value="IN_PROGRESS">En progreso</option>
                <option value="COMPLETED">Completado</option>
                <option value="CANCELLED">Cancelado</option>
              </select>
            </FormField>

            <FormField label="Inicio">
              <input type="date" name="fechaInicio" value={formProyecto.fechaInicio} onChange={actualizarCampoProyecto} />
            </FormField>

            <FormField label="Fin estimado">
              <input type="date" name="fechaFinEstimada" value={formProyecto.fechaFinEstimada} onChange={actualizarCampoProyecto} />
            </FormField>

            <FormField label="Descripción" className="span-2">
              <textarea name="descripcion" value={formProyecto.descripcion} onChange={actualizarCampoProyecto} maxLength="500" rows="4" required />
            </FormField>

            <div className="form-actions span-2 form-actions-pro">
              <button className="button secondary" type="button" onClick={() => setMostrarFormulario(false)}>
                Cancelar
              </button>
              <button className="button" type="submit" disabled={guardando}>
                {guardando ? "Guardando..." : "Crear proyecto"}
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="work-toolbar">
        <label className="search-field">
          Buscar
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
      </section>

      {proyectosFiltrados.length === 0 && !error ? (
        <section className="panel empty-state">
          <h2>No se encontraron proyectos</h2>
          <p>Prueba con otro filtro o crea un nuevo proyecto para empezar.</p>
        </section>
      ) : (
        <section className="work-project-grid">
          {proyectosFiltrados.map((proyecto) => (
            <article className="work-project-card" key={proyecto.id}>
              <div className="work-card-top">
                <span className={`status status-${proyecto.estado}`}>{formatearEstado(proyecto.estado)}</span>
                <small>{obtenerIndicadorFecha(proyecto)}</small>
              </div>

              <h2>{proyecto.nombre}</h2>
              <p>{proyecto.descripcion}</p>

              <div className="work-card-dates">
                <div>
                  <small>Inicio</small>
                  <strong>{formatearFecha(proyecto.fechaInicio)}</strong>
                </div>
                <div>
                  <small>Fin estimado</small>
                  <strong>{formatearFecha(proyecto.fechaFinEstimada)}</strong>
                </div>
              </div>

              <Link className="button full-button" to={`/proyectos/${proyecto.id}`}>
                Abrir tablero
              </Link>
            </article>
          ))}
        </section>
      )}
    </main>
  );
}

function WorkMetric({ label, value, helper }) {
  return (
    <div className="metric work-metric">
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{helper}</small>
    </div>
  );
}

function FormField({ label, className = "", children }) {
  return (
    <label className={`form-field-pro ${className}`}>
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

function formatearEstado(estado) {
  const estados = {
    PLANNED: "Planificado",
    IN_PROGRESS: "En progreso",
    COMPLETED: "Completado",
    CANCELLED: "Cancelado",
  };

  return estados[estado] || estado || "Sin estado";
}

function formatearFecha(fecha) {
  if (!fecha) {
    return "Sin fecha";
  }

  return new Date(`${fecha}T00:00:00`).toLocaleDateString("es-CL");
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

export default ProyectosPage;
