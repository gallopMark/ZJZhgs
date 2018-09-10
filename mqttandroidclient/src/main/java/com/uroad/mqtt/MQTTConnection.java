package com.uroad.mqtt;

import android.content.Context;
import android.text.TextUtils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MQTTConnection {
    public static final String TAG = "MQTTConnection";
    // This the application level keep-alive interval, that is used by the
    // AlarmManager
    // to keep the connection active, even when the device goes to sleep.
    private static final int KEEP_ALIVE_INTERVAL = 1000 * 60 * 1;
    // We don't need to remember any state between the connections, so we use a
    // clean start.
    private static boolean MQTT_CLEAN_START = true;
    // 消息级别：0（最多一次）;1(至少一次);2（只有一次）
    private static int[] MQTT_QUALITIES_OF_SERVICE = {2};
    // The broker should not retain any messages.
    private static boolean MQTT_RETAINED_PUBLISH = false;
    private String deviceid = "";// 使用设备id作为客户端的唯一识别码
    Context mContext;
    // Create connection spec
    String mqttConnSpec = "tcp://" + Constant.MQTT_IP + ":"
            + Constant.MQTT_PORT;
    MqttClient mqttClient = null;
    MQTTCallback mqttCallback = null;
    MqttCallback callback = null;

    public MQTTConnection(Context context, String deviceid, MQTTCallback mattcallback)
            throws MqttException {
        mContext = context;
        this.deviceid = deviceid;
        mqttCallback = mattcallback;
        callback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                mqttCallback.onConnectLost(throwable);
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//                byte[] result = Base64
//                        .decode(mqttMessage.getPayload(), Base64.DEFAULT);
//                String jsonStr = new String(result);
                mqttCallback.onMessageReceive(s, mqttMessage.getPayload());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                mqttCallback.onDeliveryComplete();
            }
        };

    }

    /*
     * 订阅消息 Send a request to the message broker to be sent messages published
     * with the specified topic name. Wildcards are allowed.
     */
    public void subscribeToTopic(String topicName) throws MqttException {

        if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
            // quick sanity check - don't try and subscribe if we don't have
            // a connection
            log("Connection error" + "No connection");
        } else {
            String[] topics = {topicName};
            mqttClient.subscribe(topics, MQTT_QUALITIES_OF_SERVICE);
        }
    }

    /*
     * 取消订阅 Send a request to the message broker to be sent messages published
     * with the specified topic name. Wildcards are allowed.
     */
    public void unsubscribeTopic(String topicName) throws MqttException {

        if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
            // quick sanity check - don't try and subscribe if we don't have
            // a connection
            log("Connection error" + "No connection");
        } else {
            String[] topics = {topicName};
            mqttClient.unsubscribe(topics);
        }
    }

    /*
     * 发布消息 Sends a message to the message broker, requesting that it be
     * published to the specified topic.
     */
    public void publishToTopic(String topicName, String message)
            throws MqttException {
        publishToTopic(topicName, message, MQTT_RETAINED_PUBLISH);
    }

    /*
     * Sends a message to the message broker, requesting that it be published to
     * the specified topic.
     */
    public void publishToTopic(String topicName, String message, boolean retain)
            throws MqttException {
        if ((mqttClient == null) || (mqttClient.isConnected() == false)) {
            // quick sanity check - don't try and publish if we don't have
            // a connection
            log("No connection to public to");
            throw new MqttException(1);
        } else {
            mqttClient.publish(topicName, message.getBytes(),
                    MQTT_QUALITIES_OF_SERVICE[0], retain);
        }
    }

    /**
     * 连接
     **/
    public void Connect() {
        try {
            // Create the client and connect
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(mqttConnSpec, deviceid, persistence);
            // 客户端设置
            MqttConnectOptions options = new MqttConnectOptions();
            if(!TextUtils.isEmpty(Constant.MQTT_USERNAME))
                options.setUserName(Constant.MQTT_USERNAME);
            if(!TextUtils.isEmpty(Constant.MQTT_PASSWORD))
                options.setPassword(Constant.MQTT_PASSWORD.toCharArray());
            // 设置keepalive time
            options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
            // 在客户机建立连接时，将除去客户机的任何旧预订。当客户机断开连接时，会除去客户机在会话期间创建的任何新预订。(默认为TRUE)
            options.setCleanSession(MQTT_CLEAN_START);
            // 设置下线通知
            options.setWill("Alive/app/" + deviceid, "".getBytes(), 0, false);
            mqttClient.connect(options);
            // 设置回调
            mqttClient.setCallback(callback);
//            subscribeToTopic("Message/worker/+/client/" + deviceid);
//            subscribeToTopic("Message/system/client/" + deviceid);
//            subscribeToTopic("Connect/" + deviceid + "/+");
//            subscribeToTopic("Unconnect/" + deviceid + "/+");
//            subscribeToTopic("Message/system/from/+/to/" + deviceid);
            subscribeToTopic("Msg/+");
            subscribeToTopic("Login/+");
            // // 上线通知(1:上线;0：下线)
            // publishToTopic("Alive/app/" + deviceid, "1", false);
        } catch (MqttException e) {
            log(e.getMessage());
        }
    }

    /**
     * 判断是否连接
     **/
    public boolean isConnect() {
        if (mqttClient != null)
            return mqttClient.isConnected();
        else
            return false;
    }

    /**
     * Disconnect(断开连接)
     **/
    public void disConnect() {
        try {

            mqttClient.disconnect();
            mqttClient = null;
        } catch (MqttPersistenceException e) {
            log("MqttException"
                    + (e.getMessage() != null ? e.getMessage() : " NULL"), e);
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            log("MqttException"
                    + (e.getMessage() != null ? e.getMessage() : " NULL"), e);
            e.printStackTrace();
        }
    }

    // log helper function
    private void log(String message) {
        log(message, null);
    }

    private void log(String message, Throwable e) {
        if (e != null) {


        } else {

        }

    }

    public interface MQTTCallback {
        void onMessageReceive(String topic, byte[] bytes);

        void onDeliveryComplete();

        void onConnectLost(Throwable tr);
    }
}
