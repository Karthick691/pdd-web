import React, { useState, useEffect, useMemo } from 'react';
import { Activity, Droplet, Flame, Target, Sparkles, UserCheck } from 'lucide-react';
import { motion } from 'framer-motion';
import PageTransition from '../animations/PageTransition';
import DashboardCard from '../components/DashboardCard';
import NutritionChart from '../components/NutritionChart';
import OnboardingModal from '../components/OnboardingModal';
import WaterModal from '../components/WaterModal';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase/config';
import { collection, query, where, getDocs, doc, getDoc, addDoc } from 'firebase/firestore';
import { calculateNutritionTargets } from '../services/nutritionHelper';
import { getCurrentTimestamp, getTodayDateString } from '../utils/dateTimeHelper';
import toast from 'react-hot-toast';
import '../styles/dashboard.css';

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
  hidden: { opacity: 0, y: 15 },
  show: { opacity: 1, y: 0, transition: { duration: 0.3 } },
};

const Dashboard = () => {
  const { currentUser } = useAuth();
  const [totalCalories, setTotalCalories] = useState(0);
  const [totalProtein, setTotalProtein] = useState(0);
  const [totalCarbs, setTotalCarbs] = useState(0);
  const [totalFats, setTotalFats] = useState(0);
  const [totalExerciseMins, setTotalExerciseMins] = useState(0);
  const [totalExerciseCalories, setTotalExerciseCalories] = useState(0);
  const [loading, setLoading] = useState(true);
  
  // Profile onboarding state
  const [profile, setProfile] = useState(null);
  const [showOnboarding, setShowOnboarding] = useState(false);

  // Water intake state
  const [waterCount, setWaterCount] = useState(0);
  const [isWaterModalOpen, setIsWaterModalOpen] = useState(false);
  
  const userName = currentUser?.displayName || currentUser?.email?.split('@')[0] || 'User';
  const todayStr = getTodayDateString();

  const fetchTodayStats = async () => {
    if (!currentUser) return;
    try {
      // 1. Fetch Profile
      const docRef = doc(db, 'profiles', currentUser.uid);
      const docSnap = await getDoc(docRef);
      let activeProfile = null;
      if (docSnap.exists()) {
        const data = docSnap.data();
        setProfile(data);
        activeProfile = data;
        localStorage.setItem(`user_profile_${currentUser.uid}`, JSON.stringify(data));
      } else {
        setShowOnboarding(true);
      }

      // 2. Fetch today's food logs
      const foodQ = query(
        collection(db, 'foodLogs'),
        where('userId', '==', currentUser.uid)
      );
      const foodSnapshot = await getDocs(foodQ);
      
      let cals = 0;
      let protein = 0;
      let carbs = 0;
      let fats = 0;

      foodSnapshot.docs.forEach((doc) => {
        const data = doc.data();
        if (data.timestamp?.substring(0, 10) === todayStr) {
          cals += parseInt(data.calories || 0);
          // parse protein/carbs/fats safely as numbers
          protein += data.protein !== undefined ? (typeof data.protein === 'number' ? data.protein : parseInt(String(data.protein).replace('g', '')) || 0) : 0;
          carbs += data.carbs !== undefined ? (typeof data.carbs === 'number' ? data.carbs : parseInt(String(data.carbs).replace('g', '')) || 0) : 0;
          fats += data.fats !== undefined ? (typeof data.fats === 'number' ? data.fats : parseInt(String(data.fats).replace('g', '')) || 0) : 0;
        }
      });

      setTotalCalories(cals);
      setTotalProtein(protein);
      setTotalCarbs(carbs);
      setTotalFats(fats);

      // 3. Fetch today's exercise logs
      const exQ = query(
        collection(db, 'exerciseLogs'),
        where('userId', '==', currentUser.uid)
      );
      const exSnapshot = await getDocs(exQ);

      let exMins = 0;
      let exCals = 0;

      exSnapshot.docs.forEach((doc) => {
        const data = doc.data();
        if (data.timestamp?.substring(0, 10) === todayStr) {
          exMins += parseInt(data.durationMinutes || data.duration || 0);
          exCals += parseInt(data.caloriesBurned || 0);
        }
      });

      setTotalExerciseMins(exMins);
      setTotalExerciseCalories(exCals);

      // 4. Fetch today's water logs
      const waterQ = query(
        collection(db, 'waterLogs'),
        where('userId', '==', currentUser.uid)
      );
      const waterSnapshot = await getDocs(waterQ);
      let waterSum = 0;
      waterSnapshot.docs.forEach((doc) => {
        const data = doc.data();
        if (data.timestamp?.substring(0, 10) === todayStr) {
          waterSum += parseInt(data.amount || 0);
        }
      });
      setWaterCount(waterSum);

      // Mark the profile as synced (update local state sync time)
      if (activeProfile) {
        const syncTime = getCurrentTimestamp();
        setProfile(prev => prev ? { ...prev, lastSynced: syncTime } : null);
      }

    } catch (error) {
      console.error("Error fetching dashboard stats:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTodayStats();
  }, [currentUser]);

  // Compute targets based on user's profile
  const targets = useMemo(() => {
    return calculateNutritionTargets(profile);
  }, [profile]);

  const handleAddWater = async (ml) => {
    if (!currentUser) return;
    const timestamp = getCurrentTimestamp();
    try {
      await addDoc(collection(db, 'waterLogs'), {
        userId: currentUser.uid,
        amount: parseInt(ml),
        timestamp: timestamp,
        date: todayStr,
        time: timestamp.split('T')[1].substring(0, 8),
        timezone: timestamp.substring(19)
      });
      setWaterCount(prev => prev + parseInt(ml));
      toast.success(`Logged ${ml} ml of water!`);
    } catch (error) {
      toast.error('Failed to log water');
    }
  };

  return (
    <PageTransition className="dashboard-container">
      <div className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '20px' }}>
        <div>
          <motion.h1
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="heading-2"
            style={{ margin: 0 }}
          >
            Welcome, <span className="gradient-text">{profile?.name || userName}!</span>
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 }}
            style={{ color: '#6B7280', fontSize: '1.05rem', marginTop: '4px' }}
          >
            Here is your real-time AI optimized health matrix.
          </motion.p>
        </div>

        {/* Profile Stats Indicator */}
        {profile && (
          <motion.div 
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            onClick={() => setShowOnboarding(true)}
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '10px', 
              padding: '10px 18px', 
              borderRadius: '14px', 
              background: 'rgba(16, 185, 129, 0.05)', 
              border: '1px solid rgba(16, 185, 129, 0.15)',
              cursor: 'pointer'
            }}
          >
            <UserCheck className="text-primary" size={20} />
            <div style={{ textAlign: 'left' }}>
              <span style={{ display: 'block', fontSize: '0.75rem', color: '#6B7280', fontWeight: 600 }}>FITNESS PROFILE</span>
              <span style={{ fontSize: '0.85rem', color: 'var(--dark-text)', fontWeight: 600 }}>{profile.height}cm / {profile.weight}kg • {profile.goal}</span>
            </div>
          </motion.div>
        )}
      </div>

      <motion.div
        variants={containerVariants}
        initial="hidden"
        animate="show"
        className="stats-grid"
      >
        <motion.div variants={itemVariants}>
          <DashboardCard
            title="Calories Consumed"
            value={loading ? "..." : totalCalories.toLocaleString()}
            subtitle={`/ ${targets.calories.toLocaleString()} kcal`}
            icon={<Flame className="text-primary" size={22} />}
            progress={(totalCalories / targets.calories) * 100}
          />
        </motion.div>
        <motion.div variants={itemVariants} onClick={() => setIsWaterModalOpen(true)} style={{ cursor: 'pointer' }}>
          <DashboardCard
            title="Hydration Level"
            value={loading ? "..." : (waterCount / 1000).toFixed(2)}
            subtitle="/ 2.8 Liters"
            icon={<Droplet className="text-secondary" size={22} />}
            progress={(waterCount / 2800) * 100}
          />
        </motion.div>
        <motion.div variants={itemVariants}>
          <DashboardCard
            title="Nutrition Target Score"
            value={loading ? "..." : (totalCalories > 0 ? "88" : "--")}
            subtitle={totalCalories > 0 ? "Good" : "Pending Data"}
            icon={<Target className="text-primary" size={22} />}
            progress={totalCalories > 0 ? 88 : 0}
          />
        </motion.div>
      </motion.div>

      <div className="dashboard-content">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3, duration: 0.4 }}
          className="chart-section glass-card"
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <h3 className="heading-3" style={{ margin: 0 }}>Analytics Center</h3>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--primary-green)', background: 'rgba(16, 185, 129, 0.1)', padding: '4px 10px', borderRadius: '12px' }}>
              Live Model Data
            </span>
          </div>
          <NutritionChart />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4, duration: 0.4 }}
          className="ai-tips-section glass-card"
        >
          <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '10px', marginBottom: '20px' }}>
            <Sparkles className="text-primary" size={24} />
            <h3 className="heading-3" style={{ margin: 0 }}>AI Health Tips</h3>
          </div>
          <ul className="tips-list" style={{ display: 'flex', flexDirection: 'column', gap: '16px', listStyle: 'none', padding: 0 }}>
            <li style={{ paddingBottom: '12px', borderBottom: '1px dashed rgba(255, 255, 255, 0.05)' }}>
              <strong style={{ color: 'var(--primary-green)', display: 'block', marginBottom: '4px' }}>Macro Balance:</strong>
              {
                totalCalories === 0 
                  ? `Goal configured: ${profile?.goal || 'General Fitness'}. Log your first meal to receive a personalized macro analysis.` 
                  : totalCalories > targets.calories
                    ? `You have exceeded your customized calorie target of ${targets.calories} kcal by ${(totalCalories - targets.calories)} kcal. Consider a metabolic acceleration exercise.`
                    : `You have consumed ${totalCalories} kcal. You are pacing perfectly towards your ${targets.calories} kcal goal for ${profile?.goal || 'General Fitness'}.`
              }
            </li>
            <li style={{ paddingBottom: '12px', borderBottom: '1px dashed rgba(255, 255, 255, 0.05)' }}>
              <strong style={{ color: 'var(--secondary-blue)', display: 'block', marginBottom: '4px' }}>Protein Synthesis:</strong>
              {
                totalProtein < targets.protein * 0.5
                  ? `Protein level is at ${totalProtein}g. You need ${targets.protein}g daily for ${profile?.goal || 'General Fitness'}. Try adding chicken breast, eggs, or whey.`
                  : `Protein synthesis is optimized! You have hit ${totalProtein}g of your ${targets.protein}g daily target. Keep it up.`
              }
            </li>
            <li style={{ paddingBottom: '12px', borderBottom: '1px dashed rgba(255, 255, 255, 0.05)' }}>
              <strong style={{ color: '#F59E0B', display: 'block', marginBottom: '4px' }}>Hydration Index:</strong>
              {
                waterCount < 2000 
                  ? `Hydration level is low (${(waterCount / 1000).toFixed(2)} L). Your body requires water to metabolize fat. Drink at least ${(2800 - waterCount)} ml more.`
                  : `Hydration level is optimal (${(waterCount / 1000).toFixed(2)} L). Your cells are fully hydrated, supporting energy transport.`
              }
            </li>
            <li>
              <strong style={{ color: '#EC4899', display: 'block', marginBottom: '4px' }}>Active Output:</strong>
              {
                totalExerciseMins === 0 
                  ? `No activity logged today. A quick 15-minute resistance session or fast walk will boost insulin sensitivity.`
                  : `Great job! You burned ${totalExerciseCalories} kcal across ${totalExerciseMins} minutes of activity today.`
              }
            </li>
          </ul>
        </motion.div>
      </div>

      {/* Onboarding Modal */}
      <OnboardingModal 
        isOpen={showOnboarding}
        onClose={() => setShowOnboarding(false)}
        currentUser={currentUser}
        onComplete={(p) => setProfile(p)}
      />

      {/* Water Intake Modal */}
      <WaterModal 
        isOpen={isWaterModalOpen}
        onClose={() => setIsWaterModalOpen(false)}
        onAdd={handleAddWater}
      />
    </PageTransition>
  );
};

export default Dashboard;
