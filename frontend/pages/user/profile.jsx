import { useState, useEffect } from 'react';
import ProtectedRoute from '../../components/ProtectedRoute';
import Layout from '../../components/Layout';
import { getProfile, updateProfile, updatePassword } from '../../lib/api';

export default function Profile() {
  const [profile, setProfile] = useState({ firstName: '', lastName: '', email: '', role: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [pwdMessage, setPwdMessage] = useState({ text: '', type: '' });
  const [pwdSaving, setPwdSaving] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await getProfile();
        setProfile(response.data);
      } catch (err) {
        setMessage({ text: 'Failed to load profile', type: 'error' });
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage({ text: '', type: '' });
    try {
      await updateProfile({ firstName: profile.firstName, lastName: profile.lastName });
      setMessage({ text: 'Profile updated successfully', type: 'success' });
    } catch (err) {
      setMessage({ text: 'Failed to update profile', type: 'error' });
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswords(prev => ({ ...prev, [name]: value }));
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPwdMessage({ text: '', type: '' });
    
    if (passwords.newPassword !== passwords.confirmPassword) {
      setPwdMessage({ text: 'New passwords do not match', type: 'error' });
      return;
    }
    
    setPwdSaving(true);
    try {
      await updatePassword({ 
        currentPassword: passwords.currentPassword, 
        newPassword: passwords.newPassword 
      });
      setPwdMessage({ text: 'Password updated successfully', type: 'success' });
      setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPwdMessage({ text: err.response?.data?.message || 'Failed to update password', type: 'error' });
    } finally {
      setPwdSaving(false);
    }
  };

  return (
    <ProtectedRoute>
      <Layout>
        <h1 style={{ marginBottom: '2rem' }}>My Profile</h1>
        
        {loading ? (
          <p>Loading profile...</p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '2rem', alignItems: 'start' }}>
            
            {/* General Information */}
            <div className="glass card">
              <h2 style={{ margin: '0 0 1.5rem 0', fontSize: '1.25rem' }}>General Information</h2>
              
              {message.text && (
                <div style={{ 
                  padding: '0.75rem', marginBottom: '1rem', borderRadius: 'var(--radius)',
                  background: message.type === 'success' ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                  color: message.type === 'success' ? 'var(--success)' : 'var(--danger)'
                }}>
                  {message.text}
                </div>
              )}
              
              <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div style={{ flex: 1 }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>First Name</label>
                    <input type="text" name="firstName" className="input" value={profile.firstName || ''} onChange={handleProfileChange} required />
                  </div>
                  <div style={{ flex: 1 }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Last Name</label>
                    <input type="text" name="lastName" className="input" value={profile.lastName || ''} onChange={handleProfileChange} required />
                  </div>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Email Address</label>
                  <input type="email" className="input" value={profile.email || ''} disabled style={{ opacity: 0.7, cursor: 'not-allowed' }} />
                  <small style={{ color: 'var(--secondary-hover)' }}>Email address cannot be changed.</small>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Role</label>
                  <input type="text" className="input" value={profile.role || ''} disabled style={{ opacity: 0.7, cursor: 'not-allowed' }} />
                </div>
                <button type="submit" className="btn btn-primary" style={{ marginTop: '1rem' }} disabled={saving}>
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
              </form>
            </div>

            {/* Change Password */}
            <div className="glass card">
              <h2 style={{ margin: '0 0 1.5rem 0', fontSize: '1.25rem' }}>Change Password</h2>
              
              {pwdMessage.text && (
                <div style={{ 
                  padding: '0.75rem', marginBottom: '1rem', borderRadius: 'var(--radius)',
                  background: pwdMessage.type === 'success' ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                  color: pwdMessage.type === 'success' ? 'var(--success)' : 'var(--danger)'
                }}>
                  {pwdMessage.text}
                </div>
              )}
              
              <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Current Password</label>
                  <input type="password" name="currentPassword" className="input" value={passwords.currentPassword} onChange={handlePasswordChange} required />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>New Password</label>
                  <input type="password" name="newPassword" className="input" value={passwords.newPassword} onChange={handlePasswordChange} required />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Confirm New Password</label>
                  <input type="password" name="confirmPassword" className="input" value={passwords.confirmPassword} onChange={handlePasswordChange} required />
                </div>
                <button type="submit" className="btn btn-primary" style={{ marginTop: '1rem' }} disabled={pwdSaving}>
                  {pwdSaving ? 'Updating...' : 'Update Password'}
                </button>
              </form>
            </div>

          </div>
        )}
      </Layout>
    </ProtectedRoute>
  );
}
