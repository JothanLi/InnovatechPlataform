import { Navigate } from "react-router-dom";

function ProtectedRoute({ autenticado, children }) {
  if (!autenticado) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;
