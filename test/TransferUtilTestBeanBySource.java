/**
 * <p>功能描述</p>
 *
 * @author zhengyongrui .
 * @version 1.0
 * Create In  2019-04-09
 */
public class TransferUtilTestBeanBySource {

    /**
     * 要把str1值付给po的str1和str2
     */
    @TransferField("str1,str2")
    private String str1;

    private int int1;

    private Integer int2;

    public String getStr1() {
        return str1;
    }

    public void setStr1(String str1) {
        this.str1 = str1;
    }

    public int getInt1() {
        return int1;
    }

    public void setInt1(int int1) {
        this.int1 = int1;
    }

    public Integer getInt2() {
        return int2;
    }

    public void setInt2(Integer int2) {
        this.int2 = int2;
    }
}
