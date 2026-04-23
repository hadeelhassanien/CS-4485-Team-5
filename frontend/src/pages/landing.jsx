import "./landing.css";
import { NavLink, useNavigate } from "react-router-dom";

export default function Landing() {
  const navigate = useNavigate();

  return (
    <div className="claims-page">
      <header className="claims-header">
        <NavLink to="/" className="main__brand">CreatorXP</NavLink>
        <nav className="main__nav">
          <NavLink to="/trends" className={({ isActive }) => (isActive ? "active" : "")}>
            Trends
          </NavLink>
          <NavLink to="/narratives" className={({ isActive }) => (isActive ? "active" : "")}>
            Narratives
          </NavLink>
          <NavLink to="/claims" className={({ isActive }) => (isActive ? "active" : "")}>
            Claims
          </NavLink>
        </nav>

        <button className="main__infoBtn" onClick={() => navigate("/learnmore")}>
          <span className="main__infoIcon">i</span>
          info &amp; value
        </button>
      </header>

      <main className="main__layout">
        <section className="main__left">
          <h1 className="main__title">
            Stop guessing !
            <br />
            Let data drive your next
            <br />
            video.
          </h1>

          <p className="main__sub">
            Gaming trends change fast. YouTube analytics alone leave you blind. We show you exactly
            when to switch genres to boost views and income.
          </p>

          <div className="main__features">
            <div 
              className="main__feature"
              onClick={() => navigate("/trends")}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  navigate("/trends");
                }
              }}
              role="button"
              tabIndex={0}
            >
              <div className="main__featureIcon">
                <img
                  className="main__svgIcon"
                  src="/icons/landing/group-5-trends.svg"
                  alt="Trend tracker icon"
                />
              </div>
              <div>
                <div className="main__featureTitle">Trend tracker</div>
                <div className="main__featureText">
                  real-time genre popularity &amp; future predictions
                </div>
              </div>
            </div>

            <div
              className="main__feature"
              onClick={() => navigate("/narratives")}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  navigate("/narratives");
                }
              }}
              role="button"
              tabIndex={0}
            >
              <div className="main__featureIcon">
                <img
                  className="main__svgIcon"
                  src="/icons/landing/group-5-narratives.svg"
                  alt="narratives icon"
                />
              </div>
              <div>
                <div className="main__featureTitle">Narratives</div>
                <div className="main__featureText">insights into gaming narratives</div>
              </div>
            </div>

            <div 
              className="main__feature"
              onClick={() => navigate("/claims")}
              onKeyDown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  navigate("/trends");
                }
              }}
              role="button"
              tabIndex={0}
            >            
              <div className="main__featureIcon">
                <img
                  className="main__svgIcon"
                  src="/icons/landing/group-5-claims.svg"
                  alt="Claims icon"
                />
              </div>
              <div>
                <div className="main__featureTitle">Claims</div>
                <div className="main__featureText">data-driven revenue and performance</div>
              </div>
            </div>
          </div>
        </section>

        <aside className="main__card">
          <div className="main__pill">PROBLEM</div>

          <div className="main__quotes">”</div>

          <div className="main__cardHeadline">
            “I don’t know if switching to a trending game will actually grow my channel.”
          </div>

          <p className="main__cardBody">
            Gaming trends change fast. YouTube analytics alone leave you blind. We show you exactly
            when to switch genres to boost views and income.
          </p>

          <div className="main__cardActions">
            <button className="main__btn main__btnGhost" onClick={() => navigate("/learnmore")}>
              learn more
            </button>

            <button className="main__btn main__btnPrimary" onClick={() => navigate("/trends")}>
              <span>start</span>
              <span className="main__go">→</span>
            </button>
          </div>
        </aside>
      </main>

      <footer className="main__footer">
        <div className="main__mini">
          <div className="main__miniIconBox">
            <img className="main__svgIcon" src="/icons/landing/group-6.svg" alt="Track trends icon" />
          </div>
          <div>
            <div className="main__miniTitle">Track gaming trends</div>
            <div className="main__miniText">real-time YouTube data</div>
          </div>
        </div>

        <div className="main__mini">
          <div className="main__miniIconBox">
            <img className="main__svgIcon" src="/icons/landing/group-10.svg" alt="Compare performance icon" />
          </div>
          <div>
            <div className="main__miniTitle">Compare performance</div>
            <div className="main__miniText">before/after switch</div>
          </div>
        </div>

        <div className="main__mini">
          <div className="main__miniIconBox">
            <img className="main__svgIcon" src="/icons/landing/group-26.svg" alt="Dashboard icon" />
          </div>
          <div>
            <div className="main__miniTitle">Clear dashboard</div>
            <div className="main__miniText">trends + predictions</div>
          </div>
        </div>

        <div className="main__mini">
          <div className="main__miniIconBox">
            <img className="main__svgIcon" src="/icons/landing/group-33.svg" alt="Maximize profit icon" />
          </div>
          <div>
            <div className="main__miniTitle">Maximize profit</div>
            <div className="main_miniText">data-driven decisions</div>
          </div>
        </div>
      </footer>
    </div>
  );
}
