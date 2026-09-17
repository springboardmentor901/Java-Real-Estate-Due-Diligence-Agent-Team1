import { useState, useEffect } from 'react';
import { useRouter } from 'next/router';
import ProtectedRoute from '../../../components/ProtectedRoute';
import Layout from '../../../components/Layout';
import { getReportStatus, downloadReportPdf, downloadReportExcel } from '../../../lib/api';
import Link from 'next/link';

export default function ReportDetails() {
  const router = useRouter();
  const { id } = router.query;
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    if (!id) return;

    let intervalId;

    const fetchStatus = async () => {
      try {
        const response = await getReportStatus(id);
        const data = response.data;
        setReport(data);
        setLoading(false);

        // Stop polling if completed or failed
        if (data.status === 'COMPLETED' || data.status === 'FAILED') {
          if (intervalId) clearInterval(intervalId);
        }
      } catch (err) {
        setError('Failed to fetch report status.');
        if (intervalId) clearInterval(intervalId);
      }
    };

    fetchStatus();
    // Poll every 3 seconds
    intervalId = setInterval(fetchStatus, 3000);

    return () => clearInterval(intervalId);
  }, [id]);

  const handleDownload = async (type) => {
    if (!report || !report.property) return;
    setDownloading(true);
    try {
      const response = type === 'pdf' 
        ? await downloadReportPdf(report.property.id, id)
        : await downloadReportExcel(report.property.id, id);
        
      // Create a blob URL and trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `report-${id}.${type === 'pdf' ? 'pdf' : 'xlsx'}`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      alert('Failed to download file.');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '50vh' }}>
            <div className="loader" style={{ fontSize: '3rem', animation: 'spin 2s linear infinite' }}>⏳</div>
            <p style={{ marginTop: '1rem', fontSize: '1.25rem' }}>Loading report status...</p>
          </div>
        </Layout>
      </ProtectedRoute>
    );
  }

  if (error || !report) {
    return (
      <ProtectedRoute>
        <Layout>
          <p style={{ color: 'var(--danger)' }}>{error || 'Report not found'}</p>
        </Layout>
      </ProtectedRoute>
    );
  }

  const isPending = report.status === 'REQUESTED' || report.status === 'IN_PROGRESS';
  const isFailed = report.status === 'FAILED';

  return (
    <ProtectedRoute>
      <Layout>
        <div style={{ marginBottom: '2rem' }}>
          <Link href={`/properties/${report.property?.id}`} style={{ color: 'var(--primary)', textDecoration: 'underline' }}>
            &larr; Back to Property
          </Link>
        </div>

        <div className="glass card" style={{ maxWidth: '800px', margin: '0 auto' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
            <h1 style={{ margin: 0 }}>Report #{report.id}</h1>
            <span style={{ 
              padding: '0.5rem 1rem', 
              borderRadius: '20px',
              fontWeight: 'bold',
              background: report.status === 'COMPLETED' ? 'rgba(16, 185, 129, 0.1)' : 
                          isFailed ? 'rgba(239, 68, 68, 0.1)' : 'rgba(245, 158, 11, 0.1)',
              color: report.status === 'COMPLETED' ? 'var(--success)' : 
                     isFailed ? 'var(--danger)' : 'var(--warning)'
            }}>
              {report.status}
            </span>
          </div>

          <div style={{ marginBottom: '2rem' }}>
            <p><strong>Property:</strong> {report.property?.address}</p>
            <p><strong>Requested on:</strong> {new Date(report.createdAt).toLocaleString()}</p>
          </div>

          {isPending && (
            <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
              <div style={{ fontSize: '4rem', marginBottom: '1rem', animation: 'pulse 1.5s infinite' }}>⚙️</div>
              <h3>Generating your report...</h3>
              <p style={{ color: 'var(--secondary-hover)' }}>This process usually takes a few minutes. We're compiling ownership data, risk assessments, and comparables.</p>
            </div>
          )}

          {isFailed && (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--danger)' }}>
              <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>❌</div>
              <h3>Report Generation Failed</h3>
              <p>There was an error while compiling the due diligence data. Please try requesting a new report.</p>
            </div>
          )}

          {report.status === 'COMPLETED' && (
            <div>
              <div style={{ background: 'rgba(59, 130, 246, 0.05)', padding: '1.5rem', borderRadius: 'var(--radius)', marginBottom: '2rem' }}>
                <h3 style={{ margin: '0 0 1rem 0', color: 'var(--primary)' }}>Executive Summary</h3>
                <div style={{ display: 'flex', alignItems: 'center', gap: '2rem', marginBottom: '1.5rem' }}>
                  <div style={{ 
                    width: '80px', height: '80px', 
                    borderRadius: '50%', 
                    background: 'var(--surface)', 
                    border: `4px solid ${report.riskScore > 75 ? 'var(--danger)' : report.riskScore > 40 ? 'var(--warning)' : 'var(--success)'}`,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: '1.5rem', fontWeight: 'bold'
                  }}>
                    {report.riskScore}
                  </div>
                  <div style={{ flex: 1 }}>
                    <p style={{ margin: 0 }}><strong>Overall Risk Score (0-100)</strong></p>
                    <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.875rem', color: 'var(--secondary-hover)' }}>Higher score indicates greater risk.</p>
                  </div>
                </div>
                <p style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{report.executiveSummary || 'No executive summary provided.'}</p>
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <button 
                  className="btn btn-primary" 
                  style={{ flex: 1 }}
                  onClick={() => handleDownload('pdf')}
                  disabled={downloading || !report.pdfUrl}
                >
                  {downloading ? 'Downloading...' : '📄 Download PDF'}
                </button>
                <button 
                  className="btn btn-secondary" 
                  style={{ flex: 1 }}
                  onClick={() => handleDownload('excel')}
                  disabled={downloading || !report.excelUrl}
                >
                  {downloading ? 'Downloading...' : '📊 Download Excel'}
                </button>
              </div>
            </div>
          )}
        </div>
      </Layout>
    </ProtectedRoute>
  );
}
