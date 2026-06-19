import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Activity, User, Mail, Lock, Scale, Dumbbell, 
  TrendingUp, Flame, Heart, Apple, ArrowRight, ArrowLeft, Check, Compass
} from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import PageTransition from '../animations/PageTransition';
import { updateProfile } from 'firebase/auth';
import { db } from '../firebase/config';
import { doc, setDoc } from 'firebase/firestore';
import { calculateNutritionTargets } from '../services/nutritionHelper';
import { getCurrentTimestamp } from '../utils/dateTimeHelper';

const Signup = () => {
  const [step, setStep] = useState(1);
  const { signup } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  // Step 1: Credentials
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Step 2: Biometrics
  const [age, setAge] = useState(28);
  const [gender, setGender] = useState('Female');
  const [height, setHeight] = useState(165);
  const [weight, setWeight] = useState(68);

  // Step 3: Goals
  const [goal, setGoal] = useState('Weight Loss');
  const [targetWeight, setTargetWeight] = useState(62);

  // Step 4: Activity & Diet
  const [activityLevel, setActivityLevel] = useState('Moderate');
  const [dietaryPreference, setDietaryPreference] = useState('Vegetarian');

  // Password strength validation
  const passwordStrength = useMemo(() => {
    const checks = {
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /[0-9]/.test(password),
      special: /[^A-Za-z0-9]/.test(password),
    };
    const passed = Object.values(checks).filter(Boolean).length;
    let label = 'Too weak';
    let color = '#EF4444';
    if (passed >= 5) { label = 'Strong'; color = '#10B981'; }
    else if (passed >= 3) { label = 'Medium'; color = '#F59E0B'; }
    else if (passed >= 1) { label = 'Weak'; color = '#EF4444'; }
    return { checks, passed, label, color, percent: (passed / 5) * 100 };
  }, [password]);

  // Email format validation
  const isEmailValid = useMemo(() => {
    return email.match(/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/);
  }, [email]);

  const handleNext = () => {
    if (step === 1) {
      if (!username.trim()) return toast.error('Please enter a username');
      if (!isEmailValid) return toast.error('Please enter a valid email address');
      if (passwordStrength.passed < 3) return toast.error('Password must meet at least 3 strength requirements');
    }
    if (step === 2) {
      if (!age || age < 10 || age > 100) return toast.error('Please enter a valid age between 10 and 100');
      if (!height || height < 100 || height > 250) return toast.error('Please enter a valid height between 100 and 250 cm');
      if (!weight || weight < 30 || weight > 250) return toast.error('Please enter a valid weight between 30 and 250 kg');
    }
    if (step === 3) {
      if (!targetWeight || targetWeight < 30 || targetWeight > 250) {
        return toast.error('Please enter a valid target weight between 30 and 250 kg');
      }
    }
    setStep(prev => prev + 1);
  };

  const handleBack = () => {
    setStep(prev => prev - 1);
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const { user } = await signup(email, password);
      await updateProfile(user, {
        displayName: username
      });

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

      const newProfile = {
        name: username,
        email: email,
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

      // Firestore document creation
      await setDoc(doc(db, 'profiles', user.uid), newProfile);
      
      // Local cache backup
      localStorage.setItem(`user_profile_${user.uid}`, JSON.stringify(newProfile));

      toast.success('Account and fitness profile initialized successfully!');
      navigate('/dashboard');
    } catch (error) {
      console.error('Signup error:', error);
      toast.error(error.message || 'Could not create account. Please check your details and try again.');
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
    <PageTransition className="flex-center page-container" style={{ minHeight: 'calc(100vh - 120px)', padding: '20px 0' }}>
      <div className="glass-card glow-effect" style={{
        padding: '35px', 
        width: '100%', 
        maxWidth: '540px', 
        borderRadius: '24px', 
        backgroundColor: '#111827',
        border: '1px solid rgba(255, 255, 255, 0.08)'
      }}>
        {/* Header */}
        <div className="flex-center" style={{ marginBottom: '20px', flexDirection: 'column' }}>
          <div style={{ padding: '12px', borderRadius: '16px', background: 'rgba(16, 185, 129, 0.1)', marginBottom: '12px' }}>
            <Activity className="text-primary" size={32} />
          </div>
          <h2 className="heading-2" style={{ margin: 0, fontSize: '1.6rem' }}>Create Premium Account</h2>
          <p style={{ color: '#94A388', fontSize: '0.85rem', margin: '4px 0 0 0' }}>Step {step} of 5</p>
        </div>

        {/* Progress Bar */}
        <div style={{ display: 'flex', gap: '6px', marginBottom: '25px' }}>
          {[1, 2, 3, 4, 5].map((i) => (
            <div 
              key={i} 
              style={{ 
                flex: 1, 
                height: '4px', 
                borderRadius: '2px', 
                backgroundColor: step >= i ? 'var(--primary-green)' : 'rgba(255, 255, 255, 0.05)', 
                transition: 'all 0.3s ease' 
              }} 
            />
          ))}
        </div>

        <form onSubmit={(e) => e.preventDefault()} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
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
                <h3 className="heading-3" style={{ fontSize: '1.1rem', margin: 0, color: 'white' }}>Account Details</h3>
                
                <div>
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Username</label>
                  <div style={{ position: 'relative' }}>
                    <User style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#6B7280' }} size={18} />
                    <input 
                      type="text" 
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="username" 
                      required
                      style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.08)', outline: 'none', backgroundColor: '#050B18', color: 'white' }} 
                    />
                  </div>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Email Address</label>
                  <div style={{ position: 'relative' }}>
                    <Mail style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#6B7280' }} size={18} />
                    <input 
                      type="email" 
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="you@example.com" 
                      required
                      style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.08)', outline: 'none', backgroundColor: '#050B18', color: 'white' }} 
                    />
                  </div>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Password</label>
                  <div style={{ position: 'relative' }}>
                    <Lock style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#6B7280' }} size={18} />
                    <input 
                      type="password" 
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••" 
                      required
                      style={{ width: '100%', padding: '12px 12px 12px 40px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.08)', outline: 'none', backgroundColor: '#050B18', color: 'white' }} 
                    />
                  </div>

                  {/* Realtime Checks */}
                  {password.length > 0 && (
                    <div style={{ marginTop: '10px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                        <span style={{ fontSize: '0.75rem', fontWeight: 600, color: passwordStrength.color }}>Strength: {passwordStrength.label}</span>
                        <span style={{ fontSize: '0.7rem', color: '#9CA3AF' }}>{passwordStrength.passed}/5</span>
                      </div>
                      <div style={{ width: '100%', height: '4px', backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ width: `${passwordStrength.percent}%`, height: '100%', backgroundColor: passwordStrength.color, borderRadius: '4px', transition: 'all 0.3s ease' }} />
                      </div>
                      <div style={{ marginTop: '8px', display: 'flex', flexWrap: 'wrap', gap: '4px 12px' }}>
                        {[
                          { key: 'length', label: 'Min 8 chars' },
                          { key: 'uppercase', label: 'Uppercase' },
                          { key: 'lowercase', label: 'Lowercase' },
                          { key: 'number', label: 'Number' },
                          { key: 'special', label: 'Special symbol' },
                        ].map(({ key, label }) => (
                          <span key={key} style={{ fontSize: '0.7rem', color: passwordStrength.checks[key] ? '#10B981' : '#9CA3AF', display: 'flex', alignItems: 'center', gap: '3px' }}>
                            {passwordStrength.checks[key] ? '✓' : '✗'} {label}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
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
                <h3 className="heading-3" style={{ fontSize: '1.1rem', margin: 0, color: 'white' }}>Biometrics</h3>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Age (years)</label>
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
                    <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Gender</label>
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
                    <label style={{ fontSize: '0.85rem', color: '#94A388', fontWeight: 500 }}>Height (cm)</label>
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
                    <label style={{ fontSize: '0.85rem', color: '#94A388', fontWeight: 500 }}>Current Weight (kg)</label>
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

            {step === 3 && (
              <motion.div
                key="step3"
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                transition={{ duration: 0.2 }}
                style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
              >
                <h3 className="heading-3" style={{ fontSize: '1.1rem', margin: 0, color: 'white' }}>Fitness Goal</h3>
                
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
                    <label style={{ fontSize: '0.85rem', color: '#94A388', fontWeight: 500 }}>Target Weight (kg)</label>
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

            {step === 4 && (
              <motion.div
                key="step4"
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                transition={{ duration: 0.2 }}
                style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
              >
                <h3 className="heading-3" style={{ fontSize: '1.1rem', margin: 0, color: 'white' }}>Activity & Diet</h3>

                <div>
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Weekly Physical Activity</label>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {activityOptions.map((opt) => {
                      const isSelected = activityLevel === opt.id;
                      return (
                        <div
                          key={opt.id}
                          onClick={() => setActivityLevel(opt.id)}
                          style={{
                            padding: '10px 14px',
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
                  <label style={{ display: 'block', marginBottom: '6px', fontSize: '0.85rem', fontWeight: 500, color: '#94A388' }}>Dietary Selection</label>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    {dietOptions.map((d) => {
                      const isSelected = dietaryPreference === d;
                      return (
                        <div
                          key={d}
                          onClick={() => setDietaryPreference(d)}
                          style={{
                            padding: '8px 14px',
                            borderRadius: '20px',
                            background: isSelected ? 'var(--primary-green)' : 'rgba(255,255,255,0.02)',
                            border: '1px solid rgba(255,255,255,0.06)',
                            color: isSelected ? 'black' : '#E5E7EB',
                            fontSize: '0.8rem',
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

            {step === 5 && (
              <motion.div
                key="step5"
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                transition={{ duration: 0.2 }}
                style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}
              >
                <h3 className="heading-3" style={{ fontSize: '1.1rem', margin: 0, color: 'white' }}>Profile Review</h3>

                <div className="glass-card" style={{ padding: '16px', background: 'rgba(255,255,255,0.01)', border: '1px solid rgba(255,255,255,0.04)', borderRadius: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '0.8rem' }}>
                    <div>
                      <span style={{ color: '#6B7280', display: 'block' }}>USERNAME</span>
                      <strong style={{ color: 'white' }}>{username}</strong>
                    </div>
                    <div>
                      <span style={{ color: '#6B7280', display: 'block' }}>EMAIL</span>
                      <strong style={{ color: 'white' }}>{email}</strong>
                    </div>
                    <div>
                      <span style={{ color: '#6B7280', display: 'block' }}>GENDER / AGE</span>
                      <strong style={{ color: 'white' }}>{gender}, {age} yrs</strong>
                    </div>
                    <div>
                      <span style={{ color: '#6B7280', display: 'block' }}>HEIGHT / WEIGHT</span>
                      <strong style={{ color: 'white' }}>{height}cm / {weight}kg</strong>
                    </div>
                    <div>
                      <span style={{ color: '#6B7280', display: 'block' }}>FITNESS OBJECTIVE</span>
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
                      <span style={{ color: '#6B7280', display: 'block' }}>DIET OPTION</span>
                      <strong style={{ color: 'white' }}>{dietaryPreference}</strong>
                    </div>
                  </div>
                </div>

                <p style={{ margin: 0, fontSize: '0.75rem', color: '#6B7280', lineHeight: '1.4' }}>
                  By clicking Create Account, your biological profile BMR, TDEE, macronutrients, and water baseline targets will be computed via the Mifflin-St Jeor equation and saved to Firestore.
                </p>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
            {step > 1 && (
              <button 
                type="button" 
                onClick={handleBack}
                className="btn-outline" 
                style={{ flex: 1, padding: '12px', borderColor: 'rgba(255,255,255,0.08)', color: '#9CA3AF', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              >
                <ArrowLeft size={16} /> Back
              </button>
            )}
            
            {step < 5 ? (
              <button 
                type="button" 
                onClick={handleNext}
                className="btn-primary" 
                style={{ flex: step > 1 ? 2 : 1, padding: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              >
                Continue <ArrowRight size={16} />
              </button>
            ) : (
              <button 
                type="button" 
                onClick={handleSignup}
                disabled={loading}
                className="btn-primary" 
                style={{ flex: 2, padding: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              >
                {loading ? 'Registering...' : <>Complete & Create <Check size={16} /></>}
              </button>
            )}
          </div>
        </form>
        
        {step === 1 && (
          <div style={{ marginTop: '20px', textAlign: 'center', fontSize: '0.85rem', color: '#6B7280' }}>
            Already have an account? <Link to="/login" style={{ color: 'var(--primary-green)', fontWeight: 600 }}>Log In</Link>
          </div>
        )}
      </div>
    </PageTransition>
  );
};

export default Signup;
