import { auth } from './auth.js';
import { api } from './api.js';

// DOM Elements
const loginBtn = document.getElementById('login-btn');
const logoutBtn = document.getElementById('logout-btn');
const loginSection = document.getElementById('login-section');
const publicSection = document.getElementById('public-section');
const writerSection = document.getElementById('writer-section');
const moderatorSection = document.getElementById('moderator-section');
const userDisplay = document.getElementById('user-display');
const loginForm = document.getElementById('login-form');
const cancelLoginBtn = document.getElementById('cancel-login');

// Initialization
function init() {
    updateUI();
    loadPublicData();
    setupEventListeners();
}

function updateUI() {
    const isLoggedIn = auth.isAuthenticated();
    const username = auth.getUsername();

    if (isLoggedIn) {
        loginBtn.style.display = 'none';
        logoutBtn.style.display = 'inline-block';
        userDisplay.textContent = `Hello, ${username}`;

        if (username === 'writer') {
            writerSection.classList.remove('hidden');
            moderatorSection.classList.add('hidden');
        } else if (username === 'moderator') {
            moderatorSection.classList.remove('hidden');
            writerSection.classList.add('hidden');
            loadPendingCitations();
        } else {
            // Admin or others?
            writerSection.classList.add('hidden');
            moderatorSection.classList.add('hidden');
        }
    } else {
        loginBtn.style.display = 'inline-block';
        logoutBtn.style.display = 'none';
        userDisplay.textContent = '';
        writerSection.classList.add('hidden');
        moderatorSection.classList.add('hidden');
    }
}

function setupEventListeners() {
    loginBtn.addEventListener('click', () => {
        loginSection.classList.remove('hidden');
        publicSection.classList.add('hidden'); // Optional: hide public while logging in
    });

    cancelLoginBtn.addEventListener('click', () => {
        loginSection.classList.add('hidden');
        publicSection.classList.remove('hidden');
    });

    logoutBtn.addEventListener('click', () => {
        auth.logout();
        updateUI();
    });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const user = document.getElementById('username').value;
        const pass = document.getElementById('password').value;
        try {
            await auth.login(user, pass);
            loginSection.classList.add('hidden');
            publicSection.classList.remove('hidden');
            updateUI();
        } catch (err) {
            alert('Login failed: ' + err.message);
        }
    });

    // Public Section Listeners
    document.getElementById('refresh-image').addEventListener('click', loadRandomImage);
    document.getElementById('refresh-citation').addEventListener('click', loadRandomCitation);

    // Writer Listeners
    document.getElementById('submit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const text = document.getElementById('citation-text').value;
        try {
            await api.submitCitation(text, auth.getToken());
            alert('Citation submitted!');
            document.getElementById('citation-text').value = '';
        } catch (err) {
            alert('Error submitting: ' + err.message);
        }
    });

    // Moderator Listeners
    document.getElementById('refresh-pending').addEventListener('click', loadPendingCitations);
}

async function loadPublicData() {
    loadRandomImage();
    loadRandomCitation();
}

async function loadRandomImage() {
    const container = document.getElementById('image-container');
    container.innerHTML = 'Loading...';
    try {
        const data = await api.getRandomImage();
        if (data && data.url) {
            // For this API (Images API) it returns a byte array if not handling JSON?
            // Wait, the API returns a JSON? 
            // Phase 4 said "Random Image Endpoint"
            // final_verification.ps1 checks for "image/jpeg".
            // Ah, it returns a raw image.
            // api.js handles raw image?
            // My api.js tries to parse JSON.
            // I need to fix api.js or handle it here.
            // If the response is an image, I should use the URL to fetch it as blob or just set src?
            // Actually, I can just set <img src="URL">.
            // The URL is http://localhost:8000/api/v1/images/random?width=300...
            // But I need the API Key header.
            // <img src> won't send the header.
            // Only way is fetch blob -> object URL.

            // Re-reading api.js: it does parse JSON.
            // If it fails, it returns null.
        }
        // Let's refactor this to fetch blob.
        const response = await fetch('http://localhost:8000/api/v1/images/random?width=300&height=300&apikey=one-citations-api-key', {
            method: 'GET'
        });
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        container.innerHTML = `<img src="${url}" alt="Random" style="max-width:100%"/>`;
    } catch (err) {
        container.textContent = 'Error loading image';
    }
}

async function loadRandomCitation() {
    const container = document.getElementById('citation-container');
    container.innerHTML = 'Loading...';
    try {
        const data = await api.getRandomCitation();
        if (data) {
            container.innerHTML = `<blockquote>"${data.text}"</blockquote><cite>- ${data.author || 'Unknown'}</cite>`;
        } else {
            container.textContent = 'No verified citations found.';
        }
    } catch (err) {
        container.textContent = 'Error loading citation';
    }
}

async function loadPendingCitations() {
    const tbody = document.getElementById('pending-body');
    tbody.innerHTML = '<tr><td colspan="4">Loading...</td></tr>';
    try {
        const data = await api.getPendingCitations(auth.getToken());
        // Handling Page object or List
        const items = data.content || data; // Spring Page vs List

        if (!items || items.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No pending citations</td></tr>';
            return;
        }

        tbody.innerHTML = items.map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.text}</td>
                <td>${c.submitterId || 'N/A'}</td>
                <td><button onclick="window.validateCitation('${c.id}')">Validate</button></td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="4">Error: ${err.message}</td></tr>`;
    }
}

// Expose validate for inline onclick
window.validateCitation = async (id) => {
    try {
        await api.validateCitation(id, auth.getToken());
        alert('Validated!');
        loadPendingCitations();
    } catch (err) {
        alert('Error: ' + err.message);
    }
};

init();
