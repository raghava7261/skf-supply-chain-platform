#!/bin/bash
# Seeds the SCM platform with realistic sample data by calling the real
# REST APIs (not direct DB inserts) — this means every Kafka event actually
# fires as it would in production, so you can watch the dashboard fill up
# live while this runs.
#
# Usage: ./seed-data.sh
# Prerequisite: docker compose up --build (and wait ~60s for Keycloak/Kafka to be ready)

set -e

SUPPLIER_API="http://localhost:8081"
INVENTORY_API="http://localhost:8082"
PO_API="http://localhost:8083"
WAREHOUSE_API="http://localhost:8085"
SHIPMENT_API="http://localhost:8086"
KEYCLOAK_TOKEN_URL="http://localhost:8090/realms/scm-realm/protocol/openid-connect/token"

echo "==> Fetching an access token for procurement.manager..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_TOKEN_URL" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=scm-client&username=procurement.manager&password=changeMe123" \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['access_token'])")
AUTH_HEADER="Authorization: Bearer $TOKEN"

echo "==> 1. Registering suppliers..."
curl -s -X POST "$SUPPLIER_API/api/v1/suppliers" -H "Content-Type: application/json" -d '{
  "supplierCode":"SUP-001","name":"Nordic Precision Bearings AB","contactEmail":"orders@nordicprecision.test",
  "country":"SE","leadTimeDays":10}' > /dev/null

curl -s -X POST "$SUPPLIER_API/api/v1/suppliers" -H "Content-Type: application/json" -d '{
  "supplierCode":"SUP-002","name":"Rheinland Seals GmbH","contactEmail":"sales@rheinlandseals.test",
  "country":"DE","leadTimeDays":7}' > /dev/null

curl -s -X POST "$SUPPLIER_API/api/v1/suppliers" -H "Content-Type: application/json" -d '{
  "supplierCode":"SUP-003","name":"Pacific Lubrication Systems","contactEmail":"orders@pacificlube.test",
  "country":"US","leadTimeDays":14}' > /dev/null
echo "    3 suppliers created."

echo "==> 2. Creating inventory across 2 warehouses..."
curl -s -X POST "$INVENTORY_API/api/v1/inventory" -H "Content-Type: application/json" -d '{
  "skuCode":"BRG-6205","skuDescription":"Deep groove ball bearing 6205","warehouseCode":"WH-EU-1",
  "quantity":45,"reorderThreshold":20,"reorderQuantity":150,"preferredSupplierCode":"SUP-001"}' > /dev/null

curl -s -X POST "$INVENTORY_API/api/v1/inventory" -H "Content-Type: application/json" -d '{
  "skuCode":"BRG-6205","skuDescription":"Deep groove ball bearing 6205","warehouseCode":"WH-US-1",
  "quantity":12,"reorderThreshold":15,"reorderQuantity":150,"preferredSupplierCode":"SUP-001"}' > /dev/null

curl -s -X POST "$INVENTORY_API/api/v1/inventory" -H "Content-Type: application/json" -d '{
  "skuCode":"SEL-4412","skuDescription":"Rotary shaft seal 44x12mm","warehouseCode":"WH-EU-1",
  "quantity":8,"reorderThreshold":10,"reorderQuantity":100,"preferredSupplierCode":"SUP-002"}' > /dev/null

curl -s -X POST "$INVENTORY_API/api/v1/inventory" -H "Content-Type: application/json" -d '{
  "skuCode":"LUB-2200","skuDescription":"High-temp bearing grease 400g cartridge","warehouseCode":"WH-EU-1",
  "quantity":60,"reorderThreshold":25,"reorderQuantity":200,"preferredSupplierCode":"SUP-003"}' > /dev/null
echo "    4 inventory records created. Note: SEL-4412 starts BELOW its threshold (8 <= 10) —"
echo "    this fires LOW_STOCK_DETECTED -> purchase-order-service on startup, watch the dashboard."

echo "==> 3. Simulating a pick that pushes BRG-6205 @ WH-US-1 below threshold..."
curl -s -X POST "$INVENTORY_API/api/v1/inventory/BRG-6205/WH-US-1/adjust" -H "Content-Type: application/json" -d '{
  "delta": -5, "reason": "ORDER_PICKED"}' > /dev/null
echo "    12 -> 7, threshold is 15 -> auto-PO should now exist for BRG-6205 @ WH-US-1."

echo "==> 4. Manually creating one PO (procurement-initiated, not auto-triggered)..."
curl -s -X POST "$PO_API/api/v1/purchase-orders" -H "Content-Type: application/json" -H "$AUTH_HEADER" -d '{
  "supplierCode":"SUP-003","skuCode":"LUB-2200","warehouseCode":"WH-EU-1",
  "quantity":200,"unitCost":18.50,"expectedLeadTimeDays":14}' > /dev/null
echo "    Manual PO created for LUB-2200."

echo "==> 5. Creating warehouse tasks (a put-away and a pick)..."
curl -s -X POST "$WAREHOUSE_API/api/v1/warehouse-tasks" -H "Content-Type: application/json" -d '{
  "taskType":"PUTAWAY","skuCode":"BRG-6205","warehouseCode":"WH-EU-1","binLocation":"A-12-03",
  "quantity":50,"referenceId":"PO-DEMO01","assignedTo":"jkowalski"}' > /tmp/task1.json
TASK1=$(python3 -c "import json; print(json.load(open('/tmp/task1.json'))['taskId'])")
curl -s -X POST "$WAREHOUSE_API/api/v1/warehouse-tasks/$TASK1/start" > /dev/null
curl -s -X POST "$WAREHOUSE_API/api/v1/warehouse-tasks/$TASK1/complete" -H "Content-Type: application/json" \
  -d '{"scannedCode":"BRG-6205"}' > /dev/null
echo "    Put-away task $TASK1 completed -> inventory should show +50 for BRG-6205 @ WH-EU-1."

echo "==> 6. Creating a shipment..."
curl -s -X POST "$SHIPMENT_API/api/v1/shipments" -H "Content-Type: application/json" -d '{
  "poNumber":"PO-DEMO01","carrier":"DHL Freight","trackingNumber":"DHL-778812340",
  "originWarehouse":"WH-EU-1","destination":"Client site — Stuttgart, DE",
  "estimatedDelivery":"2026-08-05T12:00:00Z"}' > /tmp/shipment1.json
SHP1=$(python3 -c "import json; print(json.load(open('/tmp/shipment1.json'))['shipmentNumber'])")
curl -s -X POST "$SHIPMENT_API/api/v1/shipments/$SHP1/in-transit" > /dev/null
echo "    Shipment $SHP1 created and marked in-transit."

echo ""
echo "==> Done. Open the dashboard at http://localhost:8087 to see everything live."
echo "    Try: curl $PO_API/api/v1/purchase-orders?status=DRAFT | python3 -m json.tool"
