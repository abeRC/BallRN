import java.util.Arrays;
public class Couve{
public static void scaledIncrement(double[]a,double[]b,double s)
{
   for(int i=0; i<a.length; i++)
        a[i]=a[i]+s*b[i];
   		}
public static void main(String[] args){
   double[]a={0,3,5,-1};
   double[]b={3,0,5,0.95};
   double c=3;
   System.out.println(Arrays.toString(a));
   scaledIncrement(a,b,c);
   System.out.println("novo a:"+Arrays.toString(a)); 
   System.out.println(dotProduct(a,b));
}
private static double dotProduct(double[]arr1,double[]arr2)
{
   double produtoEscalar=0;

   for(int i=0; i<arr1.length; i++)
        produtoEscalar += arr1[i] * arr2[i];
   
    return produtoEscalar;
}
}