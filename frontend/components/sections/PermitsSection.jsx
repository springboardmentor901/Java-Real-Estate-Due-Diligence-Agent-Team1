import SectionWrapper from './SectionWrapper';
import { getPermits } from '../../lib/api';

export default function PermitsSection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Building Permits"
      fetchFn={getPermits}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          {Array.isArray(data) && data.length > 0 ? (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {data.slice(0, 5).map((permit, i) => (
                <li key={i} style={{ borderBottom: '1px solid var(--surface-border)', paddingBottom: '0.5rem' }}>
                  <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold' }}>{permit.permitNumber || 'N/A'} - {permit.type || 'Unknown'}</p>
                  <p style={{ margin: '0 0 0.25rem 0', fontSize: '0.875rem' }}>{permit.description}</p>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', color: 'var(--secondary-hover)' }}>
                    <span>{permit.issueDate ? new Date(permit.issueDate).toLocaleDateString() : 'No date'}</span>
                    <span style={{ 
                      color: permit.status === 'CLOSED' ? 'var(--success)' : 
                             permit.status === 'OPEN' ? 'var(--warning)' : 'inherit'
                    }}>
                      {permit.status}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p style={{ color: 'var(--secondary-hover)' }}>No building permits found.</p>
          )}
        </div>
      )}
    />
  );
}
