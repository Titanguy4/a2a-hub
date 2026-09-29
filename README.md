<p align="center">
  <img src="https://raw.githubusercontent.com/oscarbol09/a2a-hub/main/frontend/public/favicon.svg" alt="A2A-Hub Logo" width="96">
</p>

<h1 align="center">A2A-Hub</h1>

<p align="center">
  <strong>Decentralized Agent Discovery Registry & Orchestration Hub for the Agent2Agent (A2A) Protocol</strong>
</p>

<p align="center">
  <a href="https://github.com/oscarbol09/a2a-hub/actions/workflows/ci.yml"><img src="https://github.com/oscarbol09/a2a-hub/actions/workflows/ci.yml/badge.svg" alt="CI Pipeline"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License: Apache 2.0"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-3.4-6DB33F.svg" alt="Spring Boot 3.4"></a>
  <a href="https://www.oracle.com/java/technologies/downloads/#java21"><img src="https://img.shields.io/badge/Java-21_LTS-007396.svg" alt="Java 21 LTS"></a>
  <a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Vue-3.5-4FC08D.svg" alt="Vue 3.5"></a>
  <a href="https://github.com/a2aproject/A2A"><img src="https://img.shields.io/badge/Protocol-A2A_v1.0-orange.svg" alt="A2A Protocol v1.0"></a>
</p>

---

## Why This Exists

The [Agent2Agent (A2A) Protocol](https://github.com/a2aproject/A2A) standardizes how autonomous AI agents describe their capabilities and negotiate tasks. While client libraries handle peer-to-peer communication, multi-agent systems in enterprise environments require a central registry to:

1. **Resolve agent capabilities dynamically** instead of hardcoding service endpoints.
2. **Monitor availability and health** without coupling client agents to polling logic.
3. **Provide semantic discovery** so tasks phrased in plain language match the most suitable agent.

This project addresses the gap discussed in [a2aproject/a2a-java#683](https://github.com/a2aproject/a2a-java/issues/683) by providing an open-source, production-ready registry and web dashboard built on Java 21 and Spring Boot 3.4.

## System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                         A2A-Hub                              │
│                                                              │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────────┐  │
│  │  Vue 3 UI   │◄──│  WebSocket   │   │  REST API        │  │
│  │  (Vite)     │   │  /ws/agents  │   │  /api/v1/agents  │  │
│  └──────┬──────┘   └──────┬───────┘   └────────┬─────────┘  │
│         │                 │                    │             │
│  ┌──────▼─────────────────▼────────────────────▼──────────┐  │
│  │       Spring Boot 3.4 Runtime (Virtual Threads)         │  │
│  │  • AgentRegistryService    • AgentHealthMonitor        │  │
│  │  • AgentDiscoveryService   • TaskProxyService          │  │
│  └──────────────────────────┬──────────────────────────────┘  │
│                             │                                │
│  ┌──────────────────────────▼──────────────────────────────┐  │
│  │         PostgreSQL 16 + pgvector (HNSW Indexing)        │  │
│  └─────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
         │ HTTP / JSON-RPC / REST
         ▼
┌────────────────┐  ┌────────────────┐  ┌────────────────┐
│  Agent A       │  │  Agent B       │  │  Agent N       │
│  /.well-known/ │  │  /.well-known/ │  │  /.well-known/ │
│  agent-card... │  │  agent-card... │  │  agent-card... │
└────────────────┘  └────────────────┘  └────────────────┘
```

## Quickstart

### Prerequisites
- Docker & Docker Compose
- Java 21 JDK (optional, if running outside Docker)
- Node.js 20+ (optional, for frontend dev server)

### 1. Clone & Start Infrastructure
```bash
git clone https://github.com/oscarbol09/a2a-hub.git
cd a2a-hub

# Start PostgreSQL with pgvector extension
docker-compose up -d postgres
```

### 2. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
The API starts on `http://localhost:8080`. Flyway runs migrations automatically on boot.

### 3. Run the Web Dashboard
```bash
cd ../frontend
npm install
npm run dev
```
Open `http://localhost:5173` to access the registry UI.

## API Usage

### Register an Agent
Fetch and index an agent's `agent-card.json`:

```bash
curl -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{"url": "https://weather-agent.internal.net"}'
```

Response (`201 Created`):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "WeatherAgent",
  "description": "Provides real-time meteorological data and forecasts.",
  "url": "https://weather-agent.internal.net",
  "version": "1.0.0",
  "status": "HEALTHY",
  "authType": "NONE",
  "registeredAt": "2026-09-28T20:00:00Z"
}
```

### List Registered Agents
```bash
curl -s http://localhost:8080/api/v1/agents | jq .
```

## Engineering Decisions & Trade-offs

- **Virtual Threads over Reactive Streams:** We use Java 21 Virtual Threads (`spring.threads.virtual.enabled=true`) alongside synchronous `RestClient`. This avoids the debugging complexity and reactive virus of Project Reactor while sustaining thousands of concurrent outbound agent health probes.
- **PostgreSQL JSONB + pgvector:** Rather than running separate relational and vector databases, we store raw `AgentCard` payloads in PostgreSQL `JSONB` columns and index skill embeddings using `pgvector` with HNSW cosine distance operators.
- **SSRF Hardening:** The registry resolves and inspects agent hostnames to reject loopback addresses, private IP ranges (RFC 1918), and cloud metadata services (`169.254.169.254`) in production configurations.

## Known Limitations

- **Authentication:** Token rotation for private downstream agents is currently static (`Bearer` or `API_KEY`). Mutual TLS (mTLS) handshake support is tracked for milestone v0.4.
- **Event Streaming:** Task proxying currently buffers intermediate steps; full Server-Sent Events (SSE) streaming for long-running reasoning agents is implemented in Phase 4.

## Contributing

Pull requests are welcome. Please check [CONTRIBUTING.md](CONTRIBUTING.md) for branch naming conventions, commit formats, and test requirements before opening a PR.

## License

[Apache License 2.0](LICENSE). Free for personal and commercial use.