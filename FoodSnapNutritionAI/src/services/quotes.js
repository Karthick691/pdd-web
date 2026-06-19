const FITNESS_QUOTES = [
  { text: "Your body can stand almost anything. It's your mind that you have to convince.", author: "Unknown" },
  { text: "Success starts with self-discipline.", author: "Dwayne Johnson" },
  { text: "The only bad workout is the one that didn't happen.", author: "Unknown" },
  { text: "Energy and persistence conquer all things.", author: "Benjamin Franklin" },
  { text: "Health is a state of complete harmony of the body, mind and spirit.", author: "B.K.S. Iyengar" },
  { text: "Believe you can and you're halfway there.", author: "Theodore Roosevelt" },
  { text: "What hurts today makes you stronger tomorrow.", author: "Jay Cutler" },
  { text: "Strength does not come from physical capacity. It comes from an indomitable will.", author: "Mahatma Gandhi" },
  { text: "The clock is ticking. Are you becoming the person you want to be?", author: "Greg Plitt" },
  { text: "If you want something you've never had, you must be willing to do something you've never done.", author: "Thomas Jefferson" }
];

export const getRandomQuote = () => {
  const index = Math.floor(Math.random() * FITNESS_QUOTES.length);
  return FITNESS_QUOTES[index];
};
