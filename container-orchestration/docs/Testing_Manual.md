# Comprehensive Testing Manual
## Optimized Container Orchestration for Web Applications

This guide provides test routines to evaluate the network security boundaries, functional performance, database integration, and reliability of the container orchestration setup.

---

## 1. Functional Connectivity & Access Tests

Verify that only the designated HTTP port is publicly accessible.

### A. Verify Publicly Exposed Ports
Check active port bindings on the host machine:
```bash
docker ps
```
**Expected Output:**
```text
CONTAINER ID   IMAGE                          COMMAND                  PORTS                  NAMES
1a2b3c4d5e6f   daw-nginx:1.25.4-alpine        "nginx -g 'daemon of…"   0.0.0.0:80->80/tcp     daw-nginx-proxy
2b3c4d5e6f7a   daw-frontend-react:1.0.0       "nginx -g 'daemon of…"   8080/tcp               daw-frontend-app
3c4d5e6f7a8b   daw-backend-springboot:1.0.0   "java -jar app.jar"      8080/tcp               daw-backend-api
4d5e6f7a8b9c   mysql:8.0.36                   "docker-entrypoint.s…"   3306/tcp, 33060/tcp    daw-mysql-db
```
> [!IMPORTANT]
> Verify that the **PORTS** column lists `0.0.0.0:80->80/tcp` ONLY for the Nginx container (`daw-nginx-proxy`). The ports for other services (MySQL, Frontend, Backend) must NOT list host mappings, indicating they are hidden inside their respective Docker networks.

---

## 2. API & Frontend Integration Tests

Test the web interfaces and endpoints using command-line query tools.

### A. Query Backend REST API through Nginx
```bash
curl -i http://localhost/api/status
```
**Expected HTTP Headers and JSON Response:**
```http
HTTP/1.1 200 OK
Server: nginx/1.25.4
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin

{
  "version": "1.0.0",
  "framework": "Spring Boot 3.2.4",
  "databaseConnected": true,
  "dbName": "MySQL Database v8.0",
  "dbStatus": {
    "id": 1,
    "serviceName": "MySQL Database",
    "status": "HEALTHY",
    "responseMessage": "Database connection successfully established and initial seed data fetched.",
    "lastVerified": "2026-05-19T15:18:42"
  }
}
```

---

## 3. Network Isolation Validation Tests

Verify that security network boundaries are actively blocking unauthorized communication between containers.

### A. Verify Frontend cannot ping Database
The Frontend container lacks database-level access. Let's verify that a network ping request fails:
```bash
docker exec -it daw-frontend-app ping -c 3 mysql-db
```
**Expected Output:**
```text
ping: bad address 'mysql-db'
```
*(The DNS fails to resolve because the Frontend container is not on the `backend_net` bridge network)*

### B. Verify Backend can ping Database
The Backend is placed on the database network, so its lookup resolution must succeed:
```bash
docker exec -it daw-backend-api ping -c 3 mysql-db
```
**Expected Output:**
```text
PING mysql-db (172.20.0.2): 56 data bytes
64 bytes from 172.20.0.2: seq=0 ttl=64 time=0.065 ms
64 bytes from 172.20.0.2: seq=1 ttl=64 time=0.078 ms
64 bytes from 172.20.0.2: seq=2 ttl=64 time=0.072 ms

--- mysql-db ping statistics ---
3 packets transmitted, 3 packets received, 0% packet loss
```

---

## 4. Performance & Load Verification Tests

We execute load tests to evaluate server performance and resource allocation under heavy traffic.

### A. Load Testing with ApacheBench (ab)
Run ApacheBench to perform **1,000 HTTP requests** targeting the backend API status endpoint with a concurrency level of **50 parallel threads**:
```bash
ab -n 1000 -c 50 http://localhost/api/status
```

