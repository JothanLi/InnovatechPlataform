import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import ProyectosPage from "./pages/ProyectosPage";
import ProyectoDetallePage from "./pages/ProyectoDetallePage";
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <nav className="navbar">
        <Link to="/">Innovatech</Link>
      </nav>

      <Routes>
        <Route path="/" element={<ProyectosPage />} />
        <Route path="/proyectos/:idProyecto" element={<ProyectoDetallePage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;