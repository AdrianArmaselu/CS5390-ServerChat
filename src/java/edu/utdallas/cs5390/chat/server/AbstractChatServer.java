package edu.utdallas.cs5390.chat.server;

import edu.utdallas.cs5390.chat.server.service.AbstractClientMessagingService;

/**
 * Created by aarmaselu on 10/11/2016.
 */
public interface AbstractChatServer {
    AbstractClientMessagingService getAbstractClientMessagingService();

    String getPartnerId();

}
