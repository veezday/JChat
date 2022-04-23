package ru.veezeday.dev.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.veezeday.dev.dao.*;
import ru.veezeday.dev.model.Chat;
import ru.veezeday.dev.model.Notify;
import ru.veezeday.dev.model.NotifyException;
import ru.veezeday.dev.model.User;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping
@SessionAttributes("user")
public class UserController {

    private User user;

    @GetMapping("/register")
    public String show(@ModelAttribute("user") User user) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user) {
        try {
            User.create(user);
        } catch (RegisterException e) {
            e.printStackTrace();
            return "register";
        }
        return "registerSuccess";
    }

    @GetMapping("/login")
    public String show() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(value = "name") String name,
                        @RequestParam(value = "password") String password,
                        Model model) {
        try {
            user = User.get(name, password);
            model.addAttribute("user", user);
        } catch (LoginException e) {
            e.printStackTrace();
            return "login";
        }

        return "redirect: /chatList";
    }

    @GetMapping("/chatList")
    public String chatListShow(Model model, HttpSession session) {
        if (user==null) return "login";
        user = User.get(user.getId());
        model.addAttribute("user", user);
        return "chatList";
    }

    @PostMapping("/chatList")
    public String chatLeave(@RequestParam(value="action", required = false) String action,
                                   @RequestParam(value="userChatId", required = false) int userChatId,
                                   Model model) {
        try {
            if (user == null) return "login";
            if (action.equals("Leave")) {
                Chat chat = user.getChatList().get(userChatId);
                chat.leave(user);
            }
            user = User.get(user.getId());
            model.addAttribute("user", user);
            return "chatList";
        } catch (Exception e) {
            e.printStackTrace();
            return "chatList";
        }
    }

    @GetMapping("/chatEdit")
    public String chatEditShow(@RequestParam(value="userChatId", required = false) int userChatId,
                               Model model) {
        if (user==null) return "login";
        user = User.get(user.getId());
        model.addAttribute("user", user);
        Chat chat = user.getChatList().get(userChatId);
        model.addAttribute("chat", chat);
        return "chatEdit";
    }

    @PostMapping("/chatEdit")
    public String setEdit(@RequestParam(value="action", required = false) String action,
                          @RequestParam(value="param", required = false) String param,
                          @RequestParam(value="chatId", required = false) int chatId,
                          Model model) {
        try {
            if (user == null) return "login";

            Chat chat = Chat.get(chatId);
            if (action.equals("Change name")) {
                chat.changeName(param);
            } else if (action.equals("As admin")) {
                chat.setAdmin(Integer.parseInt(param));
            } else if (action.equals("Delete")) {
                chat.deleteChatUser(Integer.parseInt(param));
            } else {
                throw new NullPointerException();
            }
            user = User.get(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("chat", Chat.get(chatId));
            return "chatEdit";
        } catch (Exception e) {
            e.printStackTrace();
            return "chatEdit";
        }
    }

    @GetMapping("/chatInfo")
    public String chatInfoShow(@RequestParam(value="userChatId", required = false) int userChatId,
                               Model model) {
        if (user==null) return "login";
        Chat chat = user.getChatList().get(userChatId);
        model.addAttribute("chat", chat);
        user = User.get(user.getId());
        model.addAttribute("user", user);
        return "chatInfo";
    }

    @GetMapping("/chatEditInvite")
    public String chatEditInviteShow(@RequestParam(value="userChatId", required = false) int userChatId,
                               Model model) {
        if (user==null) return "login";

        if (userChatId==-1) {
            Chat chat = Chat.create(user.getId());
            model.addAttribute("chat", chat);
            user = User.get(user.getId());
            model.addAttribute("user", user);
            System.out.println(user.getChatList().indexOf(chat));
        } else {
            Chat chat = user.getChatList().get(userChatId);
            model.addAttribute("chat", chat);
            user = User.get(user.getId());
            model.addAttribute("user", user);
        }
        return "chatEditInvite";
    }

    @PostMapping("/chatEditInvite")
    public String setEditInvite(@RequestParam(value="action", required = false) String action,
                                @RequestParam(value="param", required = false) String param,
                                @RequestParam(value="chatId", required = false) int chatId,
                                Model model) {
        try {
            if (user == null) return "login";

            Chat chat = Chat.get(chatId);
            if (action.equals("Change name")) {
                chat.changeName(param);
            } else if (action.equals("Invite")) {
                chat.inviteUser(Integer.parseInt(param), user.getId());
            } else {
                throw new NullPointerException();
            }
            user = User.get(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("chat", Chat.get(chatId));
            return "chatEditInvite";
        } catch (Exception e) {
            e.printStackTrace();
            return "chatEditInvite";
        }
    }

    @GetMapping("/friendList")
    public String friendListShow(@RequestParam(value="request", required = false) String request, Model model) {
            if (user == null) return "login";
            List<User> list = User.search(user, request);
            if (list == null) {
                list = new ArrayList<User>();
            }
            if (list.isEmpty()) {
                request = "";
            }
            user = User.get(user.getId());
            model.addAttribute("request", request);
            model.addAttribute("searchResult", list);
            model.addAttribute("user", user);
            return "friendList";
    }

    @PostMapping("/friendList")
    public String friendListAddOrDelete(@RequestParam(value="action", required = false) String action,
                                        @RequestParam(value="friendAOD", required = false) int friendId,
                                        Model model) {
        try {
            if (user == null) return "login";
            List<User> list = new ArrayList<User>();
            User friend = User.get(friendId);

            if (action.equals("Add")) {
                user.inviteFriend(friend);
            } else if (action.equals("Delete")) {
                user.deleteFriend(friend);
            }

            user = User.get(user.getId());
            model.addAttribute("request", null);
            model.addAttribute("searchResult", list);
            model.addAttribute("user", user);
            return "friendList";
        } catch (Exception e) {
            e.printStackTrace();
            return "friendList";
        }
    }

    @GetMapping("/notifyList")
    public String notifyListShow(Model model) {
        if (user==null) return "login";
        user = User.get(user.getId());
        model.addAttribute("user", user);
        return "notifyList";
    }

    @PostMapping("/notifyList")
    public String inviteAcceptOrDecline(@RequestParam(value="action", required = false) String action,
                                        @RequestParam(value="notifyId", required = false) int notifyId,
                                        Model model) throws NotifyException {
        try {
            if (user == null) return "login";
            Notify notify = Notify.get(notifyId);
            notify.manage(action);
            user = User.get(user.getId());
            model.addAttribute("user", user);
            return "notifyList";
        } catch (Exception e) {
            e.printStackTrace();
            return "NotifyList";
        }
    }

    @GetMapping("/chat")
    public String chatShow(@RequestParam(value = "id", required = true) int chatId, Model model) {
        if (user==null) return "login";
        user = User.get(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("chat", user.getChatList().get(chatId));
        return "chat";
    }
}
