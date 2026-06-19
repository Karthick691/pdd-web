import { motion } from 'framer-motion';

const DashboardCard = ({ title, value, subtitle, icon, progress }) => {
  return (
    <motion.div
      whileHover={{ y: -5, scale: 1.02 }}
      transition={{ duration: 0.2 }}
      className="dashboard-card glass-card glow-effect"
      style={{ overflow: 'hidden' }}
    >
      <div className="card-header" style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
        <div
          className="card-icon-wrap flex-center"
          style={{
            padding: '10px',
            borderRadius: '12px',
            backgroundColor: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.2)',
          }}
        >
          {icon}
        </div>
        <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: '#6B7280', margin: 0 }}>{title}</h4>
      </div>
      
      <div className="card-body" style={{ marginBottom: '16px' }}>
        <h2 style={{ fontSize: '1.85rem', fontWeight: 700, margin: 0, color: 'var(--dark-text)' }}>
          {value} <span style={{ fontSize: '0.9rem', fontWeight: 500, color: '#9CA3AF' }}>{subtitle}</span>
        </h2>
      </div>

      <div
        className="progress-bar-container"
        style={{
          height: '6px',
          backgroundColor: 'var(--border-color)',
          borderRadius: '3px',
          overflow: 'hidden',
          position: 'relative',
        }}
      >
        <motion.div
          initial={{ width: 0 }}
          animate={{ width: `${progress}%` }}
          transition={{ duration: 1.2, ease: 'easeOut' }}
          style={{
            height: '100%',
            borderRadius: '3px',
            background: progress > 75
              ? 'linear-gradient(90deg, var(--primary-green), #059669)'
              : 'linear-gradient(90deg, var(--secondary-blue), #2563EB)',
          }}
        />
      </div>
    </motion.div>
  );
};

export default DashboardCard;
