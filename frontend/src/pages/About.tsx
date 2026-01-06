import { Link } from "react-router-dom";

export default function About() {
  return (
    <div className="about-screen">
      <div className="about-container">
        <div className="about-hero">
          <section className="card">
            <h1 className="about-title">Πληροφορίες</h1>
            <p className="about-subtitle">
              Το Festival Manager είναι μια εφαρμογή διαχείρισης φεστιβάλ, προγραμμάτων, προβολών και υποβολών ταινιών.
            </p>

            <div style={{ marginTop: 14, display: "flex", flexWrap: "wrap", gap: 10 }}>
              <Link to="/programs" className="btn-secondary">
                Δες Προγράμματα
              </Link>
              <Link to="/login" className="btn-primary">
                Σύνδεση
              </Link>
              <Link to="/" className="btn-ghost">
                Επιστροφή στο Home
              </Link>
            </div>

            <div style={{ marginTop: 16 }}>
              <h2 className="about-section-title">Κύριες δυνατότητες</h2>
              <ul className="about-list">
                <li className="about-li">
                  <span>Προγράμματα φεστιβάλ & καταστάσεις</span>
                  <small>Festivals</small>
                </li>
                <li className="about-li">
                  <span>Προβολές / πρόγραμμα προβολών</span>
                  <small>Screenings</small>
                </li>
                <li className="about-li">
                  <span>Υποβολές ταινιών & παρακολούθηση</span>
                  <small>Submissions</small>
                </li>
              </ul>
            </div>
          </section>

          <aside className="card">
            <h2 className="about-section-title">Ρόλοι χρηστών</h2>
            <p className="about-section-text">
              Η εφαρμογή στηρίζεται σε ρόλους ώστε κάθε χρήστης να βλέπει μόνο ό,τι χρειάζεται.
            </p>

            <ul className="about-list">
              <li className="about-li">
                <span>Staff</span>
                <small>Διαχείριση</small>
              </li>
              <li className="about-li">
                <span>Programmer</span>
                <small>Πρόγραμμα/προβολές</small>
              </li>
              <li className="about-li">
                <span>Submitter/Artist</span>
                <small>Υποβολές</small>
              </li>
            </ul>
          </aside>
        </div>

        <div className="about-footer">
          Festival Manager • Εργασία Τεχνολογία Λογισμικού
        </div>
      </div>
    </div>
  );
}
