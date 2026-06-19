import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";
import { initializeAppCheck, ReCaptchaEnterpriseProvider } from "firebase/app-check";

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyBcFXuoHnpDKRBrDrNglqRiJ3nSQX5eDVc",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "food-snap-87cfb.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "food-snap-87cfb",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "food-snap-87cfb.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "47398940550",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:47398940550:web:cffe8e94886034e8dd8607",
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || "G-5XDBM5JHVG"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Firebase App Check if reCAPTCHA key is available (V-05)
if (typeof window !== "undefined" && import.meta.env.VITE_RECAPTCHA_ENTERPRISE_KEY) {
  try {
    initializeAppCheck(app, {
      provider: new ReCaptchaEnterpriseProvider(import.meta.env.VITE_RECAPTCHA_ENTERPRISE_KEY),
      isTokenAutoRefreshEnabled: true
    });
  } catch (err) {
    console.warn("Firebase App Check initialization failed:", err);
  }
}

// Initialize services
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

export default app;
