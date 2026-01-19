const API_BASE_URL = 'http://localhost:8000/api/v1';
const API_KEY = 'one-citations-api-key'; // Hardcoded for this demo

export const api = {
    async request(endpoint, method = 'GET', body = null, token = null) {
        const headers = {
            'apikey': API_KEY,
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            method,
            headers
        };

        if (body) {
            config.body = JSON.stringify(body);
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
        
        // Handle 204 No Content or simple OK
        if (response.status === 204) return null;
        
        // Handle Errors
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`API Error ${response.status}: ${errorText}`);
        }

        // Try parsing JSON
        try {
            return await response.json();
        } catch (e) {
            return null; // or text
        }
    },

    getRandomImage() {
        return this.request('/images/random?width=300&height=300');
    },

    getRandomCitation() {
        return this.request('/citations/random');
    },

    submitCitation(text, token) {
        return this.request('/citations', 'POST', { text }, token);
    },

    getPendingCitations(token) {
        return this.request('/citations/pending', 'GET', null, token);
    },

    validateCitation(id, token) {
        return this.request(`/citations/${id}/validate`, 'PUT', null, token);
    }
};
