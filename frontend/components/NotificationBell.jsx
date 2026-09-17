import { useState, useEffect } from 'react';
import { getNotifications } from '../lib/api';

export default function NotificationBell() {
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    // We stub this out with a mock fetch as requested
    const fetchNotifications = async () => {
      try {
        const userId = localStorage.getItem('userId') || 1; // Fallback
        const response = await getNotifications(userId);
        setNotifications(response.data);
      } catch (error) {
        console.error('Failed to fetch notifications', error);
      }
    };
    fetchNotifications();
  }, []);

  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <div style={{ position: 'relative' }}>
      <button 
        onClick={() => setIsOpen(!isOpen)}
        style={{ 
          background: 'transparent', 
          border: 'none', 
          cursor: 'pointer', 
          fontSize: '1.5rem',
          position: 'relative'
        }}
      >
        🔔
        {unreadCount > 0 && (
          <span style={{
            position: 'absolute',
            top: '-5px',
            right: '-5px',
            background: 'var(--danger)',
            color: 'white',
            borderRadius: '50%',
            width: '18px',
            height: '18px',
            fontSize: '0.75rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 'bold'
          }}>
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="glass" style={{
          position: 'absolute',
          top: '100%',
          right: 0,
          width: '300px',
          marginTop: '0.5rem',
          padding: '1rem',
          zIndex: 50
        }}>
          <h3 style={{ margin: '0 0 1rem 0', fontSize: '1rem' }}>Notifications</h3>
          {notifications.length === 0 ? (
            <p style={{ margin: 0, color: 'var(--secondary-hover)' }}>No new notifications.</p>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {notifications.map(n => (
                <li key={n.id} style={{ 
                  padding: '0.75rem', 
                  borderRadius: 'var(--radius)', 
                  background: n.isRead ? 'transparent' : 'rgba(59, 130, 246, 0.1)',
                  border: n.isRead ? '1px solid var(--secondary)' : '1px solid var(--primary)'
                }}>
                  <p style={{ margin: 0, fontSize: '0.875rem' }}>{n.message}</p>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
