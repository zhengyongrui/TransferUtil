/**
 * <p>功能描述</p>
 *
 * @author zhengyongrui .
 * @version 1.0
 * Create In  2019-04-09
 */
public class TransferUtilTestBeanByTarget {

    private String str1;

    private String str2;

    /**
     * int1接受int1字段的值
     */
    @TransferField("int2")
    private Integer int1;

    /**
     * int1接受int1字段或int2的值
     */
    @TransferField("int1,int2")
    private Integer int2;

    public String getStr1() {
        return str1;
    }

    public void setStr1(String str1) {
        this.str1 = str1;
    }

    public String getStr2() {
        return str2;
    }

    public void setStr2(String str2) {
        this.str2 = str2;
    }

    public Integer getInt1() {
        return int1;
    }

    public void setInt1(Integer int1) {
        this.int1 = int1;
    }

    public Integer getInt2() {
        return int2;
    }

    public void setInt2(Integer int2) {
        this.int2 = int2;
    }

    @Override
    public String toString() {
        return "TransferUtilTestBeanByTarget{" +
                "str1='" + str1 + '\'' +
                ", str2='" + str2 + '\'' +
                ", int1=" + int1 +
                ", int2=" + int2 +
                '}';
    }
}
