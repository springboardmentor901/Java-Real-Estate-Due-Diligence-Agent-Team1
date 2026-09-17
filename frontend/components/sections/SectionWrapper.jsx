import { useState, useEffect } from 'react';

export default function SectionWrapper({ title, fetchFn, propertyId, renderContent }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!propertyId) return;

    const loadData = async () => {
      try {
        const response = await fetchFn(propertyId);
        setData(response.data);
      } catch (err) {
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [propertyId, fetchFn]);

  return (
    <div className="glass card" style={{ display: 'flex', flexDirection: 'column' }}>
      <h3 style={{ margin: '0 0 1rem 0', color: 'var(--primary)', borderBottom: '1px solid var(--surface-border)', paddingBottom: '0.5rem' }}>
        {title}
      </h3>
      <div style={{ flex: 1 }}>
        {loading && <p style={{ color: 'var(--secondary-hover)' }}>Loading...</p>}
        {error && <p style={{ color: 'var(--warning)', fontSize: '0.875rem' }}>Temporarily unavailable.</p>}
        {!loading && !error && data && renderContent(data)}
        {!loading && !error && !data && <p style={{ color: 'var(--secondary-hover)', fontSize: '0.875rem' }}>No data available.</p>}
      </div>
    </div>
  );
}
