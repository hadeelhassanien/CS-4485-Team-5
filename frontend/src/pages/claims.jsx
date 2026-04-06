import { useNavigate } from "react-router-dom";
import "./claims.css";

const breakdownRows = [
  { label: "higher CPM on battle royale", value: "+42%", percent: 83, icon: "↗️", type: "positive" },
  { label: "engagement rate", value: "+27%", percent: 70, icon: "📈", type: "positive" },
  { label: "new subscribers from trend", value: "1,230", percent: 0, icon: "👥", type: "neutral" },
];

export default function Claims() {
  const navigate = useNavigate();

  return (
    <div className="claims-page">
      <header className="claims-header">
        <img src="/icons/landing/creatorXP.svg" alt="CreatorXP" className="claims-brand-logo" />
        <img
          src="/icons/claims/houseIcon.svg"
          alt="Home"
          className="claims-home-btn"
          onClick={() => navigate("/")}
        />
      </header>

      <h1 className="claims-title">
        Claims <span className="claims-title-accent">&middot; revenue &amp; performance</span>
      </h1>

      <div className="claims-grid">
        <section className="claims-card claims-card--left">
          <div className="claims-section-label">
            <img src="/icons/claims/coin.svg" alt="" className="claims-icon claims-icon--section" />
            <span>ESTIMATED ADDITIONAL REVENUE (TREND)</span>
          </div>

          <div className="claims-amount">$3,842</div>

          <div className="claims-period">
            <img src="/icons/claims/calendar.svg" alt="" className="claims-icon claims-icon--inline" />
            <span>from Mar 1 - Mar 31 (trend-driven videos)</span>
          </div>

          <div className="claims-row">
            <span>non-trend revenue</span>
            <span>$1,210</span>
          </div>
          <div className="claims-divider" />

          <div className="claims-row">
            <span>trend boost</span>
            <span className="claims-positive">+$2,632</span>
          </div>
          <div className="claims-divider" />

          <div className="claims-balance-card">
            <div className="claims-balance-label">unclaimed balance</div>
            <div className="claims-balance-value-row">
              <span className="claims-balance-value">$1,947</span>
              <span className="claims-balance-status">pending</span>
            </div>
          </div>

          <button className="claims-cta-btn" type="button">
            <span className="claims-cta-icon">$</span>
            Claim earnings
          </button>
        </section>

        <section className="claims-card claims-card--right">
          <div className="claims-section-label">
            <img src="/icons/claims/pie.svg" alt="" className="claims-icon claims-icon--section" />
            <span>BREAKDOWN: WHY TREND WORKS</span>
          </div>

          <div className="claims-breakdown-list">
            {breakdownRows.map((row) => (
              <div key={row.label}>
                <div className="claims-row claims-row--spaced">
                  <span>
                    {row.icon} {row.label}
                  </span>
                  <span className={row.type === "positive" ? "claims-positive" : "claims-neutral"}>
                    {row.value}
                  </span>
                </div>
                {row.percent > 0 && (
                  <div className="claims-progress-track">
                    <div className="claims-progress-fill" style={{ width: `${row.percent}%` }} />
                  </div>
                )}
                <div className="claims-divider" />
              </div>
            ))}
          </div>

          <p className="claims-guarantee">
            <img src="/icons/claims/check.svg" alt="" className="claims-icon claims-icon--inline claims-guarantee-icon" />
            <strong>performance guarantee:</strong> trend adaptation yields
            <br />
            <span className="claims-muted-strong">avg. 2.3x profit (based on your history)</span>
          </p>

          <div className="claims-last-paid-card">
            <img src="/icons/claims/file.svg" alt="" className="claims-icon claims-icon--file" />
            <div>
              <div className="claims-last-paid-label">last claim paid</div>
              <div className="claims-last-paid-value">April 2, 2025 &middot; $2,410</div>
            </div>
          </div>
        </section>
      </div>

      <footer className="claims-footer">
        <div className="claims-footer-note">
          <img src="/icons/claims/clock.svg" alt="" className="claims-icon claims-icon--footer" />
          <span>funds clear within 48h after claim</span>
        </div>
        <div className="claims-footer-note">
          <img src="/icons/claims/check.svg" alt="" className="claims-icon claims-icon--footer" />
          <span>trend-proof earnings</span>
        </div>
      </footer>
    </div>
  );
}
