package ru.veezeday.dev.model;

import org.springframework.stereotype.Component;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static ru.veezeday.dev.dao.DataAccessObject.*;

@Component
public class Chat {
    private int id;
    private int adminId;
    private String name;
    private List<Integer> users;
    private List<Message> messages;

    public Chat() {
        messages = new ArrayList<Message>();
    }

    public static Chat get(int chatId) {
        Chat chat = null;
        int adminId = -1;
        String name = null;
        List<Integer> users = null;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            statement = connection.prepareStatement("SELECT * FROM chat WHERE id=?");
            statement.setInt(1, chatId);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new SQLException();
            adminId = resultSet.getInt("adminId");
            name = resultSet.getString("name");

            statement = connection.prepareStatement("SELECT * FROM chatusers WHERE chatId=?");
            statement.setInt(1, chatId);
            resultSet = statement.executeQuery();
            users = new ArrayList<Integer>();
            while (resultSet.next()) {
                int userId = resultSet.getInt("userId");
                users.add(userId);
            }
            chat = new Chat();

            statement = connection.prepareStatement("SELECT * FROM chatcontent WHERE chatId=?");
            statement.setInt(1, chatId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int userId;
                String content;
                Timestamp timestamp;
                Calendar date;

                userId = resultSet.getInt("userId");
                content = resultSet.getString("content");
                timestamp = resultSet.getTimestamp("date");
                date = new GregorianCalendar().getInstance();
                date.setTime(timestamp);
                chat.addMessage(userId, content, date);
            }
            chat.setId(chatId);
            chat.setAdminId(adminId);
            chat.setName(name);
            chat.setUsers(users);
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return chat;
    }

    public static Chat create(int adminId) {
        Chat chat = null;
        int generatedId = -1;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Create new chat
            statement = connection.prepareStatement("INSERT INTO chat SET adminId=?", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, adminId);
            statement.executeUpdate();

            //Get id of generated chat
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                generatedId = resultSet.getInt(1);
            }

