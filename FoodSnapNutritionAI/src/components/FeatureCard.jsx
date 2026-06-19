import { motion } from 'framer-motion';

const FeatureCard = ({ icon, title, description }) => {
  return (
    <motion.div
      whileHover={{ y: -8, scale: 1.02 }}
      transition={{ duration: 0.25 }}
      className="feature-card glass-card glow-effect"
      style={{
        padding: '32px',
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        height: '100%',
      }}
    >
      <div
        className="mb-4 flex-center"
        style={{
          width: '64px',
          height: '64px',
          borderRadius: '16px',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          border: '1px solid rgba(16, 185, 129, 0.2)',
          marginBottom: '20px',
        }}
      >
        {icon}
      </div>
      <h3 style={{ fontSize: '1.25rem', fontWeight: 600, color: 'var(--dark-text)', marginBottom: '12px' }}>
        {title}
      </h3>
      <p style={{ color: '#6B7280', fontSize: '0.95rem', lineHeight: 1.6, margin: 0 }}>
        {description}
      </p>
    </motion.div>
  );
};

export default FeatureCard;
