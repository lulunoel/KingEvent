package org.kingfight.kingevent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.kingfight.kingevent.command.classement;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public final class KingEvent extends JavaPlugin implements Listener {

    public Connection connection;
    public String host, database, username, password, table_player, table_faction;
    public int port, ticks, points;

    @Override
    public void onEnable() {
        // Chargement de la configuration du plugin
        loadConfig();
        // Connexion à la base de données
        connectToDatabase();

        // Récupération des messages depuis la configuration
        String permissionMessage = getConfig().getString("messages.permission").replace("&", "§");
        String usageMessage = getConfig().getString("messages.usage").replace("&", "§");
        String invalidNumberMessage = getConfig().getString("messages.invalidNumber").replace("&", "§");
        String addPointsMessage = getConfig().getString("messages.addPoints").replace("&", "§");
        String removePointsMessage = getConfig().getString("messages.removePoints").replace("&", "§");
        String addFactionPoints = getConfig().getString("messages.addFactionPoints").replace("&", "§");
        String removeFactionPoints = getConfig().getString("messages.removeFactionPoints").replace("&", "§");
        String resetPointsMessage = getConfig().getString("messages.resetPoints").replace("&", "§");
        String resetFactionPoints = getConfig().getString("messages.resetFactionPoints").replace("&", "§");

        // Configuration de la commande /classement
        Objects.requireNonNull(getCommand("classement")).setExecutor(new classement(this, connection, table_player, table_faction, permissionMessage, usageMessage, invalidNumberMessage, addPointsMessage, removePointsMessage, addFactionPoints, removeFactionPoints, resetPointsMessage, resetFactionPoints));
        Objects.requireNonNull(getCommand("classement")).setPermission("classement.classement");
        Objects.requireNonNull(getCommand("classement")).setPermissionMessage(permissionMessage);

        // Création des tables si elles n'existent pas
        createTablesIfNotExists();
        ticks = getConfig().getInt("config.ticks");
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Expansion_classement(this, connection, table_faction, table_player).register();
        }
        getServer().getPluginManager().registerEvents(this, this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            readFactionsJSON();
            readPlayersJSON();
        }, 0L, ticks * 60L); // 20 ticks par seconde * 60 secondes = 1 minute

    }


    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        host = getConfig().getString("database.host");
        port = getConfig().getInt("database.port");
        database = getConfig().getString("database.database");
        username = getConfig().getString("database.username");
        password = getConfig().getString("database.password");
        table_player = getConfig().getString("database.table_player");
        table_faction = getConfig().getString("database.table_faction");

    }

    private void createTablesIfNotExists() {
        try {
            String queryPlayer = "CREATE TABLE IF NOT EXISTS " + table_player + " (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "pseudo VARCHAR(50)," +
                    "uuid VARCHAR(36)," +
                    "points INT(11)" +
                    ")";
            connection.createStatement().executeUpdate(queryPlayer);
            getLogger().info("Table " + table_player + " crée ou déja existante.");

            String queryFaction = "CREATE TABLE IF NOT EXISTS " + table_faction + " (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "nom VARCHAR(50)," +
                    "membres TEXT," +
                    "points_total INT(11)" +
                    ")";
            connection.createStatement().executeUpdate(queryFaction);
            getLogger().info("Table " + table_faction + " crée ou déja existante.");
        } catch (SQLException e) {
            getLogger().severe("Erreur lors de la création de la table " + table_faction + " ! Erreur: " + e.getMessage());
        }
    }

    private void connectToDatabase() {
        String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            connection = DriverManager.getConnection(connectionString, username, password);
            getLogger().info("Connexion établie!");
        } catch (SQLException e) {
            getLogger().severe("Erreur lors de la connection! Erreur: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Vérifier si le joueur existe dans la base de données
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table_player + " WHERE uuid=?");
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            points = getConfig().getInt("config.points");

            if (!result.next()) {
                // Le joueur n'existe pas, on l'ajoute à la base de données
                PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO " + table_player + " (uuid, pseudo, points) VALUES (?, ?, " + points + ")");
                insertStatement.setString(1, uuid.toString());
                insertStatement.setString(2, playerName);
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().severe("Erreur lors de la vérification du joueur! Erreur: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            getLogger().severe("Failed to close database connection! Error: " + e.getMessage());
        }
    }

    private void readFactionsJSON() {
        File pluginFolder = getDataFolder().getParentFile(); // Obtient le répertoire parent du plugin
        File jsonFile = new File(pluginFolder, "Factions/factions.json");

        try {
            // Vérifie si le fichier JSON existe
            if (!jsonFile.exists()) {
                getLogger().warning("Le fichier JSON factions n'existe pas.");
                return;
            }

            // Lecture du fichier JSON
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(jsonFile);
            JsonElement jsonElement = parser.parse(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Parcourir toutes les factions
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String factionId = entry.getKey();
                JsonObject factionData = entry.getValue().getAsJsonObject();
                String tag = factionData.get("tag").getAsString();
                int id = factionData.get("id").getAsInt();

                try {
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table_faction + " WHERE id=?");
                    statement.setString(1, String.valueOf(id));
                    ResultSet result = statement.executeQuery();

                    if (!result.next()) {
                        // Le joueur n'existe pas, on l'ajoute à la base de données
                        PreparedStatement insertStatement = connection.prepareStatement(
                                "INSERT INTO " + table_faction + " (id, nom, membres, points_total) VALUES (?, ?, null, 0)");
                        insertStatement.setString(1, String.valueOf(id));
                        insertStatement.setString(2, tag);
                        insertStatement.executeUpdate();
                    }
                } catch (SQLException e) {
                    getLogger().severe("Failed to check player in the database! Error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            getLogger().severe("Erreur lors de la lecture du fichier JSON: " + e.getMessage());
        }
    }
    private void readPlayersJSON() {
        File pluginFolder = getDataFolder().getParentFile(); // Obtient le répertoire parent du plugin
        File jsonFile = new File(pluginFolder, "Factions/players.json");

        try {
            // Vérifie si le fichier JSON existe
            if (!jsonFile.exists()) {
                getLogger().warning("Le fichier JSON players n'existe pas.");
                return;
            }

            // Lecture du fichier JSON
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(jsonFile);
            JsonElement jsonElement = parser.parse(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Initialisation de la map pour stocker les membres de chaque faction
            Map<String, List<String>> factionsMap = new HashMap<>();

            // Parcourir tous les joueurs
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                JsonObject playerData = entry.getValue().getAsJsonObject();
                String name = playerData.get("name").getAsString();
                String faction = playerData.get("factionId").getAsString();

                // Ajouter le joueur à la liste des membres de sa faction
                factionsMap.computeIfAbsent(faction, k -> new ArrayList<>()).add(name);
            }

            // Ajouter les membres à la base de données pour chaque faction
            for (Map.Entry<String, List<String>> entry : factionsMap.entrySet()) {
                String faction = entry.getKey();
                List<String> members = entry.getValue();
                addMembersToDatabase(faction, members);
            }

        } catch (IOException e) {
            getLogger().severe("Erreur lors de la lecture du fichier JSON: " + e.getMessage());
        }
    }

    private void addMembersToDatabase(String faction, List<String> members) {
        try {
            // Créer une chaîne séparée par des virgules pour les membres
            String membersString = String.join(",", members);

            // Exécutez la requête SQL pour mettre à jour la table "Factions"
            PreparedStatement statementDelete = connection.prepareStatement("UPDATE " + table_faction + " SET membres = NULL WHERE id = ?");
            statementDelete.setString(1, faction);
            statementDelete.executeUpdate();

            // Exécutez la requête SQL pour mettre à jour la table "Factions"
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_faction + " SET membres = ? WHERE id = ?");
            statement.setString(1, membersString);
            statement.setString(2, faction);
            statement.executeUpdate();

        } catch (SQLException e) {
            getLogger().severe("Erreur lors de l'ajout des membres à la base de données: " + e.getMessage());
        }
    }

    public int getFactionId(String factionName) {
        File pluginFolder = getDataFolder().getParentFile(); // Obtient le répertoire parent du plugin
        File jsonFile = new File(pluginFolder, "Factions/factions.json");

        try {
            // Vérifie si le fichier JSON existe
            if (!jsonFile.exists()) {
                getLogger().warning("Le fichier JSON factions n'existe pas.");
                return -1; // Return -1 if the file doesn't exist
            }

            // Lecture du fichier JSON
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(jsonFile);
            JsonElement jsonElement = parser.parse(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Parcourir toutes les factions
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                JsonObject factionData = entry.getValue().getAsJsonObject();
                String tag = factionData.get("tag").getAsString();
                int id = factionData.get("id").getAsInt();

                // If the faction name matches, return its ID
                if (tag.equalsIgnoreCase(factionName)) {
                    return id;
                }
            }
        } catch (IOException e) {
            getLogger().severe("Erreur lors de la lecture du fichier JSON: " + e.getMessage());
        }

        return -1; // Return -1 if the faction name is not found
    }

    public String getFactionName(String factionId) {
        File pluginFolder = getDataFolder().getParentFile(); // Obtient le répertoire parent du plugin
        File jsonFile = new File(pluginFolder, "Factions/factions.json");

        try {
            // Vérifie si le fichier JSON existe
            if (!jsonFile.exists()) {
                getLogger().warning("Le fichier JSON factions n'existe pas.");
                return null; // Retourne null si le fichier n'existe pas
            }

            // Lecture du fichier JSON
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(jsonFile);
            JsonElement jsonElement = parser.parse(reader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Parcourir toutes les factions
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                JsonObject factionData = entry.getValue().getAsJsonObject();
                String tag = factionData.get("tag").getAsString();
                String id = factionData.get("id").getAsString();

                // Si l'ID de la faction correspond, retourne son nom
                if (id.equals(factionId)) {
                    return tag;
                }
            }
        } catch (IOException e) {
            getLogger().severe("Erreur lors de la lecture du fichier JSON: " + e.getMessage());
        }

        return null; // Retourne null si le nom de la faction n'est pas trouvé
    }

}
