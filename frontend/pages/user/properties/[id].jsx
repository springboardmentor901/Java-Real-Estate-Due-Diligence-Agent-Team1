import { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import ProtectedRoute from '../../../components/ProtectedRoute';
import Layout from '../../../components/Layout';
import { getPropertyById, getBasicProfile } from '../../../lib/api';
import RequestReportButton from '../../../components/RequestReportButton';
import OwnershipSection from '../../../components/sections/OwnershipSection';
import TaxHistorySection from '../../../components/sections/TaxHistorySection';
import ZoningSection from '../../../components/sections/ZoningSection';
import FloodZoneSection from '../../../components/sections/FloodZoneSection';
import PermitsSection from '../../../components/sections/PermitsSection';
import EnvironmentalSection from '../../../components/sections/EnvironmentalSection';
import UtilitySection from '../../../components/sections/UtilitySection';
import RiskDashboard from '../../../components/sections/RiskDashboard';
import ComparablesSection from '../../../components/sections/ComparablesSection';
import ValueHistoryChart from '../../../components/sections/ValueHistoryChart';
import PropertyTimeline from '../../../components/sections/PropertyTimeline';

export default function PropertyDetails() {
  const router = useRouter();
  const { id } = router.query;
  const [property, setProperty] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    
    const fetchProperty = async () => {
      try {
        const response = await getPropertyById(id);
        setProperty(response.data);
      } catch (err) {
        console.error("Failed to fetch property", err);
      } finally {
        setLoading(false);
      }
    };
    fetchProperty();
  }, [id]);

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <p>Loading property details...</p>
        </Layout>
      </ProtectedRoute>
    );
  }

  if (!property) {
    return (
      <ProtectedRoute>
        <Layout>
          <p style={{ color: 'var(--danger)' }}>Property not found.</p>
        </Layout>
      </ProtectedRoute>
    );
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem' }}>
          <div>
            <h1 style={{ margin: '0 0 0.5rem 0' }}>{property.address}</h1>
            <p style={{ margin: 0, color: 'var(--secondary-hover)' }}>
              {property.propertyType} • {property.bedrooms} beds • {property.bathrooms} baths • {property.squareFeet} sqft
            </p>
          </div>
          <RequestReportButton propertyId={id} />
        </div>

        {/* Dashboard Grid for Sections */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {/* Top Row: Basic Info & Risk */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <OwnershipSection propertyId={id} />
            <RiskDashboard propertyId={id} />
          </div>

          <PropertyTimeline propertyId={id} />
          
          {/* Middle Row: Due Diligence */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '2rem' }}>
            <TaxHistorySection propertyId={id} />
            <ZoningSection propertyId={id} />
            <FloodZoneSection propertyId={id} />
            <PermitsSection propertyId={id} />
            <EnvironmentalSection propertyId={id} />
            <UtilitySection propertyId={id} />
          </div>

          {/* Bottom Row: Financials */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <ComparablesSection propertyId={id} />
            <ValueHistoryChart propertyId={id} />
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
