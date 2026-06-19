import React, { useState, useEffect } from 'react';
import { Activity, Flame, Heart, Clock, Save, Plus, Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';
import { db } from '../firebase/config';
import { collection, addDoc, query, where, getDocs, serverTimestamp } from 'firebase/firestore';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import PageTransition from '../animations/PageTransition';

const ExerciseDetails = () => {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [exercises, setExercises] = useState([]);
  
  const [formData, setFormData] = useState({
    workoutName: '',
    duration: '',
    caloriesBurned: '',
    heartRate: ''
  });

  // Fetch today's exercise logs
  const fetchExercises = async () => {
    if (!currentUser) return;
    setFetching(true);
    try {
      const q = query(
        collection(db, 'exerciseLogs'),
        where('userId', '==', currentUser.uid)
      );
      
      const querySnapshot = await getDocs(q);
      const startOfToday = new Date();
      startOfToday.setHours(0, 0, 0, 0);

      const fetchedExercises = querySnapshot.docs
        .map(doc => {
          const data = doc.data();
          const timestamp = data.timestamp ? data.timestamp.toDate() : new Date();
          return {
            id: doc.id,
            ...data,
            timestampObj: timestamp,
            time: timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          };
        })
        .filter(ex => ex.timestampObj >= startOfToday)
        .sort((a, b) => b.timestampObj - a.timestampObj);

      setExercises(fetchedExercises);
    } catch (error) {
      console.error("Error fetching exercise logs:", error);
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    fetchExercises();
  }, [currentUser]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.workoutName || !formData.duration || !formData.caloriesBurned) {
      toast.error('Please enter workout name, duration, and calories burned');
      return;
    }

    setLoading(true);
    try {
      await addDoc(collection(db, 'exerciseLogs'), {
        userId: currentUser.uid,
        workoutName: formData.workoutName,
        duration: parseInt(formData.duration) || 0,
        caloriesBurned: parseInt(formData.caloriesBurned) || 0,
        heartRate: parseInt(formData.heartRate) || 0,
        timestamp: serverTimestamp()
      });

      toast.success('Workout logged successfully!');
      setFormData({ workoutName: '', duration: '', caloriesBurned: '', heartRate: '' });
      fetchExercises();
    } catch (error) {
      console.error("Error logging workout: ", error);
      toast.error('Failed to log workout');
    } finally {
      setLoading(false);
    }
  };

  const totalCalories = exercises.reduce((sum, ex) => sum + (parseInt(ex.caloriesBurned) || 0), 0);
  const totalDuration = exercises.reduce((sum, ex) => sum + (parseInt(ex.duration) || 0), 0);

  return (
    <PageTransition className="page-container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div className="flex-center" style={{justifyContent: 'space-between', marginBottom: '40px'}}>
        <div>
          <h1 className="heading-1" style={{ margin: 0 }}>Exercise Details</h1>
          <p style={{ color: '#6B7280', margin: '5px 0 0 0' }}>Track your active output and calorie reduction</p>
        </div>
      </div>

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '40px', alignItems: 'flex-start' }}>
        
        {/* Form Section */}
        <div className="glass-card" style={{ flex: '1 1 400px', padding: '30px' }}>
          <h3 className="heading-3 flex-center" style={{ justifyContent: 'flex-start', gap: '10px', marginBottom: '25px' }}>
            <Activity className="text-primary" size={24} /> Log Workout
          </h3>
          
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label className="label" style={{ color: '#E5E7EB', margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>Workout Name</label>
              <input 
                type="text" 
                className="input-field" 
                placeholder="e.g. Morning Run, HIIT, Weightlifting"
                style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '12px', borderRadius: '12px', boxSizing: 'border-box' }}
                value={formData.workoutName}
                onChange={(e) => setFormData({...formData, workoutName: e.target.value})}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label className="label" style={{ color: '#E5E7EB', margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>Duration (minutes)</label>
              <input 
                type="number" 
                className="input-field" 
                placeholder="e.g. 45"
                style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '12px', borderRadius: '12px', boxSizing: 'border-box' }}
                value={formData.duration}
                onChange={(e) => setFormData({...formData, duration: e.target.value})}
              />
            </div>

            <div style={{ display: 'flex', gap: '16px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1 }}>
                <label className="label" style={{ color: '#E5E7EB', margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>Calories Reduced (kcal)</label>
                <div style={{ position: 'relative' }}>
                  <Flame size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: '#F59E0B' }} />
                  <input 
                    type="number" 
                    className="input-field" 
                    placeholder="e.g. 320"
                    style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '12px 12px 12px 38px', borderRadius: '12px', boxSizing: 'border-box' }}
                    value={formData.caloriesBurned}
                    onChange={(e) => setFormData({...formData, caloriesBurned: e.target.value})}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', flex: 1 }}>
                <label className="label" style={{ color: '#E5E7EB', margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>Avg Heart Rate (BPM)</label>
                <div style={{ position: 'relative' }}>
                  <Heart size={18} style={{ position: 'absolute', left: '12px', top: '14px', color: '#EC4899' }} />
                  <input 
                    type="number" 
                    className="input-field" 
                    placeholder="e.g. 135"
                    style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '12px 12px 12px 38px', borderRadius: '12px', boxSizing: 'border-box' }}
                    value={formData.heartRate}
                    onChange={(e) => setFormData({...formData, heartRate: e.target.value})}
                  />
                </div>
              </div>
            </div>

            <button 
              type="submit" 
              className="btn-primary w-full flex-center" 
              disabled={loading}
              style={{ gap: '10px', height: '52px', marginTop: '10px', width: '100%', borderRadius: '12px' }}
            >
              {loading ? 'Saving...' : <><Save size={18} /> Save Workout</>}
            </button>
          </form>
        </div>

        {/* History Section */}
        <div className="glass-card" style={{ flex: '1 1 400px', padding: '30px', minHeight: '400px' }}>
          <div className="flex-center" style={{ justifyContent: 'space-between', marginBottom: '25px' }}>
            <h3 className="heading-3" style={{ margin: 0 }}>Today's Activity</h3>
            <span style={{ fontSize: '0.85rem', color: 'var(--primary-green)', fontWeight: 600 }}>{totalCalories} kcal burned</span>
          </div>

          {fetching ? (
            <div className="flex-center" style={{height: '200px'}}>
              <Loader2 className="animate-spin text-primary" size={40} />
            </div>
          ) : exercises.length === 0 ? (
            <div className="flex-center" style={{flexDirection: 'column', height: '200px', color: '#6B7280', textAlign: 'center'}}>
              <div style={{ padding: '20px', borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.02)', marginBottom: '20px' }}>
                <Activity size={48} style={{opacity: 0.2}} />
              </div>
              <p style={{ margin: 0, fontWeight: 500 }}>No workouts logged today.</p>
            </div>
          ) : (
            <div style={{display: 'flex', flexDirection: 'column', gap: '16px'}}>
              {exercises.map((ex) => (
                <motion.div 
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  key={ex.id} 
                  style={{
                    padding: '20px', 
                    backgroundColor: 'rgba(255,255,255,0.02)', 
                    borderRadius: '16px', 
                    border: '1px solid rgba(255,255,255,0.05)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <div className="flex-center" style={{ gap: '10px' }}>
                      <h4 style={{margin: 0, fontSize: '1.1rem'}}>{ex.workoutName}</h4>
                      <span style={{ fontSize: '0.75rem', color: '#6B7280', backgroundColor: 'rgba(255,255,255,0.05)', padding: '2px 8px', borderRadius: '10px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Clock size={12} /> {ex.time}
                      </span>
                    </div>
                    <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#F59E0B' }}>-{ex.caloriesBurned} kcal</span>
                  </div>
                  
                  <div style={{ display: 'flex', gap: '16px', fontSize: '0.85rem', color: '#9CA3AF' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Clock size={14} className="text-secondary" /> {ex.duration} mins
                    </span>
                    {ex.heartRate > 0 && (
                      <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Heart size={14} style={{ color: '#EC4899' }} /> {ex.heartRate} BPM avg
                      </span>
                    )}
                  </div>
                </motion.div>
              ))}
            </div>
          )}
        </div>

      </div>
    </PageTransition>
  );
};

export default ExerciseDetails;
