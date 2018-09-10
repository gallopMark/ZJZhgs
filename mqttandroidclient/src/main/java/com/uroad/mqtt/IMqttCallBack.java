package com.uroad.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * Created by MFB on 2018/9/5.
 */
public interface IMqttCallBack {
    /**
     * 收到消息
     *
     * @param topic   主题
     * @param message 消息内容
     * @param qos     消息策略
     */
    void messageArrived(String topic, String message, int qos);

    /**
     * 连接断开
     *
     * @param throwable 抛出的异常信息
     */
    void connectionLost(Throwable throwable);

    /**
     * 传送完成
     */
    void deliveryComplete(IMqttDeliveryToken deliveryToken);

    /**
     * 连接成功
     */
    void connectSuccess(IMqttToken token);

    /**
     * 连接失败
     */
    void connectFailed(IMqttToken token, Throwable throwable);
}
