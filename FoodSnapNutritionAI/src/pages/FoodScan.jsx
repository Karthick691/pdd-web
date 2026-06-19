import { motion } from 'framer-motion';
import PageTransition from '../animations/PageTransition';
import UploadBox from '../components/UploadBox';
import '../styles/home.css';

const FoodScan = () => {
  return (
    <PageTransition className="page-container" style={{ maxWidth: '850px', textAlign: 'center' }}>
      <motion.h1
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="heading-1"
      >
        Scan Your <span className="gradient-text">Food</span>
      </motion.h1>
      <motion.p
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        style={{ color: '#6B7280', marginBottom: '40px', fontSize: '1.05rem' }}
      >
        Upload a picture of your meal to instantly decode ingredients, validate macro integrity, and calculate caloric density.
      </motion.p>

      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.2 }}
        className="glass-card glow-effect"
        style={{ padding: '8px', borderRadius: '24px' }}
      >
        <UploadBox />
      </motion.div>
    </PageTransition>
  );
};

export default FoodScan;
