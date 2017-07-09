package tk.mybatis.simple;

import tk.mybatis.simple.model.Country;

import java.util.List;

public interface SimpleMapper {

	Country selectCountry(Long id);
	
	List<Country> selectAll();
}
