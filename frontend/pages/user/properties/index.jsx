import { useState, useEffect } from 'react';
import ProtectedRoute from '../../../../components/ProtectedRoute';
import Layout from '../../../../components/Layout';
import { getProperties } from '../../../../lib/api';
import Link from 'next/link';

export default function PropertiesList() {
  const [properties, setProperties] = useState([]);
  const [filteredProperties, setFilteredProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Client-side pagination & filtering state
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;
  const [filterType, setFilterType] = useState('');
  
  useEffect(() => {
    const fetchProperties = async () => {
      try {
        const response = await getProperties();
        setProperties(response.data);
        setFilteredProperties(response.data);
      } catch (err) {
        setError('Failed to fetch properties.');
      } finally {
        setLoading(false);
      }
    };
    fetchProperties();
  }, []);

  useEffect(() => {
    let result = properties;
    if (filterType) {
      result = result.filter(p => p.propertyType === filterType);
    }
    setFilteredProperties(result);
    setCurrentPage(1);
  }, [filterType, properties]);

  const totalPages = Math.ceil(filteredProperties.length / itemsPerPage);
  const paginatedProperties = filteredProperties.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  return (
    <ProtectedRoute>
      <Layout>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <h1 style={{ margin: 0 }}>Properties</h1>
          <Link href="/user/properties/new" className="btn btn-primary">Add Property</Link>
        </div>

        {error && <div style={{ color: 'var(--danger)', marginBottom: '1rem' }}>{error}</div>}

        <div className="glass" style={{ padding: '1rem', marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Property Type</label>
            <select className="input" value={filterType} onChange={(e) => setFilterType(e.target.value)} style={{ minWidth: '200px' }}>
              <option value="">All Types</option>
              <option value="SINGLE_FAMILY">Single Family</option>
              <option value="MULTI_FAMILY">Multi Family</option>
              <option value="COMMERCIAL">Commercial</option>
              <option value="LAND">Land</option>
            </select>
          </div>
        </div>

        {loading ? (
          <p>Loading properties...</p>
        ) : (
          <>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
              {paginatedProperties.map(property => (
                <Link href={`/user/properties/${property.id}`} key={property.id} style={{ textDecoration: 'none' }}>
                  <div className="glass card" style={{ transition: 'transform 0.2s', cursor: 'pointer' }}>
                    <div style={{ background: 'var(--secondary)', height: '150px', borderRadius: 'var(--radius)', marginBottom: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--secondary-hover)' }}>
                      No Image
                    </div>
                    <h3 style={{ margin: '0 0 0.5rem 0' }}>{property.address}</h3>
                    <p style={{ margin: 0, color: 'var(--secondary-hover)', fontSize: '0.875rem' }}>
                      {property.propertyType} • {property.bedrooms} beds • {property.bathrooms} baths
                    </p>
                    <p style={{ margin: '0.5rem 0 0 0', fontWeight: 'bold' }}>{property.squareFeet} sqft • Built {property.yearBuilt}</p>
                  </div>
                </Link>
              ))}
            </div>

            {totalPages > 1 && (
              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '2rem' }}>
                <button 
                  className="btn btn-secondary" 
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage(p => p - 1)}
                >
                  Previous
                </button>
                <span style={{ display: 'flex', alignItems: 'center', padding: '0 1rem' }}>
                  Page {currentPage} of {totalPages}
                </span>
                <button 
                  className="btn btn-secondary" 
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage(p => p + 1)}
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </Layout>
    </ProtectedRoute>
  );
}
