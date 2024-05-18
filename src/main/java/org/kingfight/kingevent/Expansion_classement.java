package org.kingfight.kingevent;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Expansion_classement extends PlaceholderExpansion {

    public final Connection connection;
    public String table_faction;
    public String table_player;
    private KingEvent kingEvent;

    public Expansion_classement(KingEvent kingEvent, Connection connection, String table_faction, String table_player) {
        this.connection = connection;
        this.table_faction = table_faction;
        this.table_player = table_player;
        this.kingEvent = kingEvent;

    }

    @Override
    public String getIdentifier() {
        return "classement";
    }

    @Override
    public String getAuthor() {
        return "Lulunoel2016";
    }


    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        switch (identifier) {
            case "top_1":
                return getTopName(1);
            case "top_2":
                return getTopName(2);
            case "top_3":
                return getTopName(3);
            case "top_4":
                return getTopName(4);
            case "top_5":
                return getTopName(5);
            case "top_6":
                return getTopName(6);
            case "top_7":
                return getTopName(7);
            case "top_8":
                return getTopName(8);
            case "top_9":
                return getTopName(9);
            case "top_10":
                return getTopName(10);
        }
        switch (identifier) {
            case "points_top_1":
                return getTopPoints(1);
            case "points_top_2":
                return getTopPoints(2);
            case "points_top_3":
                return getTopPoints(3);
            case "points_top_4":
                return getTopPoints(4);
            case "points_top_5":
                return getTopPoints(5);
            case "points_top_6":
                return getTopPoints(6);
            case "points_top_7":
                return getTopPoints(7);
            case "points_top_8":
                return getTopPoints(8);
            case "points_top_9":
                return getTopPoints(9);
            case "points_top_10":
                return getTopPoints(10);
        }
        switch (identifier) {
            case "membres_top_1":
                return getTopMembres(1);
            case "membres_top_2":
                return getTopMembres(2);
            case "membres_top_3":
                return getTopMembres(3);
            case "membres_top_4":
                return getTopMembres(4);
            case "membres_top_5":
                return getTopMembres(5);
            case "membres_top_6":
                return getTopMembres(6);
            case "membres_top_7":
                return getTopMembres(7);
            case "membres_top_8":
                return getTopMembres(8);
            case "membres_top_9":
                return getTopMembres(9);
            case "membres_top_10":
                return getTopMembres(10);
        }

        if (identifier.equals("info_nom")) {
            // Retourner la valeur du placeholder
            return String.valueOf(getfaction(player.getName()));
        }
        if (identifier.equals("info_membres")) {
            // Retourner la valeur du placeholder
            return String.valueOf(getfactionmembres(player.getName()));
        }
        if (identifier.equals("info_points")) {
            // Retourner la valeur du placeholder
            return String.valueOf(getfactionpoints(player.getName()));
        }

        if (identifier.equals("points")) {
            // Retourner la valeur du placeholder
            return String.valueOf(getpoints(player.getName()));
        }

        if (identifier.startsWith("info_player_uuid_")) {
            String uuid = identifier.replace("info_player_uuid_", "");
            String points = getPointsFromUUID(uuid); // Obtenez le nombre de points depuis votre table
            return String.valueOf(points);
        }
        if (identifier.startsWith("info_player_name_")) {
            String Pseudo = identifier.replace("info_player_name_", "");
            String points = String.valueOf(getPlayerPoints(Pseudo)); // Obtenez le nombre de points depuis votre table
            return String.valueOf(points);
        }
        if (identifier.startsWith("info_faction_somme_id_")) {
            String factionId = identifier.substring("info_faction_somme_id_".length());
            return String.valueOf(getFactionPointsSum(factionId));
        }
        if (identifier.startsWith("info_faction_somme_nom_")) {
            String factionname = identifier.substring("info_faction_somme_nom_".length());
            String factionid = String.valueOf(kingEvent.getFactionId(factionname));
            return String.valueOf(getFactionPointsSum(factionid));
        }
        if (identifier.startsWith("name_info_faction_id_")) {
            String factionId = identifier.substring("name_info_faction_id_".length());
            return String.valueOf(kingEvent.getFactionName(factionId));
        }
        if (identifier.startsWith("id_info_faction_nom_")) {
            String factionname = identifier.substring("id_info_faction_nom_".length());
            return String.valueOf(kingEvent.getFactionId(factionname));
        }
        if (identifier.startsWith("info_faction_points_id_")) {
            String factionId = identifier.substring("info_faction_points_id_".length());
            return String.valueOf(getFactionPoints(factionId));
        }
        if (identifier.startsWith("info_faction_points_nom_")) {
            String factionname = identifier.substring("info_faction_points_nom_".length());
            String factionid = String.valueOf(kingEvent.getFactionId(factionname));
            return String.valueOf(getFactionPoints(factionid));
        }
        return null;
    }

    private String getPointsFromUUID(String uuid) {
        String points = "";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT points FROM " + table_player + " WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                points = resultSet.getString("points");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

    private int getFactionPointsSum(String factionId) {
        int totalPoints = 0;
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT membres FROM " + table_faction + " WHERE id = ?");
            statement.setString(1, factionId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String[] members = resultSet.getString("membres").split(",");
                for (String member : members) {
                    totalPoints += Integer.parseInt(getPlayerPointssomme(member.trim()));
                }
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalPoints;
    }
    private String getPlayerPointssomme(String playerName) {
        String points = "0";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT points FROM " + table_player + " WHERE pseudo = ?");
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                points = resultSet.getString("points");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

    private String getPlayerPoints(String playerName) {
        String points = "0";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT points FROM " + table_player + " WHERE pseudo = ?");
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                points = resultSet.getString("points");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }
    private String getFactionPoints(String factionId) {
        String factionpoints = "";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT SUM(points_total) AS total_points FROM " + table_faction + " WHERE id = ?");
            statement.setString(1, factionId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                factionpoints = resultSet.getString("total_points");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionpoints;
    }

    private String getTopName(int position) {
        String factionName = "null";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT nom FROM " + table_faction + " ORDER BY points_total DESC LIMIT ?, 1");
            statement.setInt(1, position - 1);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                factionName = resultSet.getString("nom");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionName;
    }
    private String getTopPoints(int position) {
        String factionpoints = "null";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT points_total FROM " + table_faction + " ORDER BY points_total DESC LIMIT ?, 1");
            statement.setInt(1, position - 1);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                factionpoints = resultSet.getString("points_total");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionpoints;
    }
    private String getTopMembres(int position) {
        StringBuilder members = new StringBuilder();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT membres FROM " + table_faction + " ORDER BY points_total DESC LIMIT ?, 1");
            statement.setInt(1, position - 1);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String[] membersArray = resultSet.getString("membres").split(",");
                for (String member : membersArray) {
                    members.append("&d- &d").append(member).append("\n");
                }
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (members.length() == 0) {
            return null;
        } else {
            return members.toString();
        }
    }

    private String getfaction(String playerName) {
        String factionName = "";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT nom FROM " + table_faction + " WHERE membres LIKE ?");
            statement.setString(1, "%" + playerName + "%");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                factionName = resultSet.getString("nom");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionName;
    }
    private String getfactionmembres(String playerName) {
        StringBuilder members = new StringBuilder();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT membres FROM " + table_faction + " WHERE membres LIKE ?");
            statement.setString(1, "%" + playerName + "%");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String[] membersArray = resultSet.getString("membres").split(",");
                for (String member : membersArray) {
                    members.append("&d- &d").append(member).append("\n");
                }
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (members.length() == 0) {
            return null;
        } else {
            return members.toString();
        }
    }
    private String getfactionpoints(String playerName) {
        String factionpoints = "";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT points_total FROM " + table_faction + " WHERE membres LIKE ?");
            statement.setString(1, "%" + playerName + "%");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                factionpoints = resultSet.getString("points_total");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionpoints;
    }

    private String getpoints(String playerName) {
        String points = "";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT points FROM " + table_player + " WHERE pseudo LIKE ?");
            statement.setString(1, "%" + playerName + "%");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                points = resultSet.getString("points");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return points;
    }

}
