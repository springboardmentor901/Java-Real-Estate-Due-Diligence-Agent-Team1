import SectionWrapper from './SectionWrapper';
import { getValueHistory } from '../../lib/api';

export default function ValueHistoryChart({ propertyId }) {
  return (
    <SectionWrapper 
      title="Value History"
      fetchFn={getValueHistory}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          {Array.isArray(data) && data.length > 0 ? (
            <div style={{ display: 'flex', alignItems: 'flex-end', height: '150px', gap: '4px', marginTop: '1rem' }}>
              {/* Very basic CSS bar chart representation */}
              {data.map((point, i) => {
                const maxVal = Math.max(...data.map(d => d.value || 0));
                const heightPct = maxVal > 0 ? ((point.value || 0) / maxVal) * 100 : 0;
                return (
                  <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                    <div style={{ 
                      width: '100%', 
                      height: `${heightPct}%`, 
                      background: 'var(--primary)',
                      borderRadius: '4px 4px 0 0',
                      minHeight: '1px'
                    }}></div>
                    <span style={{ fontSize: '0.6rem', marginTop: '4px', color: 'var(--secondary-hover)' }}>{point.year}</span>
                  </div>
                );
              })}
            </div>
          ) : (
            <p style={{ color: 'var(--secondary-hover)' }}>No value history found.</p>
          )}
        </div>
      )}
    />
  );
}
