import { useEffect, useState } from "react";
import { api } from "../api/api.js";
import StatCard from "../components/StatCard.jsx";

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [guests, setGuests] = useState([]);
  const [form, setForm] = useState({
    name: "",
    surname: "",
    email: "",
    phone: "",
    additionalGuests: 0,
  });
  const [file, setFile] = useState(null);

  const load = async () => {
    const [statsRes, guestsRes] = await Promise.all([
      api.get("/admin/stats"),
      api.get("/admin/guests"),
    ]);
    setStats(statsRes.data);
  if (Array.isArray(guestsRes.data)) {
    setGuests(guestsRes.data);
  } else {
    console.error("Risposta guests non valida:", guestsRes.data);
    setGuests([]);
  }  };

  useEffect(() => {
    load();
  }, []);

  const createGuest = async (e) => {
    e.preventDefault();
    await api.post("/admin/guests", form);
    setForm({ name: "", surname: "", email: "", phone: "", additionalGuests: 0 });
    await load();
  };

  const importCsv = async (e) => {
    e.preventDefault();
    if (!file) return;

    const data = new FormData();
    data.append("file", file);

    await api.post("/admin/guests/import-csv", data, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    setFile(null);
    await load();
  };

  const sendWhatsapp = async (id) => {
    await api.post(`/admin/guests/${id}/send-whatsapp`);
    await load();
  };

  const exportPdf = async () => {
  const res = await api.get("/admin/guests/export-pdf", {
    responseType: "blob",
  });

  const url = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", "invitati.pdf");
  document.body.appendChild(link);
  link.click();
  link.remove();
};

  return (
    <main className="admin-page">
      <section className="hero-panel">
        <div>
          <p className="eyebrow">Matrimonio Martina & Riccardo</p>
          <h1>Dashboard partecipazioni</h1>
          <p className="muted">
            Gestisci gli invitati, esporta PDF, copia i link e controlla le conferme.
          </p>
        </div>
      </section>

      <section className="stats-grid">
        <StatCard label="Invitati" value={stats?.totalGuests ?? 0} />
        <StatCard label="Confermati" value={stats?.confirmedGuests ?? 0} />
        <StatCard label="In attesa" value={stats?.pendingGuests ?? 0} />
        <StatCard label="Persone totali confermate" value={stats?.totalConfirmedPeople ?? 0} />
      </section>

      <section className="four-columns">
        <form className="card form-card" onSubmit={createGuest}>
          <h2>Aggiungi invitato</h2>

          <input placeholder="Nome" value={form.name}
                 onChange={(e) => setForm({ ...form, name: e.target.value })} />

          <input placeholder="Cognome" value={form.surname}
                 onChange={(e) => setForm({ ...form, surname: e.target.value })} />

          <input placeholder="Telefono es. 393331234567" value={form.phone}
                 onChange={(e) => setForm({ ...form, phone: e.target.value })} />

          <button>Aggiungi</button>
        </form>

       
      </section>

      <section className="card form-card">
        <h2>Invitati</h2>

         <button type="card" onClick={exportPdf}>
          Esporta PDF invitati
        </button>

        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Stato</th>
                <th>Persone aggiuntive</th>
                <th>Invito</th>
                <th>WhatsApp</th>
              </tr>
            </thead>

            <tbody>
              {guests.map((guest) => (
                <tr key={guest.id}>
                  <td>{guest.name} {guest.surname}</td>
                  <td><span className={`badge ${guest.status?.toLowerCase()}`}>{guest.status}</span></td>
                  <td>{guest.additionalGuests}</td>
                  <td>
                    <a href={guest.inviteUrl} target="_blank">Apri</a>
                  </td>
                  <td className="actions">
                    {guest.whatsappLink && (
                      <button type="button" onClick={() => window.open(guest.whatsappLink, "_blank")}>
                        Invia invito
                      </button>
                     )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}
