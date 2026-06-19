import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UploadCloud, Camera } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { AIScanLoader } from '../animations/LoadingComponents';
import { analyzeFoodImage } from '../services/api';

import { addDoc, collection } from 'firebase/firestore';
import { db } from '../firebase/config';
import { useAuth } from '../context/AuthContext';

const UploadBox = () => {
  const [isDragging, setIsDragging] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  const [isCameraOpen, setIsCameraOpen] = useState(false);
  const [facingMode, setFacingMode] = useState('environment');
  const [stream, setStream] = useState(null);

  const fileInputRef = useRef(null);
  const videoRef = useRef(null);
  const navigate = useNavigate();
  const { currentUser } = useAuth();

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setIsDragging(true);
    } else if (e.type === 'dragleave') {
      setIsDragging(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      handleFile(files[0]);
    }
  };

  const handleFileChange = (e) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      handleFile(files[0]);
    }
  };

  const handleFile = async (file) => {
    if (!file.type.startsWith('image/')) {
      toast.error('Please upload an image file.');
      return;
    }

    setIsScanning(true);
    const toastId = toast.loading('AI analyzing food macros...');

    try {
      const nutritionData = await analyzeFoodImage(file);
      
      toast.dismiss(toastId);
      toast.success('Scan complete! Macros calculated.');
      
      // Pass the data to the result page - NO AUTO SAVE
      navigate('/result', { state: { nutritionData } });
    } catch (error) {
      console.error("Firestore error:", error);
      toast.dismiss(toastId);
      toast.error('Analysis failed. Please try again.');
      setIsScanning(false);
    }
  };

  const startCamera = async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: facingMode }
      });
      setStream(mediaStream);
      setIsCameraOpen(true);
      // We need to wait a tiny bit for the video element to mount
      setTimeout(() => {
        if (videoRef.current) {
          videoRef.current.srcObject = mediaStream;
        }
      }, 50);
    } catch (err) {
      console.error("Error accessing camera:", err);
      toast.error("Could not access your camera. Please check permissions.");
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
    }
    setIsCameraOpen(false);
  };

  const toggleFacingMode = () => {
    setFacingMode(prev => prev === 'user' ? 'environment' : 'user');
  };

  // Restart camera when facingMode changes
  useEffect(() => {
    if (isCameraOpen) {
      if (stream) {
        stream.getTracks().forEach(track => track.stop());
      }
      const restart = async () => {
        try {
          const mediaStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: facingMode }
          });
          setStream(mediaStream);
          if (videoRef.current) {
            videoRef.current.srcObject = mediaStream;
          }
        } catch (err) {
          console.error("Error toggling camera facingMode:", err);
        }
      };
      restart();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [facingMode]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (stream) {
        stream.getTracks().forEach(track => track.stop());
      }
    };
  }, [stream]);

  const capturePhoto = () => {
    if (!videoRef.current) return;
    const video = videoRef.current;
    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob((blob) => {
      if (blob) {
        const file = new File([blob], "captured_food.jpg", { type: "image/jpeg" });
        stopCamera();
        handleFile(file);
      }
    }, 'image/jpeg', 0.95);
  };

  return (
    <motion.div
      whileHover={!isScanning && !isCameraOpen ? { scale: 1.01 } : {}}
      transition={{ duration: 0.2 }}
      className={`upload-box ${isDragging ? 'dragging' : ''}`}
      onDragEnter={handleDrag}
      onDragLeave={handleDrag}
      onDragOver={handleDrag}
      onDrop={handleDrop}
      style={{
        border: isDragging ? '2px dashed var(--primary-green)' : '2px dashed var(--border-color)',
        borderRadius: '20px',
        padding: isCameraOpen ? '24px' : '50px 20px',
        transition: 'all 0.3s ease',
        position: 'relative',
        overflow: 'hidden',
        background: isDragging ? 'rgba(16, 185, 129, 0.05)' : 'var(--glass-bg)',
        backdropFilter: 'blur(12px)',
        textAlign: 'center',
      }}
    >
      <input 
        type="file" 
        ref={fileInputRef} 
        onChange={handleFileChange} 
        style={{ display: 'none' }} 
        accept="image/*"
      />

      {isScanning ? (
        <AIScanLoader />
      ) : isCameraOpen ? (
        <div className="camera-container flex-center flex-column" style={{ width: '100%' }}>
          <video
            ref={videoRef}
            autoPlay
            playsInline
            style={{
              width: '100%',
              maxHeight: '350px',
              borderRadius: '16px',
              backgroundColor: '#000',
              objectFit: 'cover',
              boxShadow: 'var(--shadow-md)',
              border: '1px solid var(--border-color)',
            }}
          />
          <div className="camera-controls flex-center" style={{ gap: '16px', marginTop: '20px', flexWrap: 'wrap' }}>
            <button className="btn-outline" onClick={stopCamera}>
              Cancel
            </button>
            <button className="btn-primary" onClick={capturePhoto} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Camera size={18} /> Capture Photo
            </button>
            <button className="btn-outline" onClick={toggleFacingMode}>
              Switch Camera
            </button>
          </div>
        </div>
      ) : (
        <div className="upload-content flex-center flex-column">
          <motion.div
            animate={{ y: [0, -6, 0] }}
            transition={{ duration: 2.5, repeat: Infinity }}
            style={{ marginBottom: '16px' }}
          >
            <UploadCloud size={56} className="text-secondary" />
          </motion.div>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: 'var(--dark-text)', marginBottom: '8px' }}>
            Drag & Drop your food image here
          </h3>
          <p style={{ color: '#6B7280', fontSize: '0.9rem', marginBottom: '16px' }}>or</p>
          <div className="upload-buttons flex-center" style={{ gap: '16px', flexWrap: 'wrap' }}>
            <button className="btn-primary" onClick={() => fileInputRef.current.click()}>
              Browse Files
            </button>
            <button className="btn-outline" onClick={startCamera}>
              <Camera size={18} /> Open Camera
            </button>
          </div>
          <p style={{ marginTop: '24px', fontSize: '0.8rem', color: '#9CA3AF' }}>
            Supports JPG, PNG, WEBP (Max 5MB)
          </p>
        </div>
      )}
    </motion.div>
  );
};

export default UploadBox;

