const axios = require('axios');

async function runTest() {
    try {
        const email = 'testuser_' + Date.now() + '@example.com';
        const api = axios.create({ baseURL: 'http://localhost:8080/api' });
        
        console.log('1. Registering new user...');
        await api.post('/auth/register', {
            fullName: 'Test User',
            email: email,
            password: 'Password123!',
            role: 'BUYER'
        });

        console.log('2. Logging in...');
        const loginRes = await api.post('/auth/login', {
            email: email,
            password: 'Password123!'
        });
        const token = loginRes.data.token;
        api.defaults.headers.common['Authorization'] = 'Bearer ' + token;
        const userId = loginRes.data.id;
        
        console.log('3. Fetching properties...');
        const propsRes = await api.get('/properties');
        let propertyId;
        if (propsRes.data.length === 0) {
            console.log('No properties found, creating one...');
            const createPropRes = await api.post('/properties', {
                address: '123 Test St',
                propertyType: 'SINGLE_FAMILY',
                bedrooms: 3,
                bathrooms: 2,
                squareFeet: 1500,
                yearBuilt: 2000
            });
            propertyId = createPropRes.data.id;
        } else {
            propertyId = propsRes.data[0].id;
        }
        console.log('Using property ID: ' + propertyId);

        console.log('4. Requesting report...');
        const reportRes = await api.post('/properties/' + propertyId + '/reports');
        const reportId = reportRes.data.id;
        console.log('Report generated with ID: ' + reportId);
        
        console.log('5. Checking report status...');
        const checkRes = await api.get('/users/' + userId + '/reports');
        console.log(checkRes.data);

        console.log('Test completed successfully!');

    } catch (error) {
        console.error('Test failed!');
        if (error.response) {
            console.error(error.response.status, error.response.data);
        } else {
            console.error(error.message);
        }
    }
}

runTest();
