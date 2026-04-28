import { useState, useEffect, useRef } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import "./trends.css";

// Maps genre names returned by the API to their SVG icon paths.
// Genres without an icon yet will show no icon.
const GENRE_ICONS = {
  "Battle Royale":  "/icons/trends/battleRoyale.svg",
  "Survival Craft": "/icons/trends/survivalCraft.svg",
  "Sports Sim":     "/icons/trends/sportsSim.svg",
  "Horror":         "/icons/trends/horror.svg",
  "Party / Casual": "/icons/trends/party.svg",
  "Racing":      "/icons/trends/horror.svg",
  "Simulation":  "/icons/trends/horror.svg",
  "Puzzle":      "/icons/trends/horror.svg",
  "Shooter":     "/icons/trends/horror.svg",
  "Action":      "/icons/trends/horror.svg",
};

// Formats a raw number into a string with M or K as million or thousand  
function formatNumber(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(2).replace(/\.?0+$/, "") + "M";
  if (n >= 1_000)     return (n / 1_000).toFixed(1).replace(/\.?0+$/, "") + "k";
  return String(n);
}

// Returns a signed percentage string e.g. +70%, -55%, 0%
function formatChange(change) {
  const pct = Math.round(change * 100);
  if (pct > 0) return `+${pct}%`;
  if (pct < 0) return `${pct}%`;
  return `0%`;
}

const MAX_BAR_HEIGHT = 110; // tallest bar height

const BASE = "https://165.232.136.214.sslip.io";

export default function Trends() {
  const navigate = useNavigate();

  const [showPerfInfo, setShowPerfInfo] = useState(false);
  const [showLeftInfo, setShowLeftInfo] = useState(false);
  const [selectedBlock, setSelectedBlock]           = useState("before");
  const [beforeGenre, setBeforeGenre]               = useState("Shooter");
  const [beforeDropdownOpen, setBeforeDropdownOpen] = useState(false);
  const [genres, setGenres]                         = useState([]);
  const [predicted, setPredicted]                   = useState([]);
  const [reco, setReco]                             = useState(null);
  const [loading, setLoading]                       = useState(true);
  const [error, setError]                           = useState(null);
  const beforeDropdownRef = useRef(null);

  useEffect(() => {
      function handleClickOutside(e) {
        if (beforeDropdownRef.current && !beforeDropdownRef.current.contains(e.target)) {
          setBeforeDropdownOpen(false);
        }
      }
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []); 

  useEffect(() => {
    fetch(`${BASE}/api/genres`, {
      headers: { "ngrok-skip-browser-warning": "true" },
    })
      .then((res) => {
        if (!res.ok) throw new Error(`Genres error: ${res.status}`);
        return res.json();
      })
      .then((genresData) => setGenres(genresData))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));

    fetch(`${BASE}/api/genres/predicted`, {
      headers: { "ngrok-skip-browser-warning": "true" },
    })
      .then((res) => { if (!res.ok) return null; return res.json(); })
      .then((data) => { if (data) setPredicted(data); })
      .catch(() => console.warn("Predicted endpoint not available yet"));

    fetch(`${BASE}/api/genres/recommendation`, {
      headers: { "ngrok-skip-browser-warning": "true" },
    })
      .then((res) => { if (!res.ok) return null; return res.json(); })
      .then((data) => { if (data) setReco(data); })
      .catch(() => console.warn("Recommendation endpoint not available yet"));
  }, []);

  // All genres with derived display fields — no slice, show every genre
  const maxViews = genres.length ? Math.max(...genres.map((g) => g.views)) : 1;
  const genreData = genres
    .filter((g) => g.views > 0)
    .map((g) => {
      const change = Number(g.changePercent) || 0;
      return {
        ...g,
        changePercent: change,
        percent: Math.round((g.views / maxViews) * 100),
        icon: GENRE_ICONS[g.name] ?? null,
        changeDisplay: formatChange(change),
      };
    })
    .sort((a, b) => b.views - a.views);

  const beforeGenreData = genres.find((g) => g.name === beforeGenre) ?? null;
  const afterGenreData  = genreData[0] ?? null; // top trending genre by views

  // Top 2 predicted genres for "next month peak"
