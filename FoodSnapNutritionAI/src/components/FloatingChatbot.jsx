import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bot, X } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import AIChatBox from './AIChatBox';

const FloatingChatbot = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isOpen, setIsOpen] = useState(false);
  const chatRef = useRef(null);

  // Close when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (chatRef.current && !chatRef.current.contains(event.target) && !event.target.closest('.floating-chatbot-toggle')) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  if (location.pathname === '/chat') {
    return null;
  }

  return (
    <>
      <AnimatePresence>
        {isOpen && (
          <motion.div
            ref={chatRef}
            initial={{ opacity: 0, y: 50, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 50, scale: 0.9 }}
            transition={{ type: 'spring', damping: 25, stiffness: 300 }}
            style={{
              position: 'fixed',
              bottom: '100px',
              right: '30px',
              width: '380px',
              height: '520px',
              zIndex: 1000,
              display: 'flex',
              flexDirection: 'column',
              borderRadius: '24px',
              overflow: 'hidden',
              boxShadow: '0 20px 40px rgba(0, 0, 0, 0.25)',
              border: '1px solid var(--glass-border)',
              background: 'var(--glass-bg)',
              backdropFilter: 'blur(20px)',
            }}
          >
            {/* Header */}
            <div
              style={{
                padding: '16px 20px',
                background: 'linear-gradient(135deg, var(--primary-green) 0%, #059669 100%)',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div
                  style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Bot size={18} />
                </div>
                <div>
                  <h4 style={{ margin: 0, fontSize: '0.95rem', fontWeight: 600 }}>FoodSnap AI Assistant</h4>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '5px', marginTop: '2px' }}>
                    <span
                      style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        backgroundColor: '#10B981',
                        boxShadow: '0 0 8px #10B981',
                        display: 'inline-block',
                      }}
                    />
                    <span style={{ fontSize: '0.75rem', opacity: 0.85 }}>Online & Ready</span>
                  </div>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'white',
                  cursor: 'pointer',
                  padding: '4px',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'background-color 0.2s',
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.1)'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                aria-label="Close chat"
              >
                <X size={18} />
              </button>
            </div>

            {/* Chat Body */}
            <div style={{ flex: 1, minHeight: 0 }}>
              <AIChatBox noCard={true} />
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <motion.button
        className="floating-chatbot-toggle"
        initial={{ scale: 0, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        whileHover={{ scale: 1.1, rotate: isOpen ? -90 : 5 }}
        whileTap={{ scale: 0.95 }}
        onClick={() => setIsOpen(!isOpen)}
        style={{
          position: 'fixed',
          bottom: '30px',
          right: '30px',
          width: '60px',
          height: '60px',
          borderRadius: '50%',
          backgroundColor: isOpen ? '#EF4444' : 'var(--primary-green)',
          color: 'white',
          boxShadow: isOpen 
            ? '0 10px 25px rgba(239, 68, 68, 0.5)' 
            : '0 10px 25px rgba(16, 185, 129, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 999,
          cursor: 'pointer',
          border: '2px solid rgba(255, 255, 255, 0.4)',
          transition: 'background-color 0.3s, box-shadow 0.3s',
        }}
        aria-label={isOpen ? "Close AI Chat Assistant" : "Open AI Chat Assistant"}
      >
        <motion.div
          animate={isOpen ? { rotate: 90 } : { y: [0, -3, 0] }}
          transition={isOpen ? { duration: 0.2 } : { duration: 2, repeat: Infinity }}
        >
          {isOpen ? <X size={28} /> : <Bot size={28} />}
        </motion.div>
        {!isOpen && (
          <span
            style={{
              position: 'absolute',
              top: '-5px',
              right: '-5px',
              width: '14px',
              height: '14px',
              borderRadius: '50%',
              backgroundColor: '#EF4444',
              border: '2px solid white',
            }}
          />
        )}
      </motion.button>
    </>
  );
};

export default FloatingChatbot;
