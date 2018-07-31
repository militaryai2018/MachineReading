class Question {
    private String questionID;
    private String questionContent;
    private String answer;
    private String questionType;

    /**
     * 当输入的数据为指标数据的时候时候这个构造函数
     *
     * @param questionID      问题ID号码
     * @param questionContent 问题的具体内容
     * @param answer          问题答案
     * @param questionType    问题类型
     */
    public Question(String questionID, String questionContent, String answer, String questionType) {
        this.questionID = questionID;
        this.questionContent = questionContent;
        this.answer = answer;
        this.questionType = questionType;
    }

    /**
     * 当输入的数据为预测数据时，使用该构造函数
     *
     * @param questionID 问题Id号码
     * @param answer     问题候选答案
     */
    public Question(String questionID, String answer) {
        this.questionID = questionID;
        this.answer = answer;
    }

    public String getQuestionID() {
        return questionID;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public String getQuestionType() {
        return questionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return questionID.equals(question.questionID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(questionID);
    }
}
