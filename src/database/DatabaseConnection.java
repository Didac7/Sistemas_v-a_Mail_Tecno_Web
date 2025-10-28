package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase para gestionar la conexión a PostgreSQL
 */
public class DatabaseConnection {
    
    // Credenciales de la base de datos
    private static final String URL = "jdbc:postgresql://mail.tecnoweb.org.bo:5432/db_grupo09sc";
    private static final String USER = "grupo09sc";
    private static final String PASSWORD = "grup009grup009*";
    
    private static Connection connection = null;
    
    /**
     * Obtiene una conexión a la base de datos
     * Si no existe una conexión activa, crea una nueva
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Conexión a la base de datos establecida.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver PostgreSQL no encontrado: " + e.getMessage());
            }
        }
        return connection;
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Conexión a la base de datos cerrada.");
            } catch (SQLException e) {
                System.err.println("❌ Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Prueba la conexión a la base de datos
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error al probar conexión: " + e.getMessage());
            return false;
        }
    }
}
