import org.springframework.beans.BeanUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
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
     *
     * @param poList  来源对象列表
     * @param voClass 返回对象的类
     * @param <T>     返回的对象泛型
     * @return .
     */
    public static <T> List<T> copyBeanList(final List poList, Class<?> voClass) {
        return copyBeanList(poList, voClass, true);
    }

    /**
     * 复制对象
     *
     * @param po      来源对象
     * @param voClass 返回对象的类
     * @param <T>     返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, Class<T> voClass) {
        return copyBean(po, voClass, true);
    }

    /**
     * 复制对象
     *
     * @param po  来源对象
     * @param vo  返回对象
     * @param <T> 返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, T vo) {
        return copyBean(po, vo, true);
    }

    /**
     * 复制对象
     *
     * @param po           来源对象
     * @param voClass      返回对象的类
     * @param copySameName 是否拷贝相同名称属性
     * @param <T>          返回的对象泛型
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
     *
     * @param po           来源对象
     * @param vo           返回对象
     * @param copySameName 是否拷贝相同名称属性
     * @param <T>          返回的对象泛型
     * @return .
     */
    public static <T> T copyBean(Object po, T vo, boolean copySameName) {
        if (copySameName) {
            BeanUtils.copyProperties(po, vo);
        }
        return copyBean(po, vo.getClass(), vo);
    }

    /**
     * 复制对象数组
     *
     * @param poList       来源对象列表
     * @param voClass      返回对象的类
     * @param copySameName 是否拷贝相同名称属性
     * @param <T>          返回的对象泛型
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
     *
     * @param po      来源对象
     * @param voClass 返回对象的类
     * @param <T>     返回的对象泛型
     * @return .
     */
    private static <T> T copyBean(Object po, Class<?> voClass, T vo) {
        StandardEvaluationContext voEvaluationContext = new StandardEvaluationContext(vo);
        StandardEvaluationContext poEvaluationContext = new StandardEvaluationContext(po);
        syncVoAnnotationField(po, voClass, voEvaluationContext, poEvaluationContext);
        syncPoAnnotationField(po, voClass, voEvaluationContext, poEvaluationContext);
        return vo;
    }

    /**
     * 同步vo注解的字段
     *
     * @param po                  要被转换的对象
     * @param voClass             转换后的对象类型
     * @param voEvaluationContext vo表达式
     * @param poEvaluationContext po表达式
     */
    private static void syncVoAnnotationField(Object po, Class<?> voClass, StandardEvaluationContext voEvaluationContext, StandardEvaluationContext poEvaluationContext) {
        List<Field> voFieldListForSync = Arrays.stream(voClass.getDeclaredFields()).filter(field ->
                field.getAnnotation(TransferField.class) != null
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(voFieldListForSync)) {
            for (Field voField : voFieldListForSync) {
                List<String> annotationValueList = getAnnotationValueList(voField);
                for (String annotationValue: annotationValueList) {
                    Optional<Field> poFieldOptional = getDeclaredFieldList(po.getClass(), null, true).stream().filter(poField ->
                            poField.getName().equals(annotationValue)
                    ).findFirst();
                    if (poFieldOptional.isPresent()) {
                        syncValueByEvaluation(poFieldOptional.get(), voField, poEvaluationContext, voEvaluationContext);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 同步po注解的字段
     *
     * @param po                  要被转换的对象
     * @param voClass             转换后的对象类型
     * @param voEvaluationContext vo表达式
     * @param poEvaluationContext po表达式
     */
    private static void syncPoAnnotationField(Object po, Class<?> voClass, StandardEvaluationContext voEvaluationContext, StandardEvaluationContext poEvaluationContext) {
        List<Field> poFieldListForSync = Arrays.stream(po.getClass().getDeclaredFields()).filter(field ->
                field.getAnnotation(TransferField.class) != null
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(poFieldListForSync)) {
            for (Field poField : poFieldListForSync) {
                getDeclaredFieldList(voClass, null, true).stream().filter(voField ->
                        checkAnnotationValueContainsFieldName(poField, voField)
                ).forEach(voField ->
                        syncValueByEvaluation(poField, voField, poEvaluationContext, voEvaluationContext)
                );
            }
        }
    }

    /**
     * 获取对象类型的字段
     * @param voClass 对象类型
     * @param fieldList 字段列表
     * @param isGetSuperclass 是否获取父级字段
     * @return .
     */
    private static List<Field> getDeclaredFieldList(Class<?> voClass, List<Field> fieldList, boolean isGetSuperclass) {
        if (fieldList == null) {
            fieldList = new ArrayList<>();
        }
        fieldList.addAll(Arrays.asList(voClass.getDeclaredFields()));
        if (isGetSuperclass && voClass.getSuperclass() != null) {
            getDeclaredFieldList(voClass.getSuperclass(), fieldList, isGetSuperclass);
        }
        return fieldList;
    }

    /**
     * 根据表达式同步字段值
     *
     * @param poField             .
     * @param voField             .
     * @param poEvaluationContext .
     * @param voEvaluationContext .
     */
    private static void syncValueByEvaluation(Field poField, Field voField, StandardEvaluationContext poEvaluationContext, StandardEvaluationContext voEvaluationContext) {
        ExpressionParser parser = new SpelExpressionParser();
        Object fieldValue = parser.parseExpression(poField.getName()).getValue(poEvaluationContext);
        if (checkIsDateAndLongTransfer(voField, poField)) {
            transferDateAndLongField(parser, poField, voField, voEvaluationContext, fieldValue);
        } else {
            parser.parseExpression(voField.getName()).setValue(voEvaluationContext, fieldValue);
        }
    }

    /**
     * 检查注解的字段的注解值是否包含当前字段
     *
     * @param annotationField 注解字段
     * @param field           当前字段
     * @return .
     */
    private static boolean checkAnnotationValueContainsFieldName(Field annotationField, Field field) {
        List<String> transferFieldValueList = getAnnotationValueList(annotationField);
        return transferFieldValueList.contains(field.getName());
    }

    /**
     * 获取注解的值
     *
     * @param annotationField 有注解的字段
     * @return .
     */
    private static List<String> getAnnotationValueList(Field annotationField) {
        String transferFieldValue = annotationField.getAnnotation(TransferField.class).value();
        List<String> transferFieldValueList = new ArrayList<>();
        if (!StringUtils.isEmpty(transferFieldValue)) {
            transferFieldValueList = Arrays.asList(transferFieldValue.split(","));
        }
        return transferFieldValueList;
    }

    /**
     * Date与Long俩个字段数据互相转换
     *
     * @param parser              .
     * @param poField             po字段
     * @param voField             vo字段
     * @param voEvaluationContext .
     * @param fieldValue          字段值
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
     *
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
