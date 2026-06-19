import React, { useState, useMemo } from 'react';
import { X, Activity, Bolt, Heart } from 'lucide-react';
import toast from 'react-hot-toast';

const ExerciseModal = ({ isOpen, onClose, onSave, userWeight = 70 }) => {
  const [name, setName] = useState('Cardio');
  const [customName, setCustomName] = useState('');
  const [duration, setDuration] = useState('');
  const [heartRate, setHeartRate] = useState('120');
  const [intensity, setIntensity] = useState('Moderate');

  const categories = ["Cardio", "HIIT", "Yoga", "Chest", "Back", "Legs", "Shoulders", "Arms", "Custom"];

  const exerciseName = name === 'Custom' ? customName : name;

  const estimatedCalories = useMemo(() => {
    const dur = parseInt(duration) || 0;
    if (dur <= 0 || !exerciseName) return 0;

    let met = 3.5;
    const cleanName = exerciseName.toLowerCase().trim();
    if (cleanName.includes("cardio") || cleanName.includes("run")) {
      met = intensity === "Low" ? 7.0 : intensity === "High" ? 12.5 : 10.0;
    } else if (cleanName.includes("hiit")) {
      met = intensity === "Low" ? 8.0 : intensity === "High" ? 14.0 : 11.0;
    } else if (cleanName.includes("yoga") || cleanName.includes("stretch")) {
      met = intensity === "Low" ? 2.0 : intensity === "High" ? 4.0 : 3.0;
    } else if (["chest", "back", "legs", "shoulders", "arms", "weights", "strength"].some(x => cleanName.includes(x))) {
      met = intensity === "Low" ? 3.5 : intensity === "High" ? 8.5 : 5.5;
    } else {
      met = intensity === "Low" ? 2.5 : intensity === "High" ? 4.5 : 3.5;
    }
    return Math.round(met * userWeight * (dur / 60));
  }, [exerciseName, duration, intensity, userWeight]);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const durVal = parseInt(duration);
    if (!exerciseName.trim()) {
      toast.error('Please select or type an exercise name');
      return;
    }
    if (isNaN(durVal) || durVal <= 0) {
      toast.error('Please enter a valid duration');
      return;
    }
    const hrVal = parseInt(heartRate) || 120;

    onSave({
      name: exerciseName,
      durationMinutes: durVal,
      caloriesBurned: estimatedCalories,
      intensity: intensity,
      heartRate: hrVal
    });

    setDuration('');
    setCustomName('');
    setName('Cardio');
    setIntensity('Moderate');
    setHeartRate('120');
    onClose();
  };

  return (
    <div className="modal-overlay flex-center" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      backgroundColor: 'rgba(11, 15, 23, 0.85)',
      zIndex: 1200,
      backdropFilter: 'blur(12px)',
      padding: '20px',
      boxSizing: 'border-box'
    }}>
      <div className="glass-card animate-slide-up" style={{
        width: '100%',
        maxWidth: '450px',
        padding: '25px',
        position: 'relative',
        backgroundColor: '#141b26',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)',
        borderRadius: '24px',
        boxSizing: 'border-box'
      }}>
        <button 
          onClick={onClose}
          style={{ position: 'absolute', top: '22px', right: '22px', background: 'none', border: 'none', cursor: 'pointer', color: '#9CA3AF' }}
        >
          <X size={20} />
        </button>

        <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '12px', marginBottom: '20px' }}>
          <div style={{ backgroundColor: 'rgba(245, 158, 11, 0.1)', padding: '10px', borderRadius: '12px' }}>
            <Activity style={{ color: '#F59E0B' }} size={24} />
          </div>
          <h3 className="heading-3" style={{ color: 'white', margin: 0, fontSize: '1.25rem' }}>Log Workout Activity</h3>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {/* Exercise Library / Category Dropdown */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ color: '#E5E7EB', fontSize: '0.85rem', fontWeight: 500 }}>Workout Category</label>
            <select
              value={name}
              onChange={(e) => setName(e.target.value)}
              style={{
                width: '100%',
                padding: '12px',
                borderRadius: '12px',
                border: '1px solid rgba(255,255,255,0.08)',
                background: '#0B0F17',
                color: 'white',
                outline: 'none',
                fontSize: '0.9rem'
              }}
            >
              {categories.map(cat => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          {/* Custom Name field if 'Custom' is selected */}
          {name === 'Custom' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ color: '#E5E7EB', fontSize: '0.85rem', fontWeight: 500 }}>Exercise Name</label>
              <input
                type="text"
                placeholder="e.g. Swimming, Cycling"
                value={customName}
                onChange={(e) => setCustomName(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '12px',
                  border: '1px solid rgba(255,255,255,0.08)',
                  background: '#0B0F17',
                  color: 'white',
                  outline: 'none',
                  fontSize: '0.9rem'
                }}
              />
            </div>
          )}

          {/* Duration & HR Row */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ color: '#E5E7EB', fontSize: '0.85rem', fontWeight: 500 }}>Duration (mins)</label>
              <input
                type="number"
                placeholder="e.g. 45"
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '12px',
                  border: '1px solid rgba(255,255,255,0.08)',
                  background: '#0B0F17',
                  color: 'white',
                  outline: 'none',
                  fontSize: '0.9rem',
                  boxSizing: 'border-box'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label style={{ color: '#E5E7EB', fontSize: '0.85rem', fontWeight: 500 }}>Avg HR (bpm)</label>
              <input
                type="number"
                placeholder="e.g. 120"
                value={heartRate}
                onChange={(e) => setHeartRate(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '12px',
                  border: '1px solid rgba(255,255,255,0.08)',
                  background: '#0B0F17',
                  color: 'white',
                  outline: 'none',
                  fontSize: '0.9rem',
                  boxSizing: 'border-box'
                }}
              />
            </div>
          </div>

          {/* Intensity selector buttons */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label style={{ color: '#E5E7EB', fontSize: '0.85rem', fontWeight: 500 }}>Intensity Level</label>
            <div style={{
              display: 'flex',
              background: 'rgba(255,255,255,0.02)',
              border: '1px solid rgba(255,255,255,0.08)',
              borderRadius: '10px',
              overflow: 'hidden',
              padding: '2px'
            }}>
              {["Low", "Moderate", "High"].map(item => {
                const isSelected = intensity === item;
                return (
                  <button
                    key={item}
                    type="button"
                    onClick={() => setIntensity(item)}
                    style={{
                      flex: 1,
                      padding: '10px 0',
                      border: 'none',
                      background: isSelected ? '#F59E0B' : 'transparent',
                      color: isSelected ? 'white' : '#9CA3AF',
                      fontWeight: 600,
                      fontSize: '0.8rem',
                      cursor: 'pointer',
                      borderRadius: '8px',
                      transition: 'all 0.2s'
                    }}
                  >
                    {item}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Real-time estimated calorie burn */}
          {estimatedCalories > 0 && (
            <div style={{
              padding: '12px',
              background: 'rgba(245, 158, 11, 0.08)',
              border: '1px solid rgba(245, 158, 11, 0.3)',
              borderRadius: '12px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div className="flex-center" style={{ gap: '6px', justifyContent: 'flex-start' }}>
                <Bolt size={16} style={{ color: '#F59E0B' }} />
                <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#F59E0B' }}>AI Calorie Burn Estimate</span>
              </div>
              <span style={{ fontSize: '0.9rem', fontWeight: 800, color: '#F59E0B' }}>~{estimatedCalories} kcal</span>
            </div>
          )}

          <button 
            type="submit" 
            className="btn-primary w-full flex-center" 
            style={{ gap: '10px', height: '52px', marginTop: '10px', backgroundColor: '#F59E0B', borderColor: '#F59E0B' }}
          >
            Log Workout
          </button>
        </form>
      </div>
    </div>
  );
};

export default ExerciseModal;
