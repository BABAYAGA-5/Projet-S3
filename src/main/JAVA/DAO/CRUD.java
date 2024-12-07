package DAO;

import Bean.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static DAO.JDBCConnectionManager.url;


public class CRUD
{
    public static UserBean getUserByMassar(String massar) {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt = null;
        String query = "select * from user where massar = ?";
        UserBean user = new UserBean();

        try {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, massar);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setFirst_name(rs.getString("first_name"));
                user.setLast_name(rs.getString("last_name"));
                user.setPassword(rs.getString("password"));
                user.setEmail(massar+"@um5.ac.ma");
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setToken(rs.getString("token"));
                user.setToken_validity(rs.getTimestamp("token_validity"));
                user.setUpdated_at(rs.getTimestamp("updated_at"));
                user.setSexe(rs.getString("sexe"));
                return user;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static UserBean getUserById(int id)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt=null;
        String query = "select * from user where id = ?";
        UserBean user=new UserBean();
        try
        {
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            user.setId(rs.getInt(id));
            user.setLast_name(rs.getString("first_name"));
            user.setFirst_name(rs.getString("last_name"));
            user.setPhone(rs.getString("phone"));
            user.setSexe(rs.getString("sexe"));
            user.setRole(rs.getString("role"));
            user.setMassar(rs.getString("massar"));
            user.setEmail(rs.getString("massar")+"@um5.ac.ma");
            user.setPassword(rs.getString("password"));
            user.setToken(rs.getString("token"));
            user.setToken_validity(rs.getTimestamp("token_validity"));
            user.setUpdated_at(rs.getTimestamp("updated_at"));
            user.setCreated_at(rs.getTimestamp("created_at"));
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error getting user id "+id, e);
        }
        return user;
    }

    
    public static List<UserBean> getAllUsers()
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt = null;
        String query = "select * from user";
        List<UserBean> users = new ArrayList<UserBean>();
        int columnCount = 0;
        try {
            stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                columnCount = metaData.getColumnCount();
                UserBean user = new UserBean();
                user.setId(rs.getInt("id"));
                user.setFirst_name(rs.getString("first_name"));
                user.setLast_name(rs.getString("last_name"));
                user.setPhone(rs.getString("phone"));
                user.setSexe(rs.getString("sexe"));
                user.setRole(rs.getString("role"));
                user.setMassar(rs.getString("massar"));
                user.setEmail(rs.getString("massar")+"@um5.ac.ma");
                user.setPassword(rs.getString("password"));
                user.setToken(rs.getString("token"));
                user.setToken_validity(rs.getTimestamp("token_validity"));
                user.setUpdated_at(rs.getTimestamp("updated_at"));
                user.setCreated_at(rs.getTimestamp("created_at"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all users", e);
        }
        return users;
    }

    
    public static void addUser(UserBean user)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        String query = "SELECT id FROM massar WHERE id=?";
        try(PreparedStatement stmt1 = connection.prepareStatement(query))
        {
            stmt1.setString(1, user.getMassar());
            ResultSet rs = stmt1.executeQuery();
            if (rs.next())
            {
                query = "INSERT INTO user (first_name, last_name, phone, sexe, role, massar, password) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt2 = connection.prepareStatement(query))
                {
                    stmt2.setString(1, user.getFirst_name());
                    stmt2.setString(2, user.getLast_name());
                    stmt2.setString(3, user.getPhone());
                    stmt2.setString(4, user.getSexe());
                    stmt2.setString(5, user.getRole());
                    stmt2.setString(6, user.getMassar());
                    stmt2.setString(7, user.getPassword());
                    stmt2.executeUpdate();
                }
                catch (SQLException e)
                {
                    throw new RuntimeException("Error adding user", e);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    
    public static void updateUser(UserBean user)
    {

    }

    
    public static void deleteUser(int id)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    
    public static AnnonceBean getAnnonceById(int id)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt=null;
        String query = "select * from annonce where id = ?";
        AnnonceBean annonce=new AnnonceBean();
        try
        {
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            annonce.setId(rs.getInt("id"));
            annonce.setId_user(rs.getInt("id_user"));
            annonce.setStatut(rs.getString("statut"));
            annonce.setDate(rs.getDate("date"));
            annonce.setCaption(rs.getString("caption"));
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error getting annonce id "+id, e);
        }
        return annonce;
    }

    
    public static List<AnnonceBean> getAllAnnonce()
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt=null;
        String query = "select * from annonce";
        List<AnnonceBean> annonces=new ArrayList<AnnonceBean>();
        try
        {
            stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                AnnonceBean annonce=new AnnonceBean();
                annonce.setId(rs.getInt("id"));
                annonce.setId_user(rs.getInt("id_user"));
                annonce.setStatut(rs.getString("statut"));
                annonce.setDate(rs.getDate("date"));
                annonce.setCaption(rs.getString("caption"));
                annonces.add(annonce);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error getting annonces");
        }
        return annonces;
    }

    
    public static void addAnnonce(AnnonceBean annonce)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        String query = "INSERT INTO annonce (id_user, statut, date, caption) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.setInt(1, annonce.getId());
            stmt.setString(2,annonce.getStatut());
            stmt.setDate(3,annonce.getDate());
            stmt.setString(4,annonce.getCaption());
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error adding annonce", e);
        }
    }

    
    public static void updateAnnonce(AnnonceBean user) {

    }

    
    public static void deleteAnnonce(int id)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        String query = "DELETE FROM annonce WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query))
        {
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting annonce id"+id ,e);
        }
    }

    
    public static ReclamationBean getReclamationById(int id)
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt=null;
        String query = "select * from reclamation where id = ?";
        ReclamationBean reclamation=new ReclamationBean();
        try
        {
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            reclamation.setId(rs.getInt("id"));
            reclamation.setId_user(rs.getInt("id_user"));
            reclamation.setDescription(rs.getString("description"));
            reclamation.setDate(rs.getDate("date"));
            reclamation.setStatus(rs.getString("statut"));
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error getting reclamation id "+id, e);
        }
        return reclamation;
    }

    
    public static List<ReclamationBean> getAllReclamations()
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt=null;
        String query = "select * from reclamation";
        List<ReclamationBean> reclamations=new ArrayList<ReclamationBean>();
        try
        {
            stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
            {
                ReclamationBean reclamation=new ReclamationBean();
                reclamation.setId(rs.getInt("id"));
                reclamation.setId_user(rs.getInt("id_user"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setDate(rs.getDate("date"));
                reclamation.setStatus(rs.getString("statut"));
                reclamations.add(reclamation);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error getting reclamations", e);
        }
        return reclamations;
    }
    public static void SendResetEmail(String massar) throws Exception {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt = null;
        String token_hash;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime1 = LocalDateTime.now();
        LocalDateTime currentDateTime = currentDateTime1.withNano(0);
        UserBean user = getUserByMassar(massar);

        // Add 30 minutes
        LocalDateTime updatedDateTime = currentDateTime.plusMinutes(30);
        String token=Security.RandomStringGenerator();
        try
        {
            token_hash=Security.hash(token);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        InetAddress localHost = null;
        try {
            // Get the local machine's IP address
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert user != null;
        String message="Dear "+user.getFirst_name()+" "+user.getLast_name()+",\n" +
                "\n" +
                "We received a request to reset your password for your DoctorApp account.\n" +
                "\n" +
                "If you made this request, please follow the link below to reset your password:\n" +
                "\n" +
                "http://localhost:8000/Projet_S3_war_exploded/ResetpassServlet?token_hash="+token_hash+"&id="+user.getId()+
                "\n" +
                "If you did not request a password reset, please ignore this email. Your password will remain unchanged.\n" +
                "\n" +
                "For security purposes, this link will expire in 30 minutes.\n" +
                "\n"+
                "Thank you,\n" +
                "The DoctorApp Team";
        String subject = "Password Reset Request for ParaEnsias";
        try {
            EmailSender mail=new EmailSender();
        } catch (Exception e)
        {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        EmailSender.SendMail(user.getEmail(),subject,message);

        String query = "update user set token=? ,token_validity=? ,updated_at=? where massar=?";

        try
        {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, token_hash);
            stmt.setString(2, updatedDateTime.format(formatter));
            stmt.setString(3, currentDateTime.format(formatter));
            stmt.setString(4, massar);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("Error sending email");
            throw new RuntimeException(e);
        }
    }
    public static void updatePassword(int id, String password) throws Exception
    {
        JDBCConnectionManager DAO = new JDBCConnectionManager();
        Connection connection = DAO.getConnection();
        PreparedStatement stmt = null;
        String token_hash;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = LocalDateTime.now().withNano(0).format(formatter);

        String query = "update user set password=? ,updated_at=? where id=?";
        String password_hash=Security.hash(password);

        try
        {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, password_hash);
            stmt.setString(2, currentDateTime.toString());
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static void addReclamation(ReclamationBean reclamation) {

    }

    
    public static void updateReclamation(ReclamationBean reclamation) {

    }

    
    public static void deleteReclamation(int id) {

    }

    
    public static FinancementBean getFinancementById(int id) {
        return null;
    }

    
    public static List<FinancementBean> getAllFinancements() {
        return List.of();
    }

    
    public static void addFinancement(FinancementBean financement) {

    }

    
    public static void updateFinancement(FinancementBean financement) {

    }

    
    public static void deleteFinancement(int id) {

    }

    
    public static DepensesBean getDepensesById(int id) {
        return null;
    }

    
    public static List<DepensesBean> getAllDepenses() {
        return List.of();
    }

    
    public static void addDepenses(DepensesBean depenses) {

    }

    
    public static void updateDepenses(DepensesBean depenses) {

    }

    
    public static void deleteDepenses(int id) {

    }

    
    public static ClubBean getClubById(int id) {
        return null;
    }

    
    public static List<ClubBean> getAllClubs() {
        return List.of();
    }

    
    public static void addClub(ClubBean club) {

    }

    
    public static void updateClub(ClubBean club) {

    }

    
    public static void deleteClub(int id) {

    }
}
