import React from "react";
import { Link } from "react-router-dom";

const Home: React.FC = () => {
  return (
    <div className="home-screen">
      <header className="home-topbar">
        <div className="home-container">
          <div className="home-topbar-row">
            <div className="home-brand">
              <div className="home-logo">FM</div>
              <div className="home-brand-text">
                <div className="home-brand-title">Festival Manager</div>
                <div className="home-brand-subtitle">Frontend</div>
              </div>
            </div>

            <nav className="home-nav">
              <Link className="home-nav-link" to="/programs">
                Προγράμματα
              </Link>
              <Link className="home-nav-link" to="/about">
                Πληροφορίες
              </Link>
              <Link className="home-nav-link home-nav-primary" to="/login">
                Σύνδεση
              </Link>
            </nav>
          </div>
        </div>
      </header>

      <main className="home-main">
        <div className="home-grid">
          {/* Hero */}
          <section className="home-card">
            <h1 className="home-hero-title">Καλώς ήρθατε στο Festival Manager</h1>
            <p className="home-hero-subtitle">
              Διαχείριση φεστιβάλ, προγραμμάτων, προβολών και υποβολών ταινιών σε ένα ενιαίο σύστημα.
            </p>

            <div className="home-cta-row">
              <Link to="/login" className="btn-primary">
                Σύνδεση
              </Link>
              <Link to="/programs" className="btn-secondary">
                Προγράμματα
              </Link>
              <Link to="/about" className="btn-ghost">
                Πληροφορίες
              </Link>
            </div>

            <div className="home-mini-grid">
              <div className="home-mini">
                <div className="home-mini-k">Ρόλοι</div>
                <div className="home-mini-v">Staff • Programmer • Artist</div>
              </div>
              <div className="home-mini">
                <div className="home-mini-k">Δεδομένα</div>
                <div className="home-mini-v">Φεστιβάλ • Προβολές • Ταινίες</div>
              </div>
              <div className="home-mini">
                <div className="home-mini-k">Πλοήγηση</div>
                <div className="home-mini-v">Γρήγορη πρόσβαση στις ενότητες</div>
              </div>
            </div>
          </section>

          {/* Side */}
          <aside className="home-card">
            <h2 className="home-side-title">Γρήγορη εκκίνηση</h2>
            <p className="home-side-text">
              Αν έχεις λογαριασμό, κάνε σύνδεση. Διαφορετικά μπορείς να δεις τα ανακοινωμένα προγράμματα ως επισκέπτης.
            </p>

            <div className="home-side-actions">
              <Link to="/login" className="btn-primary">
                Πήγαινε στη Σύνδεση
              </Link>
              <Link to="/programs" className="btn-secondary">
                Δες Προγράμματα
              </Link>
            </div>

            <div className="home-tip">
              <div className="home-tip-k">Tip</div>
              <div className="home-tip-v">
                Στο login μπορείς να πατήσεις <b>Enter</b>.
              </div>
            </div>
          </aside>
        </div>

        {/* Actions */}
        <section className="home-actions">
          <div className="home-card">
            <h3 className="home-action-title">Για επισκέπτες</h3>
            <p className="home-action-text">
              Δείτε ανακοινωμένα προγράμματα και λεπτομέρειες φεστιβάλ.
            </p>
            <Link className="home-action-link" to="/programs">
              Άνοιγμα προγραμμάτων →
            </Link>
          </div>

          <div className="home-card">
            <h3 className="home-action-title">Για submitters</h3>
            <p className="home-action-text">
              Συνδεθείτε για να δείτε τις υποβολές σας και την κατάστασή τους.
            </p>
            <Link className="home-action-link" to="/my-submissions">
              Οι υποβολές μου →
            </Link>
          </div>

          <div className="home-card">
            <h3 className="home-action-title">Για προσωπικό</h3>
            <p className="home-action-text">
              Διαχείριση προγραμμάτων, προβολών και ροών εργασίας.
            </p>
            <Link className="home-action-link" to="/dashboard">
              Πίνακας ελέγχου →
            </Link>
          </div>
        </section>

        <footer className="home-footer">
          Festival Manager • Εργασία Τεχνολογία Λογισμικού
        </footer>
      </main>
    </div>
  );
};

export default Home;