            //Add creator into chat
            statement = connection.prepareStatement("INSERT INTO chatusers (chatId, userId) VALUES(?,?)");
            statement.setInt(1, generatedId);
            statement.setInt(2, adminId);
            statement.executeUpdate();
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Chat.get(generatedId);
    }

    public void leave(User user) {
        //Переназначить админа после ухода админа
        //Удалять чат если в нём нет пользователей
        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such chat exist.
            statement = connection.prepareStatement("SELECT * FROM chat WHERE id=?");
            statement.setInt(1, this.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This chat does not exist."
            );

            //Is user in this chat.
            statement = connection.prepareStatement("SELECT * FROM chatusers WHERE chatId=? AND userId=?");
            statement.setInt(1, this.getId());
            statement.setInt(2, user.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "You are not a member of this chat."
            );

            //Creating chat message about leaving chat.
            String content = String.format("%s leaves the chat.",user.getName());
            statement = connection.prepareStatement("INSERT INTO chatcontent (chatId, userId, content) VALUES (?,?,?)");
            statement.setInt(1, this.getId());
            statement.setInt(2, 0);
            statement.setString(3, content);
            statement.executeUpdate();

            //Delete user from chat.
            statement = connection.prepareStatement("DELETE FROM chatusers WHERE chatId=? AND userId=?");
            statement.setInt(1, this.getId());
            statement.setInt(2, user.getId());
            statement.executeUpdate();

            //Is anyone in that chat
            statement = connection.prepareStatement("SELECT * FROM chatusers WHERE chatId=?");
            statement.setInt(1, this.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                this.delete();
            }
        } catch(SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such chat exist.
            statement = connection.prepareStatement("SELECT * FROM chat WHERE id=?");
            statement.setInt(1, this.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This chat does not exist."
            );

            //Deleting chat
            statement = connection.prepareStatement("DELETE FROM chat WHERE id=?");
            statement.setInt(1, this.getId());
            statement.executeUpdate();
        } catch(SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }
    }

    public void changeName(String newName) throws NullPointerException{
        //New name validate.
        if (newName == null) {
            throw new NullPointerException("Chat name can't be empty.");
        }
        String[] name= newName.split(" ");
        StringBuilder newNameBuilder = new StringBuilder(newName);
        for (String s : name) {
            newNameBuilder.append(s);
        }
        if (newNameBuilder.toString().equals("")) {
            throw new NullPointerException("Chat name can't be empty.");
        }


        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such chat exist.
            statement = connection.prepareStatement("SELECT * FROM chat WHERE id=?");
            statement.setInt(1, this.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This chat does not exist."
            );

            //Change chat name
            statement = connection.prepareStatement("UPDATE chat SET name=? WHERE id=?");
            statement.setString(1, newName);
            statement.setInt(2, this.getId());
            statement.executeUpdate();
        } catch(SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }
    }

    public void setAdmin(int userId) {
        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such chat exist.
            statement = connection.prepareStatement("SELECT * FROM chat WHERE id=?");
            statement.setInt(1, this.getId());
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This chat does not exist."
            );

            //Change admin of the chat
            statement = connection.prepareStatement("UPDATE chat SET adminId=? WHERE id=?");
            statement.setInt(1, userId);
            statement.setInt(2, this.getId());
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }
    }

    public void inviteUser(int invitedId, int userId) {
        TypeOfNotify typeOfNotify;
        int senderId;
        int recipientId;
        String content;
        User user = User.get(userId);
        User invited = User.get(invitedId);
        Notify invitedNotify;
        Notify userNotify;

        //Creating to recipient notify about invite.
        typeOfNotify = TypeOfNotify.CHAT_INVITE;
        senderId = userId;
        recipientId = invitedId;
        content = String.format("%s invites you to the %s chat.", user.getName(), this.getName());
        invitedNotify = Notify.create(typeOfNotify, senderId, recipientId, this.id, content);

        //Creating to sender the able to dismiss the invite.
        typeOfNotify = TypeOfNotify.DISINVITE;
        senderId = invitedId;
        recipientId = userId;
        content = String.format("%s has not accepted your invitation to the %s chat yet.", invited.getName(), this.getName());
        userNotify = Notify.create(typeOfNotify, senderId, recipientId, this.id, content);

        invitedNotify.editPairId(userNotify.getId());
        userNotify.editPairId(invitedNotify.getId());
    }

    public void deleteChatUser(int userId) {
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

            //Is such user in that chat.
            statement = connection.prepareStatement("SELECT * FROM chatusers WHERE chatId=? AND userId=?");
            statement.setInt(1, this.id);
            statement.setInt(2, userId);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This user is not in that chat."
            );

            //Delete user from the chat.
            statement = connection.prepareStatement("DELETE FROM chatusers WHERE chatId=? AND userId=?");
            statement.setInt(1, this.id);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }

        typeOfNotify = TypeOfNotify.NOTIFY;
        senderId = this.adminId;
        recipientId = userId;
        content = String.format("You was deleted from %s chat by the admin.", this.name);
        Notify.create(typeOfNotify, senderId, recipientId, this.id, content);
    }

    public void addMessage(int userId, String content, Calendar date) {
        messages.add(new Message(userId,content,date));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getName() {
        String name;
        if (this.name==null || this.name.equals("")) {
            StringBuilder sb = new StringBuilder();
            for (int i : users) {
                String userName = User.get(i).getName();
                sb.append(userName);
                sb.append(", ");
            }
            int length = sb.length();
            sb.delete(length-2, length-1);
            name = sb.toString();
        } else {
            name = this.name;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getUsers() {
        return users;
    }

    public void setUsers(List<Integer> users) {
        this.users = users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    private static class Message {
        private int userId;
        private String content;
        private Calendar date;

        Message(int userId, String content, Calendar date) {
            this.userId = userId;
            this.content = content;
            this.date = date;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDate() {
            return new SimpleDateFormat("dd.MM.yyyy").format(date.getTime());
        }

        public void setDate(Calendar date) {
            this.date = date;
        }
    }
}