const topByChange = predicted.length
  ? [...predicted]
      .sort((a, b) => b.predictedViews - a.predictedViews)
      .slice(0, 2)
      .map((p) => ({ ...p, changePercent: p.predictedGrowthPercent }))
  : [...genreData].sort((a, b) => b.views - a.views).slice(0, 2);

  // Growth diff for recommendation card
  const growthDiff = reco
    ? reco.predictedGrowth
    : (() => {
        const topChangeVal    = topByChange[0] ? topByChange[0].changePercent : 0;
        const beforeChangeVal = beforeGenreData ? Number(beforeGenreData.changePercent) || 0 : 0;
        return Math.round((topChangeVal - beforeChangeVal) * 100);
      })();

  // Viewership uplift from switching before → after
  const beforeViews = beforeGenreData?.views || 0;
  const afterViews  = afterGenreData?.views  || 0;
  const viewsUplift = beforeViews > 0
    ? Math.round(((afterViews - beforeViews) / beforeViews) * 100)
    : 0;

  const barSource = predicted.length
  ? [...predicted].sort((a, b) => b.predictedViews - a.predictedViews).slice(0, 5)
  : [...genreData].sort((a, b) => b.views - a.views).slice(0, 5);

  const maxBarVal = barSource.length
    ? Math.max(...barSource.map((g) => (predicted.length ? g.predictedViews || 1 : g.changePercent)), 1)
    : 1;

  const barData = barSource.map((g) => ({
    label: g.name.split(" ")[0],
    height: Math.round(
      ((predicted.length ? g.predictedViews || 0 : g.changePercent) / maxBarVal) * MAX_BAR_HEIGHT
    ),
  }));

  // Stats for selected block
  const activeGenreData =
    selectedBlock === "before" ? beforeGenreData :
    selectedBlock === "after"  ? afterGenreData  :
    null;

  const statViews     = activeGenreData ? formatNumber(activeGenreData.views)    : "—";
  const statComments  = activeGenreData ? formatNumber(activeGenreData.comments) : "—";
  const statLikeRatio = activeGenreData && activeGenreData.views
    ? ((activeGenreData.likes / activeGenreData.views) * 100).toFixed(1) + "%"
    : "—";

  const statRows = [
    { icon: "/icons/trends/eye.svg",      label: "Avg. views", sub: "(30d)", value: statViews      },
    { icon: "/icons/trends/heart.svg",    label: "Like ratio",               value: statLikeRatio  },
    { icon: "/icons/trends/comments.svg", label: "Comments",                 value: statComments   },
  ];

  return (
    <div className="claims-page">
      <header className="claims-header">
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


      <h1 className="trends-title">Trends · gaming genres</h1>

      <div className="trends-grid">
        {/* ── Left: Top Trending Genres ── */}
        <div className="trends-card trends-card--left">
          <div className="trends-card-header">
            <img src="/icons/trends/topTrendingGenres.svg" alt="" className="trends-icon-sm" style={{ width: "48px", height: "48px" }} />            <span className="trends-card-header-text">TOP TRENDING GENRES (BY VIEWS)</span>
            <button className="claims-info-btn" style={{ marginLeft: "auto" }} onClick={() => setShowLeftInfo(true)}>?</button>
          </div>

          {showLeftInfo && (
            <div className="claims-modal-overlay" onClick={() => setShowLeftInfo(false)}>
              <div className="claims-modal" onClick={(e) => e.stopPropagation()}>
                <button className="claims-modal-close" onClick={() => setShowLeftInfo(false)}>✕</button>
                <h3 className="claims-modal-title">How to read this chart</h3>
                <p className="claims-modal-body">
                  <strong style={{ color: "#9E77ED" }}>Purple bar</strong> shows each genre's share of total viewership relative to the top genre.
                </p>
                <p className="claims-modal-body">
                  <strong style={{ color: "#27d65c" }}>Green bar</strong> shows the genre's viewership trend over the past 30 days — how fast it's growing.
                </p>
                <p className="claims-modal-body">
                  <strong style={{ color: "#d83333" }}>Red bar</strong> indicates a declining viewership trend.
                </p>
              </div>
            </div>
          )}

          {loading && <div className="trends-status-text">Loading genres…</div>}
          {error   && <div className="trends-status-text trends-status-text--error">Failed to load: {error}</div>}

          {!loading && !error && (
            <div className="trends-genre-list">
              {genreData.map((g) => (
                <div key={g.name}>
                  <div className="trends-genre-meta">
                    <div className="trends-genre-name-group">
                      {g.icon && <img src={g.icon} alt="" className="trends-icon-sm" />}
                      <span className="trends-genre-name">{g.name}</span>
                    </div>
                  </div>
                  {/* Views bar */}
                  <div className="trends-bar-track">
                    <div className="trends-bar-fill" style={{ width: `${g.percent}%`, minWidth: g.percent > 0 ? "36px" : "0" }}>
                      <span className="trends-bar-fill-text">{g.percent}%</span>
                    </div>
                  </div>
                  {/* Change percent bar */}
                  {(() => {
                    const changePct = Math.min(Math.abs(Math.round(g.changePercent * 100)), 100);
                    return (
                      <div className="trends-bar-track trends-bar-track--change">
                        <div
                          className={`trends-bar-fill trends-bar-fill--change${g.changePercent < 0 ? " trends-bar-fill--negative-change" : ""}`}
                          style={{ width: `${changePct}%`, minWidth: "40px" }}
                        >
                          <span className="trends-bar-fill-text">{g.changeDisplay}</span>
                        </div>
                      </div>
                    );
                  })()}
                </div>
              ))}
            </div>
          )}
        <div className="trends-footer-note">
          <img src="/icons/trends/bellIcon.svg" alt="" className="trends-icon-sm" />
          <div>
            <div className="trends-footer-note-text">
              {genreData[0]?.name?.toLowerCase() ?? "—"} + {genreData[1]?.name?.toLowerCase() ?? "—"} are peaking
            </div>
            <div className="trends-footer-note-text">— recommended switch</div>
          </div>
        </div>
        </div>
        {/* ── Mid: Performance vs Trend Shift ── */}
        <div className="trends-card trends-card--mid">
          <div className="trends-section-label" style={{ display: "flex", alignItems: "center" }}>
            <span style={{ flex: 1, textAlign: "center" }}>PERFORMANCE VS TREND SHIFT</span>
            <button className="claims-info-btn" onClick={() => setShowPerfInfo(true)}>?</button>
          </div>

          {showPerfInfo && (
            <div className="claims-modal-overlay" onClick={() => setShowPerfInfo(false)}>
              <div className="claims-modal" onClick={(e) => e.stopPropagation()}>
                <button className="claims-modal-close" onClick={() => setShowPerfInfo(false)}>✕</button>
                <h3 className="claims-modal-title">How does this work?</h3>
                <p className="claims-modal-body">
                  <strong>Before</strong> shows your current genre's monthly views and trend direction. Select your genre from the dropdown to compare.
                </p>
                <p className="claims-modal-body">
                  <strong>After</strong> shows the top trending genre's views — where the audience is moving right now.
                </p>
                <p className="claims-modal-body">
                  <strong>Trend adoption payout</strong> is the estimated viewership uplift you'd gain by switching to the top genre.
                </p>
              </div>
            </div>
          )}
          {/* Before card */}
          <div
            className={`trends-before-card${selectedBlock === "before" ? " trends-perf-card--active" : ""}`}
            onClick={() => setSelectedBlock(selectedBlock === "before" ? null : "before")}
          >
            <div className="trends-genre-dropdown-wrap" ref={beforeDropdownRef}>
              <div
                className="trends-sub-card-label trends-sub-card-label--dropdown"
                onClick={(e) => { e.stopPropagation(); setBeforeDropdownOpen((o) => !o); }}
              >
                Before ({beforeGenre?.toLowerCase() ?? "..."}) <span className="trends-dropdown-caret">{beforeDropdownOpen ? "▴" : "▾"}</span>
              </div>
              {beforeDropdownOpen && (
                <ul className="trends-genre-dropdown">
                  {genres.filter(g => g.views > 0).map((g) => (
                    <li
                      key={g.name}
                      className={`trends-genre-dropdown-item${g.name === beforeGenre ? " trends-genre-dropdown-item--active" : ""}`}
                      onClick={(e) => { e.stopPropagation(); setBeforeGenre(g.name); setBeforeDropdownOpen(false); }}
                    >
                      {g.name}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <div className="trends-value-row">
              <span className="trends-value--orange">{beforeGenreData ? formatNumber(beforeGenreData.views) : "—"}</span>
              <span className="trends-value-unit">views</span>
            </div>
            {/**/}
            <div className="trends-engagement-down">
              {beforeGenreData ? ` viewership trend ${formatChange(Number(beforeGenreData.changePercent) || 0)}` : ""}
            </div>
          </div>

          {/* After card */}
          <div
            className={`trends-after-card${selectedBlock === "after" ? " trends-perf-card--active" : ""}`}
            onClick={() => setSelectedBlock(selectedBlock === "after" ? null : "after")}
          >
            <div className="trends-sub-card-label trends-sub-card-label--after">
              After ({afterGenreData ? afterGenreData.name.toLowerCase() : "—"})
            </div>
            <div className="trends-value-row">
              <span className="trends-value--purple">{afterGenreData ? formatNumber(afterGenreData.views) : "—"}</span>
              <span className="trends-value-unit">views</span>
            </div>
            <div className="trends-engagement-up">
              {afterGenreData ? `viewership trend ${formatChange(afterGenreData.changePercent || 0)}` : ""}
            </div>
          </div>

          {/* Stats rows */}
          <div className="trends-stats-list">
            {statRows.map((row, i) => (
              <div key={i}>
                <div className="trends-stat-row">
                  <div className="trends-stat-left">
                    <img src={row.icon} alt="" className="trends-icon-sm" />
                    <span className="trends-stat-label">
                      {row.label}
                      {row.sub && <span className="trends-stat-sub">{row.sub}</span>}
                    </span>
                  </div>
                  <div className="trends-stat-values">
                    <span className="trends-stat-after">
                      {loading ? "…" : error ? "—" : row.value}
                    </span>
                  </div>
                </div>
                {i < statRows.length - 1 && <div className="trends-divider" />}
              </div>
            ))}
          </div>
            {/* Trend adoption payout */}
            <div className="trends-payout-card">
              <img src="/icons/trends/checkmark.svg" alt="" className="trends-icon-md" />
              <div>
                <div className="trends-payout-text">{afterGenreData?.name ?? "trend"} adoption payout:</div>
                <div className="trends-payout-text">{viewsUplift >= 0 ? `+${viewsUplift}%` : `${viewsUplift}%`} estimated viewership</div>
              </div>
            </div>
        </div>

        {/* ── Right: Future Trends (Predicted) ── */}
        <div className="trends-card trends-card--right">
          <div className="trends-section-label">FUTURE TRENDS (PREDICTED)</div>

          <div className="trends-bar-chart">
            {barData.map((b) => (
              <div key={b.label} className="trends-bar-col">
                <div className="trends-bar-rect" style={{ height: b.height }} />
                <span className="trends-bar-col-label">{b.label}</span>
              </div>
            ))}
          </div>

          <div className="trends-next-peak">
            <img src="/icons/trends/nextMonthPeak.svg" alt="" className="trends-icon-md" />
            <div>
              <div className="trends-next-peak-sub">next month peak:</div>
              <div className="trends-next-peak-main">
                {reco ? (
                  <>
                    {reco.nextMonthPeak.split(" / ")[0]} /{" "}
                    <span className="trends-next-peak-light">{(reco.nextMonthPeak.split(" / ")[1] ?? "").toLowerCase()}</span>
                  </>
                ) : (
                  <>
                    {topByChange[0]?.name ?? "—"} /{" "}
                    <span className="trends-next-peak-light">{topByChange[1]?.name?.toLowerCase() ?? "—"}</span>
                  </>
                )}
              </div>
            </div>
          </div>

          <div className="trends-reco-card">
            <div className="trends-reco-header">
              <img src="/icons/trends/gamePad.svg" alt="" className="trends-icon-md" />
              <div>
                <div className="trends-reco-title">PERSONALIZED</div>
                <div className="trends-reco-title">RECOMMENDATION</div>
              </div>
            </div>
            <p className="trends-reco-body">
              {reco ? (
                reco.message
              ) : (
                <>
                  your top genre was <strong>{beforeGenre?.toLowerCase() ?? "—"}</strong> — shift to{" "}
                  <strong>{topByChange[0]?.name?.toLowerCase() ?? "—"}</strong> now matches predicted{" "}
                  <strong>{growthDiff > 0 ? `+${growthDiff}%` : `${growthDiff}%`} growth.</strong>
                </>
              )}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}