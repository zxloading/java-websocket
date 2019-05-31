package com.example.websocket.controller;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author :hujh
 */
@Component
@ServerEndpoint(value = "/myWebSocket/{roomName}")
public class MyWebSocket {
    //用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<MyWebSocket> user = new CopyOnWriteArraySet<MyWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

//    @OnMessage
//    public void onMessage(String message, Session session) throws IOException {
//        //群发消息
//        for (MyWebSocket myWebSocket : user) {
//            myWebSocket.session.getBasicRemote().sendText(session.getId() + "说：" + message);
//            myWebSocket.session.getBasicRemote().sendText("<img src=''/>");
//        }
//    }

//    @OnOpen
//    public void onOpen(Session session) {
//        System.out.println(session.getId() + " open...");
//        this.session = session;
//        user.add(this);
//    }
//
//    @OnClose
//    public void onClose() {
//        System.out.println(this.session.getId() + " close...");
//        user.remove(this);
//    }
//
//    @OnError
//    public void onError(Session session, Throwable error) {
//        System.out.println(this.session.getId() + " error...");
//        error.printStackTrace();
//    }


    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap();

    @OnOpen
    public void connect(@PathParam("roomName") String roomName, Session session) throws Exception {
        // 将session按照房间名来存储，将各个房间的用户隔离
        if (!rooms.containsKey(roomName)) {
            // 创建房间不存在时，创建房间
            Set<Session> room = new HashSet<>();
            // 添加用户
            room.add(session);
            rooms.put(roomName, room);
        } else {
            // 房间已存在，直接添加用户到相应的房间
            rooms.get(roomName).add(session);
        }
        System.out.println("a client has connected!");
    }

    @OnClose
    public void disConnect(@PathParam("roomName") String roomName, Session session) {
        rooms.get(roomName).remove(session);
        System.out.println("a client has disconnected!");
    }

    @OnMessage
    public void receiveMsg(@PathParam("roomName") String roomName,
                           String msg, Session session) throws Exception {
        // 此处应该有html过滤
//        msg =msg;
        System.out.println(msg);
        // 接收到信息后进行广播
        broadcast(roomName, msg);
    }

    // 按照房间名进行广播
    public static void broadcast(String roomName, String msg) throws Exception {
        for (Session session : rooms.get(roomName)) {
            session.getBasicRemote().sendText(msg);
        }
    }


}
