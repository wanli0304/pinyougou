package cn.itcast.core.service.impl;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add (Seller seller) {
        //未审核的商品
        seller.setStatus ("0");
        //密码加密
        seller.setPassword (new BCryptPasswordEncoder ().encode (seller.getPassword ()));
        sellerDao.insertSelective (seller);
    }

    @Override
    public Seller findOne (String sellerId) {
        return sellerDao.selectByPrimaryKey (sellerId);
    }

    @Override
    public pageResult search (Integer page, Integer rows, Seller seller) {
        PageHelper.startPage (page, rows);
        SellerQuery sellerQuery = new SellerQuery ();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria ();

        //判断传参
        if (null != seller.getName () && !"".equals (seller.getName ().trim ())) {
            criteria.andNameLike ("%" + seller.getName ().trim () + "%");
        }

        if (null != seller.getNickName () && !"".equals (seller.getNickName ().trim ())) {
            criteria.andNickNameLike ("%" + seller.getNickName ().trim () + "%");
        }
        if (null != seller.getStatus ()){
            criteria.andStatusEqualTo (seller.getStatus ());
        }

        Page <Seller> p = (Page <Seller>) sellerDao.selectByExample (sellerQuery);

        return new pageResult (p.getTotal (), p.getResult ());
    }

    @Override
    public void updateStatus (String sellerId, String status) {
        Seller seller = new Seller ();
        seller.setStatus (status);
        seller.setSellerId (sellerId);
        sellerDao.updateByPrimaryKeySelective (seller);
    }

}
