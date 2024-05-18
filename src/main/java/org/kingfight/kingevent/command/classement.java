package org.kingfight.kingevent.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.kingfight.kingevent.KingEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;


public class classement implements CommandExecutor {
    public final Connection connection;
    public String table_player, table_faction, permissionMessage, usageMessage, invalidNumberMessage, addPointsMessage, removePointsMessage, addFactionPointsMessage, removeFactionPointsMessage, resetPointsMessage, resetFactionPointsMessage;
    private KingEvent kingEvent;

    public classement(KingEvent kingEvent, Connection connection, String tablePlayer, String table_faction, String permissionMessage, String usageMessage, String invalidNumberMessage, String addPointsMessage, String removePointsMessage, String addFactionPointsMessage, String removeFactionPointsMessage, String resetPointsMessage, String resetFactionPointsMessage) {
        this.connection = connection;
        this.table_player = tablePlayer;
        this.permissionMessage = permissionMessage;
        this.usageMessage = usageMessage;
        this.invalidNumberMessage = invalidNumberMessage;
        this.addPointsMessage = addPointsMessage;
        this.removePointsMessage = removePointsMessage;
        this.addFactionPointsMessage = addFactionPointsMessage;
        this.removeFactionPointsMessage = removeFactionPointsMessage;
        this.resetPointsMessage = resetPointsMessage;
        this.table_faction = table_faction;
        this.resetFactionPointsMessage = resetFactionPointsMessage;
        this.kingEvent = kingEvent;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(usageMessage);
            return false;
        }

        String type = args[0].toLowerCase();
        String action = args[1].toLowerCase();

        if (!(type.equals("player") || type.equals("faction"))) {
            sender.sendMessage(usageMessage);
            return false;
        }

        if (!(action.equals("give") || action.equals("remove") || action.equals("reset"))) {
            sender.sendMessage(usageMessage);
            return false;
        }

        if (args.length < 3 && !action.equals("reset")) {
            sender.sendMessage(usageMessage);
            return false;
        }

        if (action.equals("reset")) {
            if (type.equals("player")) {
                resetPoints();
                sender.sendMessage(resetPointsMessage);
                return true;
            } else if (type.equals("faction")) {
                resetFactionPoints();
                sender.sendMessage(resetFactionPointsMessage);
                return true;
            }
        }

        String target = args[2];
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(invalidNumberMessage);
            return false;
        }

        if (type.equals("player")) {
            if (action.equals("give")) {
                addPlayerPoints(target, amount);
                String message = addPointsMessage.replace("{amount}", String.valueOf(amount))
                        .replace("{player}", target);
                sender.sendMessage("Player: " + message);
                return true;
            } else if (action.equals("remove")) {
                removePlayerPoints(target, amount);
                String message = removePointsMessage.replace("{amount}", String.valueOf(amount))
                        .replace("{player}", target);
                sender.sendMessage("Player: " + message);
                return true;
            }
        } else if (type.equals("faction")) {
            if (action.equals("give")) {
                addFactionPoints(target, amount);
                String message = addFactionPointsMessage.replace("{amount}", String.valueOf(amount))
                        .replace("{faction}", target);
                sender.sendMessage("Faction: " + message);
                return true;
            } else if (action.equals("remove")) {
                removeFactionPoints(target, amount);
                String message = removeFactionPointsMessage.replace("{amount}", String.valueOf(amount))
                        .replace("{faction}", target);
                sender.sendMessage("Faction: " + message);
                return true;
            }
        }

        return false;
    }

    private void addPlayerPoints(String player, int amount) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_player + " SET points = points + ? WHERE pseudo = ?");
            statement.setInt(1, amount);
            statement.setString(2, player);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removePlayerPoints(String player, int amount) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_player + " SET points = points - ? WHERE pseudo = ?");
            statement.setInt(1, amount);
            statement.setString(2, player);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetPoints() {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_player + " SET points = 0");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addFactionPoints(String faction, int amount) {
        try {
            int factionId = kingEvent.getFactionId(faction);
            //getLogger().info("Faction ID: " + factionId); // Log factionId
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_faction + " SET points_total = points_total + ? WHERE id = ?");
            statement.setInt(1, amount);
            statement.setString(2, String.valueOf(factionId));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeFactionPoints(String faction, int amount) {
        try {
            int factionId = kingEvent.getFactionId(faction);
            getLogger().info("Faction ID: " + factionId); // Log factionId
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_faction + " SET points_total = points_total - ? WHERE id = ?");
            statement.setInt(1, amount);
            statement.setString(2, String.valueOf(factionId));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void resetFactionPoints() {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + table_faction + " SET points_total = 0");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
