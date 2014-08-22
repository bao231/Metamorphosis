package com.taobao.meta.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executor;

import org.junit.Test;

import com.taobao.metamorphosis.Message;
import com.taobao.metamorphosis.client.consumer.MessageListener;
import com.taobao.metamorphosis.exception.MetaClientException;


/**
 * meta���ɲ���_OneProducerOneConsumer
 * 
 * @author gongyangyu(gongyangyu@taobao.com)
 * 
 */
public class OneProducerOneConsumerWithFilterTest extends BaseMetaTest {

    private final String topic = "filter-test";


    @Test
    public void sendConsume() throws Exception {
        this.createProducer();
        this.producer.publish(this.topic);
        // �����߱���ָ������
        this.createConsumer("group1");

        try {
            // ������Ϣ
            final int count = 1000;
            this.sendMessage(count, "hello", this.topic);

            // ���Ľ�����Ϣ
            try {
                this.consumer.subscribe(this.topic, 1024 * 1024, new MessageListener() {

                    public void recieveMessages(final Message messages) {
                        OneProducerOneConsumerWithFilterTest.this.queue.add(messages);
                    }


                    public Executor getExecutor() {
                        return null;
                    }
                }).completeSubscribe();
            }
            catch (final MetaClientException e) {
                throw e;
            }
            while (this.queue.size() < count / 2) {
                Thread.sleep(1000);
                System.out.println("�ȴ�������Ϣ" + count / 2 + "����Ŀǰ���յ�" + this.queue.size() + "��");
            }

            // �����Ϣ�Ƿ���յ���У������
            assertEquals(count / 2, this.queue.size());
            int i = 0;
            if (count != 0) {
                for (final Message msg : this.messages) {
                    if (++i % 2 == 0) {
                        assertTrue(this.queue.contains(msg));
                    }
                }
            }
            this.log.info("received message count:" + this.queue.size());
        }
        finally {
            this.producer.shutdown();
            this.consumer.shutdown();
        }

    }
}
