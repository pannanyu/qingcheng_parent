package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务
 */
public interface CartService  {
    /**
     * 从redis中提取某用户的购物车
     * @param username
     * @return
     */
    public List<Map<String,Object>> findCartList(String username);

    /**
     * 添加商品到购物车
     * @param username
     * @param skuId 商品Id
     * @param num  数量
     *     如果已有该商品  num为已有数量的负数  则删除该商品
     */
    public void addItem(String username,String skuId,Integer num);

    /**
     * 更新选中状态
     * @param username
     * @param skuId
     * @param checked
     * @return
     */
    public boolean updateChecked(String username,String skuId,boolean checked);

    /**
     * 删除选中的购物车
     * @param username
     */
    public void deleteCheckedCart(String username);





}

