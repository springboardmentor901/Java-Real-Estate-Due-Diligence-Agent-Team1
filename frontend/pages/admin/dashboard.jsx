import { useState, useEffect } from 'react';
import ProtectedRoute from '../../components/ProtectedRoute';
import Layout from '../../components/Layout';
import AuditLogTable from '../../components/AuditLogTable';
import { getAdminSummary } from '../../lib/api';

export default function AdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const response = await getAdminSummary();
        setSummary(response.data);
      } catch (err) {
        console.error("Failed to load admin summary", err);
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, []);

  return (
    <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
      <Layout>
        <h1 style={{ marginBottom: '2rem' }}>Admin Dashboard</h1>
        
        {loading ? (
          <p>Loading metrics...</p>
        ) : summary ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '3rem' }}>
            <div className="glass card" style={{ textAlign: 'center' }}>
              <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--secondary-hover)' }}>Total Users</h3>
              <p style={{ margin: 0, fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{summary.totalUsers}</p>
            </div>
            <div className="glass card" style={{ textAlign: 'center' }}>
              <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--secondary-hover)' }}>Reports Generated</h3>
              <p style={{ margin: 0, fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{summary.reportsGenerated}</p>
            </div>
            <div className="glass card" style={{ textAlign: 'center' }}>
              <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--secondary-hover)' }}>Active Properties</h3>
              <p style={{ margin: 0, fontSize: '2.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{summary.activeProperties}</p>
            </div>
          </div>
        ) : (
          <p style={{ color: 'var(--warning)' }}>Could not load metrics.</p>
        )}

        <div className="glass card">
          <h2 style={{ margin: '0 0 1.5rem 0' }}>Audit Logs</h2>
          <AuditLogTable />
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
