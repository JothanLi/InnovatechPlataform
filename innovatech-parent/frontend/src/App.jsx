import { BrowserRouter, Routes, Route, Link, Navigate } from "react-router-dom";
import { useMemo, useState } from "react";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import ProyectosPage from "./pages/ProyectosPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import ProyectoDetallePage from "./pages/ProyectoDetallePage";
import "./App.css";

function App() {
  const [sesion, setSesion] = useState(() => {
    const token = localStorage.getItem("innovatech_token");
    const username = localStorage.getItem("innovatech_username");
    const roles = JSON.parse(localStorage.getItem("innovatech_roles") || "[]");

    return token ? { token, username, roles } : null;
  });

  const autenticado = useMemo(() => Boolean(sesion?.token), [sesion]);
  const esAdmin = sesion?.roles?.includes("ADMIN");

    const iniciarSesion = (loginResponse) => {
        const token = loginResponse.accessToken || loginResponse.token;
        const username = loginResponse.username;
        const roles = loginResponse.roles || [];

        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("tipoUsuario");
        localStorage.removeItem("jwt");
        localStorage.removeItem("accessToken");

        localStorage.setItem("innovatech_token", token);
        localStorage.setItem("innovatech_username", username);
        localStorage.setItem("innovatech_roles", JSON.stringify(roles));

        setSesion({
            token,
            username,
            roles,
        });
    };

    const cerrarSesion = () => {
        localStorage.removeItem("innovatech_token");
        localStorage.removeItem("innovatech_username");
        localStorage.removeItem("innovatech_roles");

        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("tipoUsuario");
        localStorage.removeItem("jwt");
        localStorage.removeItem("accessToken");

        setSesion(null);
    };

  return (
    <BrowserRouter>
      <nav className="navbar">
        <Link to={esAdmin ? "/" : "/proyectos"}>Innovatech</Link>

        {autenticado && (
          <div className="session">
            {esAdmin && (
              <Link className="nav-link" to="/">
                Admin
              </Link>
            )}

            {!esAdmin && (
              <Link className="nav-link" to="/proyectos">
                Proyectos
              </Link>
            )}

            <span>{sesion.username}</span>

            <button type="button" onClick={cerrarSesion}>
              Salir
            </button>
          </div>
        )}
      </nav>

      <Routes>
        <Route
          path="/login"
          element={
            <LoginPage autenticado={autenticado} onLogin={iniciarSesion} />
          }
        />

        <Route
          path="/"
          element={
            <ProtectedRoute
              autenticado={autenticado}
              roles={sesion?.roles || []}
              rolesPermitidos={["ADMIN"]}
            >
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/proyectos"
          element={
            esAdmin ? (
              <Navigate to="/" replace />
            ) : (
              <ProtectedRoute
                autenticado={autenticado}
                roles={sesion?.roles || []}
              >
                <ProyectosPage />
              </ProtectedRoute>
            )
          }
        />

        <Route
          path="/proyectos/:idProyecto"
          element={
            esAdmin ? (
              <Navigate to="/" replace />
            ) : (
              <ProtectedRoute
                autenticado={autenticado}
                roles={sesion?.roles || []}
              >
                <ProyectoDetallePage />
              </ProtectedRoute>
            )
          }
        />

        <Route
          path="*"
          element={<Navigate to={esAdmin ? "/" : "/proyectos"} replace />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;