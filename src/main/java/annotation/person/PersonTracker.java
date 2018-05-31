package annotation.person;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zuzhaoyue on 18/5/21.
 */
public class PersonTracker {
    public static void track(List<String> names, Class clazz){
        for(Field field:clazz.getDeclaredFields()){
            Person person = field.getAnnotation(Person.class);
            if(person != null){
                System.out.println("找到名叫" + person.name() + "的人!他是一名" + field.getName());
                names.remove(person.name());
            }

        }
        if(names != null && names.size() > 0){
            for(String name : names){
                System.out.println("没找到名叫" + name + "的人~");
            }
        }
    }
    public static void main(String args[]){
        List<String> names = new ArrayList<>();
        Collections.addAll(names,"肉肉","大扣","虎虎侠");
        track(names, PersonUtils.class);
    }

}
