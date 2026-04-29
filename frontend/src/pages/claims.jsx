import { useState, useEffect, useRef } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import "./claims.css";

const BASE = "https://165.232.136.214.sslip.io";

export default function Claims() {
  const navigate = useNavigate();
  const [genres, setGenres] = useState([]);
  const [fromGenre, setFromGenre] = useState(null);
  const [topGenre, setTopGenre] = useState(null);
  const [topGenreData, setTopGenreData] = useState(null);
  const [breakdown, setBreakdown] = useState(null);
  const [fromRevenue, setFromRevenue] = useState(null);
  const [topRevenue, setTopRevenue] = useState(null);
  const [loadingGenres, setLoadingGenres] = useState(true);
  const [loadingBreakdown, setLoadingBreakdown] = useState(false);
  const [loadingRevenue, setLoadingRevenue] = useState(false);
  const [errorBreakdown, setErrorBreakdown] = useState(null);
  const [errorRevenue, setErrorRevenue] = useState(null);
  const [fromDropdownOpen, setFromDropdownOpen] = useState(false);
  const [showRevenueInfo, setShowRevenueInfo] = useState(false);
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
        setTopGenreData(top ?? null);
        setFromGenre(data.find(g => g.views > 0)?.name ?? null);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoadingGenres(false));
  }, []);

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

  const totalEstimatedRevenue = topRevenue?.totalEstimatedRevenue ?? null;
  const baseRevenue = fromRevenue?.totalEstimatedRevenue ?? null;
  // Use trendBoost directly from API
  const trendBoost = (topRevenue && fromRevenue)
  ? topRevenue.totalEstimatedRevenue - fromRevenue.totalEstimatedRevenue
  : null;

  const profitMultiplier = (topRevenue && fromRevenue && fromRevenue.totalEstimatedRevenue > 0)
    ? (topRevenue.totalEstimatedRevenue / fromRevenue.totalEstimatedRevenue).toFixed(1)
    : breakdown?.performanceMultiplier ?? "—";

  const fromGenreData = genres.find((g) => g.name === fromGenre) ?? null;
  const toGenreLabel = breakdown?.toGenre ?? topGenre ?? "trend";

  const engagementRisk = fromGenreData && fromGenreData.views > 0
    ? (() => {
        const ratio = (fromGenreData.likes / fromGenreData.views) * 100;
        if (ratio >= 5) return { label: "Low Risk", color: "#62DF9C", detail: `${ratio.toFixed(1)}% like ratio` };
        if (ratio >= 2) return { label: "Medium Risk", color: "#F5B17B", detail: `${ratio.toFixed(1)}% like ratio` };
        return { label: "High Risk", color: "#FF7B7B", detail: `${ratio.toFixed(1)}% like ratio` };
      })()
    : null;

  const viewsIncrease = breakdown && fromGenreData && topGenreData && fromGenreData.views > 0
    ? Math.round(((topGenreData.views - fromGenreData.views) / fromGenreData.views) * 100)
    : 0;

  const breakdownRows = breakdown
    ? [
        {
          label: breakdown.cpmIncrease >= 0
            ? `higher CPM on ${toGenreLabel.toLowerCase()}`
            : `lower CPM on ${toGenreLabel.toLowerCase()}`,
          value: breakdown.cpmIncrease >= 0
            ? `+${breakdown.cpmIncrease}%`
            : `${breakdown.cpmIncrease}%`,
          type: breakdown.cpmIncrease >= 0 ? "positive" : "negative",
          percent: Math.min(Math.abs(breakdown.cpmIncrease), 100),
          icon: "↗️",
        },
        {
          label: (() => {
            const fromEng = fromGenreData?.views > 0
              ? `${((fromGenreData.likes / fromGenreData.views) * 100).toFixed(1)}%`
              : "no data";
            const toEng = topGenreData?.views > 0
              ? `${((topGenreData.likes / topGenreData.views) * 100).toFixed(1)}%`
              : "no data";
            return `engagement rate  (${fromEng} → ${toEng})`;
          })(),
          value: breakdown.engagementRateDiff > 0
            ? `+${breakdown.engagementRateDiff}%`
            : `${breakdown.engagementRateDiff}%`,
          percent: Math.min(Math.abs(breakdown.engagementRateDiff), 100),
          icon: "📈",
          type: breakdown.engagementRateDiff > 0 ? "positive" : "negative",
        },
        {
          label: (() => {
            const from = fromGenreData?.views > 0 ? fmtViews(fromGenreData.views) : "no data";
            const to = topGenreData?.views > 0 ? fmtViews(topGenreData.views) : "no data";
            return `avg monthly views  (${from} → ${to})`;
          })(),
          value: viewsIncrease > 999 ? "+999%+" : viewsIncrease >= 0 ? `+${viewsIncrease}%` : `${viewsIncrease}%`,
          percent: Math.min(Math.abs(viewsIncrease), 100),
          icon: "👥",
          type: viewsIncrease >= 0 ? "positive" : "negative",
        },
      ]
    : [];
    
  return (
    <div className="claims-page">
      <header className="claims-header">
        <NavLink to="/" className="main__brand">CreatorXP</NavLink>
        <div style={{ display: "flex", alignItems: "center", gap: "24px" }}>
          <nav className="main__nav">
            <NavLink to="/trends" className={({ isActive }) => (isActive ? "active" : "")}>Trends</NavLink>
            <NavLink to="/narratives" className={({ isActive }) => (isActive ? "active" : "")}>Narratives</NavLink>
            <NavLink to="/claims" className={({ isActive }) => (isActive ? "active" : "")}>Revenue</NavLink>
          </nav>
          <img src="/icons/claims/houseIcon.svg" alt="Home" className="claims-home-btn" onClick={() => navigate("/")} />
        </div>
      </header>

      <h1 className="claims-title">
        <span className="claims-title-accent">revenue &amp; performance</span>
      </h1>

      <div className="claims-grid">
        <section className="claims-card claims-card--left">
          <div className="claims-section-label">
            <img src="/icons/claims/coin.svg" alt="" className="claims-icon claims-icon--section" />
            <span>ESTIMATED TOTAL REVENUE ({topGenre?.toUpperCase() ?? "POST-ADOPTION"})</span>
            <button className="claims-info-btn" onClick={() => setShowRevenueInfo(true)}>?</button>
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
                  <button
                    type="button"
                    className={`claims-genre-trigger${fromDropdownOpen ? " claims-genre-trigger--open" : ""}`}
                    onClick={(e) => {
                      e.stopPropagation();
                      setFromDropdownOpen((o) => !o);
                    }}
                    aria-haspopup="listbox"
                    aria-expanded={fromDropdownOpen}
                  >
                    <span className="claims-genre-trigger__label">your genre</span>
                    <span className="claims-genre-trigger__value" style={{ textTransform: "capitalize" }}>{fromGenre.toLowerCase()}</span>
                    <span className="claims-genre-trigger__caret">{fromDropdownOpen ? "▴" : "▾"}</span>
                  </button>

                  {fromDropdownOpen && (
                    <ul className="trends-genre-dropdown claims-genre-dropdown" role="listbox">
                      {genres.filter(g => g.views > 0).map((g) => (
                        <li
                          key={g.name}
                          role="option"
                          aria-selected={g.name === fromGenre}
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
                <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end" }}>
                  <span style={{ fontSize: "12px", color: "#8E93AB", marginBottom: "2px" }}>current revenue</span>
                  <span>{fmt(baseRevenue)}</span>
                </div>
              </div>
              <div className="claims-divider" />

              <div className="claims-balance-card">
                <div className="claims-balance-label">trend boost</div>
                <div className="claims-balance-value-row">
                  <span className="claims-balance-value claims-positive">+{fmt(trendBoost)}</span>
                  <span className="claims-balance-status">pending</span>
                </div>
              </div>
            </>
          )}

          {/* Revenue info modal */}
          {showRevenueInfo && (
            <div className="claims-modal-overlay" onClick={() => setShowRevenueInfo(false)}>
              <div className="claims-modal" onClick={(e) => e.stopPropagation()}>
                <button className="claims-modal-close" onClick={() => setShowRevenueInfo(false)}>✕</button>
                <h3 className="claims-modal-title">How is this calculated?</h3>
                <p className="claims-modal-body">
                  <strong>Total Estimated Revenue</strong> is your estimated monthly earnings if you switch to the top trending genre ({topGenre}).
                  It's calculated using the genre's average views and an industry CPM of $3.50 per 1,000 views.
                </p>
                <p className="claims-modal-body">
                  <strong>Current Revenue</strong> is your estimated earnings staying in your current genre, using a base CPM of $2.00. You can change this genre using the dropdown.
                </p>
                <p className="claims-modal-body">
                  <strong>Trend Boost</strong> is the additional revenue you could earn by making the switch.
                </p>
              </div>
            </div>
          )}
        </section>

        <section className="claims-card claims-card--right">
          <div className="claims-section-label">
            <img src="/icons/claims/pie.svg" alt="" className="claims-icon claims-icon--section" />
            <span>BREAKDOWN: WHY TRANSITIONING TO {topGenre?.toUpperCase() ?? "TREND"} WORKS</span>
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
                      <span className={row.type === "positive" ? "claims-positive" : row.type === "negative" ? "claims-negative" : "claims-neutral"}>
                        {row.value}
                      </span>
                    </div>
                    {row.percent > 0 && (
                      <div className="claims-progress-track">
                        <div
                          className={`claims-progress-fill${row.type === "negative" ? " claims-progress-fill--negative" : ""}`}
                          style={{ width: `${row.percent}%` }}
                        />
                      </div>
                    )}
                    <div className="claims-divider" />
                  </div>
                ))}

                {engagementRisk && (
                  <div>
                    <div className="claims-row claims-row--spaced">
                      <span>⚠️ engagement risk</span>
                      <span style={{ color: engagementRisk.color, fontWeight: 600 }}>
                        {engagementRisk.label}
                      </span>
                    </div>
                    <div style={{ fontSize: "12px", color: "#8E93AB", marginBottom: "8px" }}>
                      {engagementRisk.detail} on {fromGenre?.toLowerCase()}
                    </div>
                    <div className="claims-divider" />
                  </div>
                )}
              </div>

              <div className="claims-last-paid-card">
                <img src="/icons/claims/check.svg" alt="" className="claims-icon claims-icon--file" />
                <div>
                  <div className="claims-last-paid-label">performance guarantee</div>
                  <div className="claims-last-paid-value">
                    trend adaptation yields avg. {profitMultiplier}x profit
                  </div>
                  {breakdown?.message && (
                    <div style={{ fontSize: "17px", color: "#b2b9d5", marginTop: "4px" }}>
                      {breakdown.message}
                    </div>
                  )}
                </div>
              </div>
            </>
          )}
        </section>
      </div>

      <footer className="claims-footer">
        <div className="claims-footer-note">
          <img src="/icons/claims/clock.svg" alt="" className="claims-icon claims-icon--footer" />
          <span></span>
        </div>
        <div className="claims-footer-note">
          <img src="/icons/claims/check.svg" alt="" className="claims-icon claims-icon--footer" />
          <span>genre-optimized earnings</span>
        </div>
      </footer>
    </div>
  );
}
