import { useState } from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from 'recharts';
import { motion } from 'framer-motion';

const calorieData = [
  { day: 'Mon', calories: 1850, target: 2000 },
  { day: 'Tue', calories: 2100, target: 2000 },
  { day: 'Wed', calories: 1650, target: 2000 },
  { day: 'Thu', calories: 1950, target: 2000 },
  { day: 'Fri', calories: 2250, target: 2000 },
  { day: 'Sat', calories: 1750, target: 2000 },
  { day: 'Sun', calories: 1980, target: 2000 },
];

const macroData = [
  { name: 'Protein', value: 140, color: '#10B981' },
  { name: 'Carbs', value: 220, color: '#3B82F6' },
  { name: 'Fats', value: 70, color: '#F59E0B' },
];

const waterData = [
  { day: 'Mon', intake: 1.8 },
  { day: 'Tue', intake: 2.2 },
  { day: 'Wed', intake: 1.5 },
  { day: 'Thu', intake: 2.5 },
  { day: 'Fri', intake: 2.0 },
  { day: 'Sat', intake: 2.8 },
  { day: 'Sun', intake: 2.4 },
];

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div
        style={{
          background: 'var(--glass-bg)',
          backdropFilter: 'blur(12px)',
          border: '1px solid var(--glass-border)',
          borderRadius: '12px',
          padding: '10px 15px',
          boxShadow: 'var(--shadow-md)',
          color: 'var(--dark-text)',
        }}
      >
        <p style={{ fontWeight: 600, margin: 0, fontSize: '0.9rem' }}>{label || payload[0].name}</p>
        {payload.map((entry, index) => (
          <p key={index} style={{ color: entry.color, margin: 0, fontSize: '0.85rem', fontWeight: 500 }}>
            {entry.name}: {entry.value} {entry.name === 'intake' ? 'L' : entry.name === 'calories' ? 'kcal' : 'g'}
          </p>
        ))}
      </div>
    );
  }
  return null;
};

const NutritionChart = () => {
  const [activeTab, setActiveTab] = useState('calories');

  return (
    <div style={{ width: '100%' }}>
      {/* Chart Selector Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', overflowX: 'auto', paddingBottom: '4px' }}>
        {['calories', 'macros', 'hydration'].map((tab) => (
          <motion.button
            key={tab}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={() => setActiveTab(tab)}
            style={{
              padding: '6px 16px',
              borderRadius: '20px',
              fontSize: '0.85rem',
              fontWeight: activeTab === tab ? 600 : 500,
              textTransform: 'capitalize',
              backgroundColor: activeTab === tab ? 'var(--primary-green)' : 'var(--light-gray)',
              color: activeTab === tab ? 'white' : 'var(--dark-text)',
              transition: 'background-color 0.3s ease',
            }}
          >
            {tab}
          </motion.button>
        ))}
      </div>

      {/* Render Dynamic Graphs */}
      <div style={{ height: '280px', width: '100%', position: 'relative' }}>
        {activeTab === 'calories' && (
          <motion.div
            key="calories"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.4 }}
            style={{ width: '100%', height: '100%' }}
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={calorieData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="day" stroke="#6B7280" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#6B7280" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(16, 185, 129, 0.05)' }} />
                <Bar dataKey="calories" name="Calories" fill="var(--primary-green)" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </motion.div>
        )}

        {activeTab === 'macros' && (
          <motion.div
            key="macros"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.4 }}
            style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={macroData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={5}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  labelLine={false}
                  style={{ fontSize: '0.75rem', fontWeight: 600, fill: 'var(--dark-text)' }}
                >
                  {macroData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} stroke="transparent" />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
              </PieChart>
            </ResponsiveContainer>
          </motion.div>
        )}

        {activeTab === 'hydration' && (
          <motion.div
            key="hydration"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.4 }}
            style={{ width: '100%', height: '100%' }}
          >
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={waterData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="day" stroke="#6B7280" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#6B7280" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip content={<CustomTooltip />} />
                <Area
                  type="monotone"
                  dataKey="intake"
                  name="Water Intake"
                  stroke="var(--secondary-blue)"
                  fillOpacity={0.2}
                  fill="var(--secondary-blue)"
                  strokeWidth={3}
                />
              </AreaChart>
            </ResponsiveContainer>
          </motion.div>
        )}
      </div>
    </div>
  );
};

export default NutritionChart;
