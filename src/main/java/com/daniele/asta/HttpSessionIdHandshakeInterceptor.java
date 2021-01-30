package com.daniele.asta;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class HttpSessionIdHandshakeInterceptor implements HandshakeInterceptor {

 @Override
 public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
  if (request instanceof ServletServerHttpRequest) {
   ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
   HttpSession session = servletRequest.getServletRequest().getSession(false);
   if (session != null) {
    attributes.put("HTTPSESSIONID", session);
   }
  }
  return true;
 }

 public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
 }
}