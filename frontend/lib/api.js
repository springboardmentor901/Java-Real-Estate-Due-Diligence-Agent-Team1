import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
});

// Request interceptor to add JWT
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401s
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('userRole');
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Auth
export const register = (data) => api.post('/auth/register', data);
export const login = (data) => api.post('/auth/login', data);

// Properties
export const getProperties = () => api.get('/properties');
export const getPropertyById = (id) => api.get(`/properties/${id}`);
export const geocodeAddress = (address) => api.get(`/properties/geocode?address=${encodeURIComponent(address)}`);
export const createProperty = (data) => api.post('/properties', data);
export const getPropertyDetails = (id) => api.get(`/properties/${id}/details`);
export const getBasicProfile = (id) => api.get(`/properties/${id}/basic-profile`);

// Due Diligence
export const getDueDiligence = (id) => api.get(`/properties/${id}/due-diligence`);
export const getOwnership = (id) => api.get(`/properties/${id}/ownership`);
export const getTaxHistory = (id) => api.get(`/properties/${id}/tax-history`);
export const getZoning = (id) => api.get(`/properties/${id}/zoning`);
export const getFloodZone = (id) => api.get(`/properties/${id}/flood-zone`);
export const getPermits = (id) => api.get(`/properties/${id}/permits`);
export const getEnvironmental = (id) => api.get(`/properties/${id}/environmental`);
export const getComparables = (id) => api.get(`/properties/${id}/comparables`);
export const getComparableTrends = (id) => api.get(`/properties/${id}/comparables/trends`);
export const getValueHistory = (id) => api.get(`/properties/${id}/value-history`);
export const getPropertyHistory = (id) => api.get(`/properties/${id}/history`);
export const getUtilities = (propertyId) => api.get(`/properties/${propertyId}/utilities`);
export const createUtility = (propertyId, data) => api.post(`/properties/${propertyId}/utilities`, data);
export const updateUtility = (propertyId, utilityId, data) => api.put(`/properties/${propertyId}/utilities/${utilityId}`, data);
export const deleteUtility = (propertyId, utilityId) => api.delete(`/properties/${propertyId}/utilities/${utilityId}`);

// Risk Assessment
export const triggerRiskAssessment = (id) => api.post(`/properties/${id}/risk-assessment`);
export const triggerOverallRiskAssessment = (id) => api.post(`/properties/${id}/risk-assessment/overall`);

// Reports
export const requestReport = (propertyId) => api.post(`/properties/${propertyId}/reports`);
export const getReportStatus = (reportId) => api.get(`/reports/${reportId}`); // Correct endpoint from prompt: GET /api/reports/{id}
export const getReportHistory = (userId) => api.get(`/reports?user=${userId}`);
export const downloadReportPdf = (propertyId, reportId) => 
  api.get(`/properties/${propertyId}/reports/${reportId}/pdf`, { responseType: 'blob' });
export const downloadReportExcel = (propertyId, reportId) => 
  api.get(`/properties/${propertyId}/reports/${reportId}/excel`, { responseType: 'blob' });

// Profile
export const getProfile = () => api.get('/users/profile');
export const updateProfile = (data) => api.put('/users/profile', data);
export const updatePassword = (data) => api.put('/users/profile/password', data);

// Stubs (Known Gaps)
export const getNotifications = async (userId) => {
  console.warn('Backend endpoint for notifications not yet implemented.');
  return { data: [
    { id: 1, message: 'Welcome to Due Diligence Agent!', isRead: false },
    { id: 2, message: 'Report #123 has completed.', isRead: true }
  ] };
};

export const getAdminSummary = async () => {
  console.warn('Backend endpoint for admin summary not yet implemented.');
  return { data: { totalUsers: 150, reportsGenerated: 340, activeProperties: 25 } };
};

export const getAuditLogs = async (filters) => {
  console.warn('Backend endpoint for audit logs not yet implemented.');
  return { data: [
    { id: 1, action: 'LOGIN', user: 'admin@test.com', timestamp: new Date().toISOString() },
    { id: 2, action: 'REPORT_REQUESTED', user: 'buyer@test.com', timestamp: new Date().toISOString() }
  ] };
};

export default api;
