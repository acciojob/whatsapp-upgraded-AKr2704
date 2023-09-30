package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Repository
public class WhatsappRepository {

    HashMap<Group, List<User>> groupUserHM;
    HashMap<Group, List<Message>> groupMsgHM;
    HashMap<Message, User> senderHM;
    HashMap<Group, User> adminHM;
    HashSet<String> mobileNoList;
    int customGroup;
    int msgId;

    public WhatsappRepository() {
        this.groupMsgHM = new HashMap<Group, List<Message>>();
        this.groupUserHM = new HashMap<Group, List<User>>();
        this.senderHM = new HashMap<Message, User>();
        this.adminHM = new HashMap<Group, User>();
        this.mobileNoList = new HashSet<>();
        this.customGroup = 0;
        this.msgId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if (mobileNoList.contains(mobile)) {
            throw new Exception("User already exists");
        }
        mobileNoList.add(mobile);
        User user = new User(name, mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {

        if (users.size() == 2) {
            Group group = new Group(users.get(1).getName(), 2);
            adminHM.put(group, users.get(0));
            groupUserHM.put(group, users);
            groupMsgHM.put(group, new ArrayList<Message>());
            return group;
        }
        this.customGroup += 1;

        Group group = new Group(new String("Group " + this.customGroup), users.size());
        adminHM.put(group, users.get(0));
        groupUserHM.put(group, users);
        groupMsgHM.put(group, new ArrayList<Message>());
        return group;
    }


    public int createMessage(String content) {
        this.msgId += 1;
        Message message = new Message(msgId, content);
        return message.getId();

    }


    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if (adminHM.containsKey(group)) {
            List<User> users = groupUserHM.get(group);
            Boolean userFound = false;
            //if sender exists
            for (User user : users) {
                if (user.equals(sender)) {
                    userFound = true;
                    break;
                }
            }


            if (userFound) {
                senderHM.put(message, sender);
                List<Message> messages = groupMsgHM.get(group);
                messages.add(message);
                groupMsgHM.put(group, messages);
                return messages.size();
            }
            throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group does not exist");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if (adminHM.containsKey(group)) {
            if (adminHM.get(group).equals(approver)) {
                List<User> participants = groupUserHM.get(group);

                Boolean userFound = false;

                for (User participant : participants) {
                    if (participant.equals(user)) {
                        userFound = true;
                        break;
                    }
                }

                if (userFound) {
                    adminHM.put(group, user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Insufficient permission");
        }
        throw new Exception("Group does not exist");
    }

    public int removeUser(User user) throws Exception {
        Boolean userFound = false;
        Group userGroup = null;
        for (Group group : groupUserHM.keySet()) {
            List<User> participants = groupUserHM.get(group);
            for (User participant : participants) {
                if (participant.equals(user)) {
                    if (adminHM.get(group).equals(user)) {
                        throw new Exception("Cannot remove admin");
                    }
                    userGroup = group;
                    userFound = true;
                    break;
                }
            }

            if (userFound) {
                break;
            }
        }

        if (userFound) {
            List<User> users = groupUserHM.get(userGroup);
            List<User> updatedUsers = new ArrayList<>();
            for (User participant : users) {
                if (participant.equals(user))
                    continue;
                updatedUsers.add(participant);
            }
            groupUserHM.put(userGroup, updatedUsers);


            List<Message> messages = groupMsgHM.get(userGroup);
            List<Message> updatedMessages = new ArrayList<>();
            for (Message message : messages) {
                if (senderHM.get(message).equals(user))
                    continue;
                updatedMessages.add(message);
            }
            groupMsgHM.put(userGroup, updatedMessages);


            HashMap<Message, User> updatedSenderMap = new HashMap<>();
            for (Message message : senderHM.keySet()) {
                if (senderHM.get(message).equals(user))
                    continue;
                updatedSenderMap.put(message, senderHM.get(message));
            }
            senderHM = updatedSenderMap;
            return updatedUsers.size() + updatedMessages.size() + updatedSenderMap.size();
        }
        throw new Exception("User not found");
    }
}