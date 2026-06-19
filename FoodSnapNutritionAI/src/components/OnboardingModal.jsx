import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, ArrowRight, ArrowLeft, Check, Flame, TrendingUp, Dumbbell, Heart } from 'lucide-react';
import { db } from '../firebase/config';
import { doc, setDoc } from 'firebase/firestore';
import { calculateNutritionTargets } from '../services/nutritionHelper';
import { getCurrentTimestamp } from '../utils/dateTimeHelper';
import toast from 'react-hot-toast';

const OnboardingModal = ({ isOpen, onClose, currentUser, onComplete }) => {
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);

  // Form States
  const [age, setAge] = useState(28);
  const [gender, setGender] = useState('Female');
  const [height, setHeight] = useState(165);
  const [weight, setWeight] = useState(68);
  const [goal, setGoal] = useState('Weight Loss');
  const [targetWeight, setTargetWeight] = useState(62);
  const [activityLevel, setActivityLevel] = useState('Moderate');
  const [dietaryPreference, setDietaryPreference] = useState('Vegetarian');

  useEffect(() => {
    if (isOpen) {
      setStep(1);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleNext = () => {
    if (step === 1) {
      if (!age || age < 10 || age > 100) return toast.error('Please enter a valid age between 10 and 100');
      if (!height || height < 100 || height > 250) return toast.error('Please enter a valid height between 100 and 250 cm');
      if (!weight || weight < 30 || weight > 250) return toast.error('Please enter a valid weight between 30 and 250 kg');
    }
    if (step === 2) {
      if (!targetWeight || targetWeight < 30 || targetWeight > 250) {
        return toast.error('Please enter a valid target weight between 30 and 250 kg');
      }
    }
    setStep(prev => prev + 1);
  };

  const handleBack = () => {
    setStep(prev => prev - 1);
  };

  const handleSubmit = async () => {
    setLoading(true);
    
    // Calculate targets
    const profileInput = {
      weight: parseFloat(weight),
      height: parseFloat(height),
      age: parseInt(age),
      gender,
      activityLevel,
      goal
    };
    
    const targets = calculateNutritionTargets(profileInput);
    const bmi = parseFloat((profileInput.weight / ((profileInput.height / 100) * (profileInput.height / 100))).toFixed(2));
    const syncTime = getCurrentTimestamp();

    const profileData = {
      name: currentUser?.displayName || currentUser?.email?.split('@')[0] || 'User',
      email: currentUser?.email || '',
      goal: goal,
      targetCalories: targets.calories,
      targetProtein: targets.protein,
      targetCarbs: targets.carbs,
      targetFats: targets.fats,
      weight: parseFloat(weight),
      targetWeight: parseFloat(targetWeight),
      height: parseFloat(height),
      age: parseInt(age),
      gender: gender,
      activityLevel: activityLevel,
      dietaryPreference: dietaryPreference,
      bmi: bmi,
      bmr: targets.bmr,
      tdee: targets.tdee,
      targetWater: targets.targetWater,
      showOnboarding: false,
      lastSynced: syncTime
    };

    try {
      if (currentUser) {
        // Save to Firestore
        await setDoc(doc(db, 'profiles', currentUser.uid), profileData);
        // Save to LocalStorage (as cache backup)
        localStorage.setItem(`user_profile_${currentUser.uid}`, JSON.stringify(profileData));
        
        toast.success('Fitness profile updated successfully!');
        if (onComplete) onComplete(profileData);
        onClose();
      } else {
        toast.error('User session not found');
      }
    } catch (error) {
      console.error('Error saving profile:', error);
      toast.error('Could not save profile. Please check connectivity.');
    } finally {
      setLoading(false);
    }
  };

  const goalsList = [
    {
      id: 'Weight Loss',
      title: 'Weight Loss',
      desc: 'Burn body fat and achieve a lean physique',
      icon: <Flame className="text-primary" size={24} />,
      color: 'rgba(16, 185, 129, 0.08)'
    },
    {
      id: 'Weight Gain',
      title: 'Weight Gain',
      desc: 'Increase body mass and caloric intake capacity',
      icon: <TrendingUp style={{ color: '#06B6D4' }} size={24} />,
      color: 'rgba(6, 182, 212, 0.08)'
    },
    {
      id: 'Muscle Building',
      title: 'Muscle Building',
      desc: 'Optimize protein synthesis and gain strength',
      icon: <Dumbbell style={{ color: '#F59E0B' }} size={24} />,
      color: 'rgba(245, 158, 11, 0.08)'
    },
    {
      id: 'General Fitness',
      title: 'General Fitness',
      desc: 'Maintain weight, increase energy, and build health',
      icon: <Heart style={{ color: '#EC4899' }} size={24} />,
      color: 'rgba(236, 72, 153, 0.08)'
    }
  ];

  const activityOptions = [
    { id: 'Sedentary', label: 'Sedentary', desc: 'Little to no exercise / Desk job' },
    { id: 'Light', label: 'Lightly Active', desc: 'Light exercise 1-3 days/week' },
    { id: 'Moderate', label: 'Moderately Active', desc: 'Moderate exercise 3-5 days/week' },
    { id: 'Very Active', label: 'Very Active', desc: 'Hard exercise 6-7 days/week' }
  ];

  const dietOptions = ['Everything', 'Vegetarian', 'Vegan', 'Keto', 'Paleo'];

  return (
    <div className="modal-overlay flex-center" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      backgroundColor: 'rgba(11, 15, 23, 0.85)',
      zIndex: 1100,
      backdropFilter: 'blur(12px)',
      padding: '20px'
    }}>
      <div className="glass-card animate-slide-up" style={{
        width: '100%',
        maxWidth: '520px',
        padding: '35px',
        position: 'relative',
        backgroundColor: '#111827',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)',
        borderRadius: '24px'
      }}>
        {/* Close Button */}
        <button 
          onClick={onClose}
          style={{ position: 'absolute', top: '22px', right: '22px', background: 'none', border: 'none', cursor: 'pointer', color: '#9CA3AF' }}
        >
          <X size={20} />
        </button>

        {/* Header */}
        <div style={{ marginBottom: '20px' }}>
          <h2 className="heading-3" style={{ margin: 0, color: 'white', display: 'flex', alignItems: 'center', gap: '8px' }}>
            Fitness Onboarding
          </h2>
          <p style={{ color: '#9CA3AF', fontSize: '0.8rem', margin: '4px 0 0 0' }}>
            Step {step} of 4 • Set biometrics to configure AI health target calculations.
          </p>
        </div>

        {/* Step Indicator */}
        <div style={{ display: 'flex', gap: '6px', marginBottom: '25px' }}>
          {[1, 2, 3, 4].map((i) => (
            <div 
              key={i} 
              style={{ 
                flex: 1, 
                height: '4px', 
                borderRadius: '2px', 
                backgroundColor: step >= i ? 'var(--primary-green)' : 'rgba(255, 255, 255, 0.05)', 
                transition: 'all 0.3s' 
              }} 
            />
          ))}
        </div>

        {/* Form Content */}
        <AnimatePresence mode="wait">
          {step === 1 && (
            <motion.div
              key="step1"
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 10 }}
              transition={{ duration: 0.2 }}
              style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#9CA3AF' }}>Age (years)</label>
                  <input 
                    type="number" 
                    value={age}
                    onChange={(e) => setAge(parseInt(e.target.value) || '')}
                    min="10" 
                    max="100" 
                    style={{ width: '100%', padding: '12px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.08)', outline: 'none', backgroundColor: '#050B18', color: 'white' }} 
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#9CA3AF' }}>Gender</label>
                  <select
                    value={gender}
                    onChange={(e) => setGender(e.target.value)}
                    style={{ width: '100%', padding: '12px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.08)', outline: 'none', backgroundColor: '#050B18', color: 'white' }}
                  >
                    <option value="Female">Female</option>
                    <option value="Male">Male</option>
                  </select>
                </div>
              </div>

              {/* Height Slider */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <label style={{ fontSize: '0.85rem', color: '#9CA3AF', fontWeight: 500 }}>Height (cm)</label>
                  <span style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--primary-green)' }}>{height} cm</span>
                </div>
                <input 
                  type="range" 
                  min="100" 
                  max="250" 
                  value={height}
                  onChange={(e) => setHeight(parseInt(e.target.value))}
                  style={{ width: '100%', height: '6px', borderRadius: '3px', background: 'rgba(255,255,255,0.05)', outline: 'none', cursor: 'pointer', accentColor: 'var(--primary-green)' }}
                />
              </div>

              {/* Weight Slider */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <label style={{ fontSize: '0.85rem', color: '#9CA3AF', fontWeight: 500 }}>Current Weight (kg)</label>
                  <span style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--primary-green)' }}>{weight} kg</span>
                </div>
                <input 
                  type="range" 
                  min="30" 
                  max="200" 
                  value={weight}
                  onChange={(e) => setWeight(parseInt(e.target.value))}
                  style={{ width: '100%', height: '6px', borderRadius: '3px', background: 'rgba(255,255,255,0.05)', outline: 'none', cursor: 'pointer', accentColor: 'var(--primary-green)' }}
                />
              </div>
            </motion.div>
          )}

          {step === 2 && (
            <motion.div
              key="step2"
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              transition={{ duration: 0.2 }}
              style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              <label style={{ display: 'block', fontSize: '0.85rem', color: '#9CA3AF', fontWeight: 500 }}>What is your fitness objective?</label>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                {goalsList.map((g) => {
                  const isSelected = goal === g.id;
                  return (
                    <div
                      key={g.id}
                      onClick={() => setGoal(g.id)}
                      style={{
                        padding: '12px',
                        borderRadius: '16px',
                        background: isSelected ? 'rgba(16, 185, 129, 0.08)' : 'rgba(255, 255, 255, 0.01)',
                        border: isSelected ? `2px solid var(--primary-green)` : '2px solid rgba(255, 255, 255, 0.04)',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '6px',
                        position: 'relative'
                      }}
                    >
                      {isSelected && (
                        <div style={{ position: 'absolute', top: '10px', right: '10px', backgroundColor: 'var(--primary-green)', borderRadius: '50%', padding: '2px' }}>
                          <Check size={8} color="white" strokeWidth={3} />
                        </div>
                      )}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        {g.icon}
                        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'white' }}>{g.title}</span>
                      </div>
                      <p style={{ margin: 0, fontSize: '0.7rem', color: '#9CA3AF', lineHeight: '1.3' }}>{g.desc}</p>
                    </div>
                  );
                })}
              </div>

              {/* Target Weight */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <label style={{ fontSize: '0.85rem', color: '#9CA3AF', fontWeight: 500 }}>Target Weight (kg)</label>
                  <span style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--primary-green)' }}>{targetWeight} kg</span>
                </div>
                <input 
                  type="range" 
                  min="30" 
                  max="200" 
                  value={targetWeight}
                  onChange={(e) => setTargetWeight(parseInt(e.target.value))}
                  style={{ width: '100%', height: '6px', borderRadius: '3px', background: 'rgba(255,255,255,0.05)', outline: 'none', cursor: 'pointer', accentColor: 'var(--primary-green)' }}
                />
              </div>
            </motion.div>
          )}

          {step === 3 && (
            <motion.div
              key="step3"
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              transition={{ duration: 0.2 }}
              style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              <div>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#9CA3AF' }}>Weekly Physical Activity</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {activityOptions.map((opt) => {
                    const isSelected = activityLevel === opt.id;
                    return (
                      <div
                        key={opt.id}
                        onClick={() => setActivityLevel(opt.id)}
                        style={{
                          padding: '8px 12px',
                          borderRadius: '12px',
                          background: isSelected ? 'rgba(16, 185, 129, 0.08)' : 'rgba(255, 255, 255, 0.01)',
                          border: isSelected ? `1px solid var(--primary-green)` : '1px solid rgba(255, 255, 255, 0.04)',
                          cursor: 'pointer',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          transition: 'all 0.2s'
                        }}
                      >
                        <div>
                          <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'white', display: 'block' }}>{opt.label}</span>
                          <span style={{ fontSize: '0.7rem', color: '#9CA3AF' }}>{opt.desc}</span>
                        </div>
                        {isSelected && <Check size={16} className="text-primary" />}
                      </div>
                    );
                  })}
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#9CA3AF' }}>Dietary Option</label>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {dietOptions.map((d) => {
                    const isSelected = dietaryPreference === d;
                    return (
                      <div
                        key={d}
                        onClick={() => setDietaryPreference(d)}
                        style={{
                          padding: '8px 12px',
                          borderRadius: '20px',
                          background: isSelected ? 'var(--primary-green)' : 'rgba(255,255,255,0.02)',
                          border: '1px solid rgba(255,255,255,0.06)',
                          color: isSelected ? 'black' : '#E5E7EB',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                          cursor: 'pointer',
                          transition: 'all 0.2s'
                        }}
                      >
                        {d}
                      </div>
                    );
                  })}
                </div>
              </div>
            </motion.div>
          )}

          {step === 4 && (
            <motion.div
              key="step4"
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              transition={{ duration: 0.2 }}
              style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
              <h3 className="heading-3" style={{ fontSize: '1rem', margin: 0, color: 'white' }}>Onboarding Review</h3>

              <div className="glass-card" style={{ padding: '16px', background: 'rgba(255,255,255,0.01)', border: '1px solid rgba(255,255,255,0.04)', borderRadius: '16px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', fontSize: '0.75rem' }}>
                  <div>
                    <span style={{ color: '#6B7280', display: 'block' }}>GENDER / AGE</span>
                    <strong style={{ color: 'white' }}>{gender}, {age} yrs</strong>
                  </div>
                  <div>
                    <span style={{ color: '#6B7280', display: 'block' }}>HEIGHT / WEIGHT</span>
                    <strong style={{ color: 'white' }}>{height}cm / {weight}kg</strong>
                  </div>
                  <div>
                    <span style={{ color: '#6B7280', display: 'block' }}>FITNESS GOAL</span>
                    <strong style={{ color: 'var(--primary-green)' }}>{goal}</strong>
                  </div>
                  <div>
                    <span style={{ color: '#6B7280', display: 'block' }}>TARGET WEIGHT</span>
                    <strong style={{ color: 'white' }}>{targetWeight} kg</strong>
                  </div>
                  <div>
                    <span style={{ color: '#6B7280', display: 'block' }}>ACTIVITY RATIO</span>
                    <strong style={{ color: 'white' }}>{activityLevel}</strong>
                  </div>
                  <div>
                    <span style={{ color: '#6B7280', display: 'block' }}>DIET CHOICE</span>
                    <strong style={{ color: 'white' }}>{dietaryPreference}</strong>
                  </div>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: '12px', marginTop: '25px' }}>
          {step > 1 && (
            <button 
              type="button" 
              onClick={handleBack}
              className="btn-outline" 
              style={{ flex: 1, padding: '12px', borderColor: 'rgba(255,255,255,0.08)', color: '#9CA3AF' }}
            >
              <ArrowLeft size={18} /> Back
            </button>
          )}
          
          {step < 4 ? (
            <button 
              type="button" 
              onClick={handleNext}
              className="btn-primary" 
              style={{ flex: step > 1 ? 2 : 1, padding: '12px', gap: '8px' }}
            >
              Continue <ArrowRight size={18} />
            </button>
          ) : (
            <button 
              type="button" 
              onClick={handleSubmit}
              disabled={loading}
              className="btn-primary" 
              style={{ flex: 2, padding: '12px', gap: '8px' }}
            >
              {loading ? 'Saving Profile...' : <>Complete & Save <Check size={18} /></>}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default OnboardingModal;
