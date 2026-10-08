#!/usr/bin/env bash
# Pruebas funcionales y de seguridad contra el servicio en ejecucion (mvn spring-boot:run).
# Uso: bash pruebas/pruebas-api.sh   (requiere curl y jq)
BASE=${BASE:-http://localhost:8082/api/v1}
PASS=${APP_DEMO_PASSWORD:-Demo#2026}
ok=0; fail=0
check() { if [ "$2" == "$3" ]; then echo "  [OK]   $1 -> HTTP $3"; ok=$((ok+1)); else echo "  [FALLO] $1 -> esperado $2, obtenido $3"; fail=$((fail+1)); fi; }
token() { curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d "{\"usuario\":\"$1\",\"password\":\"$PASS\"}" | jq -r .accessToken; }
code() { curl -s -o /tmp/resp.json -w '%{http_code}' "$@"; }

CAJ=$(token cajero01); SUP1=$(token supervisor01); SUP2=$(token supervisor02); ADM=$(token admin01); AUD=$(token auditor01)

echo "== Autenticacion =="
check "Sin token"               401 $(code $BASE/tipos-cambio/USD)
check "Token manipulado"        401 $(code $BASE/tipos-cambio/USD -H "Authorization: Bearer ${CAJ}x")
check "Login password erroneo"  401 $(code -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"usuario":"cajero01","password":"x"}')
check "Consulta con token"      200 $(code $BASE/tipos-cambio/USD -H "Authorization: Bearer $CAJ")

echo "== Autorizacion =="
check "Cajero modifica TC"      403 $(code -X PUT $BASE/tipos-cambio/USD -H "Authorization: Bearer $CAJ" -H 'Content-Type: application/json' -d '{"compra":1,"venta":2}')
check "Auditor registra op."    403 $(code -X POST $BASE/operaciones -H "Authorization: Bearer $AUD" -H "Idempotency-Key: k-aud-00001" -H 'Content-Type: application/json' -d '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":100}')

echo "== Funcionales =="
K=k-$(date +%s%N)
check "Cajero registra venta"   201 $(code -X POST $BASE/operaciones -H "Authorization: Bearer $CAJ" -H "Idempotency-Key: $K" -H 'Content-Type: application/json' -d '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":1000}')
ID1=$(jq .id /tmp/resp.json); echo "         total a cobrar: S/ $(jq .totalSoles /tmp/resp.json)"
code -X POST $BASE/operaciones -H "Authorization: Bearer $CAJ" -H "Idempotency-Key: $K" -H 'Content-Type: application/json' -d '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":1000}' >/dev/null
check "Reintento no duplica (mismo id)" "$ID1" "$(jq .id /tmp/resp.json)"
check "Op. > limite: supervisor01" 201 $(code -X POST $BASE/operaciones -H "Authorization: Bearer $SUP1" -H "Idempotency-Key: g-$K" -H 'Content-Type: application/json' -d '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":10000}')
ID2=$(jq .id /tmp/resp.json)
check "Aprobar su propia op."   422 $(code -X POST $BASE/operaciones/$ID2/aprobar -H "Authorization: Bearer $SUP1")
check "supervisor02 aprueba"    200 $(code -X POST $BASE/operaciones/$ID2/aprobar -H "Authorization: Bearer $SUP2")
check "Admin actualiza EUR"     200 $(code -X PUT $BASE/tipos-cambio/EUR -H "Authorization: Bearer $ADM" -H 'Content-Type: application/json' -d '{"compra":4.01,"venta":4.12}')
check "Auditor ve historial"    200 $(code $BASE/tipos-cambio/EUR/historial -H "Authorization: Bearer $AUD")

echo "== Validaciones =="
check "Monto negativo"          400 $(code -X POST $BASE/operaciones -H "Authorization: Bearer $CAJ" -H "Idempotency-Key: v-$K" -H 'Content-Type: application/json' -d '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":-5}')
check "Sin Idempotency-Key"     400 $(code -X POST $BASE/operaciones -H "Authorization: Bearer $CAJ" -H 'Content-Type: application/json' -d '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":5}')
check "Cliente inhabilitado"    422 $(code -X POST $BASE/operaciones -H "Authorization: Bearer $CAJ" -H "Idempotency-Key: c-$K" -H 'Content-Type: application/json' -d '{"clienteId":2,"tipo":"COMPRA","moneda":"USD","monto":5}')
check "Compra > venta"          422 $(code -X PUT $BASE/tipos-cambio/USD -H "Authorization: Bearer $ADM" -H 'Content-Type: application/json' -d '{"compra":3.9,"venta":3.7}')
C=$(code "$BASE/tipos-cambio/USD'%20OR%201=1--" -H "Authorization: Bearer $CAJ")
[[ "$C" == "400" || "$C" == "422" ]] && check "Inyeccion SQL en ruta (rechazada)" "$C" "$C" || check "Inyeccion SQL en ruta" "400/422" "$C"

echo; echo "RESULTADO: $ok OK, $fail fallos"
