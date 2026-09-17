import { useState, useEffect } from 'react';
import { getAuditLogs } from '../lib/api';

export default function AuditLogTable() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const response = await getAuditLogs();
        setLogs(response.data);
      } catch (err) {
        console.error("Failed to fetch audit logs", err);
      } finally {
        setLoading(false);
      }
    };
    fetchLogs();
  }, []);

  if (loading) return <p style={{ color: 'var(--secondary-hover)' }}>Loading logs...</p>;
  
  if (logs.length === 0) return <p style={{ color: 'var(--secondary-hover)' }}>No logs found.</p>;

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--secondary)' }}>
            <th style={{ padding: '0.75rem 0.5rem' }}>ID</th>
            <th style={{ padding: '0.75rem 0.5rem' }}>Timestamp</th>
            <th style={{ padding: '0.75rem 0.5rem' }}>Action</th>
            <th style={{ padding: '0.75rem 0.5rem' }}>User</th>
          </tr>
        </thead>
        <tbody>
          {logs.map(log => (
            <tr key={log.id} style={{ borderBottom: '1px solid var(--surface-border)' }}>
              <td style={{ padding: '0.75rem 0.5rem' }}>{log.id}</td>
              <td style={{ padding: '0.75rem 0.5rem' }}>{new Date(log.timestamp).toLocaleString()}</td>
              <td style={{ padding: '0.75rem 0.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{log.action}</td>
              <td style={{ padding: '0.75rem 0.5rem' }}>{log.user}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
