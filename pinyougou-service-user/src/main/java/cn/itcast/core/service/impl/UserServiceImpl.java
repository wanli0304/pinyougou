package cn.itcast.core.service.impl;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Destination smsDestination;

    @Autowired
    private UserDao userDao;

    @Override
    public void sendCode (String phone) {
        //生成六位随机数
        String randomNumeric = RandomStringUtils.randomNumeric (6);
        System.out.println (randomNumeric);
        //将生成的验证码传入缓存中 设置验证时间
        redisTemplate.boundValueOps (phone).set (randomNumeric);

        redisTemplate.boundValueOps (phone).expire (5, TimeUnit.MINUTES);
        //发消息
        jmsTemplate.send (smsDestination, new MessageCreator () {
            @Override
            public Message createMessage (Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage ();
                mapMessage.setString ("phone",phone);
                mapMessage.setString ("signName","品优购商城");
                mapMessage.setString ("templateCode","");
                mapMessage.setString ("templateParam","");
                return mapMessage;
            }
        });
    }

    @Override
    public void add (User user, String smscode) {
        //判断验证码
        String code = (String) redisTemplate.boundValueOps (user.getPhone ()).get ();
        if (null!=code){
            if (code.equals (smscode)){
                //添加到数据库
                user.setCreated (new Date ());
                user.setUpdated (new Date ());
                userDao.insertSelective (user);
            }else {
                //验证码错
                throw new RuntimeException ("验证码错误");
            }
        }else {
            //验证码失效
            throw new RuntimeException ("验证码失效");
        }
    }
}
