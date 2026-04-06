import { useNavigate } from "react-router-dom";
import "./narratives.css";

const recentNarratives = [
  "Introduce varied enemy behaviors, such as slow-moving groups, fast-moving targets, and unpredictable movements, to create dynamic combat situations. Use environmental elements like walls and platforms to enhance evasion options.",
  "Enhance the loot drops system to ensure that Leon encounters more items scattered throughout the game world. Include a feature where he can gather more items than usual, encouraging exploration and interaction with the environment.",
  "Create dynamic environments that offer diverse routes and pathways, forcing players to constantly switch between them. Use variable lighting and weather effects to simulate urgency and create a sense of danger.",
  "Increase the frequency and intensity of encounters gradually, ensuring that enemies become more challenging as the game progresses. Introduce temporary debuffs or health reductions that build tension and engagement.",
  "Expand the game's scope by adding new missions, side quests, and hidden areas. Increase the number of available tasks and opportunities for players to interact with the world.",
];

const narrativeGroups = [
  {
    title: "Party / casual",
    icon: "/icons/narratives/Puzzle.svg",
    claims: [
      "The player has discovered a new ability to control brain routes in their game, which allows them to interact with objects and potentially influence gameplay mechanics.",
      "The player is controlling characters and trying to steal items from each other while also battling against enemies within their own base.",
      "The group is dealing with theft and returning items to their rightful owner.",
    ],
  },
  {
    title: "Survival craft",
    icon: "/icons/narratives/Diamond.svg",
    claims: [
      "The player is upgrading their fishing rod and gathering resources to prepare for catching a legendary star fragment.",
      "The player needs to complete four quests involving converting fire fish into fire, fighting various bosses, and collecting orbs to defeat a final boss.",
      "The user has obtained a shiny fish in a video game and wants others to join in the multiplayer mode.",
    ],
  },
  {
    title: "Battle Royale",
    icon: "/icons/narratives/Badge.svg",
    claims: [
      "The player explores a series of dungeons and upgrades their equipment to increase their mining efficiency, ultimately reaching a higher level where they gain access to powerful tools and earn significant amounts of gold.",
      "The player successfully completes a high-speed race through a virtual world by utilizing powerful upgrades and quick reflexes to avoid obstacles and maximize their earnings.",
      "The narrator discusses increasing their in-game score by purchasing rare brain rods at an accelerating rate.",
    ],
  },
];

export default function Narratives() {
  const navigate = useNavigate();

  return (
    <div className="narratives-page">
      <header className="narratives-header">
        <img src="/icons/landing/creatorXP.svg" alt="CreatorXP" className="narratives-brand-logo" />
        <img
          src="/icons/claims/houseIcon.svg"
          alt="Home"
          className="narratives-home-btn"
          onClick={() => navigate("/")}
        />
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

          <div className="narratives-list">
            {recentNarratives.map((item, index) => (
              <article key={index} className="narratives-quote-card">
                <img src="/icons/narratives/User.svg" alt="" className="narratives-quote-icon" />
                <p className="narratives-quote-text">"{item}"</p>
              </article>
            ))}
          </div>
        </section>

        <section className="narratives-card narratives-card--right">
          <div className="narratives-section-label">
            <img src="/icons/narratives/Book.svg" alt="" className="narratives-section-icon" />
            <span>FACTUAL CLAIMS &middot; TOP TRENDING GENRES (30D)</span>
          </div>

          <div className="narratives-divider" />

          <div className="narratives-groups">
            {narrativeGroups.map((group) => (
              <section key={group.title} className="narratives-group">
                <div className="narratives-group-title">
                  <img src={group.icon} alt="" className="narratives-group-icon" />
                  <span>{group.title}</span>
                </div>

                <div className="narratives-group-card">
                  {group.claims.map((claim, index) => (
                    <p key={index} className="narratives-group-claim">
                      "{claim}"
                    </p>
                  ))}
                </div>
              </section>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
