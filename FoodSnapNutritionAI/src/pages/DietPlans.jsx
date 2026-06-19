import React, { useState, useEffect, useMemo } from 'react';
import { ChevronLeft, Coffee, Utensils, Moon, Clock, Flame, Sparkles, User, Award, ShieldAlert, Star } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../context/AuthContext';
import { calculateNutritionTargets } from '../services/nutritionHelper';

const DietPlans = () => {
  const { currentUser } = useAuth();
  const [selectedPlan, setSelectedPlan] = useState(null);
  const [profile, setProfile] = useState(null);

  // Load profile from localStorage
  useEffect(() => {
    if (currentUser) {
      const savedProfile = localStorage.getItem(`user_profile_${currentUser.uid}`);
      if (savedProfile) {
        setProfile(JSON.parse(savedProfile));
      }
    }
  }, [currentUser]);

  // Compute targets
  const targets = useMemo(() => {
    return calculateNutritionTargets(profile);
  }, [profile]);

  const plans = [
    { 
      id: 1,
      title: "Weight Loss", 
      desc: "Low-calorie, high-protein meals to help you shed pounds.", 
      color: "#10B981",
      calories: "1,500 kcal",
      goals: ["weight loss"],
      meals: [
        { time: "08:00 AM", type: "Breakfast", food: "3 Egg White Omelet with Spinach & Feta", cals: "220" },
        { time: "01:00 PM", type: "Lunch", food: "Grilled Chicken Breast with Steamed Broccoli", cals: "450" },
        { time: "04:30 PM", type: "Snack", food: "Fat-free Greek Yogurt with 5 Almonds", cals: "180" },
        { time: "08:00 PM", type: "Dinner", food: "Baked Cod with Asparagus and Lemon", cals: "350" }
      ]
    },
    { 
      id: 2,
      title: "Muscle Gain", 
      desc: "High-protein, calorie-surplus plans for building strength.", 
      color: "#3B82F6",
      calories: "3,200 kcal",
      goals: ["weight gain", "muscle building"],
      meals: [
        { time: "08:00 AM", type: "Breakfast", food: "4 Whole Eggs + 1 Cup Oats with Banana", cals: "750" },
        { time: "01:00 PM", type: "Lunch", food: "Lean Beef Mince with Brown Rice & Avocado", cals: "850" },
        { time: "05:00 PM", type: "Post Workout", food: "Whey Protein + Peanut Butter Sandwich", cals: "600" },
        { time: "09:00 PM", type: "Dinner", food: "Grilled Salmon with Sweet Potato & Kale", cals: "700" }
      ]
    },
    { 
      id: 3,
      title: "Diabetic Friendly", 
      desc: "Low glycemic index foods to maintain stable blood sugar.", 
      color: "#F59E0B",
      calories: "1,800 kcal",
      goals: [],
      meals: [
        { time: "08:30 AM", type: "Breakfast", food: "Chia Seed Pudding with Berries", cals: "280" },
        { time: "01:30 PM", type: "Lunch", food: "Quinoa and Chickpea Mediterranean Salad", cals: "420" },
        { time: "05:00 PM", type: "Snack", food: "Hummus with Cucumber and Celery Sticks", cals: "150" },
        { time: "08:00 PM", type: "Dinner", food: "Grilled Turkey with Sautéed Zucchini", cals: "480" }
      ]
    },
    { 
      id: 4,
      title: "Vegetarian", 
      desc: "Plant-based plans rich in essential nutrients.", 
      color: "#8B5CF6",
      calories: "2,000 kcal",
      goals: [],
      meals: [
        { time: "08:00 AM", type: "Breakfast", food: "Tofu Scramble with Whole Grain Toast", cals: "350" },
        { time: "01:00 PM", type: "Lunch", food: "Black Bean Burger with Side Salad", cals: "550" },
        { time: "04:30 PM", type: "Snack", food: "Roasted Chickpeas and an Apple", cals: "220" },
        { time: "08:30 PM", type: "Dinner", food: "Lentil Pasta with Marinara & Mushrooms", cals: "600" }
      ]
    }
  ];

  return (
    <div className="page-container animate-fade-in" style={{ paddingBottom: '60px' }}>
      <AnimatePresence mode="wait">
        {!selectedPlan ? (
          <motion.div
            key="list"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
          >
            <h1 className="heading-1 text-center" style={{textAlign: 'center', marginBottom: '10px'}}>Personalized Diet Plans</h1>
            <p className="text-center mb-10" style={{textAlign: 'center', color: '#6B7280', marginBottom: '30px'}}>Expertly curated nutrition schedules for your specific goals.</p>
            
            {/* Personalized Caloric Blueprint Block */}
            {profile ? (
              <motion.div
                initial={{ opacity: 0, scale: 0.98 }}
                animate={{ opacity: 1, scale: 1 }}
                className="glass-card glow-effect"
                style={{
                  padding: '30px',
                  marginBottom: '40px',
                  borderLeft: '5px solid var(--primary-green)',
                  background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.05), rgba(59, 130, 246, 0.02))'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '20px', alignItems: 'center', marginBottom: '20px' }}>
                  <div>
                    <h3 className="heading-3" style={{ margin: 0, color: 'var(--dark-text)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Award className="text-primary" size={24} /> Your Personalized Caloric Blueprint
                    </h3>
                    <p style={{ margin: '4px 0 0 0', color: '#9CA3AF', fontSize: '0.85rem' }}>
                      Based on biometrics: {profile.height} cm | {profile.weight} kg | Goal: {profile.goal}
                    </p>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                    <span style={{ fontSize: '0.75rem', color: '#6B7280', fontWeight: 600 }}>TDEE TARGET</span>
                    <span style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary-green)' }}>{targets.calories} <span style={{ fontSize: '1rem', fontWeight: 500 }}>kcal</span></span>
                  </div>
                </div>

                {/* Macro Split row */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(100px, 1fr))', gap: '15px' }}>
                  <div style={{ padding: '15px', background: 'rgba(255,255,255,0.02)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.75rem', color: '#6B7280', display: 'block', fontWeight: 600, marginBottom: '4px' }}>PROTEIN</span>
                    <strong style={{ fontSize: '1.2rem', color: 'var(--primary-green)' }}>{targets.protein}g</strong>
                    <span style={{ fontSize: '0.7rem', color: '#6B7280', display: 'block', marginTop: '2px' }}>{targets.protein * 4} kcal</span>
                  </div>
                  <div style={{ padding: '15px', background: 'rgba(255,255,255,0.02)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.75rem', color: '#6B7280', display: 'block', fontWeight: 600, marginBottom: '4px' }}>CARBS</span>
                    <strong style={{ fontSize: '1.2rem', color: 'var(--secondary-blue)' }}>{targets.carbs}g</strong>
                    <span style={{ fontSize: '0.7rem', color: '#6B7280', display: 'block', marginTop: '2px' }}>{targets.carbs * 4} kcal</span>
                  </div>
                  <div style={{ padding: '15px', background: 'rgba(255,255,255,0.02)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.75rem', color: '#6B7280', display: 'block', fontWeight: 600, marginBottom: '4px' }}>FATS</span>
                    <strong style={{ fontSize: '1.2rem', color: '#F59E0B' }}>{targets.fats}g</strong>
                    <span style={{ fontSize: '0.7rem', color: '#6B7280', display: 'block', marginTop: '2px' }}>{targets.fats * 9} kcal</span>
                  </div>
                </div>
              </motion.div>
            ) : (
              <div className="glass-card" style={{ padding: '20px', marginBottom: '40px', borderLeft: '4px solid #F59E0B', background: 'rgba(245, 158, 11, 0.03)', display: 'flex', alignItems: 'center', gap: '15px' }}>
                <ShieldAlert className="text-secondary" style={{ color: '#F59E0B' }} size={24} />
                <div>
                  <h4 style={{ margin: 0, color: 'var(--dark-text)', fontSize: '0.95rem' }}>Personalized blueprint disabled</h4>
                  <p style={{ margin: '2px 0 0 0', color: '#9CA3AF', fontSize: '0.8rem' }}>Set your biological metrics in the Dashboard to generate custom calorie and macro targets.</p>
                </div>
              </div>
            )}
            
            <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '30px'}}>
              {plans.map((plan) => {
                const isRecommended = profile && plan.goals.includes(profile.goal.toLowerCase());
                
                return (
                  <div 
                    key={plan.id} 
                    className="glass-card glow-effect" 
                    style={{
                      padding: '30px 30px 42px 30px', 
                      borderTop: `6px solid ${plan.color}`, 
                      position: 'relative', 
                      overflow: 'hidden',
                      boxShadow: isRecommended ? `0 0 20px rgba(16, 185, 129, 0.15)` : 'var(--shadow-md)',
                      borderColor: isRecommended ? 'var(--primary-green)' : 'var(--glass-border)'
                    }}
                  >
                    <h3 className="heading-3" style={{ marginTop: '0' }}>{plan.title}</h3>
                    <p className="text-secondary" style={{marginBottom: '20px', minHeight: '60px', fontSize: '0.9rem'}}>{plan.desc}</p>
                    <div className="flex-center" style={{justifyContent: 'flex-start', gap: '10px', marginBottom: '25px', fontSize: '0.9rem', color: plan.color, fontWeight: 600}}>
                      <Flame size={18} /> {plan.calories} target
                    </div>
                    <button 
                      onClick={() => setSelectedPlan(plan)}
                      className="btn-outline" 
                      style={{width: '100%', borderColor: plan.color, color: plan.color}}
                    >
                      View Full Plan
                    </button>
                    {isRecommended && (
                      <div style={{
                        position: 'absolute',
                        bottom: '8px',
                        left: '0',
                        right: '0',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '4px',
                        fontSize: '0.65rem',
                        fontWeight: 700,
                        color: 'var(--primary-green)',
                        textTransform: 'uppercase',
                        letterSpacing: '0.5px'
                      }}>
                        <Star size={10} fill="var(--primary-green)" stroke="none" /> Recommend
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </motion.div>
        ) : (
          <motion.div
            key="detail"
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -50 }}
            className="glass-card"
            style={{ padding: '40px' }}
          >
            <button 
              onClick={() => setSelectedPlan(null)}
              className="flex-center mb-8" 
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--primary-green)', fontWeight: 600, gap: '8px' }}
            >
              <ChevronLeft size={20} /> Back to Plans
            </button>

            <div className="flex-center" style={{ justifyContent: 'space-between', marginBottom: '40px', flexWrap: 'wrap', gap: '20px' }}>
              <div>
                <h1 className="heading-1" style={{ margin: 0 }}>{selectedPlan.title} <span style={{ color: selectedPlan.color }}>Schedule</span></h1>
                <p style={{ color: '#6B7280', marginTop: '8px' }}>Daily meal plan optimized for maximum efficiency.</p>
              </div>
              <div className="glass-card" style={{ padding: '15px 25px', backgroundColor: 'rgba(255,255,255,0.05)', border: `1px solid ${selectedPlan.color}` }}>
                <span style={{ fontSize: '0.8rem', color: '#6B7280', textTransform: 'uppercase' }}>Daily Target</span>
                <h3 className="heading-3" style={{ margin: 0, color: selectedPlan.color }}>{selectedPlan.calories}</h3>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              {selectedPlan.meals.map((meal, idx) => (
                <div key={idx} className="flex-center" style={{ 
                  justifyContent: 'flex-start', 
                  gap: '30px', 
                  padding: '25px', 
                  backgroundColor: 'rgba(255,255,255,0.02)', 
                  borderRadius: '16px',
                  border: '1px solid rgba(255,255,255,0.05)'
                }}>
                  <div className="flex-center" style={{ 
                    flexDirection: 'column', 
                    minWidth: '100px', 
                    padding: '10px', 
                    borderRadius: '12px', 
                    backgroundColor: 'rgba(255,255,255,0.03)',
                    borderLeft: `4px solid ${selectedPlan.color}`
                  }}>
                    <Clock size={16} className="mb-1" style={{ color: '#6B7280' }} />
                    <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{meal.time}</span>
                  </div>

                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                      {meal.type === "Breakfast" && <Coffee size={18} className="text-primary" />}
                      {meal.type === "Dinner" && <Moon size={18} className="text-secondary" />}
                      {(meal.type === "Lunch" || meal.type === "Snack" || meal.type === "Post Workout") && <Utensils size={18} style={{ color: '#F59E0B' }} />}
                      <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: selectedPlan.color, fontWeight: 700, letterSpacing: '1px' }}>{meal.type}</span>
                    </div>
                    <h4 className="heading-4" style={{ margin: 0 }}>{meal.food}</h4>
                  </div>

                  <div style={{ textAlign: 'right' }}>
                    <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>{meal.cals}</span>
                    <span style={{ fontSize: '0.8rem', color: '#6B7280', display: 'block' }}>kcal</span>
                  </div>
                </div>
              ))}
            </div>
            
            <div className="mt-10 p-6 glass-card" style={{ background: 'rgba(16, 185, 129, 0.05)', border: '1px dashed var(--primary-green)', marginTop: '40px' }}>
               <h4 className="heading-4 flex-center" style={{ justifyContent: 'flex-start', gap: '10px', margin: '0 0 10px 0' }}>
                 <Sparkles size={18} className="text-primary" /> Pro Tip
               </h4>
               <p style={{ fontSize: '0.9rem', color: '#9CA3AF', margin: 0 }}>
                 For {selectedPlan.title}, ensure you stay hydrated with at least 3 liters of water per day. Combining this plan with {selectedPlan.title === "Muscle Gain" ? "heavy resistance training" : "light cardio"} will accelerate your results by 40%.
               </p>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default DietPlans;
