import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "./trends.css";

// Maps genre names returned by the API to their SVG icon paths.
// Genres without an icon yet will show no icon.
const GENRE_ICONS = {
  "Battle Royale":  "/icons/trends/battleRoyale.svg",
  "Survival Craft": "/icons/trends/survivalCraft.svg",
  "Sports Sim":     "/icons/trends/sportsSim.svg",
  "Horror":         "/icons/trends/horror.svg",
  "Party / Casual": "/icons/trends/party.svg",
  "Racing":      null,
  "Simulation":  null,
  "Puzzle":      null,
  "Shooter":     null,
  "Action":      null,
};

// Formats a raw number into a short human-readable string (e.g. 1240000 → "1.24M")
function formatNumber(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(2).replace(/\.?0+$/, "") + "M";
  if (n >= 1_000)     return (n / 1_000).toFixed(1).replace(/\.?0+$/, "") + "k";
  return String(n);
}

const MAX_BAR_HEIGHT = 110; // px — tallest bar in the chart

const tags = [
  { label: "Valorant"       },
  { label: "Minecraft"      },
  { label: "Fortnite"       },
  { label: "FIFA"           },
  { label: "Lethal Company" },
];

const PLACEHOLDER_GENRES = [
  { name: "Battle Royale",  views: 4800000, likes: 320000, comments: 41200, changePercent: 0.48 },
  { name: "Survival Craft", views: 3900000, likes: 260000, comments: 33800, changePercent: 0.92 },
  { name: "Sports Sim",     views: 2700000, likes: 154000, comments: 19400, changePercent: 0.57 },
  { name: "Horror",         views: 2100000, likes: 118000, comments: 15600, changePercent: 0.43 },
  { name: "Shooter",        views: 1850000, likes: 102000, comments: 13200, changePercent: 0.38 },
  { name: "Action",         views: 1600000, likes:  88000, comments: 11400, changePercent: 0.31 },
  { name: "Racing",         views: 9300000, likes:  71000, comments:  9200, changePercent: 0.24 },
  { name: "Party / Casual", views: 1100000, likes:  59000, comments:  7800, changePercent: 0.22 },
  { name: "Simulation",     views:  820000, likes:  43000, comments:  5600, changePercent: 0.15 },
  { name: "Puzzle",         views:  540000, likes:  27000, comments:  3400, changePercent: 0.09 },
];

