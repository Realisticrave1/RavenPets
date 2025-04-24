package RavenMC.ravenPets.database;

import RavenMC.ravenPets.RavenPets;
import RavenMC.ravenPets.enums.RavenTier;
import RavenMC.ravenPets.model.Raven;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final RavenPets plugin;
    private Connection connection;

    public DatabaseManager(RavenPets plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            // Create data folder if it doesn't exist
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Connect to SQLite database
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/ravens.db");

            // Create tables if they don't exist
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Ravens table
            statement.execute("CREATE TABLE IF NOT EXISTS ravens (" +
                    "owner_id VARCHAR(36) PRIMARY KEY, " +
                    "entity_id VARCHAR(36), " +
                    "name VARCHAR(16) NOT NULL, " +
                    "level INT NOT NULL, " +
                    "xp INT NOT NULL, " +
                    "tier VARCHAR(20) NOT NULL, " +
                    "inventory_slots INT NOT NULL, " +
                    "flight_duration INT NOT NULL, " +
                    "detection_radius INT NOT NULL, " +
                    "is_active BOOLEAN NOT NULL" +
                    ")");

            // Abilities table
            statement.execute("CREATE TABLE IF NOT EXISTS raven_abilities (" +
                    "owner_id VARCHAR(36), " +
                    "ability_name VARCHAR(50), " +
                    "is_unlocked BOOLEAN NOT NULL, " +
                    "PRIMARY KEY (owner_id, ability_name), " +
                    "FOREIGN KEY (owner_id) REFERENCES ravens(owner_id) ON DELETE CASCADE" +
                    ")");

            // Home locations table
            statement.execute("CREATE TABLE IF NOT EXISTS raven_homes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "owner_id VARCHAR(36), " +
                    "world VARCHAR(50) NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL, " +
                    "yaw FLOAT NOT NULL, " +
                    "pitch FLOAT NOT NULL, " +
                    "FOREIGN KEY (owner_id) REFERENCES ravens(owner_id) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Raven loadRaven(UUID ownerId) {
        try {
            // Check if raven exists
            String ownerIdStr = ownerId.toString();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM ravens WHERE owner_id = ?")) {
                statement.setString(1, ownerIdStr);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    // Create raven instance
                    Raven raven = new Raven(ownerId);

                    // Set basic properties
                    raven.setName(rs.getString("name"));
                    raven.setLevel(rs.getInt("level"));
                    raven.setXp(rs.getInt("xp"));

                    // Load abilities
                    loadRavenAbilities(raven);

                    // Load home locations
                    List<Location> homes = loadRavenHomes(ownerIdStr);
                    for (Location home : homes) {
                        raven.addHomeLocation(home);
                    }

                    return raven;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load raven for " + ownerId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void loadRavenAbilities(Raven raven) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT ability_name, is_unlocked FROM raven_abilities WHERE owner_id = ?")) {
            statement.setString(1, raven.getOwnerId().toString());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String abilityName = rs.getString("ability_name");
                boolean isUnlocked = rs.getBoolean("is_unlocked");

                // This would set the ability in the raven
                // In our simplified model, we're not directly manipulating the abilities map
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load abilities for raven " + raven.getOwnerId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Location> loadRavenHomes(String ownerId) {
        List<Location> homes = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT world, x, y, z, yaw, pitch FROM raven_homes WHERE owner_id = ?")) {
            statement.setString(1, ownerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");

                    Location location = new Location(world, x, y, z, yaw, pitch);
                    homes.add(location);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load home locations for raven " + ownerId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return homes;
    }

    public void saveRaven(Raven raven) {
        try {
            connection.setAutoCommit(false);

            // Save or update basic raven data
            boolean exists = ravenExists(raven.getOwnerId());

            if (exists) {
                updateRaven(raven);
            } else {
                insertRaven(raven);
            }

            // Save abilities
            saveRavenAbilities(raven);

            // Save home locations
            saveRavenHomes(raven);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                plugin.getLogger().severe("Failed to rollback transaction: " + ex.getMessage());
                ex.printStackTrace();
            }

            plugin.getLogger().severe("Failed to save raven for " + raven.getOwnerId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean ravenExists(UUID ownerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM ravens WHERE owner_id = ?")) {
            statement.setString(1, ownerId.toString());
            ResultSet rs = statement.executeQuery();
            return rs.next();
        }
    }

    private void insertRaven(Raven raven) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO ravens (owner_id, entity_id, name, level, xp, tier, inventory_slots, " +
                        "flight_duration, detection_radius, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, raven.getOwnerId().toString());
            statement.setString(2, raven.getEntityId() != null ? raven.getEntityId().toString() : null);
            statement.setString(3, raven.getName());
            statement.setInt(4, raven.getLevel());
            statement.setInt(5, raven.getXp());
            statement.setString(6, raven.getTier().name());
            statement.setInt(7, raven.getInventorySlots());
            statement.setInt(8, raven.getFlightDuration());
            statement.setInt(9, raven.getDetectionRadius());
            statement.setBoolean(10, raven.isActive());

            statement.executeUpdate();
        }
    }

    private void updateRaven(Raven raven) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE ravens SET entity_id = ?, name = ?, level = ?, xp = ?, tier = ?, " +
                        "inventory_slots = ?, flight_duration = ?, detection_radius = ?, is_active = ? " +
                        "WHERE owner_id = ?")) {
            statement.setString(1, raven.getEntityId() != null ? raven.getEntityId().toString() : null);
            statement.setString(2, raven.getName());
            statement.setInt(3, raven.getLevel());
            statement.setInt(4, raven.getXp());
            statement.setString(5, raven.getTier().name());
            statement.setInt(6, raven.getInventorySlots());
            statement.setInt(7, raven.getFlightDuration());
            statement.setInt(8, raven.getDetectionRadius());
            statement.setBoolean(9, raven.isActive());
            statement.setString(10, raven.getOwnerId().toString());

            statement.executeUpdate();
        }
    }

    private void saveRavenAbilities(Raven raven) throws SQLException {
        // Clear existing abilities
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM raven_abilities WHERE owner_id = ?")) {
            statement.setString(1, raven.getOwnerId().toString());
            statement.executeUpdate();
        }

        // Insert abilities
        // In a real implementation, we would iterate through all abilities
        // For simplicity, this is not fully implemented
    }

    private void saveRavenHomes(Raven raven) throws SQLException {
        // Clear existing homes
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM raven_homes WHERE owner_id = ?")) {
            statement.setString(1, raven.getOwnerId().toString());
            statement.executeUpdate();
        }

        // Insert homes
        String ownerIdStr = raven.getOwnerId().toString();

        for (Location location : raven.getHomeLocations()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO raven_homes (owner_id, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, ownerIdStr);
                statement.setString(2, location.getWorld().getName());
                statement.setDouble(3, location.getX());
                statement.setDouble(4, location.getY());
                statement.setDouble(5, location.getZ());
                statement.setFloat(6, location.getYaw());
                statement.setFloat(7, location.getPitch());

                statement.executeUpdate();
            }
        }
    }
}