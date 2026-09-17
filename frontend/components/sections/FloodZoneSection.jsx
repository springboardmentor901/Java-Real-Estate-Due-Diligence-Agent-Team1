import SectionWrapper from './SectionWrapper';
import { getFloodZone } from '../../lib/api';

export default function FloodZoneSection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Flood Zone Assessment"
      fetchFn={getFloodZone}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          <p>
            <strong>Zone Code:</strong> 
            <span style={{ 
              marginLeft: '0.5rem', 
              color: data.riskLevel === 'HIGH' ? 'var(--danger)' : 'var(--foreground)'
            }}>
              {data.zoneCode || 'N/A'}
            </span>
          </p>
          <p><strong>Risk Level:</strong> {data.riskLevel || 'UNKNOWN'}</p>
          <p><strong>FEMA Map Panel:</strong> {data.femaMapPanel || 'N/A'}</p>
          <p><strong>Insurance Required:</strong> {data.insuranceRequired ? 'Yes ⚠️' : 'No ✅'}</p>
        </div>
      )}
    />
  );
}
