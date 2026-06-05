import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import bffApi from "../api/bffApi";

function ProyectoDetallePage() {
  const { idProyecto } = useParams();

  const [detalle, setDetalle] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    cargarDetalleProyecto();
  }, [idProyecto]);

  const cargarDetalleProyecto = async () => {
    try {
      const response = await bffApi.get(`/proyectos/${idProyecto}/detalle`);
      setDetalle(response.data);
    } catch (error) {
      setError("No se pudo cargar el detalle del proyecto.");
    }
  };

  if (error) {
    return (
      <div className="container">
        <Link to="/">← Volver</Link>
        <p className="error">{error}</p>
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
    <div className="container">
      <Link to="/">← Volver a proyectos</Link>

      <h1>{detalle.proyecto.nombre}</h1>
      <p>{detalle.proyecto.descripcion}</p>

      <p>
        <strong>Estado:</strong> {detalle.proyecto.estado}
      </p>

      <section className="card">
        <h2>Avance del proyecto</h2>
        <p>Total tareas: {detalle.avance.totalTareas}</p>
        <p>Pendientes: {detalle.avance.tareasPendientes}</p>
        <p>En progreso: {detalle.avance.tareasEnProgreso}</p>
        <p>Terminadas: {detalle.avance.tareasTerminadas}</p>
        <h3>{detalle.avance.porcentajeAvance}% completado</h3>
      </section>

      <section>
        <h2>Tareas</h2>

        {detalle.tareas.length === 0 ? (
          <p>No hay tareas registradas.</p>
        ) : (
          detalle.tareas.map((tarea) => (
            <div className="card" key={tarea.id}>
              <h3>{tarea.descripcion}</h3>
              <p>
                <strong>Estado:</strong> {tarea.estado}
              </p>
              <p>
                <strong>Responsable:</strong> {tarea.responsable}
              </p>
            </div>
          ))
        )}
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
    </div>
  );
}

export default ProyectoDetallePage;