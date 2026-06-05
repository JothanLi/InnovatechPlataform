import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import bffApi from "../api/bffApi";

function ProyectosPage() {
  const [proyectos, setProyectos] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    cargarProyectos();
  }, []);

  const cargarProyectos = async () => {
    try {
      const response = await bffApi.get("/proyectos");
      setProyectos(response.data);
    } catch (error) {
      setError("No se pudieron cargar los proyectos. Verifica que el BFF esté encendido.");
    }
  };

  return (
    <div className="container">
      <h1>Proyectos Innovatech</h1>

      {error && <p className="error">{error}</p>}

      {proyectos.length === 0 && !error && (
        <p>No hay proyectos para mostrar.</p>
      )}

      <div className="grid">
        {proyectos.map((proyecto) => (
          <div className="card" key={proyecto.id}>
            <h2>{proyecto.nombre}</h2>
            <p>{proyecto.descripcion}</p>

            <p>
              <strong>Estado:</strong> {proyecto.estado}
            </p>

            <Link className="button" to={`/proyectos/${proyecto.id}`}>
              Ver detalle
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ProyectosPage;