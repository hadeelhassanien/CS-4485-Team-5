import { useState, useEffect, useRef } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import "./claims.css";

const BASE = "https://165.232.136.214.sslip.io";

export default function Claims() {
  const navigate = useNavigate();
  const [genres, setGenres] = useState([]);
  const [fromGenre, setFromGenre] = useState("Horror");
  const [topGenre, setTopGenre] = useState(null);
  const [breakdown, setBreakdown] = useState(null);
  const [fromRevenue, setFromRevenue] = useState(null);
  const [topRevenue, setTopRevenue] = useState(null);
  const [loadingGenres, setLoadingGenres] = useState(true);
  const [loadingBreakdown, setLoadingBreakdown] = useState(false);
  const [loadingRevenue, setLoadingRevenue] = useState(false);
  const [errorBreakdown, setErrorBreakdown] = useState(null);
  const [errorRevenue, setErrorRevenue] = useState(null);
  const [claiming, setClaiming] = useState(false);
  const [fromDropdownOpen, setFromDropdownOpen] = useState(false);
  const fromDropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (fromDropdownRef.current && !fromDropdownRef.current.contains(e.target)) {
        setFromDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Fetch all genres and derive top trending genre
  useEffect(() => {
    fetch(`${BASE}/api/genres`)
      .then((res) => {
        if (!res.ok) throw new Error(`Genres error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        setGenres(data);
        const top = [...data].sort((a, b) => b.views - a.views)[0];
        setTopGenre(top?.name ?? null);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoadingGenres(false));
  }, []);

  // Fetch breakdown, fromRevenue, and topRevenue whenever fromGenre or topGenre changes
  useEffect(() => {
    if (!topGenre || !fromGenre) return;

    setLoadingBreakdown(true);
    setErrorBreakdown(null);
    fetch(
      `${BASE}/api/genres/breakdown?fromGenre=${encodeURIComponent(fromGenre)}&toGenre=${encodeURIComponent(topGenre)}`
    )
      .then((res) => {
        if (!res.ok) throw new Error(`Breakdown error: ${res.status}`);
        return res.json();
      })
      .then((data) => setBreakdown(data))
      .catch((err) => setErrorBreakdown(err.message))
      .finally(() => setLoadingBreakdown(false));

    setLoadingRevenue(true);
    setErrorRevenue(null);

    // Fetch both fromGenre and topGenre revenue in parallel
    Promise.all([
      fetch(`${BASE}/api/claims/revenue?genre=${encodeURIComponent(fromGenre)}`).then((res) => {
        if (!res.ok) throw new Error(`Revenue error: ${res.status}`);
        return res.json();
      }),
      fetch(`${BASE}/api/claims/revenue?genre=${encodeURIComponent(topGenre)}`).then((res) => {
        if (!res.ok) throw new Error(`Revenue error: ${res.status}`);
        return res.json();
      }),
    ])
      .then(([fromRev, topRev]) => {
        setFromRevenue(fromRev);
        setTopRevenue(topRev);
      })
      .catch((err) => setErrorRevenue(err.message))
      .finally(() => setLoadingRevenue(false));
  }, [fromGenre, topGenre]);

  const handleClaim = () => {
    if (!topGenre) return;
    setClaiming(true);
    fetch(`${BASE}/api/claims/claim?genre=${encodeURIComponent(topGenre)}`, {
      method: "POST",
    })
      .then((res) => {
        if (!res.ok) throw new Error(`Claim error: ${res.status}`);
        return res.json();
      })
      .then((data) => setTopRevenue(data))
      .catch((err) => setErrorRevenue(err.message))
      .finally(() => setClaiming(false));
  };

  const fmt = (n) => {
    if (n === undefined || n === null) return "—";
    return "$" + Number(n).toLocaleString("en-US", { minimumFractionDigits: 0, maximumFractionDigits: 0 });
  };

  const fmtViews = (n) => {
    if (!n) return "—";
    if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + "M";
    if (n >= 1_000) return (n / 1_000).toFixed(1) + "k";
    return String(n);
  };

  // Total estimated revenue = topGenre's total (what they could earn)
  // Base revenue = fromGenre's base (what they currently earn)
  // Trend boost = difference between topGenre total and fromGenre total
  const totalEstimatedRevenue = topRevenue?.totalEstimatedRevenue ?? null;
  const baseRevenue = fromRevenue?.baseRevenue ?? null;
  const trendBoost = (topRevenue && fromRevenue)
    ? Math.max(0, topRevenue.totalEstimatedRevenue - fromRevenue.totalEstimatedRevenue)
    : null;
  const unclaimedBalance = topRevenue?.unclaimedBalance ?? null;

  const profitMultiplier = (topRevenue && fromRevenue && fromRevenue.totalEstimatedRevenue > 0)
    ? (topRevenue.totalEstimatedRevenue / fromRevenue.totalEstimatedRevenue).toFixed(1)
    : breakdown?.performanceMultiplier ?? "—";

  const breakdownRows = breakdown
    ? [
        {
          label: `higher CPM on ${breakdown.toGenre?.toLowerCase() ?? topGenre?.toLowerCase()}`,
          value: `+${breakdown.cpmIncrease}%`,
          percent: Math.min(breakdown.cpmIncrease, 100),
          icon: "↗️",
          type: "positive",
        },
        {
          label: "engagement rate",
          value: breakdown.engagementRateDiff > 0
            ? `+${breakdown.engagementRateDiff}%`
            : `${breakdown.engagementRateDiff}%`,
          percent: Math.min(Math.abs(breakdown.engagementRateDiff), 100),
          icon: "📈",
          type: breakdown.engagementRateDiff > 0 ? "positive" : "neutral",
        },
        {
          label: "avg monthly views from trend",
          value: fmtViews(breakdown.avgMonthlyViews),
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
      <div style={{ display: "flex", alignItems: "center", gap: "24px" }}>
          <nav className="main__nav">
            <NavLink to="/trends" className={({ isActive }) => (isActive ? "active" : "")}>Trends</NavLink>
            <NavLink to="/narratives" className={({ isActive }) => (isActive ? "active" : "")}>Narratives</NavLink>
            <NavLink to="/" end className={({ isActive }) => (isActive ? "active" : "")}>Home</NavLink>
            <NavLink to="/claims" className={({ isActive }) => (isActive ? "active" : "")}>Claims</NavLink>
          </nav>
          <img src="/icons/claims/houseIcon.svg" alt="Home" className="claims-home-btn" onClick={() => navigate("/")} />
        </div>
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

          {loadingRevenue && <div className="claims-status-text">Loading revenue…</div>}
          {errorRevenue && <div className="claims-status-text claims-status-text--error">Failed to load: {errorRevenue}</div>}

          {!loadingRevenue && !errorRevenue && topRevenue && fromRevenue && (
            <>
              <div className="claims-amount">{fmt(totalEstimatedRevenue)}</div>

              <div className="claims-period">
                <img src="/icons/claims/calendar.svg" alt="" className="claims-icon claims-icon--inline" />
                <span>from {topRevenue.periodStart} - {topRevenue.periodEnd} (trend-driven videos)</span>
              </div>

              <div className="claims-row">
                <div className="trends-genre-dropdown-wrap" ref={fromDropdownRef}>
                  <div
                    className="trends-sub-card-label trends-sub-card-label--dropdown"
                    onClick={(e) => { e.stopPropagation(); setFromDropdownOpen((o) => !o); }}
                  >
                    {fromGenre.toLowerCase()} <span className="trends-dropdown-caret">{fromDropdownOpen ? "▴" : "▾"}</span>
                  </div>
                  {fromDropdownOpen && (
                    <ul className="trends-genre-dropdown">
                      {genres.map((g) => (
                        <li
                          key={g.name}
                          className={`trends-genre-dropdown-item${g.name === fromGenre ? " trends-genre-dropdown-item--active" : ""}`}
                          onClick={(e) => {
                            e.stopPropagation();
                            setFromGenre(g.name);
                            setFromDropdownOpen(false);
                          }}
                        >
                          {g.name}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
                <span>{fmt(baseRevenue)}</span>
              </div>
              <div className="claims-divider" />

              <div className="claims-row">
                <span>trend boost</span>
                <span className="claims-positive">+{fmt(trendBoost)}</span>
              </div>
              <div className="claims-divider" />

              <div className="claims-balance-card">
                <div className="claims-balance-label">unclaimed balance</div>
                <div className="claims-balance-value-row">
                  <span className="claims-balance-value">{fmt(unclaimedBalance)}</span>
                  <span className="claims-balance-status">pending</span>
                </div>
              </div>
            </>
          )}

          <button className="claims-cta-btn" type="button" onClick={handleClaim} disabled={claiming}>
            <span className="claims-cta-icon">$</span>
            {claiming ? "Claiming…" : "Claim earnings"}
          </button>
        </section>

        <section className="claims-card claims-card--right">
          <div className="claims-section-label">
            <img src="/icons/claims/pie.svg" alt="" className="claims-icon claims-icon--section" />
            <span>BREAKDOWN: WHY TREND WORKS</span>
          </div>

          {loadingBreakdown && <div className="claims-status-text">Loading breakdown…</div>}
          {errorBreakdown && <div className="claims-status-text claims-status-text--error">Failed to load: {errorBreakdown}</div>}

          {!loadingBreakdown && !errorBreakdown && (
            <>
              <div className="claims-breakdown-list">
                {breakdownRows.map((row) => (
                  <div key={row.label}>
                    <div className="claims-row claims-row--spaced">
                      <span>{row.icon} {row.label}</span>
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
                  avg. {profitMultiplier}x profit (based on your history)
                </span>
              </p>

              {breakdown?.message && (
                <p className="claims-muted-strong" style={{ marginTop: "0.5rem", fontSize: "0.8rem" }}>
                  {breakdown.message}
                </p>
              )}
            </>
          )}

          <div className="claims-last-paid-card">
            <img src="/icons/claims/file.svg" alt="" className="claims-icon claims-icon--file" />
            <div>
              <div className="claims-last-paid-label">last claim paid</div>
              <div className="claims-last-paid-value">
                {topRevenue?.lastClaimDate ?? "—"} &middot; {fmt(topRevenue?.lastClaimAmount)}
              </div>
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