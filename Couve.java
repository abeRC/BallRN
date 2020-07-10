public class Couve {
    private static double dotProduct(double[]arr1,double[]arr2)
    {
        double produtoEscalar=0;

        for(int i=0; i<arr1.length; i++)
            produtoEscalar += arr1[i] * arr2[i];
   
        return produtoEscalar;
    }

    public static void scaledIncrement (double[] arr, double s, double[] b) {
        //faz arr = arr + s*b, para todas as posicoes dos vetores
    }

    public static double[] scale (double s, double[] arr) {
        double[] ret = null;
        //faz ret = s*arr, para todas as posições do vetor, e depois retorna ret
        return ret;
    }

    public static void main(String[] args){
        double[]a={0,3,5,-1};
        double[]b={3,0,5,0.95};
        System.out.println(dotProduct(a,b));
    }
}

