import SectionWrapper from './SectionWrapper';
import { getPropertyHistory } from '../../lib/api';

export default function PropertyTimeline({ propertyId }) {
  return (
    <SectionWrapper 
      title="Property History Timeline"
      fetchFn={getPropertyHistory}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          {Array.isArray(data) && data.length > 0 ? (
            <div style={{ position: 'relative', borderLeft: '2px solid var(--secondary)', paddingLeft: '1.5rem', marginLeft: '1rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {data.map((event, i) => (
                <div key={i} style={{ position: 'relative' }}>
                  <div style={{
                    position: 'absolute',
                    left: '-1.9rem',
                    top: '0.25rem',
                    width: '12px',
                    height: '12px',
                    borderRadius: '50%',
                    background: 'var(--primary)',
                    border: '2px solid var(--background)'
                  }} />
                  <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold' }}>{event.eventType}</p>
                  <p style={{ margin: '0 0 0.25rem 0', fontSize: '0.875rem' }}>{event.description}</p>
                  <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--secondary-hover)' }}>
                    {event.eventDate ? new Date(event.eventDate).toLocaleDateString() : 'Unknown date'}
                  </p>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ color: 'var(--secondary-hover)' }}>No history events found.</p>
          )}
        </div>
      )}
    />
  );
}
