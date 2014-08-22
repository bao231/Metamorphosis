package com.taobao.meta.test;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.taobao.metamorphosis.client.MetaClientConfig;
import com.taobao.metamorphosis.client.MetaMessageSessionFactory;


/**
 * �շ�������Ϣ�Ĳ���
 * 
 * @author �޻�
 * @since 2011-8-17 ����5:41:41
 */
@Ignore
public class BigMessageTest extends BaseMetaTest {
    private final String topic = "meta-test";


    @Override
    @Before
    public void setUp() throws Exception {
        MetaClientConfig metaClientConfig = new MetaClientConfig();
        this.sessionFactory = new MetaMessageSessionFactory(metaClientConfig);
        this.startServer("bigmessageserver");
        System.out.println("before run");
    }


    @Test
    public void sendConsume() throws Exception {

        this.createProducer();
        this.producer.publish(this.topic);
        // �����߱���ָ������
        this.createConsumer("group1");

        try {
            // ����ÿ��2M����Ϣ
            final int count = 50;
            this.sendMessage(count, Utils.getData(2 * 1024 * 1024), this.topic);

            // ���Ľ�����Ϣ����֤������ȷ
            this.subscribe(this.topic, 5 * 1024 * 1024, count);

        }
        finally {
            this.producer.shutdown();
            this.consumer.shutdown();
        }

    }
}
