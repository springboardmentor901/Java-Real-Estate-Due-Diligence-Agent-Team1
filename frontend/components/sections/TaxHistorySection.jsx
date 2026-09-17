import SectionWrapper from './SectionWrapper';
import { getTaxHistory } from '../../lib/api';

export default function TaxHistorySection({ propertyId }) {
  return (
    <SectionWrapper 
      title="Tax History"
      fetchFn={getTaxHistory}
      propertyId={propertyId}
      renderContent={(data) => (
        <div>
          {Array.isArray(data) && data.length > 0 ? (
            <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--secondary)' }}>
                  <th style={{ padding: '0.5rem 0' }}>Year</th>
                  <th style={{ padding: '0.5rem 0' }}>Amount</th>
                  <th style={{ padding: '0.5rem 0' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {data.slice(0, 5).map((tax, i) => (
                  <tr key={i} style={{ borderBottom: '1px solid var(--surface-border)' }}>
                    <td style={{ padding: '0.5rem 0' }}>{tax.year}</td>
                    <td style={{ padding: '0.5rem 0' }}>${tax.amount?.toLocaleString()}</td>
                    <td style={{ padding: '0.5rem 0' }}>
                      <span style={{ 
                        color: tax.status === 'PAID' ? 'var(--success)' : 'var(--warning)',
                        fontWeight: 'bold', fontSize: '0.875rem'
                      }}>
                        {tax.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p style={{ color: 'var(--secondary-hover)' }}>No tax history found.</p>
          )}
        </div>
      )}
    />
  );
}
