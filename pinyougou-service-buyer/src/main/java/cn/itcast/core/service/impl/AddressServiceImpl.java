package cn.itcast.core.service.impl;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import cn.itcast.core.service.AddressService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressDao addressDao;

    @Override
    public List <Address> findListByLoginUser (String name) {
        AddressQuery addressQuery = new AddressQuery ();
        addressQuery.createCriteria ().andUserIdEqualTo (name);
        return addressDao.selectByExample (addressQuery);
    }
}
