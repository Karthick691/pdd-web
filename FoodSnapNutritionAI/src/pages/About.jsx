import { Activity, Brain, Code, Users } from 'lucide-react';

const About = () => {
  return (
    <div className="page-container animate-fade-in" style={{maxWidth: '900px', margin: '0 auto'}}>
      <h1 className="heading-1 text-center" style={{textAlign: 'center', marginBottom: '20px'}}>About FoodSnap AI</h1>
      <p className="text-center text-secondary" style={{textAlign: 'center', marginBottom: '50px', fontSize: '1.1rem'}}>Revolutionizing nutrition tracking with the power of Artificial Intelligence.</p>
      
      <div className="glass-card" style={{padding: '40px', marginBottom: '40px'}}>
        <div className="flex-center" style={{justifyContent: 'flex-start', gap: '15px', marginBottom: '20px'}}>
          <Activity className="text-primary" size={32} />
          <h2 className="heading-2" style={{margin: 0}}>Our Mission</h2>
        </div>
        <p style={{lineHeight: '1.8', color: '#4B5563', fontSize: '1.05rem'}}>
          We believe that tracking your nutrition should be as simple as taking a photo. FoodSnap AI was created to eliminate the tedious manual entry of food logging. By leveraging advanced machine learning, we provide instant, accurate nutritional breakdowns, making healthy living accessible and effortless for everyone.
        </p>
      </div>

      <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '30px', marginBottom: '40px'}}>
        <div className="glass-card" style={{padding: '30px'}}>
          <Brain className="text-secondary mb-4" size={36} />
          <h3 className="heading-3">How the AI Works</h3>
          <p style={{color: '#6B7280'}}>Our system uses state-of-the-art computer vision to identify food items and cross-references them with a massive nutritional database to estimate portion sizes and macros.</p>
        </div>
        
        <div className="glass-card" style={{padding: '30px'}}>
          <Code className="text-primary mb-4" size={36} />
          <h3 className="heading-3">Technologies</h3>
          <ul style={{color: '#6B7280', listStylePosition: 'inside'}}>
            <li>React.js & Tailwind</li>
            <li>Node.js Backend</li>
            <li>Python Flask Microservice</li>
            <li>TensorFlow / PyTorch</li>
          </ul>
        </div>
      </div>

      <div className="glass-card" style={{padding: '40px'}}>
        <div className="flex-center" style={{justifyContent: 'flex-start', gap: '15px', marginBottom: '20px'}}>
          <Users className="text-secondary" size={32} />
          <h2 className="heading-2" style={{margin: 0}}>The Team</h2>
        </div>
        <p style={{color: '#4B5563'}}>Built by a passionate team of developers, data scientists, and nutritionists dedicated to improving global health through technology.</p>
      </div>
    </div>
  );
};

export default About;
