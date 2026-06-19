import { Link } from 'react-router-dom';
import { Activity } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="footer" style={{ borderTop: '1px solid var(--border-color)', backgroundColor: 'var(--card-background)', padding: '60px 20px 30px', marginTop: 'auto' }}>
      <div className="footer-content" style={{ maxWidth: '1200px', margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '40px', marginBottom: '40px' }}>
        <div className="footer-brand" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '10px' }}>
            <Activity className="text-primary" size={26} />
            <span style={{ fontSize: '1.25rem', fontWeight: 700 }} className="gradient-text">FoodSnap AI</span>
          </div>
          <p style={{ color: '#6B7280', fontSize: '0.9rem', lineHeight: 1.6 }}>
            Your premium AI-powered health companion. Scan foods, unlock ultimate accuracy, and stay consistently inspired.
          </p>
        </div>

        <div className="footer-links" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <h4 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--dark-text)' }}>Quick Links</h4>
          <Link to="/about" style={{ color: '#6B7280', fontSize: '0.9rem', transition: 'color 0.2s ease' }}>About Startup</Link>
          <Link to="/contact" style={{ color: '#6B7280', fontSize: '0.9rem', transition: 'color 0.2s ease' }}>Support Desk</Link>
          <Link to="/diet-plans" style={{ color: '#6B7280', fontSize: '0.9rem', transition: 'color 0.2s ease' }}>Premium Diet Plans</Link>
          <Link to="/scan" style={{ color: '#6B7280', fontSize: '0.9rem', transition: 'color 0.2s ease' }}>Live AI Scanner</Link>
        </div>

        <div className="footer-social" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <h4 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--dark-text)' }}>Connect Community</h4>
          <div className="social-icons" style={{ display: 'flex', gap: '16px', color: 'var(--secondary-blue)' }}>
            <a href="https://github.com" target="_blank" rel="noopener noreferrer" aria-label="GitHub" style={{ padding: '8px', borderRadius: '8px', backgroundColor: 'var(--background-white)', border: '1px solid var(--border-color)', display: 'flex' }}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"></path><path d="M9 18c-4.51 2-5-2-7-2"></path></svg>
            </a>
            <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" aria-label="Twitter" style={{ padding: '8px', borderRadius: '8px', backgroundColor: 'var(--background-white)', border: '1px solid var(--border-color)', display: 'flex' }}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"></path></svg>
            </a>
            <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" aria-label="LinkedIn" style={{ padding: '8px', borderRadius: '8px', backgroundColor: 'var(--background-white)', border: '1px solid var(--border-color)', display: 'flex' }}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"></path><rect x="2" y="9" width="4" height="12"></rect><circle cx="4" cy="4" r="2"></circle></svg>
            </a>
          </div>
        </div>
      </div>

      <div className="footer-bottom" style={{ maxWidth: '1200px', margin: '0 auto', textAlign: 'center', paddingTop: '24px', borderTop: '1px solid var(--border-color)', fontSize: '0.85rem', color: '#9CA3AF' }}>
        <p>&copy; 2026 Food Snap Nutrition AI. Powered by cutting-edge neural models. Built for seamless health tracking.</p>
      </div>
    </footer>
  );
};

export default Footer;
