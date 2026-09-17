import { useState } from 'react';
import { useRouter } from 'next/router';
import { requestReport } from '../lib/api';

export default function RequestReportButton({ propertyId }) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleRequest = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await requestReport(propertyId);
      // Redirect to the report polling page
      router.push(`/reports/${response.data.id}`);
    } catch (err) {
      setError('Failed to request report.');
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
      <button 
        className="btn btn-primary" 
        onClick={handleRequest} 
        disabled={loading}
      >
        {loading ? 'Requesting...' : 'Generate Full Report'}
      </button>
      {error && <span style={{ color: 'var(--danger)', fontSize: '0.875rem', marginTop: '0.5rem' }}>{error}</span>}
    </div>
  );
}
