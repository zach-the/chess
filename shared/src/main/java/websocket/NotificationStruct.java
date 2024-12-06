package websocket;

import websocket.messages.ServerMessage;

public record NotificationStruct(ServerMessage.ServerMessageType serverMessageType, String message) { }
