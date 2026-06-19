import React, { useState, useEffect } from 'react';
import { User, Mail, Settings, LogOut, Award, Calendar, Activity, Scale, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import PageTransition from '../animations/PageTransition';
import toast from 'react-hot-toast';
import { db } from '../firebase/config';
import { doc, getDoc } from 'firebase/firestore';

const Profile = () => {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const userName = currentUser?.displayName || currentUser?.email?.split('@')[0] || 'User';

  useEffect(() => {
    const fetchProfile = async () => {
      if (!currentUser) return;
      try {
        const docRef = doc(db, 'profiles', currentUser.uid);
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
          setProfile(docSnap.data());
        }
      } catch (error) {
        console.error("Error fetching profile:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [currentUser]);

  const handleLogout = async () => {
    try {
      await logout();
      toast.success('Logged out successfully');
      navigate('/login');
    } catch (error) {
      toast.error('Failed to log out');
    }
  };

  return (
    <PageTransition className="page-container">
      <div className="flex-center" style={{ flexDirection: 'column', gap: '30px' }}>
        
        {/* User Card */}
        <div className="glass-card" style={{ width: '100%', maxWidth: '800px', padding: '40px', textAlign: 'center' }}>
          <div style={{ position: 'relative', display: 'inline-block', marginBottom: '20px' }}>
            <div style={{ 
              width: '120px', 
              height: '120px', 
              borderRadius: '50%', 
              background: 'linear-gradient(135deg, var(--primary-green), var(--secondary-blue))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto',
              border: '4px solid rgba(255,255,255,0.1)'
            }}>
              <User size={60} color="white" />
            </div>
            <div style={{
              position: 'absolute',
              bottom: '5px',
              right: '5px',
              backgroundColor: 'var(--primary-green)',
              padding: '8px',
              borderRadius: '50%',
              border: '3px solid #0f172a'
            }}>
              <Award size={18} color="white" />
            </div>
          </div>
          
          <h1 className="heading-1" style={{ marginBottom: '5px' }}>{userName}</h1>
          <p style={{ color: '#6B7280' }}>Premium Health Member</p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '20px', marginTop: '40px' }}>
            <div className="glass-card" style={{ padding: '20px', backgroundColor: 'rgba(255,255,255,0.02)', borderColor: 'rgba(255,255,255,0.04)' }}>
              <Activity className="text-primary mb-2" />
              <h4 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--dark-text)' }}>Level 1</h4>
              <p style={{ margin: 0, fontSize: '0.8rem', color: '#6B7280' }}>Health XP</p>
            </div>
            <div className="glass-card" style={{ padding: '20px', backgroundColor: 'rgba(255,255,255,0.02)', borderColor: 'rgba(255,255,255,0.04)' }}>
              <Calendar className="text-secondary mb-2" />
              <h4 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--dark-text)' }}>0 Days</h4>
              <p style={{ margin: 0, fontSize: '0.8rem', color: '#6B7280' }}>Best Streak</p>
            </div>
          </div>
        </div>

        {/* Biometrics Profile Card */}
        {profile ? (
          <div className="glass-card" style={{ width: '100%', maxWidth: '800px', padding: '30px', textAlign: 'left' }}>
            <h3 className="heading-3" style={{ margin: '0 0 20px 0', fontSize: '1.25rem', color: 'var(--dark-text)' }}>Biometric Profile</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
              <div style={{ padding: '20px', backgroundColor: 'rgba(255,255,255,0.01)', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)' }}>
                <span style={{ fontSize: '0.8rem', color: '#6B7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>HEIGHT</span>
                <strong style={{ fontSize: '1.4rem', color: 'var(--primary-green)' }}>{profile.height} cm</strong>
              </div>
              <div style={{ padding: '20px', backgroundColor: 'rgba(255,255,255,0.01)', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)' }}>
                <span style={{ fontSize: '0.8rem', color: '#6B7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>WEIGHT</span>
                <strong style={{ fontSize: '1.4rem', color: 'var(--secondary-blue)' }}>{profile.weight} kg</strong>
              </div>
              <div style={{ padding: '20px', backgroundColor: 'rgba(255,255,255,0.01)', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)' }}>
                <span style={{ fontSize: '0.8rem', color: '#6B7280', fontWeight: 600, display: 'block', marginBottom: '6px' }}>FITNESS GOAL</span>
                <strong style={{ fontSize: '1.25rem', color: '#F59E0B', display: 'block', marginTop: '3px' }}>{profile.goal}</strong>
              </div>
            </div>
          </div>
        ) : (
          <div className="glass-card" style={{ width: '100%', maxWidth: '800px', padding: '25px', borderLeft: '4px solid #F59E0B', background: 'rgba(245, 158, 11, 0.03)', textAlign: 'left' }}>
            <h4 style={{ margin: 0, color: 'var(--dark-text)', fontSize: '0.95rem' }}>No biometrics configured</h4>
            <p style={{ margin: '4px 0 0 0', color: '#9CA3AF', fontSize: '0.8rem' }}>Set your biological metrics in the Dashboard to generate your custom profile here.</p>
          </div>
        )}

        {/* Settings Card */}
        <div className="glass-card" style={{ width: '100%', maxWidth: '800px', padding: '0' }}>
          <div style={{ padding: '25px', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
            <h3 className="heading-3" style={{ margin: 0, color: 'var(--dark-text)' }}>Account Settings</h3>
          </div>
          
          <div style={{ padding: '10px' }}>
            <div className="profile-item flex-center" style={{ justifyContent: 'space-between', padding: '15px 20px', borderRadius: '12px', cursor: 'pointer' }}>
              <div className="flex-center" style={{ gap: '15px' }}>
                <Mail size={20} style={{ color: '#6B7280' }} />
                <span style={{ color: 'var(--dark-text)' }}>{currentUser?.email}</span>
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--primary-green)' }}>Verified</span>
            </div>

            <div className="profile-item flex-center" style={{ justifyContent: 'space-between', padding: '15px 20px', borderRadius: '12px', cursor: 'pointer' }}>
              <div className="flex-center" style={{ gap: '15px' }}>
                <Settings size={20} style={{ color: '#6B7280' }} />
                <span style={{ color: 'var(--dark-text)' }}>Preferences</span>
              </div>
            </div>

            <div className="profile-item flex-center" style={{ justifyContent: 'space-between', padding: '15px 20px', borderRadius: '12px' }}>
              <div className="flex-center" style={{ gap: '15px' }}>
                <RefreshCw size={20} style={{ color: '#6B7280' }} />
                <span style={{ color: 'var(--dark-text)' }}>Database Sync Status</span>
              </div>
              <span style={{ fontSize: '0.8rem', color: '#9CA3AF' }}>
                {profile?.lastSynced ? `Last Synced: ${new Date(profile.lastSynced).toLocaleString()}` : 'Not synced yet'}
              </span>
            </div>

            <div 
              onClick={handleLogout}
              className="profile-item flex-center" 
              style={{ justifyContent: 'space-between', padding: '15px 20px', borderRadius: '12px', cursor: 'pointer', color: '#EF4444' }}
            >
              <div className="flex-center" style={{ gap: '15px' }}>
                <LogOut size={20} />
                <span style={{ fontWeight: 600 }}>Logout of FoodSnap</span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </PageTransition>
  );
};

export default Profile;
