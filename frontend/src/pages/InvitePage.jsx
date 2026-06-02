import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/api.js";
import headerImage from "../assets/header.jpeg";

export default function InvitePage() {
  const { token } = useParams();
  const [guest, setGuest] = useState(null);
  const [showGift, setShowGift] = useState(false);
  const [saved, setSaved] = useState(false);
  const [form, setForm] = useState({
    status: "CONFERMATO",
    additionalGuests: 0,
    allergies: "",
    message: "",
  });

  useEffect(() => {
    api.get(`/invite/${token}`).then((res) => {
      setGuest(res.data);
      setForm({
        status: res.data.status === "RIFIUTATO" ? "RIFIUTATO" : "CONFERMATO",
        additionalGuests: res.data.additionalGuests ?? 0,
        allergies: res.data.allergies ?? "",
        message: res.data.message ?? "",
      });
    });
  }, [token]);

  const submit = async (e) => {
    e.preventDefault();
    await api.post(`/invite/${token}/reply`, form);
    setSaved(true);
  };

  if (!guest) {
    return <main className="invite-page"><div className="invite-card">Caricamento...</div></main>;
  }

  return (
    <main className="invite-page">
      <section className="invite-card">

        <div className="invite-image-wrapper">
          <img src={headerImage} alt="Martina e Riccardo" className="invite-image"/>
        </div>

        <p className="eyebrow">Save the date</p>
        <h1>Martina & Riccardo</h1>
        <p className="subtitle">Siamo felici di invitarti al nostro matrimonio</p>

        <div className="event-box">
          <p><strong>Data:</strong> 18 settembre 2026, ore 17:00</p>
          <p><strong>Luogo:</strong> Tenuta la Gramignana, Vitorchiano (VT)</p>
          <a href="https://maps.app.goo.gl/KzdkGAmsC9G4Qkru5" target="_blank" rel="noopener noreferrer" className="maps-button">
            ✨ Come arrivare
          </a>
        </div>

        <p className="guest-hi">
          Ciao {guest.name}, conferma qui la tua presenza.
        </p>

        {saved && <div className="success">Risposta salvata correttamente.</div>}

        <form onSubmit={submit} className="rsvp-form">
          <label>Partecipazione</label>
          <select value={form.status}
                  onChange={(e) => setForm({ ...form, status: e.target.value })}>
            <option value="CONFERMATO">Parteciperò</option>
            <option value="RIFIUTATO">Non potrò partecipare</option>
          </select>

          <label>Porti qualcuno? faccelo sapere: </label>
          <input type="number" min="0" value={form.additionalGuests}
                 onChange={(e) => setForm({ ...form, additionalGuests: Number(e.target.value) })} />

          <label>Allergie o intolleranze</label>
          <textarea value={form.allergies}
                    onChange={(e) => setForm({ ...form, allergies: e.target.value })} />

          <label>Messaggio agli sposi</label>
          <textarea value={form.message}
                    onChange={(e) => setForm({ ...form, message: e.target.value })} />

          <button>Invia conferma</button>

          <button type="button" className="gift-button" onClick={() => setShowGift(true)}>
              🎁 Regalo agli sposi
          </button>
        </form>
        {showGift && (
  <div className="modal-overlay">
    <div className="gift-modal">

      <button
        className="close-button"
        onClick={() => setShowGift(false)}
      >
        ×
      </button>

      <h2>Lista nozze</h2>

      <p>
        Se desideri farci un regalo,
        puoi contribuire al nostro viaggio ❤️
      </p>

      <div className="iban-box">
        <strong>IBAN</strong>

        <p>IT60X0542811101000000123456</p>

        <button
          type="button"
          onClick={() => {
            navigator.clipboard.writeText(
              "IT60X0542811101000000123456"
            );
          }}
        >
          Copia IBAN
        </button>
      </div>

    </div>
  </div>
)}
      </section>
    </main>
  );
}
