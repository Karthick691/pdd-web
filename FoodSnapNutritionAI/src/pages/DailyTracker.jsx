import React, { useState, useEffect, useMemo } from 'react';
import { Plus, Coffee, Apple, Utensils, Loader2, Droplet, Activity as ActivityIcon, Clock, Trash2, Copy, Edit, Heart, Eye } from 'lucide-react';
import DashboardCard from '../components/DashboardCard';
import ManualEntryModal from '../components/ManualEntryModal';
import ExerciseModal from '../components/ExerciseModal';
import WaterModal from '../components/WaterModal';
import { db } from '../firebase/config';
import { collection, query, where, getDocs, doc, deleteDoc, addDoc, getDoc, updateDoc } from 'firebase/firestore';
import { useAuth } from '../context/AuthContext';
import { calculateNutritionTargets } from '../services/nutritionHelper';
import { getCurrentTimestamp, getTodayDateString, formatLogTimestamp } from '../utils/dateTimeHelper';
import toast from 'react-hot-toast';

const DailyTracker = () => {
  const { currentUser } = useAuth();
  
  // Modals state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isExerciseModalOpen, setIsExerciseModalOpen] = useState(false);
  const [isWaterModalOpen, setIsWaterModalOpen] = useState(false);
  
  // Data lists
  const [meals, setMeals] = useState([]);
  const [waterLogs, setWaterLogs] = useState([]);
  const [exerciseLogs, setExerciseLogs] = useState([]);
  
  // Selected detail popups
  const [selectedMeal, setSelectedMeal] = useState(null);
  const [isEditingMeal, setIsEditingMeal] = useState(false);
  const [editMealData, setEditMealData] = useState(null);
  const [selectedExercise, setSelectedExercise] = useState(null);
  
  // Loading & Profile state
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState(null);
  
  const todayStr = getTodayDateString();

  // Load profile from Firestore
  const fetchProfile = async () => {
    if (!currentUser) return;
    try {
      const docRef = doc(db, 'profiles', currentUser.uid);
      const docSnap = await getDoc(docRef);
      if (docSnap.exists()) {
        const data = docSnap.data();
        setProfile(data);
        localStorage.setItem(`user_profile_${currentUser.uid}`, JSON.stringify(data));
      }
    } catch (error) {
      console.warn("Failed to load profile in tracker:", error);
    }
  };

  // Fetch food logs
  const fetchMeals = async () => {
    if (!currentUser) return;
    try {
      const q = query(
        collection(db, 'foodLogs'),
        where('userId', '==', currentUser.uid)
      );
      const querySnapshot = await getDocs(q);
      
      const list = querySnapshot.docs
        .map(doc => {
          const data = doc.data();
          // parse protein/carbs/fats safely as numbers
          const p = data.protein !== undefined ? (typeof data.protein === 'number' ? data.protein : parseInt(String(data.protein).replace('g', '')) || 0) : 0;
          const c = data.carbs !== undefined ? (typeof data.carbs === 'number' ? data.carbs : parseInt(String(data.carbs).replace('g', '')) || 0) : 0;
          const f = data.fats !== undefined ? (typeof data.fats === 'number' ? data.fats : parseInt(String(data.fats).replace('g', '')) || 0) : 0;
          const fib = data.fiber !== undefined ? parseInt(data.fiber) || 0 : 0;
          const sug = data.sugar !== undefined ? parseInt(data.sugar) || 0 : 0;
          const sod = data.sodium !== undefined ? parseInt(data.sodium) || 0 : 0;
          const hs = data.healthScore !== undefined ? parseInt(data.healthScore) || 70 : 70;

          return {
            id: doc.id,
            ...data,
            protein: p,
            carbs: c,
            fats: f,
            fiber: fib,
            sugar: sug,
            sodium: sod,
            healthScore: hs
          };
        })
        // Filter local today's logs
        .filter(meal => meal.timestamp?.substring(0, 10) === todayStr)
        .sort((a, b) => b.timestamp.localeCompare(a.timestamp));

      setMeals(list);
    } catch (error) {
      console.error("Error fetching food logs:", error);
    }
  };

  // Fetch water logs
  const fetchWaterLogs = async () => {
    if (!currentUser) return;
    try {
      const q = query(
        collection(db, 'waterLogs'),
        where('userId', '==', currentUser.uid)
      );
      const querySnapshot = await getDocs(q);
      const list = querySnapshot.docs
        .map(doc => ({ id: doc.id, ...doc.data() }))
        .filter(log => log.timestamp?.substring(0, 10) === todayStr)
        .sort((a, b) => a.timestamp.localeCompare(b.timestamp));

      setWaterLogs(list);
    } catch (error) {
      console.error("Error fetching water logs:", error);
    }
  };

  // Fetch exercise logs
  const fetchExerciseLogs = async () => {
    if (!currentUser) return;
    try {
      const q = query(
        collection(db, 'exerciseLogs'),
        where('userId', '==', currentUser.uid)
      );
      const querySnapshot = await getDocs(q);
      const list = querySnapshot.docs
        .map(doc => ({ id: doc.id, ...doc.data() }))
        .filter(log => log.timestamp?.substring(0, 10) === todayStr)
        .sort((a, b) => a.timestamp.localeCompare(b.timestamp));

      setExerciseLogs(list);
    } catch (error) {
      console.error("Error fetching exercise logs:", error);
    }
  };

  const loadAllData = async () => {
    setLoading(true);
    await Promise.all([
      fetchProfile(),
      fetchMeals(),
      fetchWaterLogs(),
      fetchExerciseLogs()
    ]);
    setLoading(false);
  };

  useEffect(() => {
    loadAllData();
  }, [currentUser]);

  // Compute stats
  const targets = useMemo(() => calculateNutritionTargets(profile), [profile]);
  const totalCalories = meals.reduce((sum, meal) => sum + (parseInt(meal.calories) || 0), 0);
  const water = waterLogs.reduce((sum, log) => sum + (parseInt(log.amount) || 0), 0);
  const exercise = exerciseLogs.reduce((sum, log) => sum + (parseInt(log.durationMinutes) || 0), 0);
  const caloriesBurned = exerciseLogs.reduce((sum, log) => sum + (parseInt(log.caloriesBurned) || 0), 0);

  // Actions
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
      toast.success(`Logged ${ml} ml of water!`);
      fetchWaterLogs();
    } catch (error) {
      toast.error('Failed to log water');
    }
  };

  const handleResetWater = async () => {
    if (!currentUser) return;
    try {
      const deletePromises = waterLogs.map(log => deleteDoc(doc(db, 'waterLogs', log.id)));
      await Promise.all(deletePromises);
      toast.success('Water logs reset successfully');
      fetchWaterLogs();
    } catch (error) {
      toast.error('Failed to reset water logs');
    }
  };

  const handleSaveExercise = async (workout) => {
    if (!currentUser) return;
    const timestamp = getCurrentTimestamp();
    try {
      await addDoc(collection(db, 'exerciseLogs'), {
        userId: currentUser.uid,
        name: workout.name,
        durationMinutes: workout.durationMinutes,
        duration: workout.durationMinutes, // compatibility
        caloriesBurned: workout.caloriesBurned,
        intensity: workout.intensity,
        heartRate: workout.heartRate,
        timestamp: timestamp,
        date: todayStr,
        time: timestamp.split('T')[1].substring(0, 8),
        timezone: timestamp.substring(19)
      });
      toast.success('Workout logged successfully!');
      fetchExerciseLogs();
    } catch (error) {
      toast.error('Failed to log workout');
    }
  };

  const handleDeleteExercise = async (id) => {
    try {
      await deleteDoc(doc(db, 'exerciseLogs', id));
      toast.success('Workout deleted successfully');
      setSelectedExercise(null);
      fetchExerciseLogs();
    } catch (error) {
      toast.error('Failed to delete workout');
    }
  };

  const handleDeleteMeal = async (id) => {
    try {
      await deleteDoc(doc(db, 'foodLogs', id));
      toast.success('Meal deleted successfully');
      setSelectedMeal(null);
      fetchMeals();
    } catch (error) {
      toast.error('Failed to delete meal');
    }
  };

  const handleDuplicateMeal = async (meal) => {
    try {
      const timestamp = getCurrentTimestamp();
      await addDoc(collection(db, 'foodLogs'), {
        userId: currentUser.uid,
        foodName: meal.foodName,
        calories: meal.calories,
        protein: meal.protein,
        carbs: meal.carbs,
        fats: meal.fats,
        fiber: meal.fiber,
        sugar: meal.sugar,
        sodium: meal.sodium,
        mealType: meal.mealType,
        aiAnalysis: meal.aiAnalysis,
        healthScore: meal.healthScore,
        imageUrl: meal.imageUrl || "",
        timestamp: timestamp
      });
      toast.success('Meal duplicated successfully!');
      setSelectedMeal(null);
      fetchMeals();
    } catch (error) {
      toast.error('Failed to duplicate meal');
    }
  };

  const handleSaveEditMeal = async (e) => {
    e.preventDefault();
    try {
      const ref = doc(db, 'foodLogs', editMealData.id);
      await updateDoc(ref, {
        foodName: editMealData.foodName,
        calories: parseInt(editMealData.calories) || 0,
        protein: parseInt(editMealData.protein) || 0,
        carbs: parseInt(editMealData.carbs) || 0,
        fats: parseInt(editMealData.fats) || 0,
        fiber: parseInt(editMealData.fiber) || 0,
        sugar: parseInt(editMealData.sugar) || 0,
        sodium: parseInt(editMealData.sodium) || 0,
        mealType: editMealData.mealType,
        aiAnalysis: editMealData.aiAnalysis,
        healthScore: parseInt(editMealData.healthScore) || 70
      });
      toast.success('Meal updated successfully!');
      setSelectedMeal({ ...selectedMeal, ...editMealData });
      setIsEditingMeal(false);
      fetchMeals();
    } catch (error) {
      toast.error('Failed to update meal');
    }
  };

  return (
    <div className="page-container animate-fade-in" style={{ maxWidth: '1200px', margin: '0 auto', paddingBottom: '80px' }}>
      <div className="flex-center" style={{justifyContent: 'space-between', marginBottom: '40px'}}>
        <div>
          <h1 className="heading-1" style={{ margin: 0 }}>Daily Tracker</h1>
          <p style={{ color: '#6B7280', margin: '5px 0 0 0' }}>{new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}</p>
        </div>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="btn-primary flex-center" 
          style={{gap: '8px', padding: '12px 24px'}}
        >
          <Plus size={20}/> Add Entry
        </button>
      </div>

      {loading ? (
        <div className="flex-center" style={{height: '400px'}}>
          <Loader2 className="animate-spin text-primary" size={40} />
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 350px', gap: '30px', alignItems: 'start' }}>
          
          {/* Main Logs Column */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
            
            {/* Meals Container */}
            <div className="glass-card" style={{ padding: '30px' }}>
              <div className="flex-center" style={{ justifyContent: 'space-between', marginBottom: '20px' }}>
                <h3 className="heading-3" style={{ margin: 0 }}>Today's Meals</h3>
                <span style={{ fontSize: '0.85rem', color: 'var(--primary-green)', fontWeight: 600 }}>{meals.length} items logged</span>
              </div>
              
              {meals.length === 0 ? (
                <div className="flex-center" style={{flexDirection: 'column', height: '200px', color: '#6B7280', textAlign: 'center'}}>
                  <div style={{ padding: '15px', borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.02)', marginBottom: '16px' }}>
                    <Utensils size={36} style={{opacity: 0.2}} />
                  </div>
                  <p style={{ margin: 0, fontWeight: 500 }}>Log is empty for today.</p>
                </div>
              ) : (
                <div style={{display: 'flex', flexDirection: 'column', gap: '12px'}}>
                  {meals.map((meal) => (
                    <div 
                      key={meal.id} 
                      onClick={() => setSelectedMeal(meal)}
                      className="meal-item" 
                      style={{
                        padding: '16px 20px 24px 20px', 
                        backgroundColor: 'rgba(255,255,255,0.02)', 
                        borderRadius: '16px', 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'center',
                        border: '1px solid rgba(255,255,255,0.05)',
                        cursor: 'pointer',
                        transition: 'all 0.2s',
                        position: 'relative'
                      }}
                    >
                      <div className="flex-center" style={{gap: '16px'}}>
                        <div style={{
                          backgroundColor: 'rgba(16, 185, 129, 0.1)', 
                          width: '42px',
                          height: '42px',
                          borderRadius: '12px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}>
                          <Utensils className="text-primary" size={20} />
                        </div>
                        <div>
                          <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '10px' }}>
                            <h4 style={{margin: 0, fontSize: '1.05rem'}}>{meal.foodName}</h4>
                          </div>
                          <p style={{fontSize: '0.8rem', color: '#6B7280', margin: '4px 0 0 0'}}>
                            <span style={{ color: 'var(--primary-green)' }}>P: {meal.protein}g</span> • <span>C: {meal.carbs}g</span> • <span>F: {meal.fats}g</span>
                          </p>
                        </div>
                      </div>
                      <div className="flex-center" style={{ gap: '15px' }}>
                        <div style={{ textAlign: 'right' }}>
                          <span style={{ fontSize: '1.1rem', fontWeight: 700, display: 'block' }}>{meal.calories}</span>
                          <span style={{ fontSize: '0.7rem', color: '#6B7280', textTransform: 'uppercase' }}>kcal</span>
                        </div>
                        <Eye size={16} style={{ color: '#6B7280' }} />
                      </div>
                      <div style={{
                        position: 'absolute',
                        bottom: '6px',
                        right: '12px',
                        fontSize: '0.65rem',
                        color: '#6B7280',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '3px'
                      }}>
                        <Clock size={9} /> {formatLogTimestamp(meal.timestamp).replace('Today ', '')}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Exercise Logs Container */}
            <div className="glass-card" style={{ padding: '30px' }}>
              <div className="flex-center" style={{ justifyContent: 'space-between', marginBottom: '20px' }}>
                <h3 className="heading-3" style={{ margin: 0 }}>Workout Logs Today</h3>
                <span style={{ fontSize: '0.85rem', color: '#F59E0B', fontWeight: 600 }}>{exerciseLogs.length} routines logged</span>
              </div>
              
              {exerciseLogs.length === 0 ? (
                <div className="flex-center" style={{flexDirection: 'column', height: '150px', color: '#6B7280', textAlign: 'center'}}>
                  <p style={{ margin: 0, fontSize: '0.9rem' }}>No exercise logged today.</p>
                </div>
              ) : (
                <div style={{display: 'flex', flexDirection: 'column', gap: '12px'}}>
                  {exerciseLogs.map((ex) => (
                    <div 
                      key={ex.id}
                      onClick={() => setSelectedExercise(ex)}
                      style={{
                        padding: '16px 20px 24px 20px', 
                        backgroundColor: 'rgba(255,255,255,0.02)', 
                        borderRadius: '16px', 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'center',
                        border: '1px solid rgba(255,255,255,0.05)',
                        cursor: 'pointer',
                        position: 'relative'
                      }}
                    >
                      <div className="flex-center" style={{gap: '16px'}}>
                        <div style={{
                          backgroundColor: 'rgba(245, 158, 11, 0.1)', 
                          width: '42px',
                          height: '42px',
                          borderRadius: '12px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}>
                          <ActivityIcon style={{ color: '#F59E0B' }} size={20} />
                        </div>
                        <div>
                          <h4 style={{margin: 0, fontSize: '1.05rem'}}>{ex.name}</h4>
                          <p style={{fontSize: '0.8rem', color: '#6B7280', margin: '4px 0 0 0'}}>
                            Duration: {ex.durationMinutes} mins | HR: {ex.heartRate} bpm | Intensity: {ex.intensity}
                          </p>
                        </div>
                      </div>
                      <div className="flex-center" style={{ gap: '15px' }}>
                        <div style={{ textAlign: 'right' }}>
                          <span style={{ fontSize: '1.1rem', fontWeight: 700, display: 'block', color: '#F59E0B' }}>-{ex.caloriesBurned}</span>
                          <span style={{ fontSize: '0.7rem', color: '#6B7280', textTransform: 'uppercase' }}>kcal</span>
                        </div>
                        <Eye size={16} style={{ color: '#6B7280' }} />
                      </div>
                      <div style={{
                        position: 'absolute',
                        bottom: '6px',
                        right: '12px',
                        fontSize: '0.65rem',
                        color: '#6B7280',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '3px'
                      }}>
                        <Clock size={9} /> {formatLogTimestamp(ex.timestamp).replace('Today ', '')}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Water Logs List Container */}
            <div className="glass-card" style={{ padding: '30px' }}>
              <div className="flex-center" style={{ justifyContent: 'space-between', marginBottom: '20px' }}>
                <h3 className="heading-3" style={{ margin: 0 }}>Water Logs Today</h3>
                {waterLogs.length > 0 && (
                  <button onClick={handleResetWater} style={{ background: 'none', border: 'none', color: '#EF4444', fontSize: '0.8rem', cursor: 'pointer', fontWeight: 600 }}>
                    Reset Logs
                  </button>
                )}
              </div>
              
              {waterLogs.length === 0 ? (
                <div className="flex-center" style={{flexDirection: 'column', height: '120px', color: '#6B7280', textAlign: 'center'}}>
                  <p style={{ margin: 0, fontSize: '0.9rem' }}>No water logs recorded today.</p>
                </div>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '12px' }}>
                  {waterLogs.map((log) => (
                    <div 
                      key={log.id} 
                      style={{
                        padding: '12px 16px',
                        background: 'rgba(59, 130, 246, 0.03)',
                        border: '1px solid rgba(59, 130, 246, 0.12)',
                        borderRadius: '12px',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                      }}
                    >
                      <div className="flex-center" style={{ gap: '8px', justifyContent: 'flex-start' }}>
                        <Droplet className="text-secondary" size={16} />
                        <span style={{ fontSize: '0.9rem', fontWeight: 700, color: 'white' }}>{log.amount} ml</span>
                      </div>
                      <span style={{ fontSize: '0.7rem', color: '#6B7280' }}>
                        {formatLogTimestamp(log.timestamp).replace('Today ', '')}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

          </div>

          {/* Cards Column */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <DashboardCard 
              title="Total Calories" 
              value={totalCalories.toLocaleString()} 
              subtitle={`kcal of ${targets.calories.toLocaleString()}`} 
              progress={(totalCalories / targets.calories) * 100} 
              icon={<ActivityIcon className="text-primary" size={20} />}
            />
            
            <div onClick={() => setIsWaterModalOpen(true)} style={{ cursor: 'pointer' }}>
              <DashboardCard 
                title="Water Intake" 
                value={`${(water / 1000).toFixed(2)} / ${(targets.water ? targets.water / 1000 : 2.8).toFixed(1)} L`} 
                subtitle="Liters (Tap to add)" 
                progress={(water / (targets.water || 2800)) * 100} 
                icon={<Droplet className="text-secondary" size={20} />}
              />
            </div>
            
            <div onClick={() => setIsExerciseModalOpen(true)} style={{ cursor: 'pointer' }}>
              <DashboardCard 
                title="Exercise duration" 
                value={`${exercise} mins`} 
                subtitle={`${caloriesBurned} kcal burned (Tap to add)`} 
                progress={(exercise / 60) * 100} 
                icon={<ActivityIcon style={{ color: '#F59E0B' }} size={20} />}
              />
            </div>

            <div className="glass-card" style={{ padding: '25px', backgroundColor: 'rgba(16, 185, 129, 0.03)', border: '1px dashed var(--primary-green)' }}>
              <h4 style={{ margin: '0 0 10px 0', fontSize: '1rem' }}>Daily Progress</h4>
              <p style={{ fontSize: '0.85rem', color: '#6B7280', lineHeight: 1.5, margin: 0 }}>
                You are currently at <strong>{Math.round((totalCalories / targets.calories) * 100)}%</strong> of your daily goal ({targets.calories} kcal).
              </p>
            </div>
          </div>

        </div>
      )}

      {/* Manual Meal Add Entry modal */}
      <ManualEntryModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onRefresh={fetchMeals}
      />

      {/* Exercise Logger Modal */}
      <ExerciseModal 
        isOpen={isExerciseModalOpen} 
        onClose={() => setIsExerciseModalOpen(false)}
        onSave={handleSaveExercise}
        userWeight={profile?.weight || 70}
      />

      {/* Water Logger Modal */}
      <WaterModal 
        isOpen={isWaterModalOpen}
        onClose={() => setIsWaterModalOpen(false)}
        onAdd={handleAddWater}
      />

      {/* DETAILED MEAL POPUP DIALOG */}
      {selectedMeal && (
        <div className="modal-overlay flex-center" style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
          backgroundColor: 'rgba(11, 15, 23, 0.9)', zIndex: 1100, backdropFilter: 'blur(12px)',
          padding: '20px', boxSizing: 'border-box'
        }}>
          <div className="glass-card" style={{
            width: '100%', maxWidth: '500px', padding: '30px', position: 'relative',
            backgroundColor: '#141b26', border: '1px solid rgba(255, 255, 255, 0.08)',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)', borderRadius: '24px',
            boxSizing: 'border-box', overflowY: 'auto', maxHeight: '90vh'
          }}>
            <button 
              onClick={() => { setSelectedMeal(null); setIsEditingMeal(false); }}
              style={{ position: 'absolute', top: '22px', right: '22px', background: 'none', border: 'none', cursor: 'pointer', color: '#9CA3AF' }}
            >
              <X size={20} />
            </button>

            {isEditingMeal ? (
              // Edit meal details form
              <form onSubmit={handleSaveEditMeal} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <h3 className="heading-3" style={{ margin: 0, color: 'white' }}>Edit Meal Entry</h3>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.8rem', color: '#9CA3AF' }}>Food Name</label>
                  <input 
                    type="text" 
                    className="input-field" 
                    value={editMealData.foodName}
                    onChange={(e) => setEditMealData({ ...editMealData, foodName: e.target.value })}
                    style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)' }}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.8rem', color: '#9CA3AF' }}>Calories (kcal)</label>
                    <input 
                      type="number" 
                      className="input-field" 
                      value={editMealData.calories}
                      onChange={(e) => setEditMealData({ ...editMealData, calories: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)' }}
                    />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.8rem', color: '#9CA3AF' }}>Meal Type</label>
                    <select
                      className="input-field" 
                      value={editMealData.mealType}
                      onChange={(e) => setEditMealData({ ...editMealData, mealType: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)' }}
                    >
                      <option value="Breakfast">Breakfast</option>
                      <option value="Lunch">Lunch</option>
                      <option value="Dinner">Dinner</option>
                      <option value="Snack">Snack</option>
                    </select>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.75rem', color: '#10B981' }}>Protein (g)</label>
                    <input 
                      type="number" 
                      value={editMealData.protein}
                      onChange={(e) => setEditMealData({ ...editMealData, protein: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', textAlign: 'center' }}
                    />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.75rem', color: '#3B82F6' }}>Carbs (g)</label>
                    <input 
                      type="number" 
                      value={editMealData.carbs}
                      onChange={(e) => setEditMealData({ ...editMealData, carbs: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', textAlign: 'center' }}
                    />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.75rem', color: '#F59E0B' }}>Fats (g)</label>
                    <input 
                      type="number" 
                      value={editMealData.fats}
                      onChange={(e) => setEditMealData({ ...editMealData, fats: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', textAlign: 'center' }}
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.75rem', color: '#9CA3AF' }}>Fiber (g)</label>
                    <input 
                      type="number" 
                      value={editMealData.fiber}
                      onChange={(e) => setEditMealData({ ...editMealData, fiber: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', textAlign: 'center' }}
                    />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.75rem', color: '#9CA3AF' }}>Sugar (g)</label>
                    <input 
                      type="number" 
                      value={editMealData.sugar}
                      onChange={(e) => setEditMealData({ ...editMealData, sugar: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', textAlign: 'center' }}
                    />
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <label style={{ fontSize: '0.75rem', color: '#9CA3AF' }}>Sodium (mg)</label>
                    <input 
                      type="number" 
                      value={editMealData.sodium}
                      onChange={(e) => setEditMealData({ ...editMealData, sodium: e.target.value })}
                      style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', textAlign: 'center' }}
                    />
                  </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.8rem', color: '#9CA3AF' }}>AI Coach Analysis</label>
                  <textarea 
                    rows="3"
                    value={editMealData.aiAnalysis}
                    onChange={(e) => setEditMealData({ ...editMealData, aiAnalysis: e.target.value })}
                    style={{ width: '100%', padding: '10px', background: '#0B0F17', color: 'white', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.08)', fontFamily: 'inherit', resize: 'vertical' }}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginTop: '10px' }}>
                  <button type="button" onClick={() => setIsEditingMeal(false)} className="btn-outline" style={{ height: '48px' }}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-primary" style={{ height: '48px' }}>
                    Save Changes
                  </button>
                </div>

              </form>
            ) : (
              // View meal details mode
              <div>
                <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                  <div style={{ 
                    fontSize: '64px', margin: '0 auto 12px auto', width: '100px', height: '100px', 
                    borderRadius: '20px', background: 'linear-gradient(135deg, var(--primary-green), var(--secondary-blue))',
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                  }}>
                    {selectedMeal.imageUrl ? <img src={selectedMeal.imageUrl} alt="" style={{ width: '100%', height: '100%', borderRadius: '20px', objectFit: 'cover' }} /> : "🥗"}
                  </div>
                  <h3 className="heading-3" style={{ margin: 0, color: 'white', fontSize: '1.4rem' }}>{selectedMeal.foodName}</h3>
                  <p style={{ color: '#9CA3AF', fontSize: '0.85rem', margin: '4px 0 0 0' }}>
                    {selectedMeal.mealType} • {formatLogTimestamp(selectedMeal.timestamp)}
                  </p>
                </div>

                {/* Macro Badges Grid */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '10px', marginBottom: '24px' }}>
                  <div style={{ padding: '10px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '12px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: '#6B7280', display: 'block', fontWeight: 600 }}>CALORIES</span>
                    <strong style={{ fontSize: '1.1rem', color: 'white' }}>{selectedMeal.calories} <span style={{ fontSize: '0.7rem' }}>kcal</span></strong>
                  </div>
                  <div style={{ padding: '10px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '12px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: 'var(--primary-green)', display: 'block', fontWeight: 600 }}>PROTEIN</span>
                    <strong style={{ fontSize: '1.1rem', color: 'var(--primary-green)' }}>{selectedMeal.protein}g</strong>
                  </div>
                  <div style={{ padding: '10px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '12px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: 'var(--secondary-blue)', display: 'block', fontWeight: 600 }}>CARBS</span>
                    <strong style={{ fontSize: '1.1rem', color: 'var(--secondary-blue)' }}>{selectedMeal.carbs}g</strong>
                  </div>
                  <div style={{ padding: '10px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', borderRadius: '12px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: '#F59E0B', display: 'block', fontWeight: 600 }}>FATS</span>
                    <strong style={{ fontSize: '1.1rem', color: '#F59E0B' }}>{selectedMeal.fats}g</strong>
                  </div>
                </div>

                {/* Additional micro-nutrients */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', background: 'rgba(255,255,255,0.01)', border: '1px solid rgba(255,255,255,0.04)', padding: '16px', borderRadius: '16px', marginBottom: '24px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <span style={{ color: '#9CA3AF' }}>Fiber</span>
                    <span style={{ fontWeight: 600, color: 'white' }}>{selectedMeal.fiber || 0} g</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <span style={{ color: '#9CA3AF' }}>Sugar</span>
                    <span style={{ fontWeight: 600, color: 'white' }}>{selectedMeal.sugar || 0} g</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <span style={{ color: '#9CA3AF' }}>Sodium</span>
                    <span style={{ fontWeight: 600, color: 'white' }}>{selectedMeal.sodium || 0} mg</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <span style={{ color: '#9CA3AF' }}>AI Health Score</span>
                    <span style={{ fontWeight: 700, color: 'var(--primary-green)' }}>{selectedMeal.healthScore || 70} / 100</span>
                  </div>
                </div>

                {/* AI Coach recommendation summary box */}
                <div style={{ padding: '16px', background: 'rgba(16, 185, 129, 0.04)', border: '1px solid rgba(16, 185, 129, 0.15)', borderRadius: '16px', marginBottom: '24px' }}>
                  <h4 style={{ margin: '0 0 6px 0', fontSize: '0.9rem', color: 'var(--primary-green)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    🤖 AI Analysis
                  </h4>
                  <p style={{ margin: 0, fontSize: '0.8rem', color: '#D1D5DB', lineHeight: 1.5 }}>
                    {selectedMeal.aiAnalysis || "A nutrient-rich selection. Consuming adequate protein helps rebuild active muscle fibers, while balanced hydration supports standard metabolic processes."}
                  </p>
                </div>

                {/* Actions row */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
                  <button 
                    onClick={() => {
                      setEditMealData({ ...selectedMeal });
                      setIsEditingMeal(true);
                    }}
                    className="btn-outline flex-center" 
                    style={{ gap: '6px', height: '48px', color: '#3B82F6', borderColor: '#3B82F6' }}
                  >
                    <Edit size={16} /> Edit
                  </button>
                  <button 
                    onClick={() => handleDuplicateMeal(selectedMeal)}
                    className="btn-outline flex-center" 
                    style={{ gap: '6px', height: '48px', color: 'var(--primary-green)', borderColor: 'var(--primary-green)' }}
                  >
                    <Copy size={16} /> Duplicate
                  </button>
                  <button 
                    onClick={() => handleDeleteMeal(selectedMeal.id)}
                    className="btn-outline flex-center" 
                    style={{ gap: '6px', height: '48px', color: '#EF4444', borderColor: '#EF4444' }}
                  >
                    <Trash2 size={16} /> Delete
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* DETAILED EXERCISE POPUP DIALOG */}
      {selectedExercise && (
        <div className="modal-overlay flex-center" style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
          backgroundColor: 'rgba(11, 15, 23, 0.9)', zIndex: 1100, backdropFilter: 'blur(12px)',
          padding: '20px', boxSizing: 'border-box'
        }}>
          <div className="glass-card" style={{
            width: '100%', maxWidth: '400px', padding: '30px', position: 'relative',
            backgroundColor: '#141b26', border: '1px solid rgba(255, 255, 255, 0.08)',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)', borderRadius: '24px',
            boxSizing: 'border-box'
          }}>
            <button 
              onClick={() => setSelectedExercise(null)}
              style={{ position: 'absolute', top: '22px', right: '22px', background: 'none', border: 'none', cursor: 'pointer', color: '#9CA3AF' }}
            >
              <X size={20} />
            </button>

            <div style={{ textAlign: 'center', marginBottom: '24px' }}>
              <div style={{ 
                fontSize: '48px', margin: '0 auto 12px auto', width: '80px', height: '80px', 
                borderRadius: '50%', background: 'rgba(245, 158, 11, 0.1)',
                display: 'flex', alignItems: 'center', justifyContent: 'center'
              }}>
                <ActivityIcon size={36} style={{ color: '#F59E0B' }} />
              </div>
              <h3 className="heading-3" style={{ margin: 0, color: 'white', fontSize: '1.3rem' }}>{selectedExercise.name}</h3>
              <p style={{ color: '#9CA3AF', fontSize: '0.8rem', margin: '4px 0 0 0' }}>
                {formatLogTimestamp(selectedExercise.timestamp)}
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', background: 'rgba(255,255,255,0.01)', border: '1px solid rgba(255,255,255,0.04)', padding: '16px', borderRadius: '16px', marginBottom: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                <span style={{ color: '#9CA3AF' }}>Duration</span>
                <span style={{ fontWeight: 600, color: 'white' }}>{selectedExercise.durationMinutes} minutes</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                <span style={{ color: '#9CA3AF' }}>Energy Expended</span>
                <span style={{ fontWeight: 600, color: '#F59E0B' }}>{selectedExercise.caloriesBurned} kcal</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                <span style={{ color: '#9CA3AF' }}>Avg Heart Rate</span>
                <span style={{ fontWeight: 600, color: '#EF4444', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Heart size={14} fill="#EF4444" /> {selectedExercise.heartRate || 120} bpm
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                <span style={{ color: '#9CA3AF' }}>Intensity</span>
                <span style={{ fontWeight: 600, color: 'white' }}>{selectedExercise.intensity}</span>
              </div>
            </div>

            <button 
              onClick={() => handleDeleteExercise(selectedExercise.id)}
              className="btn-outline flex-center w-full" 
              style={{ gap: '8px', height: '48px', color: '#EF4444', borderColor: '#EF4444' }}
            >
              <Trash2 size={16} /> Delete Workout
            </button>
          </div>
        </div>
      )}

    </div>
  );
};

// Simple helper local component for Close icon if not imported
const X = ({ size }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
);

export default DailyTracker;
