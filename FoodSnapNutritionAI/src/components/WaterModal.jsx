import React, { useState } from 'react';
import { X, Droplet, Plus } from 'lucide-react';

const WaterModal = ({ isOpen, onClose, onAdd }) => {
  const [customMl, setCustomMl] = useState('');

  if (!isOpen) return null;

  const handleQuickAdd = (ml) => {
    onAdd(ml);
    onClose();
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const ml = parseInt(customMl);
    if (isNaN(ml) || ml <= 0) {
      return;
    }
    onAdd(ml);
    setCustomMl('');
    onClose();
  };

  return (
    <div className="modal-overlay flex-center" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      backgroundColor: 'rgba(11, 15, 23, 0.8)',
      zIndex: 1200,
      backdropFilter: 'blur(10px)',
      padding: '20px'
    }}>
      <div className="glass-card animate-slide-up" style={{
        width: '100%',
        maxWidth: '360px',
        padding: '25px',
        position: 'relative',
        backgroundColor: '#141b26',
        border: '1px solid rgba(59, 130, 246, 0.15)',
        boxShadow: '0 20px 40px rgba(59, 130, 246, 0.1)',
        borderRadius: '20px',
        textAlign: 'center'
      }}>
        {/* Close Button */}
        <button 
          onClick={onClose}
          style={{ position: 'absolute', top: '15px', right: '15px', background: 'none', border: 'none', cursor: 'pointer', color: '#9CA3AF' }}
        >
          <X size={18} />
        </button>

        <div className="flex-center" style={{ flexDirection: 'column', marginBottom: '20px' }}>
          <div style={{ padding: '10px', borderRadius: '50%', background: 'rgba(59, 130, 246, 0.1)', marginBottom: '12px' }}>
            <Droplet className="text-secondary" size={32} />
          </div>
          <h3 className="heading-3" style={{ margin: 0, color: 'white', fontSize: '1.25rem' }}>Log Water Intake</h3>
          <p style={{ margin: '4px 0 0 0', color: '#9CA3AF', fontSize: '0.8rem' }}>Select a preset or input custom ml value.</p>
        </div>

        {/* Preset selections */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px', marginBottom: '20px' }}>
          <button
            type="button"
            onClick={() => handleQuickAdd(250)}
            style={{
              padding: '12px 6px',
              borderRadius: '12px',
              border: '1px solid rgba(59, 130, 246, 0.15)',
              background: 'rgba(59, 130, 246, 0.03)',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '4px',
              fontSize: '0.8rem'
            }}
          >
            <strong>+250</strong>
            <span style={{ fontSize: '0.65rem', color: '#6B7280' }}>Glass</span>
          </button>

          <button
            type="button"
            onClick={() => handleQuickAdd(500)}
            style={{
              padding: '12px 6px',
              borderRadius: '12px',
              border: '1px solid rgba(59, 130, 246, 0.15)',
              background: 'rgba(59, 130, 246, 0.03)',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '4px',
              fontSize: '0.8rem'
            }}
          >
            <strong>+500</strong>
            <span style={{ fontSize: '0.65rem', color: '#6B7280' }}>Bottle</span>
          </button>

          <button
            type="button"
            onClick={() => handleQuickAdd(750)}
            style={{
              padding: '12px 6px',
              borderRadius: '12px',
              border: '1px solid rgba(59, 130, 246, 0.15)',
              background: 'rgba(59, 130, 246, 0.03)',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '4px',
              fontSize: '0.8rem'
            }}
          >
            <strong>+750</strong>
            <span style={{ fontSize: '0.65rem', color: '#6B7280' }}>Flask</span>
          </button>
        </div>

        {/* Custom Input Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input
              type="number"
              placeholder="Custom ml (e.g. 300)"
              value={customMl}
              onChange={(e) => setCustomMl(e.target.value)}
              style={{
                flex: 1,
                padding: '12px',
                borderRadius: '10px',
                border: '1px solid rgba(255, 255, 255, 0.08)',
                background: '#0B0F17',
                color: 'white',
                outline: 'none',
                fontSize: '0.9rem'
              }}
            />
            <span style={{ color: '#9CA3AF', fontSize: '0.9rem' }}>ml</span>
          </div>

          <button
            type="submit"
            className="btn-secondary"
            style={{
              width: '100%',
              padding: '12px',
              borderRadius: '10px',
              fontSize: '0.9rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px'
            }}
          >
            <Plus size={16} /> Log Custom Water
          </button>
        </form>
      </div>
    </div>
  );
};

export default WaterModal;
