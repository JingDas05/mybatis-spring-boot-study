package sample.mybatis.tx.domain;

/**
 *
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table t_hotel
 *
 * @mbggenerated do_not_delete_during_merge
 */
public class Hotel {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_hotel.city
     *
     * @mbggenerated
     */
    private Integer city;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_hotel.name
     *
     * @mbggenerated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_hotel.address
     *
     * @mbggenerated
     */
    private String address;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_hotel.zip
     *
     * @mbggenerated
     */
    private String zip;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_hotel.city
     *
     * @return the value of t_hotel.city
     *
     * @mbggenerated
     */
    public Integer getCity() {
        return city;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_hotel.city
     *
     * @param city the value for t_hotel.city
     *
     * @mbggenerated
     */
    public void setCity(Integer city) {
        this.city = city;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_hotel.name
     *
     * @return the value of t_hotel.name
     *
     * @mbggenerated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_hotel.name
     *
     * @param name the value for t_hotel.name
     *
     * @mbggenerated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_hotel.address
     *
     * @return the value of t_hotel.address
     *
     * @mbggenerated
     */
    public String getAddress() {
        return address;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_hotel.address
     *
     * @param address the value for t_hotel.address
     *
     * @mbggenerated
     */
    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_hotel.zip
     *
     * @return the value of t_hotel.zip
     *
     * @mbggenerated
     */
    public String getZip() {
        return zip;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_hotel.zip
     *
     * @param zip the value for t_hotel.zip
     *
     * @mbggenerated
     */
    public void setZip(String zip) {
        this.zip = zip == null ? null : zip.trim();
    }
}