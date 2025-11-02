import database.DatabaseConnection;
import java.sql.*;

/**
 * Programa simple para ver el contenido de las tablas
 */
public class VerBaseDatos {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        VISUALIZADOR DE BASE DE DATOS - TRANS VELASCO     ║");
        System.out.println("║                    Grupo09sc - 2025                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("✅ Conectado a la base de datos exitosamente!\n");
            
            // Mostrar todas las tablas
            mostrarTabla(conn, "usuarios");
            mostrarTabla(conn, "vehiculos");
            mostrarTabla(conn, "destinos");
            mostrarTabla(conn, "rutas");
            mostrarTabla(conn, "paquetes");
            mostrarTabla(conn, "seguimiento");
            mostrarTabla(conn, "pagos");
            mostrarTabla(conn, "reportes");
            
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar a la base de datos:");
            System.err.println("   " + e.getMessage());
        }
    }
    
    private static void mostrarTabla(Connection conn, String nombreTabla) {
        System.out.println("\n================================================================================");
        System.out.println("📋 TABLA: " + nombreTabla.toUpperCase());
        System.out.println("================================================================================");
        
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + nombreTabla + " LIMIT 10");
            
            // Obtener metadata para nombres de columnas
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Imprimir encabezados
            System.out.print("\n");
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-20s | ", metaData.getColumnName(i));
            }
            System.out.println("\n--------------------------------------------------------------------------------");
            
            // Imprimir datos
            int count = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String valor = rs.getString(i);
                    if (valor == null) valor = "NULL";
                    if (valor.length() > 18) valor = valor.substring(0, 15) + "...";
                    System.out.printf("%-20s | ", valor);
                }
                System.out.println();
                count++;
            }
            
            if (count == 0) {
                System.out.println("(Tabla vacía - sin registros)");
            } else {
                System.out.println("\n✅ Total registros mostrados: " + count + " (máximo 10)");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("⚠️ No se pudo leer la tabla: " + e.getMessage());
        }
    }
}
