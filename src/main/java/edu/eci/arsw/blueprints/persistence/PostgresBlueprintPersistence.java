package edu.eci.arsw.blueprints.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

public class PostgresBlueprintPersistence implements BlueprintPersistence {
    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        try (Connection conn = PostgresDBConnector.getConnection()) {
            String query = "SELECT id FROM blueprints WHERE author = ? AND name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, author);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new BlueprintNotFoundException("Blueprint not found: " + author + "/" + name);
            }
            int blueprintId = rs.getInt("id");
            String insertPoint = "INSERT INTO points (blueprint_id, x, y) VALUES (?, ?, ?)";
            PreparedStatement stmtPoint = conn.prepareStatement(insertPoint);
            stmtPoint.setInt(1, blueprintId);
            stmtPoint.setInt(2, x);
            stmtPoint.setInt(3, y);
            stmtPoint.executeUpdate();
        } catch (SQLException e) {
            throw new BlueprintNotFoundException("Error adding point: " + e.getMessage());
        }
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        try (Connection conn = PostgresDBConnector.getConnection()) {
            // Insertar blueprint
            String insertBlueprint = "INSERT INTO blueprints (author, name) VALUES (?, ?) RETURNING id";
            PreparedStatement stmt = conn.prepareStatement(insertBlueprint);
            stmt.setString(1, bp.getAuthor());
            stmt.setString(2, bp.getName());
            ResultSet rs = stmt.executeQuery();

            int blueprintId = -1;
            if (rs.next()) {
                blueprintId = rs.getInt("id");
            }

            // Insertar puntos
            String insertPoint = "INSERT INTO points (blueprint_id, x, y) VALUES (?, ?, ?)";
            for (Point p : bp.getPoints()) {
                PreparedStatement stmtPoint = conn.prepareStatement(insertPoint);
                stmtPoint.setInt(1, blueprintId);
                stmtPoint.setInt(2, p.x());
                stmtPoint.setInt(3, p.y());
                stmtPoint.executeUpdate();
            }

        } catch (SQLException e) {
            throw new BlueprintPersistenceException("Error saving blueprint: " + e.getMessage());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        try (Connection conn = PostgresDBConnector.getConnection()) {
            String query = "SELECT b.id, p.x, p.y " +
                           "FROM blueprints b " +
                           "LEFT JOIN points p ON b.id = p.blueprint_id " +
                           "WHERE b.author = ? AND b.name = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, author);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();

            Blueprint bp = new Blueprint(author, name, new ArrayList<>());
            while (rs.next()) {
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                bp.addPoint(new Point(x, y));
            }
            return bp;

        } catch (SQLException e) {
            throw new BlueprintNotFoundException("Error loading blueprint: " + e.getMessage());
        }
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = new HashSet<>();
        try (Connection conn = PostgresDBConnector.getConnection()) {
            String query = "SELECT b.name, p.x, p.y " +
                           "FROM blueprints b " +
                           "LEFT JOIN points p ON b.id = p.blueprint_id " +
                           "WHERE b.author = ? " +
                           "ORDER BY b.name";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, author);
            ResultSet rs = stmt.executeQuery();

            Blueprint current = null;
            String currentName = null;
            while (rs.next()) {
                String bpName = rs.getString("name");
                if (current == null || !bpName.equals(currentName)) {
                    current = new Blueprint(author, bpName, new ArrayList<>());
                    blueprints.add(current);
                    currentName = bpName;
                }
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                current.addPoint(new Point(x, y));
            }
            return blueprints;

        } catch (SQLException e) {
            throw new BlueprintNotFoundException("Error loading blueprints by author: " + e.getMessage());
        }
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        Set<Blueprint> blueprints = new HashSet<>();
        try (Connection conn = PostgresDBConnector.getConnection()) {
            String query = "SELECT b.author, b.name, p.x, p.y " +
                           "FROM blueprints b " +
                           "LEFT JOIN points p ON b.id = p.blueprint_id " +
                           "ORDER BY b.author, b.name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            Blueprint current = null;
            String currentAuthor = null, currentName = null;
            while (rs.next()) {
                String author = rs.getString("author");
                String name = rs.getString("name");
                if (current == null || !author.equals(currentAuthor) || !name.equals(currentName)) {
                    current = new Blueprint(author, name, new ArrayList<>());
                    blueprints.add(current);
                    currentAuthor = author;
                    currentName = name;
                }
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                current.addPoint(new Point(x, y));
            }
            return blueprints;

        } catch (SQLException e) {
            // getAllBlueprints no lanza excepción según la interfaz
            return blueprints;
        }
    }
}
