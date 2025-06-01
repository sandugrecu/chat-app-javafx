package com.sandugrecu.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseConnection {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    static {
        Properties databaseProperties = new Properties();
        try (var input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath");
            }
            databaseProperties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config file", e);
        }

        URL = databaseProperties.getProperty("db.url");
        USERNAME = databaseProperties.getProperty("db.username");
        PASSWORD = databaseProperties.getProperty("db.password");
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, username);
            stmt.setString(2, password); // Should hashs password before storing them in the database

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If a record exists, user is valid
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isUsernameAvailable(String username) {
        String checkSql = "SELECT * FROM Users WHERE username = ?";
        
        try (Connection conn = getConnection();
        		PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

        		checkStmt.setString(1, username);
        		ResultSet rs = checkStmt.executeQuery();
        		
        		if (rs.next()) {
        			System.out.println("Username already exists (DB).");
        			return false;
        		}
        		
        		//username is available
        		return true;
        	}catch (Exception e) {
                e.printStackTrace();
                return false;
        	}
    }
    
    public static boolean createUser(String username, String password) {

        String checkSql = "SELECT * FROM Users WHERE username = ?";
        String insertSql = "INSERT INTO Users (username, password) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            // Check if username already exists
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Username already exists. Please choose another. (DB)");
                return false;
            }

            // Username is unique, proceed with insertion
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // In production, hash the password!
                int rowsInserted = insertStmt.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("User registered successfully. (DB)");
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
		return false;
    }
    
    public static boolean usernameExists(String username) {
        String checkSql = "SELECT * FROM Users WHERE username = ?";
        
        try (Connection conn = getConnection();
        		PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

        		checkStmt.setString(1, username);
        		ResultSet rs = checkStmt.executeQuery();
        		
        		if (rs.next()) {
        			System.out.println("Username exists. (DB)");
        			return true;
        		}
        		//username doesnt exist
        		return false;
        	}catch (Exception e) {
                e.printStackTrace();
                return true;
        	}
    }
    
    public static String addContact(String user1, String user2) throws ClassNotFoundException {
        if (!usernameExists(user1)) {
            return "ADD_CONTACT_FAIL:" + user1 + ": does not exit";
        }

        if (!usernameExists(user2)) {
            return "ADD_CONTACT_FAIL:" + user2 + ": doe not exist";
        }

        int user1ID = getUserIdByUsername(user1);
        int user2ID = getUserIdByUsername(user2);

        try (Connection conn = getConnection()) {
            // First, check if the contact already exists
            String checkSQL = "SELECT * FROM CONTACTS WHERE UserID = ? AND ContactUserID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setInt(1, user1ID);
            checkStmt.setInt(2, user2ID);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return "ADD_CONTACT_FAIL:" + user2 + ": already added";
            }

            // Insert both directions
            String insertSQL = "INSERT INTO CONTACTS (UserID, ContactUserID) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL);

            insertStmt.setInt(1, user1ID);
            insertStmt.setInt(2, user2ID);
            insertStmt.executeUpdate();

            insertStmt.setInt(1, user2ID);
            insertStmt.setInt(2, user1ID);
            insertStmt.executeUpdate();
            return "ADD_CONTACT_SUCCESS";
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "ADD_CONTACT_FAIL:SQL_Error";
        }
    }

    // Get list of contact IDs for a user
    // Updated helper method to return contacts ordered by last message timestamp
    public static List<Integer> getContacts(int userId) throws ClassNotFoundException {
        List<Integer> contactIds = new ArrayList<>();
        String sql = """
            SELECT c.ContactUserID, MAX(m.date) AS LastMessageTime
		    FROM Contacts c
		    LEFT JOIN Message m ON (
		        (m.SenderID = ? AND m.ReceiverID = c.ContactUserID)
		        OR (m.SenderID = c.ContactUserID AND m.ReceiverID = ?)
		    )
		    WHERE c.UserID = ?
		    GROUP BY c.ContactUserID
		    ORDER BY MAX(m.date) DESC
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("[INFO] No contacts found for UserID: " + userId + " (DB)");
            }

            while (rs.next()) {
                contactIds.add(rs.getInt("ContactUserID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contactIds;
    }

    public static ArrayList<String> getContacts(String username) throws ClassNotFoundException {
        ArrayList<String> contactNames = new ArrayList<>();
        int userID = getUserIdByUsername(username);

        List<Integer> contactIDs = getContacts(userID);

        for (int contactID : contactIDs) {
            String contactUsername = getUserNameById(contactID);
            contactNames.add(contactUsername);
        }
        return contactNames;
    }

    // Get username by user ID
    public static String getUserNameById(int userId) throws ClassNotFoundException {
        String userName = "";
        String sql = "SELECT username FROM Users WHERE UserID = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                userName = rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return userName;
    }
    
    // Get the user ID by username (used to pass to the ContactsController)
    public static int getUserIdByUsername(String username) throws ClassNotFoundException {
        String sql = "SELECT UserID FROM Users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UserID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if no user found (error case)
    }
    
    public static ArrayList<String> getMessagesBetweenUsers(String userA, String userB) throws ClassNotFoundException {
        ArrayList<String> messages = new ArrayList<>();
        String query = "SELECT senderID, receiverID, message FROM Message WHERE " +
                       "(senderID = ? AND receiverID = ?) OR (senderID = ? AND receiverID = ?) ORDER BY date ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int userAID = getUserIdByUsername(userA);
            int userBID = getUserIdByUsername(userB);

            stmt.setInt(1, userAID);
            stmt.setInt(2, userBID);
            stmt.setInt(3, userBID);
            stmt.setInt(4, userAID);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int senderId = rs.getInt("senderID");
                String messageContent = rs.getString("message");

                // Convert senderID back to username
                String senderUsername = getUserNameById(senderId);
                messages.add(senderUsername + ": " + messageContent);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public static void storeMessage(String fromUser, String toUser, String message) {
        String insertSql = "INSERT INTO Message (senderID, receiverID, message, date) VALUES (?, ?, ?, GETDATE())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            int senderId = getUserIdByUsername(fromUser);
            int receiverId = getUserIdByUsername(toUser);

            if (senderId == -1 || receiverId == -1) {
                System.out.println("[ERROR] Invalid sender or receiver username. (DB)");
                return;
            }

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, message);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("[INFO] Message stored successfully. (DB)");
            } else {
                System.out.println("[ERROR] Message not stored. (DB)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
