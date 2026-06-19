import React, { useState } from 'react';
import { Send, Mail, MapPin, Phone, CheckCircle } from 'lucide-react';
import PageTransition from '../animations/PageTransition';

const Contact = () => {
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    // Simulate API call
    setTimeout(() => {
      setLoading(false);
      setSubmitted(true);
    }, 1500);
  };

  if (submitted) {
    return (
      <PageTransition className="page-container flex-center">
        <div className="glass-card text-center" style={{ padding: '60px', maxWidth: '500px' }}>
          <CheckCircle size={80} className="text-primary mb-6" style={{ margin: '0 auto' }} />
          <h2 className="heading-2">Message Sent!</h2>
          <p style={{ color: '#6B7280', marginBottom: '30px' }}>
            Thank you for reaching out. Our nutrition experts will get back to you within 24 hours.
          </p>
          <button className="btn-primary" onClick={() => setSubmitted(false)}>Send Another Message</button>
        </div>
      </PageTransition>
    );
  }

  return (
    <PageTransition className="page-container">
      <div style={{ textAlign: 'center', marginBottom: '60px' }}>
        <h1 className="heading-1">Contact Our Experts</h1>
        <p style={{ color: '#6B7280', fontSize: '1.1rem' }}>Have questions? We are here to help you on your health journey.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '50px', maxWidth: '1100px', margin: '0 auto' }}>
        <div className="glass-card" style={{ padding: '40px' }}>
          <h3 className="heading-3 mb-6">Send a Message</h3>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <label className="label">Full Name</label>
              <input type="text" className="input-field" placeholder="John Doe" required />
            </div>
            <div>
              <label className="label">Email Address</label>
              <input type="email" className="input-field" placeholder="john@example.com" required />
            </div>
            <div>
              <label className="label">Subject</label>
              <select className="input-field" style={{ appearance: 'none' }}>
                <option>General Inquiry</option>
                <option>Nutrition Coaching</option>
                <option>App Support</option>
                <option>Partnership</option>
              </select>
            </div>
            <div>
              <label className="label">Message</label>
              <textarea className="input-field" rows="4" placeholder="How can we help?" required></textarea>
            </div>
            <button type="submit" disabled={loading} className="btn-primary flex-center" style={{ gap: '10px' }}>
              {loading ? 'Sending...' : <><Send size={18} /> Send Message</>}
            </button>
          </form>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
          <div className="glass-card" style={{ padding: '30px' }}>
            <h4 className="heading-4 mb-4">Contact Information</h4>
            <div className="flex-center mb-4" style={{ justifyContent: 'flex-start', gap: '15px' }}>
              <div style={{ backgroundColor: 'rgba(16, 185, 129, 0.1)', padding: '10px', borderRadius: '12px' }}>
                <Mail className="text-primary" size={20} />
              </div>
              <div>
                <span style={{ display: 'block', fontSize: '0.8rem', color: '#6B7280' }}>Email Us</span>
                <span style={{ fontWeight: 600 }}>support@foodsnap.ai</span>
              </div>
            </div>
            <div className="flex-center mb-4" style={{ justifyContent: 'flex-start', gap: '15px' }}>
              <div style={{ backgroundColor: 'rgba(59, 130, 246, 0.1)', padding: '10px', borderRadius: '12px' }}>
                <Phone className="text-secondary" size={20} />
              </div>
              <div>
                <span style={{ display: 'block', fontSize: '0.8rem', color: '#6B7280' }}>Call Us</span>
                <span style={{ fontWeight: 600 }}>+1 (555) 000-HEALTH</span>
              </div>
            </div>
            <div className="flex-center" style={{ justifyContent: 'flex-start', gap: '15px' }}>
              <div style={{ backgroundColor: 'rgba(139, 92, 246, 0.1)', padding: '10px', borderRadius: '12px' }}>
                <MapPin style={{ color: '#8B5CF6' }} size={20} />
              </div>
              <div>
                <span style={{ display: 'block', fontSize: '0.8rem', color: '#6B7280' }}>Location</span>
                <span style={{ fontWeight: 600 }}>Silicon Valley, CA</span>
              </div>
            </div>
          </div>

          <div className="glass-card" style={{ padding: '30px', backgroundColor: 'var(--primary-green)', color: 'white' }}>
            <h4 className="heading-4 mb-2" style={{ color: 'white' }}>Enterprise Solutions</h4>
            <p style={{ opacity: 0.9, fontSize: '0.9rem', marginBottom: '20px' }}>
              Looking for FoodSnap for your employees or gym? We offer custom solutions for organizations.
            </p>
            <button className="btn-secondary" style={{ width: '100%', backgroundColor: 'white', color: 'var(--primary-green)' }}>Learn More</button>
          </div>
        </div>
      </div>
    </PageTransition>
  );
};

export default Contact;
