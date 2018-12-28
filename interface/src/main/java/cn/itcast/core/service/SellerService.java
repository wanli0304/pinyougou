package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import entity.pageResult;

public interface SellerService {
    void add (Seller seller);

    Seller findOne (String sellerId);

    pageResult search (Integer page, Integer rows, Seller seller);

    void updateStatus (String sellerId, String status);
}
