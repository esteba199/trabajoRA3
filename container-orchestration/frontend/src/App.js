import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [backendData, setBackendData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Call the relative path /api/status. Nginx handles proxying to http://backend:8080/api/status
    fetch('/api/status')
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        setBackendData(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching API data: ", err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-badge">2º DAW - Despliegue de Aplicaciones Web</div>
        <h1>Optimized Container Orchestration</h1>
        <p className="subtitle">Production-Ready Multi-Tier Container Architecture Integration</p>
      </header>

      <main className="dashboard">
        <section className="architecture-status">
          <h2>System Tier Status</h2>
          
          <div className="status-grid">
            {/* Tier 1: Reverse Proxy (Nginx) */}
            <div className="status-card active">
              <div className="card-header">
                <span className="badge badge-proxy">ENTRY POINT</span>
                <h3>Nginx Reverse Proxy</h3>
              </div>
              <div className="card-body">
                <p className="status-indicator online">ONLINE</p>
                <p className="desc">Acting as the single public entry point on port <strong>80</strong>. Routing frontend and backend traffic securely.</p>
              </div>
            </div>

            {/* Tier 2: Frontend (React) */}
            <div className="status-card active">
              <div className="card-header">
                <span className="badge badge-ui">PRESENTATION</span>
                <h3>React Frontend</h3>
              </div>
              <div className="card-body">
                <p className="status-indicator online">ONLINE</p>
                <p className="desc">Served by a hardened, lightweight non-root <strong>Nginx-alpine</strong> service running inside the frontend container.</p>
              </div>
            </div>

            {/* Tier 3: Backend (Spring Boot) */}
            <div className="status-card-api status-card active">
              <div className="card-header">
                <span className="badge badge-api">LOGIC TIER</span>
                <h3>Spring Boot API</h3>
              </div>
              <div className="card-body">
                {loading ? (
                  <p className="status-indicator checking">CHECKING...</p>
                ) : error ? (
                  <>
                    <p className="status-indicator offline">OFFLINE</p>
                    <p className="desc error-msg">Failed to connect to API: <code>{error}</code></p>
                  </>
                ) : (
                  <>
                    <p className="status-indicator online">ONLINE</p>
                    <p className="desc">
                      Spring Boot endpoint <code>/api/status</code> responded successfully. 
                      Version: <strong>{backendData?.version || '1.0.0'}</strong>
                    </p>
                  </>
                )}
              </div>
            </div>

            {/* Tier 4: Database (MySQL) */}
            <div className="status-card active">
              <div className="card-header">
                <span className="badge badge-db">PERSISTENCE</span>
                <h3>MySQL Database</h3>
              </div>
              <div className="card-body">
                {loading ? (
                  <p className="status-indicator checking">CHECKING...</p>
                ) : error ? (
                  <>
                    <p className="status-indicator offline">UNKNOWN</p>
                    <p className="desc">Awaiting Backend availability to verify connection status.</p>
                  </>
                ) : backendData?.databaseConnected ? (
                  <>
                    <p className="status-indicator online">CONNECTED</p>
                    <p className="desc">
                      <strong>{backendData?.dbName || 'MySQL'}</strong> is healthy and seeding tables. Persistence mounted on volume <code>mysql-data</code>.
                    </p>
                  </>
                ) : (
                  <>
                    <p className="status-indicator offline">ERROR</p>
                    <p className="desc error-msg">Backend reports connection to database failed.</p>
                  </>
                )}
              </div>
            </div>
          </div>
        </section>

        {backendData && (
          <section className="db-verification-section">
            <h2>Data Verification from Database</h2>
            <div className="data-table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Service Name</th>
                    <th>Status</th>
                    <th>Database Response Message</th>
                    <th>Last Verified</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>{backendData.dbStatus?.serviceName}</td>
                    <td><span className="badge-online">{backendData.dbStatus?.status}</span></td>
                    <td>{backendData.dbStatus?.responseMessage}</td>
                    <td>{new Date(backendData.dbStatus?.lastVerified).toLocaleString()}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        )}
      </main>

      <footer className="app-footer">
        <p>Project: <strong>Optimized Container Orchestration for Web Applications</strong></p>
        <p>Multi-stage builds • Nginx Routing • Healthchecks • Non-Root Users • Network Isolation</p>
      </footer>
    </div>
  );
}

export default App;
