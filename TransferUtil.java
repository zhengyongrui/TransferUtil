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
    public static <T> T copyBean(Object po, Class<T> voClass) {
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
            List<T> voList = new ArrayList<>();
            for (Object po : poList) {
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
                    Object fieldValue = parser.parseExpression(poField.getName()).getValue(poEvaluationContext);
                    if (checkIsDateAndLongTransfer(voField, poField))
                    {
                        transferDateAndLongField(parser, poField, voField, voEvaluationContext, fieldValue);
                    } else {
                        parser.parseExpression(voField.getName()).setValue(voEvaluationContext, fieldValue);
                    }
                });
            });
        }
        return vo;
    }

    /**
     * Date与Long俩个字段数据互相转换
     * @param parser .
     * @param poField po字段
     * @param voField vo字段
     * @param voEvaluationContext .
     * @param fieldValue 字段值
     */
    private static void transferDateAndLongField(ExpressionParser parser, Field poField, Field voField, StandardEvaluationContext voEvaluationContext, Object fieldValue) {
        if (poField.getType().equals(Date.class)) {
            Date fieldValueDate = (Date) Optional.ofNullable(fieldValue).orElse(new Date());
            parser.parseExpression(voField.getName()).setValue(voEvaluationContext, fieldValueDate.getTime());
        } else {
            Long fieldValueDate = (Long) Optional.ofNullable(fieldValue).orElse(0);
            parser.parseExpression(voField.getName()).setValue(voEvaluationContext, new Date(fieldValueDate));
        }
    }

    /**
     * 判断是否2个字段类型不一样并且是Date与Long互相转换
     * @param voField vo
     * @param poField po
     * @return 是否
     */
    private static boolean checkIsDateAndLongTransfer(Field voField, Field poField) {
        boolean result = false;
        if (!voField.getType().equals(poField.getType())) {
            List<Class> checkTypes = Arrays.asList(Date.class, Long.class, long.class);
            List<Class> fieldTypes = Arrays.asList(voField.getType(), poField.getType());
            // 从Date Long long过滤字段的类型
            List<Class> reduceTypes = checkTypes.stream().filter(aClass -> !fieldTypes.contains(aClass)).collect(Collectors.toList());
            // 如果剩下的类型只剩下一个数值类型,证明他们一个是date一个是long,返回true
            result = reduceTypes.size() == 1 && Arrays.asList(Long.class, long.class).contains(reduceTypes.get(0));
        }
        return result;
    }

}
