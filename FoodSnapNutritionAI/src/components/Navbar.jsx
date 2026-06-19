import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Menu, X, Activity, Sun, Moon, User } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '../context/ThemeContext';
import { useAuth } from '../context/AuthContext';
import '../styles/navbar.css';

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const location = useLocation();
  const { isDarkMode, toggleTheme } = useTheme();
  const { currentUser } = useAuth();

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 20) {
        setScrolled(true);
      } else {
        setScrolled(false);
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    setIsOpen(false);
  }, [location.pathname]);

  const navItems = [
    { name: 'Home', path: '/' },
    { name: 'Dashboard', path: '/dashboard' },
    { name: 'Scan Food', path: '/scan' },
    { name: 'Diet Plans', path: '/diet-plans' },
    { name: 'Tracker', path: '/tracker' },
    { name: 'AI Assistant', path: '/chat' },
    { name: 'Exercise', path: '/exercise' },
  ];

  return (
    <header
      className={`navbar ${scrolled ? 'scrolled' : ''}`}
    >
      <div className="nav-container">
        <Link to="/" className="nav-logo flex-center" style={{ gap: '10px' }}>
          <motion.div
            whileHover={{ rotate: 180 }}
            transition={{ duration: 0.4 }}
          >
            <Activity className="text-primary" size={28} />
          </motion.div>
          <span style={{ fontWeight: 700, fontSize: '1.4rem' }} className="gradient-text">
            FoodSnap AI
          </span>
        </Link>

        {/* Desktop Links */}
        <nav className="nav-links desktop-only">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                style={{ position: 'relative', padding: '6px 10px' }}
              >
                <span style={{ color: isActive ? 'var(--primary-green)' : 'inherit', fontWeight: isActive ? 600 : 500 }}>
                  {item.name}
                </span>
                {isActive && (
                  <motion.div
                    layoutId="activeNavIndicator"
                    style={{
                      position: 'absolute',
                      bottom: 0,
                      left: '10px',
                      right: '10px',
                      height: '3px',
                      backgroundColor: 'var(--primary-green)',
                      borderRadius: '2px',
                    }}
                    transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                  />
                )}
              </Link>
            );
          })}
        </nav>

        {/* Actions (Theme + Auth + Mobile toggle) */}
        <div className="nav-actions flex-center" style={{ gap: '12px' }}>
          <motion.button
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.95 }}
            onClick={toggleTheme}
            className="theme-toggle-btn flex-center"
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              backgroundColor: 'var(--light-gray)',
              color: 'var(--dark-text)',
            }}
            aria-label="Toggle Dark Theme"
          >
            {isDarkMode ? <Sun size={20} className="text-primary" /> : <Moon size={20} className="text-secondary" />}
          </motion.button>

          {currentUser ? (
            <Link to="/profile" className="flex-center" style={{ 
              width: '40px', 
              height: '40px', 
              borderRadius: '50%', 
              backgroundColor: 'var(--primary-green)',
              color: 'white',
              fontWeight: 700,
              textDecoration: 'none'
            }}>
              {currentUser.displayName ? currentUser.displayName[0].toUpperCase() : (currentUser.email ? currentUser.email[0].toUpperCase() : 'U')}
            </Link>
          ) : (
            <Link to="/login" className="btn-primary desktop-only" style={{ padding: '8px 20px', fontSize: '0.9rem' }}>
              Login
            </Link>
          )}

          <button
            className="mobile-menu-btn"
            onClick={() => setIsOpen(!isOpen)}
            aria-label="Toggle Navigation Menu"
            style={{ color: 'var(--dark-text)' }}
          >
            {isOpen ? <X size={26} /> : <Menu size={26} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3 }}
            className="mobile-drawer glass-card"
          >
            <div className="mobile-drawer-links">
              {navItems.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={`mobile-link ${isActive ? 'active' : ''}`}
                    onClick={() => setIsOpen(false)}
                  >
                    {item.name}
                  </Link>
                );
              })}
              <div style={{ marginTop: '15px', paddingTop: '15px', borderTop: '1px solid var(--border-color)', display: 'flex', gap: '10px' }}>
                {currentUser ? (
                  <Link to="/profile" className="btn-primary flex-1" style={{ justifyContent: 'center' }}>
                    Profile
                  </Link>
                ) : (
                  <>
                    <Link to="/login" className="btn-outline flex-1" style={{ justifyContent: 'center' }}>
                      Login
                    </Link>
                    <Link to="/signup" className="btn-primary flex-1" style={{ justifyContent: 'center' }}>
                      Sign Up
                    </Link>
                  </>
                )}
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
};

export default Navbar;
