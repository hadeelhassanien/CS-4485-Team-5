import { Routes, Route } from "react-router-dom";
import Landing from "./pages/landing.jsx";
import Claims from "./pages/claims.jsx";
import Narratives from "./pages/narratives.jsx";
import Trends from "./pages/trends.jsx";
import Learnmore from "./pages/learnmore.jsx"; 

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/claims" element={<Claims />} />
      <Route path="/narratives" element={<Narratives />} />
      <Route path="/trends" element={<Trends />} />
      <Route path="/learnmore" element={<Learnmore />} />
    </Routes>
  );
}