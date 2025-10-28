import email.EmailReader;
//import email.EmailSender;
import database.DatabaseConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Sistema de Seguimiento de Paquetes - Trans Velasco
 * Grupo09sc - Tecnología Web
 * 
 * Sistema que procesa comandos vía email para gestionar:
 * - Usuarios, Vehículos, Destinos, Rutas
 * - Paquetes, Seguimiento, Pagos, Reportes
 */
public class Main {
    
    private static final long INTERVALO_LECTURA = 30000; // 30 segundos
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   SISTEMA DE SEGUIMIENTO DE PAQUETES - TRANS VELASCO     ║");
        System.out.println("║                    Grupo09sc - 2025                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Verificar conexión a la base de datos
        System.out.println("🔌 Verificando conexión a la base de datos...");
        if (!DatabaseConnection.testConnection()) {
            System.err.println("❌ ERROR: No se pudo conectar a la base de datos.");
            System.err.println("   Verifica las credenciales en DatabaseConnection.java");
            return;
        }
        System.out.println("✅ Conexión a la base de datos exitosa!");
        System.out.println();
        
        // Verificar configuración de email
        System.out.println("📧 Verificando configuración de email...");
        EmailReader emailReader = new EmailReader();
        
        if (!emailReader.testConnection()) {
            System.err.println("❌ ERROR: No se pudo conectar al servidor de correo.");
            System.err.println("   Verifica las credenciales en EmailReader.java");
            return;
        }
        System.out.println("✅ Configuración de email correcta!");
        System.out.println();
        
        // Iniciar el sistema de lectura de correos
        System.out.println("🚀 Iniciando sistema de procesamiento de comandos...");
        System.out.println("⏱️  Intervalo de lectura: " + (INTERVALO_LECTURA / 1000) + " segundos");
        System.out.println("📬 Monitoreando: grupo09sc@tecnoweb.org.bo");
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("   Sistema activo. Presiona Ctrl+C para detener.");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println();
        
        // Timer para leer correos periódicamente
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("📨 [" + java.time.LocalDateTime.now() + "] Revisando correos...");
                    emailReader.procesarCorreosPendientes();
                } catch (Exception e) {
                    System.err.println("❌ Error al procesar correos: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 0, INTERVALO_LECTURA);
        
        // Mantener el programa corriendo
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("🛑 Deteniendo el sistema...");
            timer.cancel();
            DatabaseConnection.closeConnection();
            System.out.println("✅ Sistema detenido correctamente.");
        }));
    }
}
