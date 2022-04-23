package ru.veezeday.dev.model;

import java.sql.*;

import static ru.veezeday.dev.dao.DataAccessObject.*;

public class Notify {
    private int id;
    private int pairId;
    private TypeOfNotify typeOfNotify;
    private int senderId;
    private int recipientId;
    private int chatId;
    private String content;

    public static Notify get(int id) {
        int pairId;
        TypeOfNotify typeOfNotify;
        int recipientId;
        int senderId;
        int chatId;
        String content;
        Notify notify = null;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            statement = connection.prepareStatement("SELECT * FROM notify WHERE id=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                typeOfNotify = TypeOfNotify.valueOf(resultSet.getString("typeOfNotify"));
                pairId = resultSet.getInt("pairId");
                recipientId = resultSet.getInt("recipientId");
                senderId = resultSet.getInt("senderId");
                chatId = resultSet.getInt("chatId");
                content = resultSet.getString("content");

                notify = new Notify();
                notify.setId(id);
                notify.setPairId(pairId);
                notify.setTypeOfNotify(typeOfNotify);
                notify.setRecipientId(recipientId);
                notify.setSenderId(senderId);
                notify.setChatId(chatId);
                notify.setContent(content);
            }
        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return notify;
    }

    public static Notify create(TypeOfNotify typeOfNotify, int senderId, int recipientId, String content) {
        int id = -1;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Creating notify
            statement = connection.prepareStatement("INSERT INTO notify (typeOfNotify, content, senderId, recipientId) VALUES (?,?,?,?)",  Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, typeOfNotify.toString());
            statement.setString(2, content);
            statement.setInt(3, senderId);
            statement.setInt(4, recipientId);
            statement.executeUpdate();

            //Get id of generated chat
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }

        } catch(SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return Notify.get(id);
    }

    public static Notify create(TypeOfNotify typeOfNotify, int senderId, int recipientId, int chatId, String content) {
        int id = -1;

        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such chat exist.
            statement = connection.prepareStatement("SELECT * FROM chat WHERE id=?");
            statement.setInt(1, chatId);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This chat does not exist."
            );

            //Creating notify
            statement = connection.prepareStatement("INSERT INTO notify (typeOfNotify, content, senderId, recipientId, chatId) VALUES (?,?,?,?,?)",  Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, typeOfNotify.toString());
            statement.setString(2, content);
            statement.setInt(3, senderId);
            statement.setInt(4, recipientId);
            statement.setInt(5, chatId);
            statement.executeUpdate();

            //Get id of generated chat
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }

        } catch(SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }

        return Notify.get(id);
    }

    public void editPairId(int value) {
        try {
            Connection connection;
            PreparedStatement statement;
            ResultSet resultSet;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //Is such notify exist.
            statement = connection.prepareStatement("SELECT * FROM notify WHERE id=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This notify does not exist."
            );

            //Edit column
            statement = connection.prepareStatement("UPDATE notify SET pairId=? WHERE id=?");
            statement.setInt(1, value);
            statement.setInt(2, id);
            statement.executeUpdate();
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

            //Is such notify exist.
            statement = connection.prepareStatement("SELECT * FROM notify WHERE id=?");
            statement.setInt(1, id);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) throw new NotifyException(
                    "This notify does not exist."
            );

            //Deleting notify
            statement = connection.prepareStatement("DELETE FROM notify WHERE id=?");
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch(SQLException | ClassNotFoundException | NotifyException e) {
            e.printStackTrace();
        }
    }

    public void manage(String action) throws NotifyException {
        if (typeOfNotify.equals(TypeOfNotify.NOTIFY)) {
            if (action.equals("Ok")) {
                this.delete();
            } else {
                throw new NotifyException("Incorrect act.");
            }
        } else if (typeOfNotify.equals(TypeOfNotify.FRIEND_INVITE)) {
            if (action.equals("Accept")) {
                try {
                    Connection connection;
                    PreparedStatement statement;
                    ResultSet resultSet;
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

                    //Is such notify exist.
                    statement = connection.prepareStatement("SELECT * FROM notify WHERE id=?");
                    statement.setInt(1, id);
                    resultSet = statement.executeQuery();
                    if (!resultSet.next()) throw new NotifyException(
                            "Such invite does not exist."
                    );

                    //Is such friend record exist.
                    statement = connection.prepareStatement("SELECT * FROM userfriends WHERE userId=? AND friendId=? OR userId=? AND friendId=?");
                    statement.setInt(1, senderId);
                    statement.setInt(2, recipientId);
                    statement.setInt(3, recipientId);
                    statement.setInt(4, senderId);
                    resultSet = statement.executeQuery();
                    if (resultSet.next()) throw new NotifyException(
                            "He is already your friend."
                    );

                    //Creating friend record.
                    statement = connection.prepareStatement("INSERT INTO userfriends (userId, friendId) VALUES (?,?)");
                    statement.setInt(1, senderId);
                    statement.setInt(2, recipientId);
                    statement.executeUpdate();

                    //Deleting friend invite.
                    //DB automatically delete pairNotify cause exist foreign key with notify pairId and notify id

                    //Notify pairNotify = Notify.get(pairId);
                    //pairNotify.delete();
                    this.delete();
                } catch (SQLException | ClassNotFoundException | NotifyException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("Decline")) {
                User user = User.get(recipientId);
                //Creating notify back.
                TypeOfNotify typeOfNotify = TypeOfNotify.NOTIFY;
                int senderId = this.recipientId;
                int recipient = this.senderId;
                String content = String.format("%s decline your friend invite.", user.getName());
                Notify.create(typeOfNotify, senderId, recipientId, content);

                //Deleting notify
                //DB automatically delete pairNotify cause exist foreign key with notify pairId and notify id

                //Notify pairNotify = Notify.get(this.pairId);
                //pairNotify.delete();
                this.delete();
            } else {
                throw new NotifyException("Incorrect act.");
            }

        } else if (typeOfNotify.equals(TypeOfNotify.CHAT_INVITE)) {
            try {
                Connection connection;
                PreparedStatement statement;
                ResultSet resultSet;
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

                if (action.equals("Accept")) {
                    //Is such notify exist.
                    statement = connection.prepareStatement("SELECT * FROM notify WHERE id=?");
                    statement.setInt(1, id);
                    resultSet = statement.executeQuery();
                    if (!resultSet.next()) throw new NotifyException(
                            "Such invite does not exist."
                    );

                    //Is such chatuser record exist.
                    statement = connection.prepareStatement("SELECT * FROM chatusers WHERE userId=? AND chatId=?");
                    statement.setInt(1, recipientId);
                    statement.setInt(2, chatId);
                    resultSet = statement.executeQuery();
                    if (resultSet.next()) throw new NotifyException(
                            "You are in this chat already."
                    );

                    //Creating chatuser record.
                    statement = connection.prepareStatement("INSERT INTO chatusers (chatId, userId) VALUES (?,?)");
                    statement.setInt(1, chatId);
                    statement.setInt(2, recipientId);
                    statement.executeUpdate();

                    //Deleting chat invite.
                    //DB automatically delete pairNotify cause exist foreign key with notify pairId and notify id

                    //Notify pairNotify = Notify.get(pairId);
                    //pairNotify.delete();
                    this.delete();
                }

                //If you want to decline chat invite.
                else if (action.equals("Decline")) {
                    //Is such notify exist.
                    statement = connection.prepareStatement("SELECT * FROM notify WHERE id=?");
                    statement.setInt(1, this.id);
                    resultSet = statement.executeQuery();
                    if (!resultSet.next()) throw new NotifyException(
                            "Such invite does not exist."
                    );

                    //Creating to notify about declined invite to sender.
                    TypeOfNotify typeOfNotify = TypeOfNotify.NOTIFY;
                    int senderId = this.recipientId;
                    int recipientId = this.senderId;
                    int chatId = this.chatId;
                    User user = User.get(this.recipientId);
                    Chat chat = Chat.get(this.chatId);
                    String content = String.format("%s declined your invite to the %s chat", user.getName(), chat.getName());
                    Notify.create(typeOfNotify, senderId, recipientId, chatId, content);

                    //Deleting chat invite.
                    //DB automatically delete pairNotify cause exist foreign key with notify pairId and notify id

                    //Notify pairNotify = Notify.get(pairId);
                    //pairNotify.delete();
                    this.delete();
                }
            } catch (SQLException | ClassNotFoundException | NotifyException e) {
                e.printStackTrace();
            }
        } else if (typeOfNotify.equals(TypeOfNotify.DISINVITE)) {

            //DB automatically delete pairNotify cause exist foreign key with notify pairId and notify id

            //Notify pairNotify = Notify.get(pairId);
            //pairNotify.delete();
            this.delete();
        } else {
            throw new IllegalStateException("Unexpected value: " + typeOfNotify);
        }
    }

    public Notify() {}

    public Notify(int id, TypeOfNotify typeOfNotify, int recipientId, int senderId, int chatId, int pairId, String content) {
        this.id = id;
        this.typeOfNotify = typeOfNotify;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.chatId = chatId;
        this.content = content;
        this.pairId = pairId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TypeOfNotify getTypeOfNotify() {
        return typeOfNotify;
    }

    public void setTypeOfNotify(TypeOfNotify typeOfNotify) {
        this.typeOfNotify = typeOfNotify;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public int getPairId() {
        return pairId;
    }

    public void setPairId(int pairId) {
        this.pairId = pairId;
    }
}
