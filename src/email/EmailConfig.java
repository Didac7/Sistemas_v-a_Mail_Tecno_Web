package email;

/**
 * Configuración de credenciales de email
 */
public class EmailConfig {
    
    // Configuración del servidor de correo
    public static final String EMAIL_ADDRESS = "grupo09sc@tecnoweb.org.bo";
    public static final String EMAIL_PASSWORD = "grup009grup009*";
    
    // Servidor IMAP para leer correos
    public static final String IMAP_HOST = "mail.tecnoweb.org.bo";
    public static final String IMAP_PORT = "993"; // Puerto SSL
    
    // Servidor SMTP para enviar correos
    public static final String SMTP_HOST = "mail.tecnoweb.org.bo";
    public static final String SMTP_PORT = "587"; // Puerto TLS
    public static final String SMTP_PORT_SSL = "465"; // Puerto SSL alternativo
    
    // Carpeta de correos
    public static final String INBOX_FOLDER = "INBOX";
    
    // Configuración de protocolo
    public static final boolean USE_SSL = true;
    public static final boolean USE_TLS = true;
}
