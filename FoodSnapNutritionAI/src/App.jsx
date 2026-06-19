import { Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import { Toaster } from 'react-hot-toast';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import FloatingChatbot from './components/FloatingChatbot';
import ScrollToTop from './components/ScrollToTop';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import FoodScan from './pages/FoodScan';
import NutritionResult from './pages/NutritionResult';
import DietPlans from './pages/DietPlans';
import DailyTracker from './pages/DailyTracker';
import AIChatAssistant from './pages/AIChatAssistant';
import Profile from './pages/Profile';
import About from './pages/About';
import Contact from './pages/Contact';
import PrivateRoute from './components/PrivateRoute';
import ExerciseDetails from './pages/ExerciseDetails';

function App() {
  const location = useLocation();

  return (
    <div className="app-container">
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: 'var(--glass-bg)',
            color: 'var(--dark-text)',
            backdropFilter: 'blur(16px)',
            border: '1px solid var(--glass-border)',
            borderRadius: '16px',
            boxShadow: 'var(--shadow-md)',
            padding: '16px 24px',
          },
          success: {
            iconTheme: {
              primary: 'var(--primary-green)',
              secondary: 'white',
            },
          },
        }}
      />
      <Navbar />
      <main className="page-container">
        <AnimatePresence mode="wait">
          <Routes location={location} key={location.pathname}>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
            <Route path="/scan" element={<PrivateRoute><FoodScan /></PrivateRoute>} />
            <Route path="/result" element={<PrivateRoute><NutritionResult /></PrivateRoute>} />
            <Route path="/diet-plans" element={<PrivateRoute><DietPlans /></PrivateRoute>} />
            <Route path="/tracker" element={<PrivateRoute><DailyTracker /></PrivateRoute>} />
            <Route path="/chat" element={<PrivateRoute><AIChatAssistant /></PrivateRoute>} />
            <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
            <Route path="/exercise" element={<PrivateRoute><ExerciseDetails /></PrivateRoute>} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
          </Routes>
        </AnimatePresence>
      </main>
      <FloatingChatbot />
      <ScrollToTop />
      <Footer />
    </div>
  );
}

export default App;

