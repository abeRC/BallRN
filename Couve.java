import java.util.Arrays;
public class Couve{
public static void scaledIncrement(double[]a,double[]b,double s)
{
   for(int i=0; i<a.length; i++)
        a[i]=a[i]+s*b[i];
}
public static void main(String[] args){
   double[]a={0,3,5,-1, 7, 0,3,2,1, 5, 6, 7, 0, 1, 2,3,5,-1, 7, 0,3,2,1, 5, 6, 7, 0, 1, 2, 1};
   double[]b={3,0,5,0.95, 7,5,3,2,1, 5, 6, 7, 0, 1, 2,3,0,5,0.95, 7,5,3,2,1, 5, 6, 7, 0, 1, 2};
   double c=3;
   System.out.println(Arrays.toString(a));
   scaledIncrement(a,b,c);
   System.out.println("novo a:"+Arrays.toString(a)); 
   System.out.println(dotProduct(a,b));
   System.out.println(Arrays.toString(scale(c,a)));
   Double[] arr = {4.2, 2.3333, 1., 9.};
   printArray(arr);
}
private static double dotProduct(double[]arr1,double[]arr2)
{
   double produtoEscalar=0;

   for(int i=0; i<arr1.length; i++)
        produtoEscalar += arr1[i] * arr2[i];
   
    return produtoEscalar;
}
private static double[]scale(double s,double[]arr)
{
   double[]ret=null;
   ret=new double[arr.length];
   for(int i=0; i<arr.length; i++)
        ret[i]=s*arr[i];
      return ret;
}
private static void printArray(Object[]arr)
{
   double[]ret=null;
   ret=new double[arr.length];
   for(int i=0; i<arr.length; i++)
        System.out.println("posicao"+i+": "+arr[i]);
}
}