# Screenshots to capture

Save each screenshot into this folder (`docs/screenshots/`) with the **exact filename** below —
the main README already links to these paths, so once you drop the files in, the images
will just appear when you view the README on GitHub.

Do these **after** `docker compose up --build` and `./seed-data.sh` have both run successfully.

---

### 01-dashboard-overview.png
Open **http://localhost:8087**. Wait ~15s for all panels to populate after seeding.
Capture the full page: header (live status dot should be green/"live"), the alert feed on
the left, and the Inventory / Purchase Orders / Warehouse Tasks / Shipments / Suppliers
panels all showing real data.

### 02-live-alert-feed.png
On the same dashboard, run the manual auto-reorder curl commands from the README's
"Try the auto-reorder flow manually" section (or re-run `./seed-data.sh`) while the
dashboard is open in another window. Screenshot the **left-hand Live Alert Feed panel**
right after new alerts appear — you want to show at least 3-4 different alert types/colors
(INFO in teal, WARNING in amber, CRITICAL in red) stacked in the feed.

### 03-auto-po-draft.png
Run:
```bash
curl http://localhost:8083/api/v1/purchase-orders?status=DRAFT | python3 -m json.tool
```
Screenshot the terminal output showing a PO with `"autoTriggered": true` — this is the
proof that a purchase order was created automatically from a low-stock event, no human
involved. (Alternatively, screenshot the Purchase Orders panel on the dashboard with a
DRAFT-status row visible — either works, terminal output is more explicit about
`autoTriggered`.)

### 04-kafdrop-topics.png
Open **http://localhost:9000** (Kafdrop). Screenshot the topic list page showing the
`scm.*` topics (e.g. `scm.stock.updated`, `scm.stock.low-stock-detected`, `scm.po.created`,
`scm.warehouse.task-completed`, `scm.shipment.status-updated`) with non-zero message
counts — proves the event bus is actually carrying traffic, not just configured.

### 05-keycloak-realm.png
Open **http://localhost:8090**, log in as `admin` / `admin`, switch to the `scm-realm`
realm (top-left dropdown), and go to **Realm roles**. Screenshot the roles list showing
`PROCUREMENT_MANAGER`, `WAREHOUSE_STAFF`, `ADMIN`, `VIEWER` — proves the OAuth2/RBAC setup
is real, not just code that was never run.

### 06-docker-ps.png
In your terminal, run:
```bash
docker compose ps
```
Screenshot the output showing all containers (`scm-postgres`, `scm-redis`, `scm-kafka`,
`scm-zookeeper`, `scm-kafdrop`, `scm-keycloak`, `scm-supplier-service`,
`scm-inventory-service`, `scm-purchase-order-service`, `scm-notification-service`,
`scm-warehouse-ops-service`, `scm-shipment-service`, `scm-dashboard`) in the "Up" state —
this is the single best "yes, this actually runs" proof for a portfolio README.

---

## Optional extras (nice to have, not required)

- **07-postgres-databases.png** — `docker exec -it scm-postgres psql -U scm_user -l` showing
  the 6 separate databases (`supplier_db`, `inventory_db`, `po_db`, `warehouse_db`,
  `shipment_db`, `keycloak_db`)
- **08-warehouse-task-scan-mismatch.png** — call the complete-task endpoint with a
  deliberately wrong `scannedCode` and screenshot the 422 error response, showing the
  scan-validation guard actually works
- **09-shipment-delayed.png** — create a shipment with an `estimatedDelivery` a few minutes
  in the past, wait for the scheduled sweep (runs every 5 min) to flip it to `DELAYED`,
  and screenshot the dashboard/alert feed showing the critical alert

## Tips for clean screenshots

- Use a wide browser window (~1400px+) for the dashboard shots so all 5 panels are visible without scrolling
- Crop out personal info (bookmarks bar, other tabs, notification banners)
- PNG format, not JPEG — text stays sharp
- If GitHub's dark/light theme matters to you, the dashboard's own dark theme will look consistent either way
