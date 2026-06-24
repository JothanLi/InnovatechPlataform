import { useState } from "react";
import { Navigate } from "react-router-dom";
import { authApi } from "../api/bffApi";

function LoginPage({ autenticado, onLogin }) {
  const [credenciales, setCredenciales] = useState({
    username: "manager",
    password: "manager123",
  });
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  if (autenticado) {
    return <Navigate to="/" replace />;
  }

  const actualizarCampo = (event) => {
    const { name, value } = event.target;
    setCredenciales((actuales) => ({ ...actuales, [name]: value }));
  };

  const iniciarSesion = async (event) => {
    event.preventDefault();
    setError("");
    setCargando(true);

    try {
      const response = await authApi.post("/login", credenciales);
      onLogin(response.data);
    } catch (error) {
      setError("Usuario o contraseña inválidos.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <main className="login-shell">
      <section className="login-panel">
        <h1>Innovatech</h1>
        <p>Acceso a la plataforma de gestión de proyectos.</p>

        {error && <p className="error">{error}</p>}

        <form className="form-grid single" onSubmit={iniciarSesion}>
          <label>
            Usuario
            <input
              name="username"
              value={credenciales.username}
              onChange={actualizarCampo}
              required
            />
          </label>

          <label>
            Contraseña
            <input
              type="password"
              name="password"
              value={credenciales.password}
              onChange={actualizarCampo}
              required
            />
          </label>

          <button className="button" type="submit" disabled={cargando}>
            {cargando ? "Ingresando..." : "Ingresar"}
          </button>
        </form>

        <div className="credentials-hint">
          <strong>Usuarios demo</strong>
          <span>project_manager / project_manager123</span>
          <span>scrum_master / scrum_master123</span>
          <span>developer / developer123</span>
          <span>qa / qa123</span>
          <span>devops / devops123</span>
          <span>ui_ux / ui_ux123</span>
        </div>
      </section>
    </main>
  );
}

export default LoginPage;
