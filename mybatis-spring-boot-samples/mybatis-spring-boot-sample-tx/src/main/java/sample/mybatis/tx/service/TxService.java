package sample.mybatis.tx.service;

import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import sample.mybatis.tx.domain.City;
import sample.mybatis.tx.domain.CityExample;
import sample.mybatis.tx.domain.Hotel;
import sample.mybatis.tx.mapper.CityMapper;
import sample.mybatis.tx.mapper.HotelMapper;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wusi
 * @version 2018/12/11.
 */
@Service
public class TxService {

    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private CityMapper cityMapper;
    @Resource
    private HotelMapper hotelMapper;

    public void txWithCode() {
        TransactionDefinition td = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = transactionManager.getTransaction(td);
        try {
            City city = new City();
            city.setId(1111);
            city.setName("辽宁");
            city.setSubId(1112);
            city.setCountry("中国");
            city.setState("有效");
            cityMapper.insert(city);

            Hotel hotel = new Hotel();
            hotel.setName("如家");
            hotel.setAddress("辽宁");
            hotel.setZip("112000");
            hotel.setCity(1111);
            hotelMapper.insert(hotel);
            Map hashMap = new HashMap();
            hashMap.get("1").toString();
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            e.printStackTrace();
        }

    }

    @Transactional
    public void txWithAnnotation() {
        City city = new City();
        city.setId(1111);
        city.setName("辽宁");
        city.setSubId(1112);
        city.setCountry("中国");
        city.setState("有效");
        cityMapper.insert(city);

        Hotel hotel = new Hotel();
        hotel.setName("如家");
        hotel.setAddress("辽宁");
        hotel.setZip("112000");
        hotel.setCity(1111);
        hotelMapper.insert(hotel);
        throw new RuntimeException();
    }
}
