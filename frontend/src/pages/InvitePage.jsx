import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/api.js";
import headerImage from "../assets/header.jpeg";
import envelopeBg from "../assets/envelop-bg-blu.png";

export default function InvitePage() {
  const { token } = useParams();
  const [guest, setGuest] = useState(null);
  const [showGift, setShowGift] = useState(false);
  const [saved, setSaved] = useState(false);
  const [editing, setEditing] = useState(false);
  const [envelopeOpen, setEnvelopeOpen] = useState(false);
  const [showPhoto, setShowPhoto] = useState(false);
  const alreadyAnswered = guest?.status && guest.status !== "IN_ATTESA" && !editing;
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

{!envelopeOpen && (
  <div className="envelope-cover">
    <button
      type="button"
      className="envelope-cover-button"
      onClick={() => setEnvelopeOpen(true)}
      style={{ backgroundImage: `url(${envelopeBg})` }}
      aria-label="Apri invito"
    />
  </div>
)}

<div className={`invite-content ${envelopeOpen ? "open" : "closed"}`}>

      <div className="invite-image-wrapper"
        onClick={() => setShowPhoto(true)}>
          <img src={headerImage} alt="Martina e Riccardo" className="invite-image"/>
      </div>

      <p className="save-date">Save the date</p>
      <h1 className="couple-title">
        Martina
        <br />
          &
        <br />
        Riccardo
      </h1>
      <h4 className="invite-subtitle">Siamo felici di invitarti al nostro matrimonio</h4>
      <p className="date">18 settembre 2026</p>

      <div className="event-box">
        <h4>Celebrazione</h4>
        <p><strong>Casa Comunale di Vitorchiano</strong></p>
        <p>ore 17:00</p>
        <a
          href="https://maps.app.goo.gl/1wsw1Gfb9FjAoeBX6"
          target="_blank"
          rel="noopener noreferrer"
          className="maps-button"
        >
          ✧ Come arrivare
        </a>
      </div>

      <div className="event-box">
        <h4>Ricevimento</h4>
        <p><strong>Tenuta la Gramignana</strong></p>
        <p>ore 18:00</p>

        <a
          href="https://maps.app.goo.gl/KzdkGAmsC9G4Qkru5"
          target="_blank"
          rel="noopener noreferrer"
          className="maps-button"
        >
          ✧ Come arrivare
        </a>
      </div>

      {!alreadyAnswered && (
  <p className="guest-hi">
    Ciao {guest.name} conferma qui la tua presenza.
  </p>
)}

      {alreadyAnswered ? (
        <div className="saved-rsvp-card">

          <div className="saved-icon">
            ✓
          </div>

          <h2>Abbiamo già salvato la tua risposta.</h2>

          <div className="saved-status">
            {guest.status === "CONFERMATO"
              ? "Parteciperai al matrimonio ♡"
              : "Non potrai partecipare"}
          </div>

          <button
            type="button"
            onClick={() => {
              setForm({
                status: guest.status,
                additionalGuests: guest.additionalGuests || 0,
                allergies: guest.allergies || "",
                message: guest.message || "",
              });

              setEditing(true);
            }}
          >
            Modifica risposta
          </button>

        </div>
      ) : (
        <form onSubmit={submit} className="rsvp-form">

          <label>Partecipazione</label>
          <select
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value })}
          >
            <option value="CONFERMATO">Parteciperò</option>
            <option value="RIFIUTATO">Non potrò partecipare</option>
          </select>

          <label>Porti qualcuno? Faccelo sapere:</label>
         <select
  value={form.additionalGuests}
  onChange={(e) =>
    setForm({
      ...form,
      additionalGuests: Number(e.target.value),
    })
  }
>
  <option value={0}>Verrò da solo/a</option>
  <option value={1}>Porterò 1 ospite</option>
  <option value={2}>Porterò 2 ospiti</option>
  <option value={3}>Porterò 3 ospiti</option>
  <option value={4}>Porterò 4 ospiti</option>
</select>

          <label>Allergie o intolleranze</label>
          <textarea
            value={form.allergies}
            onChange={(e) =>
              setForm({ ...form, allergies: e.target.value })
            }
          />

          <label>Messaggio agli sposi</label>
          <textarea
            value={form.message}
            onChange={(e) =>
              setForm({ ...form, message: e.target.value })
            }
          />

          <button type="submit">
            Invia conferma
          </button>

        </form>
      )}

            {saved && (
        <div className="success">
          Risposta salvata correttamente.
        </div>
      )}

      <button
        type="button"
        className="gift-button"
        onClick={() => setShowGift(true)}
      >
        ❀ Un pensiero per gli sposi ❀
      </button>
</div>
    </section>

    {showGift && (
      <div className="modal-overlay">
        <div className="gift-modal">

          <button
            type="button"
            className="close-button"
            onClick={() => setShowGift(false)}
          >
            ×
          </button>

          <h2>❀</h2>

          <p>
            La vostra presenza, sarà per noi la gioia più grande.
            Se desiderate accompagnarci con un pensiero, potrete contribuire ai nostri progetti futuri ♡
          </p>

<div className="iban-box">
  <div className="iban-label">IBAN</div>

  <div className="iban-value">
    IT63C0366901600474478451042
  </div>

  <div className="iban-label">
    Beneficiario
  </div>

  <div className="beneficiary">
    Martina Fornara
    <br />
    &
    <br />
    Riccardo Marzolini
  </div>

  <button
    type="button"
    onClick={() =>
      navigator.clipboard.writeText(
        "IT63C0366901600474478451042"
      )
    }
  >
    Copia IBAN
  </button>
</div>

        </div>
      </div>
    )}

    {showPhoto && (
  <div
    className="photo-overlay"
    onClick={() => setShowPhoto(false)}
  >
    <button
      className="photo-close"
      onClick={() => setShowPhoto(false)}
    >
      ×
    </button>

    <img
      src={headerImage}
      alt="Martina e Riccardo"
      className="photo-full"
    />
  </div>
)}

  </main>
);
}
