import { useState, useEffect } from 'react';
import { getReportHistory } from '../../lib/api';

export default function RiskDashboard({ propertyId }) {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!propertyId) return;

    const fetchRisk = async () => {
      try {
        const userId = localStorage.getItem('userId') || 1;
        const response = await getReportHistory(userId);
        // Find the latest completed report for this property
        const propertyReports = response.data.filter(r => String(r.property?.id) === String(propertyId) && r.status === 'COMPLETED');
        if (propertyReports.length > 0) {
          // Sort by date descending
          propertyReports.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
          setReport(propertyReports[0]);
        }
      } catch (err) {
        console.error("Failed to fetch risk reports", err);
      } finally {
        setLoading(false);
      }
    };
    fetchRisk();
  }, [propertyId]);

  return (
    <div className="glass card" style={{ display: 'flex', flexDirection: 'column', background: 'rgba(59, 130, 246, 0.05)' }}>
      <h3 style={{ margin: '0 0 1rem 0', color: 'var(--primary)', borderBottom: '1px solid var(--surface-border)', paddingBottom: '0.5rem' }}>
        Overall Risk Assessment
      </h3>
      
      {loading ? (
        <p style={{ color: 'var(--secondary-hover)' }}>Checking for existing reports...</p>
      ) : report ? (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1 }}>
          <div style={{ 
            fontSize: '3rem', 
            fontWeight: 'bold', 
            color: report.riskScore > 75 ? 'var(--danger)' : report.riskScore > 40 ? 'var(--warning)' : 'var(--success)'
          }}>
            {report.riskScore}
          </div>
          <p style={{ margin: '0 0 1rem 0', color: 'var(--secondary-hover)' }}>Overall Risk Score (0-100)</p>
          <p style={{ margin: 0, textAlign: 'center', fontSize: '0.875rem' }}>{report.executiveSummary}</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flex: 1, color: 'var(--secondary-hover)', textAlign: 'center' }}>
          <p>No completed reports found for this property.</p>
          <p style={{ fontSize: '0.875rem' }}>Generate a full report to view the risk assessment.</p>
        </div>
      )}
    </div>
  );
}
