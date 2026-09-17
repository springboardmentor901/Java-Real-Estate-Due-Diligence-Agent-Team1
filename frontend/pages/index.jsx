import { useEffect, useState } from 'react';
import ProtectedRoute from '../components/ProtectedRoute';
import Layout from '../components/Layout';
import Link from 'next/link';
import { getProperties } from '../lib/api';

const DashboardCard = ({ title, value, icon, link }) => (
  <Link href={link || "#"} className="glass card" style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', transition: 'transform 0.2s', textDecoration: 'none' }}>
    <div style={{ fontSize: '2.5rem' }}>{icon}</div>
    <div>
      <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--secondary-hover)', fontSize: '1rem' }}>{title}</h3>
      <p style={{ margin: 0, fontSize: '1.75rem', fontWeight: 'bold' }}>{value}</p>
    </div>
  </Link>
);

const BuyerDashboard = () => (
  <div>
    <h2 style={{ marginBottom: '2rem' }}>Welcome, Buyer</h2>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
      <DashboardCard title="Saved Properties" value="3" icon="🏠" link="/user/properties" />
      <DashboardCard title="Active Reports" value="1" icon="📊" link="/user/reports" />
      <DashboardCard title="Completed Reports" value="2" icon="✅" link="/user/reports" />
    </div>
  </div>
);

const AgentDashboard = () => (
  <div>
    <h2 style={{ marginBottom: '2rem' }}>Welcome, Real Estate Agent</h2>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
      <DashboardCard title="My Listings" value="12" icon="🏢" link="/user/properties" />
      <DashboardCard title="Client Reports" value="5" icon="📑" link="/user/reports" />
    </div>
  </div>
);

const LegalDashboard = () => (
  <div>
    <h2 style={{ marginBottom: '2rem' }}>Welcome, Legal Reviewer</h2>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
      <DashboardCard title="Pending Reviews" value="7" icon="⚖️" link="/user/reports" />
      <DashboardCard title="High Risk Properties" value="2" icon="⚠️" link="/user/properties" />
    </div>
  </div>
);

const FinancialDashboard = () => (
  <div>
    <h2 style={{ marginBottom: '2rem' }}>Welcome, Financial Institution</h2>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem' }}>
      <DashboardCard title="Loan Appraisals" value="14" icon="💰" link="/user/reports" />
      <DashboardCard title="Risk Assessments" value="8" icon="📈" link="/user/reports" />
    </div>
  </div>
);

export default function Dashboard() {
  const [role, setRole] = useState(null);

  useEffect(() => {
    setRole(localStorage.getItem('userRole'));
  }, []);

  const renderDashboard = () => {
    switch (role) {
      case 'BUYER': return <BuyerDashboard />;
      case 'REAL_ESTATE_AGENT': return <AgentDashboard />;
      case 'LEGAL_REVIEWER': return <LegalDashboard />;
      case 'FINANCIAL_INSTITUTION': return <FinancialDashboard />;
      case 'ADMINISTRATOR': 
        return (
          <div>
            <h2 style={{ marginBottom: '2rem' }}>Welcome, Administrator</h2>
            <p>Please navigate to the <Link href="/admin/dashboard" style={{ color: 'var(--primary)' }}>Admin Dashboard</Link>.</p>
          </div>
        );
      default: return <p>Loading dashboard...</p>;
    }
  };

  return (
    <ProtectedRoute>
      <Layout>
        {renderDashboard()}
      </Layout>
    </ProtectedRoute>
  );
}
