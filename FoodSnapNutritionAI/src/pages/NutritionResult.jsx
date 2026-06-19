import { Link, useNavigate, useLocation } from 'react-router-dom';
import { CheckCircle, AlertCircle, RefreshCw, Flame, Apple, HelpCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import PageTransition from '../animations/PageTransition';
import DashboardCard from '../components/DashboardCard';

const containerVariants = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, scale: 0.95 },
  show: { opacity: 1, scale: 1, transition: { duration: 0.25 } },
};

import { db } from '../firebase/config';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { useAuth } from '../context/AuthContext';

const NutritionResult = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentUser } = useAuth();
  
  // Get data from navigation state (passed from UploadBox)
  const nutritionData = location.state?.nutritionData || {
    food_name: "Grilled Salmon Salad",
    confidence: 98.4,
    calories: 450,
    macros: { protein: 35, carbs: 12, fats: 22 },
    vitamins: ["Vitamin D: 80%", "Vitamin B12: 120%", "Active Iron: 15%"],
    health_score: "A+",
    description: "Optimal balance of active short-chain Omega-3s and premium organic greens. Excellent low glycemic load.",
    alternatives: "This meal exhibits extreme nutritional confidence. For ultra-premium performance, consider utilizing cold-pressed avocado infusion."
  };

  const handleLogMeal = async () => {
    if (!currentUser) {
      toast.error("Please login to log meals");
      return;
    }

    if (nutritionData.source === "client_simulation") {
      toast.error("Offline simulated scans cannot be logged to your health records.");
      return;
    }

    const toastId = toast.loading('Saving to log...');
    try {
      await addDoc(collection(db, 'foodLogs'), {
        userId: currentUser.uid,
        foodName: nutritionData.foodName || nutritionData.food_name || "Unknown Meal",
        calories: parseInt(nutritionData.calories) || 0,
        protein: String(nutritionData.protein || (nutritionData.macros && nutritionData.macros.protein) || "0g"),
        carbs: String(nutritionData.carbs || (nutritionData.macros && nutritionData.macros.carbs) || "0g"),
        fats: String(nutritionData.fats || (nutritionData.macros && nutritionData.macros.fats) || "0g"),
        healthScore: nutritionData.healthScore || nutritionData.health_score || "N/A",
        timestamp: serverTimestamp(),
        imageUrl: "" 
      });

      toast.dismiss(toastId);
      toast.success('Meal logged successfully!');
      navigate('/tracker');
    } catch (error) {
      console.error("Error logging meal:", error);
      toast.dismiss(toastId);
      toast.error('Failed to save meal');
    }
  };

  return (
    <PageTransition className="page-container">
      {nutritionData.source === "client_simulation" && (
        <div style={{
          padding: '16px',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          border: '1px solid rgba(239, 68, 68, 0.3)',
          borderRadius: '12px',
          color: '#EF4444',
          marginBottom: '24px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          <AlertCircle size={24} />
          <div>
            <strong style={{ display: 'block' }}>Unverified Offline Estimation</strong>
            <span style={{ fontSize: '0.9rem' }}>The backend AI service was unreachable. This result is based on filename matching and cannot be logged to your history.</span>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '32px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h1 className="heading-2" style={{ margin: 0 }}>Analysis <span className="gradient-text">Complete</span></h1>
          <p style={{ color: '#6B7280', fontSize: '0.95rem', margin: 0 }}>
            Neural verification matched with {nutritionData.confidence}% confidence
          </p>
        </div>
        <Link to="/scan" className="btn-outline flex-center" style={{ gap: '8px' }}>
          <RefreshCw size={18} /> Scan Again
        </Link>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '32px' }}>
        {/* Left column: Preview & Score summary */}
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.4 }}
          className="glass-card glow-effect"
          style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}
        >
          <div
            style={{
              height: '240px',
              backgroundColor: 'var(--light-gray)',
              borderRadius: '16px',
              marginBottom: '24px',
              position: 'relative',
              overflow: 'hidden',
              border: '1px solid var(--border-color)',
            }}
            className="flex-center flex-column"
          >
            <Apple size={48} className="text-primary mb-2" />
            <span style={{ fontSize: '0.9rem', color: '#6B7280', fontWeight: 500 }}>Live Analysis Preview</span>
            <div style={{ position: 'absolute', bottom: '12px', left: '12px', background: 'rgba(0,0,0,0.6)', color: 'white', padding: '4px 10px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 600 }}>
              AI Sensor Matrix
            </div>
          </div>

          <h3 className="heading-3" style={{ marginBottom: '4px' }}>{nutritionData.food_name}</h3>
          <span style={{ display: 'inline-block', color: 'var(--primary-green)', fontWeight: 600, fontSize: '0.85rem', marginBottom: '20px', padding: '4px 12px', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '12px', alignSelf: 'flex-start' }}>
            Confidence Level: {nutritionData.confidence}%
          </span>

          <div
            className="glass-card"
            style={{
              padding: '20px',
              backgroundColor: 'rgba(16, 185, 129, 0.05)',
              border: '1px solid rgba(16, 185, 129, 0.3)',
              borderRadius: '16px',
              marginTop: 'auto',
            }}
          >
            <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '10px', color: 'var(--primary-green)' }}>
              <CheckCircle size={24} />
              <h4 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 700 }}>Health Score: {nutritionData.health_score}</h4>
            </div>
            <p style={{ fontSize: '0.9rem', color: 'var(--dark-text)', marginTop: '8px', marginBottom: 0, lineHeight: 1.5 }}>
              {nutritionData.description}
            </p>
          </div>
        </motion.div>

        {/* Right column: Macro Grid & Detailed tips */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="show"
          style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}
        >
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '16px' }}>
            <motion.div variants={itemVariants}>
              <DashboardCard title="Calories" value={nutritionData.calories} subtitle="kcal" progress={(nutritionData.calories / 2000) * 100} />
            </motion.div>
            <motion.div variants={itemVariants}>
              <DashboardCard title="Protein" value={nutritionData.macros.protein} subtitle="g" progress={(nutritionData.macros.protein / 150) * 100} />
            </motion.div>
            <motion.div variants={itemVariants}>
              <DashboardCard title="Carbs" value={nutritionData.macros.carbs} subtitle="g" progress={(nutritionData.macros.carbs / 250) * 100} />
            </motion.div>
            <motion.div variants={itemVariants}>
              <DashboardCard title="Fats" value={nutritionData.macros.fats} subtitle="g" progress={(nutritionData.macros.fats / 70) * 100} />
            </motion.div>
          </div>

          {/* Micro density pills */}
          <motion.div variants={itemVariants} className="glass-card" style={{ padding: '24px' }}>
            <h3 className="heading-3" style={{ fontSize: '1.1rem', marginBottom: '16px' }}>Vitamins & Micronutrients</h3>
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
              {nutritionData.vitamins.map((vit, idx) => {
                const [name, val] = vit.includes(':') ? vit.split(':') : [vit, ""];
                return (
                  <motion.span
                    key={idx}
                    whileHover={{ scale: 1.05 }}
                    style={{
                      padding: '8px 16px',
                      backgroundColor: 'var(--light-gray)',
                      borderRadius: '20px',
                      fontSize: '0.85rem',
                      fontWeight: 500,
                      color: 'var(--dark-text)',
                      borderLeft: `3px solid var(--primary-green)`,
                      boxShadow: 'var(--shadow-sm)',
                    }}
                  >
                    {name} <strong style={{ color: 'var(--primary-green)' }}>{val}</strong>
                  </motion.span>
                );
              })}
            </div>
          </motion.div>

          {/* Tips panel */}
          <motion.div variants={itemVariants} className="glass-card" style={{ padding: '24px' }}>
            <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '10px', marginBottom: '12px' }}>
              <AlertCircle className="text-secondary" size={20} />
              <h3 className="heading-3" style={{ margin: 0, fontSize: '1.1rem' }}>Dietitian Alternatives</h3>
            </div>
            <p style={{ color: '#6B7280', fontSize: '0.9rem', margin: 0, lineHeight: 1.6 }}>
              {nutritionData.alternatives}
            </p>
          </motion.div>

          {/* Action Log Trigger */}
          <motion.button
            variants={itemVariants}
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.98 }}
            onClick={handleLogMeal}
            className="btn-primary flex-center"
            style={{ width: '100%', padding: '16px', fontSize: '1.1rem', borderRadius: '16px', marginTop: 'auto' }}
          >
            <Flame size={20} /> Log This Meal
          </motion.button>
        </motion.div>
      </div>
    </PageTransition>
  );
};

export default NutritionResult;
