import java.lang.annotation.*;

/**
 * <p>功能杝述</p>
 *
 * @author zhengyongrui .
 * @version 1.0
 * Create In  2019-03-16
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TransferField {

    String value();

}