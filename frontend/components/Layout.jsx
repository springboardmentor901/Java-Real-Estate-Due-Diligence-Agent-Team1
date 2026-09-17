import Link from 'next/link';
import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import NotificationBell from './NotificationBell';

export default function Layout({ children }) {
  const router = useRouter();
  const [userRole, setUserRole] = useState(null);

  useEffect(() => {
    setUserRole(localStorage.getItem('userRole'));
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    router.push('/login');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <aside className="glass" style={{ width: '250px', padding: '1.5rem', display: 'flex', flexDirection: 'column', borderRadius: 0, borderTop: 'none', borderBottom: 'none', borderLeft: 'none' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ margin: 0, color: 'var(--primary)', fontSize: '1.25rem', fontWeight: 'bold' }}>Due Diligence Agent</h2>
        </div>
        
        <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <Link href="/" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>Dashboard</Link>
          <Link href="/user/properties" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>Properties</Link>
          <Link href="/user/reports" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>Reports</Link>
          <Link href="/user/profile" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>Profile</Link>
          
          {userRole === 'ADMINISTRATOR' && (
            <Link href="/admin/dashboard" className="btn btn-secondary" style={{ justifyContent: 'flex-start', marginTop: '1rem', borderColor: 'var(--primary)' }}>Admin Dashboard</Link>
          )}
        </nav>
        
        <div style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button onClick={handleLogout} className="btn" style={{ background: 'transparent', color: 'var(--danger)', border: '1px solid var(--danger)' }}>
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <header className="glass" style={{ padding: '1rem 2rem', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', borderRadius: 0, borderTop: 'none', borderRight: 'none', borderLeft: 'none', position: 'sticky', top: 0, zIndex: 10 }}>
          <NotificationBell />
        </header>

        {/* Page Content */}
        <div style={{ padding: '2rem', flex: 1, overflowY: 'auto' }}>
          {children}
        </div>
      </main>
    </div>
  );
}
