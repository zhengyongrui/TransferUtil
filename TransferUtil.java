import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>根据{@link TransferField}注解转换对象属性数据</p>
 *
 * @author zhengyongrui .
 * @version 1.0
 * Create In  2019-03-28
 */
public class TransferUtil {

    /**
     * 复制对象列表
     * @param poList 来源对象列表
     * @param voClass 返回对象的类
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> List<T> copyBeanList(final List poList, Class<?> voClass) {
        return copyBeanList(poList, voClass, true);
    }

    /**
     * 复制对象
     * @param po 来源对象
     * @param voClass 返回对象的类
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, Class<?> voClass) {
       return copyBean(po, voClass, true);
    }

    /**
     * 复制对象
     * @param po 来源对象
     * @param vo 返回对象
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, T vo) {
        return copyBean(po, vo, true);
    }

    /**
     * 复制对象
     * @param po 来源对象
     * @param voClass 返回对象的类
     * @param copySameName 是否拷贝相同名称属性
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, Class<?> voClass, boolean copySameName) {
        try {
            T vo = (T) voClass.newInstance();
            vo = copyBean(po, vo, copySameName);
            return vo;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 复制对象
     * @param po 来源对象
     * @param vo 返回对象
     * @param copySameName 是否拷贝相同名称属性
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, T vo, boolean copySameName) {
        BeanUtils.copyProperties(po, vo);
        return copyBean(po, vo.getClass(), vo);
    }

    /**
     * 复制对象数组
     * @param poList 来源对象列表
     * @param voClass 返回对象的类
     * @param copySameName 是否拷贝相同名称属性
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> List<T> copyBeanList(final List poList, Class<?> voClass, boolean copySameName) {
        if (poList == null) {
            return null;
        } else {
            List<T> voList = new ArrayList();
            Iterator var4 = poList.iterator();
            while (var4.hasNext()) {
                Object po = var4.next();
                voList.add(copyBean(po, voClass, copySameName));
            }
            return voList;
        }
    }

    /**
     * 复制对象
     * @param po 来源对象
     * @param voClass 返回对象的类
     * @param <T> 返回的对象泛型
     * @return .
     */
    private static <T> T copyBean(Object po, Class<?> voClass, T vo) {
        StandardEvaluationContext voEvaluationContext = new StandardEvaluationContext(vo);
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext poEvaluationContext = new StandardEvaluationContext(po);
        // 需要同步的vo字段
        List<Field> voFieldListForSync = Arrays.stream(voClass.getDeclaredFields()).filter(field ->
                field.getAnnotation(TransferField.class) != null
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(voFieldListForSync)) {
            Arrays.stream(po.getClass().getDeclaredFields()).forEach(poField -> {
                // 查找po对应的字段
                voFieldListForSync.stream().filter(voField ->
                        voField.getAnnotation(TransferField.class).value().equals(poField.getName())
                ).findFirst().ifPresent(voField -> {
                    // 同步数据
                    Object fileValue = parser.parseExpression(poField.getName()).getValue(poEvaluationContext);
                    parser.parseExpression(voField.getName()).setValue(voEvaluationContext, fileValue);
                });
            });
        }
        return vo;
    }

}
