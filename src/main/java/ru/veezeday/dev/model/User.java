package ru.veezeday.dev.model;

import org.springframework.stereotype.Component;
import ru.veezeday.dev.dao.LoginException;
import ru.veezeday.dev.dao.RegisterException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ru.veezeday.dev.dao.DataAccessObject.*;

@Component
public class User {
    private int id;
    private String name;
    private String password;
    private List<Integer> friendList;
    private List<Chat> chatList;
    private List<Notify> notifyList;

    public static User get(int id) {
        User user = new User();
        String name = null;
        List<Integer> friendList = null;
        List<Chat> chatList = null;
        List<Notify> notifyList = null;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Set name.
            statement = connection.prepareStatement("SELECT * FROM user WHERE id=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                name = resultSet.getString("name");
            }

            //Set friendList 1 row.
            statement = connection.prepareStatement("SELECT * FROM userfriends WHERE userId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            friendList = new ArrayList<Integer>();
            while (resultSet.next()) {
                int friendId = resultSet.getInt("friendId");
                if (friendId!=id) {
                    friendList.add(friendId);
                }
            }

            //Set friendList 2 row.
            statement = connection.prepareStatement("SELECT * FROM userfriends WHERE friendId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int friendId = resultSet.getInt("userId");
                if (friendId!=id) {
                    if (!friendList.contains(friendId)) {
                        friendList.add(friendId);
                    }
                }
            }

            //Set chatList.
            statement = connection.prepareStatement("SELECT * FROM chatusers WHERE userId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            chatList = new ArrayList<Chat>();
            while (resultSet.next()) {
                int chatId = resultSet.getInt("chatId");
                Chat chat = Chat.get(chatId);
                chatList.add(chat);
            }

            //Set notifyList
            statement = connection.prepareStatement("SELECT * FROM notify WHERE recipientId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            notifyList = new ArrayList<Notify>();
            while (resultSet.next()) {
                int notifyId;
                notifyId = resultSet.getInt("id");

                Notify notify = Notify.get(notifyId);
                notifyList.add(notify);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        user.setId(id);
        user.setName(name);
        user.setFriendList(friendList);
        user.setNotifyList(notifyList);
        user.setChatList(chatList);
        return user;
    }

    public static User get(String name, String password) throws LoginException {
        User user = new User();
        String passwordDB;
        int id = -1;
        List<Integer> friendList = null;
        List<Chat> chatList = null;
        List<Notify> notifyList = null;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is there such the pair name and password
            statement = connection.prepareStatement("SELECT * FROM user WHERE name=?");
            statement.setString(1, name);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                passwordDB = resultSet.getString("password");
                if (!password.equals(passwordDB)) {
                    throw new LoginException(
                            "Wrong name or password."
                    );
                }
            } else throw new LoginException(
                    "Wrong name or password."
            );

            //Set id.
            id = resultSet.getInt("id");

            //Set friendList 1 row.
            statement = connection.prepareStatement("SELECT * FROM userfriends WHERE userId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            friendList = new ArrayList<Integer>();
            while (resultSet.next()) {
                int friendId = resultSet.getInt("friendId");
                if (friendId!=id) {
                    friendList.add(friendId);
                }
            }

            //Set friendList 2 row.
            statement = connection.prepareStatement("SELECT * FROM userfriends WHERE friendId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int friendId = resultSet.getInt("userId");
                if (friendId!=id) {
                    if (!friendList.contains(friendId)) {
                        friendList.add(friendId);
                    }
                }
            }

            //Set chatList.
            statement = connection.prepareStatement("SELECT * FROM chatusers WHERE userId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            chatList = new ArrayList<Chat>();
            while (resultSet.next()) {
                int chatId = resultSet.getInt("chatId");
                Chat chat = Chat.get(chatId);
                chatList.add(chat);
            }

            //Set notifyList.
            statement = connection.prepareStatement("SELECT * FROM notify WHERE recipientId=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            notifyList = new ArrayList<Notify>();
            while (resultSet.next()) {
                int notifyId;
                notifyId = resultSet.getInt("id");
                Notify notify = Notify.get(notifyId);
                notifyList.add(notify);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        user.setId(id);
        user.setName(name);
        user.setFriendList(friendList);
        user.setNotifyList(notifyList);
        user.setChatList(chatList);

        return user;
    }

    public static void create(User user) throws RegisterException {
        String name;
        String password;

        name = user.getName();
        password = user.getPassword();

        try {
            //Is name or password are null or empty string
            if (name == null || password == null || name.equals("") || password.equals(""))
                throw new RegisterException("The name are empty.");

            //Is name:
            //Only contains alphanumeric characters, underscore and dot.
            //Underscore and dot can't be at the end or start of a username (e.g _username / username_ / .username / username.).
            //Underscore and dot can't be next to each other (e.g user_.name).
            //Underscore or dot can't be used multiple times in a row (e.g. user__name / user..name).
            //Number of characters must be between 4 and 20.
            if (!Pattern.matches("^(?=.{4,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$", name))
                throw new RegisterException(
                        "Make sure the name:\n " +
                                "Only contains alphanumeric characters, underscore and dot.\n" +
                                "Underscore and dot can't be at the end or start of a username (e.g _username / username_ / .username / username.).\n" +
                                "Underscore and dot can't be next to each other (e.g user_.name).\n" +
                                "Underscore or dot can't be used multiple times in a row (e.g. user__name / user..name).\n" +
                                "Number of characters must be between 4 and 20."
                );

            //Is password:
            //At least 8 chars
            //Contains at least one digit
            //Contains at least one lower alpha char and one upper alpha char
            //Contains at least one char within a set of special chars (@#%$^ etc.)
            //Does not contain space, tab, etc.
            if (!Pattern.matches("^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", password))
                throw new RegisterException(
                        "Make sure the password:\n" +
                                "At least 8 chars\n" +
                                "Contains at least one digit\n" +
                                "Contains at least one lower alpha char and one upper alpha char\n" +
                                "Contains at least one char within a set of special chars (@#%$^ etc.)\n" +
                                "Does not contain space, tab, etc."
                );

            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is name already exist.
            statement = connection.prepareStatement("SELECT * FROM user WHERE name=?");
            statement.setString(1, name);
            resultSet = statement.executeQuery();
            if (resultSet.next()) throw new RegisterException(
                    "Such the name already exist."
            );

            //Creating the record.
            statement = connection.prepareStatement("INSERT INTO user (name, password) VALUES (?,?)");
            statement.setString(1, name);
            statement.setString(2, password);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<User> search(User requester, String request) {
        List<User> userList = null;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            statement = connection.prepareStatement("SELECT * FROM user WHERE name=?");
            statement.setString(1, request);
            resultSet = statement.executeQuery();

            userList = new ArrayList<User>();
            while(resultSet.next()) {
                User user = new User();
                int userId;
                String username;

                //Set id.
                userId = resultSet.getInt("id");

                //Skip iteration if searchable is a requester.
                if (userId == requester.getId()) continue;

                //Skip iteration if searchable is requester's friend.
                boolean match = false;
                for (int friendId : requester.getFriendList()) {
                    if (userId == friendId) {
                        match = true;
                        break;
                    }
                }
                if (match) continue;

                //Set other parameters.
                username = resultSet.getString("name");
                user.setId(userId);
                user.setName(username);
                userList.add(user);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public void inviteFriend(User friend) {
        TypeOfNotify typeOfNotify;
        int senderId;
        int recipientId;
        String content;
        Notify userNotify;
        Notify friendNotify;

        //Friend notify
        typeOfNotify = TypeOfNotify.FRIEND_INVITE;
        senderId = this.getId();
        recipientId = friend.getId();
        content = String.format("%s invites you to be friends.", this.getName());
        friendNotify = Notify.create(typeOfNotify, senderId, recipientId, content);

        //User notify
        typeOfNotify = TypeOfNotify.DISINVITE;
        senderId = friend.getId();
        recipientId = this.getId();
        content = String.format("%s has not accepted your invitation to be friends yet.", friend.getName());
        userNotify = Notify.create(typeOfNotify, senderId, recipientId, content);

        friendNotify.editPairId(userNotify.getId());
        userNotify.editPairId(friendNotify.getId());
    }

    public void deleteFriend (User friend) {
        TypeOfNotify typeOfNotify;
        int senderId;
        int recipientId;
        String content;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such friend record exist.
            statement = connection.prepareStatement("SELECT * FROM userfriends WHERE userId=? AND friendId=? OR userId=? AND friendId=?");
            statement.setInt(1, friend.getId());
            statement.setInt(2, this.getId());
            statement.setInt(3, this.getId());
            statement.setInt(4, friend.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "You are not friends."
            );

            //Creating to notify about delete from friends to deleted user.
            typeOfNotify = TypeOfNotify.NOTIFY;
            senderId = this.getId();
            recipientId = friend.getId();
            content = String.format("%s deleted you from friends.", this.getName());
            Notify.create(typeOfNotify, senderId, recipientId, content);


            //Delete friend record.
            statement = connection.prepareStatement("DELETE FROM userfriends WHERE userId=? AND friendId=? OR userId=? AND friendId=?");
            statement.setInt(1, this.getId());
            statement.setInt(2, friend.getId());
            statement.setInt(3, friend.getId());
            statement.setInt(4, this.getId());
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFriendList(List<Integer> friendList) {
        this.friendList = friendList;
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = chatList;
    }

    public void setNotifyList(List<Notify> notifyList) {
        this.notifyList = notifyList;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getFriendList() {
        return friendList;
    }

    public List<Chat> getChatList() {
        return chatList;
    }

    public List<Notify> getNotifyList() {
        return notifyList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
