# SKF-Style Supply Chain & Warehouse Management Platform

**An event-driven microservices platform for industrial parts distribution**, built in Spring Boot around the operational realities of a company like SKF: high SKU counts, multi-warehouse distribution, and just-in-time manufacturing clients who can't tolerate stockouts.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-black)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2%2FJWT-purple)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Per--Service-336791)

> Domain informed by exposure to SKF's business through an HCL engagement.
> This is an original portfolio build, not SKF's actual internal system.

---

## Table of Contents

- [Problem Statement](#problem-statement)
- [Architecture Overview](#architecture-overview)
- [Screenshots](#screenshots)
- [What's Implemented](#whats-implemented)
- [Database Design](#database-one-postgres-database-per-service)
- [Role-Based Access (OAuth2/Keycloak)](#role-based-access--real-oauth2jwt-via-keycloak)
- [The Live Dashboard](#the-live-dashboard)
- [Event Flows](#event-flows)
- [Running It Locally](#running-it-locally)
- [API Reference (quick)](#api-reference-quick)
- [Roadmap](#roadmap-still-open)
- [Resume Bullet](#resume-bullet-draft)

---

## Problem Statement

### Background

A global industrial components distributor — think precision bearings, seals, and lubrication systems, the kind of parts a company like SKF supplies — operates multiple regional warehouses serving manufacturing clients who run just-in-time production. A single missed delivery or oversold part doesn't just cost a sale; it can halt a client's production line at a cost of tens of thousands of dollars per hour.

This kind of business carries a specific set of hard constraints that generic e-commerce inventory systems don't handle well:

- **High SKU complexity** — thousands of near-identical part variants (bore size, seal type, tolerance class), where picking the wrong SKU is a costly, sometimes safety-critical error
- **Multi-region, multi-warehouse distribution** — regional warehouses need to operate independently but stay visible centrally
- **Just-in-time client dependency** — manufacturing clients expect predictable, on-time replenishment; a missed reorder window cascades into their downtime

### The core problem

Disconnected regional inventory systems and manual, spreadsheet-driven purchase order workflows create three recurring risks:

1. **Stock discrepancies** between physical and recorded inventory → overselling
2. **Missed supplier reorder windows** → client-facing stockouts
3. **No unified real-time visibility** for procurement or operations leadership to catch problems before a client has to call and ask where their order is

### What this platform does about it

- A **single source of truth** for inventory across every warehouse, instead of four spreadsheets that disagree with each other
- **Event-driven auto-reordering**: the moment stock crosses its threshold, a draft purchase order exists — no human has to notice and remember to act
- **Barcode/QR scan validation** on every warehouse pick/put-away, so a worker can't complete a task against the wrong near-identical SKU
- **Automated delay detection** on shipments, instead of waiting for a client to ask
- **A live operations dashboard** that makes the "flying blind" failure mode structurally impossible — every state change appears on screen in real time, not on a spreadsheet nobody's watching

---

## Architecture Overview

```
                                   ┌─────────────────┐
                                   │   Keycloak       │
                                   │  (OAuth2/JWT)    │
                                   └────────┬─────────┘
                                            │ issues tokens
                                            │
   ┌──────────────┐    ┌──────────────┐    │    ┌───────────────────┐
   │ supplier-svc │    │inventory-svc │◄───┼───►│ purchase-order-svc │
   │   :8081      │    │   :8082      │    │    │      :8083         │
   │  (Postgres)  │    │(Postgres+    │    │    │   (Postgres,       │
   └──────┬───────┘    │ Redis cache) │    │    │    OAuth2 RBAC)    │
          │            └──────┬───────┘    │    └─────────┬──────────┘
          │                   │            │              │
          │                   │      ┌─────┴──────┐       │
          └───────────────────┴─────►│   Kafka    │◄──────┘
                                      │  (events)  │
          ┌───────────────────┬──────┴──────┬─────────────┐
          │                   │             │             │
   ┌──────▼───────┐   ┌───────▼──────┐ ┌────▼────────┐ ┌──▼─────────────┐
   │warehouse-ops  │   │ shipment-svc │ │notification-│ │   dashboard    │
   │   :8085       │   │   :8086      │ │ svc  :8084  │ │ (nginx) :8087  │
   │  (Postgres)   │   │  (Postgres)  │ │ (WebSocket) │ │ live console   │
   └───────────────┘   └──────────────┘ └─────────────┘ └────────────────┘
```

Every service owns its own Postgres database and talks to the others only through Kafka events — no service ever queries another service's tables directly. See [Database Design](#database-one-postgres-database-per-service) for why.

---

## Screenshots

> _Screenshots go here — see "Screenshots to capture" further down for exactly what to take and where to save each one._

### Live Operations Dashboard
![Dashboard overview](docs/screenshots/01-dashboard-overview.png)

### Live Alert Feed in Action
![Live alert feed](docs/screenshots/02-live-alert-feed.png)

### Auto-Generated Purchase Order (from a low-stock event)
![Auto-generated PO](docs/screenshots/03-auto-po-draft.png)

### Kafka Topics in Kafdrop
![Kafdrop topics](docs/screenshots/04-kafdrop-topics.png)

### Keycloak Realm & Roles
![Keycloak realm](docs/screenshots/05-keycloak-realm.png)

### Docker Compose — Full Stack Running
![Docker containers running](docs/screenshots/06-docker-ps.png)

---

## What's Implemented

| Service | Port | Responsibility |
|---|---|---|
| `supplier-service` | 8081 | Supplier master data, lead times, reliability scoring |
| `inventory-service` | 8082 | SKU-per-warehouse stock, Redis-cached reads, low-stock detection |
| `purchase-order-service` | 8083 | PO lifecycle, **auto-drafts POs from low-stock events**, OAuth2/JWT RBAC |
| `notification-service` | 8084 | Fans all platform events into one feed, pushes live via WebSocket |
| `warehouse-ops-service` | 8085 | Pick/pack/put-away tasks with barcode/QR scan-match validation |
| `shipment-service` | 8086 | Shipment lifecycle, scheduled delay detection every 5 min |
| `dashboard` (nginx) | 8087 | Live ops console — WebSocket feed + tables for every domain |
| `keycloak` | 8090 | Identity provider issuing JWTs for role-based access |
| `kafdrop` | 9000 | Kafka topic/message browser UI |

## Database: one Postgres database per service

Each service owns its own database — `supplier_db`, `inventory_db`, `po_db`, `warehouse_db`, `shipment_db` — all on the same Postgres container for local dev simplicity, but logically fully separate (no service ever queries another service's tables directly). Keycloak also gets its own `keycloak_db`.

**Why per-service databases instead of one shared schema:** each service can evolve its schema independently, deploy independently, and in production could even move to a different database engine without affecting anyone else. The tradeoff is you give up cross-service SQL joins — that's exactly why Kafka events exist here: instead of `SELECT ... JOIN` across services, state changes propagate as events (`STOCK_UPDATED`, `PO_CREATED`, etc.) and each service keeps only the slice of data it actually needs. This is the standard microservices data pattern, and being able to explain its cost (eventual consistency, not immediate) is as important as knowing why to use it.

## Role-Based Access — real OAuth2/JWT via Keycloak

`purchase-order-service` validates JWTs issued by Keycloak instead of using hardcoded in-memory users. Roles (`PROCUREMENT_MANAGER`, `WAREHOUSE_STAFF`, `ADMIN`, `VIEWER`) live in Keycloak's `scm-realm` (auto-imported from `infra/keycloak/scm-realm.json` on first startup) and are read from the JWT's `realm_access.roles` claim.

Demo users (all password `changeMe123`): `procurement.manager`, `warehouse.staff`, `ops.admin`, `dashboard.viewer`.

```bash
TOKEN=$(curl -s -X POST http://localhost:8090/realms/scm-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=scm-client&username=procurement.manager&password=changeMe123" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")

curl -H "Authorization: Bearer $TOKEN" http://localhost:8083/api/v1/purchase-orders
```

The other services currently accept unauthenticated requests — the pattern above is what to replicate onto them (see [Roadmap](#roadmap-still-open)).

## The Live Dashboard

A single static page (`dashboard/index.html`, served by nginx) that:
- Connects to `notification-service`'s WebSocket (`/topic/alerts`) and shows every platform event in real time
- Signs in against Keycloak to get a token for PO actions
- Polls every service's REST API every 15s for inventory, POs, warehouse tasks, shipments, and suppliers

Open it at **http://localhost:8087** once the stack is up.

## Event Flows

### Auto-reorder (the centerpiece — this is the story to tell in an interview)

```
inventory-service                purchase-order-service
   |  stock drops below              |
   |  reorder threshold               |
   └──► LOW_STOCK_DETECTED ──Kafka──► listens, auto-creates DRAFT PO
                                       |
                                       ▼
                                 procurement approves (RBAC: PROCUREMENT_MANAGER)
                                       |
                                       ▼
                                 warehouse marks RECEIVED (RBAC: WAREHOUSE_STAFF)
                                       |
                                       └──► PO_RECEIVED ──Kafka──► inventory-service
                                                                    auto-increments stock
```

### Warehouse operations & shipment tracking

```
warehouse-ops-service                    shipment-service
  worker scans SKU,                        scheduled sweep every 5 min
  completes a PICK/PUTAWAY task             checks IN_TRANSIT shipments
       |                                    against their ETA
       └──► WAREHOUSE_TASK_COMPLETED             |
             │  (Kafka)                          └──► SHIPMENT_STATUS_UPDATED
             ▼                                         (DELAYED if overdue)
      inventory-service consumes it,                    │
      adjusts stock automatically                        ▼
      (PICK: -qty, PUTAWAY: +qty)                notification-service
                                                   pushes it to the live
                                                   dashboard instantly
```

The scan-match check on task completion (`scannedCode` must equal the task's `skuCode`) is the direct fix for the SKU-complexity risk in the problem statement — a worker can't accidentally complete a pick against the wrong near-identical bearing variant.

### Why each tech choice is there
- **Kafka** — decouples inventory, procurement, warehouse ops, shipments, and notifications; each service reacts to events instead of polling or being tightly coupled via REST chains
- **Redis** — caches hot inventory lookups (60s TTL) so dashboard-style read traffic doesn't hammer Postgres; every write path evicts the cache immediately
- **Postgres per service** — no shared-database coupling between microservices
- **Keycloak/OAuth2** — centralized identity, stateless JWT validation, no service manages its own passwords
- **Docker Compose** — one command spins up the entire stack

---

## Running It Locally

**Prerequisites:** Docker Desktop running, ~4GB free RAM for the containers.

```bash
git clone https://github.com/raghava7261/skf-supply-chain-platform.git
cd skf-supply-chain-platform
docker compose up --build
```

This starts Postgres, Redis, Zookeeper, Kafka, Kafdrop, Keycloak, all seven application services, and the dashboard. **Give it 60-90 seconds** on first boot — Keycloak and Kafka both take a moment to become healthy.

### Load sample data

```bash
./seed-data.sh
```

This registers 3 suppliers, creates inventory across 2 warehouses (deliberately seeding one SKU below its reorder threshold), simulates a pick that trips another threshold, creates a manual PO, runs a full put-away task, and creates an in-transit shipment — all through the real REST APIs, so every Kafka event fires as it would in production.

Then open **http://localhost:8087** to watch it all on the live dashboard.

### Try the auto-reorder flow manually instead

```bash
# 1. Register a supplier
curl -X POST http://localhost:8081/api/v1/suppliers \
  -H "Content-Type: application/json" \
  -d '{"supplierCode":"SUP-001","name":"Acme Bearings","contactEmail":"orders@acme.test","country":"DE","leadTimeDays":10}'

# 2. Create an inventory item with a reorder threshold
curl -X POST http://localhost:8082/api/v1/inventory \
  -H "Content-Type: application/json" \
  -d '{"skuCode":"BRG-6205","skuDescription":"Deep groove ball bearing 6205","warehouseCode":"WH-EU-1","quantity":15,"reorderThreshold":10,"reorderQuantity":100,"preferredSupplierCode":"SUP-001"}'

# 3. Pick stock down below threshold — this triggers the whole chain
curl -X POST http://localhost:8082/api/v1/inventory/BRG-6205/WH-EU-1/adjust \
  -H "Content-Type: application/json" \
  -d '{"delta":-8,"reason":"ORDER_PICKED"}'

# 4. Check that a PO was auto-created
curl http://localhost:8083/api/v1/purchase-orders?status=DRAFT

# 5. Watch it happen live
curl http://localhost:8084/api/v1/notifications
```

### Useful local URLs

| What | URL |
|---|---|
| Live dashboard | http://localhost:8087 |
| Kafka topic browser (Kafdrop) | http://localhost:9000 |
| Keycloak admin console | http://localhost:8090 (admin/admin) |

---

## API Reference (quick)

<details>
<summary><b>supplier-service</b> — :8081/api/v1/suppliers</summary>

| Method | Path | Description |
|---|---|---|
| POST | `/` | Register a supplier |
| GET | `/` | List all suppliers |
| GET | `/{supplierCode}` | Get one supplier |
| PUT | `/{supplierCode}` | Update supplier |
| PATCH | `/{supplierCode}/reliability?onTime=true` | Adjust reliability score |
| DELETE | `/{supplierCode}` | Deactivate supplier |
</details>

<details>
<summary><b>inventory-service</b> — :8082/api/v1/inventory</summary>

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create inventory item |
| GET | `/{skuCode}/{warehouseCode}` | Get stock (Redis-cached) |
| GET | `/sku/{skuCode}` | Stock across all warehouses |
| GET | `/warehouse/{warehouseCode}` | All stock at one warehouse |
| POST | `/{skuCode}/{warehouseCode}/adjust` | Adjust stock (+/-) |
| PATCH | `/{skuCode}/{warehouseCode}/thresholds` | Update reorder thresholds |
</details>

<details>
<summary><b>purchase-order-service</b> — :8083/api/v1/purchase-orders (OAuth2)</summary>

| Method | Path | Required Role |
|---|---|---|
| POST | `/` | PROCUREMENT_MANAGER or WAREHOUSE_STAFF |
| GET | `/`, `/{poNumber}` | Any authenticated role |
| POST | `/{poNumber}/approve` | PROCUREMENT_MANAGER |
| POST | `/{poNumber}/receive` | WAREHOUSE_STAFF |
| POST | `/{poNumber}/cancel` | PROCUREMENT_MANAGER |
</details>

<details>
<summary><b>warehouse-ops-service</b> — :8085/api/v1/warehouse-tasks</summary>

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create a pick/pack/putaway task |
| GET | `/`, `/{taskId}` | List / get tasks |
| POST | `/{taskId}/start` | Mark in progress |
| POST | `/{taskId}/complete` | Complete (requires `scannedCode` matching SKU) |
| POST | `/{taskId}/cancel` | Cancel |
</details>

<details>
<summary><b>shipment-service</b> — :8086/api/v1/shipments</summary>

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create shipment |
| GET | `/`, `/{shipmentNumber}` | List / get shipments |
| POST | `/{shipmentNumber}/in-transit` | Mark in transit |
| POST | `/{shipmentNumber}/delivered` | Mark delivered |
| POST | `/{shipmentNumber}/cancel` | Cancel |
</details>

<details>
<summary><b>notification-service</b> — :8084</summary>

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/notifications?limit=50` | Recent alert feed (REST fallback) |
| WS | `/ws` → subscribe `/topic/alerts` | Live alert stream (STOMP over SockJS) |
</details>

---


---

## License

Personal portfolio project. Not affiliated with or endorsed by SKF Group.
