import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./claims.css";

const BASE = "https://cleistogamously-phosphaturic-marlen.ngrok-free.dev";
const FROM_GENRE = "Horror";
const TO_GENRE = "Battle Royale";
const FALLBACK_BREAKDOWN = {
  fromGenre: FROM_GENRE,
  toGenre: TO_GENRE,
  cpmIncrease: 42,
  engagementRateDiff: 27,
  avgMonthlyViews: 1230,
  performanceMultiplier: 2.3,
  recommendedGenre: TO_GENRE,
  message: `${TO_GENRE} is a better genre for revenue growth`,
};

function formatSignedPercent(value) {
  if (value > 0) return `+${value}%`;
  return `${value}%`;
}

function formatNumber(value) {
  return new Intl.NumberFormat("en-US").format(value);
}

export default function Claims() {
  const navigate = useNavigate();
  const [breakdown, setBreakdown] = useState(FALLBACK_BREAKDOWN);
  const [loadingBreakdown, setLoadingBreakdown] = useState(true);
  const [breakdownError, setBreakdownError] = useState(null);

  useEffect(() => {
    let isMounted = true;

    async function loadBreakdown() {
      try {
        setLoadingBreakdown(true);
        setBreakdownError(null);

        const params = new URLSearchParams({
          fromGenre: FROM_GENRE,
          toGenre: TO_GENRE,
        });

        const response = await fetch(`${BASE}/api/genres/breakdown?${params.toString()}`, {
          headers: { "ngrok-skip-browser-warning": "true" },
        });

        if (!response.ok) {
          throw new Error(`Breakdown error: ${response.status}`);
        }

        const data = await response.json();

        if (isMounted) {
          setBreakdown(data);
        }
      } catch (error) {
        if (isMounted) {
          setBreakdownError(error.message);
        }
      } finally {
        if (isMounted) {
          setLoadingBreakdown(false);
        }
      }
    }

    loadBreakdown();

    return () => {
      isMounted = false;
    };
  }, []);

  const cpmPercent = Math.min(Math.abs(breakdown?.cpmIncrease ?? 0), 100);
  const engagementPercent = Math.min(Math.abs(breakdown?.engagementRateDiff ?? 0), 100);

  const breakdownRows = breakdown
    ? [
        {
          label: `higher CPM on ${breakdown.toGenre.toLowerCase()}`,
          value: formatSignedPercent(breakdown.cpmIncrease),
          percent: cpmPercent,
          icon: "↗️",
          type: breakdown.cpmIncrease > 0 ? "positive" : "neutral",
        },
        {
          label: "engagement rate",
          value: formatSignedPercent(breakdown.engagementRateDiff),
          percent: engagementPercent,
          icon: "📈",
          type: breakdown.engagementRateDiff > 0 ? "positive" : "neutral",
        },
        {
          label: `avg monthly views on ${breakdown.toGenre.toLowerCase()}`,
          value: formatNumber(breakdown.avgMonthlyViews),
          percent: 0,
          icon: "👥",
          type: "neutral",
        },
      ]
    : [];

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
            {loadingBreakdown && <div className="claims-row"><span>Loading breakdown…</span></div>}

            {!loadingBreakdown && breakdownError && (
              <div className="claims-row">
                <span>Using saved breakdown values</span>
                <span className="claims-neutral">live API unavailable</span>
              </div>
            )}

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
            <span className="claims-muted-strong">
              avg. {breakdown ? `${breakdown.performanceMultiplier.toFixed(1)}x` : "2.3x"} profit
              {" "}
              (based on your history)
            </span>
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
