import React, { useState } from 'react';
import { signOut } from 'firebase/auth';
import { auth, storage } from './lib/firebaseClient';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import logo from './logo.svg';
import './Home.css';

function Home({ user }) {
  const [uploading, setUploading] = useState(false);
  const [url, setUrl] = useState('');
  const [description, setDescription] = useState('');

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    try {
      const fileRef = ref(
        storage,
        `uploads/${user.uid}/${Date.now()}_${file.name}`
      );
      await uploadBytes(fileRef, file);
      const downloadURL = await getDownloadURL(fileRef);
      setUrl(downloadURL);

      try {
        const token = await user.getIdToken();
        await fetch(
          `${process.env.REACT_APP_BACKEND_BASE_URL || ''}/records`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
              url: downloadURL,
              description,
            }),
          }
        );
      } catch (err) {
        console.error('Failed to create record', err);
      }
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="home-container">
      <img src={user.photoURL || logo} alt="Profile" className="profile" />
      <h1>Welcome {user.displayName || user.email}</h1>
      <p>{user.email}</p>

      <textarea
        placeholder="Description (optional)"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <input
        type="file"
        accept="image/*"
        capture="environment"
        onChange={handleFileChange}
      />
      {uploading && <p>Uploading...</p>}
      {url && (
        <>
          <img src={url} alt="Uploaded" className="upload-preview" />
          <p>
            <a href={url} target="_blank" rel="noopener noreferrer">
              {url}
            </a>
          </p>
        </>
      )}

      <button onClick={() => signOut(auth)}>Sign Out</button>
    </div>
  );
}

export default Home;
