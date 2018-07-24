public class RougeL {

    private static double GAMMA;
    private double scoreSum;
    private int scoreSize;

    static {
        Properties prop = new Properties();
        try {
            //读取属性文件
            InputStream in = new BufferedInputStream(new FileInputStream("src/args.properties"));
            prop.load(in);  //加载属性列表
            GAMMA = Double.parseDouble(prop.getProperty("GAMMA"));
//            System.out.println("GAMMA: " + GAMMA);
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    /**
     * RougeL算法默认赋值gamma=1.2
     */
    public RougeL() {
        this.scoreSum = 0.0;
        this.scoreSize = 0;
    }

    /**
     * 计算str和sub的最长公共子串的大小
     *
     * @param str 字符串1
     * @param sub 字符串2
     * @return 最长公共子串的size
     */
    public int getLCS(String str, String sub) {
        // receive the length of tow string
        int strLength = str.length();
        int subLength = sub.length();
        // initialize length to sort midterm info
        int[][] lengths = new int[strLength + 1][subLength + 1];
        // compute the longest size store in lengths[strLength + 1][subLength + 1]
        for (int i = 1; i < strLength + 1; i++) {
            for (int j = 1; j < subLength + 1; j++) {
                char strChar = str.charAt(i - 1);
                char subChar = sub.charAt(j - 1);
                if (strChar == subChar) {
                    lengths[i][j] = lengths[i - 1][j - 1] + 1;
                } else {
                    lengths[i][j] = Math.max(lengths[i - 1][j], lengths[i][j - 1]);
                }
            }
        }

        return lengths[strLength][subLength];
    }

    /**
     * 计算分数并添加到InstanceScore中去
     * @param candSent 预测答案句子
     * @param refSent 参考答案句子
     */
    public void addInstance(String candSent, String refSent) {
        // 判断是否为空
        if (refSent == null) {
            return;
        } else if (candSent == null) {
            candSent = "";
        }

        // 获取字符串长度
        int candLength = candSent.length();
        int refsLength = refSent.length();

        double score = 0;
        if (candLength > 0 && refsLength > 0) {
//            System.out.println("reference: " + refSent);
            // 计算最大公共子序列表
            int lcs = getLCS(candSent, refSent);
//            System.out.println("LCS: " + lcs);
            // 计算准确率
            double precs = lcs * 1.0 / candLength;
//            System.out.println("P_LCS: " + lcs + " / " + candLength + " = " + precs);
            // 计算召回率
            double recall = lcs * 1.0 / refsLength;
//            System.out.println("R_LCS: " + lcs + " / " + refsLength + " = " + recall);

            // 计算RougeL分数
            score = (1 + Math.pow(GAMMA, 2)) * recall * precs;
//            System.out.println("score = (1 + GAMMA^2)R_lcs*P_lcs");
//            System.out.println("score = (1 + " + GAMMA +"^2) * " + recall + " * " + precs);
//            System.out.println("score = " + score);
            score /= recall + Math.pow(GAMMA, 2) * precs;
//            System.out.println("score /= R_lcs + GAMMA^2 * P_lcs");
//            System.out.println("score /= " + recall + " " + GAMMA + "^2" + " * " + precs);
//            System.out.println("score: " + score);
        } else if (refsLength == 0) {
            return;
        }

        this.scoreSum += score;
        this.scoreSize++;
    }

    /**
     * 计算预测答案的RougeL分数，并返回
     *
     * @return RougeL分数, 如果句子中存在空的字符串返回null
     */
    public double getScore() {
        return this.scoreSum / this.scoreSize;
    }

    public static void main(String[] args) {
        String candidate = "我是孙维松";
        String refrences = "孙维松是我";

        RougeL rougeLScorer = new RougeL();
        int lcs = rougeLScorer.getLCS(candidate, refrences);
        System.out.println("LCS: " + lcs);

        double score = rougeLScorer.getScore();
        System.out.println("RougeL score: " + score);
    }
}