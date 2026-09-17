import SectionWrapper from './SectionWrapper';
import { getOwnership } from '../../lib/api';

export default function OwnershipSection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Ownership Details"
      fetchFn={getOwnership}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          <p><strong>Owner:</strong> {data.ownerName || 'Unknown'}</p>
          <p><strong>Status:</strong> {data.ownershipStatus || 'N/A'}</p>
          <p><strong>Last Transfer:</strong> {data.lastTransferDate ? new Date(data.lastTransferDate).toLocaleDateString() : 'N/A'}</p>
          <p><strong>Title Clear:</strong> {data.isTitleClear ? 'Yes ✅' : 'No ❌'}</p>
        </div>
      )}
    />
  );
}
