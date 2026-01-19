const KEYCLOAK_URL = 'http://localhost:8085/realms/one-citations/protocol/openid-connect/token';
const CLIENT_ID = 'test-client';

export const auth = {
    async login(username, password) {
        const body = new URLSearchParams();
        body.append('client_id', CLIENT_ID);
        body.append('grant_type', 'password');
        body.append('username', username);
        body.append('password', password);

        const response = await fetch(KEYCLOAK_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: body
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        const data = await response.json();
        localStorage.setItem('access_token', data.access_token);
        localStorage.setItem('username', username);
        return data.access_token;
    },

    logout() {
        localStorage.removeItem('access_token');
        localStorage.removeItem('username');
    },

    getToken() {
        return localStorage.getItem('access_token');
    },

    getUsername() {
        return localStorage.getItem('username');
    },

    isAuthenticated() {
        return !!this.getToken();
    }
};
