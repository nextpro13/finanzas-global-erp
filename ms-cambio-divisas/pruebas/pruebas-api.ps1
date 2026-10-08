# Pruebas funcionales y de seguridad para Windows (PowerShell 5.1 o 7+)
# Uso (con el servicio levantado):  powershell -ExecutionPolicy Bypass -File pruebas\pruebas-api.ps1
$Base = if ($env:BASE) { $env:BASE } else { "http://localhost:8082/api/v1" }
$Pass = if ($env:APP_DEMO_PASSWORD) { $env:APP_DEMO_PASSWORD } else { "Demo#2026" }
$script:ok = 0; $script:fail = 0

function Llamar($Metodo, $Ruta, $Token = $null, $Body = $null, $Clave = $null) {
    $h = @{}
    if ($Token) { $h["Authorization"] = "Bearer $Token" }
    if ($Clave) { $h["Idempotency-Key"] = $Clave }
    $p = @{ Uri = "$Base$Ruta"; Method = $Metodo; Headers = $h; UseBasicParsing = $true }
    if ($Body) { $p["Body"] = $Body; $p["ContentType"] = "application/json" }
    try {
        $r = Invoke-WebRequest @p
        return @{ Code = [int]$r.StatusCode; Json = ($r.Content | ConvertFrom-Json) }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) { return @{ Code = [int]$resp.StatusCode; Json = $null } }
        throw
    }
}

function Check($Nombre, $Esperado, $Obtenido) {
    if ("$Esperado" -eq "$Obtenido") {
        Write-Host ("  [OK]    {0} -> {1}" -f $Nombre, $Obtenido) -ForegroundColor Green; $script:ok++
    } else {
        Write-Host ("  [FALLO] {0} -> esperado {1}, obtenido {2}" -f $Nombre, $Esperado, $Obtenido) -ForegroundColor Red; $script:fail++
    }
}

function Token($Usuario) {
    (Llamar POST "/auth/login" $null ('{"usuario":"' + $Usuario + '","password":"' + $Pass + '"}')).Json.accessToken
}

$CAJ = Token "cajero01"; $SUP1 = Token "supervisor01"; $SUP2 = Token "supervisor02"
$ADM = Token "admin01"; $AUD = Token "auditor01"

$v1000  = '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":1000}'
$v10000 = '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":10000}'
$K = "k-" + [guid]::NewGuid().ToString("N").Substring(0, 20)

Write-Host "== Autenticacion ==" -ForegroundColor Cyan
Check "Sin token"              401 (Llamar GET "/tipos-cambio/USD").Code
Check "Token manipulado"       401 (Llamar GET "/tipos-cambio/USD" ($CAJ + "x")).Code
Check "Password erroneo"       401 (Llamar POST "/auth/login" $null '{"usuario":"cajero01","password":"x"}').Code
Check "Consulta con token"     200 (Llamar GET "/tipos-cambio/USD" $CAJ).Code

Write-Host "== Autorizacion ==" -ForegroundColor Cyan
Check "Cajero modifica TC"     403 (Llamar PUT "/tipos-cambio/USD" $CAJ '{"compra":1,"venta":2}').Code
Check "Cajero aprueba"         403 (Llamar POST "/operaciones/1/aprobar" $CAJ).Code
Check "Auditor registra op."   403 (Llamar POST "/operaciones" $AUD $v1000 "k-aud-000001").Code

Write-Host "== Funcionales ==" -ForegroundColor Cyan
$r1 = Llamar POST "/operaciones" $CAJ $v1000 $K
Check "Cajero registra venta"  201 $r1.Code
Write-Host ("          total a cobrar: S/ {0}  (estado {1})" -f $r1.Json.totalSoles, $r1.Json.estado)
$r2 = Llamar POST "/operaciones" $CAJ $v1000 $K
Check "Reintento no duplica (mismo id)" $r1.Json.id $r2.Json.id
$r3 = Llamar POST "/operaciones" $SUP1 $v10000 ("g-" + $K)
Check "Op. sobre limite"       "PENDIENTE_APROBACION" $r3.Json.estado
Check "Aprobar su propia op."  422 (Llamar POST ("/operaciones/" + $r3.Json.id + "/aprobar") $SUP1).Code
Check "supervisor02 aprueba"   200 (Llamar POST ("/operaciones/" + $r3.Json.id + "/aprobar") $SUP2).Code
Check "Admin actualiza EUR"    200 (Llamar PUT "/tipos-cambio/EUR" $ADM '{"compra":4.01,"venta":4.12}').Code
Check "Auditor ve historial"   200 (Llamar GET "/tipos-cambio/EUR/historial" $AUD).Code

Write-Host "== Validaciones ==" -ForegroundColor Cyan
Check "Monto negativo"         400 (Llamar POST "/operaciones" $CAJ '{"clienteId":1,"tipo":"VENTA","moneda":"USD","monto":-5}' ("v-" + $K)).Code
Check "Sin Idempotency-Key"    400 (Llamar POST "/operaciones" $CAJ $v1000).Code
Check "Cliente inhabilitado"   422 (Llamar POST "/operaciones" $CAJ '{"clienteId":2,"tipo":"COMPRA","moneda":"USD","monto":5}' ("c-" + $K)).Code
Check "Compra mayor que venta" 422 (Llamar PUT "/tipos-cambio/USD" $ADM '{"compra":3.9,"venta":3.7}').Code
$sql = (Llamar GET "/tipos-cambio/USD'%20OR%201=1--" $CAJ).Code
if ($sql -eq 400 -or $sql -eq 422) { Check "Inyeccion SQL rechazada" $sql $sql } else { Check "Inyeccion SQL rechazada" "400/422" $sql }

Write-Host ""
Write-Host ("RESULTADO: {0} OK, {1} fallos" -f $script:ok, $script:fail) -ForegroundColor Yellow