export default function Trends() {
  const navigate = useNavigate();
  const [activeTag, setActiveTag]           = useState("Valorant");
  const [selectedBlock, setSelectedBlock]   = useState(null); // 'before' | 'after' | null
  const [beforeGenre, setBeforeGenre]       = useState("Horror");
  const [beforeDropdownOpen, setBeforeDropdownOpen] = useState(false);
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

  // Raw genre data from the API
  // Expected shape per item: { name, views, likes, comments, changePercent }
  const [genres, setGenres]   = useState(PLACEHOLDER_GENRES);
  const [loading] = useState(false);
  const [error]   = useState(null);

  useEffect(() => {
    fetch("/api/trends/genres")
      .then((res) => {
        if (!res.ok) throw new Error(`Server error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        setGenres(data);
      })
      .catch(() => {
        // Backend not yet available — placeholder data stays visible
      });
  }, []);

  // Normalize name casing (`name` vs `Name`) so placeholder/API payloads both work.
  const normalizedGenres = genres.map((g) => ({
    ...g,
    name: g.name ?? g.Name ?? "",
  }));

  // Derive percentages relative to the genre with the most views
  const maxViews = normalizedGenres.length ? Math.max(...normalizedGenres.map((g) => g.views)) : 1;
  const genreData = normalizedGenres
    .map((g) => {
      const change = Number(g.changePercent) || 0;
      return {
        ...g,
        changePercent: change,
        percent: Math.round((g.views / maxViews) * 100),
        icon: GENRE_ICONS[g.name] ?? null,
        changeDisplay: `+${Math.round(change * 100)}%`,
      };
    })
    .sort((a, b) => b.views - a.views)
    .slice(0, 5);

  const beforeGenreData = genreData.find((g) => g.name === beforeGenre) ?? null;
  const afterGenreData  = genreData[0] ?? null; // top trending genre

  // Top 2 genres by changePercent for the "next month peak" prediction
  const topByChange = [...genreData]
    .sort((a, b) => b.changePercent - a.changePercent)
    .slice(0, 2);

  // Personalized recommendation — difference between top genre's growth and the before genre's growth
  const topChangeVal    = topByChange[0] ? topByChange[0].changePercent : 0;
  const beforeChangeVal = beforeGenreData ? (Number(beforeGenreData.changePercent) || 0) : 0;
  const growthDiff      = Math.round((topChangeVal - beforeChangeVal) * 100);

  // Viewership uplift from switching before → after genre
  const beforeViews = beforeGenreData?.views || 0;
  const afterViews  = afterGenreData?.views  || 0;
  const viewsUplift = beforeViews > 0
    ? Math.round(((afterViews - beforeViews) / beforeViews) * 100)
    : 0;

  // Bar chart heights derived from top-5 changePercent values
  const maxChange = genreData.length
    ? Math.max(...genreData.map((g) => g.changePercent))
    : 1;
  const barData = genreData.map((g) => ({
    label: g.name.split(" ")[0],
    height: Math.round((g.changePercent / maxChange) * MAX_BAR_HEIGHT),
  }));

  // Stats shown at the bottom of the middle card — reflect the active block's genre
  const activeGenreData =
    selectedBlock === "before" ? beforeGenreData :
    selectedBlock === "after"  ? afterGenreData  :
    null;

  const statViews    = activeGenreData ? formatNumber(activeGenreData.views)    : "—";
  const statComments = activeGenreData ? formatNumber(activeGenreData.comments) : "—";
  const statLikeRatio = activeGenreData && activeGenreData.views
    ? ((activeGenreData.likes / activeGenreData.views) * 100).toFixed(1) + "%"
    : "—";

  const statRows = [
    { icon: "/icons/trends/eye.svg",      label: "Avg. views", sub: "(30d)", value: statViews      },
    { icon: "/icons/trends/heart.svg",    label: "Like ratio",               value: statLikeRatio  },
    { icon: "/icons/trends/comments.svg", label: "Comments",                 value: statComments   },
  ];

  return (
    <div className="trends-page">
      {/* Header */}
      <div className="trends-header">
        <img src="/icons/trends/creatorXP.svg" alt="CreatorXP" className="trends-brand-logo" />
        <img src="/icons/trends/houseIcon.svg" alt="Home" className="trends-icon-md trends-home-btn" onClick={() => navigate("/")} />
      </div>

      <h1 className="trends-title">Trends · gaming genres</h1>

      {/* Three column layout */}
      <div className="trends-grid">
        {/* ===== LEFT CARD: Top Trending Genres ===== */}
        <div className="trends-card trends-card--left">
          <div className="trends-card-header">
            <img src="/icons/trends/topTrendingGenres.svg" alt="" className="trends-icon-sm" />
            <span className="trends-card-header-text">TOP TRENDING GENRES (30D)</span>
          </div>

          {/* Genre rows */}
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
                    <span className="trends-genre-badge">{g.changeDisplay}</span>
                  </div>
                  <div className="trends-bar-track">
                    {/* width is dynamic — kept as inline style */}
                    <div className="trends-bar-fill" style={{ width: `${g.percent}%` }}>
                      <span className="trends-bar-fill-text">{g.percent}%</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Game tags */}
          <div className="trends-tags">
            {tags.map((t) => (
              <button
                key={t.label}
                onClick={() => setActiveTag(t.label)}
                className={`trends-tag-btn${activeTag === t.label ? " trends-tag-btn--active" : ""}`}
              >
                {t.label}
              </button>
            ))}
          </div>

          {/* Footer note */}
          <div className="trends-footer-note">
            <img src="/icons/trends/bellIcon.svg" alt="" className="trends-icon-sm" />
            <div>
              <div className="trends-footer-note-text">battle royale + survival are peaking</div>
              <div className="trends-footer-note-text">— recommended switch</div>
            </div>
          </div>
        </div>

        {/* ===== MIDDLE CARD: Performance vs Trend Shift ===== */}
        <div className="trends-card trends-card--mid">
          <div className="trends-section-label">PERFORMANCE VS TREND SHIFT</div>

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
                Before ({beforeGenre.toLowerCase()}) <span className="trends-dropdown-caret">{beforeDropdownOpen ? "▴" : "▾"}</span>
              </div>
              {beforeDropdownOpen && (
                <ul className="trends-genre-dropdown">
                  {normalizedGenres.map((g) => (
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
            <div className="trends-engagement-down">{beforeGenreData ? `+${Math.round((Number(beforeGenreData.changePercent) || 0) * 100)}% trend` : ""}</div>
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
            <div className="trends-engagement-up">{afterGenreData ? `↑ +${Math.round((afterGenreData.changePercent || 0) * 100)}%` : ""}</div>
          </div>

          {/* Stats rows — driven by live genre data */}
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
              <div className="trends-payout-text">trend adoption payout:</div>
              <div className="trends-payout-text">{viewsUplift >= 0 ? `+${viewsUplift}%` : `${viewsUplift}%`} estimated viewership</div>
            </div>
          </div>
        </div>

        {/* ===== RIGHT CARD: Future Trends (Predicted) ===== */}
        <div className="trends-card trends-card--right">
          <div className="trends-section-label">FUTURE TRENDS (PREDICTED)</div>

          {/* Bar chart */}
          <div className="trends-bar-chart">
            {barData.map((b) => (
              <div key={b.label} className="trends-bar-col">
                {/* height is dynamic — kept as inline style */}
                <div className="trends-bar-rect" style={{ height: b.height }} />
                <span className="trends-bar-col-label">{b.label}</span>
              </div>
            ))}
          </div>

          {/* Next month peak */}
          <div className="trends-next-peak">
            <img src="/icons/trends/nextMonthPeak.svg" alt="" className="trends-icon-md" />
            <div>
              <div className="trends-next-peak-sub">next month peak:</div>
              <div className="trends-next-peak-main">
                {topByChange[0]?.name ?? "—"} / <span className="trends-next-peak-light">{topByChange[1]?.name?.toLowerCase() ?? "—"}</span>
              </div>
            </div>
          </div>

          {/* Personalized recommendation */}
          <div className="trends-reco-card">
            <div className="trends-reco-header">
              <img src="/icons/trends/gamePad.svg" alt="" className="trends-icon-md" />
              <div>
                <div className="trends-reco-title">PERSONALIZED</div>
                <div className="trends-reco-title">RECOMMENDATION</div>
              </div>
            </div>
            <p className="trends-reco-body">
              your top genre was <strong>{beforeGenre.toLowerCase()}</strong> — shift to{" "}
              <strong>{topByChange[0]?.name?.toLowerCase() ?? "—"}</strong> now matches predicted{" "}
              <strong>{growthDiff > 0 ? `+${growthDiff}%` : `${growthDiff}%`} growth.</strong>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
