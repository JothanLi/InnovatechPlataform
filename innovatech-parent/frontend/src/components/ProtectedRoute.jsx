import { Navigate } from "react-router-dom";

function ProtectedRoute({ autenticado, roles = [], rolesPermitidos = [], children }) {
  if (!autenticado) {
    return <Navigate to="/login" replace />;
  }

  if (rolesPermitidos.length > 0) {
    const tieneRolPermitido = roles.some((rol) => rolesPermitidos.includes(rol));

    if (!tieneRolPermitido) {
      return <Navigate to="/proyectos" replace />;
    }
  }

  return children;
}

export default ProtectedRoute;