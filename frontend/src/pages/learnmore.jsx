import { useNavigate, NavLink } from "react-router-dom";
import "./learnmore.css";

export default function Learnmore() {
  const navigate = useNavigate();

  return (
    <div className="learnmore-page">
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

      <div className="learnmore-intro">
        <p className="learnmore-eyebrow">About CreatorXP</p>
        <h1 className="learnmore-title">Built for gaming creators who want to grow smarter.</h1>
        <p className="learnmore-subtitle">CreatorXP analyzes real-time YouTube data to help gaming content creators know exactly when to switch genres, what trends to chase, and how much revenue they're leaving on the table.</p>
      </div>

      <div className="learnmore-grid">
        {[
          { label: "Trend tracker", title: "Real-time genre popularity", body: "See which gaming genres are trending right now based on live YouTube data — views, likes, comments, and growth rate over the past 30 days. The top 5 genres are ranked so you always know where the audience is." },
          { label: "Performance shift", title: "Before vs. after comparison", body: "Select your current genre and compare it against the top trending one. See the difference in views, like ratio, and comments — and get an estimated viewership uplift if you make the switch." },
          { label: "Future trends", title: "Predicted next-month peaks", body: "Our model predicts which genres will peak next month based on current momentum. A personalized recommendation tells you exactly which genre to target and why — with projected growth percentages." },
          { label: "Narratives", title: "Insights into gaming narratives", body: "Explore recent community narratives and factual claims tied to the top trending genres. Understand what players and viewers are talking about so you can create content that resonates." },
          { label: "Claims", title: "Revenue & performance", body: "See your estimated additional revenue from trend-driven content. Compare your current genre's earnings against the top genre, track your unclaimed balance, and claim your earnings directly from the dashboard." },
        ].map((item) => (
          <div key={item.label} className="learnmore-card">
            <div className="learnmore-card-label">{item.label}</div>
            <p className="learnmore-card-title">{item.title}</p>
            <p className="learnmore-card-body">{item.body}</p>
          </div>
        ))}

        <div className="learnmore-card learnmore-card--cta">
          <p className="learnmore-card-title">Ready to grow your channel?</p>
          <p className="learnmore-card-body">Start by checking what's trending right now in gaming — and see exactly how much you could gain by making the switch.</p>
          <div className="learnmore-cta-buttons">
            <button className="learnmore-btn" onClick={() => navigate("/trends")}>View trends</button>
            <button className="learnmore-btn" onClick={() => navigate("/claims")}>See revenue</button>
          </div>
        </div>
      </div>

      <div className="learnmore-steps">
        <p className="learnmore-steps-label">How it works</p>
        <div className="learnmore-steps-grid">
          {[
            { n: "1", title: "Data collection", body: "YouTube API pulls real-time stats for 10 gaming genres daily." },
            { n: "2", title: "Trend analysis", body: "Growth rates, engagement ratios, and CPM differences are calculated." },
            { n: "3", title: "Prediction model", body: "Next-month peaks are predicted based on momentum and historical patterns." },
            { n: "4", title: "Personalized insight", body: "You get a recommendation tailored to your current genre and revenue potential." },
          ].map((step) => (
            <div key={step.n} className="learnmore-step">
              <div className="learnmore-step-num">{step.n}</div>
              <div>
                <p className="learnmore-step-title">{step.title}</p>
                <p className="learnmore-step-body">{step.body}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="learnmore-contributors">
        <p className="learnmore-contributors-label">Contributors</p>
        <div className="learnmore-contributors-list">
          {[
            "Hadeel Hassanien",
            "Claudia Nguyen",
            "Kriti Raja",
            "Sarah Huynh-Mai Truong",
            "Keertana Lakshmi Valluru",
            "Mariyam Zaki",
          ].map((name) => (
            <span key={name} className="learnmore-contributor">{name}</span>
          ))}
        </div>
      </div>      
    </div>
  );
}