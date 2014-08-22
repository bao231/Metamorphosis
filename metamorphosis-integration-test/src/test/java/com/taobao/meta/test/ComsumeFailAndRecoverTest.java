package com.taobao.meta.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.taobao.metamorphosis.Message;
import com.taobao.metamorphosis.client.MetaClientConfig;
import com.taobao.metamorphosis.client.MetaMessageSessionFactory;
import com.taobao.metamorphosis.client.consumer.ConsumerConfig;
import com.taobao.metamorphosis.client.consumer.MessageListener;


/**
 * ��Ϣ����ʧ��,���Զ�λ���ʧ�ܣ�����recover
 * 
 * @author �޻�
 * @since 2011-11-14 ����6:45:35
 */

public class ComsumeFailAndRecoverTest extends BaseMetaTest {

    private final String topic = "meta-test";


    @Override
    @Before
    public void setUp() throws Exception {
        final MetaClientConfig metaClientConfig = new MetaClientConfig();
        metaClientConfig.setRecoverMessageIntervalInMills(2000);// recoverʱ���һЩ
        this.sessionFactory = new MetaMessageSessionFactory(metaClientConfig);
        this.startServer("server1");
        System.out.println("before run");
    }


    @Test
    public void sendConsume() throws Exception {
        this.createProducer();
        this.producer.publish(this.topic);
        final ConsumerConfig consumerConfig = new ConsumerConfig("group1");
        consumerConfig.setMaxFetchRetries(5);
        this.consumer = this.sessionFactory.createConsumer(consumerConfig);
        final AtomicInteger i = new AtomicInteger(0);
        try {
            int count = 2;
            this.sendMessage(count, "hello", this.topic);

            this.consumer.subscribe(this.topic, 1024 * 1024, new MessageListener() {

                public void recieveMessages(final Message messages) {
                    ComsumeFailAndRecoverTest.this.queue.add(messages);
                    // ��һ�ν��յ����쳣,retry 5+1�ξ����쳣֮����Ϣ�Ž���recover,recoverһ�δ���ɹ�
                    // ������������Ϣ8��
                    // ���ϵڶ�����Ϣ,������һ����9����Ϣ
                    if (Arrays.equals(messages.getData(), "hello0".getBytes())
                            && i.get() <= consumerConfig.getMaxFetchRetries() + 1) {
                        i.incrementAndGet();
                        throw new RuntimeException("don't worry,just for test");
                    }
                }


                public Executor getExecutor() {
                    return null;
                }
            }).completeSubscribe();

            while (this.queue.size() < count + 2 + consumerConfig.getMaxFetchRetries()) {
                Thread.sleep(1000);
                System.out.println("�ȴ�������Ϣ" + (count + 2 + consumerConfig.getMaxFetchRetries()) + "����Ŀǰ���յ�"
                        + this.queue.size() + "��");
            }

            int j = 0;
            for (Message msg : this.queue) {
                if (Arrays.equals(msg.getData(), "hello0".getBytes())) {
                    ++j;
                }
                else {
                    System.out.println(new String(msg.getData()));
                    assertTrue(Arrays.equals(msg.getData(), "hello1".getBytes()));
                }
            }
            // ֻ��һ����hello1,��������hello0
            assertEquals(j, this.queue.size() - 1);

        }
        finally {
            this.producer.shutdown();
            this.consumer.shutdown();
        }
    }
}
