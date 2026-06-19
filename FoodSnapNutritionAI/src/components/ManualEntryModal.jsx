import React, { useState, useMemo } from 'react';
import { X, Save, Sparkles, Flame, Apple, Heart } from 'lucide-react';
import { db } from '../firebase/config';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

const ManualEntryModal = ({ isOpen, onClose, onRefresh }) => {
  const { currentUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    foodName: '',
    calories: '',
    protein: '',
    carbs: '',
    fats: ''
  });

  // Common preset templates
  const presets = [
    { name: 'Protein Shake', calories: 200, protein: 25, carbs: 5, fats: 2 },
    { name: 'Chicken & Rice', calories: 550, protein: 40, carbs: 65, fats: 10 },
    { name: 'Scrambled Eggs', calories: 280, protein: 18, carbs: 2, fats: 22 },
    { name: 'Oatmeal Bowl', calories: 310, protein: 10, carbs: 55, fats: 5 }
  ];

  // Calculate live macro percentages for visualizer
  const macroSummary = useMemo(() => {
    const p = parseFloat(formData.protein) || 0;
    const c = parseFloat(formData.carbs) || 0;
    const f = parseFloat(formData.fats) || 0;
    const total = p + c + f;
    
    if (total === 0) return { proteinPct: 0, carbsPct: 0, fatsPct: 0, totalGrams: 0 };
    return {
      proteinPct: Math.round((p / total) * 100),
      carbsPct: Math.round((c / total) * 100),
      fatsPct: Math.round((f / total) * 100),
      totalGrams: total
    };
  }, [formData.protein, formData.carbs, formData.fats]);

  if (!isOpen) return null;

  const applyPreset = (preset) => {
    setFormData({
      foodName: preset.name,
      calories: preset.calories.toString(),
      protein: preset.protein.toString(),
      carbs: preset.carbs.toString(),
      fats: preset.fats.toString()
    });
    toast.success(`${preset.name} template applied`);
  };

  const adjustValue = (field, amount) => {
    const currentVal = parseFloat(formData[field]) || 0;
    const newVal = Math.max(0, currentVal + amount);
    setFormData(prev => ({
      ...prev,
      [field]: newVal.toString()
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.foodName || !formData.calories) {
      toast.error('Please enter food name and calories');
      return;
    }

    setLoading(true);
    try {
      await addDoc(collection(db, 'foodLogs'), {
        userId: currentUser.uid,
        foodName: formData.foodName,
        calories: parseInt(formData.calories) || 0,
        protein: `${formData.protein || 0}g`,
        carbs: `${formData.carbs || 0}g`,
        fats: `${formData.fats || 0}g`,
        healthScore: 'N/A',
        timestamp: serverTimestamp(),
        imageUrl: ""
      });

      toast.success('Meal logged successfully!');
      onRefresh();
      onClose();
      setFormData({ foodName: '', calories: '', protein: '', carbs: '', fats: '' });
    } catch (error) {
      console.error("Error adding document: ", error);
      toast.error('Failed to log meal');
    } finally {
      setLoading(false);
    }
  };

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
      padding: '20px',
      boxSizing: 'border-box'
    }}>
      <div className="glass-card animate-slide-up" style={{
        width: '100%',
        maxWidth: '520px',
        padding: '24px',
        position: 'relative',
        backgroundColor: '#141b26',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.6)',
        borderRadius: '24px',
        boxSizing: 'border-box'
      }}>
        {/* Close button */}
        <button 
          onClick={onClose}
          style={{ position: 'absolute', top: '22px', right: '22px', background: 'none', border: 'none', cursor: 'pointer', color: '#9CA3AF' }}
        >
          <X size={20} />
        </button>

        <h2 className="heading-3 mb-4" style={{ color: 'white', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Sparkles className="text-primary" size={22} /> Add Meal Log
        </h2>
        <p style={{ color: '#9CA3AF', fontSize: '0.85rem', marginBottom: '24px' }}>
          Add custom nutrition metrics or select from templates.
        </p>

        {/* Quick Presets Section */}
        <div style={{ marginBottom: '20px' }}>
          <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#6B7280', display: 'block', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '8px' }}>Quick Presets</span>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            {presets.map((p) => (
              <button
                key={p.name}
                type="button"
                onClick={() => applyPreset(p)}
                style={{
                  padding: '6px 12px',
                  borderRadius: '10px',
                  border: '1px solid rgba(255,255,255,0.06)',
                  background: 'rgba(255,255,255,0.02)',
                  color: 'white',
                  fontSize: '0.75rem',
                  fontWeight: 500,
                  transition: 'all 0.2s ease',
                  cursor: 'pointer'
                }}
                onMouseOver={(e) => e.target.style.background = 'rgba(255,255,255,0.05)'}
                onMouseOut={(e) => e.target.style.background = 'rgba(255,255,255,0.02)'}
              >
                {p.name}
              </button>
            ))}
          </div>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {/* Food Name input */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label className="label" style={{ color: '#E5E7EB', margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>Food Name</label>
            <input 
              type="text" 
              className="input-field" 
              placeholder="e.g. Chicken Caesar Salad"
              style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '12px', borderRadius: '12px', boxSizing: 'border-box' }}
              value={formData.foodName}
              onChange={(e) => setFormData({...formData, foodName: e.target.value})}
            />
          </div>

          {/* Calories input with quick adds */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label className="label" style={{ color: '#E5E7EB', margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>Calories (kcal)</label>
              <div style={{ display: 'flex', gap: '4px' }}>
                <button type="button" onClick={() => adjustValue('calories', 100)} style={{ fontSize: '0.7rem', color: 'var(--primary-green)', padding: '2px 6px', background: 'rgba(16, 185, 129, 0.08)', borderRadius: '6px', fontWeight: 600 }}>+100</button>
                <button type="button" onClick={() => adjustValue('calories', 250)} style={{ fontSize: '0.7rem', color: 'var(--primary-green)', padding: '2px 6px', background: 'rgba(16, 185, 129, 0.08)', borderRadius: '6px', fontWeight: 600 }}>+250</button>
              </div>
            </div>
            <input 
              type="number" 
              className="input-field" 
              placeholder="e.g. 450"
              style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '12px', borderRadius: '12px', boxSizing: 'border-box' }}
              value={formData.calories}
              onChange={(e) => setFormData({...formData, calories: e.target.value})}
            />
          </div>

          {/* Macros input row with adjust controls */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            
            {/* Protein */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: 'var(--primary-green)', fontSize: '0.75rem', fontWeight: 600 }}>Protein</span>
                <button type="button" onClick={() => adjustValue('protein', 5)} style={{ fontSize: '0.65rem', color: 'var(--primary-green)', background: 'none' }}>+5g</button>
              </div>
              <input 
                type="number" 
                className="input-field" 
                placeholder="0"
                style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '10px', borderRadius: '10px', textAlign: 'center', boxSizing: 'border-box' }}
                value={formData.protein}
                onChange={(e) => setFormData({...formData, protein: e.target.value})}
              />
            </div>

            {/* Carbs */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: 'var(--secondary-blue)', fontSize: '0.75rem', fontWeight: 600 }}>Carbs</span>
                <button type="button" onClick={() => adjustValue('carbs', 10)} style={{ fontSize: '0.65rem', color: 'var(--secondary-blue)', background: 'none' }}>+10g</button>
              </div>
              <input 
                type="number" 
                className="input-field" 
                placeholder="0"
                style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '10px', borderRadius: '10px', textAlign: 'center', boxSizing: 'border-box' }}
                value={formData.carbs}
                onChange={(e) => setFormData({...formData, carbs: e.target.value})}
              />
            </div>

            {/* Fats */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: '#F59E0B', fontSize: '0.75rem', fontWeight: 600 }}>Fats</span>
                <button type="button" onClick={() => adjustValue('fats', 5)} style={{ fontSize: '0.65rem', color: '#F59E0B', background: 'none' }}>+5g</button>
              </div>
              <input 
                type="number" 
                className="input-field" 
                placeholder="0"
                style={{ backgroundColor: '#0B0F17', color: 'white', borderColor: 'rgba(255,255,255,0.08)', width: '100%', padding: '10px', borderRadius: '10px', textAlign: 'center', boxSizing: 'border-box' }}
                value={formData.fats}
                onChange={(e) => setFormData({...formData, fats: e.target.value})}
              />
            </div>

          </div>

          {/* Real-time Macro Ratio Visualizer */}
          {macroSummary.totalGrams > 0 && (
            <div style={{ marginTop: '10px', padding: '14px', background: 'rgba(255,255,255,0.02)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: '#9CA3AF', marginBottom: '8px' }}>
                <span>Macro Ratio Balance</span>
                <span>Total: {macroSummary.totalGrams}g</span>
              </div>
              
              {/* Ratio bar */}
              <div style={{ width: '100%', height: '8px', borderRadius: '4px', display: 'flex', overflow: 'hidden', backgroundColor: 'rgba(255,255,255,0.05)' }}>
                <div style={{ width: `${macroSummary.proteinPct}%`, height: '100%', backgroundColor: 'var(--primary-green)', transition: 'all 0.3s ease' }} />
                <div style={{ width: `${macroSummary.carbsPct}%`, height: '100%', backgroundColor: 'var(--secondary-blue)', transition: 'all 0.3s ease' }} />
                <div style={{ width: `${macroSummary.fatsPct}%`, height: '100%', backgroundColor: '#F59E0B', transition: 'all 0.3s ease' }} />
              </div>

              {/* Labels with percentages */}
              <div style={{ display: 'flex', justifyItems: 'center', gap: '14px', marginTop: '8px', fontSize: '0.7rem' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--primary-green)' }}>
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--primary-green)' }} />
                  P: {macroSummary.proteinPct}%
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--secondary-blue)' }}>
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--secondary-blue)' }} />
                  C: {macroSummary.carbsPct}%
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#F59E0B' }}>
                  <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#F59E0B' }} />
                  F: {macroSummary.fatsPct}%
                </span>
              </div>
            </div>
          )}

          <button 
            type="submit" 
            className="btn-primary w-full flex-center" 
            disabled={loading}
            style={{ gap: '10px', height: '52px', marginTop: '10px', width: '100%', borderRadius: '12px' }}
          >
            {loading ? 'Saving...' : <><Save size={18} /> Save Log Entry</>}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ManualEntryModal;
