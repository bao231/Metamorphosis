package com.taobao.meta.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.metamorphosis.client.MetaClientConfig;
import com.taobao.metamorphosis.client.MetaMessageSessionFactory;


/**
 * meta���ɲ���_OneProducerOneConsumer
 * 
 * @author gongyangyu(gongyangyu@taobao.com)
 * 
 */
public class SetDefaultTopicTest extends BaseMetaTest {

    private final String topic = "meta-test";


    @Override
    @Before
    public void setUp() throws Exception {
        final MetaClientConfig metaClientConfig = new MetaClientConfig();
        this.sessionFactory = new MetaMessageSessionFactory(metaClientConfig);
        this.startServer("server3");
        System.out.println("before run");
    }


    @Override
    @After
    public void tearDown() throws Exception {
        this.sessionFactory.shutdown();
        Utils.stopServers(this.brokers);
        System.out.println("after run");
    }


    @Test
    public void sendConsume() throws Exception {
        this.createProducer();
        this.producer.setDefaultTopic(this.topic);
        // �����߱���ָ������
        this.createConsumer("group1");

        try {
            // ������Ϣ
            final int count = 5;
            // ���͵������ڵ�topic
            this.sendMessage(count, "hello", "SetDefaultTopicTest");

            // ���Ľ�����Ϣ����֤������ȷ
            this.subscribe("SetDefaultTopicTest", 1024 * 1024, count);
        }
        finally {
            this.producer.shutdown();
            this.consumer.shutdown();
        }

    }
}
