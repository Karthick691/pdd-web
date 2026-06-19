import { motion } from 'framer-motion';
import { Activity, Camera } from 'lucide-react';

export const FullPageLoader = () => {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="flex-center"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        backgroundColor: 'rgba(255, 255, 255, 0.85)',
        backdropFilter: 'blur(12px)',
        zIndex: 9999,
        flexDirection: 'column',
        gap: '20px',
      }}
    >
      <motion.div
        animate={{
          scale: [1, 1.2, 1],
          rotate: [0, 180, 360],
        }}
        transition={{
          duration: 2,
          repeat: Infinity,
          ease: 'easeInOut',
        }}
        style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          border: '3px solid transparent',
          borderTopColor: 'var(--primary-green)',
          borderBottomColor: 'var(--secondary-blue)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Activity size={32} className="text-primary" />
      </motion.div>
      <motion.h3
        animate={{ opacity: [0.4, 1, 0.4] }}
        transition={{ duration: 1.5, repeat: Infinity }}
        style={{ fontWeight: 600, color: 'var(--dark-text)' }}
      >
        Loading Food Snap AI...
      </motion.h3>
    </motion.div>
  );
};

export const AIScanLoader = () => {
  return (
    <div style={{ position: 'relative', overflow: 'hidden', padding: '40px', borderRadius: '16px', background: 'rgba(255, 255, 255, 0.6)', border: '1px solid rgba(16, 185, 129, 0.2)' }} className="flex-center flex-column glass-card">
      <motion.div
        animate={{ y: [0, 120, 0] }}
        transition={{ duration: 1.8, repeat: Infinity, ease: 'linear' }}
        style={{
          position: 'absolute',
          top: '20px',
          left: 0,
          width: '100%',
          height: '4px',
          background: 'linear-gradient(90deg, transparent, var(--primary-green), transparent)',
          boxShadow: '0 0 15px var(--primary-green)',
          zIndex: 10,
        }}
      />
      <motion.div
        animate={{ scale: [1, 1.05, 1] }}
        transition={{ duration: 1.5, repeat: Infinity }}
        style={{ marginBottom: '15px' }}
      >
        <Camera size={56} className="text-primary" />
      </motion.div>
      <h4 style={{ margin: 0, fontWeight: 600, color: 'var(--dark-text)' }}>AI Engine Scanning...</h4>
      <p style={{ fontSize: '0.875rem', color: '#6B7280', marginTop: '5px' }}>Analyzing macro density & nutritional confidence</p>
    </div>
  );
};

export const SkeletonCard = ({ height = '150px' }) => {
  return (
    <div
      style={{
        height,
        width: '100%',
        borderRadius: '16px',
        background: 'linear-gradient(90deg, var(--card-background) 25%, var(--light-gray) 50%, var(--card-background) 75%)',
        backgroundSize: '200% 100%',
        animation: 'skeletonLoading 1.5s infinite',
      }}
    />
  );
};

export const Spinner = ({ size = 24, color = 'var(--primary-green)' }) => {
  return (
    <motion.div
      animate={{ rotate: 360 }}
      transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
      style={{
        width: size,
        height: size,
        borderRadius: '50%',
        border: `2px solid ${color}`,
        borderTopColor: 'transparent',
      }}
    />
  );
};
