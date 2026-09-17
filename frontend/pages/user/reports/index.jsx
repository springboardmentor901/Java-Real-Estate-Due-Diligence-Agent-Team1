import { useState, useEffect } from 'react';
import ProtectedRoute from '../../../../components/ProtectedRoute';
import Layout from '../../../../components/Layout';
import { getReportHistory } from '../../../../lib/api';
import Link from 'next/link';

export default function ReportsList() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchReports = async () => {
      try {
        const userId = localStorage.getItem('userId');
        if (!userId) throw new Error('User ID not found');
        
        const response = await getReportHistory(userId);
        
        // Sort reports by newest first
        const sorted = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setReports(sorted);
      } catch (err) {
        setError('Failed to fetch reports.');
      } finally {
        setLoading(false);
      }
    };
    fetchReports();
  }, []);

  return (
    <ProtectedRoute>
      <Layout>
        <h1 style={{ marginBottom: '2rem' }}>My Reports</h1>

        {error && <div style={{ color: 'var(--danger)', marginBottom: '1rem' }}>{error}</div>}

        {loading ? (
          <p>Loading reports...</p>
        ) : reports.length === 0 ? (
          <p style={{ color: 'var(--secondary-hover)' }}>You have no reports.</p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
            {reports.map(report => (
              <Link href={`/user/reports/${report.id}`} key={report.id} style={{ textDecoration: 'none' }}>
                <div className="glass card" style={{ transition: 'transform 0.2s', cursor: 'pointer' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                    <h3 style={{ margin: 0, fontSize: '1.125rem' }}>Report #{report.id}</h3>
                    <span style={{ 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '12px',
                      fontSize: '0.75rem',
                      fontWeight: 'bold',
                      background: report.status === 'COMPLETED' ? 'rgba(16, 185, 129, 0.1)' : 
                                  report.status === 'FAILED' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(245, 158, 11, 0.1)',
                      color: report.status === 'COMPLETED' ? 'var(--success)' : 
                             report.status === 'FAILED' ? 'var(--danger)' : 'var(--warning)'
                    }}>
                      {report.status}
                    </span>
                  </div>
                  
                  {report.property && (
                    <p style={{ margin: '0 0 0.5rem 0', fontWeight: 'bold' }}>{report.property.address}</p>
                  )}
                  
                  <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem', color: 'var(--secondary-hover)' }}>
                    Generated: {new Date(report.createdAt).toLocaleString()}
                  </p>

                  {report.status === 'COMPLETED' && (
                    <div style={{ marginTop: '1rem', borderTop: '1px solid var(--surface-border)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ fontSize: '0.875rem' }}>Risk Score: <strong>{report.riskScore}</strong></span>
                    </div>
                  )}
                </div>
              </Link>
            ))}
          </div>
        )}
      </Layout>
    </ProtectedRoute>
  );
}
