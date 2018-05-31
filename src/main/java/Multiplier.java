/**
 * Created by zuzhaoyue on 18/5/21.
 */
@ExtractInterface(value = "IMultiplier")
public class Multiplier {
    public static int multiply(int x,int y){
        int total = 0;
        for(int i = 0; i< x ; i++){
            total = add(total,y);
        }
        return total;
    }
    private static int add(int x,int y){
        return x + y;
    }
    public static void main(String args[]){
        Multiplier multiplier = new Multiplier();
        System.out.println(multiplier.multiply(2,3));
    }
}
