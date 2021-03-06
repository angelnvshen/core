package own.stu.netty.lecture.action.chat_two.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import own.stu.netty.lecture.action.chat_two.protocal.request.MessageRequestPacket;
import own.stu.netty.lecture.action.chat_two.protocal.response.MessageResponsePacket;
import own.stu.netty.lecture.action.chat_two.session.Session;
import own.stu.netty.lecture.action.chat_two.util.SessionUtil;

import java.util.Date;

public class MessageRequestHandler extends SimpleChannelInboundHandler<MessageRequestPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageRequestPacket msg) throws Exception {

        // 1.拿到消息发送方的会话信息
        Session session = SessionUtil.getSession(ctx.channel());

        // 2.通过消息发送方的会话信息构造要发送的消息
        MessageResponsePacket responsePacket = new MessageResponsePacket();
        responsePacket.setFromUserId(session.getUserId());
        responsePacket.setFromUserName(session.getUserName());
        responsePacket.setMessage(msg.getMessage());

        // 3.拿到消息接收方的 channel
        Channel toUserChannel = SessionUtil.getChannel(msg.getToUserId());
        if(toUserChannel != null && SessionUtil.hasLogin(toUserChannel)){
            toUserChannel.writeAndFlush(responsePacket);
        }else{
            System.err.println("[" + msg.getToUserId() + "] 不在线，发送失败!");
        }
    }
}
