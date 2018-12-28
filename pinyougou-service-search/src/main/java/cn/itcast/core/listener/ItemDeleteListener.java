package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 消息接受
 */
public class ItemDeleteListener implements MessageListener{
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage (Message message) {
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String id = atm.getText ();
            System.out.println ("删除索引接受到ID:"+id);
            //删除索引
            Criteria criteria = new Criteria("item_goodsid").is(id);
            SimpleQuery simpleQuery = new SimpleQuery (criteria);
            solrTemplate.delete (simpleQuery);
            solrTemplate.commit ();
        } catch (JMSException e) {
            e.printStackTrace ();
        }
    }
}
