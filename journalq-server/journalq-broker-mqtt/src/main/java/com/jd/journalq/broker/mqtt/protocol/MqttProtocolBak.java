//package com.jd.journalq.server.broker.mqtt.protocol;
//
//import ChannelHandlerProvider;
//import ProtocolService;
//import CodecFactory;
//import CommandHandlerFactory;
//import BrokerContext;
//import MqttConsts;
//import MqttHandlerFactory;
//import MqttProtocolHandlerPipeline;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandler;
//import io.netty.util.CharsetUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author majun8
// */
//@Deprecated
//public class MqttProtocolBak implements ProtocolService, ChannelHandlerProvider {
//    private static final Logger logger = LoggerFactory.getLogger(MqttProtocolBak.class);
//
//    private BrokerContext brokerContext;
//
//    public MqttProtocolBak(BrokerContext brokerContext) {
//        this.brokerContext = brokerContext;
//    }
//
//    @Override
//    public boolean isSupport(ByteBuf buffer) {
//        return decodeProtocol(buffer);
//    }
//
//    private boolean decodeProtocol(ByteBuf buffer) {
//        // FixedHeader
//        short b1 = buffer.readUnsignedByte();
//
//        int messageType = b1 >> 4;
//        boolean dupFlag = (b1 & 0x08) == 0x08;
//        int qosLevel = (b1 & 0x06) >> 1;
//        boolean retain = (b1 & 0x01) != 0;
//
//        int remainingLength = 0;
//        int multiplier = 1;
//        short digit;
//        int loops = 0;
//        do {
//            digit = buffer.readUnsignedByte();
//            remainingLength += (digit & 127) * multiplier;
//            multiplier *= 128;
//            loops++;
//        } while ((digit & 128) != 0 && loops < 4);
//
//        // MQTT protocol limits Remaining Length to 4 bytes
//        if (loops == 4 && (digit & 128) != 0) {
//            return false;
//        }
//
//        // VariableHeader
//        short msbSize = buffer.readUnsignedByte();
//        short lsbSize = buffer.readUnsignedByte();
//        int result = msbSize << 8 | lsbSize;
//        String protocolName = buffer.toString(buffer.readerIndex(), result, CharsetUtil.UTF_8);
//        if (!protocolName.equals("MQTT")) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public CodecFactory createCodecFactory() {
//        return null;
//    }
//
//    @Override
//    public CommandHandlerFactory createCommandHandlerFactory() {
//        MqttHandlerFactory mqttHandlerFactory = new MqttHandlerFactory();
//        return MqttHandlerRegister.register(mqttHandlerFactory);
//    }
//
//    @Override
//    public String type() {
//        return MqttConsts.PROTOCOL_MQTT_TYPE;
//    }
//
//    @Override
//    public ChannelHandler getChannelHandler(ChannelHandler channelHandler) {
//        return new MqttProtocolHandlerPipeline(this, channelHandler, brokerContext);
//    }
//}
