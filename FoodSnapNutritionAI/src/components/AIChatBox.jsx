import { useState, useEffect, useMemo, useRef } from 'react';
import { Send, Bot, User, Target, Heart, Droplet, Flame, Sparkles, ChevronDown, ChevronUp } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { chatWithAI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase/config';
import { collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore';
import { getTodayDateString } from '../utils/dateTimeHelper';

const AIChatBox = ({ noCard = false }) => {
  const { currentUser } = useAuth();
  
  const [messages, setMessages] = useState([
    { text: "Hello! I'm your FoodSnap AI Assistant. How can I help you with your nutrition goals today?", sender: "bot" }
  ]);
  const [input, setInput] = useState("");
  const [isThinking, setIsThinking] = useState(false);
  
  // Real-time Context Stats States
  const [profile, setProfile] = useState(null);
  const [water, setWater] = useState(0);
  const [foodCals, setFoodCals] = useState(0);
  const [foodProtein, setFoodProtein] = useState(0);
  const [foodCarbs, setFoodCarbs] = useState(0);
  const [foodFats, setFoodFats] = useState(0);
  const [exerciseMins, setExerciseMins] = useState(0);
  const [exerciseCals, setExerciseCals] = useState(0);
  const [showContextPanel, setShowContextPanel] = useState(true);
  const [loadingContext, setLoadingContext] = useState(true);

  const todayStr = getTodayDateString();
  const conversationEndRef = useRef(null);

  // Auto-scroll to bottom of conversation
  useEffect(() => {
    conversationEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isThinking]);

  // Load Real-time Context stats
  useEffect(() => {
    const loadContextData = async () => {
      if (!currentUser) return;
      setLoadingContext(true);
      
      try {
        // 1. Profile from Firestore
        const docRef = doc(db, 'profiles', currentUser.uid);
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          setProfile(docSnap.data());
        }

        // 2. Fetch today's water logs from Firestore
        const waterQ = query(
          collection(db, 'waterLogs'),
          where('userId', '==', currentUser.uid)
        );
        const waterSnap = await getDocs(waterQ);
        let waterSum = 0;
        waterSnap.docs.forEach(doc => {
          const d = doc.data();
          if (d.timestamp?.substring(0, 10) === todayStr) {
            waterSum += parseInt(d.amount || 0);
          }
        });
        setWater(waterSum);

        // 3. Fetch today's food logs from Firestore
        const foodQ = query(
          collection(db, 'foodLogs'),
          where('userId', '==', currentUser.uid)
        );
        const foodSnap = await getDocs(foodQ);
        let foodCaloriesSum = 0;
        let proteinSum = 0;
        let carbsSum = 0;
        let fatsSum = 0;
        foodSnap.docs.forEach(doc => {
          const d = doc.data();
          if (d.timestamp?.substring(0, 10) === todayStr) {
            foodCaloriesSum += parseInt(d.calories || 0);
            proteinSum += d.protein !== undefined ? (typeof d.protein === 'number' ? d.protein : parseInt(String(d.protein).replace('g', '')) || 0) : 0;
            carbsSum += d.carbs !== undefined ? (typeof d.carbs === 'number' ? d.carbs : parseInt(String(d.carbs).replace('g', '')) || 0) : 0;
            fatsSum += d.fats !== undefined ? (typeof d.fats === 'number' ? d.fats : parseInt(String(d.fats).replace('g', '')) || 0) : 0;
          }
        });
        setFoodCals(foodCaloriesSum);
        setFoodProtein(proteinSum);
        setFoodCarbs(carbsSum);
        setFoodFats(fatsSum);

        // 4. Fetch today's exercise logs from Firestore
        const exQ = query(
          collection(db, 'exerciseLogs'),
          where('userId', '==', currentUser.uid)
        );
        const exSnap = await getDocs(exQ);
        let exMinsSum = 0;
        let exCalsSum = 0;
        exSnap.docs.forEach(doc => {
          const d = doc.data();
          if (d.timestamp?.substring(0, 10) === todayStr) {
            exMinsSum += parseInt(d.durationMinutes || d.duration || 0);
            exCalsSum += parseInt(d.caloriesBurned || 0);
          }
        });
        setExerciseMins(exMinsSum);
        setExerciseCals(exCalsSum);

      } catch (e) {
        console.warn("Failed to fetch Firestore logs for chatbot context:", e);
      } finally {
        setLoadingContext(false);
      }
    };

    loadContextData();
  }, [currentUser]);

  // Dynamically generate suggested prompts based on actual stats
  const dynamicSuggestions = useMemo(() => {
    const suggestions = [];
    
    // Suggestion based on fitness goal
    if (profile?.goal) {
      suggestions.push(`Diet tips for my ${profile.goal} goal?`);
    } else {
      suggestions.push("How to set a proper fitness goal?");
    }

    // Suggestion based on water consumption
    if (water < 2000) {
      suggestions.push("How can I hit my hydration goals?");
    } else {
      suggestions.push("Benefits of high water consumption?");
    }

    // Suggestion based on workout status
    if (exerciseMins === 0) {
      suggestions.push("Suggest a quick active workout.");
    } else {
      suggestions.push("Post-workout nutrition recovery?");
    }

    return suggestions;
  }, [profile, water, exerciseMins]);

  const handleSend = async (e, customText = null) => {
    if (e) e.preventDefault();
    const queryText = customText || input;
    if (!queryText.trim()) return;

    // Display user message in UI
    setMessages((prev) => [...prev, { text: queryText, sender: "user" }]);
    if (!customText) setInput("");
    setIsThinking(true);

    // Build the context package
    const healthContext = {
      goal: profile?.goal || 'General Fitness',
      age: profile?.age || 28,
      gender: profile?.gender || 'Female',
      height: profile?.height || 170,
      weight: profile?.weight || 70,
      bmi: profile?.bmi || 24.9,
      bmr: profile?.bmr || 1500,
      tdee: profile?.tdee || 2000,
      activityLevel: profile?.activityLevel || 'Moderate',
      dietaryPreference: profile?.dietaryPreference || 'Vegetarian',
      foodCals: foodCals,
      targetCalories: profile?.targetCalories || 2000,
      currentProtein: foodProtein,
      targetProtein: profile?.targetProtein || 130,
      currentCarbs: foodCarbs,
      targetCarbs: profile?.targetCarbs || 220,
      currentFats: foodFats,
      targetFats: profile?.targetFats || 65,
      water: water,
      targetWater: profile?.targetWater || 2500,
      exerciseMins: exerciseMins,
      exerciseCals: exerciseCals
    };

    try {
      // Send message with context
      const responseData = await chatWithAI(queryText, healthContext);
      setIsThinking(false);
      setMessages((prev) => [
        ...prev,
        {
          text: responseData.response,
          sender: "bot",
        },
      ]);
      toast.success('AI insight generated');
    } catch (error) {
      setIsThinking(false);
      toast.error('AI Service is unavailable. Using offline mode.');
      setMessages((prev) => [
        ...prev,
        {
          text: "I'm having trouble connecting to my neural core right now, but I'm still here to help! Please try again in a moment.",
          sender: "bot",
        },
      ]);
    }
  };

  return (
    <div
      className={noCard ? "" : "glass-card"}
      style={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        background: noCard ? 'transparent' : undefined,
        border: noCard ? 'none' : undefined,
        boxShadow: noCard ? 'none' : undefined,
        borderRadius: noCard ? '0' : undefined,
        maxHeight: noCard ? '100%' : 'calc(100vh - 240px)',
      }}
    >
      {/* Real-time AI Context Matrix Panel */}
      <div 
        style={{
          borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
          backgroundColor: 'rgba(16, 185, 129, 0.02)',
        }}
      >
        <button
          onClick={() => setShowContextPanel(!showContextPanel)}
          style={{
            width: '100%',
            padding: '12px 24px',
            background: 'none',
            border: 'none',
            color: 'white',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: '0.85rem',
            fontWeight: 600,
            letterSpacing: '0.5px',
          }}
        >
          <div className="flex-center" style={{ gap: '8px', justifyContent: 'flex-start' }}>
            <Sparkles size={16} className="text-primary" />
            <span style={{ color: '#E5E7EB' }}>REAL-TIME AI CONTEXT MATRIX</span>
            <span style={{ 
              fontSize: '0.7rem', 
              color: '#10b981', 
              background: 'rgba(16, 185, 129, 0.1)', 
              padding: '2px 8px', 
              borderRadius: '8px', 
              fontWeight: 500 
            }}>
              Connected
            </span>
          </div>
          {showContextPanel ? <ChevronUp size={16} style={{ color: '#9CA3AF' }} /> : <ChevronDown size={16} style={{ color: '#9CA3AF' }} />}
        </button>

        <AnimatePresence>
          {showContextPanel && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.2 }}
              style={{ overflow: 'hidden' }}
            >
              <div 
                style={{ 
                  padding: '0 24px 20px 24px', 
                  display: 'grid', 
                  gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', 
                  gap: '12px' 
                }}
              >
                <div style={{ padding: '10px 14px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#9CA3AF', fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase', marginBottom: '4px' }}>
                    <Target size={14} className="text-primary" /> Goal
                  </div>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'white' }}>{loadingContext ? "..." : (profile?.goal || 'General Fitness')}</span>
                </div>
                
                <div style={{ padding: '10px 14px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#9CA3AF', fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase', marginBottom: '4px' }}>
                    <Heart size={14} style={{ color: '#ec4899' }} /> Body Stats
                  </div>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'white' }}>{loadingContext ? "..." : (profile ? `${profile.height}cm / ${profile.weight}kg` : 'No Profile')}</span>
                </div>

                <div style={{ padding: '10px 14px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#9CA3AF', fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase', marginBottom: '4px' }}>
                    <Droplet size={14} className="text-secondary" /> Hydration
                  </div>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'white' }}>{loadingContext ? "..." : `${(water / 1000).toFixed(2)} / 2.8 L`}</span>
                </div>

                <div style={{ padding: '10px 14px', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#9CA3AF', fontSize: '0.7rem', fontWeight: 600, textTransform: 'uppercase', marginBottom: '4px' }}>
                    <Flame size={14} style={{ color: '#F59E0B' }} /> Calories & Activity
                  </div>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'white' }}>{loadingContext ? "..." : `${foodCals} kcal • ${exerciseMins}m`}</span>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Conversation stream */}
      <div style={{ flex: 1, padding: '24px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <AnimatePresence>
          {messages.map((msg, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 10, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ duration: 0.25 }}
              style={{
                display: 'flex',
                gap: '12px',
                alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                maxWidth: '85%',
              }}
            >
              {msg.sender === 'bot' && (
                <div
                  style={{
                    width: '36px',
                    height: '36px',
                    borderRadius: '12px',
                    backgroundColor: 'var(--primary-green)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    flexShrink: 0,
                    boxShadow: '0 4px 10px rgba(16, 185, 129, 0.3)',
                  }}
                >
                  <Bot size={20} />
                </div>
              )}
              <div
                style={{
                  padding: '12px 18px',
                  borderRadius: '18px',
                  backgroundColor: msg.sender === 'user' ? 'var(--secondary-blue)' : 'rgba(255, 255, 255, 0.03)',
                  color: 'white',
                  border: msg.sender === 'bot' ? '1px solid rgba(255, 255, 255, 0.05)' : 'none',
                  borderBottomRightRadius: msg.sender === 'user' ? '4px' : '18px',
                  borderBottomLeftRadius: msg.sender === 'bot' ? '4px' : '18px',
                  fontSize: '0.95rem',
                  lineHeight: 1.5,
                  boxShadow: 'var(--shadow-sm)',
                  whiteSpace: 'pre-wrap',
                }}
              >
                {msg.text}
              </div>
              {msg.sender === 'user' && (
                <div
                  style={{
                    width: '36px',
                    height: '36px',
                    borderRadius: '12px',
                    backgroundColor: 'var(--secondary-blue)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    flexShrink: 0,
                    boxShadow: '0 4px 10px rgba(59, 130, 246, 0.3)',
                  }}
                >
                  <User size={20} />
                </div>
              )}
            </motion.div>
          ))}

          {isThinking && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              style={{ display: 'flex', gap: '12px', alignSelf: 'flex-start' }}
            >
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '12px',
                  backgroundColor: 'var(--primary-green)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                }}
              >
                <Bot size={20} />
              </div>
              <div
                style={{
                  padding: '12px 18px',
                  borderRadius: '18px',
                  backgroundColor: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid rgba(255, 255, 255, 0.05)',
                  color: 'white',
                  borderBottomLeftRadius: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                }}
              >
                <motion.span animate={{ opacity: [0.3, 1, 0.3] }} transition={{ repeat: Infinity, duration: 1 }} style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--primary-green)' }} />
                <motion.span animate={{ opacity: [0.3, 1, 0.3] }} transition={{ repeat: Infinity, duration: 1, delay: 0.2 }} style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--primary-green)' }} />
                <motion.span animate={{ opacity: [0.3, 1, 0.3] }} transition={{ repeat: Infinity, duration: 1, delay: 0.4 }} style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--primary-green)' }} />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
        <div ref={conversationEndRef} />
      </div>

      {/* Suggested Input Quick Prompts */}
      <div style={{ padding: '16px 24px', borderTop: '1px solid rgba(255, 255, 255, 0.08)', backgroundColor: '#101724' }}>
        <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '12px' }}>
          {dynamicSuggestions.map((promptText, idx) => (
            <motion.button
              key={idx}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="btn-outline"
              style={{ 
                padding: '6px 14px', 
                fontSize: '0.8rem', 
                borderRadius: '20px', 
                whiteSpace: 'nowrap',
                backgroundColor: 'rgba(255,255,255,0.02)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                color: '#D1D5DB',
                cursor: 'pointer'
              }}
              onClick={() => handleSend(null, promptText)}
            >
              {promptText}
            </motion.button>
          ))}
        </div>

        {/* Input prompt line */}
        <form onSubmit={(e) => handleSend(e)} style={{ display: 'flex', gap: '12px' }}>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask your AI nutritionist..."
            style={{
              flex: 1,
              padding: '14px 20px',
              borderRadius: '24px',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              outline: 'none',
              backgroundColor: '#0B0F17',
              color: 'white',
              fontSize: '0.95rem',
            }}
          />
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            type="submit"
            className="btn-primary"
            style={{
              borderRadius: '50%',
              width: '50px',
              height: '50px',
              padding: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
              cursor: 'pointer'
            }}
            aria-label="Send Message"
          >
            <Send size={20} />
          </motion.button>
        </form>
      </div>
    </div>
  );
};

export default AIChatBox;
