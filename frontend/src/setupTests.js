// src/setupTests.js
// -----------------------------------------------------------------------------
// 1. Extra matchers for Testing-Library
import '@testing-library/jest-dom';

// -----------------------------------------------------------------------------
// 2. firebase/app — trivial stub
jest.mock('firebase/app', () => ({
  initializeApp: jest.fn(() => ({})),
  getApp:        jest.fn(() => ({})),
}));

// -----------------------------------------------------------------------------
// 3. firebase/firestore — only the helpers your code touches
jest.mock('firebase/firestore', () => ({
  getFirestore: jest.fn(() => ({})),
  doc:          jest.fn(),
  getDoc:       jest.fn(),
  setDoc:       jest.fn(),
  collection:   jest.fn(),
  addDoc:       jest.fn(),
  updateDoc:    jest.fn(),
  onSnapshot:   jest.fn(),
}));

// -----------------------------------------------------------------------------
// 4. firebase/auth — make onAuthStateChanged *return an unsubscribe fn*
//    so useEffect cleanup in App.js stops crashing.
jest.mock('firebase/auth', () => {
  const mockAuthInstance = { currentUser: null };

  const onAuthStateChanged = jest.fn((_, cb) => {
    cb(null);               // emit “logged-out” once
    return jest.fn();       // <-- unsubscribe stub
  });

  return {
    getAuth:                    jest.fn(() => mockAuthInstance),
    onAuthStateChanged,
    signOut:                    jest.fn(),
    signInWithEmailAndPassword: jest.fn(),
    createUserWithEmailAndPassword: jest.fn(),
    GoogleAuthProvider:         jest.fn(),
    signInWithPopup:            jest.fn(),
  };
});

// -----------------------------------------------------------------------------
// 5. firebase/storage — fully stubbed
jest.mock('firebase/storage', () => {
  const ref = jest.fn(() => ({ child: jest.fn() }));
  const uploadBytes    = jest.fn();
  const getDownloadURL = jest.fn();

  return {
    getStorage: jest.fn(() => ({ ref, uploadBytes, getDownloadURL })),
    ref,
    uploadBytes,
    getDownloadURL,
  };
});
