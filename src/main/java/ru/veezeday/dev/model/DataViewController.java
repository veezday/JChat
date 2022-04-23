package ru.veezeday.dev.model;

import java.util.ArrayList;
import java.util.List;

public class DataViewController {

    public static List<User> getChatUsers(List<Integer> chatUsersId) {
        ArrayList<User> chatUsers = new ArrayList<User>();
        for (Integer id : chatUsersId) {
            User user = User.get(id);
            chatUsers.add(user);
        }
        return chatUsers;
    }

    public  static User getUser(int id) {
        return User.get(id);
    }
}
