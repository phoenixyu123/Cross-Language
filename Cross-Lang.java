import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Cross-Lang extends Thread {


    public static int x[][] = null;
    public static int y[][] = null;
    public static int x_m = 1;
    public static int x_n = 1;
    public static int z[][] = null;
    public static int u[] = null;
    public static int v[] = null;
    public static int M[][] = null;
    public static Element m[] = null;
    public static Element SK_s[] = null;
    public static Element SK_r[] = null;
    public static boolean flag = true;
    private static int[][] Result;


    public static void SKGenerator(Field Zr) {
        for (int i = 0; i < x_m; i++) {
            SK_s[i] = Zr.newRandomElement().getImmutable();
        }
        System.out.println("\n");
        for (int i = 0; i < x_n; i++) {
            SK_r[i] = Zr.newRandomElement().getImmutable();
        }
    }


    public static void calculateVK(Element[] egg_3_right_2, Element[] PK2) {
        CountDownLatch countDownLatch = new CountDownLatch(x_m);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        long start = System.currentTimeMillis();
        for (int i = 0; i < x_m; i++) {
            final int d = i;
            Runnable run = () -> {
                try {
                    egg_3_right_2[d] = PK2[0].duplicate().pow(BigInteger.valueOf(x[0][d]));
                    for (int k = 1; k < x_n; k++) {
                        egg_3_right_2[d] = egg_3_right_2[d].mul(PK2[k].duplicate().pow(BigInteger.valueOf(x[k][d])));
                    }
                    countDownLatch.countDown();
                } catch (Exception e) {
                }
            };
            pool.execute(run);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        pool.shutdown();
        System.out.println("ProbGen:" + (end - start) + "ms");
    }

    public static void calculatePK1(Element[] PK1, Element g) {
        for (int i = 0; i < x_m; i++) {
            PK1[i] = g.powZn(SK_s[i]);
        }
    }

    public static void calculatePK2(Element[] PK2, Element g, Element h_b, Element a, Pairing bp) {
        CountDownLatch countDownLatch = new CountDownLatch(x_n);
        ExecutorService pool = Executors.newFixedThreadPool(12);
        long start = System.currentTimeMillis();
        for (int i = 0; i < x_n; i++) {
            final int d = i;
            Runnable run = () -> {
                try {
                    Element temp = g.powZn(SK_r[d]).powZn(a);
                    PK2[d] = bp.pairing(temp, h_b);
                    countDownLatch.countDown();
                } catch (Exception e) {
                }
            };
            pool.execute(run);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        pool.shutdown();
        System.out.println("PK2:" + (end - start) + "ms");
    }

    public static void calculate_m() {
        for (int i = 0; i < x_n; i++) {
            Element sum = SK_s[0].mul(M[0][i]);
            for (int j = 1; j < x_m; j++) {
                sum = sum.add(SK_s[j].mul(M[j][i]));
            }
            m[i] = sum;
        }
    }

    public static void calculate_pai(Element[] pai, Element delta, Element g) {
        CountDownLatch countDownLatch = new CountDownLatch(x_m);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        long start = System.currentTimeMillis();
        for (int i = 0; i < x_m; i++) {
            final int j = i;
            Runnable run = () -> {
                try {
                    Element temp1 = delta.mul(m[0]).mul(x[0][j]);
                    Element temp2 = SK_r[0].mul(x[0][j]);
                    Element sum_g;
                    for (int k = 1; k < x_n; k++) {
                        temp1 = temp1.add(delta.mul(m[k]).mul(x[k][j]));
                        temp2 = temp2.add(SK_r[k].mul(x[k][j]));
                    }
                    sum_g = temp1.add(temp2);
                    pai[j] = g.powZn(sum_g);
                    countDownLatch.countDown();
                } catch (Exception e) {
                }
            };
            pool.execute(run);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        pool.shutdown();
        System.out.println("Compute:" + (end - start) + "ms");
    }

    public static void calculate_egg_pai_j_h(Element[] egg_pai_j_h, Element[] pai, Element a, Pairing bp, Element h_b) {
        CountDownLatch countDownLatch = new CountDownLatch(x_m);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        long start = System.currentTimeMillis();
        for (int i = 0; i < x_m; i++) {
            final int j = i;
            Runnable run = () -> {
                try {
                    egg_pai_j_h[j] = pai[j].powZn(a);
                    egg_pai_j_h[j] = bp.pairing(egg_pai_j_h[j], h_b);
                    countDownLatch.countDown();
                } catch (Exception e) {
                }
            };
            pool.execute(run);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        pool.shutdown();
        System.out.println("egg_pai_j_h:" + (end - start) + "ms");
    }

    public static void calculate_sum_PK1_y(Element[] sum_PK1_y, Element[] PK1) {
        CountDownLatch countDownLatch = new CountDownLatch(x_m);

        ExecutorService pool2 = Executors.newFixedThreadPool(8);
        for (int i = 0; i < x_m; i++) {
            final int j = i;
            Runnable run = () -> {
                try {
                    sum_PK1_y[j] = PK1[0].pow(BigInteger.valueOf(y[0][j]));
                    for (int k = 1; k < x_m; k++) {
                        sum_PK1_y[j] = sum_PK1_y[j].mul(PK1[k].duplicate().pow(BigInteger.valueOf(y[k][j])));
                    }
                    System.out.println("sum_PK1_y:" + j + "/" + x_m);
                    countDownLatch.countDown();
                } catch (Exception e) {
                }
            };
            pool2.execute(run);
        }
        pool2.shutdown();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        while (true) {
            Pairing bp = PairingFactory.getPairing("a.properties");
            PairingFactory.getInstance().setUsePBCWhenPossible(true);

            Field G1 = bp.getG1();
            Field G2 = bp.getG2();
            Field Zr = bp.getZr();


            inputXandZ();
            if (isNull()) {
                System.out.println("Matrix can not be null!");
                continue;
            }
            calculateNewX();
            calculateY();

            Element g = G1.newRandomElement().getImmutable();
            Element h = G2.newRandomElement().getImmutable();
            Element delta = Zr.newRandomElement().getImmutable();
            Element a = Zr.newRandomElement().getImmutable();
            Element b = Zr.newRandomElement().getImmutable();
            Element h_b = h.powZn(b);
            Element h_delta = h.powZn(delta);
            Element h_delta_b = h_delta.powZn(b);

            SKGenerator(Zr);

            Element egg_3_right_2[] = new Element[x_m];
            Element PK2[] = new Element[x_n];
            Element PK1[] = new Element[x_m];
            Element pai[] = new Element[x_m];
            Element egg_pai_j_h[] = new Element[x_m];
            Element sum_PK1_y[] = new Element[x_m];


            calculatePK2(PK2, g, h_b, a, bp);

            calculateVK(egg_3_right_2, PK2);

            calculatePK1(PK1, g);

            calculate_m();

            calculate_pai(pai, delta, g);

            calculate_egg_pai_j_h(egg_pai_j_h, pai, a, bp, h_b);

            calculate_sum_PK1_y(sum_PK1_y, PK1);


            CountDownLatch countDownLatch = new CountDownLatch(x_m);
            ExecutorService pool = Executors.newFixedThreadPool(17);
            long start5 = System.currentTimeMillis();
            for (int i = 0; i < x_m; i++) {
                final int j = i;
                Runnable run = () -> {
                    try {
                        Element sum_PK1_y_a = sum_PK1_y[j].powZn(a);
                        Element egg_PK1_y_h_delta = bp.pairing(sum_PK1_y_a, h_delta_b);
                        Element egg_ab_PKparthdelta_vk = egg_PK1_y_h_delta.mul(egg_3_right_2[j]);
                        if (egg_pai_j_h[j].isEqual(egg_ab_PKparthdelta_vk)) {
                            System.out.println("Yes" + j);
                        } else {
                            flag = false;
                            System.out.println("No" + j);
                        }
                        countDownLatch.countDown();
                    } catch (Exception e) {
                    }
                };
                pool.execute(run);
            }
            pool.shutdown();

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long end5 = System.currentTimeMillis();
            System.out.println("Verify:" + (end5 - start5) + "ms" + Runtime.getRuntime().availableProcessors());
            System.out.println(flag);
            flag = true;

            int[] Mu = new int[x_m];
            long start6 = System.nanoTime();
            for (int i = 0; i < x_n; i++) {
                for (int j = 0; j < x_m; j++) {
                    Result[i][j] = y[i][j]-M[i][j];
                }
            }
            long end6 = System.nanoTime();

            double duration = (end6 - start6) / 1000000.0;
            System.out.printf("Solve: %.2f ms\n", duration);

//            long start_solve = System.currentTimeMillis();
//            for (int i = 0; i < x_m; i++) {
//                for (int j = 0; j < x_n; j++) {
//                    Mu[i] += M[i][j] * u[j];
//                }
//            }
//            System.out.println();
//            int[][] MZ = new int[x_m][x_m];
//            for (int i = 0; i < x_m; i++) {
//                for (int j = 0; j < x_m; j++) {
//                    MZ[i][j] = Mu[i] * v[j];
//                }
//            }
//            System.out.println("Y=");
//            for (int i = 0; i < x_m; i++) {
//                for (int j = 0; j < x_m; j++) {
//                    y[i][j] = y[i][j] - MZ[i][j];
////                    System.out.print(y[i][j]+" ");
//                }
////                System.out.println();
//            }
//            long end_solve = System.currentTimeMillis();
//            System.out.println("Solve:" + (end_solve - start_solve) + "ms");
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < x_m; i++) {
            System.out.println(i);
        }
    }

    public static void calculateY() {
        CountDownLatch countDownLatch = new CountDownLatch(x_m);
        ExecutorService pool = Executors.newFixedThreadPool(17);
        for (int i = 0; i < x_m; i++) {
            final int d = i;
            Runnable run = () -> {
                try {
                    for (int j = 0; j < x_m; j++) {
                        y[d][j] = 0;
                        for (int k = 0; k < x_n; k++) {
                            y[d][j] += M[d][k] * x[k][j];
                        }
                    }
                    countDownLatch.countDown();
                } catch (Exception e) {
                }
            };
            pool.execute(run);
        }
        pool.shutdown();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void inputXandZ() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Input Matrix #n#：");
        x_n = sc.nextInt();
        System.out.println("Input Matrix #m#：");
        x_m = sc.nextInt();
        x = new int[x_n][x_m];
        z = new int[x_n][x_m];
        y = new int[x_m][x_m];
        SK_s = new Element[x_m];
        SK_r = new Element[x_n];
        Result = new int[x_n][x_m];
        m = new Element[x_n];
        v = new int[x_m];
        u = new int[x_n];

        M = new int[x_m][x_n];
        for (int i = 0; i < x_m; i++) {
            for (int j = 0; j < x_n; j++) {
                M[i][j] = (int) (1 + Math.random() * (10 - (-10) + 1));
            }
        }

        for (int j = 0; j < x_n; j++) {
            for (int i = 0; i < x_m; i++) {
                x[j][i] = (int) (1 + Math.random() * (10 - (-10) + 1));
            }
        }

        Zgenerator();

        for (int i = 0; i < x_m; i++) {
            for (int j = 0; j < x_m; j++) {
                y[i][j] = 0;
            }
        }
    }

    public static void Zgenerator() {
        int l = 4;
        int p = 3;
        int q = 3;
        int c = (int) Math.pow(2, p);
        System.out.print("v=");
        for (int i = 0; i < x_m; i++) {
            v[i] = (int) (1 + Math.random() * (128 - 16 + 1));
        }
        System.out.println();
        System.out.print("u=");
        for (int i = 0; i < x_n; i++) {
            u[i] = (int) (1 + Math.random() * (8 - (-8) + 1));
        }
        System.out.println();
        for (int i = 0; i < x_n; i++) {
            for (int j = 0; j < x_m; j++) {
                z[i][j] = u[i] * v[j];
            }
        }

    }

    public static void calculateNewX() {
        for (int i = 0; i < x_n; i++) {
            for (int j = 0; j < x_m; j++) {
                x[i][j] = x[i][j] + z[i][j];
            }
        }

    }

    public static boolean isNull() {
        if (x_n == 0 || x_m == 0) {
            return true;
        } else {
            return false;
        }
    }
}