**Expected Output Metrics (Under load caps):**
```text
Server Software:        nginx/1.25.4
Server Hostname:        localhost
Server Port:            80

Document Path:          /api/status
Document Length:        348 bytes

Concurrency Level:      50
Time taken for tests:   1.428 seconds
Complete requests:      1000
Failed requests:        0
Total transferred:      535000 bytes
HTML transferred:       348000 bytes
Requests per second:    700.28 [#/sec] (mean)
Time per request:       71.400 [ms] (mean)
Time per request:       1.428 [ms] (mean, across all concurrent requests)
Transfer rate:          365.88 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    1   1.2      1       8
Processing:    12   68  18.4     65     142
Waiting:       10   65  17.9     62     138
Total:         13   69  18.5     66     145

Percentage of the requests served within a certain time (ms)
  50%     66ms
  66%     74ms
  75%     80ms
  90%     95ms
  95%    108ms
  99%    132ms
 100%    145ms (longest request)
```

During the test run, monitor limits by opening a separate terminal and running `docker stats`. You will see CPU limits throttle around **50.0%** usage for the Spring Boot container, protecting the host system from resource starvation.

---

## 5. Live Log Validation Procedures

Verify container logs to ensure database queries, health checks, and connection handshakes are functioning correctly:

### A. MySQL Startup and Seeding Logs:
```bash
docker logs daw-mysql-db
```
**Expected success indicators:**
```text
2026-05-19T15:17:40.485125Z 0 [System] [MY-010116] [Server] /usr/sbin/mysqld (mysqld 8.0.36) starting as process 1
2026-05-19T15:17:40.912384Z 0 [System] [MY-013576] [InnoDB] InnoDB initialization has started.
2026-05-19T15:17:42.582491Z 0 [System] [MY-013577] [InnoDB] InnoDB initialization has ended.
2026-05-19T15:17:43.210492Z 0 [System] [MY-011323] [Server] X Plugin ready for connections. Bind on [::]:33060
2026-05-19T15:17:43.512019Z 0 [System] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections. Version: '8.0.36' socket: '/var/run/mysqld/mysqld.sock' port: 3306  Source distribution.
/usr/local/bin/docker-entrypoint.sh: running /docker-entrypoint-initdb.d/init.sql
...
```

### B. Spring Boot Boot Handshake Logs:
```bash
docker logs daw-backend-api
```
**Expected success indicators:**
```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2026-05-19T15:17:55.852Z  INFO 1 --- [main] c.d.o.OrchestrationApplication           : Starting OrchestrationApplication v1.0.0 on daw-backend-api
2026-05-19T15:17:55.860Z  INFO 1 --- [main] c.d.o.OrchestrationApplication           : Active profiles: prod
2026-05-19T15:17:56.912Z  INFO 1 --- [main] o.h.e.t.j.p.i.JtaPlatformInitiator      : HHH000490: Using JtaPlatform
2026-05-19T15:17:57.102Z  INFO 1 --- [main] o.h.e.t.j.p.i.JtaPlatformInitiator      : Connection to MySQL Database Established successfully.
2026-05-19T15:17:58.210Z  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2026-05-19T15:17:58.450Z  INFO 1 --- [main] c.d.o.OrchestrationApplication           : Started OrchestrationApplication in 3.125 seconds (process running as user: spring)
```

### C. Nginx Reverse Proxy Log Verification:
```bash
docker logs daw-nginx-proxy
```
**Expected access routing traces:**
```text
172.20.0.1 - - [19/May/2026:15:18:02 +0000] "GET / HTTP/1.1" 200 421 "-" "Mozilla/5.0..."
172.20.0.1 - - [19/May/2026:15:18:03 +0000] "GET /static/css/main.css HTTP/1.1" 200 1205 "http://localhost/" "Mozilla/5.0..."
172.20.0.1 - - [19/May/2026:15:18:03 +0000] "GET /api/status HTTP/1.1" 200 348 "http://localhost/" "Mozilla/5.0..."
```
*(Confirms Nginx is receiving traffic on port 80 and routing `/` to Frontend and `/api/status` to Backend)*
