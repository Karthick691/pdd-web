import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Camera, Activity, Zap, Sparkles, Apple, Flame } from 'lucide-react';
import { motion } from 'framer-motion';
import PageTransition from '../animations/PageTransition';
import FeatureCard from '../components/FeatureCard';
import { getRandomQuote } from '../services/quotes';
import '../styles/home.css';

const Home = () => {
  const quote = useMemo(() => getRandomQuote(), []);

  return (
    <PageTransition className="home-container">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="animated-gradient-bg" />

        <div className="hero-content">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '8px',
              padding: '6px 14px',
              borderRadius: '20px',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              border: '1px solid rgba(16, 185, 129, 0.2)',
              marginBottom: '20px',
            }}
          >
            <Sparkles size={16} className="text-primary" />
            <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--primary-green)' }}>
              Next-Gen Neural Nutrition AI
            </span>
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="heading-1"
          >
            Smart Nutrition Tracking <br />
            <span className="gradient-text">Powered by AI</span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            Instantly analyze your meals, track calories, and get personalized health insights with a simple photo. Built for high achievers.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="hero-buttons"
          >
            <Link to="/scan" className="btn-primary">
              <Camera size={20} /> Upload Food
            </Link>
            <Link to="/dashboard" className="btn-outline">
              View Dashboard
            </Link>
          </motion.div>

          {/* Fitness Quote Section */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="glass-card"
            style={{
              padding: '18px 22px',
              marginTop: '35px',
              borderLeft: '4px solid var(--primary-green)',
              background: 'rgba(16, 185, 129, 0.03)',
              textAlign: 'left',
              maxWidth: '550px',
              borderRadius: '16px'
            }}
          >
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--primary-green)', textTransform: 'uppercase', letterSpacing: '1px', display: 'block', marginBottom: '6px' }}>Daily Health Inspiration</span>
            <p style={{ fontStyle: 'italic', fontSize: '0.9rem', color: 'var(--dark-text)', lineHeight: '1.4', margin: 0 }}>
              "{quote.text}"
            </p>
            <p style={{ fontSize: '0.8rem', color: '#6B7280', margin: '4px 0 0 0', textAlign: 'right' }}>— {quote.author}</p>
          </motion.div>
        </div>

        {/* Hero Interactive Mock Graphic */}
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="hero-image glass-card glow-effect"
        >
          <div className="ai-glow-ring" />

          {/* Floating Widget 1 */}
          <motion.div
            animate={{ y: [-10, 10, -10] }}
            transition={{ duration: 4, repeat: Infinity, ease: 'easeInOut' }}
            className="floating-food-card"
            style={{ top: '30px', left: '-30px' }}
          >
            <Apple className="text-primary" size={24} />
            <div>
              <p style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--dark-text)', margin: 0 }}>Avocado Toast</p>
              <span style={{ fontSize: '0.75rem', color: '#10B981', fontWeight: 500 }}>98% Match</span>
            </div>
          </motion.div>

          {/* Floating Widget 2 */}
          <motion.div
            animate={{ y: [10, -10, 10] }}
            transition={{ duration: 5, repeat: Infinity, ease: 'easeInOut' }}
            className="floating-food-card"
            style={{ bottom: '40px', right: '-20px' }}
          >
            <Flame className="text-secondary" size={24} />
            <div>
              <p style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--dark-text)', margin: 0 }}>320 kcal</p>
              <span style={{ fontSize: '0.75rem', color: '#6B7280' }}>22g Protein</span>
            </div>
          </motion.div>

          <div className="mockup-content">
            <motion.div
              animate={{ scale: [1, 1.1, 1] }}
              transition={{ duration: 2, repeat: Infinity }}
            >
              <Activity className="text-primary" size={64} />
            </motion.div>
            <h3>AI Analysis Active</h3>
            <p>Scanning food nutritional matrix...</p>
          </div>
        </motion.div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          style={{ textAlign: 'center' }}
        >
          <h2 className="heading-2">Why Choose FoodSnap AI?</h2>
          <p style={{ color: '#6B7280', maxWidth: '500px', margin: '0 auto' }}>
            Experience the benchmark in automated macro validation and fitness optimization.
          </p>
        </motion.div>

        <div className="features-grid">
          <FeatureCard
            icon={<Zap className="text-secondary" size={32} />}
            title="Instant Recognition"
            description="Our advanced AI identifies thousands of complex foods from a single image in split seconds."
          />
          <FeatureCard
            icon={<Activity className="text-primary" size={32} />}
            title="Accurate Macros"
            description="Get ultra-precise estimations of calories, lean protein density, net carbs, and dietary fats."
          />
          <FeatureCard
            icon={<Camera className="text-secondary" size={32} />}
            title="Smart Tracking"
            description="Log daily meals automatically to maintain zero-friction diet history without tedious manual entries."
          />
        </div>
      </section>

      {/* CTA Section */}
      <section style={{ 
        padding: '100px 20px', 
        textAlign: 'center', 
        background: 'rgba(16, 185, 129, 0.05)', 
        borderRadius: '30px',
        margin: '40px 0'
      }}>
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          whileInView={{ opacity: 1, scale: 1 }}
          viewport={{ once: true }}
        >
          <h2 className="heading-2" style={{ fontSize: '2.5rem', marginBottom: '20px' }}>Ready to optimize your health?</h2>
          <p style={{ color: '#6B7280', fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto 40px' }}>
            Join 10,000+ high achievers who use FoodSnap AI to automate their nutrition.
          </p>
          <Link to="/signup" className="btn-primary" style={{ padding: '18px 40px', fontSize: '1.2rem' }}>
            Get Started For Free
          </Link>
        </motion.div>
      </section>
    </PageTransition>
  );
};

export default Home;
