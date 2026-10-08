package pe.finanzasglobal.divisas.domain.exception;

/** Se lanza cuando una operacion viola una regla de negocio financiera. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}
