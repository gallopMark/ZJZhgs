package com.uroad.mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by MFB on 2018/9/5.
 */
public class MqttService {
    private MqttAndroidClient client;
    private MqttConnectOptions conOpt;
    private Context context;
    private String serverUrl;
    private String userName;
    private String passWord;
    //唯一标示 保证每个设备都唯一就可以 建议 imei
    private String clientId;
    private int timeOut;
    private int keepAliveInterval;
    private boolean cleanSession;
    private boolean autoReconnect = true;
    private IMqttCallBack starMQTTCallBack;

    /**
     * builder设计模式
     */
    private MqttService(Builder builder) {
        this.context = builder.context;
        this.serverUrl = builder.serverUrl;
        this.userName = builder.userName;
        this.passWord = builder.passWord;
        this.clientId = builder.clientId;
        this.timeOut = builder.timeOut;
        this.keepAliveInterval = builder.keepAliveInterval;
        this.cleanSession = builder.cleanSession;
        this.autoReconnect = builder.autoReconnect;
        init();
    }

    /**
     * Builder 构造类
     */
    public static final class Builder {

        private Context context;
        private String serverUrl;
        private String userName = "admin";
        private String passWord = "password";
        private String clientId;
        private int timeOut = 10;
        private int keepAliveInterval = 20;
        private boolean cleanSession = false;
        private boolean autoReconnect = true;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder passWord(String passWord) {
            this.passWord = passWord;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public Builder autoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        public Builder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public MqttService create() {
            return new MqttService(this);
        }
    }

    /**
     * 发布消息
     */
    public void publish(String msg, String topic, int qos, boolean retained) {
        try {
            client.publish(topic, msg.getBytes(), qos, retained);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        client = new MqttAndroidClient(context, serverUrl, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);
        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(cleanSession);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(timeOut);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(keepAliveInterval);
        // 用户名
        conOpt.setUserName(userName);
        conOpt.setServerURIs(new String[]{serverUrl});
        // 密码
        conOpt.setPassword(passWord.toCharArray());
        conOpt.setAutomaticReconnect(autoReconnect); //自动重连
        conOpt.setMaxInflight(10); //允许同时发送几条消息（未收到broker确认信息）
    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接MQTT服务器
     */
    public void connect(IMqttCallBack starMQTTCallBack) {
        this.starMQTTCallBack = starMQTTCallBack;
        if (!client.isConnected()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void subscribe(String topics, int qos) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topics, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题
     *
     * @param topics 主题
     * @param qos    策略
     */
    public void subscribe(String[] topics, int[] qos) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topics, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topicFilter, qos, userContext, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String[] topicFilters, int[] qos, Object userContext, IMqttActionListener callback) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topicFilters, qos, userContext, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback, IMqttMessageListener messageListener) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topicFilter, qos, userContext, callback, messageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topicFilter, int qos, IMqttMessageListener messageListener) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topicFilter, qos, messageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String[] topicFilters, int[] qos, IMqttMessageListener[] messageListeners) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topicFilters, qos, messageListeners);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String[] topicFilters, int[] qos, Object userContext, IMqttActionListener callback, IMqttMessageListener[] messageListeners) {
        if (!isConnected()) return;
        try {
            // 订阅topic话题
            client.subscribe(topicFilters, qos, userContext, callback, messageListeners);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload, int qos,
                        boolean retained) {
        if (!isConnected()) return;
        try {
            client.publish(topic, payload, qos, retained);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, MqttMessage message) {
        if (!isConnected()) return;
        try {
            client.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload, int qos,
                        boolean retained, Object userContext, IMqttActionListener callback) {
        if (!isConnected()) return;
        try {
            client.publish(topic, payload, qos, retained, userContext, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, MqttMessage message,
                        Object userContext, IMqttActionListener callback) {
        if (!isConnected()) return;
        try {
            client.publish(topic, message, userContext, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String topicFilter) {
        if (!isConnected()) return;
        try {
            client.unsubscribe(topicFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String[] topic) {
        if (!isConnected()) return;
        try {
            client.unsubscribe(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String topicFilter, Object userContext, IMqttActionListener callback) {
        if (!isConnected()) return;
        try {
            client.unsubscribe(topicFilter, userContext, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String[] topicFilters, Object userContext, IMqttActionListener callback) {
        if (!isConnected()) return;
        try {
            client.unsubscribe(topicFilters, userContext, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (client == null) return;
        try {
            client.unregisterResources();
            client.close();
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断连接是否断开
     */
    public boolean isConnected() {
        if (client == null) return false;
        try {
            return client.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * MQTT是否连接成功
     */
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            if (starMQTTCallBack != null) {
                starMQTTCallBack.connectSuccess(arg0);
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            if (starMQTTCallBack != null) {
                starMQTTCallBack.connectFailed(arg0, arg1);
            }
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            String msgContent = new String(message.getPayload());
            if (starMQTTCallBack != null) {
                starMQTTCallBack.messageArrived(topic, msgContent, message.getQos());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            if (starMQTTCallBack != null) {
                starMQTTCallBack.deliveryComplete(arg0);
            }
        }

        @Override
        public void connectionLost(Throwable arg0) {
            if (starMQTTCallBack != null) {
                starMQTTCallBack.connectionLost(arg0);
            }
            // 失去连接，重连
        }
    };
}
