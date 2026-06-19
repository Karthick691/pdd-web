import AIChatBox from '../components/AIChatBox';

const AIChatAssistant = () => {
  return (
    <div className="page-container animate-fade-in" style={{height: 'calc(100vh - 200px)', display: 'flex', flexDirection: 'column'}}>
      <h1 className="heading-1 text-center" style={{textAlign: 'center', marginBottom: '20px'}}>Your AI Health Assistant</h1>
      <p className="text-center text-secondary" style={{textAlign: 'center', marginBottom: '40px'}}>Ask me anything about your diet, health goals, or nutrition.</p>
      
      <div style={{flex: 1, minHeight: '400px'}}>
        <AIChatBox />
      </div>
    </div>
  );
};

export default AIChatAssistant;
