import SectionWrapper from './SectionWrapper';
import { getComparables } from '../../lib/api';

export default function ComparablesSection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Comparable Properties"
      fetchFn={getComparables}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          {Array.isArray(data) && data.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {data.slice(0, 3).map((comp, i) => (
                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--surface-border)', paddingBottom: '0.5rem' }}>
                  <div>
                    <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold' }}>{comp.address}</p>
                    <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--secondary-hover)' }}>
                      {comp.bedrooms} bd • {comp.bathrooms} ba • {comp.squareFeet} sqft
                    </p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold', color: 'var(--success)' }}>${comp.price?.toLocaleString()}</p>
                    <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--secondary-hover)' }}>{comp.distance} miles away</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ color: 'var(--secondary-hover)' }}>No comparable properties found.</p>
          )}
        </div>
      )}
    />
  );
}
