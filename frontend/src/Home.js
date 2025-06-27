import React from 'react';
import { signOut } from 'firebase/auth';
import { auth } from './lib/firebaseClient';
import logo from './logo.svg';

function Home({ user }) {
  return (
    <div className="App">
      <img
        src={user.photoURL || logo}
        alt="Profile"
        style={{ borderRadius: '50%', width: 96, height: 96 }}
      />
      <h1>Welcome {user.displayName || user.email}</h1>
      <p>{user.email}</p>
      <button onClick={() => signOut(auth)}>Sign Out</button>
    </div>
  );
}

export default Home;
