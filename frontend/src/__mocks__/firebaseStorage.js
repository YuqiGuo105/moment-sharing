// __mocks__/firebase/storage.js
export const getStorage = () => ({
  ref:      jest.fn(),
  uploadBytes:  jest.fn(),
  getDownloadURL: jest.fn(),
});
