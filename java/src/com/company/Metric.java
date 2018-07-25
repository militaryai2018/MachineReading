package com.company;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Metric {
    private static List<Article> refList = null;
    private static byte[] refCached = new byte[1];

    private static final String ARTICLE_ID = "article_id";
    private static final String QUESTIONS = "questions";
    private static final String QUESTION_ID = "questions_id";
    private static final String ANSWER = "answer";
    public static final String BLEU_SCORE = "bleu_score";
    public static final String ROUGE_SCORE = "rouge_score";

    /**
     * 分别计算RougeL分数和Bleu分数，以ConcurrentHashMap返回
     *
     * @param candFile 预测答案文件
     * @return ConcurrentHashMap返回存储方式{"bleu_log": score1, "rouge_log": score2}
     */
    public static ConcurrentHashMap<String, Double> getScore(File refFile, File candFile) throws RuntimeException {

        if (candFile == null) {
            throw new RuntimeException("提交的答案不能为空");
        }

        if (refFile == null && refList == null) {
            throw new RuntimeException("没有初始化参考答案");
        }
        while (true) {
            if (refFile != null && refList == null) {
                setRefList(refFile);
            }
            if (refList != null) {
                break;
            }
        }

        return scoreCandFile(candFile);
    }

    public static ConcurrentHashMap<String, Double> scoreCandFile(File candFile) {
        // 开始进行统计
        JsonFactory jsonFactory = new JsonFactory();

        // 初始化计分器
        Bleu bleuEval = new Bleu();
        RougeL rougeLEval = new RougeL();
        ConcurrentHashMap<String, Double> scoreMap = new ConcurrentHashMap<>();

        int NEXT_BLOCK_STEP = 3;
        int NEXT_ARTICLE_STEP = 2;
        int NEXT_TOKEN_STEP = 1;

        try {
            JsonParser jsonParser = jsonFactory.createParser(candFile);
            for (Article article :
                    refList) {
                String refArticleID = article.getArticleID();
                // 读取答卷键值
                jsonParser = jump(jsonParser, NEXT_BLOCK_STEP);
                String curName = jsonParser.getCurrentName();
                if (!ARTICLE_ID.equals(curName)) {
                    throw new RuntimeException("无法识别article_id键值，JSON文件格式出错");
                }
                // 读取article_id
                jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                String candArticleID = jsonParser.getText();
                if (!refArticleID.equals(candArticleID)) {
                    throw new RuntimeException("article_id不吻合，请检查JSON文件");
                }
                jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                if (!QUESTIONS.equals(jsonParser.getCurrentName())) {
                    throw new RuntimeException("无法识别questions键值，JSON文件格式出错");
                }
                List<Question> refQuestions = article.getQuestions();
                for (Question refQuestion: refQuestions) {
                    String refQuestionID = refQuestion.getQuestionID();
                    String refAnswer = refQuestion.getAnswer();

                    // 获取candidate question id
                    // 读取答卷键值
                    jsonParser = jump(jsonParser, NEXT_BLOCK_STEP);
                    String candQuestionKey = jsonParser.getCurrentName();
                    if (!QUESTION_ID.equals(candQuestionKey)) {
                        throw new RuntimeException("questions_id键值不吻合，请检查JSON文件");
                    }
                    jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                    String candQuestionID = jsonParser.getText();
                    if (!refQuestionID.equals(candQuestionID)) {
                        throw new RuntimeException("答卷question_id不吻合");
                    }
                    jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                    String candAnswerKey = jsonParser.getCurrentName();
                    if (!ANSWER.equals(candAnswerKey)) {
                        throw new RuntimeException("answer键值不吻合，请检查JSON文件");
                    }
                    jsonParser = jump(jsonParser, NEXT_TOKEN_STEP);
                    String candAnswer = jsonParser.getText();

                    candAnswer = processString(candAnswer);
                    bleuEval.addInstance(candAnswer, refAnswer);
                    rougeLEval.addInstance(candAnswer, refAnswer);
                }
                jsonParser = jump(jsonParser, NEXT_ARTICLE_STEP);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法正确解析答卷JSON文件");
        }

        // 开始进行统计
        scoreMap.put(BLEU_SCORE, bleuEval.getScore());
        scoreMap.put(ROUGE_SCORE, rougeLEval.getScore());
        return scoreMap;
    }

    public static JsonParser jump(JsonParser jsonParser, int jumpStep) {

        int curIDX = 0;
        try {
            while ( curIDX < jumpStep && jsonParser.nextToken() != null) {
                curIDX++;
            }
            if (curIDX != jumpStep) {
                throw new RuntimeException("JSON文件格式出错");
            }
        } catch (IOException e) {
            throw new RuntimeException("无法正确解析答卷JSON文件");
        }
        return jsonParser;
    }

    /**
     * 读取参考答案文件，将参考答案加载到内存中去
     *
     * @param refFile 参考答案文件
     * @throws RuntimeException 读取文件时出错抛出错误
     */
    public synchronized static void setRefList(File refFile) throws RuntimeException {
        // 初始化对象
        if (refList != null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        refList = new ArrayList<>();

        // 读取参考答案数据
        try {
            // 初始化JSON解析方法
            JsonNode rootNode = mapper.readTree(refFile);

            // 初始化数组
            int size = rootNode.size();
            refList = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                JsonNode articleJSON = rootNode.get(i);
                String articleID = articleJSON.path(ARTICLE_ID).asText();
                if (articleID == null) {
                    throw new RuntimeException("参考答案文件不包含article_id键值，请检查该文件");
                }
                JsonNode questionsJson = articleJSON.path(QUESTIONS);
                if (questionsJson == null) {
                    throw new RuntimeException("参考答案文件不包含questions键值");
                }
                int questionSize = questionsJson.size();

                List<Question> questionList = new ArrayList<>(questionSize);
                for (int j = 0; j < questionSize; j++) {
                    JsonNode questionNode = questionsJson.get(j);
                    String questionID = questionNode.path(QUESTION_ID).asText();
                    if (questionID == null) {
                        throw new RuntimeException("参考答案不包含questions_id键值，请检查文件");
                    }
                    String answer = questionNode.path(ANSWER).asText();
                    answer = processString(answer);

                    Question question = new Question(questionID, answer);
                    questionList.add(question);
                }
                Article article = new Article(articleID, questionList);
                refList.add(article);
            }
        } catch (IOException e) {
            throw new RuntimeException("参考答案JSON文件格式有问题，请检查！");
        }

//        System.out.println("加载参考答案完毕！！！");
    }

    /**
     * 将短语中的英文单词转化为单个字母，
     * 请去掉换行符等无用字符
     *
     * @param str 字符串
     * @return 处理后的字符串
     */
    public static String processString(String str) {
        if (str == null || str.equals("")) {
            return str;
        }
        String sub = str.replaceAll("\\s+", "");
        return sub;
    }

    public static void main(String args[]) {

    }
}






