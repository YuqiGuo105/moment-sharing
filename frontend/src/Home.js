import React from 'react';
import { signOut } from 'firebase/auth';
import { auth } from './lib/firebaseClient';

function Home({ user }) {
  return (
    <div className="App">
      {user.photoURL && (
        <img src={user.photoURL} alt="Profile" style={{ borderRadius: '50%' }} />
      )}
      <h1>Welcome {user.displayName || user.email}</h1>
      <p>{user.email}</p>
      <button onClick={() => signOut(auth)}>Sign Out</button>
    </div>
  );
}

export default Home;
