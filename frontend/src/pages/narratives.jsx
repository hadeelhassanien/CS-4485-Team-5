import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";

import "./narratives.css";

const BASE = "http://localhost:8080";

export default function Narratives() {
  const navigate = useNavigate();
  const [narratives, setNarratives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;

    fetch(`${BASE}/api/narratives`)
      .then((res) => {
        if (!res.ok) throw new Error(`Narratives error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        if (isMounted) {
          setNarratives(Array.isArray(data) ? data : []);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setError(err.message);
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const trendNarratives = narratives.filter((item) => item?.type?.toLowerCase() === "trend");
  const recommendations = narratives.filter((item) => item?.type?.toLowerCase() === "recommendation");

  return (
    <div className="narratives-page">
      <header className="narratives-header">
        <NavLink to="/" className="main__brand">CreatorXP</NavLink>
        <div style={{ display: "flex", alignItems: "center", gap: "24px" }}>
          <nav className="main__nav">
            <NavLink to="/trends" className={({ isActive }) => (isActive ? "active" : "")}>Trends</NavLink>
            <NavLink to="/narratives" className={({ isActive }) => (isActive ? "active" : "")}>Narratives</NavLink>
            <NavLink to="/claims" className={({ isActive }) => (isActive ? "active" : "")}>Claims</NavLink>
          </nav>
          <img src="/icons/claims/houseIcon.svg" alt="Home" className="claims-home-btn" onClick={() => navigate("/")} />
        </div>
      </header>

      <h1 className="narratives-title">
        Narratives <span className="narratives-title-accent">&middot; Trends Analysis</span>
      </h1>

      <div className="narratives-grid">
        <section className="narratives-card narratives-card--left">
          <div className="narratives-section-label">
            <img src="/icons/narratives/Doc.svg" alt="" className="narratives-section-icon" />
            <span>RECENT NARRATIVES</span>
          </div>

          <div className="narratives-divider" />

          {loading && <div className="narratives-status">Loading narratives...</div>}
          {!loading && error && <div className="narratives-status narratives-status--error">Failed to load: {error}</div>}

          {!loading && !error && (
            <div className="narratives-list">
              {trendNarratives.length === 0 && (
                <div className="narratives-status">No trend narratives available.</div>
              )}

              {trendNarratives.map((item, index) => (
                <article key={`${item.type}-${index}`} className="narratives-quote-card">
                  <img src="/icons/narratives/User.svg" alt="" className="narratives-quote-icon" />
                  <p className="narratives-quote-text">"{item.content}"</p>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="narratives-card narratives-card--right">
          <div className="narratives-section-label">
            <img src="/icons/narratives/Book.svg" alt="" className="narratives-section-icon" />
            <span>CREATOR RECOMMENDATIONS</span>
          </div>

          <div className="narratives-divider" />

          {loading && <div className="narratives-status">Loading recommendations...</div>}
          {!loading && error && <div className="narratives-status narratives-status--error">Failed to load: {error}</div>}

          {!loading && !error && (
            <div className="narratives-group-card">
              {recommendations.length === 0 && (
                <div className="narratives-status">No recommendations available.</div>
              )}

              {recommendations.map((item, index) => (
                <p key={`${item.type}-${index}`} className="narratives-group-claim">
                  "{item.content}"
                </p>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
