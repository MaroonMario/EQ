package org.example.dao;

import org.example.userList.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class Dao {
    private static final String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/db_eq?user=root＆password=root";
    private static Connection conn = null;
    private static Dao dao = null;

    private Dao() {
        try {
            Class.forName(driver);
            if (!DataBaseExists()) {
                conn = DriverManager.getConnection(url + ";create=true");
                createTable();
            } else
                conn = DriverManager.getConnection(url);
            addDefUser();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据库连接异常，或者本软件已经运行。");
            System.exit(0);
        }
    }
    private boolean DataBaseExists() {
        boolean bExists = false;
        File dbFileDir = new File("db_EQ");
        if (dbFileDir.exists()) {
            bExists = true;
        }
        return bExists;
    }

    public static Dao getDao() {
        if (dao == null)
            dao = new Dao();
        return dao;
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<User>();
        try {
            String sql = "select * from tb_users";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                User user = new User();
                user.setIp(rs.getString(1));
                user.setHost(rs.getString(2));
                user.setName(rs.getString(3));
                user.setTipText(rs.getString(4));
                user.setIcon(rs.getString(5));
                users.add(user);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public User getUser(String ip) {
        String sql = "select * from tb_users where ip=?";
        User user = null;
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, ip);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setIp(rs.getString(1));
                user.setHost(rs.getString(2));
                user.setName(rs.getString(3));
                user.setTipText(rs.getString(4));
                user.setIcon(rs.getString(5));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public void addUser(User user) {
        try {
            String sql = "insert into tb_users values(?,?,?,?,?)";
            PreparedStatement ps = null;
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getIp());
            ps.setString(2, user.getHost());
            ps.setString(3, user.getName());
            ps.setString(4, user.getTipText());
            ps.setString(5, user.getIcon());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            String sql = "update tb_users set host=?,name=?,tooltip=?,icon=? where ip='"
                    + user.getIp() + "'";
            PreparedStatement ps = null;
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getHost());
            ps.setString(2, user.getName());
            ps.setString(3, user.getTipText());
            ps.setString(4, user.getIcon());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delUser(User user) {
        try {
            String sql = "delete from tb_users where ip=?";
            PreparedStatement ps = null;
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getIp());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLocation(Rectangle location) {
        String sql = "update tb_location set xLocation=?,yLocation=?,width=?,height=?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, location.x);
            pst.setInt(2, location.y);
            pst.setInt(3, location.width);
            pst.setInt(4, location.height);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Rectangle getLocation() {
        Rectangle rec = new Rectangle(100, 0, 240, 500);
        String sql = "select * from tb_location";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                rec.x = rs.getInt(1);
                rec.y = rs.getInt(2);
                rec.width = rs.getInt(3);
                rec.height = rs.getInt(4);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rec;
    }

    public void createTable() {
        String createUserSql = "CREATE TABLE tb_users ("
                + "ip varchar(16) primary key," + "host varchar(30),"
                + "name varchar(20)," + "tooltip varchar(50),"
                + "icon varchar(50))";
        String createLocationSql = "CREATE TABLE tb_location ("
                + "xLocation int," + "yLocation int," + "width int,"
                + "height int)";
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(createUserSql);
            stmt.execute(createLocationSql);
            addDefLocation();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDefUser() {// 创建本机用户
        try {
            InetAddress local = InetAddress.getLocalHost();
            User user = new User();
            user.setIp(local.getHostAddress());
            user.setHost(local.getHostName());
            user.setName(local.getHostName());
            user.setTipText(local.getHostAddress());
            user.setIcon("1.gif");
            if (getUser(user.getIp()) == null) {
                addUser(user);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void addDefLocation() {
        String sql = "insert into tb_location values(?,?,?,?)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, 100);
            pst.setInt(2, 0);
            pst.setInt(3, 240);
            pst.setInt(4, 500);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}