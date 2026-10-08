package pe.finanzasglobal.divisas.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Pruebas de integracion: contexto Spring completo + H2 + Spring Security. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperacionApiSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private static RequestPostProcessor usuario(String nombre, String rol, String oficina) {
        return jwt().jwt(j -> j.subject(nombre).claim("oficina", oficina))
                    .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    private static final String VENTA_1000 =
            "{\"clienteId\":1,\"tipo\":\"VENTA\",\"moneda\":\"USD\",\"monto\":1000.00}";
    private static final String VENTA_10000 =
            "{\"clienteId\":1,\"tipo\":\"VENTA\",\"moneda\":\"USD\",\"monto\":10000.00}";

    private static String clave() { return UUID.randomUUID().toString(); }

    // ---------------- AUTENTICACION ----------------
    @Test @DisplayName("SEC-01 Sin token -> 401")
    void sinToken() throws Exception {
        mvc.perform(get("/api/v1/operaciones/1")).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("SEC-02 Token falso/manipulado -> 401")
    void tokenInvalido() throws Exception {
        mvc.perform(get("/api/v1/tipos-cambio/USD").header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.e30.xxx"))
           .andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("SEC-03 Login correcto emite JWT y el JWT real permite consultar")
    void loginYUsoDeToken() throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"cajero01\",\"password\":\"Test#2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(body).get("accessToken").asText();
        mvc.perform(get("/api/v1/tipos-cambio/USD").header("Authorization", "Bearer " + token))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.moneda").value("USD"));
    }

    @Test @DisplayName("SEC-04 Password incorrecto -> 401 generico")
    void loginIncorrecto() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"cajero01\",\"password\":\"incorrecta\"}"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.detail").value("Credenciales invalidas"));
    }

    // ---------------- AUTORIZACION ----------------
    @Test @DisplayName("SEC-05 Cajero NO puede modificar el tipo de cambio -> 403")
    void cajeroNoModificaTipoCambio() throws Exception {
        mvc.perform(put("/api/v1/tipos-cambio/USD").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"compra\":1.00,\"venta\":1.10}"))
           .andExpect(status().isForbidden());
    }

    @Test @DisplayName("SEC-06 Cajero NO puede aprobar operaciones -> 403")
    void cajeroNoAprueba() throws Exception {
        mvc.perform(post("/api/v1/operaciones/1/aprobar").with(usuario("cajero01", "CAJERO", "LIMA-01")))
           .andExpect(status().isForbidden());
    }

    @Test @DisplayName("SEC-07 Auditor NO puede registrar operaciones -> 403")
    void auditorNoRegistra() throws Exception {
        mvc.perform(post("/api/v1/operaciones").with(usuario("auditor01", "AUDITOR", "CENTRAL"))
                        .header("Idempotency-Key", clave())
                        .contentType(MediaType.APPLICATION_JSON).content(VENTA_1000))
           .andExpect(status().isForbidden());
    }

    @Test @DisplayName("SEC-08 Ruta no declarada -> denegada por defecto")
    void denyByDefault() throws Exception {
        mvc.perform(delete("/api/v1/operaciones/1").with(usuario("admin01", "ADMIN", "CENTRAL")))
           .andExpect(status().isForbidden());
    }

    // ---------------- FUNCIONALES ----------------
    @Test @DisplayName("FUN-01 Cajero registra venta: calculo, comision y trazabilidad")
    void registraVenta() throws Exception {
        mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                        .header("Idempotency-Key", clave())
                        .contentType(MediaType.APPLICATION_JSON).content(VENTA_1000))
           .andExpect(status().isCreated())
           .andExpect(header().exists("Location"))
           .andExpect(jsonPath("$.estado").value("REGISTRADA"))
           .andExpect(jsonPath("$.montoSoles").value(3740.00))
           .andExpect(jsonPath("$.comision").value(18.70))
           .andExpect(jsonPath("$.totalSoles").value(3758.70))
           .andExpect(jsonPath("$.usuario").value("cajero01"))
           .andExpect(jsonPath("$.oficina").value("LIMA-01"))
           .andExpect(jsonPath("$.tipoCambioId").isNumber());
    }

    @Test @DisplayName("FUN-02 Reintento con la misma Idempotency-Key no duplica")
    void idempotencia() throws Exception {
        String k = clave();
        String r1 = mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                .header("Idempotency-Key", k).contentType(MediaType.APPLICATION_JSON).content(VENTA_1000))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String r2 = mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                .header("Idempotency-Key", k).contentType(MediaType.APPLICATION_JSON).content(VENTA_1000))
                .andReturn().getResponse().getContentAsString();
        JsonNode a = mapper.readTree(r1), b = mapper.readTree(r2);
        org.junit.jupiter.api.Assertions.assertEquals(a.get("id").asLong(), b.get("id").asLong());
    }

    @Test @DisplayName("FUN-03 Flujo de aprobacion con segregacion de funciones")
    void flujoAprobacion() throws Exception {
        String body = mvc.perform(post("/api/v1/operaciones").with(usuario("supervisor01", "SUPERVISOR", "LIMA-01"))
                        .header("Idempotency-Key", clave()).contentType(MediaType.APPLICATION_JSON).content(VENTA_10000))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(body).get("id").asLong();

        mvc.perform(post("/api/v1/operaciones/" + id + "/aprobar").with(usuario("supervisor01", "SUPERVISOR", "LIMA-01")))
           .andExpect(status().isUnprocessableEntity());               // no puede aprobar lo suyo
        mvc.perform(post("/api/v1/operaciones/" + id + "/aprobar").with(usuario("supervisor02", "SUPERVISOR", "LIMA-02")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.estado").value("APROBADA"))
           .andExpect(jsonPath("$.aprobadoPor").value("supervisor02"));
    }

    @Test @DisplayName("FUN-04 Admin actualiza EUR y el auditor ve el historial")
    void historialTipoCambio() throws Exception {
        mvc.perform(put("/api/v1/tipos-cambio/EUR").with(usuario("admin01", "ADMIN", "CENTRAL"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"compra\":4.0100,\"venta\":4.1200}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.registradoPor").value("admin01"));
        mvc.perform(get("/api/v1/tipos-cambio/EUR/historial").with(usuario("auditor01", "AUDITOR", "CENTRAL")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    // ---------------- VALIDACION Y ERRORES ----------------
    @Test @DisplayName("VAL-01 Monto negativo y moneda invalida -> 400 con detalle por campo")
    void validacionCampos() throws Exception {
        mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                        .header("Idempotency-Key", clave()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"tipo\":\"VENTA\",\"moneda\":\"usd\",\"monto\":-5}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errores.monto").exists())
           .andExpect(jsonPath("$.errores.moneda").exists());
    }

    @Test @DisplayName("VAL-02 Sin Idempotency-Key -> 400")
    void sinClaveIdempotencia() throws Exception {
        mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                        .contentType(MediaType.APPLICATION_JSON).content(VENTA_1000))
           .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("VAL-03 JSON malformado / tipo inexistente -> 400 sin traza")
    void jsonMalformado() throws Exception {
        mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                        .header("Idempotency-Key", clave()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"tipo\":\"ROBAR\",\"moneda\":\"USD\",\"monto\":1}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test @DisplayName("VAL-04 Cliente inhabilitado -> 422")
    void clienteInhabilitado() throws Exception {
        mvc.perform(post("/api/v1/operaciones").with(usuario("cajero01", "CAJERO", "LIMA-01"))
                        .header("Idempotency-Key", clave()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":2,\"tipo\":\"COMPRA\",\"moneda\":\"USD\",\"monto\":100}"))
           .andExpect(status().isUnprocessableEntity());
    }

    @Test @DisplayName("VAL-05 Cotizacion incoherente (compra > venta) -> 422")
    void cotizacionIncoherente() throws Exception {
        mvc.perform(put("/api/v1/tipos-cambio/USD").with(usuario("admin01", "ADMIN", "CENTRAL"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"compra\":3.90,\"venta\":3.70}"))
           .andExpect(status().isUnprocessableEntity());
    }

    @Test @DisplayName("VAL-06 Operacion inexistente -> 404")
    void noExiste() throws Exception {
        mvc.perform(get("/api/v1/operaciones/999999").with(usuario("auditor01", "AUDITOR", "CENTRAL")))
           .andExpect(status().isNotFound());
    }
}
