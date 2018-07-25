# 使用说明

## 引用jar包

- jackson-databind-2.9.5.jar
- jackson-core-2.9.5.jar
- jackson-annotations-2.9.5.jar
- junit-jupiter-api-5.0.0.jar
- junit-platform-commons-1.0.0.jar
- opentest4j-1.0.0.jar
- apiguardian-api-1.0.0.jar

## 可能会出现的错误提示及其原因

- 提交的答案不能为空
- 无法识别article_id键值(key)，JSON文件格式出错
  - 错误原因：
  - 将键值写为article或articleID等，请将键值改为article_id与参考答案格式保持一致
  - 在article_id键值对之前多出了格外无用信息，比如上一篇文章多回答了一个问题，使得jsonParser方法无法找到需要的键值
- article_id不吻合，请检查JSON文件
  - 错误原因：
  - 提交答案中文档的id号码与参考答案不吻合
- 无法识别questions键值，JSON文件格式出错
  - 错误的原因：
  - 将键值写为question或其他错误的字符串，请将键值改为questions(注意复数形式)与参考答案保持一致
  - 在questions键值对之前多出了无用的信息，比如包含article_type或article_title键值对，请确认删除
- questions_id键值不吻合，请检查JSON文件
  - 错误的原因
  - 将键值写为question或questions_id等，请将键值改为question_id与参考答案保持一致
- 答卷question_id不吻合
  - 错误原因：
  - 问题的ID号码与参考答案不一致, 请检查是否按照测试数据的顺序生成json文件
- answer键值存在问题
  - 将键值写为answers或其他错误字符串，请将键值改为answer与参考答案保持一致
- 回答问题不完整
- json文件格式有问题
  - json文件中缺少对应的括号或逗号, 请使用能自动检测json文件错误的编辑器(如vscode)进行修改
  - 如果没有回答某个问题, jsonParser指针会提留在"]"并取出对应的null键值，请检查是否完整回答问题。
