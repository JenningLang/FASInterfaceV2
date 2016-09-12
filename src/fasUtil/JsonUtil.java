package fasUtil;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON ����������
 *
 * @author huangyong
 * @since 1.0
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * �� Java ����תΪ JSON �ַ���
     */
    public static <T> String toJSON(T obj) {
        String jsonStr;
        try {
            jsonStr = objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Java ת JSON ����", e);
            throw new RuntimeException(e);
        }
        return jsonStr;
    }

    /**
     * �� JSON �ַ���תΪ Java ����
     */
    public static <T> T fromJSON(String json, Class<T> type) {
        T obj;
        try {
            obj = objectMapper.readValue(json, type);
        } catch (Exception e) {
            logger.error("JSON ת Java ����", e);
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * ��JSON�ַ���ת��ΪList<>
     */
    public static <T> List<T> JSONtoList(String json, Class<T> type)
    {
        JavaType javaType = getCollectionType(ArrayList.class, type);
        try {
            return objectMapper.readValue(json,javaType);
        } catch (IOException e) {
            logger.error("JSON ת Java List<> ����", e);
            throw new RuntimeException(e);
        }
    }

    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses)
    {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
    }



    public static <K,V> Map<K,V> JsonToMap(String json,Class<K> keyType,Class<V> valueType){
        JavaType javaType = getCollectionType(Map.class,keyType,valueType);
        try {
            return objectMapper.readValue(json,javaType);
        } catch (IOException e) {
            logger.error("JSON ת Java Map<> ����", e);
            throw new RuntimeException(e);
        }

    }
    
}
