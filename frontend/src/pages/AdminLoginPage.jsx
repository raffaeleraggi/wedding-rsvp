import { useState } from "react";
import { api } from "../api/api.js";

export default function AdminLoginPage({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const login = async (e) => {
    e.preventDefault();

    const res = await api.post("/admin/auth/login", {
      username,
      password,
    });

    localStorage.setItem("adminToken", res.data.token);
    onLogin();
  };

  return (
    <main className="admin-page">
      <form className="card form-card" onSubmit={login}>
        <h2>Accesso admin</h2>

        <input
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />

        <input
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button type="submit">Entra</button>
      </form>
    </main>
  );
}