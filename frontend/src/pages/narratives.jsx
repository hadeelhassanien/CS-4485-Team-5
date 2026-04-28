import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";

import "./narratives.css";

const BASE = "https://165.232.136.214.sslip.io";

export default function Narratives() {
  const navigate = useNavigate();
  const [narratives, setNarratives] = useState([]);
  const [pageData, setPageData] = useState({ recentNarratives: [], aiGameplayOverview: [] });
  const [genreData, setGenreData] = useState({ genres: [], selectedGenre: null, narratives: [] });
  const [loadingNarratives, setLoadingNarratives] = useState(true);
  const [loadingPageData, setLoadingPageData] = useState(true);
  const [loadingGenreClaims, setLoadingGenreClaims] = useState(true);
  const [narrativesError, setNarrativesError] = useState(null);
  const [pageDataError, setPageDataError] = useState(null);
  const [genreError, setGenreError] = useState(null);
  const [titleDropdownOpen, setTitleDropdownOpen] = useState(false);
  const [sectionDropdownOpen, setSectionDropdownOpen] = useState(false);
  const [genreDropdownOpen, setGenreDropdownOpen] = useState(false);
  const [selectedTitle, setSelectedTitle] = useState(null);
  const [selectedSectionTitle, setSelectedSectionTitle] = useState(null);
  const titleDropdownRef = useRef(null);
  const sectionDropdownRef = useRef(null);
  const genreDropdownRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(e) {
      if (titleDropdownRef.current && !titleDropdownRef.current.contains(e.target)) {
        setTitleDropdownOpen(false);
      }
      if (sectionDropdownRef.current && !sectionDropdownRef.current.contains(e.target)) {
        setSectionDropdownOpen(false);
      }
      if (genreDropdownRef.current && !genreDropdownRef.current.contains(e.target)) {
        setGenreDropdownOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    let isMounted = true;

    fetch(`${BASE}/api/narratives/page-data`)
      .then((res) => {
        if (!res.ok) throw new Error(`Page data error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        if (isMounted) {
          const overview = Array.isArray(data?.aiGameplayOverview) ? data.aiGameplayOverview : [];
          setPageData({
            recentNarratives: Array.isArray(data?.recentNarratives) ? data.recentNarratives : [],
            aiGameplayOverview: overview,
          });
          setSelectedSectionTitle((current) => current ?? overview[0]?.title ?? null);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setPageDataError(err.message);
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoadingPageData(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    fetch(`${BASE}/api/narratives`)
      .then((res) => {
        if (!res.ok) throw new Error(`Narratives error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        if (isMounted) {
          const results = Array.isArray(data)
            ? data
            : Array.isArray(data?.results)
              ? data.results
              : [];

          const validNarratives = results.filter(
            (item) => typeof item?.title === "string" && typeof item?.recommendation === "string"
          );

          setNarratives(validNarratives);
          setSelectedTitle((current) => current ?? validNarratives[0]?.title ?? null);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setNarrativesError(err.message);
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoadingNarratives(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    fetch(`${BASE}/api/narratives/claims-by-genre`)
      .then((res) => {
        if (!res.ok) throw new Error(`Genre claims error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        if (isMounted) {
          setGenreData({
            genres: Array.isArray(data?.genres) ? data.genres : [],
            selectedGenre: data?.selectedGenre ?? null,
            narratives: Array.isArray(data?.narratives) ? data.narratives : [],
          });
        }
      })
      .catch((err) => {
        if (isMounted) {
          setGenreError(err.message);
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoadingGenreClaims(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const selectGenre = async (genre) => {
    try {
      setLoadingGenreClaims(true);
      setGenreError(null);
      setGenreDropdownOpen(false);

      const response = await fetch(`${BASE}/api/narratives/claims-by-genre?genre=${encodeURIComponent(genre)}`);
      if (!response.ok) {
        throw new Error(`Genre claims error: ${response.status}`);
      }

      const data = await response.json();
      setGenreData({
        genres: Array.isArray(data?.genres) ? data.genres : [],
        selectedGenre: data?.selectedGenre ?? genre,
        narratives: Array.isArray(data?.narratives) ? data.narratives : [],
      });
    } catch (err) {
      setGenreError(err.message);
    } finally {
      setLoadingGenreClaims(false);
    }
  };

  const selectedNarrative = narratives.find((item) => item.title === selectedTitle) ?? narratives[0] ?? null;
  const selectedSection = pageData.aiGameplayOverview.find((item) => item.title === selectedSectionTitle)
    ?? pageData.aiGameplayOverview[0]
    ?? null;

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
        Narratives <span className="narratives-title-accent">&middot; Claims &amp; Recommendations</span>
      </h1>

      <div className="narratives-grid">
        <section className="narratives-card narratives-card--left">
          <div className="narratives-section-label">
            <img src="/icons/narratives/Doc.svg" alt="" className="narratives-section-icon" />
            <span>GENRE CLAIMS</span>
          </div>

          <div className="narratives-divider" />

          {loadingGenreClaims && <div className="narratives-status">Loading genre claims...</div>}
          {!loadingGenreClaims && genreError && <div className="narratives-status narratives-status--error">Failed to load: {genreError}</div>}

          {!loadingGenreClaims && !genreError && (
            <div className="narratives-subsection narratives-subsection--left">
              <div className="narratives-dropdown-wrap" ref={genreDropdownRef}>
                <button
                  type="button"
                  className={`narratives-genre-trigger${genreDropdownOpen ? " narratives-genre-trigger--open" : ""}`}
                  onClick={() => setGenreDropdownOpen((open) => !open)}
                  aria-haspopup="listbox"
                  aria-expanded={genreDropdownOpen}
                >
                  <span className="narratives-genre-trigger__label">genre</span>
                  <span className="narratives-genre-trigger__value">{genreData.selectedGenre ?? "Select genre"}</span>
                  <span className="narratives-genre-trigger__caret">{genreDropdownOpen ? "▴" : "▾"}</span>
                </button>

                {genreDropdownOpen && genreData.genres.length > 0 && (
                  <ul className="narratives-genre-dropdown" role="listbox">
                    {genreData.genres.map((genre) => (
                      <li
                        key={genre}
                        role="option"
                        aria-selected={genre === genreData.selectedGenre}
                        className={`narratives-genre-dropdown-item${genre === genreData.selectedGenre ? " narratives-genre-dropdown-item--active" : ""}`}
                        onClick={() => selectGenre(genre)}
                      >
                        {genre}
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="narratives-list">
                {genreData.narratives.length === 0 && (
                  <div className="narratives-status">No genre claims available.</div>
                )}

                {genreData.narratives.map((item, index) => (
                  <article key={`${genreData.selectedGenre}-${index}`} className="narratives-quote-card">
                    <img src="/icons/narratives/User.svg" alt="" className="narratives-quote-icon" />
                    <p className="narratives-quote-text">"{item}"</p>
                  </article>
                ))}
              </div>
            </div>
          )}
        </section>

        <section className="narratives-card narratives-card--right">
          <div className="narratives-section-label">
            <img src="/icons/narratives/Book.svg" alt="" className="narratives-section-icon" />
            <span>AI OVERVIEW &middot; NARRATIVE RECOMMENDATIONS</span>
          </div>

          <div className="narratives-divider" />

          <div className="narratives-right-stack">
            <div className="narratives-subsection">
              <div className="narratives-subsection-label">AI Overview</div>

              <div className="narratives-dropdown-wrap" ref={sectionDropdownRef}>
                <button
                  type="button"
                  className={`narratives-genre-trigger${sectionDropdownOpen ? " narratives-genre-trigger--open" : ""}`}
                  onClick={() => setSectionDropdownOpen((open) => !open)}
                  aria-haspopup="listbox"
                  aria-expanded={sectionDropdownOpen}
                >
                  <span className="narratives-genre-trigger__label">section</span>
                  <span className="narratives-genre-trigger__value">{selectedSection?.title ?? "Select section"}</span>
                  <span className="narratives-genre-trigger__caret">{sectionDropdownOpen ? "▴" : "▾"}</span>
                </button>

                {sectionDropdownOpen && pageData.aiGameplayOverview.length > 0 && (
                  <ul className="narratives-genre-dropdown" role="listbox">
                    {pageData.aiGameplayOverview.map((section) => (
                      <li
                        key={section.title}
                        role="option"
                        aria-selected={section.title === selectedSection?.title}
                        className={`narratives-genre-dropdown-item${section.title === selectedSection?.title ? " narratives-genre-dropdown-item--active" : ""}`}
                        onClick={() => {
                          setSelectedSectionTitle(section.title);
                          setSectionDropdownOpen(false);
                        }}
                      >
                        {section.title}
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              {loadingPageData && <div className="narratives-status">Loading overview...</div>}
              {!loadingPageData && pageDataError && <div className="narratives-status narratives-status--error">Failed to load: {pageDataError}</div>}

              {!loadingPageData && !pageDataError && selectedSection && (
                <>
                  <div className="narratives-group-items">
                    {(selectedSection.items ?? []).map((item, index) => (
                      <article key={`${selectedSection.title}-${index}`} className="narratives-item-card">
                        <p className="narratives-group-claim">"{item}"</p>
                      </article>
                    ))}
                  </div>
                </>
              )}
            </div>

            <div className="narratives-subsection">
              <div className="narratives-subsection-label">Narrative Recommendations</div>

          <div className="narratives-dropdown-wrap" ref={titleDropdownRef}>
            <button
              type="button"
              className={`narratives-genre-trigger${titleDropdownOpen ? " narratives-genre-trigger--open" : ""}`}
              onClick={() => setTitleDropdownOpen((open) => !open)}
              aria-haspopup="listbox"
              aria-expanded={titleDropdownOpen}
            >
              <span className="narratives-genre-trigger__label">title</span>
              <span className="narratives-genre-trigger__value">{selectedNarrative?.title ?? "Select title"}</span>
              <span className="narratives-genre-trigger__caret">{titleDropdownOpen ? "▴" : "▾"}</span>
            </button>

            {titleDropdownOpen && narratives.length > 0 && (
              <ul className="narratives-genre-dropdown" role="listbox">
                {narratives.map((item) => (
                  <li
                    key={item.title}
                    role="option"
                    aria-selected={item.title === selectedNarrative?.title}
                    className={`narratives-genre-dropdown-item${item.title === selectedNarrative?.title ? " narratives-genre-dropdown-item--active" : ""}`}
                    onClick={() => {
                      setSelectedTitle(item.title);
                      setTitleDropdownOpen(false);
                    }}
                  >
                    {item.title}
                  </li>
                ))}
              </ul>
            )}
          </div>

          {loadingNarratives && <div className="narratives-status">Loading recommendation...</div>}
          {!loadingNarratives && narrativesError && <div className="narratives-status narratives-status--error">Failed to load: {narrativesError}</div>}

          {!loadingNarratives && !narrativesError && (
            <div className="narratives-groups">
              {!selectedNarrative && (
                <div className="narratives-status">No recommendation available.</div>
              )}

              {selectedNarrative && (
                <section className="narratives-group">
                  <div className="narratives-group-items">
                    <article className="narratives-item-card">
                      <p className="narratives-group-claim">
                        {selectedNarrative.recommendation}
                      </p>
                    </article>
                  </div>
                </section>
              )}
            </div>
          )}
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
