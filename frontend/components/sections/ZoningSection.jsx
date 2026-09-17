import SectionWrapper from './SectionWrapper';
import { getZoning } from '../../lib/api';

export default function ZoningSection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Zoning Information"
      fetchFn={getZoning}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          <p><strong>Code:</strong> {data.zoningCode || 'N/A'}</p>
          <p><strong>Description:</strong> {data.description || 'N/A'}</p>
          <p><strong>Permitted Uses:</strong> {data.permittedUses || 'N/A'}</p>
          <p><strong>Restrictions:</strong> {data.restrictions || 'None listed'}</p>
        </div>
      )}
    />
  );
}
