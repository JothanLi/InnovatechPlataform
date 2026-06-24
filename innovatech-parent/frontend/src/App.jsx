import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import { useMemo, useState } from "react";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import ProyectosPage from "./pages/ProyectosPage";
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

  const iniciarSesion = (loginResponse) => {
    localStorage.setItem("innovatech_token", loginResponse.accessToken);
    localStorage.setItem("innovatech_username", loginResponse.username);
    localStorage.setItem("innovatech_roles", JSON.stringify(loginResponse.roles));
    setSesion({
      token: loginResponse.accessToken,
      username: loginResponse.username,
      roles: loginResponse.roles,
    });
  };

  const cerrarSesion = () => {
    localStorage.removeItem("innovatech_token");
    localStorage.removeItem("innovatech_username");
    localStorage.removeItem("innovatech_roles");
    setSesion(null);
  };

  return (
    <BrowserRouter>
      <nav className="navbar">
        <Link to="/">Innovatech</Link>
        {autenticado && (
          <div className="session">
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
          element={<LoginPage autenticado={autenticado} onLogin={iniciarSesion} />}
        />
        <Route
          path="/"
          element={
            <ProtectedRoute autenticado={autenticado}>
              <ProyectosPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/proyectos/:idProyecto"
          element={
            <ProtectedRoute autenticado={autenticado}>
              <ProyectoDetallePage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
