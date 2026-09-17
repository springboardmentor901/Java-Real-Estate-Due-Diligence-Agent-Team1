import SectionWrapper from './SectionWrapper';
import { getUtilities } from '../../lib/api';

export default function UtilitySection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Utility Information"
      fetchFn={getUtilities}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          {Array.isArray(data) && data.length > 0 ? (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {data.map((utility, i) => (
                <li key={i} style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--surface-border)', paddingBottom: '0.5rem' }}>
                  <span style={{ fontWeight: 'bold' }}>{utility.type}</span>
                  <span>{utility.provider || 'Unknown'} - {utility.status || 'N/A'}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p style={{ color: 'var(--secondary-hover)' }}>No utility information found.</p>
          )}
        </div>
      )}
    />
  );
}
