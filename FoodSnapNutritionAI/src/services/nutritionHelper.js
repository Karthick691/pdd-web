export const calculateNutritionTargets = (profile) => {
  if (!profile || !profile.weight || !profile.height) {
    return { calories: 2500, protein: 150, carbs: 300, fats: 80, bmr: 1500, tdee: 2000, targetWater: 2500 };
  }

  const w = parseFloat(profile.weight);
  const h = parseFloat(profile.height);
  const a = parseInt(profile.age || 25);
  const gender = profile.gender || 'Female';
  const activityLevel = profile.activityLevel || 'Moderate';
  const goal = profile.goal || 'General Fitness';

  if (isNaN(w) || isNaN(h)) {
    return { calories: 2500, protein: 150, carbs: 300, fats: 80, bmr: 1500, tdee: 2000, targetWater: 2500 };
  }

  // Mifflin-St Jeor Equation for BMR
  let bmr = 0;
  if (gender === 'Male') {
    bmr = 10 * w + 6.25 * h - 5 * a + 5;
  } else {
    bmr = 10 * w + 6.25 * h - 5 * a - 161;
  }

  // Activity multipliers
  let activityMultiplier = 1.2;
  if (activityLevel === 'Light') activityMultiplier = 1.375;
  else if (activityLevel === 'Moderate') activityMultiplier = 1.55;
  else if (activityLevel === 'Very Active') activityMultiplier = 1.725;

  const tdee = Math.round(bmr * activityMultiplier);

  let calories = tdee;
  let proteinMultiplier = 1.8;
  let fatPercent = 0.25;

  const cleanGoal = goal.toLowerCase().trim();

  if (cleanGoal === 'weight loss') {
    calories = Math.round(tdee - 500);
    proteinMultiplier = 2.0;
  } else if (cleanGoal === 'weight gain') {
    calories = Math.round(tdee + 500);
    proteinMultiplier = 1.8;
  } else if (cleanGoal === 'muscle building' || cleanGoal === 'muscle gain') {
    calories = Math.round(tdee + 300);
    proteinMultiplier = 2.2;
  } else {
    calories = tdee;
    proteinMultiplier = 1.6;
  }

  calories = Math.max(1200, Math.min(5000, calories));

  const proteinG = Math.round(w * proteinMultiplier);
  const fatG = Math.round((calories * fatPercent) / 9);
  const carbG = Math.round((calories - (proteinG * 4) - (fatG * 9)) / 4);
  const waterTarget = Math.round(w * 35);

  return {
    calories,
    protein: proteinG,
    carbs: carbG,
    fats: fatG,
    bmr: Math.round(bmr),
    tdee: Math.round(tdee),
    targetWater: waterTarget
  };
};
