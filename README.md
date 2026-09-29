<p align="center">
  <img src="https://raw.githubusercontent.com/oscarbol09/a2a-hub/main/frontend/public/favicon.svg" alt="A2A-Hub Logo" width="120">
</p>

<h1 align="center">A2A-Hub</h1>

<p align="center">
  <em>Hub de descubrimiento y orquestación de agentes AI basado en el protocolo abierto Agent2Agent (A2A).</em>
</p>

<p align="center">
  <a href="https://github.com/oscarbol09/a2a-hub/actions"><img src="https://img.shields.io/badge/CI-Passing-brightgreen.svg" alt="CI Status"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-3.4-6DB33F.svg" alt="Spring Boot 3.4"></a>
  <a href="https://www.oracle.com/java/technologies/downloads/#java21"><img src="https://img.shields.io/badge/Java-21_LTS-007396.svg" alt="Java 21 LTS"></a>
  <a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Vue-3-4FC08D.svg" alt="Vue 3"></a>
  <a href="https://github.com/a2aproject/A2A"><img src="https://img.shields.io/badge/Protocol-A2A_v1.0-orange.svg" alt="A2A Protocol"></a>
</p>

---

## 🚀 Motivación

El protocolo **A2A (Agent2Agent)**, mantenido por la Linux Foundation y originalmente contribuido por Google, define cómo agentes de Inteligencia Artificial independientes pueden descubrirse, comunicarse y delegar tareas entre sí utilizando interfaces estandarizadas.

Aunque existen SDKs oficiales para interactuar punto a punto, **el ecosistema carecía de una capa de registro centralizada con interfaz gráfica** (identificado en el [Issue #683](https://github.com/a2aproject/a2a-java/issues/683)). **A2A-Hub** resuelve esto proporcionando un Registry y Discovery Hub *Open Source* construido en Java empresarial. Permite registrar agentes A2A, descubrirlos semánticamente por sus capacidades mediante LangChain4j y PostgreSQL (`pgvector`), monitorearlos en tiempo real y orquestar flujos multi-agente desde una interfaz intuitiva en Vue 3.

## ✨ Características Principales

- **Registro Estandarizado:** Consume y parsea el archivo `/.well-known/agent-card.json` de los agentes para su registro automático en el hub.
- **Discovery Inteligente (RAG):** Búsqueda de capacidades exactas y búsqueda semántica en lenguaje natural motorizada por `pgvector` y Gemini (Vía LangChain4j).
- **Monitoreo Resiliente:** Health checks asíncronos y no bloqueantes usando **Java 21 Virtual Threads** y actualizaciones a la interfaz gráfica en tiempo real mediante WebSockets.
- **Task Proxy con Streaming:** Interfaz para delegar tareas a agentes y visualizar su proceso de pensamiento (Server-Sent Events) directo en el dashboard.
- **Seguridad por Diseño:** Prevención de SSRF, límites de rate y soporte para autenticación entre agentes.

## 🏗️ Arquitectura y Tecnologías

El proyecto se estructura en un monorepo que contiene tanto el Backend como el Frontend:

- **Backend:** Java 21, Spring Boot 3.4, `RestClient` (con Virtual Threads), Spring Data JPA, PostgreSQL + `pgvector`, Flyway, WebSocket (STOMP), LangChain4j, A2A Java SDK.
- **Frontend:** Vue 3 (Composition API), TypeScript, Vite, Tailwind CSS v4, Pinia (Manejo de estado reactivo), Lucide Icons.
- **Infraestructura:** Docker & Docker Compose para un arranque rápido y sin fricciones.

## 📦 Quickstart (Comenzar localmente)

Para levantar el Hub completo en tu máquina, solo necesitas Docker y Java 21 instalados.

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/oscarbol09/a2a-hub.git
   cd a2a-hub
   ```

2. **Levantar PostgreSQL con pgvector:**
   ```bash
   docker-compose up -d postgres
   ```

3. **Ejecutar el Backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. **Ejecutar el Frontend:**
   ```bash
   cd ../frontend
   npm install
   npm run dev
   ```
   > 🌐 Accede al dashboard en `http://localhost:5173`

## 🤝 Cómo Contribuir

¡A2A-Hub es un proyecto Open Source y toda ayuda es bienvenida!
Desde corrección de bugs, traducciones, hasta nuevos componentes o ideas de diseño.

1. Por favor, lee nuestra [Guía de Contribución (CONTRIBUTING.md)](CONTRIBUTING.md) para conocer nuestro flujo de trabajo (Git Flow, Conventional Commits).
2. Asegúrate de revisar nuestro [Código de Conducta](CODE_OF_CONDUCT.md).
3. Revisa los [Issues Abiertos](https://github.com/oscarbol09/a2a-hub/issues) o únete a las discusiones para proponer tu idea antes de codificar.

## 📄 Licencia

Este proyecto está licenciado bajo la **Apache License 2.0**. Puedes usarlo, modificarlo y distribuirlo libremente tanto en entornos comerciales como personales. Ver el archivo [LICENSE](LICENSE) para más detalles.

---
*Construido con ❤️ para impulsar el futuro de la orquestación multi-agente.*