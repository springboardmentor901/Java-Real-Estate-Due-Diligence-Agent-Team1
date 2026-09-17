import SectionWrapper from './SectionWrapper';
import { getEnvironmental } from '../../lib/api';

export default function EnvironmentalSection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Environmental Risks"
      fetchFn={getEnvironmental}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          <p><strong>Contamination Risk:</strong> {data.contaminationRisk || 'N/A'}</p>
          <p><strong>Proximity to Superfund:</strong> {data.superfundProximity || 'N/A'} miles</p>
          <p><strong>Radon Level:</strong> {data.radonLevel || 'N/A'} pCi/L</p>
          <p>
            <strong>Overall Status:</strong> 
            <span style={{ 
              marginLeft: '0.5rem',
              fontWeight: 'bold',
              color: data.status === 'CLEAR' ? 'var(--success)' : 'var(--danger)'
            }}>
              {data.status || 'UNKNOWN'}
            </span>
          </p>
        </div>
      )}
    />
  );
}
